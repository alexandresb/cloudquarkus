package org.cesi.chapter06.boundary;

import org.cesi.chapter06.integration.Customer;
import org.cesi.chapter06.integration.CustomerEndpointItf;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/customersclient")
@Produces("application/json")
@Consumes("application/json")
public class CustomerClientService {
    @Inject
    @RestClient //injection d'un client REST annoté @RegisterRestClient
    CustomerEndpointItf customerEndpoint; //private fonctionne mais Quarkus conseille d'utiliser un niveau package

    @GET
    public List<Customer> getAll() {
        return customerEndpoint.getAllCustomers();
    }


    @POST
    public Response create(Customer c) {
        return customerEndpoint.createNewCustomer(c);
    }


    @PUT
    public Response update(Customer c) {
        return customerEndpoint.updateCustomer(c);
    }
    /*
    le code source original utilise @QueryParam
    le code de l'UI web a été mis à jour pour exécuter une requête DELETE matchant avec cette méthode
     */
    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long customerId) {
        return customerEndpoint.deleteCustomer(customerId);
    }
}