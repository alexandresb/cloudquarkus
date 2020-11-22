package org.cesi.chapter09.boundary;

import io.vertx.axle.core.eventbus.EventBus;
import io.vertx.axle.core.eventbus.Message;
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
    //injection d'une instance EventBus // 1 seule instance par Application
    @Inject
    EventBus eventBus;

    @Inject
    private CustomerRepository customerRepository;

    //logger pour suivre la gestion des threads managés par Quarkus
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //méthode asynchrone envoyant un message à 1 consommateur via un bus selon le pattern request-response (peer-to-peer avec réponse retournée)
    //cf. https://vertx.io/docs/vertx-core/java/#event_bus
    /*
    le thread dans lequel s'exécute la méthode est libéré avant que le traitement métier asynchrone (envoi d'un message et réponse) soit terminé.
    Lorsque le traitement métier asynchrone est terminé ( lorsque le message de réponse est embarqué dans un CompletionStage), alors un thread de requête est assigné pour
    retourner la réponse au client web.
    le thread principal de requête n'est donc pas bloqué durant le traitement métier et peut donc servir entretemps une autre requête http entrante
    cf.http://heidloff.net/article/developing-reactive-rest-apis-with-quarkus/
     */
    @GET
    @Path("/call")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> call(@QueryParam("id") Integer CustomerId){
        logger.info("exécution de call - envoi d'1 message dans un bus");
        //messaging de type request-response:
        CompletionStage<String> customerMessage = eventBus.
                //le message, avec comme body le customer retourné, est envoyé à l'adresse callcustomer
                 //request définit un modèle peer-to-peer : un seul handler enregistré à l'adresse indiquée  peut consommer le message.
                 //le message sera routé vers un seul handler enregistré à cette adresse. Si plusieurs handlers sont enregisrés à la même adresse,
                // Vert.x utilisera un round robin non stricte pour en choisir 1 seul.
                //eventBus.request retourne un objet de typeCompletionStage<Message<String>> correspondant à la tâche d'envoi et qui contiendra la réponse
                //renvoyé via le bus par le consommateur.
                <String>request("callcustomer",customerRepository.findCustomerById(CustomerId))
                //thenApply prend en argument une fonction qui reçoit le message de réponse (comme argument) et le traite.
                // Ici, le traitement consiste à extraire le corps du message de réponse. Ce retour de la fonction est embarqqué dans le CompletionStage
                //retourné par thenApply : thenApply(fn(msg de réponse) =>corps)=>CompletionStage(corps)
                //thenApply s'exécute quand le message de réponse est dispo / arrivé et a été assigné au CompletionStage sur lequel thenApply est invoqué
                //note : le message de réponse transite bien évidemment par le bus.
               .thenApply(replyMessage->{
                   //<=> thenApply(Message::body) excepté le log d'info
                   logger.info("thenApply- extraction du corps du message de réponse");
                   return replyMessage.body();
               })
                .thenApply(message->{//pas dans le livre - le corps est mis en MAJ
                    logger.info("thenApply- corps du message converti en MAJ");
                   return message.toUpperCase();
                })
               .exceptionally(Throwable::getMessage);
        logger.info("fin de l'exécution de call - fin d'exécution du thread principal");
        return customerMessage;
    }

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
    @Produces(MediaType.TEXT_PLAIN)
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
