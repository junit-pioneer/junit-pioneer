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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Geolocation;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.Test;

public class Playground {

	@Test
	void mobileWithGeolocation() {
		Playwright playwright = Playwright.create();
		Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().withHeadless(true));
		BrowserContext context = browser
				.newContext(new Browser.NewContextOptions()
						.withDevice(playwright.devices().get("Pixel 2"))
						.withLocale("en-US")
						.withGeolocation(new Geolocation(41.889938, 12.492507))
						.withPermissions(Arrays.asList("geolocation")));
		Page page = context.newPage();
		page.navigate("https://nipafx.dev/");
		page.route(url -> {
			return true;
		}, route -> System.out.println(route));
		ElementHandle tagListHandle = page.querySelectorAll(".postFilter-module--entries--xXHQ6").get(1);
		List<String> anchors = tagListHandle
				.querySelectorAll("a")
				.stream()
				.map(handle -> handle.getAttribute("href"))
				//        .map(ElementHandle::innerHTML)
				.collect(toList());

		assertThat(anchors).hasSize(60);

		System.out.println(anchors);
	}

}
