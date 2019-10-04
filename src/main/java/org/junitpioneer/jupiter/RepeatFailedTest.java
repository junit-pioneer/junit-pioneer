/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RepeatFailedTestExtension.class)
@TestTemplate
// TODO: comment
public @interface RepeatFailedTest {

	int value();

	LogLevel logFailedTestOn() default LogLevel.OFF;

	enum LogLevel {

		/**
		 * @see  java.util.logging.Level#OFF
		 */
		OFF,

		/**
		 * @see  java.util.logging.Level#FINER
		 */
		FINER,

		/**
		 * @see  java.util.logging.Level#FINE
		 */
		FINE,

		/**
		 * @see  java.util.logging.Level#CONFIG
		 */
		CONFIG,

		/**
		 * @see  java.util.logging.Level#INFO
		 */
		INFO,

		/**
		 * @see  java.util.logging.Level#WARNING
		 */
		WARNING,

		/**
		 * @see  java.util.logging.Level#SEVERE
		 */
		SEVERE;

	}

}
