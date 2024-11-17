/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Random;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@DisplayName("Random parameter extension")
public class RandomParameterExtensionTests {

	@Test
	@DisplayName("should work with all the types included in SupportedTypes class")
	void shouldWorkWithAllSupportedTypes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(RandomParameterTestCases.class, "allSupportedTypes",
					SupportedTypes.class);

		assertThat(results).hasSingleSucceededTest();
	}

	@Test
	@DisplayName("should work with javax/jakarta validation annotations on parameter types")
	void shouldWorkWithValidationOnParameter() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(RandomParameterTestCases.class, "primitive", int.class);

		assertThat(results).hasSingleSucceededTest();
	}

	@Test
	@DisplayName("should work with javax/jakarta validation annotations on fields in the parameter type")
	void shouldWorkWithSimpleTypesWithJakartaValidationOnFields() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(RandomParameterTestCases.class, "simpleType", Simple.class);

		assertThat(results).hasSingleSucceededTest();
	}

	@Test
	@DisplayName("should work with javax/jakarta validation annotations on nested types in the parameter type")
	void shouldHaveValidFields() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(RandomParameterTestCases.class, "complexType", Complex.class);

		assertThat(results).hasSingleSucceededTest();
	}

	@Test
	void withSetters(@Random(seed = 12312) WithSetters withSetters) {
		System.out.println(withSetters.isBool());
		Assertions.assertThat(withSetters).hasNoNullFieldsOrProperties();
	}

	static class RandomParameterTestCases {

		@Test
		void allSupportedTypes(@Random SupportedTypes supportedTypes) {
			Assertions.assertThat(supportedTypes).usingRecursiveAssertion().hasNoNullFields();
		}

		@Test
		void primitive(@Random(seed = 11) @Min(100) @Max(101) int primitive) {
			Assertions.assertThat(primitive).isEqualTo(100);
		}

		@Test
		void simpleType(@Random Simple simple) {
			Assertions.assertThat(simple).usingRecursiveAssertion().hasNoNullFields();
			Assertions.assertThat(simple.getI()).isBetween(1, 999);
			Assertions.assertThat(simple.isBool()).isFalse();
		}

		@Test
		void complexType(@Random Complex complex) {
			Assertions.assertThat(complex).usingRecursiveAssertion().hasNoNullFields();
			Assertions.assertThat(complex.getSimple().getI()).isBetween(1, 999);
			Assertions.assertThat(complex.getSimple().isBool()).isFalse();
		}

	}

	public static class Simple {

		@Min(1)
		@Max(1000)
		private final int i;

		@AssertFalse
		private final boolean bool;

		public Simple(int i, boolean bool) {
			this.i = i;
			this.bool = bool;
		}

		public int getI() {
			return i;
		}

		public boolean isBool() {
			return bool;
		}

	}

	public static class Complex {

		private final Simple simple;
		private final double d;

		public Complex(Simple simple, double d) {
			this.simple = simple;
			this.d = d;
		}

		public Simple getSimple() {
			return simple;
		}

		public double getD() {
			return d;
		}

	}

	public static class WithSetters {

		private boolean bool;

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}

	}

	public static class SupportedTypes {

		private final int i;
		private final double d;
		private final boolean b;
		private final float f;
		private final long l;

		public SupportedTypes(int i, double d, boolean b, float f, long l) {
			this.i = i;
			this.d = d;
			this.b = b;
			this.f = f;
			this.l = l;
		}

		public int getI() {
			return i;
		}

		public double getD() {
			return d;
		}

		public boolean isB() {
			return b;
		}

		public float getF() {
			return f;
		}

		public long getL() {
			return l;
		}

	}

}
