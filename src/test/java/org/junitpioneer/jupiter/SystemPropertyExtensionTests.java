package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName( "SystemProperty extension" )
class SystemPropertyExtensionTests {

	@BeforeAll
	static void globalSetUp() {
		System.setProperty( "some property", "old value" );
		System.setProperty( "another property", "old value" );
	}

	@AfterAll
	static void globalTearDown() {
		assertThat( System.getProperty( "some property" ) ).isEqualTo( "old value" );
		assertThat( System.getProperty( "another property" ) ).isEqualTo( "old value" );
	}

	@Nested
	@DisplayName( "used with ClearSystemProperty" )
	class ClearSystemPropertyTests {

		@ClearSystemProperty( key = "some property" )
		@Test
		@DisplayName( "should clear system property" )
		void shouldClearSystemProperty() {
			assertThat( System.getProperty( "some property" ) ).isNull();
		}

		@ClearSystemProperty( key = "some property" )
		@ClearSystemProperty( key = "another property" )
		@Test
		@DisplayName( "should be repeatable" )
		void shouldBeRepeatable() {
			assertThat( System.getProperty( "some property" ) ).isNull();
			assertThat( System.getProperty( "another property" ) ).isNull();
		}

	}

	@Nested
	@DisplayName( "used with SetSystemProperty" )
	class SetSystemPropertyTests {

		@SetSystemProperty( key = "some property", value = "new value" )
		@Test
		@DisplayName( "should set system property to value" )
		void shouldSetSystemPropertyToValue() {
			assertThat( System.getProperty( "some property" ) ).isEqualTo( "new value" );
		}

		@SetSystemProperty( key = "some property", value = "new value" )
		@SetSystemProperty( key = "another property", value = "new value" )
		@Test
		@DisplayName( "should be repeatable" )
		void shouldBeRepeatable() {
			assertThat( System.getProperty( "some property" ) ).isEqualTo( "new value" );
			assertThat( System.getProperty( "another property" ) ).isEqualTo( "new value" );
		}

	}

	@Nested
	@DisplayName( "used with both ClearSystemProperty and SetSystemProperty" )
	class MixedSystemPropertyTests {

		@ClearSystemProperty( key = "some property" )
		@SetSystemProperty( key = "another property", value = "new value" )
		@Test
		@DisplayName( "should be mixable" )
		void clearAndSetSystemPropertyShouldBeMixable() {
			assertThat( System.getProperty( "some property" ) ).isNull();
			assertThat( System.getProperty( "another property" ) ).isEqualTo( "new value" );
		}

	}

}
