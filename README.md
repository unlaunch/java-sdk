# Unlaunch Java SDK

## Overview
Unlaunch Java SDK is designed to help with work with Unlaunch a platform for feature flags. Feature flags are created
 and defined using the Unlaunch web console which is available at [https://app.unlaunch.io](https://app.unlaunch.io).

In a nutshell, here is how this SDK works:

- On initialization, it connects to your Unlaunch project and downloads all feature flags
- You evaluate feature flags using its public methods.
- Feature flag changes are automatically fetched in, so you do not need to refresh.

### Compatibility
Unlaunch Java SDK requires Java 8 or higher.

## Getting Started
Here is a simple example:

```java
import io.unlaunch.UnlaunchClient;

public class ExampleApp {

        public static void main(String[] args) {

            UnlaunchClient client = UnlaunchClient.create("INSERT_YOUR_SDK_KEY");
            String variation = client.getVariation("flagKey", "userId123");
            if (variation.equals("on")) {
                System.out.println("Variation is on");
            } else if (variation.equals("off")) {
                System.out.println("Variation is off");
            } else {
                System.out.println("default variation");
            }

            // If you want to pass attributes which are used in the targeting rules
            variation = client.getVariation("flagKey", "userId123", 
                            UnlaunchAttribute.newString("device-type", "iOS"), 
                            UnlaunchAttribute.newBoolean("paid-user", true));

            // If you attached (key-value) configuration to your feature flag variations, here's how you can retrieve it:
            Feature f = client.getFeature("flagKey", "userId123");
            f.getVariationConfig().getString("buttonColor");

            client.shutdown();
        }
}
```

## Build instructions

### Requirements
- Java 8 or higher
- Maven 2 or higher

To build the project using maven, run the following command:
```$xslt
mvn clean install 
```
To run all unit and integration tests:
```$xslt
mvn verify
```

If tests are failing, and you need to build (not recommended,) you can force to skip tests:
```$xslt
mvn clean install -Dmaven.test.skip=true
```

### Adding as a dependency in your project
```$xslt
  <dependency>
      <groupId>com.unlaunch</groupId>
      <artifactId>unlaunch-java-sdk</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
```

## Usage and Examples

### Using the builder to specify settings

```$xslt
UnlaunchClient.builder().
                sdkKey("sdkKey").
                eventFlushInterval(-1, TimeUnit.SECONDS).
                build();
```


## License
TBD

Visit [www.unlaunch.io](www.unlaunch.io) to learn more about Unlaunch.
