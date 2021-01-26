/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * {@code @CartesianEnumSource} is an argument source for constants of a
 * specified {@linkplain #value Enum}.
 *
 * <p>The enum constants will be provided as an argument source to
 * the corresponding parameter of the annotated {@code @CartesianProductTest} method.
 *
 * <p>The set of enum constants can be restricted via the {@link #names} and
 * {@link #mode} attributes.
 *
 * <p>This annotation is {@link Repeatable}. You should declare one
 * {@code @CartesianEnumSource} per parameter.
 * </p>
 *
 * @see CartesianProductTest
 *
 * @since 1.3.0
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(CartesianEnumSource.CartesianEnumSources.class)
@ArgumentsSource(CartesianEnumArgumentsProvider.class)
public @interface CartesianEnumSource {

	/**
	 * The enum type that serves as the source of the enum constants.
	 *
	 * @see #names
	 * @see #mode
	 */
	Class<? extends Enum<?>> value();

	/**
	 * The names of enum constants to provide, or regular expressions to select
	 * the names of enum constants to provide.
	 *
	 * <p>If no names or regular expressions are specified, all enum constants
	 * declared in the specified {@linkplain #value enum type} will be provided.
	 *
	 * <p>The {@link #mode} determines how the names are interpreted.
	 *
	 * @see #value
	 * @see #mode
	 */
	String[] names() default {};

	/**
	 * The enum constant selection mode.
	 *
	 * <p>Defaults to {@link CartesianEnumSource.Mode#INCLUDE INCLUDE}.
	 *
	 * @see CartesianEnumSource.Mode#INCLUDE
	 * @see CartesianEnumSource.Mode#EXCLUDE
	 * @see CartesianEnumSource.Mode#MATCH_ALL
	 * @see CartesianEnumSource.Mode#MATCH_ANY
	 * @see #names
	 */
	CartesianEnumSource.Mode mode() default CartesianEnumSource.Mode.INCLUDE;

	/**
	 * Enumeration of modes for selecting enum constants by name.
	 */
	enum Mode {

		/**
		 * Select only those enum constants whose names are supplied via the
		 * {@link CartesianEnumSource#names} attribute.
		 */
		INCLUDE(CartesianEnumSource.Mode::validateNames, (name, names) -> names.contains(name)),

		/**
		 * Select all declared enum constants except those supplied via the
		 * {@link CartesianEnumSource#names} attribute.
		 */
		EXCLUDE(CartesianEnumSource.Mode::validateNames, (name, names) -> !names.contains(name)),

		/**
		 * Select only those enum constants whose names match all patterns supplied
		 * via the {@link CartesianEnumSource#names} attribute.
		 *
		 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
		 */
		MATCH_ALL(CartesianEnumSource.Mode::validatePatterns,
				(name, patterns) -> patterns.stream().allMatch(name::matches)),

		/**
		 * Select only those enum constants whose names match any pattern supplied
		 * via the {@link CartesianEnumSource#names} attribute.
		 *
		 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
		 */
		MATCH_ANY(CartesianEnumSource.Mode::validatePatterns,
				(name, patterns) -> patterns.stream().anyMatch(name::matches));

		private final BiConsumer<CartesianEnumSource, Set<String>> validator;
		private final BiPredicate<String, Set<String>> selector;

		Mode(BiConsumer<CartesianEnumSource, Set<String>> validator, BiPredicate<String, Set<String>> selector) {
			this.validator = validator;
			this.selector = selector;
		}

		void validate(CartesianEnumSource enumSource, Set<String> names) {
			validator.accept(requireNonNull(enumSource), requireNonNull(names));
		}

		boolean select(Enum<?> constant, Set<String> names) {
			return selector.test(requireNonNull(constant.name()), requireNonNull(names));
		}

		private static void validateNames(CartesianEnumSource enumSource, Set<String> names) {
			Set<String> allNames = stream(enumSource.value().getEnumConstants()).map(Enum::name).collect(toSet());
			if (!allNames.containsAll(names)) {
				throw new PreconditionViolationException(
					"Invalid enum constant name(s) in " + enumSource + ". Valid names include: " + allNames);
			}
		}

		private static void validatePatterns(CartesianEnumSource enumSource, Set<String> names) {
			try {
				names.forEach(Pattern::compile);
			}
			catch (PatternSyntaxException e) {
				throw new PreconditionViolationException(
					"Pattern compilation failed for a regular expression supplied in " + enumSource, e);
			}
		}

	}

	@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface CartesianEnumSources {

		CartesianEnumSource[] value();

	}

}
