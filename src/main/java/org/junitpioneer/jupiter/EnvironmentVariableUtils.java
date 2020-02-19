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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class modifies the internals of the environment variables map with reflection.
 * Warning: If your {@link SecurityManager} does not allow modifications, it fails.
 */
public class EnvironmentVariableUtils {

	/**
	 * Set the values of an environment variables.
	 *
	 * @param entries with name and new value of the environment variables
	 */
	public static void set(Map<String, String> entries) {
		modifyEnvironmentVariables(map -> map.putAll(entries));
	}

	/**
	 * Clears environment variables.
	 *
	 * @param names of the environment variables.
	 */
	public static void clear(Collection<String> names) {
		modifyEnvironmentVariables(map -> names.forEach(map::remove));
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
			tryProcessEnvironmentClassFallbackSystemEnvClass(consumer);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException("Could not modify environment variables");
		}
	}

	private static void tryProcessEnvironmentClassFallbackSystemEnvClass(Consumer<Map<String, String>> consumer)
			throws NoSuchFieldException {
		try {
			setInProcessEnvironmentClass(consumer);
		}
		catch (NoSuchFieldException | ClassNotFoundException e) {
			setInSystemEnvClass(consumer);
		}
	}

	/*
	 * Works on Windows
	 */
	private static void setInProcessEnvironmentClass(Consumer<Map<String, String>> consumer)
			throws ClassNotFoundException, NoSuchFieldException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		consumer.accept(getFieldValue(processEnvironmentClass, null, "theEnvironment"));
		consumer.accept(getFieldValue(processEnvironmentClass, null, "theCaseInsensitiveEnvironment"));
	}

	/*
	 * Works on Linux
	 */
	private static void setInSystemEnvClass(Consumer<Map<String, String>> consumer) throws NoSuchFieldException {
		Map<String, String> env = System.getenv();
		consumer.accept(getFieldValue(env.getClass(), env, "m"));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getFieldValue(Class<?> clazz, Object object, String name)
			throws NoSuchFieldException {
		Field field = clazz.getDeclaredField(name);
		try {
			field.setAccessible(true);
			return (Map<String, String>) field.get(object);
		}
		catch (IllegalAccessException e) {
			boolean staticField = Modifier.isStatic(field.getModifiers());
			throw new RuntimeException(
				"Cannot access " + (staticField ? "static " : "") + "field " + clazz.getName() + "." + name, e);
		}
	}

}
