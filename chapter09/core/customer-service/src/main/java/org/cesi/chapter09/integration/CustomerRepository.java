package org.cesi.chapter09.integration;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.cesi.chapter09.entity.Customer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@ApplicationScoped
public class CustomerRepository {
    //injection d'une instance managée Vertx - coeur d'une application Vert.x
    @Inject
    Vertx vertx;

    @ConfigProperty(name = "file.path")
    String path;

    //logger pour suivre la gestion des threads managés par Quarkus
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Customer> customerList = new ArrayList<>();
    private int counter;

    //s'exécute dans le même thread que l'endpoint appelant
    public CompletionStage<String> writeFile(){
        logger.info("--exécution de la méthode writeFile");
        //Future pouvant être explicitement terminé
        //implémente CompletionStage
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        //création d'un tableau json contenant la liste des customers
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(Customer c: customerList){
            //pour chaque entrée de la liste, on ajoute
            // un objet json représentant le customer
            arrayBuilder.add(Json.createObjectBuilder()
                                    .add("id",c.getId())
                                    .add("name",c.getName())
                                    .add("surname",c.getSurname()).build());
            }
        JsonArray array = arrayBuilder.build();

        //utilisation de Vert.x pour écrire de manière non bloquante /asyncrhone dans le fic.
        //writeFile créé le fichier et écrit les données du Buffer (Vert.x) dans le fichier spécifié par le path
        //de manière asynchrone.
        //Lorsque writeFile est invoqué, un thread est mis à disposition (assigné) par Vert.x pour éxécuter l'op d'écriture
        vertx.fileSystem().writeFile(path, Buffer.buffer(array.toString()),
                asyncResult->{//définition d'un handler Handler<AsyncResult> exécuté quand l'écriture asynchrone a fini
                //définition d'un Handler Vert.x prenant en charge un AsyncResult Vert.x
                //le handler s'exécute de manière asynchrone dans un thread géré par Vert.x

                logger.info("----exécution du handler d'écriture");

                if(asyncResult.succeeded()){ //si l'op d'écriture à réussi
                        //on écrit le message suivant dans le CompletableFuture qui sera retourné par la méthode writeFile
                        //complete termine CompletableFuture ce qui libère le thread.
                        completableFuture.complete("données JSON écrite dans le fichier "+path);
                    }else{//sinon on journalise la cause de l'échec.
                        completableFuture.complete("erreur");//ce n'est pas dans le livre
                        System.out.println("erreur pendant l'écriture : "+asyncResult.cause().getMessage());
                }
                }//fin def handler
        );//fin writeFile
        return completableFuture;
    }
    // lecture dans le fichier customer.json après 1 sec
    public CompletionStage<String> readFile(){
        logger.info("--exécution de readFile");

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        long now = System.nanoTime(); //instant t en nanosecondes
        //déclenchement de la tâche après 1 seconde
        //création d'un callback/handler temporel Handler<Long> qui sera déclenché 1 seule fois dans 1 sec
        vertx.setTimer(100, l->{// 100 ms= 1sec :délai après lequel le callback temporel est déclenché
            //calcul du temps passé quand le timer se déclenche
            long duration = MILLISECONDS.convert(System.nanoTime()-now, NANOSECONDS);
            System.out.println("temps écoulé entre le début de la méthode et déclenchement du timer :"+duration);

            //lecture dans le fichier dans un thread "dédié"
            vertx.fileSystem().readFile(path, ar->{//def d'un handler Handler<AsyncResult> s''exécutant une fois la lecture terminée

                logger.info("----exécution du handler de lecture");

                if(ar.succeeded()){//si l'op de lecture a réussi
                    //lecture du contenu de l'AsyncResult au format UTF-8
                    String response = ar.result().toString("UTF-8");
                    completableFuture.complete(response); //assignation du contenu dans le CompletableFuture
                }else{
                    completableFuture.complete("erreur de lecture : "+ar.cause().getMessage());
                }
            }//fin du handler "de lecture"
            );//fin readFile

                }//fin def handler temporel
        );
        return completableFuture;
    }

    /**retourne la valeur et incrémente
    la première valeur retournée est donc 0
     */
    public int getNextCustomerId(){ return counter++; }

    public List<Customer> findAll(){
        return customerList;
    }

    public Customer findCustomerById(Integer id){
        for(Customer c : customerList){
            if(c.getId().equals(id)){
                return c;
            }
        }
        throw new CustomerException("customer not found");
    }

    public void updateCustomer(Customer customer){
        Customer customerToUpdate = findCustomerById(customer.getId());
        customerToUpdate.setName(customer.getName());
        customerToUpdate.setSurname(customer.getSurname());
    }

    public void createCustomer(Customer customer){
        customer.setId(getNextCustomerId());
        findAll().add(customer);
    }

    public void deleteCustomer(Integer customerId){
        Customer c= findCustomerById(customerId);
        findAll().remove(c);
    }


}
