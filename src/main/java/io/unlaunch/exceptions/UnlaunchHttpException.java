package io.unlaunch.exceptions;

/**
 * Exception class to indicate HTTP / REST failures.
 *
 * @author umermansoor
 */
public class UnlaunchHttpException extends UnlaunchRuntimeException {
    public UnlaunchHttpException() {
        super();
    }

    public UnlaunchHttpException(String message) {
        super(message);
    }

    public UnlaunchHttpException(String message, Throwable cause) {
        super(message, cause);
    }

}