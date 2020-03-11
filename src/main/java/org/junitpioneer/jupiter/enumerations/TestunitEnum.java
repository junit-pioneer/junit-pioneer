package org.junitpioneer.jupiter.enumerations;

/**
 * Enumeration for testobjects to be used e.g. as parameters or in messages.
 */
public enum TestunitEnum {
  CLASS("Class"), TEST("Test"), PACKAGE("Package");

  private String name;

  TestunitEnum(String name) {
  }

  public String getName() {
    return name;
  }

  /**
   * Method to find an enum element by its name.
   *
   * @param nameToFind Name the element should have
   * @return Entry of the enum if found
   * @throws IllegalArgumentException Thrown when no value was found
   */
  public TestunitEnum findByName(String nameToFind) {
    for(TestunitEnum unit : values()) {
      if(unit.getName().equals(nameToFind)) {
        return unit;
      }
    }

    throw new IllegalArgumentException("Search value does not correspondent to any value in the enum.");
  }
}
