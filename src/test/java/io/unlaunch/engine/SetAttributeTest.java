package io.unlaunch.engine;

import io.unlaunch.UnlaunchFeature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SetAttributeTest {

    final String flagKey = "flag";
    final String attribute = "attribute";
    final String varKeyON = "ON";
    final String varKeyOFF = "OFF";

    FeatureFlag flag = Mockito.mock(FeatureFlag.class);
    List<Variation> variations = new ArrayList<>();
    UnlaunchUser user = Mockito.mock(UnlaunchUser.class);

    @Before
    public void setUp() {
        when(user.getId()).thenReturn("user123");

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

    // All Of
    @Test
    public void All_Of_EqualSets_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user1");
        userSet.add("user2");

        setCondition(Operator.HAS_ALL_OF, userSet, "user2,user1");

        OnVariation();
    }

    @Test
    public void All_Of_UserSetIsSuperset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user3");
        userSet.add("user1");
        userSet.add("user2");

        setCondition(Operator.HAS_ALL_OF, userSet, "user2,user1");

        OnVariation();
    }

    @Test
    public void All_Of_WhenUserSetIsSubset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user2");

        setCondition(Operator.HAS_ALL_OF, userSet, "user2,user1");

        OffVariation();
    }

    @Test
    public void All_Of_WhenUserSetIsEmpty_ShouldNotMatch() {
        Set<String> userSet = new HashSet();

        setCondition(Operator.HAS_ALL_OF, userSet, "user2,user1");

        OffVariation();
    }

    // Not All Of
    @Test
    public void Not_All_Of_EqualSets_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user1");
        userSet.add("user2");

        setCondition(Operator.DOES_NOT_HAVE_ALL_OF, userSet, "user2,user1");

        OffVariation();
    }

    @Test
    public void Not_All_Of_UserSetIsSuperset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user3");
        userSet.add("user1");
        userSet.add("user2");

        setCondition(Operator.DOES_NOT_HAVE_ALL_OF, userSet, "user2,user1");

        OffVariation();
    }

    @Test
    public void Not_All_Of_WhenUserSetIsSubset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("user2");

        setCondition(Operator.DOES_NOT_HAVE_ALL_OF, userSet, "user2,user1");

        OnVariation();
    }

    @Test
    public void Not_All_Of_WhenUserSetIsEmpty_ShouldMatch() {
        Set<String> userSet = new HashSet();
        setCondition(Operator.DOES_NOT_HAVE_ALL_OF, userSet, "user2,user1");

        OnVariation();
    }

    // Any Of
    @Test
    public void Any_Of_EqualSets_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.HAS_ANY_OF, userSet, "1,2");

        OnVariation();
    }

    @Test
    public void Any_Of_UserSetIsSuperset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("3");
        userSet.add("2");

        setCondition(Operator.HAS_ANY_OF, userSet, "1,2");

        OnVariation();
    }

    @Test
    public void Any_Of_UserSetIsSubSet_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("2");

        setCondition(Operator.HAS_ANY_OF, userSet, "1,2");

        OnVariation();
    }

    @Test
    public void Any_Of_UserSetIsSubSetUnordered_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");
        userSet.add("3");
        userSet.add("4");

        setCondition(Operator.HAS_ANY_OF, userSet, "3,2,1,10");

        OnVariation();
    }

    @Test
    public void Any_Of_UserSetIsDisjoint_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("4");
        userSet.add("5");

        setCondition(Operator.HAS_ANY_OF, userSet, "1,2,3");

        OffVariation();
    }

    @Test
    public void Any_Of_UserSetIsEmpty_ShouldNotMatch() {
        Set<String> userSet = new HashSet();

        setCondition(Operator.HAS_ANY_OF, userSet, "1");

        OffVariation();
    }

    // Not Any Of
    @Test
    public void Not_Any_Of_EqualSets_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "1,2");

        OffVariation();
    }

    @Test
    public void Not_Any_Of_UserSetIsSuperset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("3");
        userSet.add("2");

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "1,2");

        OffVariation();
    }

    @Test
    public void Not_Any_Of_UserSetIsSubSet_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("2");

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "1,2");

        OffVariation();
    }

    @Test
    public void Not_Any_Of_UserSetIsSubSetUnordered_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");
        userSet.add("3");
        userSet.add("4");

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "3,2,1,10");

        OffVariation();
    }

    @Test
    public void Not_Any_Of_UserSetIsDisjoint_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("4");
        userSet.add("5");

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "1,2,3");

        OnVariation();
    }

    @Test
    public void Not_Any_Of_UserSetIsEmpty_ShouldMatch() {
        Set<String> userSet = new HashSet();

        setCondition(Operator.DOES_NOT_HAVE_ANY_OF, userSet, "1");

        OnVariation();
    }

    // Part Of
    @Test
    public void Part_Of_WhenEqualSets_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.IS_PART_OF, userSet, "2,1");

        OnVariation();
    }

    @Test
    public void Part_Of_WhenUserSetIsSuperset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");
        userSet.add("3");

        setCondition(Operator.IS_PART_OF, userSet, "2,1");

        OffVariation();
    }

    @Test
    public void Part_Of_WhenUserSetIsSubset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.IS_PART_OF, userSet, "2,1,3");

        OnVariation();
    }

    // Not Part Of
    @Test
    public void Not_Part_Of_WhenEqualSets_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.IS_NOT_PART_OF, userSet, "2,1");

        OffVariation();
    }

    @Test
    public void Not_Part_Of_WhenUserSetIsSuperset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");
        userSet.add("3");

        setCondition(Operator.IS_NOT_PART_OF, userSet, "2,1");

        OnVariation();
    }

    @Test
    public void Not_Part_Of_WhenUserSetIsSubset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.IS_NOT_PART_OF, userSet, "2,1,3");

        OffVariation();
    }

    // Equals
    @Test
    public void Equals_EqualSets_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.EQUALS, userSet, "2,1");

        OnVariation();
    }

    @Test
    public void Equals_UserSetIsSubset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.EQUALS, userSet, "1,3,2");

        OffVariation();
    }

    @Test
    public void Equals_UserSetIsEmpty_ShouldNotMatch() {
        Set<String> userSet = new HashSet();

        setCondition(Operator.EQUALS, userSet, "1");

        OffVariation();
    }

    @Test
    public void Equals_UserSetIsSuperset_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.EQUALS, userSet, "1");

        OffVariation();
    }

    // Not Equals
    @Test
    public void Not_Equals_EqualSets_ShouldNotMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.NOT_EQUALS, userSet, "2,1");

        OffVariation();
    }

    @Test
    public void Not_Equals_UserSetIsSubset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.NOT_EQUALS, userSet, "1,3,2");

        OnVariation();
    }

    @Test
    public void Not_Equals_UserSetIsEmpty_ShouldMatch() {
        Set<String> userSet = new HashSet();

        setCondition(Operator.NOT_EQUALS, userSet, "1");

        OnVariation();
    }

    @Test
    public void Not_Equals_UserSetIsSuperset_ShouldMatch() {
        Set<String> userSet = new HashSet();
        userSet.add("1");
        userSet.add("2");

        setCondition(Operator.NOT_EQUALS, userSet, "1");

        OnVariation();
    }

    private void OnVariation() {
        Evaluator instance = new Evaluator();
        UnlaunchFeature feature = instance.evaluate(flag, user);

        assertEquals(feature.getVariation(), varKeyON);
    }

    private void OffVariation() {
        Evaluator instance = new Evaluator();
        UnlaunchFeature feature = instance.evaluate(flag, user);

        assertEquals(feature.getVariation(), varKeyOFF);
    }

    private void setCondition(Operator operator, Set<String> userSet, String value) {
        UnlaunchSetValue setValue = Mockito.mock(UnlaunchSetValue.class);
        when(setValue.get()).thenReturn(userSet);

        Map<String, UnlaunchValue> map = new HashMap();
        map.put(attribute, setValue);
        when(user.getAllAttributes()).thenReturn(map);

        Condition condition = new Condition(attribute, operator, AttributeType.SET, value);
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);

        Rule rule = new Rule(false, 1, conditions, variations);

        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        when(flag.getRules()).thenReturn(rules);
    }
}
