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

/**
 * Exception thrown when JUnit Pioneer fails to load in a Jackson module.
 */
public class JacksonModuleNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -37749272149104838L;

	public JacksonModuleNotFoundException(String message, Exception exception) {
		super(message, exception);
	}

}
