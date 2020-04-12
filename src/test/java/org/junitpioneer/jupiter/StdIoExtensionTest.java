/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.StdIOExtension.StdIn;
import org.junitpioneer.jupiter.StdIOExtension.StdOut;

@ExtendWith(StdIOExtension.class)
public class StdIoExtensionTest {

	BasicCommandLineApp app = new BasicCommandLineApp();

	@Test
	@DisplayName("catches the output on the standard out")
	void catchesOut(@Std StdOut out) {
		app.multiline();
		assertThat(out.linesArray()).containsExactly("Hello", "World!");
	}

	@Test
	@DisplayName("catches the input from the standard in")
	void catchesIn(@Std({ "Hello", "World!" }) StdIn in, @Std StdOut out) throws IOException {
		app.readAndWrite();
		assertThat(in.linesArray()).containsExactly("Hello", "World!");
		assertThat(out.linesArray()).containsExactly("Hello", "World!");
	}

	/**
	 * A sample class that I would write tests for.
	 */
	private static class BasicCommandLineApp {

		public void multiline() {
			System.out.println("Hello");
			System.out.println("World!");
		}

		public void readAndWrite() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String read = reader.readLine();
			System.out.println(read);
			read = reader.readLine();
			System.out.println(read);
		}

	}

}
