/**
 * JUnit Pioneer provides extensions for <a href="https://github.com/junit-team/junit5/">JUnit 5</a>
 * and its Jupiter API.
 *
 * <p>Pioneer does not limit itself to proven ideas with wide application but is purposely open to
 * experiments. It aims to spin off successful and cohesive portions into sibling projects or back
 * into the JUnit 5 code base.
 *
 * <p>The dependencies on Jupiter modules could be marked as <code>transitive</code> but that would
 * allow users who depend on this module to not `require` org.junit.*, which would be backwards.
 */
module org.junitpioneer {
	// see Javadoc for why these aren't transitive
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	requires org.junit.platform.commons;
	requires org.junit.platform.launcher;

	exports org.junitpioneer.vintage;
	exports org.junitpioneer.jupiter;
	exports org.junitpioneer.jupiter.cartesian;
	exports org.junitpioneer.jupiter.params;

	opens org.junitpioneer.vintage to org.junit.platform.commons;
	opens org.junitpioneer.jupiter to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.cartesian to org.junit.platform.commons;
	opens org.junitpioneer.jupiter.params to org.junit.platform.commons;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener;
	uses org.junitpioneer.jupiter.IssueProcessor;
}
