/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.unlaunch.engine;

import java.util.Collections;
import java.util.List;

/**
 * A class representing Rule.
 *
 * @author jawad
 * @author umer
 */
class Rule {

    private final boolean isDefault;
    private final long priority;
    private final List<Condition> conditions;
    private final List<Variation> variations;

    /**
     *
     * @param isDefault
     * @param priority
     * @param conditions
     * @param variations
     */
    public Rule(boolean isDefault, long priority, List<Condition> conditions, List<Variation> variations) {
        this.isDefault = isDefault;
        this.priority = priority;
        this.conditions = conditions;
        this.variations = variations;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public long getPriority() {
        return priority;
    }

    public List<Condition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public List<Variation> getVariations() {
        return Collections.unmodifiableList(variations);
    }

     /**
     * Matches Unlaunch user attribute values with conditions defined in this object.
     * Returns true if all the conditions defined in the rule are satisfied.
     * Returns false if any of the condition fails to satisfy.
     * @param user
     * @return
     */
    public boolean matches(UnlaunchUser user){
    
        for (Condition condition : conditions) {
           if(!condition.match(user)){
               return false;
           }
        }
        
        return true;
    }
    
}
