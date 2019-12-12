package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IssueExtension.class)
public class IssueTests {

//    @Test
//    void checkMethodNotAnnotated() {
//        assertThat(IssueTestCase#testNoAnnotation).isNotAnnotatedWith(Issue.class);
//        assertThat(IssueTestCase#testIsAnnotated).isAnnotatedWith(Issue.class);
//    }
//
//    @Test
//    void checkMethodIstAnnotated() {
//        assertThat(IssueTestCase#testIsAnnotated).isAnnotatedWith(Issue.class);
//        assertThat(IssueTestCase#testIsAnnotated).hasAnnotationValue(Issue.class, "Req 11");
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
