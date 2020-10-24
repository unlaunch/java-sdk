package io.unlaunch.it;

import io.unlaunch.UnlaunchAttribute;
import io.unlaunch.UnlaunchClient;
import io.unlaunch.UnlaunchConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests.
 *
 * @author jawad
 */
public class DefaultUnlaunchClientIT {

    String userId = "10";
    UnlaunchAttribute attr1;
    UnlaunchAttribute attr2;
    UnlaunchAttribute attr3;

    String user2Id = "70";
    UnlaunchAttribute attr2_1;
    UnlaunchAttribute attr2_2;
    UnlaunchAttribute attr2_3;

    String user3Id = "90";
    UnlaunchAttribute attr3_1;
    UnlaunchAttribute attr3_2;
    UnlaunchAttribute attr3_3;

    // Test environment
    static UnlaunchClient client = UnlaunchClient.builder().sdkKey("sdk-8cd7482a-fcd7-470f-81f0-9caa2b795a95").build();
    
    @Before
    public void init() {
         attr1 = UnlaunchAttribute.newString("account_type", "prepaid");
         attr2 = UnlaunchAttribute.newNumber("max_loan", 500);
         attr3 = UnlaunchAttribute.newNumber("min_loan", 50);

        attr2_1 = UnlaunchAttribute.newString("account_type", "postpaid");
        attr2_2 = UnlaunchAttribute.newNumber("max_loan", 500);
        attr2_3 = UnlaunchAttribute.newNumber("min_loan", 50);

        attr3_1 = UnlaunchAttribute.newString("account_type", "prepaid");
        attr3_2 = UnlaunchAttribute.newNumber("max_loan", 400);
        attr3_3 = UnlaunchAttribute.newNumber("min_loan", 50);

        try {
            client.awaitUntilReady(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {

        }
    }

    @Test
    public void testBoolEvaluate1() {

        boolean varKey = Boolean.valueOf(client.getVariation("bolsas", userId, attr1, attr2, attr3));
        Assert.assertEquals(true, varKey);

        varKey = Boolean.valueOf(client.getVariation("bolsas", user3Id, attr3_1, attr3_2, attr3_3 ));
        Assert.assertEquals(true, varKey);
    }

    @Test
    public void testBoolEvaluate2()  {
        boolean varKey = Boolean.valueOf(client.getVariation("bolsas", user2Id, attr2_1, attr2_2, attr2_3));
        Assert.assertEquals(false, varKey);
    }

    @Test
    public void testEvaluate1() {
        Boolean r =  Boolean.valueOf(client.getVariation("presta-facil", userId, attr1, attr2, attr3));
        Assert.assertEquals(true, r);
    }

    @Test
    public void testEvaluate2() {
        Boolean b =  Boolean.valueOf(client.getVariation("presta-facil", user2Id, attr2_1, attr2_2,
                attr2_3));
        Assert.assertEquals(false, b);
    }

    @Test
    public void testEvaluate3()  {
        Boolean b = Boolean.valueOf(client.getVariation("presta-facil", user3Id, attr3_1, attr3_2, attr3_3));
        Assert.assertEquals(true, b);
    }

    @Test
    public void testVariationPropsAsMap()  {
        Map<String, String> props1  = client.getFeature( "presta-facil", user3Id).getVariationConfigAsMap();
        Assert.assertEquals("bold", props1.get("text_format"));

        Map<String, String> props2 =
                client.getFeature("bolsas", user3Id, attr3_1, attr3_2, attr3_3).getVariationConfigAsMap();
        Assert.assertEquals("green", props2.get("header_color"));
    }

    @Test
    public void testVariationPropsAsConfig()  {
        UnlaunchConfig c1 = client.getFeature( "presta-facil", user3Id).getVariationConfig();
        Assert.assertEquals("bold", c1.getString("text_format"));

        UnlaunchConfig c2 = client.getFeature("bolsas", user3Id, attr3_1, attr3_2,
                attr3_3).getVariationConfig();
        Assert.assertEquals("green", c2.getString("header_color"));
    }

    @AfterClass
    public static void close() throws Exception {
        client.shutdown();
    }
}
