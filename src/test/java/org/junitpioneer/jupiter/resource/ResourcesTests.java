/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClasses;
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

@DisplayName("Resources extension")
class ResourcesTests {

	@DisplayName("when a new resource factory is applied to a parameter")
	@Nested
	class WhenNewResourceFactoryAppliedToParameterTests {

		@DisplayName("then ::create is called")
		@Test
		void thenCreateIsCalled() {
			ExecutionResults results = executeTestClass(CountingResourceFactory1TestCases.class);

			assertThat(results).hasSingleSucceededTest();
			assertThat(CountingResourceFactory1.createCalls).isEqualTo(1);
		}

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults results = executeTestClass(ThrowOnNewRFCreateTestCases.class);

				assertThat(results)
						.hasSingleFailedTest()
						.withExceptionInstanceOf(ParameterResolutionException.class)
						.hasMessage("Unable to create a resource from `"
								+ ThrowOnRFCreateResourceFactory.class.getTypeName() + "`")
						.cause()
						.isInstanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass())
						.hasMessage(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage());
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults results = executeTestClass(ThrowOnNewRFCloseTestCases.class);

				assertThat(results)
						.hasSingleFailedTest()
						.withExceptionInstanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass())
						.hasMessage(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage());
			}

		}

		@DisplayName("and the factory returns null on ::create")
		@Nested
		class AndFactoryReturnsNullOnCreateTests {

			@DisplayName("then a proper exception is thrown")
			@Test
			void thenProperExceptionIsThrown() {
				ExecutionResults results = executeTestClass(NewRFCreateReturnsNullTestCases.class);

				assertThat(results)
						.hasSingleFailedTest()
						.withExceptionInstanceOf(ParameterResolutionException.class)
						.hasMessageMatching(".*`Resource` instance.*was null.*");
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
					ExecutionResults results = executeTestClass(ThrowOnNewRGetTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(ParameterResolutionException.class)
							.hasMessage("Unable to get the contents of the resource created by `"
									+ ThrowOnRGetResourceFactory.class.getTypeName() + "`")
							.cause()
							.isInstanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass())
							.hasMessage(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage());
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults results = executeTestClass(ThrowOnNewRCloseTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass())
							.hasMessage(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage());
				}

			}

			@DisplayName("and the resource returns null on ::get")
			@Nested
			class AndResourceReturnsNullOnGetTests {

				@DisplayName("then a proper exception is thrown")
				@Test
				void thenProperExceptionIsThrown() {
					ExecutionResults results = executeTestClass(NewRGetReturnsNullTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(ParameterResolutionException.class)
							.hasMessageMatching(".*resource.*was null.*");
				}

			}

			@DisplayName("and the resource returned can't be cast to the parameter type")
			@Nested
			class AndResourceReturnedIsNotOfCorrectTypeTests {

				@DisplayName("then a proper exception is thrown")
				@Test
				void thenProperExceptionIsThrown() {
					ExecutionResults results = executeTestClass(TestMethodWithMismatchedParamsTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(ParameterResolutionException.class)
							.hasMessageMatching("Parameter.*is not of the correct target type.*");
				}

			}

		}

	}

	static class CountingResourceFactory1TestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(CountingResourceFactory1.class) Object object) {

		}

	}

	static final class CountingResourceFactory1 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return () -> "some resource";
		}

	}

	static class ThrowOnNewRFCreateTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(ThrowOnRFCreateResourceFactory.class) Object object) {

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
		void test(@New(ThrowOnRFCloseResourceFactory.class) Object object) {

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

	static class NewRFCreateReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(value = RFCreateReturnsNullResourceFactory.class, arguments = { "some-arg" }) Object object) {

		}

	}

	static final class RFCreateReturnsNullResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return null;
		}

	}

	static class ThrowOnNewRGetTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(ThrowOnRGetResourceFactory.class) Object object) {

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

	static class ThrowOnNewRCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(ThrowOnRCloseResourceFactory.class) Object object) {

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

	static class NewRGetReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@New(RGetReturnsNullResourceFactory.class) Object object) {

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
			ExecutionResults results = executeTestClass(
				TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases.class);
			Method failingTest = TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases.class
					.getDeclaredMethod("test", String.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage("Parameter [%s] in method [%s] is annotated with both @New and @Shared",
						failingTest.getParameters()[0], failingTest);
		}

	}

	static class TestMethodWithParameterAnnotatedWithBothNewAndSharedTestCases {

		@Test
		void test(
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
			ExecutionResults results = executeTestClass(
				SingleTestMethodWithParamsWithSharedSameNameButDifferentTypesTestCases.class);
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage(String
							.format("Two or more parameters are annotated with @Shared annotations with the "
									+ "name \"%s\" but with different factory classes",
								"some-name"));

			results = executeTestClass(TwoTestMethodsWithParamsWithSharedSameNameButDifferentTypesTestCases.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage(String
							.format("Two or more parameters are annotated with @Shared annotations with the "
									+ "name \"%s\" but with different factory classes",
								"some-name"));
		}

	}

	static class SingleTestMethodWithParamsWithSharedSameNameButDifferentTypesTestCases {

		@Test
		void test(@Shared(factory = DummyResourceFactory.class, name = "some-name") String first,
				@Shared(factory = OtherResourceFactory.class, name = "some-name") String second) {

		}

	}

	static class TwoTestMethodsWithParamsWithSharedSameNameButDifferentTypesTestCases {

		@Test
		void test_1(@Shared(factory = DummyResourceFactory.class, name = "some-name") String foo) {

		}

		@Test
		void test_2(@Shared(factory = OtherResourceFactory.class, name = "some-name") String bar) {

		}

	}

	static final class OtherResourceFactory implements ResourceFactory<String> {

		@Override
		public Resource<String> create(List<String> arguments) {
			return () -> "other";
		}

	}

	// ---

	@DisplayName("when a parameter is annotated with @Shared, and another parameter is annotated with @Shared with "
			+ "the same name but a different scope")
	@Nested
	class WhenParameterIsAnnotatedWithSharedAndAnotherParamIsAnnotatedWithSharedWithSameNameButDifferentScopeTests {

		@DisplayName("then it throws an exception")
		@Test
		void thenItThrowsAnException() {
			ExecutionResults results = executeTestClass(
				TwoTestMethodsWithParamsWithSharedSameNameButDifferentScopesTestCases.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage(String
							.format("Two or more parameters are annotated with @Shared annotations with "
									+ "the name \"%s\" but with different scopes",
								"some-name-1"));

			results = executeTestClasses(List
					.of(TestMethodWithParamsWithSharedSameNameButDifferentScopesTestCases1.class,
						TestMethodWithParamsWithSharedSameNameButDifferentScopesTestCases2.class));

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage(String
							.format("Two or more parameters are annotated with @Shared annotations with "
									+ "the name \"%s\" but with different scopes",
								"some-name-2"));
		}

	}

	static class TwoTestMethodsWithParamsWithSharedSameNameButDifferentScopesTestCases {

		@Test
		void test_1(
				@Shared(factory = DummyResourceFactory.class, name = "some-name-1", scope = Shared.Scope.GLOBAL) String first) {

		}

		@Test
		void test_2(
				@Shared(factory = DummyResourceFactory.class, name = "some-name-1", scope = Shared.Scope.SOURCE_FILE) String second) {

		}

	}

	static class TestMethodWithParamsWithSharedSameNameButDifferentScopesTestCases1 {

		@Test
		void test(
				@Shared(factory = DummyResourceFactory.class, name = "some-name-2", scope = Shared.Scope.GLOBAL) String first) {

		}

	}

	static class TestMethodWithParamsWithSharedSameNameButDifferentScopesTestCases2 {

		@Test
		void test(
				@Shared(factory = DummyResourceFactory.class, name = "some-name-2", scope = Shared.Scope.SOURCE_FILE) String second) {

		}

	}

	static class TestMethodWithMismatchedParamsTestCases {

		@Test
		void test(@New(DummyResourceFactory.class) Number number) {
			fail("We should not get this far.");
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
			ExecutionResults results = executeTestClass(TestMethodWithTwoParamsWithSameSharedAnnotationTestCases.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessage("A test method has 2 parameters annotated with @Shared with the same "
							+ "factory type and name; this is redundant, so it is not allowed");
		}

	}

	static class TestMethodWithTwoParamsWithSameSharedAnnotationTestCases {

		@Test
		void test(@Shared(factory = DummyResourceFactory.class, name = "some-name") String first,
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
			ExecutionResults results = executeTestClass(CountingResourceFactory2TestCases.class);
			assertThat(results).hasSingleSucceededTest();
		}

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults results = executeTestClass(ThrowOnSharedRFCreateTestCases.class);

				assertThat(results)
						.hasSingleFailedTest()
						.withExceptionInstanceOf(ParameterResolutionException.class)
						.hasMessage("Unable to create a resource from `"
								+ ThrowOnRFCreateResourceFactory.class.getTypeName() + "`")
						.cause()
						.isInstanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass())
						.hasMessage(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage());
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults results = executeTestClass(ThrowOnSharedRFCloseTestCases.class);

				assertThat(results)
						.hasSingleFailedContainer()
						.withExceptionInstanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass())
						.hasMessage(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage());
			}

		}

		@DisplayName("and the factory returns null on ::create")
		@Nested
		class AndFactoryReturnsNullOnCreateTests {

			@DisplayName("then a proper exception is thrown")
			@Test
			void thenProperExceptionIsThrown() {
				ExecutionResults results = executeTestClass(SharedRFCreateReturnsNullTestCases.class);

				assertThat(results)
						.hasSingleFailedTest()
						.withExceptionInstanceOf(ParameterResolutionException.class)
						.hasMessageMatching(".*`Resource` instance.*was null.*");
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
					ExecutionResults results = executeTestClass(ThrowOnSharedRGetTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(ParameterResolutionException.class)
							.hasMessage("Unable to get the contents of the resource created by `"
									+ ThrowOnRGetResourceFactory.class + "`")
							.cause()
							.isInstanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass())
							.hasMessage(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage());
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults results = executeTestClass(ThrowOnSharedRCloseTestCases.class);

					assertThat(results)
							.hasSingleFailedContainer()
							.withExceptionInstanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass())
							.hasMessage(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage());
				}

			}

			@DisplayName("and the resource returns null on ::get")
			@Nested
			class AndResourceReturnsNullOnGetTests {

				@DisplayName("then a proper exception is thrown")
				@Test
				void thenProperExceptionIsThrown() {
					ExecutionResults results = executeTestClass(SharedRGetReturnsNullTestCases.class);

					assertThat(results)
							.hasSingleFailedTest()
							.withExceptionInstanceOf(ParameterResolutionException.class)
							.hasMessageMatching(".*resource.*was null.*");
				}

			}

		}

	}

	static class CountingResourceFactory2TestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(factory = CountingResourceFactory2.class, name = "some-name") Object object) {

		}

	}

	static final class CountingResourceFactory2 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return () -> "some resource";
		}

	}

	static class ThrowOnSharedRFCreateTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(factory = ThrowOnRFCreateResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRFCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(factory = ThrowOnRFCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRGetTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(factory = ThrowOnRGetResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRCloseTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(factory = ThrowOnRCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class SharedRFCreateReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(name = "foo", factory = RFCreateReturnsNullResourceFactory.class) Object object) {

		}

	}

	static class SharedRGetReturnsNullTestCases {

		@Test
		@SuppressWarnings("unused")
		void test(@Shared(name = "foo", factory = RGetReturnsNullResourceFactory.class) Object object) {

		}

	}

	// ---

	@DisplayName("when a shared resource factory is applied to two parameters")
	@Nested
	class WhenSharedResourceFactoryAppliedToTwoParametersTests {

		@DisplayName("then ::create is called only once")
		@Test
		void thenCreateIsCalledOnlyOnce() {
			ExecutionResults results = executeTestClass(CountingResourceFactory3TestCases.class);

			assertThat(results).hasNumberOfSucceededTests(2);
			assertThat(CountingResourceFactory3.createCalls).isEqualTo(1);
		}

	}

	static class CountingResourceFactory3TestCases {

		@Test
		@SuppressWarnings("unused")
		void test_1(@Shared(factory = CountingResourceFactory3.class, name = "some-name") Object object) {

		}

		@Test
		@SuppressWarnings("unused")
		void test_2(@Shared(factory = CountingResourceFactory3.class, name = "some-name") Object object) {

		}

	}

	static final class CountingResourceFactory3 implements ResourceFactory<Object> {

		static int createCalls = 0;

		@Override
		public Resource<Object> create(List<String> arguments) {
			createCalls++;
			return () -> "some resource";
		}

	}

}
