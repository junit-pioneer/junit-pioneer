/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

class DisableIfParameterExtension implements InvocationInterceptor {

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Method testMethod = extensionContext.getRequiredTestMethod();
        Optional<DisableIfParameter> annotation = AnnotationSupport.findAnnotation(testMethod, DisableIfParameter.class);
        if (!annotation.isPresent())
            throw new ExtensionConfigurationException(DisableIfParameterExtension.class.getSimpleName() + " is active but no "
                + DisableIfParameter.class.getSimpleName() + " was found. This may be a bug.");
        if (invocationContext.getArguments().stream().anyMatch(arg -> arg.toString().equals(annotation.get().value())))
            throw new TestAbortedException();
        invocation.proceed();
	}

}
