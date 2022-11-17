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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
class FileScopeCheckingTests {

	/*
	 * The documentation of the resource extension claims that file-scoped resources are cleaned up
	 * once all tests in that source file were executed. The tests in this class and in
	 * `ResourceCreatingTests` verify that in two parts:
	 *
	 *  - a file-scoped resource is not cleaned up before all tests in the same file were executed
	 *    (this is asserted in `ResourceCreatingTests`)
	 *  - a file-scoped resource is cleaned up before tests in another file are executed
	 *    (this is asserted in this class here)
	 *
	 * To make sure the test classes run in the right order, Jupiter's class order[1] is used with
	 * this class running after `ResourceCreatingTests`.
	 *
	 * [1]: https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-execution-order-classes
	 */

	@Test
	void verifyResourceAndFactoryWereClosed() {
		assertThat(CountingResourceFactory.CLOSED_RESOURCES).hasValue(1);
		assertThat(CountingResourceFactory.CLOSED_RESOURCE_FACTORIES).hasValue(1);
	}

	public static class CountingResourceFactory implements ResourceFactory<String> {

		public static final AtomicInteger CREATED_RESOURCE_FACTORIES = new AtomicInteger();
		public static final AtomicInteger CLOSED_RESOURCE_FACTORIES = new AtomicInteger();

		public static final AtomicInteger CREATED_RESOURCES = new AtomicInteger();
		public static final AtomicInteger CLOSED_RESOURCES = new AtomicInteger();

		public CountingResourceFactory() {
			CREATED_RESOURCE_FACTORIES.getAndIncrement();
		}

		@Override
		public Resource<String> create(List<String> arguments) {
			return new CountingResource();
		}

		@Override
		public void close() {
			CLOSED_RESOURCE_FACTORIES.getAndIncrement();
		}

		public static class CountingResource implements Resource<String> {

			@Override
			public String get() {
				CREATED_RESOURCES.getAndIncrement();
				return "a resource";
			}

			@Override
			public void close() {
				CLOSED_RESOURCES.getAndIncrement();
			}

		}

	}

}
