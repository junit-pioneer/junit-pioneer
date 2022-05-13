package org.junitpioneer.jupiter.cartesian;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PeopleProviderSources {

    // tag::cartesian_people_provider_with_CartesianParameterArgumentsProvider[]
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CartesianArgumentsSource(PeopleProvider.class)
    @interface People {

        String[] names();

        int[] ages();

    }

    class PeopleProvider implements CartesianParameterArgumentsProvider {

        @Override
        public Stream<Person> provideArguments(ExtensionContext context, Parameter parameter) {
            People source = Objects.requireNonNull(parameter.getAnnotation(People.class));
            return IntStream.range(0, source.names().length)
                    .mapToObj(i -> new Person(source.names()[i], source.ages()[i]));
        }
    }
    // end::cartesian_people_provider_with_CartesianParameterArgumentsProvider[]

    class Person {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    // tag::cartesian_people_provider_with_AnnotationConsumer[]
    class PeopleProviderWithAnnotationConsumer implements CartesianParameterArgumentsProvider, AnnotationConsumer<People> {

        private People source;

        @Override
        public Stream<Person> provideArguments(ExtensionContext context, Parameter parameter) {
            return IntStream.range(0, source.names().length)
                    .mapToObj(i -> new Person(source.names()[i], source.ages()[i]));
        }

        @Override
        public void accept(People source) {
            this.source = source;
        }

    }
    // end::cartesian_people_provider_with_AnnotationConsumer[]


}
