package io.unlaunch.engine;

/**
 * List of all supported attribute types in Unlaunch.
 * 
 * @author jawad
 */
enum AttributeType {
    
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    DATE("date"),
    DATE_TIME("datetime"),       
    SET("set");          
            
    String name;

    AttributeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static AttributeType getByName(String name) {
        for (AttributeType object : values()) {
            if(object.getName().equals(name)){
                return object;
            }
        }
        return null;
    }
}
