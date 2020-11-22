package org.cesi.chapter10.source;

import io.smallrye.mutiny.Multi;
import org.cesi.chapter10.entity.Company;
import org.cesi.chapter10.entity.Operation;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped // ou @Dependent
public class OperationGenerator {

    Random random = new Random();//thread safe

    /*
    méthode retournant un Publisher source de flux publiant des payloads dans la channel stock-quote.
    C'est donc une méthode générant un flux.
    Invoquée une seule fois lors de la création / l'assemblage de la reactive streams lors du déploiement de l'appli
    Cette méthode est un Reactive Streams Publisher
    Note : nextInt(n) - tirage au sort d'un entier entre [0 n[
     */
    @Outgoing("out-operation")
    public Multi<String> generate(){
        //création d'une source de stream publiant toutes les 2 seconde une opération dans stock-quote
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                //configuration du comportement de back-pressure
                .onOverflow().drop()//si le consommateur (ici ActiveMQ) est débordé par l'arrivée des messages, alors le message est supprimé
                .map(tick->generateOperation(random.nextInt(2), random.nextInt(5), random.nextInt(100)));
    }

    //type = SELL or BUY
    private String generateOperation(int type, int company, int amount){
        //obtention d'une instance du moteur Jsonb
        Jsonb jsonb = JsonbBuilder.create();
        //création d'une op
        Operation operation = new Operation(type, Company.values()[company],amount);
        //retour d'une payload de message au format JSON
        return jsonb.toJson(operation);


    }
}
