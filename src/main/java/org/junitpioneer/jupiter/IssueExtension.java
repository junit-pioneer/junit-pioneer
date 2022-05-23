/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener.REPORT_ENTRY_KEY;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.internal.PioneerAnnotationUtils;

/**
 * This class implements the functionality for the {@code @Issue} annotation.
 *
 * @see Issue
 */
class IssueExtension implements BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) {
		PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, Issue.class).ifPresent(annotation -> {
			String issueId = annotation.value();
			context.publishReportEntry(REPORT_ENTRY_KEY, issueId);
		});
	}

}
