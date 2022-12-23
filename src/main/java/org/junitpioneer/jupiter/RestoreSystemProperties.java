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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @RestoreSystemProperties} is a JUnit Jupiter extension to restore the entire set of
 * system properties to the original value, or the value of the higher-level container, after the
 * annotated element has been executed.
 *
 * <p>Use this annotation when you need programmatically modify system properties in a test method
 * or in {@code @BeforeAll} / {@code @BeforeEach} blocks.
 * To simply set or clear a system property, consider {@link SetSystemProperty @SetSystemProperty} or
 * {@link ClearSystemProperty @ClearSystemProperty} instead.
 * </p>
 *
 * <p>{@code RestoreSystemProperties} can be used on the method and on the class level.
 * When placed on a test method, system properties are stored before the method is run and restored
 * after the method is complete.  Specifically, properties are stored after any {@code @BeforeAll}
 * methods have run and before any {@code @BeforeEach} methods.</p>
 *
 * <p>When placed on a test class, system properties are stored before the test class runs and
 * restored after the test class is complete, <em>in addition too</em> running before and after
 * each test method just as if the annotation was on each method.  An advanced usage could include
 * modifying some system properties in a {@code @BeforeAll} block to apply to all tests and
 * additional property modifications within some tests themselves, all while safely restoring
 * the state of the system properties after each test and after the entire test class.</p>
 *
 * <p>SetSystemProperty and ClearSystemProperty interaction....</p>
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link RestoreSystemProperties}, {@link SetSystemProperty},
 * {@link ReadsSystemProperty}, and {@link WritesSystemProperty}
 * are scheduled in a way that guarantees correctness under mutation of shared global state.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/system-properties/" target="_top">the documentation on
 * <code>@ClearSystemProperty, @SetSystemProperty and @RestoreSystemProperties</code></a>.
 * </p>
 *
 * <p>Note:  System properties are normally just strings, however, it is technically possible to bend the
 * rules a bit to store other objects.  If this is your situation, be aware that this extension
 * does a shallow copy of the system properties hashmap.</p>
 *
 * @since 1.9.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@WritesSystemProperty
@ExtendWith(SystemPropertyExtension.class)
public @interface RestoreSystemProperties {  }
