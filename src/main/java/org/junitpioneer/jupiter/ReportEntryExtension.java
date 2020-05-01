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
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ALWAYS;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_ABORTED;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_FAILURE;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_SUCCESS;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

class ReportEntryExtension implements TestWatcher, BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) {
		findAnnotations(context).forEach(ReportEntryExtension::verifyKeyValueAreNotBlank);
	}

	private Stream<ReportEntry> findAnnotations(ExtensionContext context) {
		return PioneerAnnotationUtils.findAllEnclosingRepeatableAnnotations(context, ReportEntry.class);
	}

	private static void verifyKeyValueAreNotBlank(ReportEntry entry) {
		if (entry.key().isEmpty() || entry.value().isEmpty()) {
			String message = "Report entries can't have blank key or value: { key=\"%s\", value=\"%s\" }";
			throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
		}
	}

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
		// if the test is disabled, we consider the annotation disabled too and don't publish anything
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		publishOnConditions(context, ALWAYS, ON_SUCCESS);
	}

	@Override
	public void testAborted(ExtensionContext context, Throwable cause) {
		publishOnConditions(context, ALWAYS, ON_ABORTED);
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		publishOnConditions(context, ALWAYS, ON_FAILURE);
	}

	private void publishOnConditions(ExtensionContext context, ReportEntry.PublishCondition... conditions) {
		findAnnotations(context)
				.filter(entry -> Arrays.asList(conditions).contains(entry.when()))
				// we filter for empty keys/values because this is called if the test failed -
				// even if it's due to bad extension configuration (but we don't publish for those)
				.filter(entry -> !entry.key().isEmpty() && !entry.value().isEmpty())
				.forEach(entry -> context.publishReportEntry(entry.key(), entry.value()));
	}

}
