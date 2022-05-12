/*
 * Copyright 2016-2022 the original author or authors.
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
 * {@code @Issue} is a JUnit Jupiter extension to mark tests that
 * exist to cover an issue, like a requirement or a bugfix.
 *
 * The annotated issue ID will be published as a report entry - where
 * this information will be visible, depends on the tool used to
 * execute the tests.
 *
 * <p>{@code @Issue} can be used on the method and class level.
 * Warning: If you place it on class level, make sure to not mix tests which belong to the issue and those which don't!</p>
 *
 * <p>{@code @Issue} can only be used once per method.
 * This is done on purpose because a test case should only cover exactly
 * one aspect of a method.</p>
 *
 * @since 1.1
 * @see IssueProcessor
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(IssueExtension.class)
public @interface Issue {

	/**
	 * The id of the issue as defined by the issue-tracker, e.g. "REQ-123".
	 */
	String value();

}
