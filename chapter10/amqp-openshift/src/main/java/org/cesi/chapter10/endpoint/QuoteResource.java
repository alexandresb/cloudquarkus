package org.cesi.chapter10.endpoint;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
/*
Ce bean récupère le flux géré au sein de notre système de messagerie réactive
 */
@ApplicationScoped
@Path("/quotes")
public class QuoteResource {//QuoteEndpoint dans le livre
    //injection d'un publisher associé à in-memory-channel pour publier les cotations reçues depuis le canal
    //cet éditeur, alimenté par le canal in-memory-channel, fournit un flux d'events au clients.
    @Inject
    @Channel("in-memory-stock-quote")
    Publisher<String> quoteStream; //quote dans le livre

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)//le type de contenu fourni en réponse est un flux SSE
    @SseElementType("text/plain")//au format "texte"
    public Publisher<String> stream(){
        return quoteStream;//envoi des evmnts au client
    }
}
