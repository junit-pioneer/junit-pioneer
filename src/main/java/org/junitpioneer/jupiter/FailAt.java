/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @FailAt} is a JUnit Jupiter extension to mark tests that shouldn't be executed after a given date,
 * essentially failing a test when the date is reached. The date is given as an ISO 8601 string.
 *
 * <p>It may optionally be declared with a reason to document why the annotated test class or test
 * method should fail at the given date.</p>
 *
 * <p>{@code @FailAt} can be used on the method and class level. It can only be used once per method or class,
 * but is inherited from higher-level containers.</p>
 *
 * <p><strong>WARNING:</strong> This annotation allows the user to move an assumption out of one or multiple test
 * method's code into an annotation. But this comes at a cost: Applying {@code @FailAt} can make the test suite
 * non-reproducible. If a passing test is run again after the specified date, that build would fail. A report entry is
 * issued for every test that does not fail until a certain date.</p>
 *
 * @since 2.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@ExtendWith(FailAtExtension.class)
public @interface FailAt {

	/**
	 * The reason this annotated test class or test method should fail as soon as the given date is reached.
	 */
	String reason() default "";

	/**
	 * The date from which this annotated test class or test method should fail as an ISO 8601 string in the
	 * format yyyy-MM-dd, e.g. 2023-05-28. The test will be executed regularly if that date is not yet reached.
	 */
	String date();

}
