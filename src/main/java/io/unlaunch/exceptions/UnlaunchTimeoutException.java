package io.unlaunch.exceptions;

/**
 * An exception indicating that timeout has occurred.
 *
 * @author umermansoor
 */
public class UnlaunchTimeoutException extends UnlaunchRuntimeException {
    public UnlaunchTimeoutException() {
        super();
    }

    public UnlaunchTimeoutException(String message) {
        super(message);
    }

    public UnlaunchTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
