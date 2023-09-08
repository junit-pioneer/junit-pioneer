/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static org.junitpioneer.internal.PioneerAnnotationUtils.findClosestEnclosingAnnotation;

import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

class StdIoExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

	static final String SEPARATOR = System.getProperty("line.separator");

	private static final Namespace NAMESPACE = Namespace.create(StdIoExtension.class);

	private static final String SYSTEM_IN_KEY = "StdIo_System_In";
	private static final String SYSTEM_OUT_KEY = "StdIo_System_Out";
	private static final String SYSTEM_ERR_KEY = "StdIo_System_Err";
	private static final String STD_IN_KEY = "StdIo_Std_In";

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> type = parameterContext.getParameter().getType();
		return (type == StdIn.class || type == StdOut.class || type == StdErr.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		if (parameterType == StdOut.class)
			return prepareStdOut(extensionContext);
		if (parameterType == StdErr.class)
			return prepareStdErr(extensionContext);
		if (parameterType == StdIn.class) {
			String[] source = extensionContext.getRequiredTestMethod().getAnnotation(StdIo.class).value();
			if (source.length == 0)
				throw new ExtensionConfigurationException(
					"@StdIo defined no input, so System.in is still in place and no StdIn parameter can be provided. "
							+ "If you want to define empty input, use `@StdIo(\"\")`.");
			else
				//@formatter:off
				return extensionContext
						.getStore(NAMESPACE)
						.getOrComputeIfAbsent(
								STD_IN_KEY,
								__ -> createSwapStoreStdIn(extensionContext, source),
								StdIn.class);
				//@formatter:on
		}
		throw new ParameterResolutionException(format("Could not resolve parameter of type %s.", parameterType));
	}

	private StdOut prepareStdOut(ExtensionContext context) {
		storeStdOut(context);
		return createOut();
	}

	private void storeStdOut(ExtensionContext context) {
		context.getStore(NAMESPACE).put(SYSTEM_OUT_KEY, System.out); //NOSONAR never writing to System.out, only storing it
	}

	private StdOut createOut() {
		StdOut out = new StdOut();
		System.setOut(new PrintStream(out));
		return out;
	}

	private StdIn createSwapStoreStdIn(ExtensionContext context, String[] source) {
		StdIn newStdIn = new StdIn(source);
		swapAndStoreIn(context, newStdIn);
		return newStdIn;
	}

	private void swapAndStoreIn(ExtensionContext context, StdIn stdIn) {
		context.getStore(NAMESPACE).put(SYSTEM_IN_KEY, System.in); //NOSONAR never reading from System.in, only storing it
		context.getStore(NAMESPACE).put(STD_IN_KEY, stdIn);
		System.setIn(stdIn); //NOSONAR required to redirect output
	}

	private StdErr prepareStdErr(ExtensionContext context) {
		storeStdErr(context);
		return createErr();
	}

	private void storeStdErr(ExtensionContext context) {
		context.getStore(NAMESPACE).put(SYSTEM_ERR_KEY, System.err); //NOSONAR never writing to System.err, only storing it
	}

	private StdErr createErr() {
		StdErr err = new StdErr();
		System.setErr(new PrintStream(err));
		return err;
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		String[] source = findClosestEnclosingAnnotation(context, StdIo.class)
				.orElseThrow(() -> new ExtensionConfigurationException(
					format("StdIoExtension is active but no %s annotation was found.", StdIo.class.getName())))
				.value();
		boolean testMethodIsParameterless = context.getRequiredTestMethod().getParameterCount() == 0;
		if (source.length == 0 && testMethodIsParameterless)
			throw new ExtensionConfigurationException(
				"StdIoExtension is active but neither System.out or System.in are getting redirected.");

		boolean stdInStillInPlace = context.getStore(NAMESPACE).get(STD_IN_KEY) == null;
		if (source.length > 0 && stdInStillInPlace)
			createSwapStoreStdIn(context, source);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		// only reset those streams that were actually stored in "before"

		InputStream storedSystemIn = context.getStore(NAMESPACE).get(SYSTEM_IN_KEY, InputStream.class);
		if (storedSystemIn != null)
			System.setIn(storedSystemIn); //NOSONAR resetting input

		PrintStream storedSystemOut = context.getStore(NAMESPACE).get(SYSTEM_OUT_KEY, PrintStream.class);
		if (storedSystemOut != null)
			System.setOut(storedSystemOut); //NOSONAR resetting input

		PrintStream storedSystemErr = context.getStore(NAMESPACE).get(SYSTEM_ERR_KEY, PrintStream.class);
		if (storedSystemErr != null)
			System.setErr(storedSystemErr); //NOSONAR resetting input
	}

}
