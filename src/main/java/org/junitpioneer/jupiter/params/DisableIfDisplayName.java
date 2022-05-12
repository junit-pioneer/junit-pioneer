/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * {@code @DisableIfDisplayName} is a JUnit Jupiter extension that can be used to
 * selectively disable a {@link ParameterizedTest} execution based on their
 * {@link ExtensionContext#getDisplayName() display name}.
 *
 * <p>The extension is an {@link ExecutionCondition} that validates dynamically registered tests.
 * Unlike {@link Disabled} annotations, this extension doesn't disable the whole test method.
 * With {@code DisableIfDisplayName}, it is possible to selectively disable tests out of the plethora
 * of dynamically registered parameterized tests.</p>
 *
 * <p>If neither {@link DisableIfDisplayName#contains() contains} nor
 * {@link DisableIfDisplayName#matches() matches} is configured, or if both are present,
 * the extension will throw an exception.
 * </p>
 *
 * @since 0.7
 * @see DisableIfNameExtension
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfNameExtension.class)
public @interface DisableIfDisplayName {

	/**
	 * Disable test cases whose display name contain the specified strings
	 * (according to {@link String#contains(CharSequence)}).
	 *
	 * @return test case display name substrings
	 */
	String[] contains() default {};

	/**
	 * Disable test cases whose display name matches the specified regular expression
	 * (according to {@link String#matches(java.lang.String)}).
	 *
	 * @return test case display name regular expressions
	 */
	String[] matches() default {};

}
