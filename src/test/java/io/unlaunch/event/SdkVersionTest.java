package io.unlaunch.event;

import org.junit.Assert;
import org.junit.Test;

public class SdkVersionTest {

    @Test
    public void testVersionIsNotEmpty() {
        Assert.assertFalse(SdkVersion.VERSION_FROM_MANIFEST.isEmpty());
    }
}