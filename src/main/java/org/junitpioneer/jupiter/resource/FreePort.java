/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static java.lang.String.format;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.internal.PioneerPreconditions;

public final class FreePort implements ResourceFactory<ServerSocket> {

	public FreePort() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public Resource<ServerSocket> create(List<String> arguments) throws Exception {
		if (arguments.isEmpty())
			return new FreePortResource();
		else {
			try {
				int port = Integer.parseInt(arguments.get(0));
				return new FreePortResource(port);
			}
			catch (NumberFormatException exception) {
				throw new ExtensionConfigurationException(
					format("Could not parse port number %s for opening a socket", arguments.get(0)));
			}
		}
	}

	private static final class FreePortResource implements Resource<ServerSocket> {

		private final ServerSocket serverSocket;

		FreePortResource() throws IOException {
			this.serverSocket = new ServerSocket(0);
		}

		FreePortResource(int port) throws IOException {
			this.serverSocket = new ServerSocket(port);
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
