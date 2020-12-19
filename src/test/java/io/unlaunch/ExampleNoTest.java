package io.unlaunch;

public class ExampleNoTest {

    public static void main(String[] args) {
        testThisIsAnExampleNotATest();
    }

    public static void testThisIsAnExampleNotATest() {
        UnlaunchClient client = UnlaunchClient.create("sdk-8cd7482a-fcd7-470f-81f0-9caa2b795a95");

        String variation = client.getVariation("flagKey", "userId123");
        if (variation.equals("on")) {
            System.out.println("Variation is on");
        } else if (variation.equals("off")) {
            System.out.println("Variation is off");
        } else {
            System.out.println("default variation");
        }
        client.shutdown();
    }

    public void testThisIsAnExampleOfUnlaunchAttributesNotATest() {

        UnlaunchClient client = UnlaunchClient.create("sdk-u8cd7482a-fcd7-470f-81f0-9caa2b795a95");

        String variation = client.getVariation("flagKey", "userId123",
                UnlaunchAttribute.newString("device-type", "iOS"),
                UnlaunchAttribute.newBoolean("paid-user", true));
        if (variation.equals("on")) {
            System.out.println("Variation is on");
        } else if (variation.equals("off")) {
            System.out.println("Variation is off");
        } else {
            System.out.println("default variation");
        }
        client.shutdown();
    }

    public void testThisIsAnExampleOfUnlaunchConfigNotATest() {
        UnlaunchClient client = UnlaunchClient.create("sdk-8cd7482a-fcd7-470f-81f0-9caa2b795a95");

        UnlaunchFeature f = client.getFeature("flagKey", "userId123",
                UnlaunchAttribute.newString("device-type", "iOS"),
                UnlaunchAttribute.newBoolean("paid-user", true));
        f.getVariationConfig().getString("buttonColor", "blue");
        if (f.getVariation().equals("on")) {
            System.out.println("Variation is on");
        } else if (f.getVariation().equals("off")) {
            System.out.println("Variation is off");
        } else {
            System.out.println("default variation");
        }
        client.shutdown();
    }
}
