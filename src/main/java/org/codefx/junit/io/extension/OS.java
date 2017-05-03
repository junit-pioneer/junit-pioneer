/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io.extension;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Enumerates the operating systems that JUnit Pioneer can discern.
 */
public enum OS {

	/**
	 * A Linux-based operating system.
	 */
	LINUX,

	/**
	 * A Mac operating system.
	 */
	MAC,

	/**
	 * A Windows operating system.
	 */
	WINDOWS;

	private static final OS RUNNING_OS = determineOs();

	static OS determine() {
		return RUNNING_OS;
	}

	private static OS determineOs() {
		String lowerCaseOsName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		// these checks were inspired by how Apache Commons-Lang's 'SystemUtils' solves the problem
		// https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SystemUtils.java
		if (lowerCaseOsName.startsWith("linux")) {
			return OS.LINUX;
		}
		if (lowerCaseOsName.startsWith("mac")) {
			return OS.MAC;
		}
		if (lowerCaseOsName.startsWith("windows")) {
			return OS.WINDOWS;
		}

		throw new IllegalArgumentException("Unknown OS \"" + System.getProperty("os.name") + "\".");
	}

	static OS[] except(OS... others) {
		List<OS> othrs = Arrays.asList(others);
		// @formatter:off
		return Arrays.stream(OS.values())
				.filter(os -> !othrs.contains(os))
				.toArray(OS[]::new);
		// @formatter:on
	}

}
