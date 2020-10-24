package io.unlaunch.exceptions;

public class UnlaunchFlagTypeMismatchException extends UnlaunchRuntimeException {

    public UnlaunchFlagTypeMismatchException() {
        super();
    }

    public UnlaunchFlagTypeMismatchException(String message) {
        super(message);
    }

    public UnlaunchFlagTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
