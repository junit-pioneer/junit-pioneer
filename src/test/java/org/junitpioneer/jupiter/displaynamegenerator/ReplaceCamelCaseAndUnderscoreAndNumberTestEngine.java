/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import java.util.Optional;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

public class ReplaceCamelCaseAndUnderscoreAndNumberTestEngine implements TestEngine {

	@Override
	public String getId() {
		return "replace-camelcase-and-underscore-and-number";
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
		TestDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "ReplaceCamelCaseAndUnderscoreAndNumber Test");
		engineDiscoveryRequest.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			appendTestsInClass(selector.getJavaClass(), engineDescriptor);
		});
		return engineDescriptor;
	}

	private void appendTestsInClass(Class<?> javaClass, TestDescriptor engineDescriptor) {
		engineDescriptor.addChild(new ClassTestDescriptor(javaClass, engineDescriptor));
	}

	@Override
	public void execute(ExecutionRequest executionRequest) {
		TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();
		TestExecutor testExecutor = new TestExecutor(executionRequest.getEngineExecutionListener());
		testExecutor.execute(rootTestDescriptor);

	}

	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.junitpioneer.jupiter");
	}

	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("replace-camelcase-and-underscore-and-number-engine");
	}

	protected TestDescriptor discoverTests(LauncherDiscoveryRequest request) {
		return discover(request, UniqueId.forEngine(getId()));
	}

}
