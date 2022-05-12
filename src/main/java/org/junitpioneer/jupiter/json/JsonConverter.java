/*
 * Copyright 2016-2022 the original author or authors.
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
 * A JSON Converter which parses an input stream into a Node.
 * This class is a Pioneer abstraction which allows using different JSON parsers.
 */
interface JsonConverter {

	/**
	 * Convert the given {@code stream} into a {@link Node}.
	 *
	 * @param stream the stream that should be converted
	 * @return the {@link Node} for the stream, never {@code null}
	 */
	Node toNode(InputStream stream);

	/**
	 * Convert the given {@code value} into a {@link Node}
	 *
	 * @param value the json value that should be converted
	 * @param lenient whether the conversion should be lenient
	 * @return the {@link Node} for the value, never {@code null}
	 */
	Node toNode(String value, boolean lenient);

}
