package io.unlaunch.exceptions;

/**
 * An exception indicating that a flag that was attempted to be used or accessed does not exists.
 *
 * @author umermansoor
 */
public class UnlaunchUnknownFlagException extends UnlaunchRuntimeException {
    public UnlaunchUnknownFlagException() {
        super();
    }

    public UnlaunchUnknownFlagException(String message) {
        super(message);
    }

    public UnlaunchUnknownFlagException(String message, Throwable cause) {
        super(message, cause);
    }
}
