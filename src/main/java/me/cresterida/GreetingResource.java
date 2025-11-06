package me.cresterida;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private Logger LOGGER= Logger.getLogger(GreetingResource.class.getName());
    @Inject
    EntityManager em;
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional

    public String hello() {
        MyEntity myEntity = new MyEntity();
        myEntity.name = "Hello";
        myEntity.persist();
        LOGGER.info("Persisted entity with ID: " + myEntity.id);
        return "Hello from Quarkus REST";
    }
}
