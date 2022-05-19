/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Helper class to generate objects for JUnit {@link org.junit.platform.launcher.TestPlan}.
 */
public class TestPlanHelper {

	public static TestIdentifier createTestIdentifier(String uniqueId) {
		return TestIdentifier.from(new TestDescriptorStub(UniqueId.root("test", uniqueId), uniqueId));
	}

	static class TestDescriptorStub extends AbstractTestDescriptor {

		public TestDescriptorStub(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public TestDescriptor.Type getType() {
			return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
		}

	}

}
