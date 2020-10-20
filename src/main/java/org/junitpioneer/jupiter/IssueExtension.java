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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * This class implements the functionality for the {@code @Issue} annotation.
 *
 * @see Issue
 */
class IssueExtension implements BeforeEachCallback {

	static final String KEY = "Issue";

	@Override
	public void beforeEach(ExtensionContext context) {
		String issueId = readIssueIdFromAnnotation(context);
		context.publishReportEntry(KEY, issueId);
	}

	/**
	 * Reads the {@code @Issue} value from the annotation.
	 * @param context The Extensions context
	 * @return The read value
	 */
	String readIssueIdFromAnnotation(ExtensionContext context) {
		return AnnotationSupport
				.findAnnotation(context.getElement(), Issue.class)
				.map(Issue::value)
				.orElseThrow(() -> new ExtensionConfigurationException(
					"The extension is active, but the corresponding annotation could not be found. (This may be a bug.)"));
	}

}
