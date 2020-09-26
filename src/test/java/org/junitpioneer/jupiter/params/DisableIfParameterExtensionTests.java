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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DisableIfParameterExtensionTests {

	@ParameterizedTest
	@DisableIfParameter("1")
	@ValueSource(ints = { 1, 2, 4 })
	void interceptParameterizedTest(int number) {
		System.out.println(number);
	}

}
