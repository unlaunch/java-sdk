package io.unlaunch;

import io.unlaunch.exceptions.UnlaunchHttpException;
import java.util.Date;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Unlaunch wrapper for the  javax.ws.rs classes that we use to make requests to our backend.
 *
 * @author umermansoor
 * @author fahmina
 */
public final class UnlaunchRestWrapper {

    private final Client client;
    private final WebTarget apiWebTarget;
    private final Invocation.Builder invocationBuilder;
    private Date lastModified;
    
    private static final Logger logger = LoggerFactory.getLogger(UnlaunchRestWrapper.class);

    UnlaunchRestWrapper(String sdkKey, String host, String apiPath,  long connectionTimeOutMs, long readTimeOutMs) {
        ClientConfig configuration = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        configuration.property(ClientProperties.CONNECT_TIMEOUT, (int)connectionTimeOutMs);
        configuration.property(ClientProperties.READ_TIMEOUT, (int)readTimeOutMs);
        client = ClientBuilder.newClient(configuration);

        apiWebTarget = client.target(host).path(apiPath);
        invocationBuilder = apiWebTarget.request(MediaType.APPLICATION_JSON).header("X-Api-Key", sdkKey);
    }

    public static UnlaunchRestWrapper create(
            String sdkKey, String host, String apiPath, long connectionTimeOutMs, long readTimeOutMs) {
        return new UnlaunchRestWrapper(sdkKey, host, apiPath, connectionTimeOutMs, readTimeOutMs);
    }

    /**
     * Invoke HTTP POST method for the current request synchronously.
     * @param entity
     * @return {@link Response} response body
     */
    public Response post(Entity<?> entity) {
        try {
            return invocationBuilder.post(entity);
        } catch ( ProcessingException  | WebApplicationException ex) {
            logger.warn("unable to perform HTTP POST action on URL {} for entity {}. Error was {}",
                    apiWebTarget.getUri(), entity, ex.toString());
            throw new UnlaunchHttpException("unable to perform post action on entity", ex);
        }
    }

    public String get(Class<String> responseType) {
        try {
            Response response = invocationBuilder.header("If-Modified-Since", lastModified).get();
            lastModified = response.getLastModified();
            return response.readEntity(responseType);
        } catch ( ProcessingException  | WebApplicationException ex) {
            logger.warn("unable to perform HTTP GET action on URL {}. Error was: {}", apiWebTarget.getUri(),
                    ex.getMessage());
            throw new UnlaunchHttpException("unable to perform get action on entity", ex);
        }
    }

    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "UnlaunchRestWrapper for URI: " + apiWebTarget.getUri();
    }
}
