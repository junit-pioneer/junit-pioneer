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

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	private static final SharedResourceCoordinator SHARED_RESOURCE_COORDINATOR = //
		new SharedResourceCoordinator(NAMESPACE);

	private static final Lock SHARED_ANNOTATION_RESOLUTION_LOCK = new ReentrantLock();

	private static final AtomicLong KEY_GENERATOR = new AtomicLong(0);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (parameterContext.isAnnotated(New.class) && parameterContext.isAnnotated(Shared.class)) {
			// @formatter:off
			String message =
					format(
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
			ExtensionContext.Store scopedStore = //
				SHARED_RESOURCE_COORDINATOR.scopedStore(extensionContext, sharedAnnotation.get().scope());
			ExtensionContext.Store rootStore = extensionContext.getRoot().getStore(NAMESPACE);
			Object resource = resolveShared(sharedAnnotation.get(), parameters, scopedStore, rootStore);
			return checkType(resource, parameterContext.getParameter().getType());
		}

		// @formatter:off
		String message = format(
				"Parameter [%s] in %s is not annotated with @New or @Shared",
				parameterContext.getParameter(), testMethodDescription(extensionContext));
		// @formatter:on
		throw new ParameterResolutionException(message);
	}

	private <T> T checkType(Object resource, Class<T> type) {
		if (!type.isInstance(resource)) {
			String message = format("Parameter [%s] is not of the correct target type %s", resource, type);
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
			String message = format(
					"Unable to get the contents of the resource created by `%s`",
					resourceFactory.getClass().getTypeName());
			// @formatter:on
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message = format(
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
					.computeIfAbsent( //
						factoryKey(sharedAnnotation), //
						__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
						ResourceFactory.class);
			Resource<?> resource = scopedStore
					.computeIfAbsent( //
						resourceKey(sharedAnnotation), //
						__ -> newResource(sharedAnnotation, resourceFactory), //
						Resource.class);
			SHARED_RESOURCE_COORDINATOR.putNewLockForShared(sharedAnnotation, scopedStore);

			Object result;
			try {
				result = resource.get();
			}
			catch (Exception ex) {
				// @formatter:off
				String message = format(
						"Unable to get the contents of the resource created by `%s`",
						sharedAnnotation.factory());
				// @formatter:on
				throw new ParameterResolutionException(message, ex);
			}

			if (result == null) {
				// @formatter:off
				String message = format(
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
			arguments = List.of(((New) newOrSharedAnnotation).arguments());
		} else {
			arguments = List.of();
		}

		Resource<?> result;
		try {
			result = resourceFactory.create(arguments);
		}
		catch (Exception ex) {
			String message = //
				format("Unable to create a resource from `%s`", resourceFactory.getClass().getTypeName());
			throw new ParameterResolutionException(message, ex);
		}

		if (result == null) {
			// @formatter:off
			String message = format(
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
			scopedStore.get(factoryKey(sharedAnnotation), ResourceFactory.class);

		if (presentResourceFactory == null) {
			scopedStore.put(keyOfFactoryKey(sharedAnnotation), factoryKey(sharedAnnotation));
		} else {
			String presentResourceFactoryName = //
				scopedStore.get(keyOfFactoryKey(sharedAnnotation), String.class);

			if (factoryKey(sharedAnnotation).equals(presentResourceFactoryName)
					&& !sharedAnnotation.factory().equals(presentResourceFactory.getClass())) {
				// @formatter:off
				String message =
						format(
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
		Shared presentSharedAnnotation = rootStore.get(sharedAnnotationKey(sharedAnnotation), Shared.class);

		if (presentSharedAnnotation == null) {
			rootStore.put(sharedAnnotationKey(sharedAnnotation), sharedAnnotation);
		} else {
			if (presentSharedAnnotation.name().equals(sharedAnnotation.name())
					&& !presentSharedAnnotation.scope().equals(sharedAnnotation.scope())) {
				// @formatter:off
				String message =
						format(
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
					format(
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

	private String sharedAnnotationKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " shared annotation";
	}

	private String testMethodDescription(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod().map(method -> "method [" + method + ']').orElse("an unknown method");
	}

	private Method getMethod(Class<?> clazz, String method, Class<?>... parameterTypes) {
		return ReflectionSupport
				.findMethod(clazz, method, parameterTypes)
				.orElseThrow(() -> new IllegalStateException(
					format("There should be a `%s` method on class `%s`", method, clazz.getTypeName())));
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return SHARED_RESOURCE_COORDINATOR
				.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, testFactoryMethod(extensionContext), extensionContext);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return SHARED_RESOURCE_COORDINATOR
				.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	private Method testFactoryMethod(ExtensionContext extensionContext) {
		return extensionContext
				.getParent()
				.orElseThrow(() -> new IllegalStateException(
					"The parent extension context of a DynamicTest was not a @TestFactory-annotated test method"))
				.getRequiredTestMethod();
	}

}
