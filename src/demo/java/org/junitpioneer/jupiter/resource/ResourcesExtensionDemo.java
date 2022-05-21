package org.junitpioneer.jupiter.resource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourcesExtensionDemo {

    // tag::create_new_resource_demo[]
    void test(@New(TemporaryDirectory.class) Path tempDir) {
        // Test code goes here, e.g.,
        assertTrue(Files.exists(tempDir));
    }
    // end::create_new_resource_demo[]

    // tag::create_new_resource_with_arg_demo[]
    void testWithArg(
            @New(value = TemporaryDirectory.class, arguments = { "customDirectoryName" })
            Path tempDir
    ) {
        // Test code goes here, e.g.,
        assertTrue(tempDir.endsWith("customDirectoryName"));
    }
    // end::create_new_resource_with_arg_demo[]

}
