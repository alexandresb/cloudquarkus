package org.cesi.chapter08.integration;

import org.cesi.chapter08.entity.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    private List<Customer> customerList = new ArrayList<>();

    //le processeur de bean Quarkus conseil d'utiliser une portée package plutôt que private pour les points d'injection
    @Inject //container-managed transaction-scoped EM
    EntityManager entityManager;


    public List<Customer> findAll(){
        //Ajout ASO pour étude de la gestion du cache L2
       Cache L2Cache = entityManager.getEntityManagerFactory().getCache();

        List<Customer> customers = entityManager
                .createNamedQuery("Customers.findAll",Customer.class)
                .getResultList();
        //ajout aso pour étude cache L2
        for(Customer c : customers){
            if(L2Cache.contains(Customer.class, c.getId())){
                System.out.println(c.getId()+ " est stocké dans le cache L2");
            }
        }
        return customers;
    }

    public Customer findCustomerById(Long id){
        //ajout ASO pour étude cache L2
        Cache L2Cache = entityManager.getEntityManagerFactory().getCache();
        Customer customer=entityManager.find(Customer.class, id);
        //dans le code source du livre c'est une exception JAX-RS mais cela casse la séparation des resp car
        //un repo doit être agnostique de la couche d'exposition web.
        if (customer==null)
            throw new CustomerException("customer with id "+id+ " not found");

        //ajout ASO pour étude cache L2
        if(L2Cache.contains(Customer.class, customer.getId())){
            System.out.println(customer.getId()+ " est stocké dans le cache L2");
        }

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


}
