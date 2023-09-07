/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junitpioneer.internal.PioneerUtils;

class SimpleAggregator implements ArgumentsAggregator {

	public SimpleAggregator() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
			throws ArgumentsAggregationException {
		Class<?> type = context.getParameter().getType();
		Set<Constructor<?>> constructors = Arrays
				.stream(type.getConstructors())
				// only if the constructor parameters and the supplied values are equal length
				.filter(constructor -> constructor.getParameterCount() == accessor.size())
				.collect(toUnmodifiableSet());
		if (constructors.isEmpty())
			throw new ArgumentsAggregationException(format(
				"Could not aggregate arguments, no public constructor with %d parameters was found.", accessor.size()));
		return tryEachConstructor(constructors, accessor);
	}

	private Object tryEachConstructor(Set<Constructor<?>> constructors, ArgumentsAccessor accessor) {
		Object value = null;
		List<Constructor<?>> matchingConstructors = new ArrayList<>();
		for (Constructor<?> constructor : constructors) {
			try {
				Object[] arguments = new Object[accessor.size()];
				for (int i = 0; i < accessor.size(); i++) {
					// can't just check against types explicitly because JUnit might be able to convert to
					// the types that we need, so we have to "force" that by using ArgumentsAccessor::get
					// which invokes JUnit's built-in ArgumentConverter
					// we also wrap primitive types to avoid casting problems - Java does auto unboxing later
					arguments[i] = accessor.get(i, PioneerUtils.wrap(constructor.getParameterTypes()[i]));
				}
				value = constructor.newInstance(arguments);
				matchingConstructors.add(constructor);
			}
			catch (Exception ignored) {
				// continue, we throw an exception if no matching constructor is found
			}
		}
		if (value == null)
			throw new ArgumentsAggregationException(
				"Could not aggregate arguments, no matching public constructor was found.");
		if (matchingConstructors.size() > 1)
			throw new ArgumentsAggregationException(
				format("Could not aggregate arguments. Expected only one matching public constructor but found %d: %s",
					matchingConstructors.size(), matchingConstructors));
		return value;
	}

}
