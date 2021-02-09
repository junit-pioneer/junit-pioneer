/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.playwright;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;

import org.junit.jupiter.api.Test;

@PlaywrightTests
public class BrowserParameterResolverTests {

	@Test
	void injectBrowser(Browser browser) {
		assertThat(browser).isNotNull();
	}

	// TODO: find a way to test browser specific configuration

}
