/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

class StdIoExtension implements ParameterResolver, BeforeTestExecutionCallback {

	private static final String SEPARATOR = System.getProperty("line.separator");

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> type = parameterContext.getParameter().getType();
		return (type == StdIn.class || type == StdOut.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {

		Class<?> parameterType = parameterContext.getParameter().getType();
		if (parameterType == StdOut.class) {
			return getOut();
		}
		String[] source = getSourceValuesFromAnnotation(extensionContext);
		return getIn(source);
	}

	private Object getIn(String[] source) {
		StdIn in = new StdIn(source);
		System.setIn(in); //NOSONAR required to redirect output
		return in;
	}

	private Object getOut() {
		StdOut out = new StdOut();
		System.setOut(new PrintStream(out));
		return out;
	}

	private String[] getSourceValuesFromAnnotation(ExtensionContext context) {
		return context
				.getTestMethod()
				.map(method -> method.getAnnotation(StdIo.class))
				.map(StdIo::value)
				.orElseThrow(() -> new ParameterResolutionException(
					format("Can not resolve test method parameter %s. Method has to be annotated with '%s'.",
						StdIn.class.getName(), StdIo.class.getName())));
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		Optional<Method> method = context.getTestMethod();
		if (method.isPresent()) {
			if (method.get().getAnnotation(StdIo.class) == null) {
				throw new ExtensionConfigurationException(
					format("StdIoExtension is active but no %s annotation was found.", StdIo.class.getName()));
			}
			List<Class<?>> params = Arrays.asList(method.get().getParameterTypes());
			if (!params.contains(StdIn.class) && !params.contains(StdOut.class)) {
				throw new ExtensionConfigurationException(
					format("Method is annotated with %s but no %s or %s parameters were found.", StdIo.class.getName(),
						StdIn.class.getName(), StdOut.class.getName()));
			}
			if (!String.join("", method.get().getAnnotation(StdIo.class).value()).isEmpty()
					&& !params.contains(StdIn.class)) {
				throw new ExtensionConfigurationException(format(
					"Method has no %s parameter but input sources were provided in the %s annotation (Did you forget to add a test parameter?).",
					StdIn.class.getName(), StdIo.class.getName()));
			}
		}
	}

	public static class StdOut extends OutputStream {

		Writer writer = new StringWriter();

		@Override
		public void write(int i) throws IOException {
			writer.write(i);
		}

		@Override
		public String toString() {
			return writer.toString();
		}

		public String[] capturedLines() {
			return this.toString().split(SEPARATOR);
		}

	}

	public static class StdIn extends InputStream {

		Reader reader;
		Writer writer = new StringWriter();

		public StdIn(String... values) {
			reader = new StringReader(String.join(SEPARATOR, values));
		}

		@Override
		public int read() throws IOException {
			int reading = reader.read();
			if (reading != -1) {
				writer.write(reading);
			}
			return reading;
		}

		@Override
		public String toString() {
			return writer.toString();
		}

		public String[] capturedLines() {
			return this.toString().split(SEPARATOR);
		}

	}

}
