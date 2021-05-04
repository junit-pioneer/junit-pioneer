package org.junitpioneer.jupiter.params;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junitpioneer.internal.PioneerUtils;
import org.junitpioneer.jupiter.params.RandomArguments.RandomInt;
import org.junitpioneer.jupiter.params.RandomArguments.RandomString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class RandomArgumentsProvider implements ArgumentsProvider {

	private static final Collection<Class<? extends Annotation>> RANDOM_ANNOTATIONS = Arrays.asList(
			RandomString.class, RandomInt.class
	);

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Random random = new Random();
		int count = count(context);
		List<Supplier<Object>> randomValueSuppliers = createRandomValueSuppliers(random, context);
		return IntStream
				.range(0, count)
				.mapToObj(__ -> Arguments.of(randomValueSuppliers.stream().map(Supplier::get).toArray()));
	}

	private static int count(ExtensionContext context) {
		return AnnotationUtils
				.findAnnotation(context.getTestMethod(), RandomArguments.class)
				.map(RandomArguments::count)
				.orElseThrow(IllegalStateException::new);
	}

	private List<Supplier<Object>> createRandomValueSuppliers(Random random, ExtensionContext context) {
		Method method = context.getTestMethod().orElseThrow(IllegalStateException::new);
		return Stream
				.of(method.getParameters())
				.filter(this::hasRandomAnnotation)
				.map(parameter -> supplierFor(parameter, random))
				.collect(toList());
	}

	private boolean hasRandomAnnotation(Parameter parameter) {
		return Stream
				.of(parameter.getAnnotations())
				.map(Annotation::annotationType)
				.anyMatch(RANDOM_ANNOTATIONS::contains);
	}

	private Supplier<Object> supplierFor(Parameter parameter, Random random) {
		return PioneerUtils
				.firstPresent(
						get(parameter, RandomString.class)
								.map(randomString -> new RandomStringSupplier(randomString, random)),
						get(parameter, RandomInt.class)
								.map(randomInt -> new RandomIntSupplier(randomInt, random))
				)
				.orElseThrow(IllegalStateException::new);
	}

	private static <ANNOTATION extends Annotation> Optional<ANNOTATION> get(
			Parameter parameter, Class<ANNOTATION> annotationType) {
		return Optional.ofNullable(parameter.getAnnotation(annotationType));
	}

	private class RandomStringSupplier implements Supplier<Object> {

		private final RandomString string;
		private final Random random;

		public RandomStringSupplier(RandomString string, Random random) {
			this.string = string;
			this.random = random;
		}

		@Override
		public String get() {
			int length = random.nextInt(string.maxLength() - string.minLength()) + string.minLength();
			byte[] chars = new byte[length];
			random.nextBytes(chars);
			return new String(chars, StandardCharsets.UTF_8);
		}

	}

	private class RandomIntSupplier implements Supplier<Object> {

		private final RandomInt integer;
		private final Random random;

		public RandomIntSupplier(RandomInt integer, Random random) {
			this.integer = integer;
			this.random = random;
		}

		@Override
		public Integer get() {
			return random.nextInt(integer.max() - integer.min()) + integer.min();
		}

	}

}
