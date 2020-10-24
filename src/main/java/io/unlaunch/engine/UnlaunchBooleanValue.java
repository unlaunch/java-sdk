package io.unlaunch.engine;

/**
 * This class is a wrapper class for Java Boolean type.  
 *
 * @author jawad
 */
final class UnlaunchBooleanValue implements UnlaunchValue<Boolean> {

    private final Boolean bool;

    public UnlaunchBooleanValue(Boolean bool) {
        this.bool = bool;
    } 
    
    @Override
    public Boolean get() {
        return bool;
    }

    @Override
    public String toString() {
        return bool.toString();
    }
    
    
    
}
