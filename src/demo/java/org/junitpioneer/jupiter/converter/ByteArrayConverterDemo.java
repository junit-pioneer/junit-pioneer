package org.junitpioneer.jupiter.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayConverterDemo {

    // tag::byte_array_conversion[]
    @ParameterizedTest
    @ValueSource(ints = { 13, 17, 23, 29 })
    void test(@ByteArrayConversion byte[] bytes) {
        assertThat(bytes).hasSize(4);
    }
    // end::byte_array_conversion[]

}
