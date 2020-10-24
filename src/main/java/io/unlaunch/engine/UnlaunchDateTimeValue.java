package io.unlaunch.engine;

import java.time.LocalDateTime;

/**
 * This class is a wrapper class for Java LocalDateTime type.  
 *
 * @author jawad
 */
final class UnlaunchDateTimeValue implements UnlaunchValue<LocalDateTime> {

    private final LocalDateTime dateTime;

    public UnlaunchDateTimeValue(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
            
    @Override
    public LocalDateTime get() {
        return dateTime; 
    }
    

    @Override
    public String toString() {
        return dateTime.toString();
    }
    
    
}
