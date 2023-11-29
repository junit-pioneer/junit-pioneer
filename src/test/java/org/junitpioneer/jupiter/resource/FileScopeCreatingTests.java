/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.resource.FileScopeCheckingTests.CountingResourceFactory;

@Order(0)
class FileScopeCreatingTests {

	// SEE `ResourceCheckingTests` FOR WHAT'S GOING ON HERE

	@Test
	@Order(0)
	void createResourceAndFactory(
			@Shared(name = "sharedString", factory = CountingResourceFactory.class) String resource) {
		assertThat(CountingResourceFactory.CREATED_RESOURCE_FACTORIES).hasValue(1);
		assertThat(CountingResourceFactory.CREATED_RESOURCES).hasValue(1);
		System.out.println("Bar");
	}

	@Test
	@Order(1)
	void verifyNothingWasClosed() {
		assertThat(CountingResourceFactory.CLOSED_RESOURCES).hasValue(0);
		assertThat(CountingResourceFactory.CLOSED_RESOURCE_FACTORIES).hasValue(0);
	}

}
