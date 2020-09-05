/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

/**
 * Exception thrown when the creation of the Pioneer report fails due an exception.
 */
public class IssueExtensionReportException extends RuntimeException {

	public IssueExtensionReportException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
