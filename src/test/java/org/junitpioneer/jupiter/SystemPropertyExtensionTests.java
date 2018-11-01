package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SystemPropertyExtensionTests {

	@BeforeAll
	static void setUpOnce() {
		System.setProperty( "some property", "old value" );
	}

	@SystemProperty( key = "some property", value = SystemProperty.CLEAR )
	@Test
	void extension_should_set_property_to_null() {
		assertThat( System.getProperty( "some property" ) ).isNull();
	}

	@SystemProperty( key = "some property", value = "new value" )
	@Test
	void extension_should_set_property_to_value() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "new value" );
	}

	@SystemProperty( key = "some property", value = SystemProperty.CLEAR )
	@SystemProperty( key = "another property", value = "new value" )
	@Test
	void extension_should_be_repeatable() {
		assertThat( System.getProperty( "some property" ) ).isNull();
		assertThat( System.getProperty( "another property" ) ).isEqualTo( "new value" );
	}

	@AfterAll
	static void tearDownOnce() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "old value" );
	}
}
