package org.cesi.chapter05.integration;

import io.quarkus.panache.common.Sort;
import org.cesi.chapter05.entity.Customer;
import org.cesi.chapter05.entity.Order;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class OrderRepository {

    public List<Order> findAll(Long customerId){
        return Order.list("customer.id", Sort.by("item"), customerId);
    }


    public Order findOrderById(Long id){
        Order o = Order.findById(id);
        if (o==null)
            throw new CustomerException("order with id "+id+ " not found");//pas d'exception spécifique - réutilisation CustExc
         return o;
    }

    @Transactional
    public void updateOrder(Order order) {
        //là encore EM.merge aurait pu être utilisé.
        Order orderToUpdate = findOrderById(order.id);
        orderToUpdate.item = order.item;
        orderToUpdate.item = order.item;
    }
    @Transactional
    public void createOrder(Order order, Customer c) {
        order.customer = c;//on assigne le côté proprio de la ref pour persister la relation
        order.persist();

    }
    @Transactional
    public void deleteOrder(Long orderId) {
        Order o = findOrderById(orderId);
        o.delete();
    }
    
}
