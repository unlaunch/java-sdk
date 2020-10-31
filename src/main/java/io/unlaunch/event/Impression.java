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
    private static String machineIp = "Not available";
    private static String machineName = "Not available";

    private static final Logger logger = LoggerFactory.getLogger(Impression.class);

    static {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            machineName = inetAddress.getHostName();
            
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                for (InterfaceAddress netAddress : netInterfaces.nextElement().getInterfaceAddresses()) {
                    if (netAddress.getAddress().isSiteLocalAddress()) {
                        machineIp = netAddress.getAddress().getHostAddress(); //TODO: Remove don't need locl lP. Useless
                    }
                }
            }

        } catch (Exception e) {
            logger.error("error getting machine name or IP address");
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

    public String getMachineIp() {
        return machineIp;
    }

    public String getMachineName() {
        return machineName;
    }

}
