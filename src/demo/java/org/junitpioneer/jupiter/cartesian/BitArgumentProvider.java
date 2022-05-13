package org.junitpioneer.jupiter.cartesian;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class BitArgumentProvider {

    // tag::cartesian_bit_source_annotation[]
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @CartesianArgumentsSource(BitArgumentsProvider.class)
    public @interface BitSource {
    }
    // end::cartesian_bit_source_annotation[]

    // tag::cartesian_bit_argument_provider[]
    class BitArgumentsProvider implements CartesianMethodArgumentsProvider {

        @Override
        public ArgumentSets provideArguments(ExtensionContext context) {
            int paramCount = context.getRequiredTestMethod().getParameters().length;
            ArgumentSets sets = ArgumentSets.create();
            for (int i = 0; i < paramCount; i++) {
                sets.argumentsForNextParameter(0, 1);
            }
            return sets;
        }
    }
    // end::cartesian_bit_argument_provider[]
}
