/**
 * JUnit Pioneer provides extensions for <a href="https://github.com/junit-team/junit5/">JUnit 5</a>
 * and its Jupiter API.
 *
 * <p>Pioneer does not limit itself to proven ideas with wide application but is purposely open to
 * experimentation. It aims to spin off successful and cohesive portions into sibling projects or back
 * into the JUnit 5 code base.</p>
 *
 * <p>The dependencies on Jupiter modules could be marked as <code>transitive</code> but that would
 * allow users who depend on this module to not `require` org.junit.*, which would be backwards.</p>
 */
module org.junitpioneer {
	// see Javadoc for why these aren't transitive
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	requires org.junit.platform.launcher;

	requires static com.fasterxml.jackson.core;
	requires static com.fasterxml.jackson.databind;

	exports org.junitpioneer.vintage;
	exports org.junitpioneer.jupiter;
	exports org.junitpioneer.jupiter.cartesian;
	exports org.junitpioneer.jupiter.params;
	exports org.junitpioneer.jupiter.json;
	exports org.junitpioneer.jupiter.converter;

	opens org.junitpioneer.vintage to org.junit.platform.commons;
	opens org.junitpioneer.jupiter to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.cartesian to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.issue to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.params to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.resource to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.json to org.junit.platform.commons, com.fasterxml.jackson.databind;
	opens org.junitpioneer.jupiter.converter to org.junit.platform.commons;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener;
	uses org.junitpioneer.jupiter.IssueProcessor;
}
