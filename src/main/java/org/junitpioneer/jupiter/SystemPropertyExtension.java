/*
 * Copyright 2016-2021 the original author or authors.
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
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.internal.PioneerUtils;

class SystemPropertyExtension extends AbstractEntryBasedExtension<String, String> {

	@Override
	protected Set<String> entriesToClear(ExtensionContext context) {
		return AnnotationSupport
				// This extension implements `BeforeAllCallback` and `BeforeEachCallback` and so if an outer class
				// (i.e. a class that the test class is @Nested within) uses this extension, this method will be
				// called on those extension points and discover the properties to set/clear. That means we don't need
				// to search for enclosing annotations here.
				.findRepeatableAnnotations(context.getElement(), ClearSystemProperty.class)
				.stream()
				.map(ClearSystemProperty::key)
				.collect(PioneerUtils.distinctToSet());
	}

	@Override
	protected Map<String, String> entriesToSet(ExtensionContext context) {
		return AnnotationSupport
				// This extension implements `BeforeAllCallback` and `BeforeEachCallback` and so if an outer class
				// (i.e. a class that the test class is @Nested within) uses this extension, this method will be
				// called on those extension points and discover the properties to set/clear. That means we don't need
				// to search for enclosing annotations here.
				.findRepeatableAnnotations(context.getElement(), SetSystemProperty.class)
				.stream()
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
