/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import org.junit.jupiter.api.*;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verify proper behavior when annotated on a top level class.
 *
 * These tests include testing object values but not object keys.  Properties sort-of supports
 * object values, but dies on common operations w/ object keys (e.g. propertyNames() fails), thus
 * not testing.
 *
 * Also, null keys and values are not allow in Properties.
 *
 */
@DisplayName("PropertiesAssert Tests")
class PropertiesAssertTests {

	// Objects to put in Props
	static final Object O_OBJ = new Object();
	static final Object P_OBJ = new Object();


	// Two Properties objects w/ the exact same string contents
	Properties strPropAB1;
	Properties strPropAB2;

	// Same as propAandB but 'B' comes from a default value
	Properties strPropAB1CDwDefaults;
	Properties strPropAB2CDwDefaults;

	Properties objProp1;
	Properties objProp2;

	Properties propCandD;


	@BeforeEach
	void beforeEachMethod() {
		strPropAB1 = new Properties();
		strPropAB1.setProperty("A", "is A");
		strPropAB1.setProperty("B", "is B");

		strPropAB2 = new Properties();
		strPropAB2.setProperty("A", "is A");
		strPropAB2.setProperty("B", "is B");


		strPropAB1CDwDefaults = new Properties(strPropAB1);
		strPropAB1CDwDefaults.setProperty("C", "is C");
		strPropAB1CDwDefaults.setProperty("D", "is D");

		strPropAB2CDwDefaults = new Properties(strPropAB2);
		strPropAB2CDwDefaults.setProperty("C", "is C");
		strPropAB2CDwDefaults.setProperty("D", "is D");



		objProp1 = new Properties();
		objProp1.put("O", O_OBJ);
		objProp1.put("P", P_OBJ);

		objProp2 = new Properties();
		objProp2.put("O", O_OBJ);
		objProp2.put("P", P_OBJ);
	}

	@Nested
	@DisplayName("Top level String values")
	class TopLevelStringValues {
		@Test
		@DisplayName("Effectively and strictly same for identical")
		public void compareIdentical() {
			PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);
		}

		@Test
		@DisplayName("Not same for added actual value")
		public void addedActualValue() {
			strPropAB1.setProperty("C", "I am not in set 2");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}

		@Test
		@DisplayName("Not same added exp value")
		public void addedExpectedValue() {
			strPropAB2.setProperty("C", "I am not in set 1");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}

		@Test
		@DisplayName("Not same changed actual value")
		public void changedActualValue() {
			strPropAB1.setProperty("B", "I am different");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}

		@Test
		@DisplayName("Not same for changed exp value")
		public void changedExpectedValue() {
			strPropAB2.setProperty("B", "I am different");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}

		@Test
		@DisplayName("Not same for removed actual value")
		public void removedActualValue() {
			strPropAB1.remove("B");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}

		@Test
		@DisplayName("Not same for removed exp value")
		public void removedExpValue() {
			strPropAB2.remove("B");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isEffectivelyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1).isStrictlyTheSameAs(strPropAB2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1).isNotEffectivelyTheSameAs(strPropAB2);
			PropertiesAssert.assertThat(strPropAB1).isNotStrictlyTheSameAs(strPropAB2);
		}
	}


	// This section is missing some tests that are in the above section
	// Is not completely coverted over to objects (was copy paste from above)
	@Nested
	@DisplayName("Top level Object values")
	class TopLevelObjectValues {
		@Test
		@DisplayName("Effectively and strictly same for identical")
		public void compareIdentical() {
			PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);
		}

		@Test
		@DisplayName("Not same for added actual value")
		public void addedActualValue() {
			objProp1.put("Q", new Object());
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}

		@Test
		@DisplayName("Not same added exp value")
		public void addedExpectedValue() {
			objProp2.put("Q", new Object());
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}


		@Test
		@DisplayName("Not same changed actual value")
		public void changedActualValue() {
			objProp1.put("P", new Object());
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}

		@Test
		@DisplayName("Not same for changed exp value")
		public void changedExpectedValue() {
			objProp2.put("P", new Object());
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}

		@Test
		@DisplayName("Not same for removed actual value")
		public void removedActualValue() {
			objProp1.remove("P");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}

		@Test
		@DisplayName("Not same for removed exp value")
		public void removedExpValue() {
			objProp2.remove("P");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isEffectivelyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(objProp1).isStrictlyTheSameAs(objProp2);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(objProp1).isNotEffectivelyTheSameAs(objProp2);
			PropertiesAssert.assertThat(objProp1).isNotStrictlyTheSameAs(objProp2);
		}

	}


	@Nested
	@DisplayName("Nested default String values")
	class NestedDefaultStringValues {
		@Test
		@DisplayName("Effectively and strictly same for identical")
		public void compareIdentical() {
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);
		}

		@Test
		@DisplayName("Not same for added actual default value")
		public void addedActualValue() {
			strPropAB1.setProperty("E", "I am not in '2' and set in the default prop instance");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Not same added exp value")
		public void addedExpectedValue() {
			strPropAB2.setProperty("E", "I am not in '1' and set in the default prop instance");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Not same changed actual value")
		public void changedActualValue() {
			strPropAB1.setProperty("B", "I am different than '2' and set in the default prop instance");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Not same for changed exp value")
		public void changedExpectedValue() {
			strPropAB2.setProperty("B", "I am different than '1' and set in the default prop instance");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Not same for removed actual value")
		public void removedActualValue() {
			strPropAB1.remove("B");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Not same for removed exp value")
		public void removedExpValue() {
			strPropAB2.remove("B");
			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Move actual value from default to top level")
		public void moveActualValueFromDefaultToTopLevel() {
			strPropAB1CDwDefaults.put("B", strPropAB1.getProperty("B"));
			strPropAB1.remove("B");

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Move actual value from top level to default")
		public void moveActualValueFromTopLevelToDefault() {
			strPropAB1.put("D", strPropAB1CDwDefaults.getProperty("D"));
			strPropAB1CDwDefaults.remove("D");

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}


		@Test
		@DisplayName("Move exp value from default to top level")
		public void moveExpValueFromDefaultToTopLevel() {
			strPropAB2CDwDefaults.put("B", strPropAB2.getProperty("B"));
			strPropAB2.remove("B");

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}

		@Test
		@DisplayName("Move exp value from top level to default")
		public void moveExpValueFromTopLevelToDefault() {
			strPropAB2.put("D", strPropAB2CDwDefaults.getProperty("D"));
			strPropAB2CDwDefaults.remove("D");

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isEffectivelyTheSameAs(strPropAB2CDwDefaults);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isStrictlyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			assertThatThrownBy(() -> {
				PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotEffectivelyTheSameAs(strPropAB2CDwDefaults);
			}).isInstanceOf(AssertionError.class);

			PropertiesAssert.assertThat(strPropAB1CDwDefaults).isNotStrictlyTheSameAs(strPropAB2CDwDefaults);
		}
	}


}
