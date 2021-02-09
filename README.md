# Unlaunch Java SDK

| main                                                                                                                | development                                                                                                                |
|---------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| [![Build Status](https://travis-ci.com/unlaunch/java-sdk.svg?branch=main)](https://travis-ci.com/unlaunch/java-sdk) | [![Build Status](https://travis-ci.com/unlaunch/java-sdk.svg?branch=development)](https://travis-ci.com/unlaunch/java-sdk) |

## Overview
The Unlaunch Java SDK provides a Java API to access Unlaunch feature flags and other features. Using the SDK, you can
 easily build Java applications that can evaluate feature flags, dynamic configurations, and more.

### Important Links

- To create feature flags to use with Java SDK, login to your Unlaunch Console at [https://app.unlaunch.io](https://app.unlaunch.io)
- [Official Guide](https://docs.unlaunch.io/docs/sdks/java-sdk)
- [Javadocs](https://javadoc.io/doc/io.unlaunch.sdk/unlaunch-java-sdk/latest/index.html)
- [MVN Repository](https://mvnrepository.com/artifact/io.unlaunch.sdk/unlaunch-java-sdk)
- [Sonatype](https://oss.sonatype.org/#nexus-search;quick~io.unlaunch.sdk)

### Compatibility
Unlaunch Java SDK requires Java 8 or higher.

 ### This is a server-side SDK
 This SDK is server-side and should be used in applications that you run on your own servers such as backend
  services or web servers. For more information, see [this](https://docs.unlaunch.io/docs/sdks/client-vs-server-side-sdks).

## Getting Started
Here is a simple example. 

First, add the maven dependency to your project. Use the latest version from [here](https://mvnrepository.com/artifact/io.unlaunch.sdk/unlaunch-java-sdk).

```$xslt
 <dependency>
    <groupId>io.unlaunch.sdk</groupId>
    <artifactId>unlaunch-java-sdk</artifactId>
    <version>0.0.7</version>
</dependency>
```
Here's how you'd use the Java SDK in your application.

```java
import io.unlaunch.UnlaunchClient;

public class ExampleApp { 
   public static void main(String[] args) {
 
       // initialize the client
       UnlaunchClient client = UnlaunchClient.create("INSERT_YOUR_SDK_KEY");
     
       // wait for the client to be ready
       try {
         client.awaitUntilReady(2, TimeUnit.SECONDS);
       } catch (InterruptedException | TimeoutException e) {
         System.out.println("client wasn't ready " + e.getMessage());
       }
       // get variation
       String variation = client.getVariation("flagKey", "userId123");
      
       // take action based on the returned variation
       if (variation.equals("on")) {
           System.out.println("Variation is on");
       } else if (variation.equals("off")) {
           System.out.println("Variation is off");
       } else {
           System.out.println("control variation");
       }

      // If you attached (key-value) configuration to your feature flag variations, 
      // here's how you can retrieve it:
       UnlaunchFeature feature = client.getFeature("new_login_ui", userId);
       String colorHexCode = feature.getVariationConfig().getString("login_btn_clr", "#cd5c5c");

       // shutdown the client to flush any events or metrics 
       client.shutdown();
   }
}
```

 For more information, see the [official guide](https://docs.unlaunch.io/docs/sdks/java-sdk#configuration).

## Build instructions

### Requirements
- Java 8 or higher
- Maven 2 or higher

To build the project using maven, run the following command:
```$xslt
mvn clean install -Dgpg.skip
```
Note: Use `-Dgpg.skip` to bypass GPG keyphrase prompt. It is only needed for publishing to Maven Central repo.

To run all unit and integration tests:
```$xslt
mvn verify
```

If tests are failing, and you need to build (not recommended,) you can force to skip tests:
```$xslt
mvn clean install -Dmaven.test.skip=true -Dgpg.skip
```

## Customization

You can use builder to customize the client. For more information, see the [official guide](https://docs.unlaunch.io/docs/sdks/java-sdk#configuration).

```$xslt
UnlaunchClient client = UnlaunchClient.builder()
                .sdkKey("INSERT_YOUR_SDK_KEY")
                .pollingInterval(60, TimeUnit.SECONDS)
                .eventsFlushInterval(30, TimeUnit.SECONDS)
                .eventsQueueSize(500)
                .metricsFlushInterval(30, TimeUnit.SECONDS)
                .metricsQueueSize(100)
                .build();
```

### Offline Mode

You can start the SDK in offline mode for testing purposes. In offline mode, flags aren't downloaded from the server
 and no data is sent. All calls to `getVariation` or its variants will return `control`. Read more in the [official
  guide](https://docs.unlaunch.io/docs/sdks/java-sdk#offline-mode).
 
 To start the client in offline mode for testing purposes, call the `offlineMode` method:
  
  ```$xslt
UnlaunchClient client = UnlaunchClient.builder().offlineMode().build();
  ```

## Contributing
Please see [CONTRIBUTING](CONTRIBUTING.md) to find how you can contribute.

## License
Licensed under the Apache License, Version 2.0. See: [Apache License](LICENSE.md).

## Publish Releases on Maven Central
To publish a new release version on Maven Central, there are two ways. Make sure you have `-SNAPSHOT` version in the pom file.

Create a new Git tag and let the `./deploy` script do its job. You'll need to **manually** update the version in `pom
.xml` after this is successful. Only do this on the `main` branch. For example, to release `0.0.3` version:

```$xslt
git tag 0.0.3 
git push origin --tags
```

You **must** then update `pom.xml` version to be `0.0.4-SNAPSHOT`. Commit your code to `develop` and PR into `main`.

Alternatively, you can also use the Maven Release plugin to deploy directly from your machine. 
 
Helpful guides:
- [guide](https://central.sonatype.org/pages/apache-maven.html)
- [guide](https://infra.apache.org/publishing-maven-artifacts.html)

## About Unlaunch
Unlaunch is a Feature Release Platform for engineering teams. Our mission is allow engineering teams of all
 sizes to release features safely and quickly to delight their customers. To learn more about Unlaunch, please visit
  [www.unlaunch.io](https://unlaunch.io). You can sign up to get started for free at [https://app.unlaunch.io/signup
  ](https://app.unlaunch.io/signup).

## FAQs

##### Question: I'm seeing `gpg: signing failed: Inappropriate ioctl for device`
Answer: Please run `export GPG_TTY=$(tty)` See: https://github.com/keybase/keybase-issues/issues/2798

##### Question: Where are the artifacts deployed?
Answer: The artifacts are published on Sonatype at: https://oss.sonatype.org/#nexus-search;quick~io.unlaunch.sdk

## More Questions?
At Unlaunch, we are obsessed about making it easier for developers all over the world to release features safely and with confidence. If you have *any* questions or something isn't working as expected, please email **unlaunch@gmail.com**.