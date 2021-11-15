/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.io.IOException;
import java.net.ServerSocket;

public final class FreePort {

	private final int portNumber;

	public FreePort() throws IOException {
		this.portNumber = getFreePortNumber();
	}

	private Integer getFreePortNumber() throws IOException {
		ServerSocket socket = new ServerSocket(0);
		int freePort = socket.getLocalPort();
		socket.close();
		return freePort;
	}

	public int number() {
		return portNumber;
	}

	public boolean isFreeNow() {
		try (ServerSocket ignored = new ServerSocket(portNumber)) {
			return true;
		}
		catch (IOException exception) {
			return false;
		}
	}

}
