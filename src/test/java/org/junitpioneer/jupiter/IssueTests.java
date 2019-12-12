/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

@ExtendWith(IssueExtension.class)
public class IssueTests extends AbstractJupiterTestEngineTests {


//    @Test
//    void checkMethodNotAnnotated() {
//         ExtensionContext context = JunitJupiterTestExecutionerMagicThing.getContext();
//        executeTestsForClass(IssueTestCase.class);
//
//        if("testNoAnnotation".equals(exectuedMethod.getName())) {
//            assertThat(exectuedMethod).isNotAnnotatedWith(Issue.class);
//        }
//    }
//
//    @Test
//    void checkMethodIstAnnotated() {
//        ExtensionContext context = JunitJupiterTestExecutionerMagicThing.getContext();
//        executeTestsForClass(IssueTestCase.class);
//
//        if("testNoAnnotation".equals(exectuedMethod.getName())) {
//            assertThat(exectuedMethod.isAnnotatedWith(Issue.class);
//            assertThat(exectuedMethod.hasAnnotationValue(Issue.class, "Req 11");
//        }
//    }

    static class IssueTestCase {

        @Test
        void testNoAnnotation() {

        }

        @Issue("Req 11")
        @Test
        void testIsAnnotated() {

        }
    }
}
