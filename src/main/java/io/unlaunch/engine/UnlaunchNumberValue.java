package io.unlaunch.engine;

/**
 * This class is a wrapper class for Java Integer type.  
 *
 * @author jawad
 */
final class UnlaunchNumberValue implements UnlaunchValue<Number> {

    private final Number number;

    public UnlaunchNumberValue(Number number) {
        this.number = number;
    }
    
    @Override
    public Number get() {
        return number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
    
    
}
