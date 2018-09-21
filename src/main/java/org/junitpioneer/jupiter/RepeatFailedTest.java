package org.junitpioneer.jupiter;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

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
		 * @see  java.util.logging.Level#FIN
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
