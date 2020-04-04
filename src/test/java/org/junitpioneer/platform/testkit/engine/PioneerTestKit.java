/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.platform.testkit.engine;

public class PioneerTestKit {

	/*
	 * TODO: Wrap `EngineExecutionResults` into a Pioneer-specific class, so we can add functionality, e.g.
	 * 	- make it easier to access specific payloads like Throwables and Report Entries,
	 *    by throwing AssertionErrors if they're absent
	 */

	public static PioneerEngineExecutionResults execute(Class<?> testClass) {
		PioneerEngineExecutionResults results = new PioneerEngineExecutionResults(testClass);

		return results;
	}

	public static PioneerEngineExecutionResults execute(Class<?> testClass, String testMethodName) {
		PioneerEngineExecutionResults results = new PioneerEngineExecutionResults(testClass, testMethodName);

		return results;
	}

}
