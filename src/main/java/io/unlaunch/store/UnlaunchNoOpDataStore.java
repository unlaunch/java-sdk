package io.unlaunch.store;

import io.unlaunch.engine.FeatureFlag;

import java.io.IOException;
import java.util.List;

final class UnlaunchNoOpDataStore implements UnlaunchDataStore {

    @Override
    public FeatureFlag getFlag(String flagKey) {
        return null;
    }

    @Override
    public List<FeatureFlag> getAllFlags() {
        return null;
    }

    @Override
    public boolean isFlagExist(String flagKey) {
        return false;
    }

    @Override
    public String getProjectName() {
       return null;
    }
    
    @Override
    public String getEnvironmentName() {
        return null;
    }

    @Override
    public void refreshNow() {

    }

    @Override
    public void close() throws IOException {

    }
}
