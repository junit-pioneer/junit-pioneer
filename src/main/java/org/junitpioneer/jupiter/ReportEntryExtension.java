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

import static java.lang.String.format;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

class ReportEntryExtension implements BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		Utils
				.findRepeatableAnnotation(context, ReportEntry.class)
				.peek(ReportEntryExtension::verifyKeyValueAreNotBlank)
				.forEach(entry -> context.publishReportEntry(entry.key(), entry.value()));
	}

	private static void verifyKeyValueAreNotBlank(ReportEntry entry) {
		if (entry.key().isEmpty() || entry.value().isEmpty()) {
			String message = "Report entries can't have blank key or value: { key=\"%s\", value=\"%s\" }";
			throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
		}
	}

}
