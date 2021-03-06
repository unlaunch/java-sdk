package io.unlaunch.engine;

import java.util.List;

/**
 * This class represents a Condtion which is used to define a complete Rule.  
 * A condition is defined by attribute joins with Operator to take on list of values. 
 * @author jawad
 */
final class Condition {
    
    private String attribute;
    private Operator operator;
    private AttributeType type;
    private String value;

    /**
     *
     * @param attribute
     * @param operator
     * @param type
     * @param value
     */
    public Condition(String attribute, Operator operator, AttributeType type, String value) {
        this.attribute = attribute;
        this.operator = operator;
        this.type = type;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Matches Unlaunch user attribute value with values provided in this object.
     * Match is performed on the basis of operator defined for this object.
     * @param user
     * @return
     */
    public boolean match(UnlaunchUser user){
    
        if(user.getAllAttributes().containsKey(attribute)){
            return operator.apply(value, user.getAllAttributes().get(attribute), type);
        }
        
        return false;
    }
    
    
    
}
