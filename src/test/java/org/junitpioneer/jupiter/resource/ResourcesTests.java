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

		@DisplayName("then ::create is called")
		@Test
		void thenCreateIsCalled() {
			ExecutionResults executionResults = PioneerTestKit.executeTestClass(FakeResourceFactory1TestCases.class);
			assertThat(executionResults.testEvents().debug().succeeded().count()).isEqualTo(1);
			assertThat(FakeResourceFactory1.createCalls).isEqualTo(1);
		}

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
						.executeTestClass(NewRFCreateReturnsNullTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Method [public org.junitpioneer.jupiter.resource.Resource "
											+ "org.junitpioneer.jupiter.resource.ResourcesTests$RFCreateReturnsNullResourceFactory.create(java.util.List)] "
											+ "with arguments [some-arg] returned null"))));
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

			@DisplayName("and the resource returns null on ::get")
			@Nested
			class AndResourceReturnsNullOnGetTests {

				@DisplayName("then a proper exception is thrown")
				@Test
				void thenProperExceptionIsThrown() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(NewRGetReturnsNullTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class),
										message("Method [public java.lang.Object "
												+ "org.junitpioneer.jupiter.resource.ResourcesTests$RGetReturnsNullResource.get()] returned null"))));
				}

			}

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class FakeResourceFactory1TestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(FakeResourceFactory1.class) Object object) {

		}

	}

	static final class FakeResourceFactory1 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return new SomeResource();
		}

	}

	static final class SomeResource implements Resource<Object> {

		@Override
		public Object get() {
			return "some-object";
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
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

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
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
		"Failed to clone a homunculus");

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class NewRFCreateReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(value = RFCreateReturnsNullResourceFactory.class, arguments = { "some-arg" }) Object object) {

		}

	}

	static final class RFCreateReturnsNullResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return null;
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
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
		"Wait, what's that file doing there?");

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
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
		public Object get() {
			return "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_R_CLOSE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_R_CLOSE_EXCEPTION = new UnknownHostException(
		"Wait, where's the Internet gone?!");

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class NewRGetReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(RGetReturnsNullResourceFactory.class) Object object) {

		}

	}

	static final class RGetReturnsNullResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new RGetReturnsNullResource();
		}

	}

	static final class RGetReturnsNullResource implements Resource<Object> {

		@Override
		public Object get() {
			return null;
		}

	}

	// ---

	@DisplayName("when a test class has a test method with a parameter annotated with both @New and @Shared")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithBothNewAndSharedTests {

		@DisplayName("then an exception is thrown")
		@Test
		void thenExceptionIsThrown() throws Exception {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases.class);
			Method failingTest = TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases.class
					.getDeclaredMethod("theTest", String.class);
			assertThat(executionResults)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage("Parameter [%s] in method [%s] is annotated with both @New and @Shared",
						failingTest.getParameters()[0], failingTest);
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases {

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
					.executeTestClass(SingleTestMethodWithParamsWithSharedSameNameButDifferentTypesTestCases.class);
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

			executionResults = PioneerTestKit
					.executeTestClass(TwoTestMethodsWithParamsWithSharedSameNameButDifferentTypesTestCases.class);
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

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithParamsWithSharedSameNameButDifferentTypesTestCases {

		@Test
		void theTest(@Shared(factory = DummyResourceFactory.class, name = "some-name") String first,
				@Shared(factory = OtherResourceFactory.class, name = "some-name") String second) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class TwoTestMethodsWithParamsWithSharedSameNameButDifferentTypesTestCases {

		@Test
		void theTest1(@Shared(factory = DummyResourceFactory.class, name = "some-name") String foo) {

		}

		@Test
		void theTest2(@Shared(factory = OtherResourceFactory.class, name = "some-name") String bar) {

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

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
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

		@DisplayName("then ::create is called")
		@Test
		void thenCreateIsCalled() {
			ExecutionResults executionResults = PioneerTestKit.executeTestClass(FakeResourceFactory2TestCases.class);
			assertThat(executionResults.testEvents().debug().succeeded().count()).isEqualTo(1);
			assertThat(FakeResourceFactory2.createCalls).isEqualTo(1);
		}

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

		@DisplayName("and the factory returns null on ::create")
		@Nested
		class AndFactoryReturnsNullOnCreateTests {

			@DisplayName("then a proper exception is thrown")
			@Test
			void thenProperExceptionIsThrown() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(SharedRFCreateReturnsNullTestCases.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Method [public org.junitpioneer.jupiter.resource.Resource "
											+ "org.junitpioneer.jupiter.resource.ResourcesTests$RFCreateReturnsNullResourceFactory.create(java.util.List)] "
											+ "with arguments [] returned null"))));
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

			@DisplayName("and the resource returns null on ::get")
			@Nested
			class AndResourceReturnsNullOnGetTests {

				@DisplayName("then a proper exception is thrown")
				@Test
				void thenProperExceptionIsThrown() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(SharedRGetReturnsNullTestCases.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Method [public java.lang.Object "
												+ "org.junitpioneer.jupiter.resource.ResourcesTests$RGetReturnsNullResource.get()] "
												+ "returned null"))));
				}

			}

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class FakeResourceFactory2TestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = FakeResourceFactory2.class, name = "some-name") Object object) {

		}

	}

	static final class FakeResourceFactory2 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return new SomeResource();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class ThrowOnSharedRFCreateTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCreateResourceFactory.class, name = "some-name") Object object) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class ThrowOnSharedRFCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class ThrowOnSharedRGetTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRGetResourceFactory.class, name = "some-name") Object object) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class ThrowOnSharedRCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SharedRFCreateReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(name = "foo", factory = RFCreateReturnsNullResourceFactory.class) Object object) {

		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SharedRGetReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(name = "foo", factory = RGetReturnsNullResourceFactory.class) Object object) {

		}

	}

	// ---

	@DisplayName("when a shared resource factory is applied to two parameters")
	@Nested
	class WhenSharedResourceFactoryAppliedToTwoParametersTests {

		@DisplayName("then ::create is called only once")
		@Test
		void thenCreateIsCalledOnlyOnce() {
			ExecutionResults executionResults = PioneerTestKit.executeTestClass(FakeResourceFactory3TestCases.class);
			assertThat(executionResults.testEvents().debug().succeeded().count()).isEqualTo(2);
			assertThat(FakeResourceFactory3.createCalls).isEqualTo(1);
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class FakeResourceFactory3TestCases {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = FakeResourceFactory3.class, name = "some-name") Object object) {

		}

		@Test
		@SuppressWarnings("unused")
		void bar(@Shared(factory = FakeResourceFactory3.class, name = "some-name") Object object) {

		}

	}

	static final class FakeResourceFactory3 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return new SomeResource();
		}

	}

}