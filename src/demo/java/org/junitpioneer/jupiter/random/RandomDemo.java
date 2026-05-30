/*
 * Copyright 2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import org.junit.jupiter.api.Test;

public class RandomDemo {

	//tag::employee_service_test_with_utils[]
	@Test
	void employeeServiceTest() {
		var employee = DataUtils.createNewEmployee();
		// ... test code using employee
	}
	//end::employee_service_test_with_utils[]

	//tag::random_basic_use[]
	@Test
	void basicRandomTest(@Random Employee employee) {
		// ... test code using employee
	}
	//end::random_basic_use[]

	//tag::random_with_setter[]
	@Test
	void isCloseToRetirementTest(@Random Employee employee) {
		employee.setAge(65);
		// ... testing isCloseToRetirement() using employee
	}
	//end::random_with_setter[]

	static class DataUtils {

		static Employee createNewEmployee() {
			var employee = new Employee();
			employee.setFirstName("John");
			employee.setLastName("Doe");
			employee.setAge(22);
			employee.setGender(Gender.MALE);
			employee.setDivision("FINANCE");
			employee.setCity("Foo");
			employee.setCountry("Bar");
			return employee;
		}

	}

}
