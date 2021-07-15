/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A test which executes tests using {@link PioneerTestKit} with the provided arguments.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Test
@ExtendWith(PioneerTestKitExtension.class)
public @interface TestKitTest {

	/**
	 * The class that should be run.
	 */
	Class<?> testClass();

	/**
	 * The test method name that should be run.
	 * If not set then the entire class will be run through {@link PioneerTestKit#executeTestClass(Class)}
	 * otherwise {@link PioneerTestKit#executeTestMethod(Class, String)} or {@link PioneerTestKit#executeTestMethodWithParameterTypes(Class, String, Class[])}
	 */
	String method() default "";

	/**
	 * The method parameter types that should be used for {@link PioneerTestKit#executeTestMethodWithParameterTypes(Class, String, Class[])}.
	 * If this is set then {@link #method()} has to be set as well.
	 */
	Class<?>[] methodParameterTypes() default {};

}
