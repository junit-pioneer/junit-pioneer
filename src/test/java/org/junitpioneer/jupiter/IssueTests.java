package org.junitpioneer.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junitpioneer.AbstractPioneerTestEngineTests;
import org.junitpioneer.vintage.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

@ExtendWith(IssueExtension.class)
public class IssueTests extends AbstractPioneerTestEngineTests {

    @Test
    void testIssueAnnotation() {

    }

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
