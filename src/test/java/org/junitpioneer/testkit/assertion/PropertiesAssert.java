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

import org.assertj.core.api.AbstractAssert;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Allow comparisons of java.util.Properties with awareness of their structure,
 * rather than just treating them as Maps.
 */
public class PropertiesAssert extends AbstractAssert<PropertiesAssert, Properties> {

	/** No direct construction */
	private PropertiesAssert(Properties actual) {
		super(actual, PropertiesAssert.class);
	}

	public static PropertiesAssert assertThat(Properties actual) {
		return new PropertiesAssert(actual);
	}


		/**
		 * Assert Properties has the same effective values as the passed instance, but not
		 * the same nested default structure.
		 *
		 * Specifically, these aspects are compared:
		 * <ul>
		 * <li>String and Object keys and values directly present in the Properties are the same
		 * ({@code .equals()}).</li>
		 * <li>String keys and values are the same ({@code .equals()}) regardless if they are
		 * directly present in the via a nested default.</li>
		 * </ul>
		 *
		 * And these aspects are NOT compared:
		 * <ul>
		 * <li>Non-String key entries in nested defaults</li>
		 * <li>Non-String values for String keys in nested defaults have their values considered null.</li>
		 * </ul>
		 *
		 * @param expected
		 * @return
		 */
	public PropertiesAssert isEffectivelyTheSameAs(Properties expected) {

		// Compare values present in actual
		actual.propertyNames().asIterator().forEachRemaining(k -> {

			String kStr = k.toString();

			Object actValue = actual.getProperty(kStr);
			if (actValue == null) actValue = actual.get(k);

			Object expValue = expected.getProperty(kStr);
			if (expValue == null) expValue = expected.get(k);

			if (!actValue.equals(expValue)) {
				throw failure("For the property <%s> the actual value was <%s> but <%s> was expected",
						k, actValue, expValue);
			}
		});

		// Compare values present in expected - Anything not matching must not have been present in actual
		expected.propertyNames().asIterator().forEachRemaining(k -> {

			String kStr = k.toString();

			Object actValue = actual.getProperty(kStr);
			if (actValue == null) actValue = actual.get(k);	//Handles objects

			Object expValue = expected.getProperty(kStr);
			if (expValue == null) expValue = expected.get(k); //Handles objects

			if (! expValue.equals(actValue)) {
				throw failure("The property <%s> was expected to be <%s>, but was missing",
						k, expected.get(k));
			}
		});

		return this;
	}



	public PropertiesAssert isNotEffectivelyTheSameAs(Properties expected) {
		try {
			isEffectivelyTheSameAs(expected);
		} catch (AssertionError ae) {
			return this;	// Expected
		}

		throw failure("The actual Properties should not be effectively the same as the expected one.");
	}


	/**
	 * Compare values directly present in Properties and recursively into default Properties.
	 *
	 * Objects (non-Strings) within the Properties object are compared with {@code .equals()}.
	 *
	 *
	 * @param expected
	 * @return
	 */
	public PropertiesAssert isStrictlyTheSameAs(Properties expected) {

		// Compare values present in actual
		actual.keySet().forEach(k -> {
			if (!actual.get(k).equals(expected.get(k))) {
				throw failure("For the property <%s> the actual value was <%s> but <%s> was expected",
						k, actual.get(k), expected.get(k));
			}
		});

		// Compare values present in expected - Anything not matching must not have been present in actual
		expected.keySet().forEach(k -> {
			if (!expected.get(k).equals(actual.get(k))) {
				throw failure("The property <%s> was expected to be <%s>, but was missing",
						k, expected.get(k));
			}
		});

		//
		// Dig down into the nested defaults
			Properties actualDefault = getDefaultFieldValue(actual);
			Properties expectedDefault = getDefaultFieldValue(expected);


		if (actualDefault != null && expectedDefault != null) {
			return PropertiesAssert.assertThat(actualDefault).isStrictlyTheSameAs(expectedDefault);
		} else if (actualDefault != null) {
			throw failure("The actual Properties had non-null defaults, but none were expected");
		} else if (expectedDefault != null) {
			throw failure("The expected Properties had non-null defaults, but none were in actual");
		}

		return this;
	}

	/**
	 * Simple converse of isStrictlyTheSameAs.
	 *
	 * @param expected
	 * @return
	 */
	public PropertiesAssert isNotStrictlyTheSameAs(Properties expected) {
		try {
			isStrictlyTheSameAs(expected);
		} catch (AssertionError ae) {
			return this;	// Expected
		}

		throw failure("The actual Properties should not be strictly the same as the expected one.");
	}

	/**
	 * Use reflection to grab the {@code defaults} field from a java.utils.Properties instance.
	 *
	 * @param parent
	 * @return
	 */
	protected Properties getDefaultFieldValue(Properties parent) {
		Field field = ReflectionSupport
				.findFields(Properties.class, f -> f.getName().equals("defaults"), HierarchyTraversalMode.BOTTOM_UP)
				.stream().findFirst().get();

		field.setAccessible(true);

		try {

			return (Properties) ReflectionSupport.tryToReadFieldValue(field, parent).get();

		} catch (Exception e) {
			throw new RuntimeException("Unable to access the java.util.Properties.defaults field by reflection. " +
					"Please adjust your local environment to allow this.");
		}
	}

}
