/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DisableIfParameter} is a JUnit Jupiter extension that can
 * be used to selectively disable a {@link org.junit.jupiter.params.ParameterizedTest}
 * based on their parameter values as defined by {@link Object#toString()}.
 *
 * <p>The extension utilizes Jupiter's {@link org.junit.jupiter.api.extension.InvocationInterceptor}.
 * It's important to note that since it's marked as {@link org.apiguardian.api.API.Status#EXPERIMENTAL}
 * it might be removed without prior notice.
 * Unlike {@link org.junit.jupiter.api.Disabled} annotations, this extension doesn't disable the whole test method.
 * With {@code DisableIfParameter}, it is possible to selectively disable tests out of the plethora
 * of dynamically registered parameterized tests.</p>
 *
 * <p>The extension requires that exactly one of {@link DisableIfParameter#contains() contains} or
 * {@link DisableIfParameter#matches() matches} is configured.</p>
 *
 * <p>This annotation is for disabling a test based on a single parameter which can be designated with an
 * implicit index, an explicit index or by name (if parameter name information is present). For more
 * information how the extension resolves the annotations, check the documentation.</p>
 *
 * @see DisableIfParameterExtension
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DisableIfParameter.DisableIfParameters.class)
@ExtendWith(DisableIfParameterExtension.class)
public @interface DisableIfParameter {

	/**
	 * The name of the parameter the extension checks.
	 * If parameter naming information is not present at runtime, setting this will throw
	 * {@link org.junit.jupiter.api.extension.ParameterResolutionException}.
	 */
	String name() default "";

	/**
	 * The index of the parameter the extension checks, starting from 0.
	 * Negative values are ignored. Setting this to a number higher than the parameter count
	 * of the test method will result in {@link org.junit.jupiter.api.extension.ExtensionConfigurationException}.
	 */
	int index() default -1;

	/**
	 * Disable test cases whose parameter (converted to String with {@link Object#toString()})
	 * contains any of the the specified strings (according to {@link String#contains(CharSequence)}).
	 */
	String[] contains() default {};

	/**
	 * Disable test cases whose parameter (converted to String with {@link Object#toString()})
	 * matches any of the specified regular expressions (according to {@link String#matches(String)}).
	 */
	String[] matches() default {};

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface DisableIfParameters {

		DisableIfParameter[] value();

	}

}
