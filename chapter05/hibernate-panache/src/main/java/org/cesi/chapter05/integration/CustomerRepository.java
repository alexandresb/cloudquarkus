package org.cesi.chapter05.integration;

import io.quarkus.panache.common.Sort;
import org.cesi.chapter05.entity.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {



    public List<Customer> findAll(){
        //méthode statique retournant toutes les entités
        return Customer.listAll(Sort.by("id"));//tri par identité croissante

    }

    public Customer findCustomerById(Long id){
        //retourne null si l'entité n'est pas trouvée en base
        Customer customer = Customer.findById(id);
        if(customer == null) throw new CustomerException("Cust "+id+" not found");
        return customer;
    }

    @Transactional
    public void updateCustomer(Customer customer){
        //customerToUpdate est managé
        Customer customerToUpdate = findCustomerById(customer.id);
        //De plus, si le nombre de champ (potentiellement) modifié est plus important, le merge est plus pertinent
        // que de mettre à jour via setter chaque champ.
        customerToUpdate.name=customer.name;
        customerToUpdate.surname = customer.surname;
    }

    @Transactional
    public void createCustomer(Customer customer){
       customer.persist();
    }

    @Transactional
    public void deleteCustomer(Long customerId){
        Customer c= findCustomerById(customerId);
        c.delete();
    }


}
