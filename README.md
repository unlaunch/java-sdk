# Unlaunch Java SDK

## Overview
The Unlaunch Java SDK provides a Java API to access Unlaunch. Using the SDK, you can easily build Java applications that can evaluate feature flags, access configuration, and more.

To create feature flags to use with Java SDK, login to your Unlaunch Console at [https://app.unlaunch.io](https://app.unlaunch.io).

For more information, visit the [official guide](https://docs.unlaunch.io/docs/sdks/java-sdk).

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
       String variation = client.getVariation("flagKey", "userId123");

      // If you attached (key-value) configuration to your feature flag variations, 
      // here's how you can retrieve it:
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
mvn clean install -Dgpg.skip
```
To run all unit and integration tests:
```$xslt
mvn verify
```

If tests are failing, and you need to build (not recommended,) you can force to skip tests:
```$xslt
mvn clean install -Dmaven.test.skip=true -Dgpg.skip
```

### Adding as a dependency in your project
```$xslt
  <dependency>
      <groupId>io.unlaunch.sdk</groupId>
      <artifactId>unlaunch-java-sdk</artifactId>
      <version>1.0.0</version>
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

## Contributing
Please see [CONTRIBUTING](CONTRIBUTING.md) to find how you can contribute.

## License
Licensed under the Apache License, Version 2.0. See: [Apache License](LICENSE.md).

## About Unlaunch
Unlaunch is a Feature Release Platform for engineering teams. Our mission is allow engineering teams of all
 sizes to release features safely and quickly to delight their customers. To learn more about Unlaunch, please visit
  [www.unlaunch.io](www.unlaunch.io). You can sign up to get started for free at [https://app.unlaunch.io/signup
  ](https://app.unlaunch.io/signup).

## FAQs

##### Question: I'm seeing `gpg: signing failed: Inappropriate ioctl for device`
Answer: Please run `export GPG_TTY=$(tty)` See: https://github.com/keybase/keybase-issues/issues/2798
