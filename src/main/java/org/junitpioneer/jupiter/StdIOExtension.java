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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * <p>For details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
 * </p>
 */
public class StdIOExtension implements ParameterResolver {

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
				.map(method -> method.getAnnotation(StdInSource.class))
				.map(StdInSource::value)
				.orElseThrow(() -> new ParameterResolutionException(
					format("Can not resolve test method parameter %s. Method has to be annotated with '%s'",
						StdIn.class.getName(), StdInSource.class.getName())));
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
