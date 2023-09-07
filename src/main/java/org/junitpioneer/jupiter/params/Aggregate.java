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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.aggregator.AggregateWith;

/**
 * {@code @Aggregate} is a parameter annotation, used for simple argument aggregation.
 *
 * <p>The supplied values are expected to be able to be aggregated into a single argument,
 * which is in turn supplied to the {@code @ParameterizedTest} method.</p>
 *
 * <p>For more details (including its limitations) and examples, see
 * <a href="https://junit-pioneer.org/docs/simple-arguments-aggregator/" target="_top">the documentation on
 * Simple Arguments Aggregator</a>
 * </p>
 *
 * <p>This annotation is not compatible with {@link org.junitpioneer.jupiter.cartesian.CartesianTest} since
 * this expects a single parameter as opposed to {@link org.junitpioneer.jupiter.cartesian.CartesianTest}
 * requiring multiple parameters.
 * </p>
 *
 * @since 2.1
 * @see org.junit.jupiter.params.aggregator.ArgumentsAggregator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AggregateWith(SimpleAggregator.class)
public @interface Aggregate {
}
