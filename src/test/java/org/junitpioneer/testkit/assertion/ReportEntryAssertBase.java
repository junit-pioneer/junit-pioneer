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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ListAssert;
import org.junitpioneer.testkit.assertion.reportentry.ReportEntryContentAssert;

/**
 * Used to assert a report entries.
 */
class ReportEntryAssertBase extends AbstractPioneerAssert<ReportEntryAssertBase, List<Map.Entry<String, String>>>
		implements ReportEntryContentAssert {

	ReportEntryAssertBase(List<Map.Entry<String, String>> entries, int expected) {
		super(entries, ReportEntryAssertBase.class, expected);
	}

	@Override
	public void withKeyAndValue(String key, String value) {
		if (expected != 1)
			throw new IllegalArgumentException("Can not verify key and value for non-single report entry!");
		withKeyValuePairs(key, value);
	}

	@Override
	public void withValues(String... expected) {
		Stream<String> values = actual.stream().map(Map.Entry::getValue);
		assertThat(values).containsExactlyInAnyOrder(expected);
	}

	@Override
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

	@Override
	public void asserting(Predicate<Map.Entry<String, String>> predicate) {
		this.actual.forEach(entry -> assertThat(predicate).accepts(entry));
	}

	@Override
	public void andThen(BiConsumer<String, String> testFunction) {
		this.actual.forEach(entry -> testFunction.accept(entry.getKey(), entry.getValue()));
	}

	private List<String> getValues() {
		return this.actual.stream().map(Map.Entry::getValue).collect(Collectors.toList());
	}

	private List<String> getKeys() {
		return this.actual.stream().map(Map.Entry::getKey).collect(Collectors.toList());
	}

	@Override
	public ListAssert<String> values() {
		return assertThat(getValues());
	}

	@Override
	public AbstractStringAssert<?> firstValue() {
		Optional<String> first = getValues().stream().findFirst();
		assertThat(first).isPresent();
		return assertThat(first.get());
	}

	@Override
	public AbstractStringAssert<?> anyValue() {
		Optional<String> any = getValues().stream().findAny();
		assertThat(any).isPresent();
		return assertThat(any.get());
	}

	@Override
	public AbstractStringAssert<?> value(int index) {
		List<String> elements = getValues();
		assertThat(elements).hasSize(index + 1);
		return assertThat(elements.get(index));
	}

	@Override
	public ListAssert<String> keys() {
		return assertThat(getKeys());
	}

	@Override
	public AbstractStringAssert<?> firstKey() {
		Optional<String> first = getKeys().stream().findFirst();
		assertThat(first).isPresent();
		return assertThat(first.get());
	}

	@Override
	public AbstractStringAssert<?> anyKey() {
		Optional<String> any = getKeys().stream().findAny();
		assertThat(any).isPresent();
		return assertThat(any.get());
	}

	@Override
	public AbstractStringAssert<?> key(int index) {
		List<String> elements = getKeys();
		assertThat(elements).hasSize(index + 1);
		return assertThat(elements.get(index));
	}

}
