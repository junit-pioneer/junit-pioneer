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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;

class ResourceExtension implements ParameterResolver, InvocationInterceptor {

	private static final ExtensionContext.Namespace NAMESPACE = //
		ExtensionContext.Namespace.create(ResourceExtension.class);

	private static final AtomicLong KEY_GENERATOR = new AtomicLong(0);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (parameterContext.isAnnotated(New.class) && parameterContext.isAnnotated(Shared.class)) {
			// @formatter:off
			String message =
					String.format(
							"Parameter [%s] in %s is annotated with both @New and @Shared",
							parameterContext.getParameter(), testMethodDescription(extensionContext));
			// @formatter:on
			throw new ParameterResolutionException(message);
		}
		return parameterContext.isAnnotated(New.class) || parameterContext.isAnnotated(Shared.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<New> newAnnotation = parameterContext.findAnnotation(New.class);
		if (newAnnotation.isPresent()) {
			ExtensionContext.Store testStore = extensionContext.getStore(NAMESPACE);
			Object resource = resolveNew(newAnnotation.get(), testStore);
			return checkType(resource, parameterContext.getParameter().getType());
		}

		Optional<Shared> sharedAnnotation = parameterContext.findAnnotation(Shared.class);
		if (sharedAnnotation.isPresent()) {
			Parameter[] parameters = parameterContext.getDeclaringExecutable().getParameters();
			ExtensionContext scopedContext = scopedContext(extensionContext, sharedAnnotation.get().scope());
			ExtensionContext.Store scopedStore = scopedContext.getStore(NAMESPACE);
			Object resource = resolveShared(sharedAnnotation.get(), parameters, scopedStore);
			return checkType(resource, parameterContext.getParameter().getType());
		}

		// @formatter:off
		String message =
				String.format(
						"Parameter [%s] in %s is not annotated with @New or @Shared",
						parameterContext.getParameter(), testMethodDescription(extensionContext));
		// @formatter:on
		throw new ParameterResolutionException(message);
	}

	private <T> T checkType(Object resource, Class<T> type) {
		if (!type.isInstance(resource)) {
			String message = String.format("Parameter [%s] is not of the correct target type %s", resource, type);
			throw new ParameterResolutionException(message);
		}
		return type.cast(resource);
	}

	private Object resolveNew(New newAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newAnnotation.value());
		store.put(uniqueKey(), resourceFactory);

		Resource<?> resource = newResource(newAnnotation, resourceFactory);
		store.put(uniqueKey(), resource);

		Object result;
		try {
			result = resource.get();
		}
		catch (Exception ex) {
			// @formatter:off
			String message =
					String.format(
							"Unable to get the contents of the resource created by `%s`",
							resourceFactory.getClass().getTypeName());
			// @formatter:on
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message =
					String.format(
							"Method [%s] returned null",
							ReflectionSupport.findMethod(resource.getClass(), "get")
									.orElseThrow(this::unreachable));
			// @formatter:on
			throw new ParameterResolutionException(message);
		}

