package org.junitpioneer.testkit.assertion;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.Events;
import org.junitpioneer.testkit.assertion.suite.TestSuiteFailureAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteFailureMessageAssert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TestSuiteAssertBase extends AbstractPioneerAssert<TestSuiteAssertBase, Events> implements TestSuiteFailureAssert, TestSuiteFailureMessageAssert {

    TestSuiteAssertBase(Events events) {
        super(events, TestSuiteAssertBase.class, 0);
    }

    private static String asCaptureGroup(String... strings) {
        String regexBody = Stream.of(strings).map(s -> ".*" + s + ".*").collect(Collectors.joining("|"));
        return String.format("(%s)", regexBody);
    }

    @SafeVarargs
    @Override
    public final TestSuiteFailureMessageAssert withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes) {
        Stream<Class<? extends Throwable>> throwableStream = getAllExceptions()
                .map(Throwable::getClass);
        assertThat(throwableStream).containsOnly(exceptionTypes);
        return this;
    }

    public TestSuiteFailureMessageAssert withExceptions() {
        assertThat(actual.failed().count()).isEqualTo(getAllExceptions().count());
        return this;
    }

    @Override
    public void withMessagesContainingAny(String... messageParts) {
        String regex = asCaptureGroup(messageParts);
        getAllExceptions().forEach(ex ->
                assertThat(ex).hasMessageMatching(regex)
        );
    }

    @Override
    public void withMessagesContainingAll(String... messageParts) {
        Set<String> strings = new HashSet<>(Arrays.asList(messageParts));
        getAllExceptions().forEach(ex -> {
                if (strings.stream().filter(matchingMessagePart(ex)).count() != 1)
                    throw new AssertionError("One exception should match exactly one message part!");
                strings.removeIf(matchingMessagePart(ex));
        });
        assertThat(strings).isEmpty();
    }

    private Predicate<String> matchingMessagePart(Throwable ex) {
        return messagePart -> ex.getMessage().matches(".*" + messagePart + ".*");
    }


    private Stream<Throwable> getAllExceptions() {
        return actual
                .failed()
                .stream()
                .map(fail -> fail.getPayload(TestExecutionResult.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TestExecutionResult::getThrowable)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
