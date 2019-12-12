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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * This class implements the functionality for the {@code @Issue} annotation.
 *
 * @see Issue
 */
@DisplayName("Issue extension")
class IssueExtension implements BeforeEachCallback {
	static final Namespace NAMESPACE = Namespace.create(IssueExtension.class);
	static final String KEY = "Issue";

	@Override
	public void beforeEach(ExtensionContext context) {
		if (isAnnotationPresentOnTestMethod(context)) {
			setIssueId(context);
		}
	}

	boolean isAnnotationPresentOnTestMethod(ExtensionContext context) {
		//@formatter:off
		return context.getTestMethod()
				.map(testMethod -> AnnotationSupport.isAnnotated(testMethod, Issue.class))
				.orElse(false);
		//@formatter:on
	}

	void setIssueId(ExtensionContext context) {
		String issueId = readIssueIdFromAnnotation(context);
		storeIssueId(context, issueId);
	}

	void storeIssueId(ExtensionContext context, String issueId) {
		context.getStore(NAMESPACE).put(KEY, issueId);
	}

	String readIssueIdFromAnnotation(ExtensionContext context) {
		//@formatter:off
		return AnnotationSupport
				.findAnnotation(context.getElement(), Issue.class)
				.map(IssueExtension::createIssueId)
				.orElseThrow(() -> new ExtensionConfigurationException("The extension is active, but the corresponding annotation could not be found. (This may be a bug.)"));
		//@formatter:on
	}

	static String createIssueId(Issue annotation) {
		return annotation.value();
	}

}
