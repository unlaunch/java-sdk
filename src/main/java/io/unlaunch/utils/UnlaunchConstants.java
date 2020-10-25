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
}
