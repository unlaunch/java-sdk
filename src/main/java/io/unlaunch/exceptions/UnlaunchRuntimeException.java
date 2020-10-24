package io.unlaunch.exceptions;

/**
 * Base class for all Unlaunch exceptions.
 *
 * @author umermansoor
 */
public class UnlaunchRuntimeException extends RuntimeException {
    public UnlaunchRuntimeException() {
        super();
    }

    public UnlaunchRuntimeException(Exception exception) {
        super(exception);
    }

    public UnlaunchRuntimeException(String message) {
        super(message);
    }

    public UnlaunchRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
