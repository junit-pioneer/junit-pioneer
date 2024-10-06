/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.junit.platform.commons.support.ReflectionSupport.findMethod;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Pioneer-internal utility class.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 *
 * @see PioneerAnnotationUtils
 */
public class PioneerUtils {

	private PioneerUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * A {@link java.util.stream.Collectors#toSet() toSet} collector that throws an {@link IllegalStateException}
	 * on duplicate elements (according to {@link Object#equals(Object) equals}).
	 */
	public static <T> Collector<T, Set<T>, Set<T>> distinctToSet() {
		return Collector.of(HashSet::new, PioneerUtils::addButThrowIfDuplicate, (left, right) -> {
			right.forEach(element -> addButThrowIfDuplicate(left, element));
			return left;
		});
	}

	private static <T> void addButThrowIfDuplicate(Set<T> set, T element) {
		boolean newElement = set.add(element);
		if (!newElement) {
			throw new IllegalStateException("Duplicate element '" + element + "'.");
		}
	}

	/**
	 * Find the first {@link Method} of the supplied class or interface that
	 * meets the specified criteria, beginning with the specified class or
	 * interface and traversing its enclosing classes until such a method is
	 * found or the top level class is reached.
	 *
	 * <p>The algorithm does not search for methods in {@link java.lang.Object}.</p>
	 *
	 * @param clazz the class or interface in which to find the method; never {@code null}
	 * @param methodName the name of the method to find; never {@code null} or empty
	 * @param parameterTypes the types of parameters accepted by the method, if any;
	 * never {@code null}
	 * @return an {@code Optional} containing the method found; never {@code null}
	 * but potentially empty if no such method could be found
	 * @see org.junit.platform.commons.support.ReflectionSupport#findMethod(Class, String, Class...)
	 */
	public static Optional<Method> findMethodCurrentOrEnclosing(Class<?> clazz, String methodName,
			Class<?>... parameterTypes) {
		Class<?> current = clazz;
		Optional<Method> method;
		do {
			// null checking done by ReflectionSupport.findMethod
			method = findMethod(current, methodName, parameterTypes);
			current = current.getEnclosingClass();
		} while (method.isEmpty() && current != null);
		return method;
	}

	/**
	 * Find all (parent) {@code ExtensionContext}s via {@link ExtensionContext#getParent()}.
	 *
	 * @param context the context for which to find all (parent) contexts; never {@code null}
	 * @return a list of all contexts, "outwards" in the {@link ExtensionContext#getParent() getParent}-order,
	 *         beginning with the given context; never {@code null} or empty
	 */
	public static List<ExtensionContext> findAllContexts(ExtensionContext context) {
		List<ExtensionContext> allContexts = new ArrayList<>();
		allContexts.add(context);
		List<ExtensionContext> parentContexts = context
				.getParent()
				.map(PioneerUtils::findAllContexts)
				.orElse(List.of());
		allContexts.addAll(parentContexts);
		return allContexts;
	}

	public static String nullSafeToString(Object object) {
		if (object == null) {
			return "null";
		}

		if (object.getClass().isArray()) {
			switch (object.getClass().getComponentType().getSimpleName()) {
				case "boolean":
					return Arrays.toString((boolean[]) object);
				case "byte":
					return Arrays.toString((byte[]) object);
				case "char":
					return Arrays.toString((char[]) object);
				case "int":
					return Arrays.toString((int[]) object);
				case "short":
					return Arrays.toString((short[]) object);
				case "long":
					return Arrays.toString((long[]) object);
				case "float":
					return Arrays.toString((float[]) object);
				case "double":
					return Arrays.toString((double[]) object);
				default:
					return Arrays.deepToString((Object[]) object);
			}
		}
		return object.toString();
	}

	/**
	 * Replaces all primitive types with the appropriate wrapper types.
	 * Returns the passed argument if it's not a primitive according to {@link Class#isPrimitive()}.
	 *
	 * @return the wrapped class of the primitive type, or the passed class
	 * @see MethodType#wrap()
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> wrap(Class<T> clazz) {
		return (Class<T>) MethodType.methodType(clazz).wrap().returnType();
	}

	public static List<List<?>> cartesianProduct(List<List<?>> lists) {
		List<List<?>> resultLists = new ArrayList<>();
		if (lists.isEmpty()) {
			resultLists.add(List.of());
			return resultLists;
		}
		List<?> firstList = lists.get(0);
		// Note the recursion here
		List<List<?>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
		for (Object item : firstList) {
			for (List<?> remainingList : remainingLists) {
				ArrayList<Object> resultList = new ArrayList<>();
				resultList.add(item);
				resultList.addAll(remainingList);
				resultLists.add(resultList);
			}
		}
		return resultLists;
	}

	public static Locale createLocale(String language, String country, String variant) {
		return new Locale.Builder().setLanguage(language).setRegion(country).setVariant(variant).build();
	}

	public static Locale createLocale(String language, String country) {
		return new Locale.Builder().setLanguage(language).setRegion(country).build();
	}

	public static Locale createLocale(String language) {
		return new Locale.Builder().setLanguage(language).build();
	}

	/**
	 * Determine if an instance of the supplied type can be converted into a
	 * {@code Stream}.
	 *
	 * <p>If this method returns {@code true}, {@link #toStream(Object)} can
	 * successfully convert an object of the specified type into a stream. See
	 * {@link #toStream(Object)} for supported types.
	 *
	 * <p>Based on the method with the same name in org.junit.platform.commons.util.CollectionUtils</p>
	 *
	 * @param type the type to check; may be {@code null}
	 * @return {@code true} if an instance of the type can be converted into a stream
	 * @see #toStream(Object)
	 */
	public static boolean isConvertibleToStream(Class<?> type) {
		if (type == null || type == void.class) {
			return false;
		}
		return (Stream.class.isAssignableFrom(type)//
				|| DoubleStream.class.isAssignableFrom(type)//
				|| IntStream.class.isAssignableFrom(type)//
				|| LongStream.class.isAssignableFrom(type)//
				|| Iterable.class.isAssignableFrom(type)//
				|| Iterator.class.isAssignableFrom(type)//
				|| Object[].class.isAssignableFrom(type)//
				|| (type.isArray() && type.getComponentType().isPrimitive()));
	}

	/**
	 * Convert an object of one of the following supported types into a {@code Stream}.
	 *
	 * <ul>
	 * <li>{@link Stream}</li>
	 * <li>{@link DoubleStream}</li>
	 * <li>{@link IntStream}</li>
	 * <li>{@link LongStream}</li>
	 * <li>{@link Collection}</li>
	 * <li>{@link Iterable}</li>
	 * <li>{@link Iterator}</li>
	 * <li>{@link Object} array</li>
	 * <li>primitive array</li>
	 * </ul>
	 *
	 * <p>Based on the method with the same name in org.junit.platform.commons.util.CollectionUtils</p>
	 *
	 * @param object the object to convert into a stream; never {@code null}
	 * @return the resulting stream
	 * @throws PreconditionViolationException if the supplied object is {@code null}
	 *                                        or not one of the supported types
	 * @see #isConvertibleToStream(Class)
	 */
	public static Stream<?> toStream(Object object) {
		PioneerPreconditions.notNull(object, "Object must not be null");
		if (object instanceof Stream) {
			return (Stream<?>) object;
		}
		if (object instanceof DoubleStream) {
			return ((DoubleStream) object).boxed();
		}
		if (object instanceof IntStream) {
			return ((IntStream) object).boxed();
		}
		if (object instanceof LongStream) {
			return ((LongStream) object).boxed();
		}
		if (object instanceof Collection) {
			return ((Collection<?>) object).stream();
		}
		if (object instanceof Iterable) {
			return StreamSupport.stream(((Iterable<?>) object).spliterator(), false);
		}
		if (object instanceof Iterator) {
			return StreamSupport.stream(spliteratorUnknownSize((Iterator<?>) object, ORDERED), false);
		}
		if (object instanceof Object[]) {
			return Arrays.stream((Object[]) object);
		}
		if (object instanceof double[]) {
			return DoubleStream.of((double[]) object).boxed();
		}
		if (object instanceof int[]) {
			return IntStream.of((int[]) object).boxed();
		}
		if (object instanceof long[]) {
			return LongStream.of((long[]) object).boxed();
		}
		if (object.getClass().isArray() && object.getClass().getComponentType().isPrimitive()) {
			return IntStream.range(0, Array.getLength(object)).mapToObj(i -> Array.get(object, i));
		}
		throw new PreconditionViolationException(
			"Cannot convert instance of " + object.getClass().getName() + " into a Stream: " + object);
	}

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string is blank
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().isEmpty());
	}

}
