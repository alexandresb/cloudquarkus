package org.cesi.chapter09.boundary;

import org.cesi.chapter09.entity.Customer;
import org.cesi.chapter09.integration.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletionStage;

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

    //logger pour suivre la gestion des threads managés par Quarkus
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //méthode déclenchant l'écriture dans un fichier - le GET est ici mal utilisé car il y a création de ressource
    //c'est juste pour le test
    @GET
    @Path("/writefile")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> writeFile(){
        logger.info("exécution de GET writeFile");
        return customerRepository.writeFile();
    }

    @GET
    @Path("/readfile")
    public CompletionStage<String> readFile(){
        logger.info("exécution de GET readFile");
        return customerRepository.readFile();
    }


    @GET
    public List<Customer> getAll(){
        return customerRepository.findAll();
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
    public Response delete(@QueryParam("id") Integer customerId){
        customerRepository.deleteCustomer(customerId);
        return Response.status(204).build(); //No Content
    }

}
