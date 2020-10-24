package io.unlaunch.engine;

import io.unlaunch.engine.UnlaunchUser;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnLaunchUserTest {

    @Test
    public void testConstruction() {
        String id = "user-123";

        UnlaunchUser user = new UnlaunchUser(id);
        Assert.assertEquals(id, user.getId());


        user = UnlaunchUser.createWithAttributes(id, null);

       // Assert.assertEquals(aMap, user.getAllAttributes());
    }

    @Test
    public void testValidAttributesSetupPostConstruction_String() {
        UnlaunchUser user = new UnlaunchUser("user-123");
        user.putAttribute("1", "abc");
        Assert.assertEquals("abc", user.getAttribute("1"));
    }

    @Test
    public void testValidAttributesSetupPostConstruction_Number() {
        UnlaunchUser user = new UnlaunchUser("user-123");
        user.putAttribute("1", 123);
        Assert.assertEquals(123, user.getAttribute("1"));
    }

    @Test
    public void testValidAttributesSetupPostConstruction_Boolean() {
        UnlaunchUser user = new UnlaunchUser("user-123");
        user.putAttribute("1", true);
        Assert.assertEquals(true, user.getAttribute("1"));

        user.putAttribute("2", Boolean.TRUE);
        Assert.assertEquals(Boolean.TRUE, user.getAttribute("2"));

        user.putAttribute("3", false);
        Assert.assertEquals(false, user.getAttribute("3"));
    }

    @Test
    public void testValidAttributesSetupPostConstruction_LocalDateTime() {
        UnlaunchUser user = new UnlaunchUser("user-123");
        LocalDateTime lt = LocalDateTime.now();
        user.putAttribute("1", lt);
        Assert.assertEquals(lt, user.getAttribute("1"));
        Assert.assertEquals(lt.getSecond(), ((LocalDateTime)user.getAttribute("1")).getSecond());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidAttributesSetupPostConstruction_UnSupportedType() {
        UnlaunchUser user = new UnlaunchUser("user-123");
        user.putAttribute("1", new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction1() {
        UnlaunchUser user = new UnlaunchUser(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction2() {
        UnlaunchUser user = new UnlaunchUser("");
    }
}

