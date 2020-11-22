package org.cesi.chapter10.processor;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.cesi.chapter10.entity.Company;
import org.cesi.chapter10.entity.Operation;
import org.cesi.chapter10.entity.Quote;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.HashMap;
import java.util.Random;

/*
reactive stream processor
 */

@ApplicationScoped
public class QuoteConverter {

    private HashMap<String, Double> quotes;
    private Random random = new Random();

    /*
    initialise la map des cotations avec des valeurs aléatoires
     */
    @PostConstruct
    void init(){
        quotes = new HashMap<>();
        //pour chaque compagnie de l'énumération on crée une entrée dans la map
        for(Company company : Company.values() ){
            quotes.put(company.name(),Double.valueOf(random.nextInt(100)+50));
        }
    }

    /*
     méthode Reactive Streams Processor
     Consomme les messages (embarquant des opérations de trading)  fournis par ActiveMQ.
     Produit des payloads représentant la cotation dans la channel in-memory-stock-quote.
     Méthode invoquée chaque fois qu'un message est disponible dans le broker

     */
    @Incoming("in-operation")//consommation des messages du canal entrant
    @Outgoing("in-memory-stock-quote") //ecriture dans un canal de sortie
    //fonction expérimentale
    @Broadcast //dispatche les messages (payloads) produits à tous les suscribers abonnés au canal in-memory-stock-quote
    public String createNewQuote(String payload){
        Jsonb jsonb = JsonbBuilder.create();//instanciation du moteur JSON-B
        Operation operation = jsonb.fromJson(payload,Operation.class);//mapping payload JSon -> instance Operation
        //récupération du nom de l'entreprise pour laquelle une op a été effectuée
        String companyName = operation.getCompany().name();
       // récupération de la valeur actuelle de la cotation de l'entreprise pour laquelle une op a été reçu
        Double currentQuoteValue = quotes.get(companyName);
        //calcul de la valeur de change de l'opération = 1/4 du montant de l'opération
        Double change =(double) (operation.getAmount()/25);
        Double newQuoteValue=null; //représente la nouvelle cotation après traitement

        if(operation.getType()==Operation.BUY){//si on a reçu une op d'achat
            newQuoteValue = currentQuoteValue+change; //la val de la cotation est augmenté de la val de change
        }else{//si c'est une opération de vente
            newQuoteValue = currentQuoteValue - change;// la nouvelle cotation a sa valeur diminuée
        }
        if(newQuoteValue<0) newQuoteValue = 0.0d;

        //on place dans la la nouvelle valeur de la cotation commerciale pour l'entreprise
        quotes.replace(companyName,newQuoteValue);

        //Création de la cotation commerciale
        Quote quote = new Quote(operation.getCompany(),newQuoteValue);
        //publication de la cotation au format JSON dans in-memory-channel
        return jsonb.toJson(quote); //le retour permet aussi aussi au framework d'acquitter le message consommé
    }
}
