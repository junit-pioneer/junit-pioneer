/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.vintage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @Test} is used to signal to JUnit Jupiter that the annotated method is a <em>test</em> method - it is
 * a drop-in replacement for <a href="https://junit.org/junit4/javadoc/latest/index.html">JUnit 4's <code>@Test</code></a>.
 *
 * <p>Like JUnit 4's annotation it offers the possibility to {@link #expected() expect exceptions} and to
 * {@link #timeout() time out} long running tests.</p>
 *
 * <p>Also check
 * <a href="https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Test.html" target="_top">Jupiter's <code>@Test</code></a>
 * for more information regarding JUnit Jupiter integration and
 * <a href="https://junit-pioneer.org/docs/vintage-test/" target="_top">Pioneer's documentation on this <code>@Test</code></a>
 * for more details and examples.</p>
 *
 * @deprecated This annotation is an intermediate step on a full migration from JUnit 4's {@code @Test} to Jupiter's.
 * To emphasize its character as a temporary solution and to reduce risk of accidental use, it's marked as deprecated.
 * Deprecated since v0.4; not intended to be removed.
 *
 * @since 0.1
 */
@Deprecated
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ExpectedExceptionExtension.class)
@ExtendWith(TimeoutExtension.class)
@org.junit.jupiter.api.Test
public @interface Test {

	/**
	 * Dummy default class for the <code>expected</code> parameter.
	 */
	class None extends Throwable {

		private static final long serialVersionUID = 1L;

		private None() {
		}

	}

	/**
	 * Optionally specify <code>expected</code>, a Throwable, to cause a test method to succeed if
	 * and only if an exception of the specified class is thrown by the method.
	 */
	Class<? extends Throwable> expected() default None.class;

	/**
	 * Optionally specify <code>timeout</code> in milliseconds to cause a test method to fail if it
	 * takes longer than that number of milliseconds.
	 *
	 * <p>If the timeout is exceeded, the test will be abandoned (i.e. the test execution moves on
	 * to the next test) and the thread running it is interrupted.</p>
	 *
	 * <p>In accordance with JUnit 4, the default value is {@code 0}, which means that while
	 * configuring 0 milliseconds is possible, it is indistinguishable from the default value
	 * and thus ignored. Negative values are rejected with an exception.</p>
	 */
	long timeout() default 0L;

}
