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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junitpioneer.jupiter.PioneerException;

@DisplayName("Reflexive copy of PioneerAnnotationUtils")
@ResourceLock(value = "org.junitpioneer.jupiter.params.PioneerAnnotationUtils")
public class PioneerAnnotationUtilsTests {

	private static Method METHOD;

	@BeforeAll
	static void setup() throws ReflectiveOperationException {
		Field fieldMethod = PioneerAnnotationUtils.class.getDeclaredField("FIND_CLOSEST_ENCLOSING_ANNOTATION");
		fieldMethod.setAccessible(true);
		METHOD = (Method) fieldMethod.get(null);
		fieldMethod.setAccessible(false);
	}

	@Test
	@DisplayName("throws PioneerException if method invocation fails.")
	void test() {
		METHOD.setAccessible(false);

		assertThatThrownBy(() -> PioneerAnnotationUtils.findClosestEnclosingAnnotation(null, null))
				.isInstanceOf(PioneerException.class)
				.hasMessageContaining("Internal Pioneer error.")
				.hasCauseExactlyInstanceOf(IllegalAccessException.class);
	}

	@AfterAll
	static void teardown() {
		METHOD.setAccessible(true);
	}

}
