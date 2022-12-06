/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.internal.PioneerUtils;

/**
 * An abstract base class for entry-based extensions, where entries (key-value
 * pairs) can be cleared, set, or restored.
 *
 * @param <K> The entry key type.
 * @param <V> The entry value type.
 * @param <B> The bulk collection type of the entire key-value set
 * @param <C> The clear annotation type.
 * @param <S> The set annotation type.
 * @param <R> The restore annotation type.
 */
abstract class AbstractEntryBasedExtension<K, V, B extends Map<?,?>, C extends Annotation, S extends Annotation, R extends Annotation>
		implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

	/** Key to indicate storage is for an incremental backup object */
	private static final String INCREMENTAL_KEY = "inc";

	/** Key to indicate storage is for a complete backup object */
	private static final String COMPLETE_KEY = "full";

	@Override
	public void beforeAll(ExtensionContext context) {
		applyForAllContexts(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		applyForAllContexts(context);
	}

	private void applyForAllContexts(ExtensionContext originalContext) {

		final boolean fullRestore = PioneerAnnotationUtils.isAnnotationPresent(originalContext, getRestoreAnnotationType());

		if (fullRestore) {
			B bulk = this.getAllCurrentEntries();
			storeOriginalCompleteEntries(originalContext, bulk);
		}

		/*
		 * We cannot use PioneerAnnotationUtils#findAllEnclosingRepeatableAnnotations(ExtensionContext, Class) or the
		 * like as clearing and setting might interfere. Therefore, we have to apply the extension from the outermost
		 * to the innermost ExtensionContext.
		 */
		List<ExtensionContext> contexts = PioneerUtils.findAllContexts(originalContext);
		Collections.reverse(contexts);
		contexts.forEach(currentContext -> clearAndSetEntries(currentContext, originalContext, ! fullRestore));
	}

	private void clearAndSetEntries(ExtensionContext currentContext, ExtensionContext originalContext,
			boolean doIncrementalBackup) {
		currentContext.getElement().ifPresent(element -> {
			Set<K> entriesToClear;
			Map<K, V> entriesToSet;

			try {
				entriesToClear = findEntriesToClear(element);
				entriesToSet = findEntriesToSet(element);
				preventClearAndSetSameEntries(entriesToClear, entriesToSet.keySet());
			}
			catch (IllegalStateException ex) {
				throw new ExtensionConfigurationException("Don't clear/set the same entry more than once.", ex);
			}

			if (entriesToClear.isEmpty() && entriesToSet.isEmpty())
				return;

			reportWarning(currentContext);

			// Only backup original values if we didn't already do bulk storage of the original state
			if (doIncrementalBackup) {
				storeOriginalIncrementalEntries(originalContext, entriesToClear, entriesToSet.keySet());
			}

			clearEntries(entriesToClear);
			setEntries(entriesToSet);
		});
	}

	private Set<K> findEntriesToClear(AnnotatedElement element) {
		return findAnnotations(element, getClearAnnotationType())
				.map(clearKeyMapper())
				.collect(PioneerUtils.distinctToSet());
	}

	private Map<K, V> findEntriesToSet(AnnotatedElement element) {
		return findAnnotations(element, getSetAnnotationType()).collect(toMap(setKeyMapper(), setValueMapper()));
	}

	private <A extends Annotation> Stream<A> findAnnotations(AnnotatedElement element, Class<A> clazz) {
		return AnnotationSupport.findRepeatableAnnotations(element, clazz).stream();
	}

	@SuppressWarnings("unchecked")
	private Class<C> getClearAnnotationType() {
		return (Class<C>) getActualTypeArgumentAt(3);
	}

	@SuppressWarnings("unchecked")
	private Class<S> getSetAnnotationType() {
		return (Class<S>) getActualTypeArgumentAt(4);
	}

	@SuppressWarnings("unchecked")
	private Class<B> getBulkType() {
		return (Class<B>) getActualTypeArgumentAt(2);
	}

	@SuppressWarnings("unchecked")
	private Class<R> getRestoreAnnotationType() {
		return (Class<R>) getActualTypeArgumentAt(5);
	}

	private Type getActualTypeArgumentAt(int index) {
		ParameterizedType abstractEntryBasedExtensionType = (ParameterizedType) getClass().getGenericSuperclass();
		Type type = abstractEntryBasedExtensionType.getActualTypeArguments()[index];
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getRawType();
		} else {
			return type;
		}
	}

	private void preventClearAndSetSameEntries(Collection<K> entriesToClear, Collection<K> entriesToSet) {
		String duplicateEntries = entriesToClear
				.stream()
				.filter(entriesToSet::contains)
				.map(Object::toString)
				.collect(joining(", "));
		if (!duplicateEntries.isEmpty())
			throw new IllegalStateException(
				"Cannot clear and set the following entries at the same time: " + duplicateEntries);
	}

	private void storeOriginalIncrementalEntries(ExtensionContext context, Collection<K> entriesToClear,
			Collection<K> entriesToSet) {
		getStore(context).put(getStoreKey(context, INCREMENTAL_KEY), new EntriesBackup(entriesToClear, entriesToSet));
	}

	private void storeOriginalCompleteEntries(ExtensionContext context, B originalEntries) {
		getStore(context).put(getStoreKey(context, COMPLETE_KEY), originalEntries);
	}


	/**
	 * Restore the complete original state of the entries as they were prior to this ExtensionContext,
	 * if the complete state was initially stored in a BeforeXXX event.
	 *
	 * @param originalContext
	 * @return true if a complete backup exists and was used to restore, false if not.
	 */
	private boolean restoreOriginalCompleteEntries(ExtensionContext originalContext) {
		B bulk = getStore(originalContext).get(getStoreKey(originalContext, COMPLETE_KEY), getBulkType());

		if (bulk != null) {
			this.setAllCurrentEntries(bulk);
			return true;
		} else {
			// No complete backup - false will let the caller know to continue w/ an incremental restore
			return false;
		}
	}

	private void clearEntries(Collection<K> entriesToClear) {
		entriesToClear.forEach(this::clearEntry);
	}

	private void setEntries(Map<K, V> entriesToSet) {
		entriesToSet.forEach(this::setEntry);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		restoreForAllContexts(context);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		restoreForAllContexts(context);
	}

	private void restoreForAllContexts(ExtensionContext originalContext) {

		// Try a complete restore first
		if (! restoreOriginalCompleteEntries(originalContext) ) {
				// A complete backup is not available, so restore incrementally from innermost to outermost
				PioneerUtils.findAllContexts(originalContext).forEach(__ -> restoreOriginalIncrementalEntries(originalContext));
		}
	}

	private void restoreOriginalIncrementalEntries(ExtensionContext originalContext) {
		getStore(originalContext)
				.getOrDefault(getStoreKey(originalContext, INCREMENTAL_KEY), EntriesBackup.class, new EntriesBackup())
				.restoreBackup();
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass()));
	}

	private String getStoreKey(ExtensionContext context, String discriminator) {
		return context.getUniqueId() + "-" + discriminator;
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

	/**
	 * @return Mapper function to get the key from a clear annotation.
	 */
	protected abstract Function<C, K> clearKeyMapper();

	/**
	 * @return Mapper function to get the key from a set annotation.
	 */
	protected abstract Function<S, K> setKeyMapper();

	/**
	 * @return Mapper function to get the value from a set annotation.
	 */
	protected abstract Function<S, V> setValueMapper();

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

	/**
	 * Return a Map of all current entries as reported from the current runtime environment.
	 *
	 * The returned Map must not be null and its key-value pairs must follow the rules for entries
	 * of its type.  For instance, Environment vars never contain null values, System properties may.
	 *
	 * @return A non-null Map that contains all key-values of the runtime for this entry type.
	 */
	protected abstract B getAllCurrentEntries();

	/**
	 * Update the current runtime environment to match the passed entries map.
	 *
	 * The key-value pairs in the Map must follow the rules for entries of its type.
	 * For instance, Environment vars never contain null values, System properties may.
	 *
	 * @param entries Not null.
	 */
	protected abstract void setAllCurrentEntries(B entries);

}
