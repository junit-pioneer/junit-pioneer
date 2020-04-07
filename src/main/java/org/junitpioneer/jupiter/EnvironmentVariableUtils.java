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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;

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
	 * Set the values of an environment variables.
	 *
	 * @param entries with name and new value of the environment variables
	 */
	public static void set(Map<String, String> entries) {
		modifyEnvironmentVariables(map -> map.putAll(entries));
	}

	/**
	 * Clear an environment variable.
	 *
	 * @param name of the environment variable
	 */
	public static void clear(String name) {
		modifyEnvironmentVariables(map -> map.remove(name));
	}

	/**
	 * Clears environment variables.
	 *
	 * @param names of the environment variables.
	 */
	public static void clear(Collection<String> names) {
		modifyEnvironmentVariables(map -> names.forEach(map::remove));
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
			throw new ExtensionConfigurationException("Could not modify environment variables", ex);
		}
	}

	/*
	 * Works on Windows
	 */
	private static void setInProcessEnvironmentClass(Consumer<Map<String, String>> consumer)
			throws ReflectiveOperationException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		consumer.accept(getFieldValue(processEnvironmentClass, null, "theEnvironment"));
		consumer.accept(getFieldValue(processEnvironmentClass, null, "theCaseInsensitiveEnvironment"));
	}

	/*
	 * Works on Linux and OSX
	 */
	private static void setInSystemEnvClass(Consumer<Map<String, String>> consumer)
			throws ReflectiveOperationException {
		Map<String, String> env = System.getenv(); // NOSONAR access required to implement the extension
		consumer.accept(getFieldValue(env.getClass(), env, "m"));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getFieldValue(Class<?> clazz, Object object, String name)
			throws ReflectiveOperationException {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true); // NOSONAR illegal access required to implement the extension
		return (Map<String, String>) field.get(object);
	}

}
