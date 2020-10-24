package io.unlaunch.store;

import io.unlaunch.engine.FeatureFlag;

import java.io.Closeable;
import java.util.List;

/**
 * Interface specifying objects which holds feature flags and related data retrieved from the Unlaunch service.
 *
 * All implementations must provide thread-safe access.
 *
 * @author umermansoor
 */
public interface UnlaunchDataStore extends Closeable {

    FeatureFlag getFlag(String flagKey);

    List<FeatureFlag> getAllFlags();

    boolean isFlagExist(String flagKey);

    String getProjectName();
    
    String getEnvironmentName();

    void refreshNow();
}
