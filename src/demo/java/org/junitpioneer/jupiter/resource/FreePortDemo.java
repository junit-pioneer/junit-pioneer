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

import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

public class FreePortDemo {

	// tag::basic_free_port_example[]
	@Test
	void testFreePort(@New(FreePort.class) ServerSocket port) {
		assertThat(port).isNotNull();
		assertThat(port.isClosed()).isFalse();
	}
	// end::basic_free_port_example[]

	// tag::specific_port_example[]
	@Test
	void testSpecificPort(@New(value = FreePort.class, arguments = "1234") ServerSocket port) {
		assertThat(port).isNotNull();
		assertThat(port.isClosed()).isFalse();
		assertThat(port.getLocalPort()).isEqualTo(1234);
	}
	// end::specific_port_example[]

}
