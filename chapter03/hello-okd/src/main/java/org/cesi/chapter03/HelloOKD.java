package org.cesi.chapter03;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/*
considéré par défaut comme un bean CDI avec un scope Singeleton.
cf. https://quarkus.io/guides/rest-json#lifecycle-of-resources
 */
@Path("/containerId")
public class HelloOKD {

    @Inject
    private ContainerService containerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "service s'exécutant dans "+containerService.getContainerId();
    }
}