package io.unlaunch.engine;


import io.unlaunch.UnlaunchFeature;
import io.unlaunch.utils.MurmurHash3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class contains the logic for feature flag evaluation.
 *
 * @author umermansoor
 * @author jawad
 */
public class Evaluator {
    private static final String HASH_ALGO = "SHA-256";
    private static final Logger logger = LoggerFactory.getLogger(Evaluator.class);

    public UnlaunchFeature evaluate(FeatureFlag flag, UnlaunchUser user) {

        AtomicReference<String> evaluationReasonRef = new AtomicReference<>("UNSET"); // TODO hack, please fix me
        Variation v = evaluateInternal(flag, user, evaluationReasonRef);

        return UnlaunchFeature.create(flag.getKey(), v.getKey(), v.getProperties(), evaluationReasonRef.get());
    }


    /**
     * Match user attribute values with rules one by one in the order they are defined.
     * The first rule defined has the highest priority and so on.
     * Returns variation defined for the rule if the user attribute values matches all the conditions in the rule.
     * Returns default rule variation if user not matches with any rule.
     * Returns off variation if flag is not active.
     *
     * @param flag
     * @param user
     * @return
     */
    private Variation evaluateInternal(FeatureFlag flag, UnlaunchUser user,
                                       AtomicReference<String> evaluationReasonRef /* hack, remove this */) {

        if (flag == null) {
            throw new IllegalArgumentException("unlaunchFlag must not be null");
        }

        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }

        Variation variationToServe;
        String evaluationReason = "";

        if (!flag.isEnabled()) {
            logger.debug("FLAG_DISABLED, {}, OFF_VARIATION is served to user {}", flag.getKey(), user.getId());

            variationToServe = flag.getOffVariation();

            evaluationReason = "Flag disabled. Default Variation served";

        } else if (!checkDependencies(flag, user)) {

            logger.info("PREREQUISITE_FAILED for flag {}, OFF_VARIATION is served to user {}", flag.getKey(), user.getId());

            variationToServe = flag.getOffVariation();

            evaluationReason = "Prerequisite failed. Default Variation served";

        } else if ((variationToServe = getVariationIfUserInAllowList(flag, user)) != null) {

            logger.info("USER_IN_ALLOWLIST for flag {}, VARIATION {} is served to user {}", flag.getKey(), variationToServe, user.getId());

            evaluationReason = "User is in Target Individual Users List";

        } else {
            int bucketNumber = getBucket(user.getId(), flag.getKey());

            // TODO Extract into its own method
            for (Rule rule : flag.getRules()) {
                if (variationToServe == null && !rule.isIsDefault() && rule.matches(user)) {

                    variationToServe = getVariationToServeByRule(rule, bucketNumber);

                    logger.debug(
                            "RULE_MATCHED for flag {}, {} variation is served to user {}",
                            flag.getKey(),
                            variationToServe.getKey(),
                            user.getId()
                    );

                    evaluationReason = "Targeting Rule matched";

                    break;
                }
            }

            // No variation matched by rule. Use the default rule.
            if (variationToServe == null) {
                Rule defaultRule = flag.getDefaultRule();

                variationToServe = getVariationToServeByRule(defaultRule, bucketNumber);
                logger.debug(
                        "RULE_NOT_MATCHED for flag {}, {} variation is served to user {}",
                        flag.getKey(),
                        variationToServe.getKey(),
                        user.getId()
                );

                evaluationReason = "Default Rule Served";
            }
        }

        if (evaluationReasonRef != null) {
            evaluationReasonRef.set(evaluationReason); // TODO hack, please fix me.
        }

        return variationToServe;

    }

    /**
     * @param featureFlag
     * @param user
     * @return
     */
    private boolean checkDependencies(FeatureFlag featureFlag, UnlaunchUser user) {
        Map<FeatureFlag, Variation> prerequisiteFlags = featureFlag.getPrerequisiteFlags();

        if (prerequisiteFlags == null || prerequisiteFlags.isEmpty()) {
            return true;
        }

        for (FeatureFlag prerequisiteFlag : prerequisiteFlags.keySet()) {

            Variation variation = evaluateInternal(prerequisiteFlag, user, null);

            if (!variation.getKey().equals(prerequisiteFlags.get(prerequisiteFlag).getKey())) {
                logger.info("PREREQUISITE_FAILED,{},{}", prerequisiteFlag.getKey(), user.getId());
                return false;
            }
        }

        return true;
    }

     int getBucket(String userId, String featureId) {
        if (userId == null || featureId == null) {
            throw new IllegalArgumentException("userId and featureId must not be null");
        }

        String key = userId + featureId;
        long hash = getHash(key);

        return (int) (Math.abs(hash % 100) + 1);
    }

    private long getHash(String key) {
        return MurmurHash3.murmurhash3_x86_32(key, 0, key.length(), 0);
    }

    private Variation getVariationIfUserInAllowList(FeatureFlag flag, UnlaunchUser user) {
        for (Variation variation : flag.getVariations()) {
            if (variation.getAllowList() != null) {
                List<String> allowList = Arrays.asList(variation.getAllowList().replace(" ", "").split(","));

                if (allowList.contains(user.getId())) {
                    return variation;
                }
            }
        }
        return null;
    }

    private Variation getVariationToServeByRule(Rule rule, int bucketNumber) {
        Variation variationToServe;
        int sum = 0;
        for (Variation variation : rule.getVariations()) {
            sum += variation.getRolloutPercentage();
            variationToServe = isVariationAvailable(sum, bucketNumber) ? variation : null;
            if (variationToServe != null) {
                return variationToServe;
            }
        }
        logger.warn("return null variationToServe. Something went wrong. Rule {}, bucketNumber {}", rule, bucketNumber);
        return null;
    }

    private boolean isVariationAvailable(int rolloutPercent, int bucket) {
        return bucket <= rolloutPercent;
    }
}
