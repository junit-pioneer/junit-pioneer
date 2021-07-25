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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;

class DisableIfTestFailsExtension implements TestExecutionExceptionHandler, ExecutionCondition {

	/*
	 * Basic approach:
	 *  - in `handleTestExecutionException`: if need to deactivate other tests, add that info to the store
	 *  - in `evaluateExecutionCondition`: check store for that information
	 *
	 * Because the test method that failed and the ones that need to be disabled are different methods,
	 * the information to disable can't be in a store that belongs to any specific test method. Instead
	 * add it to the store that belongs to the container where the extension is applied.
	 *
	 * Setting the information needs to be thread safe, so only positive results (i.e. tests must be disabled)
	 * will be set. The easiest way to do that is to simply use absence/presence of a key in the store
	 * as indicator, which means the specific value doesn't.
	 */

	private static final Namespace NAMESPACE = Namespace.create(DisableIfTestFailsExtension.class);
	private static final String DISABLED_KEY = "DISABLED_KEY";
	private static final String DISABLED_VALUE = "";

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		boolean disabled = context.getStore(NAMESPACE).get(DISABLED_KEY) != null;
		if (disabled)
			return ConditionEvaluationResult.disabled("Another test threw one of the specified exceptions.");
		else
			return ConditionEvaluationResult.enabled("No test threw one of the specified exceptions (yet).");
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		// we assume `context` belongs to a test method, so in order to associate annotations with the
		// correct extension context (i.e. the one belonging to the test class), get the parent
		// (which should hence always exist!)
		ExtensionContext testClassContext = context.getParent().orElseThrow(IllegalStateException::new);
		findOnContextClass(testClassContext)
				.filter(configuration -> configuration.shouldDisable(throwable))
				.findFirst()
				.ifPresent(configuration -> configuration.context().getStore(NAMESPACE).put(DISABLED_KEY, DISABLED_VALUE));
		throw throwable;
	}

	private static Stream<Configuration> findOnContextClass(ExtensionContext context) {
		Optional<Class<?>> type = context.getTestClass();
		if (!type.isPresent())
			return Stream.empty();

		List<Class<? extends Throwable>> onClassExceptions = findOnType(type.get())
				.map(DisableIfTestFails::with)
				// If the exceptions array is empty, we later need to disable on all exceptions.
				// The easiest way to achieve that is by replacing the empty array with a Throwable.class
				// because all exceptions extend it.
				.flatMap(exceptions -> exceptions.length == 0 ? Stream.of(Throwable.class) : Arrays.stream(exceptions))
				.distinct()
				.collect(toList());
		//@formatter:off
		Stream<Configuration> onClassConfig = onClassExceptions.isEmpty()
				? Stream.empty()
				: Stream.of(new Configuration(context, onClassExceptions));
		//@formatter:on
		Stream<Configuration> onParentClassConfigs = context
				.getParent()
				.map(DisableIfTestFailsExtension::findOnContextClass)
				.orElse(Stream.empty());

		return Stream.concat(onClassConfig, onParentClassConfigs);
	}

	private static Stream<DisableIfTestFails> findOnType(Class<?> element) {
		if (element == null || element == Object.class)
			return Stream.empty();

		Stream<DisableIfTestFails> onElement = AnnotationSupport
				.findAnnotation(element, DisableIfTestFails.class)
				.map(Stream::of)
				.orElse(Stream.empty());
		Stream<DisableIfTestFails> onInterfaces = Arrays
				.stream(element.getInterfaces())
				.flatMap(DisableIfTestFailsExtension::findOnType);
		Stream<DisableIfTestFails> onSuperclass = findOnType(element.getSuperclass());
		return Stream.of(onElement, onInterfaces, onSuperclass).flatMap(s -> s);
	}

	private static class Configuration {

		private final ExtensionContext context;
		private final List<Class<? extends Throwable>> disableOnExceptions;

		public Configuration(ExtensionContext context, List<Class<? extends Throwable>> disableOnExceptions) {
			this.context = context;
			this.disableOnExceptions = disableOnExceptions;
			if (disableOnExceptions.isEmpty())
				throw new IllegalArgumentException("List of exceptions to disable on must not be empty.");
		}

		public boolean shouldDisable(Throwable exception) {
			return disableOnExceptions.stream().anyMatch(type -> type.isInstance(exception));
		}

		public ExtensionContext context() {
			return context;
		}

	}

}
