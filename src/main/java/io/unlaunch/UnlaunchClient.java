package io.unlaunch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>Client for accessing Unlaunch. Provides all features for accessing and evaluating features flags setup through
 * the Unlaunch web console i.e. <a href="https://app.unlaunch.io">https://app.unlaunch.io</a>). UnLaunch is an
 * enterprise-grade feature flags platform inspired by the way you ship features. Deploy your features to
 * production more frequently and with confidence.</p>
 *
 * <p>Sample code to get you started:</p>
 *
 * <code>
 *     UnlaunchClient client = UnlaunchClient.create("your_sdk_key");
 *
 *     String variation = client.getVariation("flagKey", "userId123");
 *     if (variation == "on") {
 *         System.out.println("Variation is on");
 *     } else if (variation == "off") {
 *         System.out.println("Variation is off");
 *     } else {
 *         System.out.println("no variation");
 *     }
 *
 *     client.shutdown();
 * </code>
 *
 *<p>This is the <b>server-side</b> SDK which means it will download all feature flags and associated data
 * upon initialization (and periodically based on settings.)</p>
 *
 * <p>Creating a new client can be an expensive operation and should only be done once and shared in an application
 * The <em>close()</em> method needs to be called on the client object to clean up resources such as threads during
 * application shutdown. To create clients, you can use the <em>create</em> methods such as
 * {@link UnlaunchClient#create(String)} or using the static {@link #builder()} method which returns
 * {@link UnlaunchClientBuilder} which can be used to customize the client.</p>
 *
 * @author umermansoor
 * @see <a href="https://unlaunch.io">https://unlaunch.io</a>
 */

public interface UnlaunchClient extends Cloneable {

    /**
     * Create a {@link UnlaunchClient} with the SDK key loaded from the <em>UNLAUNCH_SDK_KEY</em> environment variable.
     * @return
     */
    static UnlaunchClient create() {
        return builder().build();
    }

    /**
     * Constructs an instance of the {@link UnlaunchClient} using the given SDK key.
     * @param sdkKey - SDK key for your Unlaunch project.
     * @return A new client
     */
    static UnlaunchClient create(String sdkKey)  {
        return builder().sdkKey(sdkKey).build();
    }

    /**
     * Create a builder that can be used to customize and create a {@link UnlaunchClient}.
     *
     */
    static UnlaunchClientBuilder builder() {
        return new DefaultUnlaunchClientBuilder();
    }

    /**
     * Returns account details for the Unlaunch project and environment the client is connected to.
     *
     * @return {@link AccountDetails}
     */
    AccountDetails accountDetails();

    /**
     *  Evaluates and returns the variation (variation key) for this feature. Variations are defined in the Unlaunch
     *  web console at <a href="https://app.unlaunch.io">https://app.unlaunch.io</a>.
     *  <p></p>
     *  <p>This method returns "none" if:</p>
     *  <ol>
     *      <li> The flag was not found.</li>
     *      <li> There was an exception evaluation the feature flag.</li>
     *      <li> The flag was archived.</li>
     *  </ol>
     * <p></p>
     * <p>This method doesn't throw any exceptions nor does it return <code>null</code> value</p>
     * @param flagKey the feature flag you want to evaluate.
     * @param identity unique id of your user or a unique identifier such as request or session id, email, etc. It
     *                 may be null but 'Percentage rollout' will not work because it is used to determine bucketing.
     *                 If this is null and 'Percentge rollout' is enabled, we'll log exceptions in your code and also
     *                 in the Unlaunch web app.
     * @return the evaluated variation or  "none" if there was an error.
     */
    String getVariation(String flagKey, String identity);

    /**
     *  Same as {@link #getVariation(String, String)} but uses <code>attributes</code> that are passed as argument when
     *  evaluating the feature flag's targeting rules. Targeting rules are defined on the Unlaunch web console.
     *
     *  To understand attributes, suppose you have defined a feature flag with targeting rules to return certain
     *  variation based on user's country e.g.
     *
     *  <code>
     *      if user's country is "USA" AND device is"handheld"
     *          return "on"
     *      otherwise
     *          return "off"
     *  </code>
     *
     *  In this example, <em>country</em> will be <em>device</em> are attributes that must be passed in.
     *
     * <p>This method doesn't throw any exceptions nor does it return <code>null</code> value</p>
     * @param flagKey the feature flag you want to evaluate.
     * @param identity unique id of your user or a unique identifier such as request or session id, email, etc. It
     *      *                 may be null but 'Percentage rollout' will not work because it is used to determine bucketing.
     *      *                 If this is null and 'Percentge rollout' is enabled, we'll log exceptions in your code and also
     *      *                 in the Unlaunch web app.
     * @param attributes attributes to apply when evaluating target rules.
     * @return the evaluated variation or  "none" if there was an error.
     */
    String getVariation(String flagKey, String identity, UnlaunchAttribute ... attributes);

    /**
     *  Same as {@link #getVariation(String, String)} but returns a {@link UnlaunchFeature} object that contains the
     *  evaluated variation (variation key) and any configuration (key, value properties or JSON) associated with the
     *  evaluated variation, defined in the Unlaunch web console. 
     *  
     * @param flagKey the feature flag you want to evaluate.
     * @param identity unique id of your user or a unique identifier such as request or session id, email, etc. It
     *    may be null but 'Percentage rollout' will not work because it is used to determine bucketing.
     * @return {@link UnlaunchFeature} object that contains evaluated variation key, configuration and evaluation reason.
     */
    UnlaunchFeature getFeature(String flagKey, String identity);

    /**
     *  Same as {@link #getFeature(String, String)} but uses <code>attributes</code> that are passed as
     *  argument when evaluating the feature flag's targeting rules. Targeting rules are defined on the Unlaunch web
     *  console.
     *
     *  To understand attributes, suppose you have defined a feature flag with targeting rules to return certain
     *  variation based on user's country e.g.
     *
     *  <code>
     *      if user's country is "USA" AND device is"handheld"
     *          return "on"
     *      otherwise
     *          return "off"
     *  </code>
     *
     *  In this example, <em>country</em> will be <em>device</em> are attributes that must be passed in.
     * @param flagKey the feature flag you want to evaluate.
     * @param identity unique id of your user or a unique identifier such as request or session id, email, etc. It
     *    may be null but 'Percentage rollout' will not work because it is used to determine bucketing.
     * @return {@link UnlaunchFeature} object that contains evaluated variation key, configuration and evaluation reason.
     */
    UnlaunchFeature getFeature(String flagKey, String identity, UnlaunchAttribute ... attributes);

    /**
     * Causes the current thread to wait until {@link UnlaunchClient} is initialized, unless the thread is interrupted,
     * or the specified waiting time elapses.
     *
     * <p>If the current thread:
     *  <ul>
     *     <li>has its interrupted status set on entry to this method; or
     *     <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     *  </ul>
     *
     * then {@link InterruptedException} is thrown and the current thread's interrupted status is cleared. </p>
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws TimeoutException if it times out before initialization is complete
     */
    void awaitUntilReady(long timeout, TimeUnit unit)  throws InterruptedException, TimeoutException;

    /**
     * Returns true is the client is intialized and ready. False otherwise.
     *
     * <p>Initialized means that the client was able to download flags and data from the Unlaunch server at leasr
     * once. </p>
     * @return
     */
    boolean isInitialized();

    /**
     * Closes the client by cleaning up all resources such as in memory caches, etc., stops running tasks and
     * rejects any new requests which arrive after this method is called.
     *
     * <p>The default behavior of this method is to <b>block</b> until everything has been shutdown or it encountered
     * an error or timed out (usually a few seconds.)</p>
     */
    void shutdown();
}
