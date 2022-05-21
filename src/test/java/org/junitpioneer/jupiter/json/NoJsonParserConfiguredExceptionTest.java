package org.junitpioneer.jupiter.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoJsonParserConfiguredExceptionTest {

	@Test
	void shouldBeUnchecked() {
		assertThat(new NoJsonParserConfiguredException()).isInstanceOf(RuntimeException.class);
	}
}