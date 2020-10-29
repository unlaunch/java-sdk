package io.unlaunch;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author umermansoor
 */
public class DefaultUnlaunchDynamicConfigTest {

    @Test
    public void testDefaultValueWhenUnderlyingMapIsNull() {
        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(null);
        Assert.assertEquals("1", config.getString("doesnt_exist", "1"));
    }

    @Test
    public void testDefaultValueWhenKeyDoesntExist() {
        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(new HashMap<>());
        Assert.assertEquals("1", config.getString("doesnt_exist", "1"));
    }

    @Test
    public void testDefaultIntValueWhenKeyExistsButNotANumber() {
        HashMap<String, String> map = new HashMap<>();
        map.put("value_is_not_a_number", "abc");

        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(map);
        Assert.assertEquals(1, config.getInt("value_is_not_a_number", 1));
    }

    @Test
    public void testDefaultFloatValueWhenKeyExistsButNotANumber() {
        HashMap<String, String> map = new HashMap<>();
        map.put("value_is_not_a_number", "abc");

        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(map);
        Assert.assertEquals(1.123f, config.getFloat("value_is_not_a_number", 1.123f), 0.0f);
    }

    @Test
    public void testDefaultDoubleValueWhenKeyExistsButNotANumber() {
        HashMap<String, String> map = new HashMap<>();
        map.put("value_is_not_a_number", "abc");

        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(map);
        Assert.assertEquals(1.123d, config.getDouble("value_is_not_a_number", 1.123d), 0.0d);
    }

    @Test
    public void testDefaultDoubleValueWhenKeyDoesntExist() {
        HashMap<String, String> map = new HashMap<>();

        DefaultUnlaunchDynamicConfig config = new DefaultUnlaunchDynamicConfig(map);
        Assert.assertEquals(1.123d, config.getDouble("value_is_not_a_number", 1.123d), 0.0d);
    }
}
