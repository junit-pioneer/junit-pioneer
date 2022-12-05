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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * pairs) can be cleared or set.
 *
 * @param <K> The entry key type.
 * @param <V> The entry value type.
 * @param <C> The clear annotation type.
 * @param <S> The set annotation type.
 */
abstract class AbstractEntryBasedExtension<K, V, C extends Annotation, S extends Annotation>
		implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

	@Override
	public void beforeAll(ExtensionContext context) {
		applyForAllContexts(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		applyForAllContexts(context);
	}

	private void applyForAllContexts(ExtensionContext originalContext) {

		// If true, a complete copy of the current state of related environment is made
		boolean fullRestoreNeeded =
				PioneerAnnotationUtils.isAnnotationPresent(originalContext, RestoreSystemProperties.class);

		/*
		 * We cannot use PioneerAnnotationUtils#findAllEnclosingRepeatableAnnotations(ExtensionContext, Class) or the
		 * like as clearing and setting might interfere. Therefore, we have to apply the extension from the outermost
		 * to the innermost ExtensionContext.
		 */
		List<ExtensionContext> contexts = PioneerUtils.findAllContexts(originalContext);
		Collections.reverse(contexts);
		contexts.forEach(currentContext -> clearAndSetEntries(currentContext, originalContext, !fullRestoreNeeded));
	}

	private void clearAndSetEntries(
			ExtensionContext currentContext, ExtensionContext originalContext, boolean storeOriginal) {
		currentContext.getElement().ifPresent(element -> {
			Set<K> entriesToClear;
			Map<K, V> entriesToSet;

			try {
				entriesToClear = findEntriesToClear(element);
				entriesToSet = findEntriesToSet(element);

				//It seems like order is important here and shouldn't always be an error.
				//If a prop is marked as cleared in a superclass and set on a method, this is OK, right?
				//I think the intention here is for this to be OK (we are in a context loop), but I think
				//the loop only applies to @Nested tests, not superclass/subclass relationships.
				System.setProperties(null);
				preventClearAndSetSameEntries(entriesToClear, entriesToSet.keySet());
			}
			catch (IllegalStateException ex) {
				throw new ExtensionConfigurationException("Don't clear/set the same entry more than once.", ex);
			}

			if (entriesToClear.isEmpty() && entriesToSet.isEmpty())
				return;

			reportWarning(currentContext);


			// If this is false, backup of original values is done elsewhere
			if (storeOriginal) {
				storeOriginalEntries(originalContext, entriesToClear, entriesToSet.keySet());
			}

			clearEntries(entriesToClear);
			setEntries(entriesToSet);
		});
	}

	abstract protected Set<Map.Entry<Object, Object>> getAllEntries();

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
		return (Class<C>) getActualTypeArgumentAt(2);
	}

	@SuppressWarnings("unchecked")
	private Class<S> getSetAnnotationType() {
		return (Class<S>) getActualTypeArgumentAt(3);
	}

	private Type getActualTypeArgumentAt(int index) {
		ParameterizedType abstractEntryBasedExtensionType = (ParameterizedType) getClass().getGenericSuperclass();
		return abstractEntryBasedExtensionType.getActualTypeArguments()[index];
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
	public void afterEach(ExtensionContext context) {
		restoreForAllContexts(context);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		restoreForAllContexts(context);
	}

	private void restoreForAllContexts(ExtensionContext originalContext) {
		// restore from innermost to outermost
		PioneerUtils.findAllContexts(originalContext).forEach(__ -> restoreOriginalEntries(originalContext));
	}

	private void restoreOriginalEntries(ExtensionContext originalContext) {
		getStore(originalContext)
				.getOrDefault(getStoreKey(originalContext), EntriesBackup.class, new EntriesBackup())
				.restoreBackup();
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

		private final boolean completeBackup;

		public EntriesBackup() {
			// empty backup
			completeBackup = false;
		}

		/**
		 * Create a backup where entriesToClear and entriesToSet are the only properties that will
		 * be modified, so they are the only ones that need to be restored.
		 *
		 * @param entriesToClear
		 * @param entriesToSet
		 */
		public EntriesBackup(Collection<K> entriesToClear, Collection<K> entriesToSet) {

			completeBackup = false;

			Stream.concat(entriesToClear.stream(), entriesToSet.stream()).forEach(entry -> {
				V backup = AbstractEntryBasedExtension.this.getEntry(entry);
				if (backup == null)
					this.entriesToClear.add(entry);
				else
					this.entriesToSet.put(entry, backup);
			});
		}

		/**
		 * Create a backup where the entriesToSet is the complete state that should be restored.
		 *
		 * <p>
		 * Any entries not present in the entriesToSet should be removed.
		 * </p>
		 * @param entriesToSet
		 */
		public EntriesBackup(Map<K, V> entriesToSet) {
			this.completeBackup = true;
			this.entriesToSet.putAll(entriesToSet);
		}


		public void restoreBackup() {
			if (completeBackup) {

				// entriesToSet is the complete and total state that should be restored

				entriesToSet.forEach(AbstractEntryBasedExtension.this::setEntry);

				// remove any other entries
				AbstractEntryBasedExtension.this.getAllEntries().stream().forEach(entry -> {
					String existingKey = entry.getKey().toString();
					if (! entriesToSet.containsKey(existingKey)) {

						// This won't work as is - force the keys to always be Strings?  Seems reasonable.
						// What about object values?
						// If everything becomes objects, then no generics are needed - maybe that is better.
						AbstractEntryBasedExtension.this.clearEntry(existingKey);
					}
				});

			} else {

				//Only the properties in these two collections need to be restored
				entriesToClear.forEach(AbstractEntryBasedExtension.this::clearEntry);
				entriesToSet.forEach(AbstractEntryBasedExtension.this::setEntry);
			}
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

}
