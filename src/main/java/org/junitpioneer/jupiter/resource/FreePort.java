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

/**
 * Resource factory for creating a {@link ServerSocket}.
 * This can be done via the {@link Shared} or {@link New} annotations.
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/free-port/" target="_top">the documentation on the FreePort extension</a>.</p>
 *
 * <p>This extension is based on the JUnit Pioneer abstract extension for resources.</p>
 * <p>For more information about that, see
 * <a href="https://junit-pioneer.org/docs/resources/" target="_top">the documentation on the resource extension.</a>.</p>
 *
 * @see Shared
 * @see New
 */
public final class FreePort implements ResourceFactory<ServerSocket> {

	/**
	 * Resource factories should not be instantiated directly, only
	 * by using {@code @New} or {@code @Shared}.
	 */
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

	/**
	 * Wrapper/resource class for creating a {@link ServerSocket} on a specific port.
	 * If no port number is specified then the port number is automatically allocated,
	 * typically from an ephemeral port range.
	 *
	 * <p>ServerSocket instances get closed automatically by the resource extension.</p>
	 *
	 * @see ServerSocket#ServerSocket(int)
	 */
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
