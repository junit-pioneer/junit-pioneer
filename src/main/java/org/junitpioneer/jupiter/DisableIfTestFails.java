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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target({ TYPE, ANNOTATION_TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfTestFailsExtension.class)
public @interface DisableIfTestFails {

	Class<? extends Throwable>[] with() default {};

	boolean onAssertion() default true;

}
