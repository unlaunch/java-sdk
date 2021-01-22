package io.unlaunch.utils;

import io.unlaunch.UnlaunchClient;
import io.unlaunch.UnlaunchFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestServerCall {
    private static final Logger logger = LoggerFactory.getLogger(TestServerCall.class);


    // EDIT ME! Set SDK_KEY to your Unlaunch SDK key.
    private static final String SDK_KEY = "prod-public-dd8a5f60-b622-4e3d-aa62-0d727c6643de";

    // EDIT ME!  Set FEATURE_FLAG_KEY to your feature flag key to evaluate
    private static final String FEATURE_FLAG_KEY = "set-attr-type-3";

    public static void main(String[] args) throws Exception{
        if (SDK_KEY == null || SDK_KEY.isEmpty()) {
            logger.error("[DEMO] You must edit Hello.java to set SDK_KEY to Unlaunch SDK key. \n" +
                    "Please visit https://docs.unlaunch.io/docs/sdks/sdk-keys for more information.");
            System.exit(1);
        }

        if (FEATURE_FLAG_KEY == null || FEATURE_FLAG_KEY.isEmpty()) {
            logger.error("[DEMO] You must edit Hello.java to set FEATURE_FLAG_KEY to key of the flag you wish to \n" +
                    "fetch. Please visit https://docs.unlaunch.io/docs/getting-started for more information.");
            System.exit(1);
        }

        // Create UnlaunchClient object simply by using SDK_KEY.
        UnlaunchClient client = UnlaunchClient.builder().sdkKey(SDK_KEY).pollingInterval(15, TimeUnit.SECONDS).build();

        // Wait for the client to be ready
        try {
            client.awaitUntilReady(15, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            logger.error("[DEMO] client wasn't ready " + e.getMessage());
        }

        // Get variation
        String variation = client.getVariation(FEATURE_FLAG_KEY, "user-id-12345");

        logger.info("[DEMO] getVariation() returned {}", variation);
        if (variation.equals("control")) {
            logger.info("'[DEMO] control' variation indicates that Unlaunch Client didn't connect with the server of the" +
                    " flag wasn't found.");
        }

        // Now get Feature with evaluation reason. This is an alternate way to obtain feature flag besides
        // getVariation() method. This returns additional information such as evaluation reason, and configuration.
        UnlaunchFeature feature = client.getFeature(FEATURE_FLAG_KEY, "user-id-123");
        logger.info("[DEMO] Feature returned variation: {}. Evaluation reason: {}",
                feature.getVariation(), feature.getEvaluationReason());
        // shutdown the client to flush any events or metrics


        Thread.sleep(100000000);

        client.shutdown();
    }
}
