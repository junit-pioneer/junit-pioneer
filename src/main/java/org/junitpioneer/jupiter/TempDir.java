/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

/**
 * {@code TempDir} can be used to annotate a test or lifecycle method or
 * test class constructor parameter of type {@link Path} that should be
 * resolved into a temporary directory.
 *
 * <p>Since JUnit Jupiter 5.4, there's a
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-built-in-extensions-TempDirectory">
 * built-in {@code @TempDir} extension</a>. If you don't need support for
 * arbitrary file systems, you should consider using that instead of this
 * extension.
 *
 * @see TempDirectoryExtension
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TempDir {
}
