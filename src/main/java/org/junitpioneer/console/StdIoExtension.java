/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.console;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author smoyer1
 *
 */
public class StdIoExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

	/* (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.ParameterResolver#supports(org.junit.jupiter.api.extension.ParameterContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.ParameterResolver#resolve(org.junit.jupiter.api.extension.ParameterContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.AfterEachCallback#afterEach(org.junit.jupiter.api.extension.TestExtensionContext)
	 */
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.BeforeEachCallback#beforeEach(org.junit.jupiter.api.extension.TestExtensionContext)
	 */
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
