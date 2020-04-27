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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * {@code @DisableIfDisplayName} is a JUnit Jupiter extension which can be used to
 * selectively disable {@link ParameterizedTest} based on their {@link ExtensionContext#getDisplayName()}
 *
 * <p>
 * The extension is an {@link ExecutionCondition} which validates dynamically registered tests
 * This is highly useful since current {@link Disabled} or {@link DisabledIf} annotations disable
 * the whole test but not the Parameterized tests selectively
 *
 * If it is required to disable selective tests out of the plethora of dynamically
 * registered Parameterized tests, then we can utilize the following
 *
 * Each repeatable annotation will be processed for each test and test will be skipped if
 * any of them evaluates true against the display name
 *
 * @since 0.5.6
 * @see DisableIfNameExtension
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfNameExtension.class)
public @interface DisableIfDisplayName {

	/**
	 *
	 * Display names of the test cases to be disabled. The whole test case name can be passed as well as sub string
	 * The values will be evaluated with {@link String#contains(CharSequence)} by default
	 * If, {@code regex} is provided, the string will be evaluated with {@link String#matches(String)} against the display name
	 * @return Test Case display name
	 */
	String[] value();

	/**
	 * @return if the {@code value} is to be evaluated as regular expression or sub-string
	 */
	boolean isRegEx() default false;

}
