package org.cesi.chapter09.integration;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import org.cesi.chapter09.entity.Customer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

        //entête pour le fichier csv
        StringBuffer sb = new StringBuffer("id,name,surname");
        sb.append("\n");

        //Création d'1 Observable (implémente ObservableSource) émettant pour chaque élément c de customerList une chaine compatible csv
        //fromIterable retourne un cold Observable émettant séquenciellement chaque item (Customer) de CustomerList(Iterable)
        //map permet d'appliquer une transformation (ici "transformation csv") pour chaque items (Customer)
        // émit par l'Observable (ObservableSource) - map retourne 1 Observable
        // cf. http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html
        Observable.fromIterable(customerList).map(c->c.getId() + "," + c.getName() + "," + c.getSurname() + "\n")
                //les méthodes de l'observateur s'exécuteront de façon asynchrone dans un même thread managé par l'ordonnanceur passé en arg
                   .subscribeOn(Schedulers.io())
                    .subscribe(//définition de l'observateur réceptionnant les items émis en séquence par l'Observable
                            //plus spécifiquement on utilise une version de subscribe pour laquelle on déclare des callbacks
                            //réagissant à l'arrivée d'un item, à une erreur ou à la fin de l'émission
                            item->{//définition de onNext()
                                //pour chaque event/item reçu
                                logger.info("---item reçu (en vue d'écriture)- exécution onNext");
                                sb.append(item);//on stocke l'item dans le buffer
                            },
                            error-> System.err.println(error),//onError(Thorwable) en cas d'erreur lors de l'émission
                            ()->{//onComplete() déclenchée quand la séquence d'émission est finie (quand l'Observable a fini d'émettre
                                logger.info("---écriture dans le fichier csv - exécution onComplete");
                                //le code ci-dessous est "similaire à celui de core/customer-service
                                vertx.fileSystem().writeFile(path, Buffer.buffer(sb.toString()),
                                        asyncResult->{//définition d'un handler Handler<AsyncResult> exécuté quand l'écriture asynchrone a fini

                                            logger.info("----exécution du handler d'écriture");

                                            if(asyncResult.succeeded()){ //si l'op d'écriture à réussi
                                                //on écrit le message suivant dans le CompletableFuture qui sera retourné par la méthode writeFile
                                                completableFuture.complete("données JSON écrite dans le fichier "+path);
                                            }else{//sinon on journalise la cause de l'échec.
                                                completableFuture.complete("erreur");//ce n'est pas dans le livre
                                                System.out.println("erreur pendant l'écriture : "+asyncResult.cause().getMessage());
                                            }
                                        }//fin def handler
                                );//fin writeFile
                            }
                    );//fin subscribe
        logger.info("-- fin d'exécution de la méthode writeFile");
        return completableFuture;
    }

    // lecture dans le fichier customer.csv
    //utilisations de Cold Observables (car abonnement via subscribe - si abo via publish, alors utilisation d'hot Observable
    public CompletionStage<String> readFile(){
        logger.info("--exécution de readFile");

        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        StringBuffer sb = new StringBuffer(); //tampon pour stocker les données lues dans le fichier

            //Single<=>Observable eméttant une seule valeur
            vertx.fileSystem().rxReadFile(path) //lecture ASYNCHRONE du fichier" - retourne un Single<Buffer>
                    //applications de transformation sur le contenu émis en tant qu'item par Single

                    //retourne un  Observable qui émet des items correspondant à chaque ligne du buffer
                    //autrement dit, split  ligne par ligne le buffer correspondant au fichier et retourne un Observable émettant chaque ligne
                    .flatMapObservable(buffer->Observable.fromArray(buffer.toString().split("\n")))
                    .skip(1)//filtrage - suppression de la ligne 1 correspondant à l'entête csv
                    .map(record-> record.split(","))//transforme chaque ligne customer en 1 tableau de String
                    .map(recordArray-> new Customer(Integer.parseInt(recordArray[0]),recordArray[1],recordArray[2]))// transforme chaque String[] en objet de type Customer
                    .subscribe(//définition de l'observateur
                        customer->{//à chaque Customer reçu, le Customer est stocké dans le StringBuffer
                            logger.info("---item Customer reçu  et stocké dans le SB - exécution onNext ");
                            sb.append(customer);
                        },
                      error->System.err.println(error),
                     ()-> {//lorsque l'ensemble des items a été reçu
                         logger.info("---retour du contenu du fichier csv - exécution onComplete");
                         completableFuture.complete(sb.toString());//on assigne au CompletableFuture le contenu du StringBuffer
                     }
                    );//fin subscribe
        //le thread principal n'est pas bloqué par le traitement métier (la lecture du fichier et le remplissage du tampon)
        logger.info("--fin d'exécution de readFile");
        //le Future est retourné au Endpoint REST quand le CompletionStage a terminé (onComplete)
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
