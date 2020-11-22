package org.cesi.chapter07.boundary;

import io.quarkus.security.identity.SecurityIdentity;
import org.cesi.chapter07.entity.Customer;
import org.cesi.chapter07.integration.CustomerRepository;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonString;//JSON-P
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * le mapping Java / JSON est réaalisé par défaut par JSON-B
 */
@Path("/customers")
@ApplicationScoped//optionnel car par défaut toute ressource REST est un bean CDI avec un scope Singleton
@Produces("application/json")
@Consumes("application/json")
@RolesAllowed({"admin","user"})
public class CustomerEndpoint {
    private static final Logger LOGGER = Logger.getLogger(CustomerEndpoint.class.getName());
    @Inject
    private CustomerRepository customerRepository;
    //représente l'utilisateur authentifié
    @Inject
    SecurityIdentity securityIdentity;

    //Eclipse Microprofile JWT
    //injection du token
    @Inject
    JsonWebToken jwt; //permet d'accéder  à toutes les infos du claim
    //injection du nom de l'utilisateur stocké dans le claim du token
    @Inject
    @Claim(standard = Claims.preferred_username)  //on utilise spécifie un attribut JWT standard
    Optional<JsonString> username;
    //injection des groupes auxquels appartient l'utilisateur authentifié
    @Inject
    @Claim(standard = Claims.groups)  //on utilise spécifie un attribut JWT standard
    Optional<JsonString> groups;

    //les user et admin peut lister tous les customers, un customer spécifique et créer un customers
    @GET
    public List<Customer> getAll(){
        LOGGER.info("loggué en tant que :"+securityIdentity.getPrincipal().getName());
        //obtention d'un itérateur pour parcourir la liste des rôles de l'utilisateur
        Iterator<String> roles = securityIdentity.getRoles().iterator();
        while(roles.hasNext()){
            LOGGER.info("Role : "+roles.next());
        }
        //log des infos JWT
        LOGGER.info("contenu du token JWT : " +jwt.toString());
        //log de l'utilisateur authentifié - Optional.get permet de retourner la valeur embarquée dans Optional
        LOGGER.info("valeur du claim preferred username stocké dans le token JWT : " +username.get());
        //log des groupes de l'utilisateur authentifié
        LOGGER.info("valeur du claim groups stocké dans le token JWT : " +groups.get());
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
    @RolesAllowed("admin") //seuls les admin peuvent modifier un customer
    @PUT
    public Response updateCustomer(Customer customer){
        customerRepository.updateCustomer(customer);
        return Response.status(204).build(); //No Content
    }

    @RolesAllowed("admin") //seuls les admin peuvent modifier un customer
    @DELETE
    public Response delete(@QueryParam("id") Long customerId){
        customerRepository.deleteCustomer(customerId);
        return Response.status(204).build(); //No Content
    }

}
