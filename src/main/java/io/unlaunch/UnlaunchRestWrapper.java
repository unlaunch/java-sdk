package io.unlaunch;

import io.unlaunch.exceptions.UnlaunchHttpException;
import java.util.Date;
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
 * This was created mainly to make testing easier.
 *
 * @author umermansoor
 * @author fahmina
 */
public final class UnlaunchRestWrapper {

    private final Client client = ClientBuilder.newClient();
    private final WebTarget apiWebTarget;
    private final Invocation.Builder invocationBuilder;
    private Date lastModified;
    
    private static final Logger logger = LoggerFactory.getLogger(UnlaunchRestWrapper.class);

    UnlaunchRestWrapper(String sdkKey, String host, String apiPath) {
        apiWebTarget = client.target(host).path(apiPath);
        invocationBuilder = apiWebTarget.request(MediaType.APPLICATION_JSON).header("X-Api-Key", sdkKey);
    }

    public static UnlaunchRestWrapper create(String sdkKey, String host, String apiPath) {
        return new UnlaunchRestWrapper(sdkKey, host, apiPath);
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
            logger.warn("unable to perform post action on entity {}", entity);
            throw new UnlaunchHttpException("unable to perform post action on entity", ex);
        }
    }

    public String get(Class<String> responseType) {
        try {
            Response response = invocationBuilder.header("If-Modified-Since", lastModified).get();
            lastModified = response.getLastModified();
            return response.readEntity(responseType);
        } catch ( ProcessingException  | WebApplicationException ex) {
            logger.warn("unable to perform get action on URL {}. Error was: {}", apiWebTarget.getUri(), ex.getMessage());
            throw new UnlaunchHttpException("unable to perform post action on entity", ex);
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