		return result;
	}

	private Object resolveShared(Shared sharedAnnotation, Parameter[] parameters, ExtensionContext.Store store) {
		throwIfHasAnnotationWithSameNameButDifferentType(store, sharedAnnotation);
		throwIfMultipleParametersHaveExactAnnotation(parameters, sharedAnnotation);

		ResourceFactory<?> resourceFactory = store
				.getOrComputeIfAbsent( //
					factoryKey(sharedAnnotation), //
					__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
					ResourceFactory.class);
		ResourceWithLock<?> resourceWithLock = store
				.getOrComputeIfAbsent( //
					resourceKey(sharedAnnotation), //
					__ -> new ResourceWithLock<>(newResource(sharedAnnotation, resourceFactory)), //
					ResourceWithLock.class);

		Object result;
		try {
			result = resourceWithLock.get();
		}
		catch (Exception ex) {
			// @formatter:off
			String message =
					String.format(
							"Unable to get the contents of the resource created by `%s`",
							sharedAnnotation.factory());
			// @formatter:on
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message =
					String.format(
							"Method [%s] returned null",
							ReflectionSupport.findMethod(resourceWithLock.delegate().getClass(), "get")
									.orElseThrow(this::unreachable));
			// @formatter:on
			throw new ParameterResolutionException(message);
		}

		return result;
	}

	private Resource<?> newResource(Object newOrSharedAnnotation, ResourceFactory<?> resourceFactory) {
		List<String> arguments;
		if (newOrSharedAnnotation instanceof New) {
			arguments = unmodifiableList(asList(((New) newOrSharedAnnotation).arguments()));
		} else {
			arguments = Collections.emptyList();
		}

		Resource<?> result;
		try {
			result = resourceFactory.create(arguments);
		}
		catch (Exception ex) {
			String message = //
				String.format("Unable to create a resource from `%s`", resourceFactory.getClass().getTypeName());
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message =
					String.format(
							"Method [%s] with arguments %s returned null",
							ReflectionSupport.findMethod(resourceFactory.getClass(), "create", List.class)
									.orElseThrow(this::unreachable),
							arguments);
			// @formatter:on
			throw new ParameterResolutionException(message);
		}

		return result;
	}

	private void throwIfHasAnnotationWithSameNameButDifferentType(ExtensionContext.Store store,
			Shared sharedAnnotation) {
		ResourceFactory<?> presentResourceFactory = //
			store.getOrDefault(factoryKey(sharedAnnotation), ResourceFactory.class, null);

		if (presentResourceFactory == null) {
			store.put(keyOfFactoryKey(sharedAnnotation), factoryKey(sharedAnnotation));
		} else {
			String presentResourceFactoryName = //
				store.getOrDefault(keyOfFactoryKey(sharedAnnotation), String.class, null);

			if (factoryKey(sharedAnnotation).equals(presentResourceFactoryName)
					&& !sharedAnnotation.factory().equals(presentResourceFactory.getClass())) {
				// @formatter:off
				String message =
						String.format(
								"Two or more parameters are annotated with @Shared annotations with the name \"%s\" "
										+ "but with different factory classes",
								sharedAnnotation.name());
				// @formatter:on
				throw new ParameterResolutionException(message);
			}
		}
	}

	private void throwIfMultipleParametersHaveExactAnnotation(Parameter[] parameters, Shared sharedAnnotation) {
		long parameterCount = //
			Arrays.stream(parameters).filter(parameter -> hasAnnotation(parameter, sharedAnnotation)).count();
		if (parameterCount > 1) {
			// @formatter:off
			String message =
					String.format(
							"A test method has %d parameters annotated with @Shared with the same factory type "
									+ "and name; this is redundant, so it is not allowed",
							parameterCount);
			// @formatter:on
			throw new ParameterResolutionException(message);
		}
	}

	private boolean hasAnnotation(Parameter parameter, Shared sharedAnnotation) {
		return AnnotationSupport
				.findAnnotation(parameter, Shared.class)
				.filter(shared -> shared.factory().equals(sharedAnnotation.factory()))
				.filter(shared -> shared.name().equals(sharedAnnotation.name()))
				.isPresent();
	}

	private long uniqueKey() {
		return KEY_GENERATOR.getAndIncrement();
	}

	private String factoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory";
	}

	private String resourceKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource";
	}

	private String keyOfFactoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory key";
	}

	private static String testMethodDescription(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod().map(method -> "method [" + method + ']').orElse("an unknown method");
	}

	private AssertionError unreachable() {
		return new AssertionError("Unreachable");
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		runDynamicTestSequentially(invocation, extensionContext);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext, extensionContext);
	}

	private <T> T runSequentially(Invocation<T> invocation,
			ReflectiveInvocationContext<? extends Executable> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		List<ReentrantLock> locks = //
			sortedLocks(findShared(invocationContext.getExecutable()), extensionContext);
		return invokeWithLocks(invocation, locks);
	}

	private void runDynamicTestSequentially(Invocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		List<ReentrantLock> locks = //
			sortedLocks(findShared(testFactoryMethod(extensionContext)), extensionContext);
		invokeWithLocks(invocation, locks);
	}

	private List<ReentrantLock> sortedLocks(Stream<Shared> sharedAnnotations, ExtensionContext extensionContext) {
		// Sort by @Shared's name to implicitly sort the locks returned by this method, to prevent deadlocks when
		// locking later.
		// This is a well-known solution to the "dining philosophers problem".
		// See ResourcesParallelismTests.ThrowIfTestsRunInParallelTestCases for more info.
		List<Shared> sortedAnnotations = sharedAnnotations.sorted(comparing(Shared::name)).collect(toList());
		List<ExtensionContext.Store> stores = //
			sortedAnnotations
					.stream() //
					.map(shared -> scopedContext(extensionContext, shared.scope()))
					.map(scopedContext -> extensionContext.getStore(NAMESPACE))
					.collect(toList());
		return IntStream
				.range(0, sortedAnnotations.size()) //
				.mapToObj(i -> findLockForShared(sortedAnnotations.get(i), stores.get(i)))
				.collect(toList());
	}

	private Method testFactoryMethod(ExtensionContext extensionContext) {
		return extensionContext
				.getParent()
				.orElseThrow(() -> new IllegalStateException(
					"The parent extension context of a DynamicTest was not a @TestFactory-annotated test method"))
				.getRequiredTestMethod();
	}

	private ExtensionContext scopedContext(ExtensionContext extensionContext, Scope scope) {
		if (scope == Scope.SOURCE_FILE) {
			ExtensionContext currentContext = extensionContext;
			Optional<ExtensionContext> parentContext = extensionContext.getParent();

			while (parentContext.isPresent() && parentContext.get() != currentContext.getRoot()) {
				currentContext = parentContext.get();
				parentContext = currentContext.getParent();
			}

			return currentContext;
		}

		return extensionContext.getRoot();
	}

	private Stream<Shared> findShared(Executable executable) {
		return Arrays
				.stream(executable.getParameters())
				.map(parameter -> AnnotationSupport.findAnnotation(parameter, Shared.class))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private ReentrantLock findLockForShared(Shared shared, ExtensionContext.Store store) {
		return Optional
				.ofNullable(store.get(resourceKey(shared), ResourceWithLock.class))
				.orElseThrow(
					() -> new IllegalStateException("There should be a shared resource for the name " + shared.name()))
				.lock();
	}

	private static <T> T invokeWithLocks(Invocation<T> invocation, List<ReentrantLock> locks) throws Throwable {
		locks.forEach(ReentrantLock::lock);
		try {
			return invocation.proceed();
		}
		finally {
			// for dining philosophers, "[t]he order in which each philosopher puts down the forks does not matter"
			// (quote from Wikipedia)
			locks.forEach(ReentrantLock::unlock);
		}
	}

	private static class ResourceWithLock<T> implements Resource<T> {

		private final Resource<T> delegate;
		private final ReentrantLock lock;

		public ResourceWithLock(Resource<T> delegate) {
			this.delegate = requireNonNull(delegate);
			this.lock = new ReentrantLock();
		}

		public Resource<T> delegate() {
			return delegate;
		}

		public ReentrantLock lock() {
			return lock;
		}

		@Override
		public T get() throws Exception {
			return delegate.get();
		}

		@Override
		public void close() throws Exception {
			delegate.close();
		}

	}

}
