package io.unlaunch.event;

import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 *
 * @author jawad
 */
public class Impression extends Event {
    
    private final String flagKey; // will be part of the key in redis
    private final String userId;
    private final String variationKey; // will be use for variation count
    private final String flagStatus; 
    private final String evaluationReason;
    private static String machineName = "Not available";

    private static final Logger logger = LoggerFactory.getLogger(Impression.class);

    static {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            machineName = localHost.getHostName();
        } catch (Exception e) {
            logger.error("error getting machine name");
        }
    }
    
    public Impression(String flagKey, String userId, String variationKey, boolean flagStatus, String evaluationReason) {
        super(UnlaunchConstants.EVENT_TYPE_FOR_IMPRESSION_EVENTS, flagKey, variationKey);
        this.flagKey = super.getKey();
        this.variationKey = super.getSecondaryKey();
        this.userId = userId;
        this.flagStatus = flagStatus ? "active": "inactive";
        this.evaluationReason = evaluationReason;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public String getUserId() {
        return userId;
    }

    public String getVariationKey() {
        return variationKey;
    }

    public String getFlagStatus() {
        return flagStatus;
    }

    public String getEvaluationReason() {
        return evaluationReason;
    }

    public String getMachineName() {
        return machineName;
    }

}
