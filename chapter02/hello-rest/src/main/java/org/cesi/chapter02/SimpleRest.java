package org.cesi.chapter02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/helloworld")
public class SimpleRest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello World";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{name}")
    public String hello(@PathParam("name") String name){
        log.info("appel avec "+name);
        return "hello "+name;
    }
}