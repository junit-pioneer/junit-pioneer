package org.junitpioneer.jupiter;

public @interface Shared {

  Class<? extends ResourceFactory<?>> value();

  String name();

  // TODO
  //String[] arguments() default {};
}
