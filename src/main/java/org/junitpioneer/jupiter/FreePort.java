/*
 * Copyright 2016-2022 the original author or authors.
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
import java.util.List;

import org.junitpioneer.jupiter.resource.Resource;
import org.junitpioneer.jupiter.resource.ResourceFactory;

public final class FreePort implements ResourceFactory<ServerSocket> {

	public FreePort() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public Resource<ServerSocket> create(List<String> arguments) throws Exception {
		return new FreePortResource();
	}

	private static final class FreePortResource implements Resource<ServerSocket> {

		private final ServerSocket serverSocket;

		FreePortResource() throws IOException {
			this.serverSocket = new ServerSocket(0);
		}

		@Override
		public ServerSocket get() {
			return serverSocket;
		}

		@Override
		public void close() throws IOException {
			this.serverSocket.close();
		}

	}

}
