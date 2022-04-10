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

import org.junit.platform.commons.JUnitException;

/**
 * Indicates that no supported JSON parsing library was found at run time.
 */
class NoJsonParserConfiguredException extends JUnitException {

	NoJsonParserConfiguredException() {
		super("There is no available JSON parsing library. Currently supported library is Jackson");
	}

}
