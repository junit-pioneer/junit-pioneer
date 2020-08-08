module org.junitpioneer {
	// could be `transitive`, but that's its own discussion
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;

	exports org.junitpioneer.vintage;
	exports org.junitpioneer.jupiter;
	exports org.junitpioneer.jupiter.params;
}
