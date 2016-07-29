package com.jeroensteenbeeke.andalite.recipes.jsr305;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.annotation.CheckForNull;

import com.jeroensteenbeeke.andalite.core.ActionResult;
import com.jeroensteenbeeke.andalite.core.TypedActionResult;
import com.jeroensteenbeeke.andalite.forge.ui.PerformableAction;
import com.jeroensteenbeeke.andalite.forge.ui.actions.JavaTransformation;
import com.jeroensteenbeeke.andalite.java.analyzer.AccessModifier;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedAnnotation;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedClass;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedField;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedMethod;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedSourceFile;
import com.jeroensteenbeeke.andalite.java.analyzer.ClassAnalyzer;
import com.jeroensteenbeeke.andalite.java.analyzer.annotation.BooleanValue;
import com.jeroensteenbeeke.andalite.java.transformation.ClassLocator;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipe;
import com.jeroensteenbeeke.andalite.java.transformation.JavaRecipeBuilder;
import com.jeroensteenbeeke.andalite.java.transformation.Operations;

public class ScanAndTransform implements PerformableAction {
	public ActionResult perform() {
		List<JavaTransformation> transformations = findJavaFiles(new File(System.getProperty("user.dir")))
				.stream()
				.parallel()
				.map(ClassAnalyzer::new)
				.map(ClassAnalyzer::analyze)
				.filter(r -> r.isOk())
				.map(TypedActionResult::getObject)
				.filter(ac -> {
					for (AnalyzedClass cl : ac.getClasses()) {
						if (cl.getAccessModifier() == AccessModifier.PUBLIC) {
							if (cl.hasAnnotation("Entity")
									|| cl.hasAnnotation("MappedSuperclass")) {
								return true;
							}
						}
					}

					return false;

				}).flatMap(this::findProperties)
				.map(pd -> {
					return new JavaTransformation(pd.getSourceFile().getOriginalFile(), pd.toRecipe());
					
				})
				.collect(Collectors.toList());
		
		for (JavaTransformation trans: transformations) {
			ActionResult result = trans.perform();
			if (!result.isOk()) {
				return result;
			}
		}
		

		return ActionResult.ok();
	}

	public List<File> findJavaFiles(File base) {
		List<File> files = new ArrayList<>();

		if (base.isDirectory()) {
			for (File file : base.listFiles()) {
				files.addAll(findJavaFiles(file));
			}
		} else if (base.getName().endsWith(".java")) {
			files.add(base);
		}

		return files;
	}

	public Stream<PropertyDescriptor> findProperties(AnalyzedSourceFile file) {
		Builder<PropertyDescriptor> builder = Stream.builder();

		for (AnalyzedClass cl : file.getClasses()) {
			if (cl.getAccessModifier() == AccessModifier.PUBLIC) {
				if (cl.hasAnnotation("Entity")
						|| cl.hasAnnotation("MappedSuperclass")) {

					for (AnalyzedField analyzedField : cl.getFields()) {
						Boolean nullable = null;
						if (analyzedField.hasAnnotation("Column")) {
							AnalyzedAnnotation annotation = analyzedField
									.getAnnotation("Column");
							if (annotation.hasValueNamed("nullable")) {
								BooleanValue booleanValue = annotation
										.getValue(BooleanValue.class,
												"nullable");
								if (booleanValue.getValue()) {
								} else {
									nullable = false;
								}
							} else {
								nullable = true;
							}

						} else if (analyzedField.hasAnnotation("ManyToOne")) {
							AnalyzedAnnotation annotation = analyzedField
									.getAnnotation("ManyToOne");
							if (annotation.hasValueNamed("optional")) {
								BooleanValue booleanValue = annotation
										.getValue(BooleanValue.class,
												"optional");
								if (booleanValue.getValue()) {
									nullable = true;
								} else {
									nullable = false;
								}
							} else {
								nullable = true;
							}
						}

						if (nullable != null) {
							builder.add(new PropertyDescriptor(analyzedField,
									file, cl.getClassName(), nullable));
						}
					}

				}
			}
		}

		return builder.build();
	}

	private static class PropertyDescriptor {
		private final AnalyzedField field;

		private final AnalyzedSourceFile sourceFile;

		private final String className;

		private final boolean nullable;

		private final AnalyzedMethod getter;

		private final AnalyzedMethod setter;

		public PropertyDescriptor(AnalyzedField field,
				AnalyzedSourceFile sourceFile, String className,
				boolean nullable) {
			super();
			this.field = field;
			this.sourceFile = sourceFile;
			this.className = className;
			this.nullable = nullable;

			AnalyzedClass classDescriptor = sourceFile
					.getClasses()
					.stream()
					.filter(c -> c.getClassName().equals(className))
					.findFirst()
					.orElseThrow(
							() -> new IllegalStateException(
									"Class not contained in source file"));

			final String firstCapitalized = capitalizeFirst(field.getName());

			this.getter = classDescriptor.getMethod()
					.withModifier(AccessModifier.PUBLIC)
					.withReturnType(field.getType().toJavaString())
					.named("get".concat(firstCapitalized));
			this.setter = classDescriptor.getMethod()
					.withModifier(AccessModifier.PUBLIC).withReturnType("void")
					.withParameterOfType(field.getType().toJavaString())
					.named("set".concat(firstCapitalized));

		}

		@CheckForNull
		public AnalyzedMethod getGetter() {
			return getter;
		}

		@CheckForNull
		public AnalyzedMethod getSetter() {
			return setter;
		}

		public AnalyzedSourceFile getSourceFile() {
			return sourceFile;
		}

		public JavaRecipe toRecipe() {
			final String firstCapitalized = capitalizeFirst(field.getName());

			JavaRecipeBuilder builder = new JavaRecipeBuilder();
			if (nullable) {
				builder.atRoot().ensure(Operations.imports("javax.annotation.CheckForNull"));
				builder.atRoot().ensure(Operations.imports("javax.annotation.Nullable"));
			} else {
				builder.atRoot().ensure(Operations.imports("javax.annotation.Nonnull"));
			}
			
			if (setter != null) {
				builder.inClass(ClassLocator.publicClass())
						.forMethod()
						.withModifier(AccessModifier.PUBLIC)
						.withReturnType("void")
						.withParameterOfType(field.getType().toJavaString())
						.named("set".concat(firstCapitalized))
						.forParameterAtIndex(0)
						.ensure(Operations
								.hasParameterAnnotation(nullable ? "Nullable"
										: "Nonnull"));
			}
			if (getter != null) {
				builder.inClass(ClassLocator.publicClass()).forMethod().withModifier(AccessModifier.PUBLIC)
				.withReturnType(field.getType().toJavaString())
				.named("get".concat(firstCapitalized)).ensure(Operations.hasMethodAnnotation(nullable ? "CheckForNull"
										: "Nonnull"));
			}

			return builder.build();
		}

		@Override
		public String toString() {
			return String.format("%s.%s -> %b (getter: %b, setter: %b)",
					className, field.getName(), nullable, getter != null,
					setter != null);
		}

	}

	public static String capitalizeFirst(String name) {
		return name.substring(0, 1).toUpperCase().concat(name.substring(1));
	}
}
