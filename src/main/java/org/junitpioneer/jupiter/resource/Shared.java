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

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @Shared} is used to create a resource that is shared with multiple tests.
 *
 * <p>It is part of the "resources" JUnit Jupiter extension, which pertains to anything that needs
 * to be injected into tests and which may need to be started up or torn down. Temporary
 * directories are a common example.
 *
 * <p>This class is intended for <i>users</i>.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/resources/" target="_top">the documentation on resources</a>
 * and <a href="https://junit-pioneer.org/docs/temp-directory/">temporary directories</a>.</p>
 *
 * @since 1.9.0
 * @see Resource
 * @see ResourceFactory
 * @see Scope
 */
@ExtendWith(ResourceExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface Shared {

	/**
	 * The class of the resource factory to get the resource from.
	 */
	Class<? extends ResourceFactory<?>> factory();

	/**
	 * The unique name of the resource.
	 */
	String name();

	/**
	 * The scope for how long the resource will live.
	 *
	 * <p>The default scope is {@link Shared.Scope#SOURCE_FILE}.</p>
	 *
	 * @see Shared.Scope#SOURCE_FILE
	 * @see Shared.Scope#GLOBAL
	 */
	Scope scope() default Scope.SOURCE_FILE;

	/**
	 * {@code Scope} specifies how long a shared resource lives.
	 *
	 * <p>It is part of the "resources" JUnit Jupiter extension, which pertains to anything that needs
	 * to be injected into tests and which may need to be started up or torn down. Temporary
	 * directories are a common example.
	 *
	 * <p>For more details and examples, see
	 * <a href="https://junit-pioneer.org/docs/resources/" target="_top">the documentation on resources</a>.</p>
	 *
	 * @since 1.9.0
	 * @see Resource
	 * @see ResourceFactory
	 * @see Shared
	 */
	enum Scope {

		/**
		 * <p>At this scope, a shared resource will last as long as the entire test suite.</p>
		 */
		GLOBAL,

		/**
		 * <p>At this scope, a shared resource will last as long as the test file it is defined in.</p>
		 */
		SOURCE_FILE

	}

}
