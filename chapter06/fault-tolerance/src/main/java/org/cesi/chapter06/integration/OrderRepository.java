package org.cesi.chapter06.integration;

import org.cesi.chapter06.entity.Customer;
import org.cesi.chapter06.entity.Order;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/*
tuto interactif sur Eclipse MicroProfile  circuit breaker :
https://openliberty.io/guides/circuit-breaker.html
 */

@ApplicationScoped
public class OrderRepository {
    @Inject EntityManager entityManager;
    //erreur dans le code source original, le logger est nommé CustomerRespository
    private static final Logger LOGGER = LoggerFactory.getLogger("OrderRepository");
    /*
    - requestVolumeThresold (rVT)= le nombre de requête de la fenêtre glissante
                          = le nb minimal de requêtes successives effectués pour que le circuit braker détermine si il faut OUVRIR
    - failureRatio (fR)= le tx minimal de requête échouant dans la fenêtre glissante pour que le circuit soit ouvert
    - le nombre de requête minimal entrainant l'ouverture du circuit = rVT X fR :
    ici il faut que 3 requêtes sur les 4 dernières requêtes échouent pour que le circuit soit ouvert
    - delay = le temps que reste ouvert le circuit avant de passer en état HALF OPEN.
    Pendant que le circuit est ouvert aucune requête n'est routée vers la méthode annotée (méthode principale) findAll
    En état HALF OPEN, l'invocation de la méthode principale est retestée.
    Si échec le circuit reste ouvert.
    - requestVolumeThreshold = le nb de requêtes successives qui doivent réussir pour que le circuit repasse en état fermé.
    ici il faut que, lorsque le circuit est en état HALF OPEN, 5 invocation de findAll réussissent pour que le circuit soit refermé.
    C-à-d, qu'il faut possiblefailure tire 5 fois d'affilée un flottant > 0.5

    ici donc il faut que 3 req  sur les 4 dernières requêtes contigues échouent via la levée d'une RTE pour que le circuit
    soit ouvert pdt 10 sec avant de passer en état presque ouvert. Il faudra que 5 appels successifs réussissent pour que le circuit passe
    en état fermé.

     */
    @CircuitBreaker(successThreshold = 5,
            requestVolumeThreshold = 4, failureRatio = 0.75,
            delay=10000L,
            failOn=RuntimeException.class)
    //inovquée quand findAll échoue (une exception est levée dans findAll).
    // sans cette spécification de fallback la chaine d'invocation cliente reçoit la RTE levée
    @Fallback(fallbackMethod = "FindAllOrdersStatic")
    public List<Order> findAll(Long customerId){
        possibleFailure();
        LOGGER.info("pas d'exception runtime levée dans findAll(customerId)");
        return (List<Order>) entityManager.createNamedQuery("Orders.findAll")
                .setParameter("customerId",customerId)
                .getResultList();//utilisation d'une requête nommée non typée d'où le downcast en List<Order>
    }


    public Order findOrderById(Long id){
        Order o = entityManager.find(Order.class, id);
        if (o==null)
            throw new CustomerException("order with id "+id+ " not found");//pas d'exception spécifique - réutilisation CustExc
         return o;
    }

    @Transactional
    public void updateOrder(Order order) {
        //là encore EM.merge aurait pu être utilisé.
        Order orderToUpdate = findOrderById(order.getId());
        orderToUpdate.setItem(order.getItem());
        orderToUpdate.setPrice(order.getPrice());
    }
    @Transactional
    public void createOrder(Order order, Customer c) {
        order.setCustomer(c);//on assigne le côté proprio de la ref pour persister la relation
        entityManager.persist(order);
        //on log de manière asynchrone - le retour n'est pas ici utilisé
        writeSomeLogging(order.getItem());

    }
    //méthode s'exécutant dans un thread différent du thread de l'invocant.
    @Asynchronous
    //value = 5 exe concurrentes max de la méthode - valable pour le mode d'isolation par thread ou semaphore
    //waitingTaskQueue = jusqu'à 10 invocations/requêtes peuvent attendre dans la queue - attribut valable que pour le mode thread isolation
    @Bulkhead(value = 5, waitingTaskQueue = 10)
    private Future writeSomeLogging (String item){
        LOGGER.info("Customer orders at : " +new Date());
        LOGGER.info("Item : {}",item);
        return CompletableFuture.completedFuture("OK");
    }
    @Transactional
    public void deleteOrder(Long orderId) {
        Order o = findOrderById(orderId);
        entityManager.remove(o);
    }

    private void possibleFailure() {
        //Random.nextFloat retourne un flottant entre 0.1 et 1.0
        float randomFloat = new Random().nextFloat();
        //si le flottant tiré aléatoirement est < à 0,5 on lève une exception Runtime
        LOGGER.info("nb flottant tiré : "+randomFloat); //ajout ASO
        if (randomFloat < 0.5f) {
            throw new RuntimeException("Resource failure.");
        }
    }
   //la méthode de fallback doit avoir le même type de retour et la même liste de paramètre
    private List<Order>FindAllOrdersStatic(Long CustomerId){
        LOGGER.info("fallback invoqué");
        Order o = new Order();
        o.setId(1L);
        o.setItem("static item");
        o.setPrice(99L);
        List<Order> orders = new ArrayList<>();
        orders.add(o);
        return orders;
    }
}
