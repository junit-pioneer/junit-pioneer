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

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @Issue} is a JUnit Jupiter extension to mark tests that they are
 * exist to cover an issue, like a requirement or a bugfix.
 *
 * The annotated issue will be enlisted in the test results, so that
 * it's easy so see if all tests of an issue are completed successfully.
 *
 * <p>{@code @Issue} can be used on the method level only.
 * This is done on purpose, so that the test class can contain tests
 * which cover the professional requirements but also tests which are only
 * exist to ensure technical functionality / implementation details.</p>
 *
 * <p>{@code @Issue} can only be used once per method.
 * This is done on purpose because a test case should only cover exactly
 * one aspect of a method.</p>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(IssueExtension.class)
public @interface Issue {

	/**
	 * The id of the issue as defined by the issue-tracker, e.g. "REQ-123".
	 */
	String value();

}