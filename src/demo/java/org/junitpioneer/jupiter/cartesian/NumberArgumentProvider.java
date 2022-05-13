package org.junitpioneer.jupiter.cartesian;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

public class NumberArgumentProvider {

    // tag::cartesian_number_argument_provider[]
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @CartesianArgumentsSource(NumberArgumentsProvider.class)
    public @interface NumberSource {
        int[] value();
    }

    class NumberArgumentsProvider implements CartesianMethodArgumentsProvider, AnnotationConsumer<NumberSource> {

        private int[] numbers;

        @Override
        public ArgumentSets provideArguments(ExtensionContext context) {
            int paramCount = context.getRequiredTestMethod().getParameters().length;
            ArgumentSets sets = ArgumentSets.create();
            for (int i = 0; i < paramCount; i++) {
                sets.argumentsForNextParameter(Arrays.stream(numbers).boxed());
            }
            return sets;
        }

        @Override
        public void accept(NumberSource source) {
            this.numbers = source.value();
        }

    }
    // end::cartesian_number_argument_provider[]
}
