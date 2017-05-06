package com.github.sho5nn.promise.fortestutils;

public class ForUnitTestException extends RuntimeException {
  public static final String MESSAGE = "Exception for UnitTest.";

  public ForUnitTestException() {
    super(MESSAGE);
  }

  public ForUnitTestException(String message) {
    super(message);
  }

  public ForUnitTestException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
