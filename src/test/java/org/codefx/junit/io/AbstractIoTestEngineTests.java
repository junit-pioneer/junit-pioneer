/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io;

import static java.util.Arrays.stream;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

public abstract class AbstractIoTestEngineTests extends AbstractJupiterTestEngineTests {

	protected ExecutionEventRecorder executeTests(Class<?> type, String... methodNames) {
		//@formatter:off
		DiscoverySelector[] selectors = stream(methodNames)
				.map(methodName -> selectMethod(type, methodName))
				.toArray(DiscoverySelector[]::new);
		LauncherDiscoveryRequest request = request().selectors(selectors).build();
		//@formatter:on
		return executeTests(request);
	}

}
