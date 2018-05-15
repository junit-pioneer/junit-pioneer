/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit$pioneer.vintage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @Test} is used to signal to JUnit Jupiter that the annotated method is a <em>test</em> method - it is designed
 * to be a drop-in replacement for JUnit 4's {@code @Test}.
 *
 * <p>Like JUnit 4's annotation it offers the possibility to {@link #expected() expect exceptions} and to
 * {@link #timeout() time out} long running tests. The latter functions slightly different from the original - see the
 * attached Javadoc.
 *
 * <p>See {@link org.junit.jupiter.api.Test the official @Test} for more information regarding JUnit Jupiter integration.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ExpectedExceptionExtension.class)
@ExtendWith(TimeoutExtension.class)
@org.junit.jupiter.api.Test
public @interface Test {

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
	 * <p>
	 * <b>NOTE:</b> Unlike the same parameter on JUnit 4's {@code @Test} annotation, this one
	 * <b>does not</b> lead to a long running test being abandoned.
	 * Tests will always be allowed to finish (if they do that at all) and their run time might lead
	 * to the test being failed retroactively.
	 * </p>
	 */
	long timeout() default 0L;

}
