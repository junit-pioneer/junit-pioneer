/**
 * Provides parameterized test arguments from JSON (inline or file).
 *
 * <p>Note that these extensions require a JSON parser to be available at run time,
 * which may include adding it to the module graph with {@code --add-modules}.
 * For details on that, see
 * <a href="https://junit-pioneer.org/docs/json-argument-source" target="_top">the documentation on <code>JSON tests</code></a>
 * </p>
 *
 * <p>Check out the following types for details on providing values for parameterized tests:
 * <ul>
 *     <li>{@link org.junitpioneer.jupiter.json.JsonSource}</li>
 *     <li>{@link org.junitpioneer.jupiter.json.JsonClasspathSource}</li>
 *     <li>{@link org.junitpioneer.jupiter.json.JsonFileSource}</li>
 * </ul>
 */

package org.junitpioneer.jupiter.json;
