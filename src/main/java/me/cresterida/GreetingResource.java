package me.cresterida;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String hello() {

        MyEntity myEntity = new MyEntity();
        myEntity.name = "Hello";
        myEntity.persist();

        return "Hello from Quarkus REST";
    }
}
