package io.unlaunch.engine;

/**
 * This class is a wrapper class for Java String type.  
 *
 * @author jawad
 */
final class UnlaunchStringValue implements UnlaunchValue<String> {

    private final String string;

    public UnlaunchStringValue(String string) {
        this.string = string;
    }
            
    @Override
    public String get() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }
    
    
}
