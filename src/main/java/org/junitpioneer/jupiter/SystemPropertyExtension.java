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

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;

class SystemPropertyExtension extends AbstractEntryBasedExtension<String, String> {

	@Override
	protected boolean isAnnotationPresent(ExtensionContext context) {
		return PioneerAnnotationUtils
				.isAnyRepeatableAnnotationPresent(context, ClearSystemProperty.class, SetSystemProperty.class);
	}

	@Override
	protected Set<String> entriesToClear(ExtensionContext context) {
		return PioneerAnnotationUtils
				.findClosestEnclosingRepeatableAnnotations(context, ClearSystemProperty.class)
				.map(ClearSystemProperty::key)
				.collect(PioneerUtils.distinctToSet());
	}

	@Override
	protected Map<String, String> entriesToSet(ExtensionContext context) {
		return PioneerAnnotationUtils
				.findClosestEnclosingRepeatableAnnotations(context, SetSystemProperty.class)
				.collect(toMap(SetSystemProperty::key, SetSystemProperty::value));
	}

	@Override
	protected void clearEntry(String key) {
		System.clearProperty(key);
	}

	@Override
	protected String getEntry(String key) {
		return System.getProperty(key);
	}

	@Override
	protected void setEntry(String key, String value) {
		System.setProperty(key, value);
	}

}
