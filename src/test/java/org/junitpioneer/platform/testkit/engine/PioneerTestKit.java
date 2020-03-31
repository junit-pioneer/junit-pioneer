package org.junitpioneer.platform.testkit.engine;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

public class PioneerTestKit {

	/*
	 * TODO: Wrap `EngineExecutionResults` into a Pioneer-specific class, so we can add functionality, e.g.
	 * 	- make it easier to access specific payloads like Throwables and Report Entries,
	 *    by throwing AssertionErrors if they're absent
	 */

	public static EngineExecutionResults execute(Class<?> testClass) {
		return EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	public static EngineExecutionResults execute(Class<?> testClass, String testMethodName) {
		return EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

}
