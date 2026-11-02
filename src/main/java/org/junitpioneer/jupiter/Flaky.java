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

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;

/**
 * Marks a {@code @Test} as "flaky" or unreliable.
 * If the initial test run is unsuccessful, the extension will try to
 * run it again, until successful or until the failure threshold
 * is reached ({@link #value()}).
 *
 * <p><strong>Note:</strong> Due to the implementation, the extension
 * does <em>not</em> get invoked if the initial test run is successful.
 * However, if it does get invoked, it will generate all the
 * test templates remaining.These may appear as skipped tests in
 * test reports, which could be misleading.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
// the extension is inherently thread-unsafe (has to wait for one execution before starting the next),
// so it forces execution of all retries onto the same thread
@Execution(SAME_THREAD)
@ExtendWith(FlakyExtension.class)
@TestTemplate
public @interface Flaky {

	/**
	 * The maximum number of invocations allowed, including the initial {@code @Test}.
	 *
	 * <p>Example: if this is set to <strong>4</strong> and the initial test fails, the extension will
	 * generate three additional test template invocations.</p>
	 *
	 * <p>The minimum required value for this is <strong>2</strong>, meaning the extension has to run
	 * at least one test template. Giving a lower value will throw an
	 * {@link org.junit.jupiter.api.extension.ExtensionConfigurationException}.</p>
	 */
	int value();

	String name() default "[{index}]";

}
