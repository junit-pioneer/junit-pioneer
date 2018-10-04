/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DefaultLocale} is a JUnit Jupiter extension to change the value
 * returned by {@link java.util.Locale#getDefault()} for a test execution.
 *
 * <p>The {@link java.util.Locale} to set as the default locale can be
 * configured using only a language, a language and a country, or a language, a
 * country and a variant. If {@link #variant()} is set, but {@link #country()}
 * is not, a
 * {@link org.junit.jupiter.api.extension.ExtensionConfigurationException} will
 * be thrown. After the annotated element has been executed, the default
 * {@code Locale} will be restored to its original value.</p>
 *
 * <p>{@code @DefaultLocale} can be used on the method and on the class level.
 * If a class is annotated, the configured {@code Locale} will be the default
 * {@code Locale} for all tests inside that class. Any method level
 * configurations will override the class level default {@code Locale}.</p>
 *
 * @since 0.2
 * @see java.util.Locale#getDefault()
 * @see DefaultTimeZone
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DefaultLocaleExtension.class)
public @interface DefaultLocale {

	/**
	 * An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to
	 * 8 characters in length. See the {@link java.util.Locale} class
	 * description about valid language values.
	 */
	String language();

	/**
	 * An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code. See
	 * the {@link java.util.Locale} class description about valid country
	 * values.
	 */
	String country() default "";

	/**
	 * Any arbitrary value used to indicate a variation of a {@code Locale}.
	 * See the {@link java.util.Locale} class description for the details.
	 */
	String variant() default "";
}
