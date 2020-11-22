package org.cesi.chapter06.integration;

import org.cesi.chapter06.entity.Customer;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class CustomerRepository {
    private List<Customer> customerList = new ArrayList<>();

    //le processeur de bean Quarkus conseil d'utiliser une portée package plutôt que private pour les points d'injection
    @Inject //container-managed transaction-scoped EM
    EntityManager entityManager;

    private static final Logger LOGGER = LoggerFactory.getLogger("CustomerRepository");

    @Timeout(250L) // type long - durée max autorisée en msec pour que la méthode retourne
    //en cas d'erreur lors de l'exe de la méthode (exception, timeout atteint) la méthode findAllStatic est invoquée
    //en cas d'erreur on route vers la méthode de fallback
    @Fallback(fallbackMethod = "findAllStatic")
    //si l'exe de la méthode "rencontre" une exception de type "timeout" ou "runtime", alors il y a 3 tentatives supplémentaires maximum
    //de retenter l'exécution de la méthode avant d'invoquer la méthode de fallback.
    // on peut tester en mettant un timeout de 3 ms qui entrainera un échec à coup sur.
    @Retry(maxRetries = 3, retryOn = {RuntimeException.class, TimeoutException.class})
    public List<Customer> findAll(){

        try {
            //On utilise l'API Java SE pour mettre"en pause" la méthode  1 et 400 ms  pour simuler le déclenchement
            // aléatoire d'une erreur de timeout.
            /* Si le délai est sup à 250ms, Quarkus déclenche l'interruption du thread dans lequel cette méthode
            s'exécute durant l'exé de La méthode Thread.sleep() en coulisse qui lève donc l'exception InterruptedException.
            Le bloc catch est donc exécuté, la méthode se termine normalement.
            puis la chaine d'interception de la requête invoque la méthode Fallback qui retourne la liste statique.
            cf. commentaires dans la méthode randomSleep()

            Normalement on n'utilise pas l'API Thread dans un env Java EE (ici c'est pour simuler) donc on n'implémente généralement pas
            la gestion d'une InterruptedException qui permet à la méthode de finir normalement malgré le timeout.
            Par conséquent dans un scénario classique JEE le dépassement du délai entraine la fin prématurée de l'exe de la
            méthode en cours quand le délai est atteint.
             */
            randomSleep();
            List<Customer> customers = entityManager
                    .createNamedQuery("Customers.findAll", Customer.class)
                    .getResultList();
            LOGGER.info("retour des données chargées depuis la base");
            return customers;
        }catch(InterruptedException e){
            LOGGER.info("l'appli a pris trop de temps pour s'exécuter");
            return null;
        }
    }

    public Customer findCustomerById(Long id){

        Customer customer=entityManager.find(Customer.class, id);
        //dans le code source du livre c'est une exception JAX-RS mais cela casse la séparation des resp car
        //un repo doit être agnostique de la couche d'exposition web.
        if (customer==null)
            throw new CustomerException("customer with id "+id+ " not found");

        return customer;
    }

    @Transactional
    public void updateCustomer(Customer customer){
        //on aurait pu utiliser EM.merge - cela évite entre autre un SELECT donc réduit l'accès à la BDD
        Customer customerToUpdate = findCustomerById(customer.getId());
        //De plus, si le nombre de champ (potentiellement) modifié est plus important, le merge est plus pertinent
        // que de mettre à jour via setter chaque champ.
        customerToUpdate.setName(customer.getName());
        customerToUpdate.setSurname(customer.getSurname());
    }

    @Transactional
    public void createCustomer(Customer customer){
        entityManager.persist(customer);
    }

    @Transactional
    public void deleteCustomer(Long customerId){
        Customer c= findCustomerById(customerId);
        entityManager.remove(c);
    }

    private List<Customer> findAllStatic() {
        LOGGER.info("Building Static List of Customers");
        return buildStaticList();

    }
    private List<Customer> buildStaticList() {
        List<Customer> customerList = new ArrayList();
        Customer c1 = new Customer();
        c1.setId(1l);
        c1.setName("John");
        c1.setSurname("Static");

        Customer c2 = new Customer();
        c2.setId(2l);
        c2.setName("Fred");
        c2.setSurname("Static");

        customerList.add(c1);
        customerList.add(c2);
        return customerList;
    }
    //méthode entrainant une pause entre 1 et 400 msec => pour simuler  l'atteinte parfois du timeout pour findAll
    private void randomSleep() throws InterruptedException{
      //  try {
            long delay = new Random().nextInt(400);
            LOGGER.info("delai de pause = "+delay);
            Thread.sleep(delay);//400 = limite supérieure
        /*
        si le délai est supérieur au temps max fixé via @Timeout, Quarkus invoque Thread.interrupt() pour le thread
        courant. Il y a sortie de la méthode sleep avec exception InterruptedException levée
        et le thread passe en état interrupted.
        le bloc catch est exécutée. Dans ce cas le thread n'est pas interrompu.
        le thread (et donc la méthode findAll dans laquelle randomSleep est invoqué)se termine normalement
        mais le résultat est discardé et la méthode fallback invoquée.
        Si pas de fallback alors on a une TimeoutException qui est déclenchée.


        cf. spec fault tolerance - partie @Timeout : https://github.com/eclipse/microprofile-fault-tolerance/blob/master/spec/src/main/asciidoc/timeout.asciidoc
        la situation est décrite ici : https://github.com/eclipse/microprofile-fault-tolerance/issues/408

         */
        /*
        } catch (InterruptedException e) {
            LOGGER.info("L'exécution prend trop de temps...{}", e.getMessage());
            e.printStackTrace();
        }*/
    }

}
