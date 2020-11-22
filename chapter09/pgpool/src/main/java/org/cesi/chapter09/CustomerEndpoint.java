package org.cesi.chapter09;

import io.vertx.axle.pgclient.PgPool;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerEndpoint {

    @Inject
    PgPool client;

    /**
     * thenComppose prend en arg une fonction qui prend en arg le CompletionStage
     * depuis lequel thenCompose est invoqué
     * exécution  dans un thread séparées de requêtes successives permettant de créer et d'alimenter
     * la table CUSTOMER
     *
     */
    @PostConstruct
    void init(){
        //éxécutions de requêtes dans un thread séparé
        client.query("DROP TABLE IF EXISTS CUSTOMER")
                //exécution de la requête dans un thread différent du thread principal
                .execute()//retourne un completionStage
                //exécution successives de requêtes
                .thenCompose(r->client.query("CREATE SEQUENCE IF NOT EXISTS  customerId_seq").execute())
                .thenCompose(r -> client.query("CREATE TABLE CUSTOMER (id SERIAL PRIMARY KEY, name TEXT NOT NULL,surname TEXT NOT NULL)").execute())
                .thenCompose(r -> client.query("INSERT INTO CUSTOMER (id, name, surname) VALUES ( nextval('customerId_seq'), 'John','Doe')").execute())
                .thenCompose(r -> client.query("INSERT INTO CUSTOMER (id, name, surname) VALUES ( nextval('customerId_seq'), 'Fred','Smith')").execute())
                .toCompletableFuture()
                //bloque jusquà ce que toutes les requêtes se sont exécutées
                // le blocage permet d'éviter d'invoquer le endpoint avant que la base soit prête.
                //retourne le résultat de l'exécution successive
                .join();
    }
    /*
    le thread de requête est libéré avant que la réponse http soit construite et retournée
    quand la liste est retournée, la réponse est créée puis un thread de requête est
    assigné pour retourner la réponse au client / l'appelant
     */
    @GET
    public CompletionStage<Response> findAll(){
        return Customer.findAll(client)
                //quand la récupération de la liste a fini de s'exécutée on construit la réponse
                .thenApply(Response::ok) //<=> Response.ok(List<Customer> listeResulatDuCompletionStagePrecedent)
                .thenApply(Response.ResponseBuilder::build);

    }

    /*
    ces 3 méthodes lance des tâches asynchrones, libèrent le thread de requête qui leur ai assigné.
    Lorsque la tâche (série de tâches) async est terminé un thread est assigné pour retourner au client web la réponse
     */

    //le corps de la réponse contient l'id du customer créé
    @POST
    public CompletionStage<Response> create(Customer customer){//désérialisation du corps json en objet de type Customer
        return customer.create(client) //lorque l'id du customer inséré est retourné / disponible
                .thenApply(Response::ok)//on crée la réponse avec le statut OK - <=> Response.ok(id)
                .thenApply(Response.ResponseBuilder::build);//retourne un CompletionStage contenant la réponse construite
    }

    @PUT
    public CompletionStage<Response> update(Customer customer){
        return customer.update(client)
                    //on définit le statut de la réponse - pas de corps de réponse
                    .thenApply(updated-> updated ? Response.Status.OK:Response.Status.NOT_FOUND) //dans le livre OK au lieu de NO_CONTENT
                    //retour d'un CompletionStage embarquant la réponse sans corps
                    .thenApply(status -> Response.status(status).build());
    }

    @DELETE
    public CompletionStage<Response> delete(@QueryParam("id") Long id){
        return Customer.delete(client,id)
                        .thenApply(deleted->deleted ? Response.Status.NO_CONTENT:Response.Status.NOT_FOUND)
                        .thenApply(status -> Response.status(status).build());
    }

}
