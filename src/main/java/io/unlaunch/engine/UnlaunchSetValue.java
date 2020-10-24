package io.unlaunch.engine;

import java.util.Set;

/**
 * This class is a wrapper class for Java String type.  
 *
 * @author jawad
 */
final class UnlaunchSetValue implements UnlaunchValue<Set<String>> {

    private final Set<String> set;

    public UnlaunchSetValue(Set<String> set) {
        this.set = set;
    }
            
    @Override
    public Set<String> get() {
        return set;
    }

    @Override
    public String toString() {
        return String.join(", ", set); 
    }
    
    
}
