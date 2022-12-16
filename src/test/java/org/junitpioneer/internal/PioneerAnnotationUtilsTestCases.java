/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PioneerAnnotationUtilsTestCases {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface NonRepeatableTestAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Repeatable(PioneerAnnotationUtilsTestCases.RepeatableTestAnnotation.Container.class)
	public @interface RepeatableTestAnnotation {

		String value() default "";

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE, ElementType.METHOD })
		@interface Container {

			RepeatableTestAnnotation[] value();

		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface MetaAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@MetaAnnotation
	@NonRepeatableTestAnnotation("This annotation is meta present on anything annotated with @MetaAnnotatedTestAnnotation")
	@RepeatableTestAnnotation("This annotation is meta present on anything annotated with @MetaAnnotatedTestAnnotation")
	public @interface MetaAnnotatedTestAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface SomeAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Repeatable(RepeatableAnnotation.RepeatableAnnotations.class)
	public @interface RepeatableAnnotation {

		String value() default "";

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE, ElementType.METHOD })
		@interface RepeatableAnnotations {

			RepeatableAnnotation[] value();

		}

	}

	@NonRepeatableTestAnnotation("This annotation is indirectly present (inherited) on any method of an implementing class.")
	@RepeatableTestAnnotation("This annotation is indirectly present (inherited) on any method of an implementing class.")
	@SomeAnnotation
	public interface Base {
	}

	public static class Child implements Base {

		public void notAnnotated() {
		}

	}

	@NonRepeatableTestAnnotation("This annotation is enclosing present on any method of this class.")
	@RepeatableTestAnnotation("This annotation is enclosing present on any method of this class.")
	public static class Enclosing {

		public void notAnnotated() {
		}

	}

	public static class AnnotationCheck {

		public void notAnnotated() {
		}

		@NonRepeatableTestAnnotation("This annotation is directly present on the 'basic' method")
		@RepeatableTestAnnotation("This annotation is directly present on the 'basic' method")
		public void direct() {
		}

		@MetaAnnotatedTestAnnotation
		public void meta() {
		}

	}

	@NonRepeatableTestAnnotation("This annotation is enclosing present")
	@RepeatableTestAnnotation("This annotation is enclosing present")
	@RepeatableTestAnnotation("This annotation is also enclosing present")
	public static class AnnotationCluster implements Base {

		public void notAnnotated() {
		}

		@NonRepeatableTestAnnotation("This annotation is directly present")
		@RepeatableTestAnnotation("This annotation is directly present")
		@RepeatableTestAnnotation("This annotation is also directly present")
		public void direct() {
		}

		@MetaAnnotatedTestAnnotation
		public void meta() {

		}

		@NonRepeatableTestAnnotation("Nested 1")
		@RepeatableTestAnnotation("Repeatable nested 1")
		@RepeatableTestAnnotation("Repeatable nested 2")
		public static class NestedClass {

			@NonRepeatableTestAnnotation("Nested 2")
			@RepeatableTestAnnotation("Repeatable nested 3")
			@RepeatableTestAnnotation("Repeatable nested 4")
			public static class NestedNestedClass {

				@NonRepeatableTestAnnotation("Nested 3")
				@RepeatableTestAnnotation("Repeatable nested 5")
				public void annotated() {

				}

			}

		}

	}

	@RepeatableTestAnnotation("Repeatable 1")
	@RepeatableTestAnnotation("Repeatable 2")
	@NonRepeatableTestAnnotation("Repeatable 3")
	@MetaAnnotatedTestAnnotation("Annotated with repeatable 1 and meta")
	@SomeAnnotation
	public static class AnnotatedAnnotations {

		@RepeatableTestAnnotation("Repeatable 4")
		@RepeatableTestAnnotation("Repeatable 5")
		@NonRepeatableTestAnnotation("Repeatable 6")
		@SomeAnnotation
		@MetaAnnotatedTestAnnotation("Annotated with repeatable 2 and meta")
		public void annotated() {

		}

	}

	@RepeatableAnnotation("Repeatable 7")
	@RepeatableAnnotation("Repeatable 8")
	@SomeAnnotation("Some 1")
	public interface TestInterface1 {
	}

	@RepeatableAnnotation("Repeatable 9")
	@SomeAnnotation("Some 2")
	public interface TestInterface2 {
	}

	@RepeatableAnnotation("Repeatable 10")
	@RepeatableAnnotation("Repeatable 11")
	@SomeAnnotation("Some 3")
	public static class TestSuperclass {
	}

	public static class Implementer implements TestInterface1, TestInterface2 {

		public void notAnnotated() {
		}

	}

	public static class Extender extends TestSuperclass {

		public void notAnnotated() {

		}

	}

}
