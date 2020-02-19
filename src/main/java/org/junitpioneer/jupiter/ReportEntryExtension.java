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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

class ReportEntryExtension implements BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		context
				.getElement()
				.map(element -> AnnotationSupport.findRepeatableAnnotations(element, ReportEntry.class))
				.ifPresent(entries -> entries.forEach(entry -> publish(context, entry)));
	}

	private void publish(ExtensionContext context, ReportEntry entry) {
		if (!entry.key().isEmpty()) {
			context.publishReportEntry(entry.key(), entry.value());
		} else {
			context.publishReportEntry(entry.value());
		}
	}

}
