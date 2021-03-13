package io.unlaunch;

import io.unlaunch.exceptions.UnlaunchHttpException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

public class UnlaunchGenericRestWrapper {
    private final Client client;
    private final WebTarget apiWebTarget;

    private static final Logger logger = LoggerFactory.getLogger(UnlaunchRestWrapper.class);

    UnlaunchGenericRestWrapper(String host, String path,  long connectionTimeoutMs, long readTimeoutMs) {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, (int)connectionTimeoutMs);
        configuration.property(ClientProperties.READ_TIMEOUT, (int)readTimeoutMs);
        client = ClientBuilder.newClient(configuration);

        apiWebTarget = client.target(host).path(path);
    }

    public static UnlaunchGenericRestWrapper create(
           String host, String apiPath, long connectionTimeoutMs, long readTimeoutMs) {
        return new UnlaunchGenericRestWrapper(host, apiPath, connectionTimeoutMs, readTimeoutMs);
    }



    public Response get() {
        try {
            Invocation.Builder invocationBuilder = apiWebTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();


            return response;
        } catch ( ProcessingException  | WebApplicationException ex) {
            logger.warn("unable to perform HTTP GET action on URL {}. Error was: {}", apiWebTarget.getUri(),
                    ex.getMessage());
            throw new UnlaunchHttpException("unable to perform get action on entity", ex);
        }
    }

    @Override
    public String toString() {
        return "UnlaunchRestWrapper for URI: " + apiWebTarget.getUri();
    }
}
