package org.cesi.chapter06.boundary;

import org.cesi.chapter06.entity.Customer;
import org.cesi.chapter06.integration.CustomerRepository;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * le mapping Java / JSON est réaalisé par défaut par JSON-B
 */
@Path("/customers")
@ApplicationScoped//optionnel car par défaut toute ressource REST est un bean CDI avec un scope Singleton
@Produces("application/json")
@Consumes("application/json")
public class CustomerEndpoint {
    @Inject
    private CustomerRepository customerRepository;

    @GET
    public List<Customer> getAll(){
        return customerRepository.findAll();
    }

    //non présente dans le livre - pour tester le cache via @Cacheable
    @GET
    @Path("/{customerId}")
    public Customer findCustomer(@PathParam("customerId") Long id){
        return customerRepository.findCustomerById(id);
    }

    @POST
    public Response createCustomer(Customer customer){
        customerRepository.createCustomer(customer);
        return Response.status(201).build(); //created
    }
    @PUT
    public Response updateCustomer(Customer customer){
        customerRepository.updateCustomer(customer);
        return Response.status(204).build(); //No Content
    }

    @DELETE
    public Response delete(@QueryParam("id") Long customerId){
        customerRepository.deleteCustomer(customerId);
        return Response.status(204).build(); //No Content
    }

}
