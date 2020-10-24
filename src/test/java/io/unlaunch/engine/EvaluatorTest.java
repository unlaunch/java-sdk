/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.unlaunch.engine;

import io.unlaunch.UnlaunchFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class EvaluatorTest {

    private static final Logger logger = LoggerFactory.getLogger(EvaluatorTest.class);
    
    final String flagKey = "flag123";
    final String varKeyON = "ON";
    final String varKeyOFF = "OFF";
    final String varKeyDefaultRule = "defaultRule";
    
    Evaluator evaluator = Mockito.mock(Evaluator.class);
    FeatureFlag flag = Mockito.mock(FeatureFlag.class);
    UnlaunchUser percentRolloutUser = Mockito.mock(UnlaunchUser.class);

    @Before
    public void callBeforeEachTest() {
        evaluator = Mockito.mock(Evaluator.class);

        flag = Mockito.mock(FeatureFlag.class);
        when(flag.isEnabled()).thenReturn(true);
        when(flag.getKey()).thenReturn(flagKey);

        List<Rule> rules = new ArrayList<>();

        Rule rule = Mockito.mock(Rule.class);
        when(rule.isIsDefault()).thenReturn(Boolean.FALSE);
        
        Rule defaultRule = Mockito.mock(Rule.class);
        when(defaultRule.isIsDefault()).thenReturn(Boolean.TRUE);

        List<Condition> conditions = new ArrayList<>();
        Condition condition = Mockito.mock(Condition.class);

        when(condition.getAttribute()).thenReturn("gender");
        when(condition.getOperator()).thenReturn(Operator.EQUALS);
        when(condition.getType()).thenReturn(AttributeType.STRING);

        List<String> values = new ArrayList<>();
        values.add("male");

        when(condition.getValues()).thenReturn(values);

        conditions.add(condition);
        
        when(rule.getConditions()).thenReturn(conditions);

        Variation variationON = Mockito.mock(Variation.class);
        Variation variationOFF = Mockito.mock(Variation.class);
        Variation defaultVariation = Mockito.mock(Variation.class);

        final String allowList = "user123";
        
        when(variationON.getAllowList()).thenReturn(allowList);

        when(variationON.getKey()).thenReturn(varKeyON);
        when(variationON.getRolloutPercentage()).thenReturn(50L);
        
        when(variationOFF.getKey()).thenReturn(varKeyOFF);
        when(variationOFF.getRolloutPercentage()).thenReturn(50L);
        
        when(defaultVariation.getKey()).thenReturn(varKeyDefaultRule);
        when(defaultVariation.getRolloutPercentage()).thenReturn(100L);

        List<Variation> variations = new ArrayList<>();
        variations.add(variationON);
        variations.add(variationOFF);
        
        when(rule.getVariations()).thenReturn(variations);

        List<Variation> defaultRuleVariations = new ArrayList<>();
        defaultRuleVariations.add(defaultVariation);

        when(defaultRule.getVariations()).thenReturn(defaultRuleVariations);
        
        rules.add(rule);
        rules.add(defaultRule);

        when(flag.getRules()).thenReturn(rules);
        when(flag.getDefaultRule()).thenReturn(defaultRule);

        when(flag.getVariations()).thenReturn(variations);
        when(flag.getOffVariation()).thenReturn(variationOFF);

        percentRolloutUser = Mockito.mock(UnlaunchUser.class);
        when(rule.matches(percentRolloutUser)).thenReturn(true);

        UnlaunchStringValue stringValue = Mockito.mock(UnlaunchStringValue.class);
        when(stringValue.get()).thenReturn("male");

        Map<String, UnlaunchValue> map = new HashMap();
        map.put("gender", stringValue);

        when(percentRolloutUser.getAllAttributes()).thenReturn(map);
    }

    @Test
    public void testConstruction() {
        Evaluator eval = new Evaluator();
        assertNotNull(eval);
    }

    /**
     * Test of evaluate method, of class Evaluator.
     */
    @Test
    public void testEvaluateWhenUserIsInAllowlistForOnVariation() {
        logger.debug("EVALUATE_ALLOWLIST_USER_TREATMENT_ON");

        final String userId = "user123";
        
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userId);

        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyON, null, "");

        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);

        assertEquals(unlaunchFeature.getVariationKey(), result.getVariationKey());
    }

    @Test
    public void testEvaluateWhenDefaultRuleIsServed() {
        logger.debug("EVALUATE_DEFAULT_RULE");

        final String userIdDefaultRule = "user456";
    
        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn(userIdDefaultRule);

        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyDefaultRule, null, "");

        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);

        assertEquals(unlaunchFeature.getVariationKey(), result.getVariationKey());
    }

    @Test
    public void testEvaluateWhenPercentageRolloutIsServed() {
        logger.debug("EVALUATE_PERCENTAGE_ROLLOUT");

        Evaluator instance = new Evaluator();

        int countOFF = 0;
        for (int i = 0; i < 100; i++) {
            String userPR = String.valueOf(i);
            when(percentRolloutUser.getId()).thenReturn(userPR);

            UnlaunchFeature result = instance.evaluate(flag, percentRolloutUser);

            if (result.getVariationKey().equals(varKeyOFF)) {
                countOFF++;
            }
        }

        logger.info("count {}", countOFF);
        assertTrue(countOFF < 60 && countOFF > 40);
    }

    @Test
    public void testEvaluateWhenFlagDisabledOffVariationIsServed() {
        logger.debug("EVALUATE_FLAG_DISABLED_OFF_VARIATION_SERVED");

        when(flag.isEnabled()).thenReturn(Boolean.FALSE);
        UnlaunchFeature unlaunchFeature = UnlaunchFeature.create(flagKey, varKeyOFF, null, "");

        UnlaunchUser user = Mockito.mock(UnlaunchUser.class);
        when(user.getId()).thenReturn("40");

        Evaluator instance = new Evaluator();
        UnlaunchFeature result = instance.evaluate(flag, user);

        assertEquals(unlaunchFeature.getVariationKey(), result.getVariationKey());

    }

}
