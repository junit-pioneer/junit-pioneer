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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DefaultLocale} is a JUnit Jupiter extension to change the value
 * returned by {@link java.util.Locale#getDefault()} for a test execution.
 *
 * <p>The {@link java.util.Locale} to set as the default locale can be
 * configured in several ways:</p>
 *
 * <ul>
 *     <li>using a {@link java.util.Locale#forLanguageTag(String) language tag}</li>
 *     <li>using a {@link java.util.Locale.Builder} Locale.Builder} together with
 *         <ul>
 *            <li>a language</li>
 * 		      <li>a language and a county</li>
 * 			  <li>a language, a county, and a variant</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>Please keep in mind the the {@code Locale.Builder} does a syntax check, if you use a variant!
 * The given string must match the BCP 47 (or more detailed <a href="https://www.rfc-editor.org/rfc/rfc5646.html">RFC 5646</a>) syntax.</p>
 *
 * <p>If a language tag is set, none of the other fields must be set. Otherwise an
 * {@link org.junit.jupiter.api.extension.ExtensionConfigurationException} will
 * be thrown. Specifying a {@link #country()} but no {@link #language()}, or a
 * {@link #variant()} but no {@link #country()} and {@link #language()} will
 * also cause an {@code ExtensionConfigurationException}. After the annotated
 * element has been executed, the default {@code Locale} will be restored to
 * its original value.</p>
 *
 * <p>{@code @DefaultLocale} can be used on the method and on the class level. It
 * is inherited from higher-level containers, but can only be used once per method
 * or class. If a class is annotated, the configured {@code Locale} will be the
 * default {@code Locale} for all tests inside that class. Any method level
 * configurations will override the class level default {@code Locale}.</p>
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link DefaultLocale}, {@link ReadsDefaultLocale}, and {@link WritesDefaultLocale}
 * are scheduled in a way that guarantees correctness under mutation of shared global state.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/default-locale-timezone/" target="_top">the documentation on <code>@DefaultLocale</code> and <code>@DefaultTimeZone</code></a>.</p>
 *
 * @since 0.2
 * @see java.util.Locale#getDefault()
 * @see DefaultTimeZone
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@WritesDefaultLocale
@ExtendWith(DefaultLocaleExtension.class)
public @interface DefaultLocale {

	/**
	 * A language tag string as specified by IETF BCP 47. See
	 * {@link java.util.Locale#forLanguageTag(String)} for more information
	 * about valid language tag values.
	 *
	 * @since 0.3
	 */
	String value() default "";

	/**
	 * An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to
	 * 8 characters in length. See the {@link java.util.Locale} class
	 * description about valid language values.
	 */
	String language() default "";

	/**
	 * An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code. See
	 * the {@link java.util.Locale} class description about valid country
	 * values.
	 */
	String country() default "";

	/**
	 * An IETF BCP 47 language string that matches the <a href="https://www.rfc-editor.org/rfc/rfc5646.html">RFC 5646</a> syntax.
	 * It's validated by the {@code Locale.Builder}, using {@code sun.util.locale.LanguageTag#isVariant}.
	 */
	String variant() default "";

}
