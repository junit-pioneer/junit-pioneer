/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.playwright;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(PlaywrightParameterResolver.class)
@ExtendWith(BrowserTypeParameterResolver.class)
@ExtendWith(BrowserParameterResolver.class)
@ExtendWith(BrowserContextParameterResolver.class)
@Test
public @interface PlaywrightTest {

	BrowserName browserType() default BrowserName.FIREFOX;

	enum BrowserName {
		CHROMIUM, FIREFOX, WEBKIT
	}

}
