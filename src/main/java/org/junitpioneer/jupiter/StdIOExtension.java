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

public class StdIOExtension implements ParameterResolver {

	private static final String SEPARATOR = System.getProperty("line.separator");

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(StdIntercept.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		StdIntercept stdIntercept = getInterceptAnnotation(parameterContext);
		if (parameterType == StdOut.class) {
			return new StdOut();
		}
		if (parameterType == StdIn.class) {
			return new StdIn(stdIntercept.value());
		}
		throw new ParameterResolutionException("Can only resolve parameter of type " + StdOut.class.getName() + " or "
				+ StdIn.class.getName() + " but was: " + parameterType.getName());

	}

	private StdIntercept getInterceptAnnotation(ParameterContext context) {
		return context.findAnnotation(StdIntercept.class).orElseThrow(() -> new ParameterResolutionException("Needs to be annotated with StdIntercept"));
	}

	public static class StdOut extends OutputStream {

		Writer writer = new StringWriter();

		public StdOut() {
			System.setOut(new PrintStream(this));
		}

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
			System.setIn(this);
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
