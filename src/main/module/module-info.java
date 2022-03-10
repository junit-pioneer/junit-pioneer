module org.junitpioneer {
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
