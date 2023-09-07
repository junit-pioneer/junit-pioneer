/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

class DisableIfTestFailsExtension implements TestExecutionExceptionHandler, ExecutionCondition {

	/*
	 * Basic approach:
	 *  - in `handleTestExecutionException`: if it needs to deactivate other tests, add that info to the store
	 *  - in `evaluateExecutionCondition`: check store for that information
	 *
	 * Because the test method that failed and the ones that need to be disabled are different methods,
	 * the information to disable can't be in a store that belongs to any specific test method. Instead,
	 * add it to the store that belongs to the container where the extension is applied.
	 *
	 * Setting the information needs to be thread safe, so only positive results (i.e. tests must be disabled)
	 * will be set. The easiest way to do that is to simply use absence/presence of a key in the store
	 * as indicator, which means the specific value doesn't matter.
	 */

	private static final Namespace NAMESPACE = Namespace.create(DisableIfTestFailsExtension.class);
	private static final String DISABLED_KEY = "DISABLED_KEY";
	private static final String DISABLED_VALUE = "";

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		boolean disabled = context.getStore(NAMESPACE).get(DISABLED_KEY) != null;
		if (disabled)
			return ConditionEvaluationResult.disabled("Another failed with one of the specified exceptions.");
		else
			return ConditionEvaluationResult.enabled("No test failed with one of the specified exceptions (yet).");
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		// we assume `context` belongs to a test method, so in order to associate annotations with the
		// correct extension context (i.e. the one belonging to the test class), get the parent
		// (which should hence always exist!)
		ExtensionContext testClassContext = context.getParent().orElseThrow(IllegalStateException::new);
		findConfigurations(testClassContext)
				.filter(configuration -> configuration.shouldDisable(throwable))
				.forEach(
					configuration -> configuration.context().getStore(NAMESPACE).put(DISABLED_KEY, DISABLED_VALUE));
		throw throwable;
	}

	private static Stream<Configuration> findConfigurations(ExtensionContext context) {
		Optional<Class<?>> type = context.getTestClass();
		// type may not be present because of recursion to the parent context
		if (type.isEmpty())
			return Stream.empty();

		List<DisableIfTestFails> annotations = findAnnotationOn(type.get()).collect(toUnmodifiableList());
		Stream<Configuration> onClassConfig = createConfigurationFor(context, annotations);
		Stream<Configuration> onParentClassConfigs = context
				.getParent()
				.map(DisableIfTestFailsExtension::findConfigurations)
				.orElse(Stream.empty());

		List<Configuration> configurations = Stream
				.concat(onClassConfig, onParentClassConfigs)
				.collect(toUnmodifiableList());
		return configurations.stream();
	}

	private static Stream<Configuration> createConfigurationFor(ExtensionContext context,
			List<DisableIfTestFails> annotations) {
		// annotations can be empty if a nested class isn't annotated itself (but an outer class is)
		if (annotations.isEmpty())
			return Stream.empty();

		Set<Class<? extends Throwable>> onClassExceptions = annotations
				.stream()
				.map(DisableIfTestFails::with)
				// If the exceptions array is empty, we later need to disable on all exceptions.
				// The easiest way to achieve that is by replacing the empty array with a Throwable.class
				// because all exceptions extend it.
				.flatMap(exceptions -> exceptions.length == 0 ? Stream.of(Throwable.class) : Arrays.stream(exceptions))
				.collect(toSet());
		boolean disableOnAssertions = annotations.stream().anyMatch(DisableIfTestFails::onAssertion);
		Configuration onClassConfig = new Configuration(context, onClassExceptions, disableOnAssertions);

		return Stream.of(onClassConfig);
	}

	private static Stream<DisableIfTestFails> findAnnotationOn(Class<?> element) {
		if (element == null || element == Object.class)
			return Stream.empty();

		Stream<DisableIfTestFails> onElement = AnnotationSupport
				.findAnnotation(element, DisableIfTestFails.class)
				.stream();
		Stream<DisableIfTestFails> onInterfaces = Arrays
				.stream(element.getInterfaces())
				.flatMap(DisableIfTestFailsExtension::findAnnotationOn);
		Stream<DisableIfTestFails> onSuperclass = findAnnotationOn(element.getSuperclass());
		return Stream.of(onElement, onInterfaces, onSuperclass).flatMap(s -> s);
	}

	private static class Configuration {

		private final ExtensionContext context;
		private final Set<Class<? extends Throwable>> disableOnExceptions;
		private final boolean disableOnAssertions;

		public Configuration(ExtensionContext context, Set<Class<? extends Throwable>> disableOnExceptions,
				boolean disableOnAssertions) {
			this.context = context;
			this.disableOnExceptions = disableOnExceptions;
			if (disableOnExceptions.isEmpty())
				throw new IllegalArgumentException("List of exceptions to disable on must not be empty.");
			this.disableOnAssertions = disableOnAssertions;
		}

		public boolean shouldDisable(Throwable exception) {
			// don't disable on failed assumptions
			if (exception instanceof TestAbortedException)
				return false;
			if (exception instanceof AssertionError)
				return disableOnAssertions;
			return disableOnExceptions.stream().anyMatch(type -> type.isInstance(exception));
		}

		public ExtensionContext context() {
			return context;
		}

	}

}
