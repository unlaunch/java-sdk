package io.unlaunch;


import java.util.Objects;

/**
 *  Account details for the current {@link UnlaunchClient}
 *
 * @author umermansoor
 */
public class AccountDetails {

    private final String projectName;
    private final String environmentName;

    private final int totalFlags;

     AccountDetails(String projectName, String environmentName, int totalFlags) {
        this.projectName = projectName;
        this.environmentName = environmentName;
        this.totalFlags = totalFlags;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public int getTotalFlags() {
        return totalFlags;
    }

    @Override
    public String toString() {
        return "AccountDetails{" +
                "projectName='" + projectName + '\'' +
                ", environmentName='" + environmentName + '\'' +
                ", totalFlags=" + totalFlags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDetails that = (AccountDetails) o;
        return totalFlags == that.totalFlags &&
                getProjectName().equals(that.getProjectName()) &&
                getEnvironmentName().equals(that.getEnvironmentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectName(), getEnvironmentName(), totalFlags);
    }
}
