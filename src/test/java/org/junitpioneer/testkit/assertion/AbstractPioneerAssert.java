/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import org.assertj.core.api.AbstractAssert;

/**
 * A very basic extension of the AbstractAssert, used to add a quantity to assertions.
 * By storing this value in a field we don't have to refer back to it every time.
 *
 * Instead of
 * <p>assertThat(results).hasTests().thatStarted(3).thenFailed(3)</p>
 *
 * We can write
 * <p>assertThat(results).hasNumberOfTests(3).thatStarted().andAllOfThemFailed()</p>
 *
 * @param <SELF> the "self" type of this assertion class. Please read
 *          &quot;<a href="https://web.archive.org/web/20130721224442/http:/passion.forco.de/content/emulating-self-types-using-java-generics-simplify-fluent-api-implementation" target="_blank">
 *          Emulating 'self types' using Java Generics to simplify fluent API implementation</a>&quot;
 *          for more details.
 * @param <ACTUAL> the type of the "actual" value.
 */
abstract class AbstractPioneerAssert<SELF extends AbstractAssert<SELF, ACTUAL>, ACTUAL>
		extends AbstractAssert<SELF, ACTUAL> {

	protected final int expected;

	protected AbstractPioneerAssert(ACTUAL actual, Class<?> self, int expected) {
		super(actual, self);
		this.expected = expected;
	}

}
