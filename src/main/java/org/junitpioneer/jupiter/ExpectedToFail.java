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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @ExpectedToFail} is a JUnit Jupiter extension to mark test methods as temporarily
 * 'expected to fail'. Such test methods will still be executed but when they result in a test
 * failure or error the test will be aborted. However, if the test method unexpectedly executes
 * successfully, it is marked as failure to let the developer know that the test is now
 * successful and that the {@code @ExpectedToFail} annotation can be removed.
 *
 * <p>The big difference compared to JUnit's {@link org.junit.jupiter.api.Disabled @Disabled}
 * annotation is that the developer is informed as soon as a test is successful again.
 * This helps to avoid creating duplicate tests by accident and counteracts the accumulation
 * of disabled tests over time.
 *
 * <p>The annotation can only be used on methods and as meta-annotation on other annotation types.
 * Similar to {@code @Disabled}, it has to be used in addition to a "testable" annotation, such
 * as {@link org.junit.jupiter.api.Test @Test}. Otherwise the annotation has no effect.
 *
 * <p><b>Important:</b> This annotation is <b>not</b> intended as a way to mark test methods
 * which intentionally cause exceptions. Such test methods should use
 * {@link org.junit.jupiter.api.Assertions#assertThrows(Class, org.junit.jupiter.api.function.Executable) assertThrows}
 * or similar means to explicitly test for a specific exception class being thrown by a
 * specific action.
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/expected-to-fail-tests/" target="_top">the documentation on <code>@ExpectedToFail</code></a>.
 * </p>
 *
 * @since 1.8.0
 * @see org.junit.jupiter.api.Disabled
 */
@Documented
@Retention(RUNTIME)
/*
 * Only supports METHOD and ANNOTATION_TYPE as targets but not test classes because there
 * it is not clear what the 'correct' behavior would be when only a few test methods
 * execute successfully. Would the developer then have to remove the @ExpectedToFail annotation
 * from the test class and annotate methods individually?
 */
@Target({ METHOD, ANNOTATION_TYPE })
@ExtendWith(ExpectedToFailExtension.class)
public @interface ExpectedToFail {

	/**
	 * Defines the message to show when a test is aborted because it is failing.
	 * This can be used for example to briefly explain why the tested code is not working
	 * as intended at the moment.
	 * An empty string (the default) causes a generic default message to be used.
	 */
	String value() default "";

}
