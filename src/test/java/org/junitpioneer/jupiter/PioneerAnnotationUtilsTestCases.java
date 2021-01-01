/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

class PioneerAnnotationUtilsTestCases {

	/*
	 *  ========
	 *  = NOTE =
	 *  ========
	 *
	 * When adding tests to `FailTestCases` or `RepeatableFailTestCases` consider adding
	 * equivalent ones to the other class as well.
	 */

	@Fail("root class")
	static class FailTestCases {

		@Test
		@Fail("method")
		void methodIsAnnotated() {
		}

		@Nested
		class RootClassTestCases {

			@Test
			void rootClassIsAnnotated() {
			}

		}

		@Fail("nested class")
		@Nested
		class NestedTestCases {

			@Test
			void classIsAnnotated() {
			}

			@Nested
			class TwiceNestedTestCases {

				@Test
				void outerClassIsAnnotated() {
				}

				@Nested
				class ThriceNestedTestCases {

					@Test
					void outerClassIsAnnotated() {
					}

				}

			}

		}

	}

	@RepeatableFail("root class")
	static class RepeatableFailTestCases {

		@Test
		@RepeatableFail("method")
		void methodIsAnnotated() {
		}

		@Test
		@RepeatableFail("repeated")
		@RepeatableFail("annotation")
		void methodIsRepeatablyAnnotated() {
		}

		@Nested
		class RootClassTestCases {

			@Test
			void rootClassIsAnnotated() {
			}

		}

		@RepeatableFail("nested class")
		@Nested
		class NestedTestCases {

			@Test
			void classIsAnnotated() {
			}

			@Nested
			class TwiceNestedTestCases {

				@Test
				void outerClassIsAnnotated() {
				}

				@Nested
				class ThriceNestedTestCases {

					@Test
					void outerClassIsAnnotated() {
					}

				}

			}

		}

		@Nested
		@RepeatableFail("repeated")
		@RepeatableFail("annotation")
		class NestedRepeatableTestCases {

			@Test
			void outerClassIsRepeatablyAnnotatedAnnotated() {
			}

		}

	}

}

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FailExtension.class)
@interface Fail {

	String value() default "";

}

class FailExtension implements BeforeTestExecutionCallback {

	static final AtomicBoolean FIND_ENCLOSING = new AtomicBoolean(true);

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		String message = PioneerAnnotationUtils
				.findAnnotations(context, Fail.class, false, FIND_ENCLOSING.get())
				.map(Fail::value)
				.collect(joining(","));
		if (!message.isEmpty())
			throw new AssertionError(message);
	}

}

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableFails.class)
@ExtendWith(RepeatableFailExtension.class)
@interface RepeatableFail {

	String value() default "";

}

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RepeatableFailExtension.class)
@interface RepeatableFails {

	RepeatableFail[] value();

}

class RepeatableFailExtension implements BeforeTestExecutionCallback {

	static final AtomicBoolean FIND_ENCLOSING = new AtomicBoolean(true);

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		String message = PioneerAnnotationUtils
				.findAnnotations(context, RepeatableFail.class, true, FIND_ENCLOSING.get())
				.map(RepeatableFail::value)
				.collect(joining(","));
		if (!message.isEmpty())
			throw new AssertionError(message);
	}

}
