package org.cesi.chapter09.integration;

import io.quarkus.vertx.ConsumeEvent;
import org.cesi.chapter09.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerService {
    //logger pour suivre la gestion des threads managés par Quarkus
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    méthode handler engregistrée à l'adresse callcustomer
    consomme les messages envoyés à l'adresse indiqué
    comme la méthode à 1 type de retour, elle répond à l'envoyeur en lui envoyant 1 message par l'intermédiaire du bus.
    (en coulisse l'extension Quarkus intercepte le retour et l'empbarque dans Message.reply())
    - dans le cas du retour d'un type T (par exemple String) la méthode est non bloquante.
      Cependant l'envoi et la consommation sont asynchrones.
    - Il faut utiliser CompletionStage<T> (ou Uni<T> - Mutiny) comme type de retour, si on veut mettre en oeuvre
      l'asynchronisme au sein de la méthode consommatrice dans le cas d'une réponse à l'envoyeur.
    cf. https://quarkus.io/guides/reactive-event-bus#consuming-events
    // pour en savoir plus sur la diff entre non-blocking vs Asynchronous : https://stackoverflow.com/questions/2625493/asynchronous-vs-non-blocking
     */
    @ConsumeEvent("callcustomer")
    public String reply(Customer c){
        logger.info(" exécution de reply  - consommation du message");
        return "Hello! I am " + c.getName() + " "
                +c.getSurname() + ". How are you doing?";
    }
}
