package org.cesi.chapter09.boundary;

import io.vertx.axle.core.Vertx;
import org.cesi.chapter09.integration.CustomerRepository;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/streaming")
public class StreamingEndpoint {

    @Inject
    Vertx vertx;

    @Inject
    CustomerRepository customerRepository;

    //emission asynchrone d'events toutes les 2 sec
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> stream(){
        return ReactiveStreams //pt d'entrée dans l'API Reactive Streams d'Eclipse MicroProfile
                .fromPublisher(vertx.periodicStream(2000).toPublisher())//création d'un PublisherBuilder pour pousser des données toutes les 2 sec
                        .map(//permet de définir une fonction mapper pour formater  la chaine émise par le publisher
                               //définition de l'event Emis
                                l->String.format("Number of Customers : %s. Last One Added : %s %n",
                                        customerRepository.findAll().size(), //nb total customers
                                        customerRepository.findAll().size() > 0 ? // s'il y a des customers
                                                //on retourne le dernier : sinon "N/A"
                                                customerRepository.findAll().get(customerRepository.findAll().size() - 1).toString():"N/A"
                                        )//fin format
                        ).buildRs(); //création de l'instance Publisher<String> qui va émettre ses données /items toutes les 2 sec

    }
}
