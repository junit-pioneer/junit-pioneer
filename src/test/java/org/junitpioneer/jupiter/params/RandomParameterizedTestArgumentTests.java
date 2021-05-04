package org.junitpioneer.jupiter.params;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.params.RandomArguments.RandomInt;
import org.junitpioneer.jupiter.params.RandomArguments.RandomString;

public class RandomParameterizedTestArgumentTests {

//	@ParameterizedTest
	@RandomArguments(count = 5)
	void testWithRandomValues(@RandomString String beer, TestInfo info) {
		System.out.println(beer);
	}

	@ParameterizedTest
	@RandomArguments(count = 4)
	void testWithRandomValueses(
			@RandomString(minLength = 5, maxLength = 7) String beer,
			@RandomInt(min = 8, max = 24) int proof) {
		// ...
	}

}
