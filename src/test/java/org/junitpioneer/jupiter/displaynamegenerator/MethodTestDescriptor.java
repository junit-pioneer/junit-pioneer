package org.junitpioneer.jupiter.displaynamegenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;

public class MethodTestDescriptor extends AbstractTestDescriptor {
    private final Class<?> testClass;
    private final Method testMethod;

    public MethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
        super(uniqueId,
                testMethod.isAnnotationPresent(DisplayName.class) ? testMethod.getAnnotation(DisplayName.class).value() :
                        ReplaceCamelCaseAndUnderscoreAndNumber.INSTANCE.generateDisplayNameForMethod(testClass, testMethod),
                MethodSource.from(testClass, testMethod));
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }
}
