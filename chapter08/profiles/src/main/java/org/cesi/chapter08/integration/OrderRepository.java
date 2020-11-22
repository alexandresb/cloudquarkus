package org.cesi.chapter08.integration;

import org.cesi.chapter08.entity.Customer;
import org.cesi.chapter08.entity.Order;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class OrderRepository {
    @Inject EntityManager entityManager;

    public List<Order> findAll(Long customerId){
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

    }
    @Transactional
    public void deleteOrder(Long orderId) {
        Order o = findOrderById(orderId);
        entityManager.remove(o);
    }
    
}
