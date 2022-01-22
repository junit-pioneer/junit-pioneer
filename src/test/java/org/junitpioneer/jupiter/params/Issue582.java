package org.junitpioneer.jupiter.params;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

@ClearEnvironmentVariable(key = "some variable")
public class Issue582 {

	@Test
	@SetEnvironmentVariable(key = "some variable", value = "new value")
	void test() {
		assertThat(System.getenv("some variable")).isEqualTo("new value");
	}

}
