package com.jeroensteenbeeke.andalite.recipes.jsr305;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import  com.jeroensteenbeeke.lux.ActionResult;
import com.jeroensteenbeeke.lux.TypedResult;
import com.jeroensteenbeeke.andalite.forge.ui.actions.JavaTransformation;
import com.jeroensteenbeeke.andalite.java.analyzer.AccessModifier;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedClass;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedMethod;
import com.jeroensteenbeeke.andalite.java.analyzer.ClassAnalyzer;
import com.jeroensteenbeeke.andalite.java.transformation.ClassScopeOperationBuilder;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipe;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipeBuilder;
import com.jeroensteenbeeke.andalite.java.transformation.MethodOperationBuilder;
import com.jeroensteenbeeke.andalite.java.transformation.ParameterScopeOperationBuilder;

public class RemoveEntityJSR305 extends JavaFilesAction {
	public ActionResult perform() {
		List<String> errors = Collections.synchronizedList(new LinkedList<String>());

		findJavaFiles(new File(System.getProperty("user.dir"))).stream().parallel().map(ClassAnalyzer::new)
				.map(ClassAnalyzer::analyze).filter(r -> r.isOk()).map(TypedResult::getObject).filter(ac -> {
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
		JavaRecipeBuilder java = new JavaRecipeBuilder();

		AnalyzedMethod getter = descriptor.getGetter();
		AnalyzedMethod setter = descriptor.getSetter();

		ClassScopeOperationBuilder clazz = java.inPublicClass();
		if (setter != null) {
			MethodOperationBuilder setterScope = clazz.forMethod().withModifier(AccessModifier.PUBLIC)
					.withReturnType("void").withParameterOfType(descriptor.getField().getType().toJavaString())
					.named(setter.getName());
			ParameterScopeOperationBuilder firstParameter = setterScope.forParameterAtIndex(0);

			firstParameter.removeAnnotation("Nonnull");
			firstParameter.removeAnnotation("CheckForNull");
			firstParameter.removeAnnotation("Nullable");
		}
		if (getter != null) {
			MethodOperationBuilder getterScope = clazz.forMethod().withModifier(AccessModifier.PUBLIC)
					.withReturnType(descriptor.getField().getType().toJavaString()).named(getter.getName());
			getterScope.removeAnnotation("Nonnull");
			getterScope.removeAnnotation("CheckForNull");
			getterScope.removeAnnotation("Nullable");
		}

		return java.build();
	}
}
