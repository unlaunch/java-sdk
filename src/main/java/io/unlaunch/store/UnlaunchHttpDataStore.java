package io.unlaunch.store;

import com.google.common.annotations.VisibleForTesting;
import io.unlaunch.engine.FeatureFlag;
import io.unlaunch.UnlaunchRestWrapper;
import io.unlaunch.engine.JsonObjectConversionHelper;
import io.unlaunch.exceptions.UnlaunchHttpException;
import io.unlaunch.utils.UnlaunchConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is responsible for fetching feature flags using the HTTP API and maintain a {@link ConcurrentHashMap} of
 * feature flags that are downloaded from the server in memory.
 *
 * This class is not mutable and hence it is thread-safe.
 *
 * It is safe to call run this class periodically and hence can be used by {@link RefreshableDataStoreProvider}s.
 *
 */
final class UnlaunchHttpDataStore implements UnlaunchDataStore, Runnable {

    private final AtomicReference<Map<String, FeatureFlag>> refFlagsMap;
    private AtomicReference<String> projectNameRef = new AtomicReference<>();
    private AtomicReference<String> environmentNameRef = new AtomicReference<>();
    private final UnlaunchRestWrapper restWrapper;
    private final CountDownLatch gate;
    private final JsonObjectConversionHelper flagService = new JsonObjectConversionHelper();
    private final JSONParser parser = new JSONParser();
    private final AtomicBoolean initialSyncSuccessful;
    private final AtomicInteger numHttpCalls = new AtomicInteger(0);

    private static final Logger logger = LoggerFactory.getLogger(UnlaunchHttpDataStore.class);

    protected UnlaunchHttpDataStore(UnlaunchRestWrapper restWrapper, CountDownLatch gate, AtomicBoolean initialSyncSuccessful) {
        this.restWrapper = restWrapper;
        this.gate = gate;
        this.initialSyncSuccessful = initialSyncSuccessful;
        this.refFlagsMap = new AtomicReference<>(new HashMap<>());
    }

    @Override
    public void run() {
        numHttpCalls.incrementAndGet();
        Object obj = null;

        try {
            Response response = restWrapper.get();
            if (response.getStatus() == 304) {
                logger.debug("synced flags with the server. No update. In memory data store has {} flags",
                        refFlagsMap.get().size());
            } else if (response.getStatus() == 200) {
                obj = parser.parse(response.readEntity(String.class));
                JSONObject resBodyJson = (JSONObject) obj;

                JSONObject data = (JSONObject) resBodyJson.get("data");
                projectNameRef.set((String)data.get("projectName"));
                environmentNameRef.set((String)data.get("envName"));
                JSONArray flags = (JSONArray) data.get("flags");

                List<FeatureFlag> unlaunchFlags = flagService.toUnlaunchFlags(flags);

                Map<String, FeatureFlag> newFlagsMap = new HashMap<>(unlaunchFlags.size());
                unlaunchFlags.forEach(flag -> newFlagsMap.put(flag.getKey(), flag));

                refFlagsMap.set(newFlagsMap); //  Update  the main flag store's reference

                logger.debug("downloaded {} features from the server", unlaunchFlags.size());

                if (!initialSyncSuccessful.get()) {
                    logger.info("Initial sync was successful and the client is ready. Synced {} flags", refFlagsMap.get().size());
                    initialSyncSuccessful.set(true);
                } else {
                    logger.info("Synced latest data. There are {} flags in memory.", refFlagsMap.get().size());
                }
            } else if (response.getStatus() == 403) {
                logger.error("The SDK key you provided was rejected by the server. This error in not recoverable and " +
                        "you should check to make sure you are using the correct SDK Key. All feature flag " +
                        "evaluations will return control. {}",  UnlaunchConstants.getSdkKeyHelpMessage());
            }
            else {
                logger.error("HTTP error downloading features: {} - {}", response.readEntity(String.class), response.getStatus());
            }
        } catch ( UnlaunchHttpException ex) {
            logger.warn("unable to fetch flags using REST API " + ex.getMessage());
        } catch (ParseException pex) {
            logger.warn("unable to parse flags response that the API returned: {}. Error {}", obj, pex.getMessage());
        } catch (Exception e) {
            logger.warn("an error occurred when fetching flags using the REST API " + e.getMessage());
        } finally {
            gate.countDown(); // unblock the client. For now, anything unblocks.
        }
    }

    @Override
    public FeatureFlag getFlag(String flagKey) {
        return refFlagsMap.get().get(flagKey);
    }

    @Override
    public List<FeatureFlag> getAllFlags() {
        return new ArrayList<>(refFlagsMap.get().values());
    }

    @Override
    public boolean isFlagExist(String flagKey) {
        return refFlagsMap.get().containsKey(flagKey);
    }

    @Override
    public final String getProjectName() {
        return projectNameRef.get();
    }
    
    @Override
    public final String getEnvironmentName() {
        return environmentNameRef.get();
    }

    @Override
    public void refreshNow() {
        run();
    }

    /**
     * Be kind and clean up after yourself.
     */
    public void close() {
        projectNameRef.set(null);
        environmentNameRef.set(null);
        refFlagsMap.get().clear();
    }

    @VisibleForTesting
    int getNumberOfHttpCalls() {
        return numHttpCalls.get();
    }
}
