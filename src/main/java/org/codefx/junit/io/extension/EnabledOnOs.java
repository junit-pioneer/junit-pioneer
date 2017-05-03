/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @EnabledOnOs} is used to signal that the annotated test class or
 * test method is <em>disabled</em> on all operating systems except the
 * specified ones.
 *
 * <p>When applied at the class level, all test methods within that class
 * are automatically disabled as well if not on the specified operating systems.
 *
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OsCondition.class)
public @interface EnabledOnOs {

	/**
	 * Operating systems on which the test or test container is enabled.
	 */
	OS[] value() default {};

}
