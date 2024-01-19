package com.jeroensteenbeeke.andalite.recipes.jsr305;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import  com.jeroensteenbeeke.lux.ActionResult;
import com.jeroensteenbeeke.lux.TypedResult;
import com.jeroensteenbeeke.andalite.java.analyzer.AccessModifier;
import com.jeroensteenbeeke.andalite.java.analyzer.AnalyzedClass;
import com.jeroensteenbeeke.andalite.java.analyzer.ClassAnalyzer;

public class ScanAndRemoveNewlines extends JavaFilesAction {
	private static final String LINEBREAK = "\n";
	private static final Set<String> JSR305 = ImmutableSet.<String>builder().add("@CheckForNull").add("@Nullable")
			.add("@Nonnull").build();

	private static final int LINEFEED = 10;

	private static final int CARRIAGE_RETURN = 13;

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

				}).forEach(af -> {
					File file = af.getOriginalFile();

					LinkedList<String> fileContents = Lists.newLinkedList();
					LinkedList<String> newContents = Lists.newLinkedList();

					try (FileReader fr = new FileReader(file)) {
						LinkedList<Integer> buffer = Lists.newLinkedList();

						int in = -1;
						while ((in = fr.read()) != -1) {
							buffer.add(in);

							if (in == LINEFEED) {
								buffer.removeLast();
								String lineEnding;

								if (!buffer.isEmpty() && buffer.getLast() == CARRIAGE_RETURN) {
									buffer.removeLast();
									lineEnding = "\r\n";
								} else {
									lineEnding = "\n";
								}

								fileContents.add(clearBuffer(buffer, lineEnding));
							}
						}

						if (!buffer.isEmpty()) {
							fileContents.add(clearBuffer(buffer, ""));
						}

						String prev = "";
						for (String line : fileContents) {
							if (prev != null && prev.trim().isEmpty()) {
								if (JSR305.contains(line.trim())) {
									newContents.removeLast();
								}
							} else if (line.trim().isEmpty() && prev != null && JSR305.contains(prev.trim())) {
								continue;
							}
							
							newContents.add(line);
							prev = line;
						}

					} catch (IOException e) {
						errors.add(e.getMessage());
					}

					try (final FileOutputStream out = new FileOutputStream(file)) {
						Charset utf8 = Charset.forName("UTF-8");

						for (String l : newContents) {
							out.write(l.getBytes(utf8));
						}
						out.flush();
					} catch (IOException e) {
						errors.add(e.getMessage());
					}

				});

		if (!errors.isEmpty()) {
			return ActionResult.error(errors.stream().collect(Collectors.joining(", ")));
		}

		return ActionResult.ok();
	}

	private String clearBuffer(LinkedList<Integer> buffer, String lineEnding) {
		StringBuilder builder = new StringBuilder();
		for (Integer i : buffer) {
			builder.append((char) i.intValue());
		}

		builder.append(lineEnding);
		buffer.clear();
		String string = builder.toString();
		return string;
	}

	public static String rtrim(String s) {
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}
}
