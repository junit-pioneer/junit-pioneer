/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;

final class ResourceExtension implements ParameterResolver, InvocationInterceptor {

	private static final ExtensionContext.Namespace NAMESPACE = //
		ExtensionContext.Namespace.create(ResourceExtension.class);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		// TODO: if both annotations are present, throw an error instead of silently ignoring the test
		return parameterContext.isAnnotated(New.class) ^ parameterContext.isAnnotated(Shared.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<New> newAnnotation = parameterContext.findAnnotation(New.class);
		if (newAnnotation.isPresent()) {
			return resolve(newAnnotation.get(), extensionContext.getStore(NAMESPACE));
		}
		Optional<Shared> sharedAnnotation = parameterContext.findAnnotation(Shared.class);
		if (sharedAnnotation.isPresent()) {
			return resolve(sharedAnnotation.get(), parameterContext, extensionContext.getRoot().getStore(NAMESPACE));
		}
		throw new ParameterResolutionException(String
				.format( //
					"Parameter `%s` on %s is not annotated with @New or @Shared", //
					parameterContext.getParameter(), //
					extensionContext
							.getTestMethod()
							.map(method -> "method `" + method + '`')
							.orElse("an unknown method")));
	}

	private Object resolve(New newAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newAnnotation.value());
		store.put(newKey(), resourceFactory);
		Resource<?> resource;
		try {
			resource = resourceFactory.create(unmodifiableList(asList(newAnnotation.arguments())));
			store.put(newKey(), resource);
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to create a resource from `" + resourceFactory.getClass() + '`', e);
		}
		try {
			return resource.get();
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to get the contents of the resource created by `" + resourceFactory.getClass() + '`', e);
		}
	}

	private long newKey() {
		return keyGenerator.getAndIncrement();
	}

	private static final AtomicLong keyGenerator = new AtomicLong(0);

	private Object resolve(Shared sharedAnnotation, ParameterContext parameterContext, ExtensionContext.Store store) {
		throwIfHasAnnotationWithSameNameButDifferentType(store, sharedAnnotation);
		throwIfMultipleParametersHaveExactAnnotation(parameterContext, sharedAnnotation);

		ResourceFactory<?> resourceFactory = store
				.getOrComputeIfAbsent( //
					factoryKey(sharedAnnotation), //
					__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
					ResourceFactory.class);
		ResourceWithLock<?> resource;
		try {
			resource = store.getOrComputeIfAbsent(key(sharedAnnotation), __ -> {
				try {
					return new ResourceWithLock<>(
						resourceFactory.create(unmodifiableList(asList(sharedAnnotation.arguments())))
					);
				}
				catch (Exception e) {
					throw new UncheckedParameterResolutionException(new ParameterResolutionException(
						"Unable to create a resource from `" + sharedAnnotation.factory() + "`", e));
				}
			}, ResourceWithLock.class);
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

	private void throwIfMultipleParametersHaveExactAnnotation(ParameterContext parameterContext,
			Shared sharedAnnotation) {
		long parameterCount = Arrays
				.stream(parameterContext.getDeclaringExecutable().getParameters())
				.filter(parameter -> hasAnnotation(parameter, sharedAnnotation))
				.count();
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

	private String key(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource";
	}

	private String factoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory";
	}

	private String keyOfFactoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory key";
	}

	private static final class UncheckedParameterResolutionException extends RuntimeException {

		public UncheckedParameterResolutionException(ParameterResolutionException cause) {
			super(cause);
		}

		@Override
		public synchronized ParameterResolutionException getCause() {
			return (ParameterResolutionException) super.getCause();
		}

	}

	// TODO: Intercept not just test methods, but all kinds of lifecycle methods.

	// TODO: What happens if a user requests a shared resource in a test constructor, and
	//       saves the resource in a field? That resource could then be used concurrently,
	//       which could lead to deadlocks!
	//       |
	//       So we should consider preventing users from requesting resources in test constructors.
	//       |
	//       Regardless, we need to document that if a resource is saved outside its respective
	//       lifecycle method, then if it's a @New resources then it will be closed when the
	//       lifecycle method has finished, and if it's a @Shared resource then flaky behaviour
	//       could occur since the resource can now be accessed concurrently.

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		// 1. use extension context to determine whether we're in charge (which is the case if there's at least one @Shared annotation)
		// 2. get all locks for all shared resources that are involved
		// 3. wait for all locks to become available
		// 4. invoke `invocation.proceed()`

		ExtensionContext.Store store = extensionContext.getRoot().getStore(NAMESPACE);
		List<ReentrantLock> locks = findSharedOnInvocation(invocationContext)
				// sort by @Shared's name to prevent deadlocks when locking later
				.sorted(comparing(Shared::name))
				.map(shared -> findLockForShared(shared, store))
				.collect(toList());
		System.out.println(invocationContext.getExecutable().getName() + ": locks = " + locks);
		invokeWithLocks(invocation, locks);
	}

	private Stream<Shared> findSharedOnInvocation(ReflectiveInvocationContext<Method> invocationContext) {
		return Arrays
				.stream(invocationContext.getExecutable().getParameters())
				.map(parameter -> AnnotationSupport.findAnnotation(parameter, Shared.class))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private ReentrantLock findLockForShared(Shared shared, ExtensionContext.Store store) {
		return Optional
				.ofNullable(store.get(key(shared), ResourceWithLock.class))
				.orElseThrow(
					() -> new IllegalStateException("There should be a shared resource for the name " + shared.name()))
				.lock();
	}

	private static <T> void invokeWithLocks(Invocation<T> invocation, List<ReentrantLock> locks) throws Throwable {
		// TODO handle `lock` throwing an exception?
		locks.forEach(Lock::lock);
		try {
			invocation.proceed();
		}
		finally {
			// unlock in reverse order because we have a hunch that otherwise there may be deadlocks
			// TODO handle `unlock` throwing an exception?
			for (int i = locks.size() - 1; i >= 0; i--) {
				locks.get(i).unlock();
			}
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
