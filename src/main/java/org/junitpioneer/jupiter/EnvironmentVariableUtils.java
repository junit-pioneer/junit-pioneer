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

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.platform.commons.PreconditionViolationException;

/**
 * This class modifies the internals of the environment variables map with reflection.
 * Warning: If your {@link SecurityManager} does not allow modifications, it fails.
 */
class EnvironmentVariableUtils {

	private EnvironmentVariableUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Set a value of an environment variable.
	 *
	 * @param name  of the environment variable
	 * @param value of the environment variable
	 */
	public static void set(String name, String value) {
		modifyEnvironmentVariables(map -> map.put(name, value));
	}

	/**
	 * Clear an environment variable.
	 *
	 * @param name of the environment variable
	 */
	public static void clear(String name) {
		modifyEnvironmentVariables(map -> map.remove(name));
	}

	private static void modifyEnvironmentVariables(Consumer<Map<String, String>> consumer) {
		try {
			setInProcessEnvironmentClass(consumer);
		}
		catch (ReflectiveOperationException ex) {
			trySystemEnvClass(consumer, ex);
		}
	}

	private static void trySystemEnvClass(Consumer<Map<String, String>> consumer,
			ReflectiveOperationException processEnvironmentClassEx) {
		try {
			setInSystemEnvClass(consumer);
		}
		catch (ReflectiveOperationException ex) {
			ex.addSuppressed(processEnvironmentClassEx);
			throw new PreconditionViolationException("Could not modify environment variables", ex);
		}
	}

	/*
	 * Works on Windows
	 */
	private static void setInProcessEnvironmentClass(Consumer<Map<String, String>> consumer)
			throws ReflectiveOperationException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		// The order of operations is critical here: On some operating systems, theEnvironment is present but
		// theCaseInsensitiveEnvironment is not present. In such cases, this method must throw a
		// ReflectiveOperationException without modifying theEnvironment. Otherwise, the contents of theEnvironment will
		// be corrupted. For this reason, both fields are fetched by reflection before either field is modified.
		Map<String, String> theEnvironment = getFieldValue(processEnvironmentClass, null, "theEnvironment");
		Map<String, String> theCaseInsensitiveEnvironment = getFieldValue(processEnvironmentClass, null,
			"theCaseInsensitiveEnvironment");
		consumer.accept(theEnvironment);
		consumer.accept(theCaseInsensitiveEnvironment);
	}

	/*
	 * Works on Linux and OSX
	 */
	private static void setInSystemEnvClass(Consumer<Map<String, String>> consumer)
			throws ReflectiveOperationException {
		Map<String, String> env = System.getenv(); //NOSONAR access required to implement the extension
		consumer.accept(getFieldValue(env.getClass(), env, "m"));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getFieldValue(Class<?> clazz, Object object, String name)
			throws ReflectiveOperationException {
		Field field = clazz.getDeclaredField(name);
		try {
			field.setAccessible(true); //NOSONAR illegal access required to implement the extension
		}
		catch (InaccessibleObjectException ex) {
			throw new PreconditionViolationException(
				"Cannot access and modify JDK internals to modify environment variables. "
						+ "Have a look at the documentation for possible solutions: "
						+ "https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access",
				ex);
		}
		return (Map<String, String>) field.get(object);
	}

}
