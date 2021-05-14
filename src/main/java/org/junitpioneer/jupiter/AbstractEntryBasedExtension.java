/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * An abstract base class for entry-based extensions, where entries (key-value
 * pairs) can be cleared or set.
 *
 * @param <K> The entry's key type.
 * @param <V> The entry's value type.
 */
abstract class AbstractEntryBasedExtension<K, V>
		implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	/**
	 * @param context The current extension context.
	 * @return The entry keys to be cleared.
	 */
	protected abstract Set<K> entriesToClear(ExtensionContext context);

	/**
	 * @param context The current extension context.
	 * @return The entry keys and values to be set.
	 */
	protected abstract Map<K, V> entriesToSet(ExtensionContext context);

	/**
	 * Removes the entry indicated by the specified key.
	 */
	protected abstract void clearEntry(K key);

	/**
	 * Gets the entry indicated by the specified key.
	 */
	protected abstract V getEntry(K key);

	/**
	 * Sets the entry indicated by the specified key.
	 */
	protected abstract void setEntry(K key, V value);

	/**
	 * Reports a warning about potentially unsafe practices.
	 */
	protected void reportWarning(ExtensionContext context) {
		// nothing reported by default
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		clearAndSetEntries(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		clearAndSetEntries(context);
	}

	private void clearAndSetEntries(ExtensionContext context) {
		Set<K> entriesToClear;
		Map<K, V> entriesToSet;

		try {
			entriesToClear = entriesToClear(context);
			entriesToSet = entriesToSet(context);
			preventClearAndSetSameEntries(entriesToClear, entriesToSet.keySet());
		}
		catch (IllegalStateException ex) {
			throw new ExtensionConfigurationException("Don't clear/set the same entry more than once.", ex);
		}

		if (entriesToClear.isEmpty() && entriesToSet.isEmpty())
			return;

		reportWarning(context);
		storeOriginalEntries(context, entriesToClear, entriesToSet.keySet());
		clearEntries(entriesToClear);
		setEntries(entriesToSet);
	}

	private void preventClearAndSetSameEntries(Collection<K> entriesToClear, Collection<K> entriesToSet) {
		entriesToClear
				.stream()
				.filter(entriesToSet::contains)
				.map(Object::toString)
				.reduce((e0, e1) -> e0 + ", " + e1)
				.ifPresent(duplicateEntries -> {
					throw new IllegalStateException(
						"Cannot clear and set the following entries at the same time: " + duplicateEntries);
				});
	}

	private void storeOriginalEntries(ExtensionContext context, Collection<K> entriesToClear,
			Collection<K> entriesToSet) {
		getStore(context).put(getStoreKey(context), new EntriesBackup(entriesToClear, entriesToSet));
	}

	private void clearEntries(Collection<K> entriesToClear) {
		entriesToClear.forEach(this::clearEntry);
	}

	private void setEntries(Map<K, V> entriesToSet) {
		entriesToSet.forEach(this::setEntry);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		restoreOriginalEntries(context);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		restoreOriginalEntries(context);
	}

	private void restoreOriginalEntries(ExtensionContext context) {
		getStore(context).getOrDefault(getStoreKey(context), EntriesBackup.class, new EntriesBackup()).restoreBackup();
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass()));
	}

	private Object getStoreKey(ExtensionContext context) {
		return context.getUniqueId();
	}

	private class EntriesBackup {

		private final Set<K> entriesToClear = new HashSet<>();
		private final Map<K, V> entriesToSet = new HashMap<>();

		public EntriesBackup() {
			// empty backup
		}

		public EntriesBackup(Collection<K> entriesToClear, Collection<K> entriesToSet) {
			Stream.concat(entriesToClear.stream(), entriesToSet.stream()).forEach(entry -> {
				V backup = AbstractEntryBasedExtension.this.getEntry(entry);
				if (backup == null)
					this.entriesToClear.add(entry);
				else
					this.entriesToSet.put(entry, backup);
			});
		}

		public void restoreBackup() {
			entriesToClear.forEach(AbstractEntryBasedExtension.this::clearEntry);
			entriesToSet.forEach(AbstractEntryBasedExtension.this::setEntry);
		}

	}

}
