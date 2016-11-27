/*
 * Copyright 2015-2016 the original author or authors.
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

public enum OS {

	/*
	 * This class was written for demonstration purposes.
	 * It is not at all fit for production!
	 */

	NIX, MAC, WINDOWS;

	static OS determine() {
		String os = System.getProperty("os.name").toLowerCase();

		if (isWindows(os)) {
			return WINDOWS;
		}
		else if (isMac(os)) {
			return MAC;
		}
		else if (isUnix(os)) {
			return NIX;
		}
		else {
			throw new IllegalArgumentException("Unknown OS \"" + os + "\".");
		}
	}

	static OS[] except(OS... others) {
		List<OS> otherses = Arrays.asList(others);
		return Arrays.stream(OS.values()).filter(os -> !otherses.contains(os)).toArray(OS[]::new);
	}

	private static boolean isWindows(String os) {
		return os.contains("win");
	}

	private static boolean isMac(String os) {
		return os.contains("mac");
	}

	private static boolean isUnix(String os) {
		return os.contains("nix") || os.contains("nux");
	}

}
