package org.cesi.chapter06.integration;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@RegisterRestClient // le client REST sera dispo pour l'injection
@Path("orders")
@Produces("application/json")
@Consumes("application/json")
public interface OrderEndpointItf {
    @GET
    public List<Order> getAllOrders(@QueryParam("customerId") Long customerId);

    @POST
    @Path("/{customer}")//URI relative correpondant à l'id du Customer
    public Response createOrder(Order order, @PathParam("customer") Long customerId);

    @PUT
    public Response updateOrder(Order order);

    @DELETE
    @Path("/{order}")
    public Response deleteOrder(@PathParam("order") Long orderId);
}
