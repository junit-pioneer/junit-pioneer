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

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

/**
 * If you are implementing an {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * for {@link CartesianProductTest}, it has to implement this annotation to 'consume' the annotation on
 * your test. For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 *
 * @param <A> the annotation holding necessary data for providing the arguments
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see CartesianProductTestExtension
 */
public interface CartesianAnnotationConsumer<A extends Annotation> extends Consumer<A> {
}
