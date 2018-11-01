package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SystemPropertyExtensionTests {

	@BeforeAll
	static void setUpOnce() {
		System.setProperty( "some property", "old value" );
		System.setProperty( "another property", "old value" );
	}

	@ClearSystemProperty( key = "some property" )
	@Test
	void clearSystemPropertyShouldSetPropertyToNull() {
		assertThat( System.getProperty( "some property" ) ).isNull();
	}

	@ClearSystemProperty( key = "some property" )
	@ClearSystemProperty( key = "another property" )
	@Test
	void clearSystemPropertyShouldBeRepeatable() {
		assertThat( System.getProperty( "some property" ) ).isNull();
		assertThat( System.getProperty( "another property" ) ).isNull();
	}

	@SetSystemProperty( key = "some property", value = "new value" )
	@Test
	void setSystemPropertyShouldSetPropertyToValue() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "new value" );
	}

	@SetSystemProperty( key = "some property", value = "new value" )
	@SetSystemProperty( key = "another property", value = "new value" )
	@Test
	void setSystemPropertyShouldBeRepeatable() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "new value" );
		assertThat( System.getProperty( "another property" ) ).isEqualTo( "new value" );
	}

	@ClearSystemProperty( key = "some property" )
	@SetSystemProperty( key = "another property", value = "new value" )
	@Test
	void clearAndSetSystemPropertyShouldBeMixable() {
		assertThat( System.getProperty( "some property" ) ).isNull();
		assertThat( System.getProperty( "another property" ) ).isEqualTo( "new value" );
	}

	@AfterAll
	static void tearDownOnce() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "old value" );
		assertThat( System.getProperty( "another property" ) ).isEqualTo( "old value" );
	}

}
