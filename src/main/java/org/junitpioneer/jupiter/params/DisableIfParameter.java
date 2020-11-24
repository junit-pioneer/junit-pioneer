/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.*;

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
 * <p>If neither {@link DisableIfParameter#contains() contains} nor
 * {@link DisableIfParameter#matches() matches} is configured, the extension will throw an exception.
 * It is possible to configure both, in which case the test gets disabled if at least one substring
 * was found <em>or</em> at least one regular expression matched.</p>
 *
 * @see DisableIfParameterExtension
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DisableIfParameter.DisableIfParameters.class)
@ExtendWith(DisableIfParameterExtension.class)
public @interface DisableIfParameter {

	String name() default "";

	int index() default -1;

	/**
	 * Disable test cases whose parameter (converted to String with {@link Object#toString()})
	 * contain any of the the specified strings (according to {@link String#contains(CharSequence)}).
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
