module org.junitpioneer {
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	requires org.junit.platform.commons;
	requires org.junit.platform.launcher;

	requires static com.fasterxml.jackson.core;
	requires static com.fasterxml.jackson.databind;

	exports org.junitpioneer.vintage;
	exports org.junitpioneer.jupiter;
	exports org.junitpioneer.jupiter.params;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener;
	uses org.junitpioneer.jupiter.IssueProcessor;
}
