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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Resources extension")
class ResourcesTests {

	@DisplayName("when a new resource factory is applied to a parameter")
	@Nested
	class WhenNewResourceFactoryAppliedToParameterTests {

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRFCreateTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Unable to create a resource from `"
											+ ThrowOnRFCreateResourceFactory.class.getTypeName() + "`"), //
									cause(//
										instanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage())))));
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRFCloseTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass()), //
									message(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and the factory returns null on ::create")
		@Nested
		class AndFactoryReturnsNullOnCreateTests {

			@DisplayName("then a proper exception is thrown")
			@Test
			void thenProperExceptionIsThrown() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnRFCreateReturningNullTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_FACTORY_RETURNS_NULL_ON_CREATE_EXCEPTION.getClass()), //
									message(EXPECTED_FACTORY_RETURNS_NULL_ON_CREATE_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and a resource is created")
		@Nested
		class AndResourceIsCreatedTests {

			@DisplayName("and the resource throws on ::get")
			@Nested
			class AndResourceThrowsOnGetTests {

				@DisplayName("then the thrown exception is wrapped and propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRGetTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Unable to get the contents of the resource created by `"
												+ ThrowOnRGetResourceFactory.class.getTypeName() + "`"), //
										cause(//
											instanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass()), //
											message(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage())))));
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnNewRCloseTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage()))));
				}

			}

		}

	}

	static class ThrowOnNewRFCreateTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRFCreateResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRFCreateResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) throws Exception {
			throw EXPECTED_THROW_ON_RF_CREATE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_RF_CREATE_EXCEPTION = new IOException(
		"Failed to connect to the Matrix");

	static class ThrowOnNewRFCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRFCloseResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRFCloseResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return () -> "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION = new CloneNotSupportedException(
		"failed to clone a homunculus");

	@SuppressWarnings("unused")
	static class ThrowOnRFCreateReturningNullTestCases {

		@Test
		void foo(@New(ThrowOnRFCreateReturningNullResourceFactory.class) Object object) {

		}

		// TODO: Move to "Shared" block further down this file.
		//		@Test
		//		void bar(@Shared(name = "bar", factory = ThrowOnRFCreateReturningNullResourceFactory.class) Object object) {
		//
		//		}

	}

	static final class ThrowOnRFCreateReturningNullResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return null;
		}

	}

	private static final Exception EXPECTED_FACTORY_RETURNS_NULL_ON_CREATE_EXCEPTION = new ParameterResolutionException(
		"`org.junitpioneer.jupiter.resource.ResourcesTests$ThrowOnRFCreateReturningNullResourceFactory#create` "
				+ "returned null");

	static class ThrowOnNewRGetTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRGetResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRGetResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new ThrowOnRGetResource();
		}

	}

	static final class ThrowOnRGetResource implements Resource<Object> {

		@Override
		public Object get() throws Exception {
			throw EXPECTED_THROW_ON_R_GET_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_R_GET_EXCEPTION = new FileAlreadyExistsException(
		"wait, what's that file doing there?");

	static class ThrowOnNewRCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRCloseResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRCloseResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new ThrowOnRCloseResource();
		}

	}

	static final class ThrowOnRCloseResource implements Resource<Object> {

		@Override
		public Object get() throws Exception {
			return "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_R_CLOSE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_R_CLOSE_EXCEPTION = new UnknownHostException(
		"wait, where's the Internet gone?!");

	// ---

	@DisplayName("when a test class has a test method with a parameter annotated with both @New and @Shared")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithBothNewAndSharedTests {

		@DisplayName("then an exception is thrown")
		@Test
		void thenExceptionIsThrown() throws Exception {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestMethodWithParameterAnnotatedWithBothNewAndShared.class);
			Method failingTest = TestMethodWithParameterAnnotatedWithBothNewAndShared.class
					.getDeclaredMethod("theTest", String.class);
			assertThat(executionResults)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage("Parameter [%s] in method [%s] is annotated with both @New and @Shared",
						failingTest.getParameters()[0], failingTest);
		}

	}

	static class TestMethodWithParameterAnnotatedWithBothNewAndShared {

		@Test
		void theTest(
				@New(DummyResourceFactory.class) @Shared(factory = DummyResourceFactory.class, name = "some-name") String param) {
			fail("We should not get this far.");
		}

	}

	static final class DummyResourceFactory implements ResourceFactory<String> {

		@Override
		public Resource<String> create(List<String> arguments) {
			return () -> "dummy";
		}

	}

	// ---

	@DisplayName("when a parameter is annotated with @Shared, and another parameter is annotated with @Shared with "
			+ "the same name but a different factory type")
	@Nested
	class WhenParameterIsAnnotatedWithSharedAndAnotherParamIsAnnotatedWithSharedWithSameNameButDifferentFactoryTypeTests {

		@DisplayName("then it throws an exception")
		@Test
		void thenItThrowsAnException() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithConflictingSharedTempDirParametersTestCases.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message(String
										.format(
											"Two or more parameters are annotated with @Shared annotations with the "
													+ "name \"%s\" but with different factory classes",
											"some-name")))));
		}

	}

	static class SingleTestMethodWithConflictingSharedTempDirParametersTestCases {

		@Test
		void theTest(@Shared(factory = DummyResourceFactory.class, name = "some-name") String first,
				@Shared(factory = OtherResourceFactory.class, name = "some-name") String second) {

		}

	}

	static final class OtherResourceFactory implements ResourceFactory<String> {

		@Override
		public Resource<String> create(List<String> arguments) {
			return () -> null;
		}

	}

	// ---

	@DisplayName("when a test method has two parameters annotated with "
			+ "@Shared(factory = DummyResourceFactory.class, name = \"some-name\")")
	@Nested
	class WhenTestMethodHasTwoParamsAnnotatedWithSharedAnnotationWithSameFactoryAndNameTests {

		@DisplayName("then it throws an exception")
		@Test
		void thenItThrowsAnException() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestMethodWithTwoParamsWithSameSharedAnnotationTestCases.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message("A test method has 2 parameters annotated with @Shared with the same "
										+ "factory type and name; this is redundant, so it is not allowed"))));
		}

	}

	static class TestMethodWithTwoParamsWithSameSharedAnnotationTestCases {

		@Test
		void theTest(@Shared(factory = DummyResourceFactory.class, name = "some-name") String first,
				@Shared(factory = DummyResourceFactory.class, name = "some-name") String second) {

		}

	}

	// ---

	@DisplayName("when a shared resource factory is applied to a parameter")
	@Nested
	class WhenSharedResourceFactoryAppliedToParameterTests {

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnSharedRFCreateTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Unable to create a resource from `"
											+ ThrowOnRFCreateResourceFactory.class.getTypeName() + "`"), //
									cause(//
										instanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage())))));
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnSharedRFCloseTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass()), //
									message(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and a resource is created")
		@Nested
		class AndResourceIsCreatedTests {

			@DisplayName("and the resource throws on ::get")
			@Nested
			class AndResourceThrowsOnGetTests {

				@DisplayName("then the thrown exception is wrapped and propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnSharedRGetTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Unable to get the contents of the resource created by `"
												+ ThrowOnRGetResourceFactory.class + "`"), //
										cause(//
											instanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass()), //
											message(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage())))));
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnSharedRCloseTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage()))));
				}

			}

		}

	}

	static class ThrowOnSharedRFCreateTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCreateResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRFCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRGetTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRGetResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

}
