/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * @deprecated The extension was provided to the JUnit framework.
 */
@Deprecated(forRemoval = true, since = "3.0")
public interface LocaleProvider extends Supplier<Locale> {

	/*
	* @deprecated The extension was provided to the JUnit framework.
	 */
	@Deprecated(forRemoval = true, since = "3.0")
	interface NullLocaleProvider extends LocaleProvider {
	}

}
