/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.PioneerException;

@DisplayName("Reflexive copy of PioneerAnnotationUtils")
@ResourceLock(value = "org.junitpioneer.jupiter.params.PioneerAnnotationUtils")
public class PioneerAnnotationUtilsTests {

	private static Method FIND_CLOSEST_ENCLOSING_ANNOTATION;
	private static Method FIND_ANNOTATED_ANNOTATION;
	private static Method IS_CONTAINER_ANNOTATION;

	@BeforeAll
	static void setup() throws ReflectiveOperationException {
		Field findClosestEnclosingAnnotation = PioneerAnnotationUtils.class
				.getDeclaredField("FIND_CLOSEST_ENCLOSING_ANNOTATION");
		Field findAnnotatedAnnotation = PioneerAnnotationUtils.class.getDeclaredField("FIND_ANNOTATED_ANNOTATION");
		Field isContainerAnnotation = PioneerAnnotationUtils.class.getDeclaredField("IS_CONTAINER_ANNOTATION");
		findClosestEnclosingAnnotation.setAccessible(true);
		FIND_CLOSEST_ENCLOSING_ANNOTATION = (Method) findClosestEnclosingAnnotation.get(null);
		findClosestEnclosingAnnotation.setAccessible(false);
		findAnnotatedAnnotation.setAccessible(true);
		FIND_ANNOTATED_ANNOTATION = (Method) findAnnotatedAnnotation.get(null);
		findAnnotatedAnnotation.setAccessible(false);
		isContainerAnnotation.setAccessible(true);
		IS_CONTAINER_ANNOTATION = (Method) isContainerAnnotation.get(null);
		isContainerAnnotation.setAccessible(false);
	}

	private static Stream<Arguments> methodAndRunnable() {
		return Stream
				.of(Arguments
						.of(FIND_CLOSEST_ENCLOSING_ANNOTATION,
							(ThrowingCallable) () -> PioneerAnnotationUtils.findClosestEnclosingAnnotation(null, null)),
					Arguments
							.of(FIND_ANNOTATED_ANNOTATION,
								(ThrowingCallable) () -> PioneerAnnotationUtils.findAnnotatedAnnotations(null, null)),
					Arguments
							.of(IS_CONTAINER_ANNOTATION,
								(ThrowingCallable) () -> PioneerAnnotationUtils.isContainerAnnotation(null)));
	}

	@ParameterizedTest(name = "method {0} throws PioneerException")
	@MethodSource("methodAndRunnable")
	@DisplayName("throws PioneerException if method invocation fails.")
	void test(Method method, ThrowingCallable invocation) {
		method.setAccessible(false);

		assertThatThrownBy(invocation)
				.isInstanceOf(PioneerException.class)
				.hasMessageContaining("Internal Pioneer error.")
				.hasCauseExactlyInstanceOf(IllegalAccessException.class);

		method.setAccessible(true);
	}

}
