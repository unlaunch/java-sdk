/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.unlaunch.engine;

import io.unlaunch.UnlaunchFeature;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ghauri
 */
public class AttributeTypeTest {
    
    private static final Logger logger = LoggerFactory.getLogger(AttributeTypeTest.class);
    
    final String flagKey = "flag";
    final String varKeyON = "ON";
    final String varKeyOFF = "OFF";
    
    Evaluator evaluator = Mockito.mock(Evaluator.class);
    FeatureFlag flag = Mockito.mock(FeatureFlag.class);
    List<Variation> variations = new ArrayList<>();
    
    @Before
    public void setUp() {
        evaluator = Mockito.mock(Evaluator.class);
        
        flag = Mockito.mock(FeatureFlag.class);
        when(flag.isEnabled()).thenReturn(true);
        when(flag.getKey()).thenReturn(flagKey);
        
        Variation variationON = Mockito.mock(Variation.class);
        Variation variationOFF = Mockito.mock(Variation.class);
        Variation defaultVariation = Mockito.mock(Variation.class);
        
        when(variationON.getKey()).thenReturn(varKeyON);
        when(variationON.getRolloutPercentage()).thenReturn(100L);
        
        when(variationOFF.getKey()).thenReturn(varKeyOFF);
        when(variationOFF.getRolloutPercentage()).thenReturn(100L);
        
        when(defaultVariation.getKey()).thenReturn(varKeyOFF);
        when(defaultVariation.getRolloutPercentage()).thenReturn(100L);
        
        variations.add(variationON);
        variations.add(variationOFF);
        
        List<Variation> defaultRuleVariations = new ArrayList<>();
        defaultRuleVariations.add(defaultVariation);
        
        Rule defaultRule = Mockito.mock(Rule.class);
        
        when(defaultRule.isIsDefault()).thenReturn(Boolean.TRUE);
        when(defaultRule.getVariations()).thenReturn(defaultRuleVariations);
        when(flag.getDefaultRule()).thenReturn(defaultRule);
        when(flag.getVariations()).thenReturn(variations);
        when(flag.getOffVariation()).thenReturn(variationOFF);
        
    }
    
    @Test
    public void testStringAttributeTypeOperatorEquals() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("en-US");
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        when(user.getAllAttributes()).thenReturn(map);
        
        Condition condition = new Condition("locale", Operator.EQUALS, AttributeType.STRING, "en-US");
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testStringAttributeTypeOperatorNOTEquals() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_NOT_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("no-no");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.NOT_EQUALS, AttributeType.STRING, "en-US");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testStringAttributeTypeOperatorStartsWith() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_STARTS_WITH");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("en-US");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.STARTS_WITH, AttributeType.STRING, "en");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testStringAttributeTypeOperatorEndsWith() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_ENDS_WITH");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("en-US");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.ENDS_WITH, AttributeType.STRING, "US");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testStringAttributeTypeOperatorDoesNotStartsWith() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_DOES_NOT_STARTS_WITH");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("no");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.NOT_STARTS_WITH, AttributeType.STRING, "en-US");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testStringAttributeTypeOperatorDoesNotEndsWith() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_DOES_NOT_ENDS_WITH");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("No");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.NOT_ENDS_WITH, AttributeType.STRING, "en-US");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }

    @Test
    public void testStringAttributeTypeOperatorContains() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_CONTAINS");

        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn("user123");

        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("en-US");

        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.CONTAINS, AttributeType.STRING, "n-U");

        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);

        Rule rule = new Rule(false, 1, conditions, variations);

        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);

        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");

        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);

        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }

    @Test
    public void testStringAttributeTypeOperatorNOTContains() {
        logger.debug("STRING_ATTRIBUTE_TYPE_OPERATOR_NOT_CONTAINS");

        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn("user123");

        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.toString()).thenReturn("en-US");

        Map<String, UnlaunchValue> map = new HashMap();
        map.put("locale", stringValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("locale", Operator.NOT_CONTAINS, AttributeType.STRING, "n-1");

        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);

        Rule rule = new Rule(false, 1, conditions, variations);

        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);

        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");

        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);

        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    // STRING ATTRIBUTE TEST END!

    // START NUMBER ATTRIBUTE TEST!
    @Test
    public void testNumberAttributeTypeOperatorEquals() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("123");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.EQUALS, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testNumberAttributeTypeOperatorNotEquals() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_NOT_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("312");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.NOT_EQUALS, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testNumberAttributeTypeOperatorGreaterThan() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("233");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.GREATER_THAN, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testNumberAttributeTypeOperatorGreaterThanOrEquals() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("2983");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.GREATER_THAN_OR_EQUALS, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testNumberAttributeTypeOperatorLessThan() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("32");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.LESS_THAN, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testNumberAttributeTypeOperatorLessThanOrEquals() {
        logger.debug("TEST_NUMBER_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchNumberValue numberValue = Mockito.mock(UnlaunchNumberValue.class);
        when(numberValue.toString()).thenReturn("23");
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("epoch", numberValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("epoch", Operator.LESS_THAN_OR_EQUALS, AttributeType.NUMBER, "123");
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
//    NUMBER ATTRIBUTE TYPE TEST END
//    START DATE ATTRIBUTE TEST

    @Test
    public void testDateAttributeTypeOperatorEquals() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneId.of("UTC")));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateAttributeTypeOperatorNotEquals() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_NOT_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneId.of("UTC")).minusDays(1));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.NOT_EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateAttributeTypeOperatorGreaterThan() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.GREATER_THAN, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateAttributeTypeOperatorLessThan() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now().minusWeeks(1));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.LESS_THAN, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateAttributeTypeOperatorGreaterThanOrEquals() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneOffset.UTC));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.GREATER_THAN_OR_EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateAttributeTypeOperatorLessThanOrEquals() {
        logger.debug("TEST_DATE_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now().minusDays(2));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.LESS_THAN_OR_EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
//    DATE ATTRIBUTE TYPE TEST END
//    START DATE_TIME ATTRIBUTE TYPE

    @Test
    public void testDateTimeAttributeTypeOperatorLessThan() {
        logger.debug("TEST_DATE_TIME_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now().minusDays(2));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.LESS_THAN, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateTimeAttributeTypeOperatorLessThanOrEquals() {
        logger.debug("TEST_DATE_TIME_ATTRIBUTE_TYPE_OPERATOR_LESS_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneId.of("UTC")));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.LESS_THAN_OR_EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateTimeAttributeTypeOperatorGreaterThanOrEquals() {
        logger.debug("TEST_DATE_TIME_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN_OR_EQUALS");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now(ZoneOffset.UTC));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.GREATER_THAN_OR_EQUALS, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
    
    @Test
    public void testDateTimeAttributeTypeOperatorGreaterThan() {
        logger.debug("TEST_DATE_TIME_ATTRIBUTE_TYPE_OPERATOR_GREATER_THAN");
        
        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);
        
        UnlaunchDateTimeValue dateValue = Mockito.mock(UnlaunchDateTimeValue.class);
        when(dateValue.get()).thenReturn(LocalDateTime.now().plusDays(2));
        
        Map<String, UnlaunchValue> map = new HashMap();
        map.put("start_date", dateValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition("start_date", Operator.GREATER_THAN, AttributeType.DATE, String.valueOf(new Date().getTime()));
        
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        
        Rule rule = new Rule(false, 1, conditions, variations);
        
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
        
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");
        
        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);
        
        assertEquals(unlaunchFeature.getVariation(), result.getVariation());
    }
}
