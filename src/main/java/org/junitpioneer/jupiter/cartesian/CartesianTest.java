/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.jupiter.cartesian.CartesianEnumArgumentsProvider.NullEnum;

/**
 * {@code @CartesianTest} is a JUnit Jupiter extension that marks
 * a test to be executed with all possible input combinations.
 *
 * <p>Methods annotated with this annotation should not be annotated with {@code Test}.
 * </p>
 *
 * <p>This annotation is somewhat similar to {@code @ParameterizedTest}, as in it also takes
 * arguments and can run the same test multiple times. With {@code @CartesianTest} you
 * don't specify the test cases themselves, though. Instead you specify possible values for
 * each test method parameter (for example with @{@link CartesianTest.Values}) by annotating the parameters
 * themselves and the extension runs the method with each possible combination.
 * </p>
 *
 * <p>You can specify a custom Display Name for the tests ran by {@code @CartesianTest}.
 * By default it's [{index}] {arguments}.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the documentation on <code>@CartesianTest</code></a>.
 * </p>
 *
 * @since 1.5.0
 */
@TestTemplate
@ExtendWith(CartesianTestExtension.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CartesianTest {

	/**
	 * Placeholder for the display name of a {@code @CartesianTest}
	 *
	 * @since 1.5
	 * @see #name
	 */
	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	/**
	 * Placeholder for the current invocation index of a {@code @CartesianTest}
	 * method (1-based): <code>{index}</code>
	 *
	 * @since 1.5
	 * @see #name
	 */
	String INDEX_PLACEHOLDER = "{index}";

	/**
	 * Placeholder for the complete, comma-separated arguments list of the
	 * current invocation of a {@code @CartesianTest} method:
	 * <code>{arguments}</code>
	 *
	 * @since 1.5
	 * @see #name
	 */
	String ARGUMENTS_PLACEHOLDER = "{arguments}";

	/**
	 * <p>The display name to be used for individual invocations of the
	 * parameterized test; never blank or consisting solely of whitespace.
	 * </p>
	 *
	 * <p>Defaults to {@link org.junit.jupiter.params.ParameterizedTest#DEFAULT_DISPLAY_NAME}.
	 * </p>
	 * <p>
	 * Supported placeholders:
	 * <p>
	 * - {@link org.junit.jupiter.params.ParameterizedTest#DISPLAY_NAME_PLACEHOLDER}
	 * - {@link org.junit.jupiter.params.ParameterizedTest#INDEX_PLACEHOLDER}
	 * - {@link org.junit.jupiter.params.ParameterizedTest#ARGUMENTS_PLACEHOLDER}
	 * - <code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting.
	 * </p>
	 *
	 * @see java.text.MessageFormat
	 * @see org.junit.jupiter.params.ParameterizedTest#name()
	 */
	String name() default "[{index}] {arguments}";

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
	@CartesianArgumentsSource(CartesianValueArgumentsProvider.class)
	@interface Values {

		/**
		 * The {@code short} values to use as sources of arguments; must not be empty.
		 */
		short[] shorts() default {};

		/**
		 * The {@code byte} values to use as sources of arguments; must not be empty.
		 */
		byte[] bytes() default {};

		/**
		 * The {@code int} values to use as sources of arguments; must not be empty.
		 */
		int[] ints() default {};

		/**
		 * The {@code long} values to use as sources of arguments; must not be empty.
		 */
		long[] longs() default {};

		/**
		 * The {@code float} values to use as sources of arguments; must not be empty.
		 */
		float[] floats() default {};

		/**
		 * The {@code double} values to use as sources of arguments; must not be empty.
		 */
		double[] doubles() default {};

		/**
		 * The {@code char} values to use as sources of arguments; must not be empty.
		 */
		char[] chars() default {};

		/**
		 * The {@code boolean} values to use as sources of arguments; must not be empty.
		 */
		boolean[] booleans() default {};

		/**
		 * The {@link String} values to use as sources of arguments; must not be empty.
		 */
		String[] strings() default {};

		/**
		 * The {@link Class} values to use as sources of arguments; must not be empty.
		 */
		Class<?>[] classes() default {};

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
	@CartesianArgumentsSource(CartesianEnumArgumentsProvider.class)
	@interface Enum {

		/**
		 * The enum type that serves as the source of the enum constants.
		 *
		 * <p>If this attribute is not set explicitly, the declared type of the
		 * parameter of the {@code @CartesianProductTest} method, which has the
		 * same relative index of the annotation, is used.
		 *
		 * <p>For example, in case of the following test:
		 * <pre><code class='java'>
		 * &#64;CartesianProductTest
		 * &#64;CartesianTest.Enum
		 * &#64;CartesianTest.Enum
		 * void multipleOmittedTypes(FirstEnum e1, SecondEnum e2) {
		 * 	...
		 * }
		 * </code></pre>
		 * the first {@code @CartesianTest.Enum} annotation will provide all the values of {@code FirstEnum},
		 * while the second annotation will provide all the values of {@code SecondEnum}.
		 *
		 * @see #names
		 * @see #mode
		 */
		Class<? extends java.lang.Enum<?>> value() default NullEnum.class;

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
		 * <p>Defaults to {@link CartesianTest.Enum.Mode#INCLUDE INCLUDE}.
		 *
		 * @see CartesianTest.Enum.Mode#INCLUDE
		 * @see CartesianTest.Enum.Mode#EXCLUDE
		 * @see CartesianTest.Enum.Mode#MATCH_ALL
		 * @see CartesianTest.Enum.Mode#MATCH_ANY
		 * @see #names
		 */
		CartesianTest.Enum.Mode mode() default CartesianTest.Enum.Mode.INCLUDE;

		/**
		 * Enumeration of modes for selecting enum constants by name.
		 */
		enum Mode {

			/**
			 * Select only those enum constants whose names are supplied via the
			 * {@link CartesianTest.Enum#names} attribute.
			 */
			INCLUDE(CartesianTest.Enum.Mode::validateNames, (name, names) -> names.contains(name)),

			/**
			 * Select all declared enum constants except those supplied via the
			 * {@link CartesianTest.Enum#names} attribute.
			 */
			EXCLUDE(CartesianTest.Enum.Mode::validateNames, (name, names) -> !names.contains(name)),

			/**
			 * Select only those enum constants whose names match all patterns supplied
			 * via the {@link CartesianTest.Enum#names} attribute.
			 *
			 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
			 */
			MATCH_ALL(CartesianTest.Enum.Mode::validatePatterns,
					(name, patterns) -> patterns.stream().allMatch(name::matches)),

			/**
			 * Select only those enum constants whose names match any pattern supplied
			 * via the {@link CartesianTest.Enum#names} attribute.
			 *
			 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
			 */
			MATCH_ANY(CartesianTest.Enum.Mode::validatePatterns,
					(name, patterns) -> patterns.stream().anyMatch(name::matches));

			private final CartesianTest.Enum.Mode.Validator validator;
			private final BiPredicate<String, Set<String>> selector;

			Mode(CartesianTest.Enum.Mode.Validator validator, BiPredicate<String, Set<String>> selector) {
				this.validator = validator;
				this.selector = selector;
			}

			void validate(CartesianTest.Enum enumSource, Set<? extends java.lang.Enum<?>> constants,
					Set<String> names) {
				validator.validate(requireNonNull(enumSource), constants, requireNonNull(names));
			}

			boolean select(java.lang.Enum<?> constant, Set<String> names) {
				return selector.test(requireNonNull(constant.name()), requireNonNull(names));
			}

			private static void validateNames(CartesianTest.Enum enumSource, Set<? extends java.lang.Enum<?>> constants,
					Set<String> names) {
				Set<String> allNames = constants.stream().map(java.lang.Enum::name).collect(toSet());
				if (!allNames.containsAll(names)) {
					throw new PreconditionViolationException(
						"Invalid enum constant name(s) in " + enumSource + ". Valid names include: " + allNames);
				}
			}

			private static void validatePatterns(CartesianTest.Enum enumSource,
					Set<? extends java.lang.Enum<?>> constants, Set<String> names) {
				try {
					names.forEach(Pattern::compile);
				}
				catch (PatternSyntaxException e) {
					throw new PreconditionViolationException(
						"Pattern compilation failed for a regular expression supplied in " + enumSource, e);
				}
			}

			private interface Validator {

				void validate(CartesianTest.Enum enumSource, Set<? extends java.lang.Enum<?>> constants,
						Set<String> names);

			}

		}

	}

}
