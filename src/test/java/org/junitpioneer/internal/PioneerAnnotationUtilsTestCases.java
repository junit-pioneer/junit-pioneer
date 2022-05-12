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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PioneerAnnotationUtilsTestCases {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@Inherited
	public @interface NonRepeatableTestAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@Inherited
	@Repeatable(PioneerAnnotationUtilsTestCases.RepeatableTestAnnotation.Container.class)
	public @interface RepeatableTestAnnotation {

		String value() default "";

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
		@Inherited
		@interface Container {

			RepeatableTestAnnotation[] value();

		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@NonRepeatableTestAnnotation("This annotation is meta present on anything annotated with @MetaAnnotatedTestAnnotation")
	@RepeatableTestAnnotation("This annotation is meta present on anything annotated with @MetaAnnotatedTestAnnotation")
	public @interface MetaAnnotatedTestAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface NotInheritedAnnotation {

		String value() default "";

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Repeatable(NotInheritedRepeatableAnnotation.NotInheritedRepeatableAnnotations.class)
	public @interface NotInheritedRepeatableAnnotation {

		String value() default "";

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE, ElementType.METHOD })
		@interface NotInheritedRepeatableAnnotations {

			NotInheritedRepeatableAnnotation[] value();

		}

	}

	@NonRepeatableTestAnnotation("This annotation is indirectly present (inherited) on any method of an implementing class.")
	@RepeatableTestAnnotation("This annotation is indirectly present (inherited) on any method of an implementing class.")
	@NotInheritedAnnotation
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

	@RepeatableTestAnnotation("Inherited 1")
	@RepeatableTestAnnotation("Inherited 2")
	@NonRepeatableTestAnnotation("Inherited 3")
	@MetaAnnotatedTestAnnotation("Annotated with repeatable 1")
	@NotInheritedAnnotation
	public static class AnnotatedAnnotations {

		@RepeatableTestAnnotation("Inherited 4")
		@RepeatableTestAnnotation("Inherited 5")
		@NonRepeatableTestAnnotation("Inherited 6")
		@NotInheritedAnnotation
		@MetaAnnotatedTestAnnotation("Annotated with repeatable 2")
		public void annotated() {

		}

	}

	@NotInheritedRepeatableAnnotation("Not inherited repeatable 1")
	@NotInheritedRepeatableAnnotation("Not inherited repeatable 2")
	@NotInheritedAnnotation("Not inherited 1")
	public interface TestInterface1 {
	}

	@NotInheritedRepeatableAnnotation("Not inherited repeatable 3")
	@NotInheritedAnnotation("Not inherited 2")
	public interface TestInterface2 {
	}

	@NotInheritedAnnotation
	@NotInheritedRepeatableAnnotation("Not inherited class 1")
	@NotInheritedRepeatableAnnotation("Not inherited class 2")
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
