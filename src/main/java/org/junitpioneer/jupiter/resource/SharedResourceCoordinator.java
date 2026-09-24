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
import static java.util.Comparator.comparing;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.support.AnnotationSupport;

final class SharedResourceCoordinator {

	private final ExtensionContext.Namespace namespace;

	SharedResourceCoordinator(ExtensionContext.Namespace namespace) {
		this.namespace = namespace;
	}

	<T> T runSequentially(Invocation<T> invocation, Executable executable, ExtensionContext extensionContext)
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
		// they need, and so they can also never give up the one they hold.
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

	ExtensionContext.Store scopedStore(ExtensionContext extensionContext, Shared.Scope scope) {
		ExtensionContext scopedContext = scopedContext(extensionContext, scope);
		return scopedContext.getStore(namespace);
	}

	void putNewLockForShared(Shared shared, ExtensionContext.Store store) {
		store.computeIfAbsent(resourceLockKey(shared), __ -> new ReentrantLock(), ReentrantLock.class);
	}

	private List<ReentrantLock> sortedLocksForSharedResources(Collection<Shared> sharedAnnotations,
			ExtensionContext extensionContext) {
		List<Shared> sortedAnnotations = sharedAnnotations.stream().sorted(comparing(Shared::name)).toList();
		List<ExtensionContext.Store> stores = //
			sortedAnnotations
					.stream() //
					.map(shared -> scopedStore(extensionContext, shared.scope()))
					.toList();
		return IntStream
				.range(0, sortedAnnotations.size()) //
				.mapToObj(i -> findLockForShared(sortedAnnotations.get(i), stores.get(i)))
				.toList();
	}

	private ExtensionContext scopedContext(ExtensionContext extensionContext, Shared.Scope scope) {
		if (scope == Shared.Scope.SOURCE_FILE) {
			// search for the test scope that's associated with the same source file,
			// which we assume is the one that has the root context as parent
			// (contexts in between the test method context and the source class context
			//  would belong to nested test classes)
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
				.toList();
	}

	private ReentrantLock findLockForShared(Shared shared, ExtensionContext.Store store) {
		return Optional
				.ofNullable(store.get(resourceLockKey(shared), ReentrantLock.class))
				.orElseThrow(() -> new IllegalStateException(
					format("There should be a shared resource for the name %s", shared.name())));
	}

	private String resourceLockKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource lock";
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
