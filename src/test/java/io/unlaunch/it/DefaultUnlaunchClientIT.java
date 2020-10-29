package io.unlaunch.it;

import io.unlaunch.UnlaunchAttribute;
import io.unlaunch.UnlaunchClient;
import io.unlaunch.UnlaunchDynamicConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.*;

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
    UnlaunchClient client;
    @Before
    public void init() {
        client = UnlaunchClient.builder().sdkKey("test-sdk-ff367fd3-accc-43e2-88d4-24edda0206c3").build();

         attr1 = UnlaunchAttribute.newString("account_type", "prepaid");
         attr2 = UnlaunchAttribute.newNumber("max_loan", 500);
         attr3 = UnlaunchAttribute.newNumber("min_loan", 50);

        attr2_1 = UnlaunchAttribute.newString("account_type", "postpaid");
        attr2_2 = UnlaunchAttribute.newNumber("max_loan", 500);
        attr2_3 = UnlaunchAttribute.newNumber("min_loan", 50);

        attr3_1 = UnlaunchAttribute.newString("account_type", "prepaid");
        attr3_2 = UnlaunchAttribute.newNumber("max_loan", 400);
        attr3_3 = UnlaunchAttribute.newNumber("min_loan", 50);

        Assert.assertFalse(client.isReady());
        try {
            client.awaitUntilReady(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            System.out.println(("Client was not ready"));
            Assert.fail("client was not ready");
        }

        Assert.assertTrue(client.isReady());
    }

    @Test
    public void testBoolEvaluate1() {

        String varKey = client.getVariation("bolsas", userId, attr1, attr2, attr3);
        Assert.assertEquals("on", varKey);

        varKey = client.getVariation("bolsas", user3Id, attr3_1, attr3_2, attr3_3 );
        Assert.assertEquals("on", varKey);
    }

    @Test
    public void testBoolEvaluate2()  {
        String varKey = client.getVariation("bolsas", user2Id, attr2_1, attr2_2, attr2_3);
        Assert.assertEquals("off", varKey);
    }

    @Test
    public void testEvaluate1() {
        String var =  client.getVariation("presta-facil", userId, attr1, attr2, attr3);
        Assert.assertEquals("on", var);
    }

    @Test
    public void testEvaluate2() {
        String var =  client.getVariation("presta-facil", user2Id, attr2_1, attr2_2,
                attr2_3);
        Assert.assertEquals("off", var);
    }

    @Test
    public void testEvaluate3()  {
        String var = client.getVariation("presta-facil", user3Id, attr3_1, attr3_2, attr3_3);
        Assert.assertEquals("on", var);
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
        UnlaunchDynamicConfig c1 = client.getFeature( "presta-facil", user3Id).getVariationConfig();
        Assert.assertEquals("bold", c1.getString("text_format"));

        UnlaunchDynamicConfig c2 = client.getFeature("bolsas", user3Id, attr3_1, attr3_2,
                attr3_3).getVariationConfig();
        Assert.assertEquals("green", c2.getString("header_color"));
    }

    @After
    public void close() throws Exception {
        client.shutdown();
    }
}
