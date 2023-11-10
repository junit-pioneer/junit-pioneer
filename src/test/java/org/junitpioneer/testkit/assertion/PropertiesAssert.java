/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import java.lang.reflect.Field;
import java.util.Properties;

import org.assertj.core.api.AbstractAssert;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * Allows comparison of {@link Properties} with optional awareness of their structure,
 * rather than just treating them as Maps. Object values, which are marginally supported
 * by {@code Properties}, are supported in assertions as much as possible.
 */
public class PropertiesAssert extends AbstractAssert<PropertiesAssert, Properties> {

	PropertiesAssert(Properties actual) {
		super(actual, PropertiesAssert.class);
	}

	/**
	 * Assert Properties has the same effective values as the passed instance, but not
	 * the same nested default structure.
	 *
	 * <p>Properties are considered <em>effectively equal</em> if they have the same property
	 * names returned by {@code Properties.propertyNames()} and the same values returned by
	 * {@code getProperty(name)}. Properties may come from the properties instance itself,
	 * or from a nested default instance, indiscriminately.
	 *
	 * <p>Properties partially supports object values, but return null for {@code getProperty(name)}
	 * when the value is a non-string. This assertion follows the same rules:  Any non-String
	 * value is considered null for comparison purposes.
	 *
	 * @param expected The actual is expected to be effectively the same as this Properties
	 * @return Assertion instance
	 */
	public PropertiesAssert isEffectivelyEqualsTo(Properties expected) {

		// Compare values present in actual
		actual.propertyNames().asIterator().forEachRemaining(k -> {

			String kStr = k.toString();

			String actValue = actual.getProperty(kStr);
			String expValue = expected.getProperty(kStr);

			if (actValue == null) {
				if (expValue != null) {
					// An object value is the only way to get a null from getProperty()
					throw failure("For the property '<%s>', "
							+ "the actual value was an object but the expected the string '<%s>'.",
						k, expValue);
				}
			} else if (!actValue.equals(expValue)) {
				throw failure("For the property '<%s>', the actual value was <%s> but <%s> was expected", k, actValue,
					expValue);
			}
		});

		// Compare values present in expected - Anything not matching must not have been present in actual
		expected.propertyNames().asIterator().forEachRemaining(k -> {

			String kStr = k.toString();

			String actValue = actual.getProperty(kStr);
			String expValue = expected.getProperty(kStr);

			if (expValue == null) {
				if (actValue != null) {

					// An object value is the only way to get a null from getProperty()
					throw failure("For the property '<%s>', "
							+ "the actual value was the string '<%s>', but an object was expected.",
						k, actValue);
				}
			} else if (!expValue.equals(actValue)) {
				throw failure("The property <%s> was expected to be <%s>, but was missing", k, expValue);
			}
		});

		return this;
	}

	/**
	 * The converse of isEffectivelyEqualTo.
	 *
	 * @param expected The actual is expected to NOT be effectively equal to this Properties
	 * @return Assertion instance
	 */
	public PropertiesAssert isNotEffectivelyEqualTo(Properties expected) {
		try {
			isEffectivelyEqualsTo(expected);
		}
		catch (AssertionError ae) {
			return this; // Expected
		}

		throw failure("The actual Properties should not be effectively equal to the expected one.");
	}

	/**
	 * Compare values directly present in Properties and recursively into default Properties.
	 *
	 * @param expected The actual is expected to be strictly equal to this Properties
	 * @return Assertion instance
	 */
	public PropertiesAssert isStrictlyEqualTo(Properties expected) {

		// Compare values present in actual
		actual.keySet().forEach(k -> {
			if (!actual.get(k).equals(expected.get(k))) {
				throw failure("For the property <%s> the actual value was <%s> but <%s> was expected", k, actual.get(k),
					expected.get(k));
			}
		});

		// Compare values present in expected - Anything not matching must not have been present in actual
		expected.keySet().forEach(k -> {
			if (!expected.get(k).equals(actual.get(k))) {
				throw failure("The property <%s> was expected to be <%s>, but was missing", k, expected.get(k));
			}
		});

		// Dig down into the nested defaults
		Properties actualDefault = getDefaultFieldValue(actual);
		Properties expectedDefault = getDefaultFieldValue(expected);

		if (actualDefault != null && expectedDefault != null) {
			return new PropertiesAssert(actualDefault).isStrictlyEqualTo(expectedDefault);
		} else if (actualDefault != null) {
			throw failure("The actual Properties had non-null defaults, but none were expected");
		} else if (expectedDefault != null) {
			throw failure("The expected Properties had non-null defaults, but none were in actual");
		}

		return this;
	}

	/**
	 * Simple converse of isStrictlyEqualTo.
	 *
	 * @param expected The actual is expected to NOT be strictly equal to this Properties
	 * @return Assertion instance
	 */
	public PropertiesAssert isNotStrictlyEqualTo(Properties expected) {
		try {
			isStrictlyEqualTo(expected);
		}
		catch (AssertionError ae) {
			return this; // Expected
		}

		throw failure("The actual Properties should not be strictly the same as the expected one.");
	}

	/**
	 * Use reflection to grab the {@code defaults} field from a java.utils.Properties instance.
	 *
	 * @param parent The Properties to fetch default values from
	 * @return The Properties instance that was stored as defaults in the parent.
	 */
	protected Properties getDefaultFieldValue(Properties parent) {
		Field field = ReflectionSupport
				.findFields(Properties.class, f -> f.getName().equals("defaults"), HierarchyTraversalMode.BOTTOM_UP)
				.stream()
				.findFirst()
				.get();

		field.setAccessible(true);

		try {

			return (Properties) ReflectionSupport.tryToReadFieldValue(field, parent).get();

		}
		catch (Exception e) {
			throw new RuntimeException("Unable to access the java.util.Properties.defaults field by reflection. "
					+ "Please adjust your local environment to allow this.",
				e);
		}
	}

}
