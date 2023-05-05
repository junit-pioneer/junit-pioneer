/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating the name of the JSON property that should be extracted into the method parameter.
 * <p>
 * If the test code is compiled with the {@code -parameters} flag, and the test method parameter's name
 * matches the JSON property's name, this annotation is not needed.
 * </p>
 *
 * @since 1.7.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {

	/**
	 * The name of the JSON property.
	 */
	String value();

}
