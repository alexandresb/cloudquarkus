package org.cesi.chapter07.boundary;

import org.cesi.chapter07.entity.Customer;
import org.cesi.chapter07.entity.Order;
import org.cesi.chapter07.integration.CustomerException;
import org.cesi.chapter07.integration.CustomerRepository;
import org.cesi.chapter07.integration.OrderRepository;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * le mapping Java / JSON est réaalisé par défaut par JSON-B
 */
@Path("orders")
@ApplicationScoped//optionnel car par défaut toute ressource REST est un bean CDI avec un scope Singleton
@Produces("application/json")
@Consumes("application/json")
@RolesAllowed({"admin","user"})
public class OrderEndpoint {
    @Inject
    private OrderRepository orderRepository;
    @Inject
    private CustomerRepository customerRepository;

    //les utilisateurs user et admin peuvent accéder aux méthodes GET et POST
    @GET
    public List<Order> getAll(@QueryParam("customerId") Long customerId){
        return orderRepository.findAll(customerId);
    }

    @POST
    @Path("/{customer}")//URI relative correpondant à l'id du Customer
    public Response create(Order order, @PathParam("customer") Long customerId){
        //bloc try catch non présent dans le code source du livre - l'excep web était levée dans le repo
        Customer customer=null;
        try {
            customer = customerRepository.findCustomerById(customerId);
        }catch(CustomerException e) {
            throw new WebApplicationException("Customer id="+customerId+" not found",404);
        }
        orderRepository.createOrder(order,customer);

        return Response.status(201).build(); //created
    }
    //seuls les admin peuvent modifier ou supprimer une commande
    @PUT
    @RolesAllowed("admin")
    public Response update(Order order){
        try{
            //updateOrder invoque findOrderById qui lève l'exception si l'id de l'Order passé ne correspond pas à un Order en base.
            orderRepository.updateOrder(order);
        }catch(CustomerException e){
            //on utilise CustomerException même quand un Order n'est pas trouvé en base
            throw new WebApplicationException("Order id="+order.getId()+" not found",404);
        }
        return Response.status(204).build(); //No Content
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/{order}")
    public Response delete(@PathParam("order") Long orderId){
        orderRepository.deleteOrder(orderId);
        return Response.status(204).build(); //No Content
    }

}
