/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import java.nio.file.Path;
import java.nio.file.Paths;

final class MorePaths {

	static Path rootTempDir() {
		return Paths.get(System.getProperty("java.io.tmpdir"));
	}

	private MorePaths() {
	}

}
