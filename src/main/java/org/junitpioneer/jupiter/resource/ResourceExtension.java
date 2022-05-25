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
			String message = String
					.format( //
						"Parameter [%s] in %s is annotated with both @New and @Shared", //
						parameterContext.getParameter(), testMethodDescription(extensionContext));
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

		String errorMessage = String
				.format( //
					"Parameter [%s] in %s is not annotated with @New or @Shared", //
					parameterContext.getParameter(), testMethodDescription(extensionContext));
		throw new ParameterResolutionException(errorMessage);
	}

	private <T> T checkType(Object resource, Class<T> type) {
		if (!type.isInstance(resource)) {
			String message = String.format("Parameter [%s] is not of the correct target type %s.", resource, type);
			throw new ParameterResolutionException(message);
		}
		return type.cast(resource);
	}

	private Object resolveNew(New newAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newAnnotation.value());
		store.put(uniqueKey(), resourceFactory);
		Resource<?> resource;
		try {
			resource = resourceFactory.create(unmodifiableList(asList(newAnnotation.arguments())));
			store.put(uniqueKey(), resource);
		}
		catch (Exception ex) {
			throw new ParameterResolutionException(
				"Unable to create a resource from `" + resourceFactory.getClass() + '`', ex);
		}
		try {
			return resource.get();
		}
		catch (Exception ex) {
			throw new ParameterResolutionException(
				"Unable to get the contents of the resource created by `" + resourceFactory.getClass() + '`', ex);
		}
	}

	private Object resolveShared(Shared sharedAnnotation, Parameter[] parameters, ExtensionContext.Store store) {
		throwIfHasAnnotationWithSameNameButDifferentType(store, sharedAnnotation);
		throwIfMultipleParametersHaveExactAnnotation(parameters, sharedAnnotation);

		ResourceFactory<?> resourceFactory = store
				.getOrComputeIfAbsent( //
					factoryKey(sharedAnnotation), //
					__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
					ResourceFactory.class);
		ResourceWithLock<?> resource;
		try {
			resource = store
					.getOrComputeIfAbsent( //
						resourceKey(sharedAnnotation), //
						__ -> createResource(sharedAnnotation, resourceFactory), //
						ResourceWithLock.class);
		}
		catch (UncheckedParameterResolutionException e) {
			throw e.getCause();
		}
		try {
			return resource.get();
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to get the contents of the resource created by `" + sharedAnnotation.factory() + '`', e);
		}
	}

	private ResourceWithLock<?> createResource(Shared sharedAnnotation, ResourceFactory<?> resourceFactory) {
		try {
			return new ResourceWithLock<>(
				resourceFactory.create(unmodifiableList(asList(sharedAnnotation.arguments()))));
		}
		catch (Exception e) {
			throw new UncheckedParameterResolutionException(new ParameterResolutionException(
				"Unable to create a resource from `" + sharedAnnotation.factory() + "`", e));
		}
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
				throw new ParameterResolutionException(
					"Two or more parameters are annotated with @Shared annotations with the name \""
							+ sharedAnnotation.name() + "\" but with different factory classes");
			}
		}
	}

	private void throwIfMultipleParametersHaveExactAnnotation(Parameter[] parameters, Shared sharedAnnotation) {
		long parameterCount = //
			Arrays.stream(parameters).filter(parameter -> hasAnnotation(parameter, sharedAnnotation)).count();
		if (parameterCount > 1) {
			throw new ParameterResolutionException(
				"A test method has " + parameterCount + " parameters annotated with @Shared with the same "
						+ "factory type and name; this is redundant, so it is not allowed");
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

	private static final class UncheckedParameterResolutionException extends RuntimeException {

		private static final long serialVersionUID = -8656995841157868666L;

		public UncheckedParameterResolutionException(ParameterResolutionException cause) {
			super(cause);
		}

		@Override
		public synchronized ParameterResolutionException getCause() {
			return (ParameterResolutionException) super.getCause();
		}

	}

	private static String testMethodDescription(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod().map(method -> "method [" + method + ']').orElse("an unknown method");
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

		private final Resource<T> resource;
		private final ReentrantLock lock;

		public ResourceWithLock(Resource<T> resource) {
			this.resource = requireNonNull(resource);
			this.lock = new ReentrantLock();
		}

		public ReentrantLock lock() {
			return lock;
		}

		@Override
		public T get() throws Exception {
			return resource.get();
		}

		@Override
		public void close() throws Exception {
			resource.close();
		}

	}

}
