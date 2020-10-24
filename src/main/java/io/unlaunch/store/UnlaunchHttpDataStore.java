package io.unlaunch.store;

import com.google.common.annotations.VisibleForTesting;
import io.unlaunch.engine.FeatureFlag;
import io.unlaunch.UnlaunchRestWrapper;
import io.unlaunch.engine.JsonObjectConversionHelper;
import io.unlaunch.exceptions.UnlaunchHttpException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
 * @author umermansoor
 * @author jawad
 */
final class UnlaunchHttpDataStore implements UnlaunchDataStore, Runnable {
    private  final Map<String, FeatureFlag> flagsMap;
    private AtomicReference<String> projectNameRef = new AtomicReference<>();
    private AtomicReference<String> environmentNameRef = new AtomicReference<>();
    private final UnlaunchRestWrapper restWrapper;
    private final CountDownLatch gate;
    private final JsonObjectConversionHelper flagService = new JsonObjectConversionHelper();
    private final JSONParser parser = new JSONParser();
    private final AtomicBoolean successSignal;
    private final AtomicInteger numHttpCalls = new AtomicInteger(0);

    private static final Logger logger = LoggerFactory.getLogger(UnlaunchHttpDataStore.class);

    protected UnlaunchHttpDataStore(UnlaunchRestWrapper restWrapper, CountDownLatch gate, AtomicBoolean successSignal) {
        this.restWrapper = restWrapper;
        this.gate = gate;
        this.successSignal = successSignal;
        this.flagsMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        numHttpCalls.incrementAndGet();
        boolean fetchedSuccessfully = false;

        String restApiResponse = null;
        try {
            restApiResponse = restWrapper.get(String.class);

            if (restApiResponse != null && !restApiResponse.isEmpty()) {
                Object obj = parser.parse(restApiResponse);
                JSONObject resBodyJson = (JSONObject) obj;

                JSONObject statusJson = (JSONObject)resBodyJson.get("status");
                String httpStatus = (String)  statusJson.get("code");

                if (httpStatus.equals("200")) {
                    JSONObject data = (JSONObject) resBodyJson.get("data");
                    projectNameRef.set((String)data.get("projectName"));
                    environmentNameRef.set((String)data.get("envName"));
                    JSONArray flags = (JSONArray) data.get("flags");

                    List<FeatureFlag> unlaunchFlags = flagService.toUnlaunchFlags(flags);

                    unlaunchFlags.forEach(flag -> flagsMap.put(flag.getKey(), flag));
                    logger.debug("downloaded {} features from the server", unlaunchFlags.size());

                    fetchedSuccessfully = true;
                } else {
                    logger.error("HTTP error downloading features: {} - {}", resBodyJson.get("data"), httpStatus);
                }
            } else {
                logger.debug("cached {} feature flag", flagsMap.size());
            }
        } catch ( UnlaunchHttpException ex) {
            logger.warn("unable to fetch feature flags using REST API " + ex.getMessage());
        } catch (ParseException pex) {
            logger.warn("unable to parse feature flags using REST API response {}", restApiResponse);
        } catch (Exception e) {
            logger.warn("unable to fetch feature flags using REST API " + e.getMessage());
        }

        if (fetchedSuccessfully) {
            successSignal.set(true);
            gate.countDown();
        } else {
            successSignal.set(false);
            gate.countDown();
        }
    }

    @Override
    public FeatureFlag getFlag(String flagKey) {
        return flagsMap.get(flagKey);
    }

    @Override
    public List<FeatureFlag> getAllFlags() {
        return new ArrayList<>(flagsMap.values());
    }

    @Override
    public boolean isFlagExist(String flagKey) {
        return flagsMap.containsKey(flagKey);
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
        flagsMap.clear();
    }

    @VisibleForTesting
    int getNumberOfHttpCalls() {
        return numHttpCalls.get();
    }
}
