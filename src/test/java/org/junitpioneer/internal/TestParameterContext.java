/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.support.AnnotationSupport;

public class TestParameterContext implements ParameterContext {

	private static final UnsupportedOperationException NOT_SUPPORTED_IN_TEST_CONTEXT = new UnsupportedOperationException(
		"Not supported in test context");
	private final Parameter parameter;

	public TestParameterContext(Parameter parameter) {
		this.parameter = requireNonNull(parameter);
	}

	@Override
	public Parameter getParameter() {
		return parameter;
	}

	@Override
	public int getIndex() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<Object> getTarget() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public boolean isAnnotated(Class<? extends Annotation> annotationType) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
		return AnnotationSupport.findAnnotation(parameter, annotationType);
	}

	@Override
	public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

}
