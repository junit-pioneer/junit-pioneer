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
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

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

	private static final Lock SHARED_ANNOTATION_RESOLUTION_LOCK = new ReentrantLock();

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
			ExtensionContext.Store scopedStore = scopedStore(extensionContext, sharedAnnotation.get().scope());
			ExtensionContext.Store rootStore = extensionContext.getRoot().getStore(NAMESPACE);
			Object resource = resolveShared(sharedAnnotation.get(), parameters, scopedStore, rootStore);
			return checkType(resource, parameterContext.getParameter().getType());
		}

		// @formatter:off
		String message = String.format(
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
			String message = String.format(
					"Unable to get the contents of the resource created by `%s`",
					resourceFactory.getClass().getTypeName());
			// @formatter:on
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message = String.format(
					"The resource returned by [%s] was null, which is not allowed",
					getMethod(resource.getClass(), "get"));
			// @formatter:on
			throw new ParameterResolutionException(message);
		}

		return result;
	}

	private Object resolveShared(Shared sharedAnnotation, Parameter[] parameters, ExtensionContext.Store scopedStore,
			ExtensionContext.Store rootStore) {
		// run sequentially, so that resources with the same name are never created twice at the same time
		SHARED_ANNOTATION_RESOLUTION_LOCK.lock();
		try {
			throwIfHasAnnotationWithSameNameButDifferentType(scopedStore, sharedAnnotation);
			throwIfHasAnnotationWithSameNameButDifferentScope(rootStore, sharedAnnotation);
			throwIfMultipleParametersHaveExactAnnotation(parameters, sharedAnnotation);

			ResourceFactory<?> resourceFactory = scopedStore
					.getOrComputeIfAbsent( //
						factoryKey(sharedAnnotation), //
						__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
						ResourceFactory.class);
			Resource<?> resource = scopedStore
					.getOrComputeIfAbsent( //
						resourceKey(sharedAnnotation), //
						__ -> newResource(sharedAnnotation, resourceFactory), //
						Resource.class);
			putNewLockForShared(sharedAnnotation, scopedStore);

			Object result;
			try {
				result = resource.get();
			}
			catch (Exception ex) {
				// @formatter:off
				String message = String.format(
						"Unable to get the contents of the resource created by `%s`",
						sharedAnnotation.factory());
				// @formatter:on
				throw new ParameterResolutionException(message, ex);
			}

			if (result == null) {
				// @formatter:off
				String message = String.format(
						"The resource returned by [%s] was null, which is not allowed",
						getMethod(resource.getClass(), "get"));
				// @formatter:on
				throw new ParameterResolutionException(message);
			}

			return result;
		}
		finally {
			SHARED_ANNOTATION_RESOLUTION_LOCK.unlock();
		}
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
			String message = String.format(
					"The `Resource` instance returned by the factory method [%s] with arguments %s was null, which is not allowed",
					getMethod(resourceFactory.getClass(), "create", List.class),
					arguments);
			// @formatter:on
			throw new ParameterResolutionException(message);
		}

		return result;
	}

	private void throwIfHasAnnotationWithSameNameButDifferentType(ExtensionContext.Store scopedStore,
			Shared sharedAnnotation) {
		ResourceFactory<?> presentResourceFactory = //
			scopedStore.getOrDefault(factoryKey(sharedAnnotation), ResourceFactory.class, null);

		if (presentResourceFactory == null) {
			scopedStore.put(keyOfFactoryKey(sharedAnnotation), factoryKey(sharedAnnotation));
		} else {
			String presentResourceFactoryName = //
				scopedStore.getOrDefault(keyOfFactoryKey(sharedAnnotation), String.class, null);

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

	private void throwIfHasAnnotationWithSameNameButDifferentScope(ExtensionContext.Store rootStore,
			Shared sharedAnnotation) {
		Shared presentSharedAnnotation = rootStore
				.getOrDefault(sharedAnnotationKey(sharedAnnotation), Shared.class, null);

		if (presentSharedAnnotation == null) {
			rootStore.put(sharedAnnotationKey(sharedAnnotation), sharedAnnotation);
		} else {
			if (presentSharedAnnotation.name().equals(sharedAnnotation.name())
					&& !presentSharedAnnotation.scope().equals(sharedAnnotation.scope())) {
				// @formatter:off
				String message =
						String.format(
								"Two or more parameters are annotated with @Shared annotations with the name " +
										"\"%s\" but with different scopes",
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

	private String resourceLockKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource lock";
	}

	private String keyOfFactoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory key";
	}

	private String sharedAnnotationKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " shared annotation";
	}

	private String testMethodDescription(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod().map(method -> "method [" + method + ']').orElse("an unknown method");
	}

	private Method getMethod(Class<?> clazz, String method, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(method, parameterTypes);
		}
		catch (NoSuchMethodException e) {
			throw new IllegalStateException(
				String.format("There should be a `%s` method on class `%s`", method, clazz.getTypeName()), e);
		}
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, testFactoryMethod(extensionContext), extensionContext);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	private <T> T runSequentially(Invocation<T> invocation, Executable executable, ExtensionContext extensionContext)
			throws Throwable {
		// Parallel tests must not concurrently access shared resources. To ensure that, we associate a lock with
		// each shared resource and require a test to hold all locks associated with the shared resources it uses.
		//
		// This harbors a risk of deadlocks. For example, given these tests and the respective shared resources
		// that they want to use:
		//
		//  - test1 -> [A, B]
		//  - test2 -> [B, C]
		//  - test3 -> [C, A]
		//
		// If test1 gets A, then test2 gets B, and then test3 gets C, none of the tests can get the second lock
		// they need and so they can also never give up the one they hold.
		//
		// This is known as the Dining Philosophers Problem [1] and a solution is to order locks before acquiring them.
		// In the above example, test3 would start with trying to get A and, since it can't, block on that. Then test2
		// is free to continue and eventually release the locks.
		//
		// We implement the solution here by lexicographically sorting the locks by the (globally unique) name of the
		// shared resource that each lock is (uniquely) associated with.
		//
		// [1] https://en.wikipedia.org/wiki/Dining_philosophers_problem

		List<Shared> sharedAnnotations = findShared(executable);
		List<ReentrantLock> locks = sortedLocksForSharedResources(sharedAnnotations, extensionContext);
		return invokeWithLocks(invocation, locks);
	}

	private List<ReentrantLock> sortedLocksForSharedResources(Collection<Shared> sharedAnnotations,
			ExtensionContext extensionContext) {
		List<Shared> sortedAnnotations = sharedAnnotations.stream().sorted(comparing(Shared::name)).collect(toList());
		List<ExtensionContext.Store> stores = //
			sortedAnnotations
					.stream() //
					.map(shared -> scopedStore(extensionContext, shared.scope()))
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

	private ExtensionContext.Store scopedStore(ExtensionContext extensionContext, Shared.Scope scope) {
		ExtensionContext scopedContext = scopedContext(extensionContext, scope);
		return scopedContext.getStore(NAMESPACE);
	}

	private ExtensionContext scopedContext(ExtensionContext extensionContext, Shared.Scope scope) {
		if (scope == Shared.Scope.SOURCE_FILE) {
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

	private List<Shared> findShared(Executable executable) {
		return Arrays
				.stream(executable.getParameters())
				.map(parameter -> AnnotationSupport.findAnnotation(parameter, Shared.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	private void putNewLockForShared(Shared shared, ExtensionContext.Store store) {
		store.getOrComputeIfAbsent(resourceLockKey(shared), __ -> new ReentrantLock(), ReentrantLock.class);
	}

	private ReentrantLock findLockForShared(Shared shared, ExtensionContext.Store store) {
		// @formatter:off
		return Optional.ofNullable(store.get(resourceLockKey(shared), ReentrantLock.class))
				.orElseThrow(() -> {
					String message = String.format("There should be a shared resource for the name %s", shared.name());
					return new IllegalStateException(message);
				});
		// @formatter:on
	}

	private <T> T invokeWithLocks(Invocation<T> invocation, List<ReentrantLock> locks) throws Throwable {
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

}
