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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A node representing a JSON structure.
 * This class is a Pioneer abstraction which allows using different JSON parsers.
 */
interface Node {

	/**
	 * @return {@code true} if the node represents an array of other nodes
	 */
	boolean isArray();

	/**
	 * @return all the elements of this potential array node
	 */
	Stream<Node> elements();

	/**
	 * Convert this node into the requested {@code type}
	 *
	 * @param type the type into which this node needs to be converted
	 * @param <T> the type
	 * @return the converted type
	 */
	<T> T toType(Class<T> type);

	/**
	 * Get the node value with the given name.
	 *
	 * @param name the name of the node
	 * @return the node for the given name
	 */
	Optional<Node> getNode(String name);

	/**
	 * Get the value of the node.
	 *
	 * @param typeHint the potential type of the value
	 * @return the node value
	 */
	Object value(Class<?> typeHint);

}
