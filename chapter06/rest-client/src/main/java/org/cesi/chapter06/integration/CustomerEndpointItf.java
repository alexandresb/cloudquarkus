package org.cesi.chapter06.integration;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * cette interface sera implémentée par un proxy généré par Quarkus
 */
@RegisterRestClient // permet de déclarer une interface déclarative pour invoquer un service backend REST
// permettra d'injecter l'interface dans un composant client
@Path("/customers")//uri de base du point de terminaison à invoquer
@Produces("application/json")
@Consumes("application/json")
public interface CustomerEndpointItf {
    // la définition des méthodes permet de déclarer les requêtes générées par ce client REST
    @GET
    public List<Customer> getAllCustomers();

    @POST
    public Response createNewCustomer(Customer customer);

    @PUT
    public Response updateCustomer(Customer customer);

    /*
    le code source du livre a oublié @QueryParam
    //ici une requête HTTP DELETE <url service distant>/customers?id=x sera générée
     */
    @DELETE
    public Response deleteCustomer(@QueryParam("id") Long customerId);

}
