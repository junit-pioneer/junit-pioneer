package org.junitpioneer.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith(ResourceManagerExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Resources {}
