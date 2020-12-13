/*
 * Copyright 2015-2020 the original author or authors.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.PioneerException;

@DisplayName("Reflexive copy of PioneerAnnotationUtils")
public class PioneerAnnotationUtilsTests {

	@Test
	@DisplayName("throws PioneerException if method invocation fails.")
	void test() throws NoSuchFieldException, IllegalAccessException {
		Field fieldMethod = PioneerAnnotationUtils.class.getDeclaredField("FIND_CLOSEST_ENCLOSING_ANNOTATION");
		fieldMethod.setAccessible(true);
		Method method = (Method) fieldMethod.get(null);
		method.setAccessible(false);

		assertThatThrownBy(() -> PioneerAnnotationUtils.findClosestEnclosingAnnotation(null, null))
				.isInstanceOf(PioneerException.class)
				.hasMessageContaining("Internal Pioneer error.")
				.hasCauseExactlyInstanceOf(IllegalAccessException.class);
	}

}
