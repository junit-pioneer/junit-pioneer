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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * <p>For details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
 * </p>
 */
public class StdIoExtension implements ParameterResolver, BeforeTestExecutionCallback, AfterEachCallback {

	private static final String SEPARATOR = System.getProperty("line.separator");

	private static final Namespace NAMESPACE = Namespace.create(DefaultLocaleExtension.class);

	private static final String IN_KEY = "StdIo_In";
	private static final String OUT_KEY = "StdIo_Out";

	@Override
	public void afterEach(ExtensionContext context) {
		System.setIn(context.getStore(NAMESPACE).get(IN_KEY, InputStream.class)); //NOSONAR resetting input
		System.setOut(context.getStore(NAMESPACE).get(OUT_KEY, PrintStream.class));
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> type = parameterContext.getParameter().getType();
		return (type == StdIn.class || type == StdOut.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		if (parameterType == StdOut.class) {
			storeStdOut(extensionContext);
			return getOut();
		}
		String[] source = getSourceValuesFromAnnotation(extensionContext);
		storeStdIn(extensionContext);
		return getIn(source);
	}

	private void storeStdIn(ExtensionContext context) {
		context.getStore(NAMESPACE).put(IN_KEY, System.in); //NOSONAR never reading from System.in, only storing it
	}

	private Object getIn(String[] source) {
		StdIn in = new StdIn(source);
		System.setIn(in); //NOSONAR required to redirect output
		return in;
	}

	private void storeStdOut(ExtensionContext context) {
		context.getStore(NAMESPACE).put(OUT_KEY, System.out); //NOSONAR never writing to System.out, only storing it
	}

	private Object getOut() {
		StdOut out = new StdOut();
		System.setOut(new PrintStream(out));
		return out;
	}

	private String[] getSourceValuesFromAnnotation(ExtensionContext context) {
		return context.getRequiredTestMethod().getAnnotation(StdIo.class).value();
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		Method method = context.getRequiredTestMethod();
		if (method.getAnnotation(StdIo.class) == null) {
			throw new ExtensionConfigurationException(
				format("StdIoExtension is active but no %s annotation was found.", StdIo.class.getName()));
		}
		List<Class<?>> params = Arrays.asList(method.getParameterTypes());
		if (!params.contains(StdIn.class) && !params.contains(StdOut.class)) {
			throw new ExtensionConfigurationException(
				format("Method is annotated with %s but no %s or %s parameters were found.", StdIo.class.getName(),
					StdIn.class.getName(), StdOut.class.getName()));
		}
		if (!String.join("", method.getAnnotation(StdIo.class).value()).isEmpty() && !params.contains(StdIn.class)) {
			throw new ExtensionConfigurationException(format(
				"Method has no %s parameter but input sources were provided in the %s annotation (Did you forget to add a test parameter?).",
				StdIn.class.getName(), StdIo.class.getName()));
		}
	}

	/**
	 * <p>For details and examples, see
	 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
	 * </p>
	 */
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

	/**
	 * <p>For details and examples, see
	 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
	 * </p>
	 */
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
