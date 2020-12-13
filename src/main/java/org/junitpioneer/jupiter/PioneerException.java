package org.junitpioneer.jupiter;

public class PioneerException extends RuntimeException {

	public PioneerException(String s, ReflectiveOperationException ex) {
		super(s, ex);
	}
}
