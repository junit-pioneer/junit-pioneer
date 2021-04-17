/*
 * Copyright 2016-2021 the original author or authors.
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

import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfParameterExtension.class)
public @interface DisableIfAllParameters {

	/**
	 * Disable test cases if all parameters (converted to String with {@link Object#toString()})
	 * contain any of the the specified strings (according to {@link String#contains(CharSequence)}).
	 */
	String[] contains() default {};

	/**
	 * Disable test cases if all parameters (converted to String with {@link Object#toString()})
	 * match any of the specified regular expressions (according to {@link String#matches(String)}).
	 */
	String[] matches() default {};

}
