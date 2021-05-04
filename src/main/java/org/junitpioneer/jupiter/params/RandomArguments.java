package org.junitpioneer.jupiter.params;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(RandomArgumentsProvider.class)
public @interface RandomArguments {

	int count() default 1;

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface RandomInt {

		int min() default 0;

		int max() default Integer.MAX_VALUE;

	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface RandomString {

		int minLength() default 1;

		int maxLength() default 10;

	}

}
