package io.unlaunch.engine;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author jawad
 */
public final class JsonObjectConversionHelper {

    private static final Logger logger = LoggerFactory.getLogger(JsonObjectConversionHelper.class);
    
    /**
     * Transform flags in JSONArray object to List of feature flags.
     * @param flags array of flags
     * @return list of {@link FeatureFlag}
     */
    public List<FeatureFlag> toUnlaunchFlags(JSONArray flags) {

        List<FeatureFlag> unlaunchFlags = new ArrayList<>();
        flags.iterator().forEachRemaining(jsonObj -> {
            try {
                FeatureFlag unlaunchFlag = toFlag((JSONObject) jsonObj);
                unlaunchFlags.add(unlaunchFlag);
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(JsonObjectConversionHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return unlaunchFlags;
    }

    private FeatureFlag toFlag(JSONObject json) throws ParseException {

        List<Rule> rules = new ArrayList<>();

        String flagKey = (String) json.get("key");
        String flagName = (String) json.get("name");
        String type = (String) json.get("type");
        boolean enabled = ((String)json.get("state")).equals("ACTIVE");

        JSONArray varArray = (JSONArray)json.get("variations");
        List<Variation> variations = toVariations(varArray);
        
        JSONArray rulesArray = (JSONArray) json.get("rules");
        rulesArray.iterator().forEachRemaining(jsonObj -> rules.add(toRule((JSONObject) jsonObj, varArray)));
        
        rules.sort(Comparator.comparingLong(Rule::getPriority));
        
        Rule defaultRule = rules.stream().filter(rule -> rule.isIsDefault()).findFirst().get();
            
        JSONObject offVariationJson = (JSONObject) varArray.stream().filter(varObj -> ((JSONObject) varObj).get("id").equals((Long)json.get("offVariation"))).findFirst().get();
                
        Variation offVariation = toVariation((JSONObject)offVariationJson);
        
        Map<String, String> prerequistesFlagMap = (Map<String, String>) json.get("prerequisiteFlags");
        Map<FeatureFlag, Variation> prerequistesFlags = null;
        if (prerequistesFlagMap != null && !prerequistesFlagMap.isEmpty()) {
            prerequistesFlags = toPrerequistesFlagMap(prerequistesFlagMap);
        }
        FeatureFlag unlaunchFlag = new FeatureFlag(flagKey, flagName, variations, prerequistesFlags, rules, enabled, offVariation, defaultRule, null, type);

        return unlaunchFlag;
    }

    private Map<FeatureFlag, Variation> toPrerequistesFlagMap(Map<String, String> map) throws ParseException {
        
        Map<FeatureFlag, Variation> prerequisteFlags = new HashMap<>();
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String flagString = entry.getKey();
            String varString = entry.getValue();
            
            Object jsonObject = new JSONParser().parse(flagString);
            Object varJsonObject = new JSONParser().parse(varString);
	 
            FeatureFlag featureFlag = toFlag((JSONObject) jsonObject);
            Variation variation = toVariation((JSONObject) varJsonObject);
            
            prerequisteFlags.put(featureFlag, variation);
            
        }
        return prerequisteFlags;
    }
    
    private Variation toVariation(JSONObject json) {

        Variation unlaunchVariation = new Variation();

        unlaunchVariation.setKey((String) json.get("key"));
        unlaunchVariation.setName((String) json.get("name"));
        unlaunchVariation.setProperties((Map<String,String>)json.get("configs"));
        unlaunchVariation.setAllowList((String) json.get("allowList"));
        return unlaunchVariation;
    }
    
    private List<Variation> toVariations(JSONArray variationsArray) {

        List<Variation> variations = new ArrayList<>();
        variationsArray.iterator().forEachRemaining(jsonObj -> {
            Variation variation = toVariation((JSONObject) jsonObj);
            variations.add(variation);
        });

        return variations;
    }
//    private List<Rule> toRules(JSONArray rules){
//
//        List<Rule> orcaRules = new ArrayList<>();
//        rules.iterator().forEachRemaining(jsonObj -> {
//            Rule rule = toRule((JSONObject) jsonObj);
//            orcaRules.add(rule);
//        });
//
//        return orcaRules;
//    }
    
    private Rule toRule(JSONObject rule, JSONArray jsonVariations) {
        
        List<Condition> conditions = toConditions((JSONArray) rule.get("conditions"));
        
        List<Variation> splitVariations = toVariations(jsonVariations, (JSONArray) rule.get("splits"));
        
        Rule unlaunchRule = new Rule((boolean)rule.get("isDefault"), (long)rule.get("priority"), conditions, splitVariations);

        return unlaunchRule;
    }

    private List<Condition> toConditions(JSONArray jsonArray){
    
        List<Condition> conditions = new ArrayList<>();
        jsonArray.iterator().forEachRemaining(jsonObj -> {
            Condition condition = toCondition((JSONObject) jsonObj);
            conditions.add(condition);
        });
        
        return conditions;
    }
    
    
    private Condition toCondition(JSONObject json){
    
        Condition condition = new Condition((String)json.get("attribute"), 
                                Operator.findByKey((String)json.get("op")),
                                AttributeType.getByName((String)json.get("type")),
                                (String)json.get("value"));
        
        
        return condition;
    }
    
    private List<Variation> toVariations(JSONArray variationsArray, JSONArray splitsArray) {

        List<Variation> variations = new ArrayList<>();

        splitsArray.iterator().forEachRemaining(jsonObj -> {

            JSONObject splitObj = (JSONObject) jsonObj;
            long variationId = (long) splitObj.get("variationId");
            long rolloutPercentage = (long) splitObj.get("rolloutPercentage");

            JSONObject variationObj = (JSONObject) variationsArray.stream().filter(varObj -> ((JSONObject) varObj).get("id").equals(variationId)).findFirst().get();
            Variation variation = toVariation((JSONObject) variationObj);
            variation.setRolloutPercentage(rolloutPercentage);
            variations.add(variation);

        });
        variations.sort(Comparator.comparing(Variation::getKey));
        return variations;
    }

}
