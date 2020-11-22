package org.cesi.chapter04.integration;

import org.cesi.chapter04.entity.Customer;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    private List<Customer> customerList = new ArrayList<>();
    private int counter;
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
