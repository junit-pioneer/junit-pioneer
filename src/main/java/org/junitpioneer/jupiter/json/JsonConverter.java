/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.io.InputStream;

/**
 * A JSON Parsers which parses an input stream into a Node.
 * @since tbd
 */
interface JsonConverter {

	/**
	 * Convert the given {@code stream} into a {@link Node}.
	 *
	 * @param stream the stream that should be converted
	 * @return the {@link Node} for the stream, never {@code null}
	 */
	Node toNode(InputStream stream);

}
