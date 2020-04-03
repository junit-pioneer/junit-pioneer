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

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

class ReportEntryExtension implements AfterEachCallback {

	// TODO: This can be replaced with specific TestWatcher interface methods, once we update to JUnit 5.4+
	@Override
	public void afterEach(ExtensionContext context) {
		final Optional<Throwable> ex = context.getExecutionException();
		findAnnotations(context)
				.filter(entry -> entry.when() == ReportEntry.PublishCondition.ALWAYS
						|| entry.when() == ReportEntry.PublishCondition.ON_SUCCESS && !ex.isPresent()
						|| entry.when() == ReportEntry.PublishCondition.ON_FAILURE && ex.isPresent())
				.forEach(entry -> context.publishReportEntry(entry.key(), entry.value()));
	}

	private Stream<ReportEntry> findAnnotations(ExtensionContext context) {
		return Utils
				.findRepeatableAnnotation(context, ReportEntry.class)
				.peek(ReportEntryExtension::verifyKeyValueAreNotBlank);
	}

	private static void verifyKeyValueAreNotBlank(ReportEntry entry) {
		if (entry.key().isEmpty() || entry.value().isEmpty()) {
			String message = "Report entries can't have blank key or value: { key=\"%s\", value=\"%s\" }";
			throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
		}
	}

}
