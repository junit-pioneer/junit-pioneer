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
import static org.junitpioneer.playwright.PlaywrightTest.BrowserName.CHROMIUM;
import static org.junitpioneer.playwright.PlaywrightTest.BrowserName.FIREFOX;
import static org.junitpioneer.playwright.PlaywrightTest.BrowserName.WEBKIT;

import com.microsoft.playwright.BrowserType;

import org.junit.jupiter.api.Test;

@PlaywrightTests
public class BrowserTypeParameterResolverTests {

	@Test
	void injectBrowser(BrowserType browserType) {
		assertThat(browserType).isNotNull();
	}

	@PlaywrightTest
	void injectFirefoxByDefault(BrowserType browserType) {
		assertThat(browserType.name()).isEqualTo("firefox");
	}

	@PlaywrightTest(browserType = FIREFOX)
	void injectFirefox(BrowserType browserType) {
		assertThat(browserType.name()).isEqualTo("firefox");
	}

	@PlaywrightTest(browserType = CHROMIUM)
	void injectChromium(BrowserType browserType) {
		assertThat(browserType.name()).isEqualTo("chromium");
	}

	@PlaywrightTest(browserType = WEBKIT)
	void injectWebkit(BrowserType browserType) {
		assertThat(browserType.name()).isEqualTo("webkit");
	}

}
