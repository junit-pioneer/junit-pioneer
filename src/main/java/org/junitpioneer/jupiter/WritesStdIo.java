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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

/**
 * Marks tests that call {@code System.setIn()} or {@code System.setOut()} to set the static fields {@code System.in}/{@code System.out}.
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link StdIo}, {@link WritesStdIo}, and {@link WritesStdIo}
 * are scheduled in a way that guarantees correctness under mutation of shared global state.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on Standard input/output</a>.
 * </p>
 *
 * @since 0.9
 */
@ResourceLock(value = "java.lang.System.in", mode = ResourceAccessMode.READ_WRITE)
@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PACKAGE,
		ElementType.TYPE })
public @interface WritesStdIo {
}
