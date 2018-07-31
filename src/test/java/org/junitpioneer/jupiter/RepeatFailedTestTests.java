package org.junitpioneer.jupiter;

class RepeatFailedTestTests {

	private static int FAILS_ONLY_ON_FIRST_INVOCATION;

	@RepeatFailedTest(3)
	void failsNever_executedOnce_passes() { }

	@RepeatFailedTest(3)
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		FAILS_ONLY_ON_FIRST_INVOCATION++;
		if (FAILS_ONLY_ON_FIRST_INVOCATION == 1) {
			throw new IllegalArgumentException();
		}
	}

	@RepeatFailedTest(3)
	void failsAlways_executedThreeTimes_fails() {
		throw new IllegalArgumentException();
	}

}
