/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @Dir} is a shorthand for {@code @New(TemporaryDirectory.class)}.
 *
 * <p>It is part of the "resources" JUnit Jupiter extension, which pertains to anything that needs
 * to be injected into tests and which may need to be started up or torn down. Temporary
 * directories are a common example.
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/temp-directory/">the documentation on temporary directories</a>.</p>
 *
 * @since 1.9.0
 * @see New
 * @see TemporaryDirectory
 * @see Resource
 * @see ResourceFactory
 */
@New(TemporaryDirectory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Dir {
}
