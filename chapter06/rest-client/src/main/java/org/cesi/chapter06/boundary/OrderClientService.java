package org.cesi.chapter06.boundary;

import org.cesi.chapter06.integration.Order;
import org.cesi.chapter06.integration.OrderEndpointItf;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/*
par défaut dans Quarkus tout point de terminaison REST a un scope Singleton.
Le composant supporte donc l'injection
 */

@Path("/ordersclient")
@Produces("application/json")
@Consumes("application/json")
public class OrderClientService {

    @Inject @RestClient
    OrderEndpointItf orderEndpoint;

    @GET
    public List<Order> getAllOrders(@QueryParam("customerId") Long id){
        return orderEndpoint.getAllOrders(id);
    }

    @POST
    @Path("/{customer}")//URI relative correpondant à l'id du Customer
    public Response create(Order order, @PathParam("customer") Long customerId){
        return orderEndpoint.createOrder(order,customerId);
    }

    @PUT
    public Response update(Order order){
        return orderEndpoint.updateOrder(order);
    }

    @DELETE
    @Path("/{order}")
    public Response delete(@PathParam("order") Long orderId){
        return orderEndpoint.deleteOrder(orderId);
    }

}
