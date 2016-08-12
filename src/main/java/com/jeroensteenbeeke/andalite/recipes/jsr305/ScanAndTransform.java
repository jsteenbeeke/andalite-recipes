package com.jeroensteenbeeke.andalite.recipes.jsr305;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import com.jeroensteenbeeke.andalite.core.ActionResult;
import com.jeroensteenbeeke.andalite.core.TypedActionResult;
import com.jeroensteenbeeke.andalite.forge.ui.actions.JavaTransformation;
import com.jeroensteenbeeke.andalite.java.analyzer.AccessModifier;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedClass;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedMethod;
import com.jeroensteenbeeke.andalite.java.analyzer.ClassAnalyzer;
import com.jeroensteenbeeke.andalite.java.analyzer.types.Primitive;
import com.jeroensteenbeeke.andalite.java.transformation.ClassLocator;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipe;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipeBuilder;
import com.jeroensteenbeeke.andalite.java.transformation.Operations;

public class ScanAndTransform extends JavaFilesAction {
	public ActionResult perform() {
		List<String> errors = Collections.synchronizedList(new LinkedList<String>());

		findJavaFiles(new File(System.getProperty("user.dir"))).stream().parallel().map(ClassAnalyzer::new)
				.map(ClassAnalyzer::analyze).filter(r -> r.isOk()).map(TypedActionResult::getObject).filter(ac -> {
					for (AnalyzedClass cl : ac.getClasses()) {
						if (cl.getAccessModifier() == AccessModifier.PUBLIC) {
							if (cl.hasAnnotation("Entity") || cl.hasAnnotation("MappedSuperclass")) {
								return true;
							}
						}
					}

					return false;

				}).flatMap(this::findProperties).map(pd -> {
					JavaRecipe recipe = toRecipe(pd);

					return recipe != null ? new JavaTransformation(pd.getSourceFile().getOriginalFile(), recipe) : null;

				}).filter(Objects::nonNull).forEach(t -> {
					ActionResult result = t.perform();

					if (!result.isOk()) {
						errors.add(result.getMessage());
					}
				});

		if (!errors.isEmpty()) {
			return ActionResult.error(errors.stream().collect(Collectors.joining(", ")));
		}

		return ActionResult.ok();
	}

	@CheckForNull
	public JavaRecipe toRecipe(PropertyDescriptor descriptor) {
		if (descriptor.getField().getType() instanceof Primitive) {
			// Ignore primitives in transformation. They cannot be null so need
			// not be annotated accordingly
			return null;
		}

		JavaRecipeBuilder builder = new JavaRecipeBuilder();

		if (descriptor.isNullable()) {
			builder.atRoot().ensure(Operations.imports("javax.annotation.CheckForNull"));
			builder.atRoot().ensure(Operations.imports("javax.annotation.Nullable"));
		} else {
			builder.atRoot().ensure(Operations.imports("javax.annotation.Nonnull"));
		}

		AnalyzedMethod setter = descriptor.getSetter();
		if (setter != null) {
			builder.inClass(ClassLocator.publicClass()).forMethod().withModifier(AccessModifier.PUBLIC)
					.withReturnType("void").withParameterOfType(descriptor.getField().getType().toJavaString())
					.named(setter.getName()).forParameterAtIndex(0)
					.ensure(Operations.hasParameterAnnotation(descriptor.isNullable() ? "Nullable" : "Nonnull"));
		}
		AnalyzedMethod getter = descriptor.getGetter();
		if (getter != null) {
			builder.inClass(ClassLocator.publicClass()).forMethod().withModifier(AccessModifier.PUBLIC)
					.withReturnType(descriptor.getField().getType().toJavaString())
					.named(getter.getName())
					.ensure(Operations.hasMethodAnnotation(descriptor.isNullable() ? "CheckForNull" : "Nonnull"));
		}

		return builder.build();
	}

}
