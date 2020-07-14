/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Used to assert a report entries.
 */
public class ReportEntryAssert extends AbstractPioneerAssert<ReportEntryAssert, List<Map.Entry<String, String>>> {

	ReportEntryAssert(List<Map.Entry<String, String>> entries, int expected) {
		super(entries, ReportEntryAssert.class, expected);
	}

	public void withKeyAndValue(String key, String value) {
		if (expected != 1)
			throw new IllegalArgumentException("Can not verify key and value for non-single report entry!");
		assertThat(actual).containsExactly(new AbstractMap.SimpleEntry<>(key, value));
	}

	public void withValues(String... expected) {
		Stream<String> values = actual.stream().map(Map.Entry::getValue);
		assertThat(values).containsExactlyInAnyOrder(expected);
	}

	public void withKeyValuePairs(String... keyAndValuePairs) {
		if (keyAndValuePairs.length % 2 != 0)
			throw new IllegalArgumentException("Can not verify key-value pairs because some elements are missing.");
		assertThat(actual).containsExactlyInAnyOrderElementsOf(asEntryList(keyAndValuePairs));
	}

	private Iterable<AbstractMap.SimpleEntry<String, String>> asEntryList(String... values) {
		List<AbstractMap.SimpleEntry<String, String>> entryList = new ArrayList<>();
		for (int i = 0; i < values.length; i += 2) {
			entryList.add(new AbstractMap.SimpleEntry<>(values[i], values[i + 1]));
		}
		return entryList;
	}

}
