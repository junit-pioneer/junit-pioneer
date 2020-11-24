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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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
		List<Object> arguments = invocationContext.getArguments();
		checkRequiredAnnotations(testMethod);

		AnnotationSupport.findAnnotation(testMethod, DisableIfAllParameters.class).ifPresent(allParams -> {
			verifyNonEmptyInputs(allParams);
			ArgumentChecker.checkAll(arguments).matches(allParams.matches());
			ArgumentChecker.checkAll(arguments).contains(allParams.contains());
		});
		AnnotationSupport.findAnnotation(testMethod, DisableIfAnyParameter.class).ifPresent(anyParam -> {
			verifyNonEmptyInputs(anyParam);
			ArgumentChecker.checkAny(arguments).matches(anyParam.matches());
			ArgumentChecker.checkAny(arguments).contains(anyParam.contains());
		});
		List<DisableIfParameter> annotations = AnnotationSupport
				.findRepeatableAnnotations(testMethod, DisableIfParameter.class);
		for (int i = 0; i < annotations.size(); i++) {
			DisableIfParameter parameter = annotations.get(i);
			verifyNonEmptyInputs(parameter);
			Object argument = findArgument(extensionContext, testMethod, arguments, parameter, i);
			ArgumentChecker.check(argument).matches(parameter.matches());
			ArgumentChecker.check(argument).contains(parameter.contains());
		}
		invocation.proceed();
	}

	private Object findArgument(ExtensionContext context, Method testMethod, List<Object> arguments, DisableIfParameter annotation, int index) {
		if (!annotation.name().isEmpty())
			if (Arrays.stream(testMethod.getParameters()).anyMatch(Parameter::isNamePresent))
				return findArgumentByName(testMethod, arguments, annotation.name());
			else
				context.publishReportEntry("Could not resolve parameter by name, trying by index");
		if (annotation.index() > -1)
			return findArgumentByIndex(arguments, annotation.index());
		// get argument by annotation index (implicit)
		return arguments.get(index);
	}

	private Object findArgumentByName(Method testMethod, List<Object> arguments, String name) {
		return arguments.get(findParameterIndex(testMethod, name));
	}

	private int findParameterIndex(Method testMethod, String name) {
		Parameter[] parameters = testMethod.getParameters();
		for (int i = 0; i < parameters.length; i++)
			if (parameters[i].getName().equals(name))
				return i;
		throw new ExtensionConfigurationException(
			format("Could not find parameter named %s in test %s", name, testMethod));
	}

	private Object findArgumentByIndex(List<Object> arguments, int index) {
		verifyValidIndex(arguments, index);
		return arguments.get(index);
	}

	private void verifyValidIndex(List<Object> arguments, int index) {
		if (index < 0 || index > arguments.size())
			throw new ExtensionConfigurationException(
				format("Annotation has invalid index [%s], should be less than %s", index, arguments.size()));
	}

	private static void checkRequiredAnnotations(Method testMethod) {
		if (!AnnotationSupport.findAnnotation(testMethod, DisableIfAnyParameter.class).isPresent()
				&& !AnnotationSupport.findAnnotation(testMethod, DisableIfAllParameters.class).isPresent()
				&& AnnotationSupport.findRepeatableAnnotations(testMethod, DisableIfParameter.class).isEmpty()) {
			throw new ExtensionConfigurationException(
				"Required at least one of the following: @DisableIfParameter, @DisableIfAllParameter, @DisableIfAnyParameter but found none.");
		}
	}

	private static void verifyNonEmptyInputs(DisableIfParameter annotation) {
		if (annotation.contains().length == 0 && annotation.matches().length == 0)
			throw new ExtensionConfigurationException(
				format("%s requires that either `contains` or `matches` has at least one element, but both are empty.",
					DisableIfParameter.class.getSimpleName()));
	}

	private static void verifyNonEmptyInputs(DisableIfAnyParameter annotation) {
		if (annotation.contains().length == 0 && annotation.matches().length == 0)
			throw new ExtensionConfigurationException(
				format("%s requires that either `contains` or `matches` has at least one element, but both are empty.",
					DisableIfAnyParameter.class.getSimpleName()));
	}

	private static void verifyNonEmptyInputs(DisableIfAllParameters annotation) {
		if (annotation.contains().length == 0 && annotation.matches().length == 0)
			throw new ExtensionConfigurationException(
				format("%s requires that either `contains` or `matches` has at least one element, but both are empty.",
					DisableIfAllParameters.class.getSimpleName()));
	}

	private static class ArgumentChecker {

		private final List<Object> arguments;
		private final boolean checkAny;

		private ArgumentChecker(List<Object> arguments, boolean checkAny) {
			this.arguments = arguments;
			this.checkAny = checkAny;
		}

		static ArgumentChecker checkAll(List<Object> arguments) {
			return new ArgumentChecker(arguments, false);
		}

		static ArgumentChecker checkAny(List<Object> arguments) {
			return new ArgumentChecker(arguments, true);
		}

		static ArgumentChecker check(Object argument) {
			return new ArgumentChecker(Collections.singletonList(argument), true);
		}

		public void matches(String[] matches) {
			Predicate<Object> check = argument -> Arrays.stream(matches).anyMatch(argument.toString()::matches);
			if (checkAny)
				matchAny(check);
			else
				matchAll(check);
		}

		private void matchAll(Predicate<Object> check) {
			if (arguments.stream().allMatch(check))
				throw new TestAbortedException(
					"All arguments matched one or more regular expression(s) from the `matches` array.");
		}

		private void matchAny(Predicate<Object> check) {
			if (arguments.stream().anyMatch(check))
				throw new TestAbortedException(
					"One or more arguments matched a regular expression from the `matches` array.");
		}

		public void contains(String[] contains) {
			Predicate<Object> check = argument -> Arrays.stream(contains).anyMatch(argument.toString()::contains);
			if (checkAny)
				containsAny(check);
			else
				containsAll(check);
		}

		private void containsAll(Predicate<Object> check) {
			if (arguments.stream().allMatch(check))
				throw new TestAbortedException(
					"All arguments contained one or more value(s) from the `contains` array.");
		}

		private void containsAny(Predicate<Object> check) {
			if (arguments.stream().anyMatch(check))
				throw new TestAbortedException("One or more arguments contained a value from the `contains` array.");
		}

	}

}
