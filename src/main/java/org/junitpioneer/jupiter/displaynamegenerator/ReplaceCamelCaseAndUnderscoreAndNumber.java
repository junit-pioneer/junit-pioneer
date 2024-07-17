/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * <p>A class extending {@linkplain DisplayNameGenerator.Standard }.</p>
 *
 * <p>This extension handles method names with CamelCase, underscore and numbers.</p>
 *
 * <p>The aim is to simplify unit test display names. Instead of using this method annotation {@linkplain org.junit.jupiter.api.DisplayName },
 * we can just use this class annotation {@linkplain org.junit.jupiter.api.DisplayNameGeneration } and use that method annotation if needed.
 * </p>
 *
 * <p>This generator follows 3 rules:</p>
 *
 * <ul>
 *     <li>Each uppercase letter is turned into its lowercase value prepended by space.</li>
 *     <li>Each underscore is turned into space. Words bounded by underscores or just starting with underscore are not transformed. Usually these words represent classes, variables...</li>
 *     <li>Each number is prepended by space.</li>
 * </ul>
 * <p>
 * Usage example:
 *
 * <pre>
 *
 * {@code @DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)}
 * class ExampleTest {
 *     {@code @Test}
 *     //Equivalent of @DisplayName("Should return error when maxResults is negative")
 *     void shouldReturnErrorWhen_maxResults_IsNegative() {}
 *     {@code @Test}
 *     //Equivalent of @DisplayName("Should create limit with range")
 *     void shouldCreateLimitWithRange() {}
 *
 *     {@code @Test}
 *     //Equivalent of @DisplayName("Should return 5 errors")
 *     void shouldReturn5Errors() {}
 *
 *     {@code @ParameterizedTest}
 *     {@code @ValueSource(strings = {"job", "player"})}
 *     //Equivalent of @DisplayName("Should return the value of maxResults (String)")
 *     void shouldReturnTheValueOf_maxResults(String input) {}
 *
 *     {@code @Test}
 *     //Equivalent of @DisplayName("Should return the number of errors as numberOfErrors inferior or equal to 15")
 *     void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15() {}
 *
 *     //The class annotation has no effect here
 *     {@code @DisplayName("@DisplayName prevails")}
 *     {@code @Test}
 *     void testDisplayNamePrevails() {}
 * }
 *
 *     
 * </pre>
 *
 * @since 2.3.0
 * @see org.junit.jupiter.api.DisplayNameGenerator.Standard
 */
public class ReplaceCamelCaseAndUnderscoreAndNumber extends DisplayNameGenerator.Standard {

	public static final DisplayNameGenerator INSTANCE = new ReplaceCamelCaseAndUnderscoreAndNumber();

	private ReplaceCamelCaseAndUnderscoreAndNumber() {
	}

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		if (hasParameters(testMethod)) {
			return replaceCamelCaseAndUnderscoreAndNumber(testMethod.getName()) + " "
					+ DisplayNameGenerator.parameterTypesAsString(testMethod);
		}
		return replaceCamelCaseAndUnderscoreAndNumber(testMethod.getName());
	}

	private String replaceCamelCaseAndUnderscoreAndNumber(String input) {
		StringBuilder result = new StringBuilder();
		/*
		 * Each method name starts with "should" then the displayed name starts with "Should"
		 * */
		result.append(Character.toUpperCase(input.charAt(0)));

		/*
		 * There are 2 groups of method name: with and without underscore
		 * */
		if (input.contains("_")) {
			boolean insideUnderscores = false;
			for (int i = 1; i < input.length(); i++) {
				char currentChar = input.charAt(i);
				if (currentChar == '_') {
					result.append(' ');
					/*
					 * If the current char is an underscore and insideUnderscores is true,
					 * it means there is an opening underscore and this one is the closing one
					 * then we set insideUnderscores to false.
					 * */
					/*
					 * If the current char is an underscore and insideUnderscores is false,
					 * it means there is not an opening underscore and this one is the opening one
					 * then we set insideUnderscores to true.
					 * */
					insideUnderscores = !insideUnderscores;
				} else {
					/*
					 * If the character is inside underscores, we append the character as it is.
					 * */
					if (insideUnderscores) {
						result.append(currentChar);
					} else {
						//CamelCase handling for method name containing "_"
						if (Character.isUpperCase(currentChar)) {
							//We already replace "_" with " ". If the previous character is "_", we will not add extra space
							if (input.charAt(i - 1) != '_') {
								result.append(' ');
							}
							result.append(Character.toLowerCase(currentChar));
						} else {
							result.append(currentChar);
						}
					}
				}
			}
		} else {
			//CamelCase handling for method name not containing "_"
			for (int i = 1; i < input.length(); i++) {
				if (Character.isUpperCase(input.charAt(i))) {
					result.append(' ');
					result.append(Character.toLowerCase(input.charAt(i)));
				} else {
					result.append(input.charAt(i));
				}
			}
		}

		/*Add space before all numbers
		 * Nothing is done after number because each number must be followed by an uppercase letter. Thus, there will be space between these two.
		 * In case of a lowercase letter following number, this will be considered as the user's choice. Thus, there will be no space between these two.
		 * */
		return result.toString().replaceAll("(\\d+)", " $1");
	}

	private boolean hasParameters(Method method) {
		return method.getParameterCount() > 0;
	}

}
