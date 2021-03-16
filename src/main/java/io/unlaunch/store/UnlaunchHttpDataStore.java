package io.unlaunch.store;

import com.google.common.annotations.VisibleForTesting;
import io.unlaunch.UnlaunchGenericRestWrapper;
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
 * <p>
 * This class is not mutable and hence it is thread-safe.
 * <p>
 * It is safe to call run this class periodically and hence can be used by {@link RefreshableDataStoreProvider}s.
 */
final class UnlaunchHttpDataStore implements UnlaunchDataStore, Runnable {

    private final AtomicReference<Map<String, FeatureFlag>> refFlagsMap;
    private AtomicReference<String> projectNameRef = new AtomicReference<>();
    private AtomicReference<String> environmentNameRef = new AtomicReference<>();
    private final UnlaunchRestWrapper restWrapper;
    private final UnlaunchGenericRestWrapper s3BucketRestWrapper;
    private final CountDownLatch gate;
    private final JsonObjectConversionHelper flagService = new JsonObjectConversionHelper();
    private final JSONParser parser = new JSONParser();
    private final AtomicBoolean initialSyncSuccessful;
    private final AtomicBoolean sync0Complete = new AtomicBoolean(false);
    private final AtomicInteger numHttpCalls = new AtomicInteger(0);

    private static final Logger logger = LoggerFactory.getLogger(UnlaunchHttpDataStore.class);

    protected UnlaunchHttpDataStore(UnlaunchRestWrapper restWrapper, UnlaunchGenericRestWrapper s3BucketRestWrapper,
                                    CountDownLatch gate, AtomicBoolean initialSyncSuccessful) {
        this.restWrapper = restWrapper;
        this.s3BucketRestWrapper = s3BucketRestWrapper;
        this.gate = gate;
        this.initialSyncSuccessful = initialSyncSuccessful;
        this.refFlagsMap = new AtomicReference<>(new HashMap<>());
    }

    private boolean sync0()  {

        try {
            Response response = s3BucketRestWrapper.get();

            // If the s3 object doesn't exist, attempt a regular sync
            if (response.getStatus() != 200) {
                return false;
            }

            // If the s3 doesn't parse, attempt a regular sync
            Object obj = parser.parse(response.readEntity(String.class));

            JSONObject resBodyJson = (JSONObject) obj;

            logger.debug("s3 json {}", resBodyJson);

            int numFlags = initFeatureStore(resBodyJson);

            if (numFlags > 0) {
                logger.info("First sync completed successfully");
                return true;
            } else {
                // This is a soft failure. It is possible that the JSON is valid and doesn't contain any flags.
                // However, we err on the side of caution and declare failure so regular sync can be tried
                return false;
            }

        } catch (ParseException pex) {
            logger.error("error parsing object. Fetching from server");
            return false;
        } catch (Exception e) {
            logger.error("unknown error during initial sync {}", e.toString());
            return false;
        }
    }

    private void regularServerSync() throws ParseException {
        Object obj = null;
        numHttpCalls.incrementAndGet();

        Response response = restWrapper.get();
        if (response.getStatus() == 304) {
            logger.debug("synced flags with the server. No update. In memory data store has {} flags",
                    refFlagsMap.get().size());
        } else if (response.getStatus() == 200) {
            obj = parser.parse(response.readEntity(String.class));
            JSONObject resBodyJson = (JSONObject) obj;

            JSONObject data = (JSONObject) resBodyJson.get("data");

            initFeatureStore(data);

        } else if (response.getStatus() == 403) {
            logger.error("The SDK key you provided was rejected by the server. This error in not recoverable and " +
                    "you should check to make sure you are using the correct SDK Key. All feature flag " +
                    "evaluations will return control. {}", UnlaunchConstants.getSdkKeyHelpMessage());
        } else {
            logger.error("HTTP error downloading features: {} - {}", response.readEntity(String.class), response.getStatus());
        }
    }

    /**
     * Takes the JSON data returned from the CDN or Unlaunch Server and initializes in-memory feature flag store.
     *
     * @param data
     */
    private final int initFeatureStore(JSONObject data) {
        projectNameRef.set((String) data.get("projectName"));
        environmentNameRef.set((String) data.get("envName"));
        JSONArray flags = (JSONArray) data.get("flags");

        List<FeatureFlag> unlaunchFlags = flagService.toUnlaunchFlags(flags);

        Map<String, FeatureFlag> newFlagsMap = new HashMap<>(unlaunchFlags.size());
        unlaunchFlags.forEach(flag -> newFlagsMap.put(flag.getKey(), flag));

        refFlagsMap.set(newFlagsMap); //  Update  the main flag store's reference

        if (!initialSyncSuccessful.get()) {
            logger.info("Initial sync was successful and the client is ready. Synced {} flags", refFlagsMap.get().size());
            initialSyncSuccessful.set(true);
        } else {
            logger.info("Synced latest data. There are {} flags in memory.", refFlagsMap.get().size());
        }

        logger.info("downloaded {} features from the server", unlaunchFlags.size());
        return unlaunchFlags.size();
    }

    @Override
    public void run() {
        try {
            if (!sync0Complete.get()) {
                sync0Complete.set(true); // we preemptively marked it as done
                if (!sync0()) {
                    regularServerSync(); // Attempt regular sync is s3 didn't work
                }
            } else {
                regularServerSync();
            }

        } catch (UnlaunchHttpException ex) {
            logger.warn("unable to fetch flags {}", ex.toString());
        } catch (ParseException pex) {
            logger.warn("unable to parse flags response. Error {}", pex.toString());
        } catch (Exception e) {
            logger.warn("an error occurred when fetching flags {}", e.toString());
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
        refFlagsMap.set(new HashMap<>());
    }

    @VisibleForTesting
    int getNumberOfHttpCalls() {
        return numHttpCalls.get();
    }
}
