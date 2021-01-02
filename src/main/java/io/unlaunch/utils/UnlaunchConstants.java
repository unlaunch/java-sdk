package io.unlaunch.utils;

import io.unlaunch.UnlaunchFeature;

/**
 * Global constants.
 *
 * @author umermansoor
 */
public class UnlaunchConstants {

    public static final String SDK_KEY_ENV_VARIABLE_NAME = "UNLAUNCH_SDK_KEY";

    public static final String FLAG_INVOCATIONS_COUNT_EVENT_TYPE = "VARIATIONS_COUNT_EVENT";

    public static final String EVENT_TYPE_FOR_IMPRESSION_EVENTS = "IMPRESSION";

    public static final String FLAG_DEFAULT_RETURN_TYPE = "control";

    public static final UnlaunchFeature getControlFeatureByName(String flagKey) {
        return UnlaunchFeature.create(flagKey, "", null,
                "Unable to retrieve feature. Returning control variation" );
    }

    public static final String getSdkKeyHelpMessage() {
        return "To obtain the SDK key for your project, please sign in to the Unlaunch Console at " +
                "https://app.unlaunch.io  Then on the right sidebar, click on 'Settings'. From the 'Projects' tab, " +
                "Copy the 'SERVER_KEY' for the environment you want to connect to, and provide it to this SDK. For " +
                "more information, see this: https://docs.unlaunch.io/docs/sdks/sdk-keys";
    }
}
