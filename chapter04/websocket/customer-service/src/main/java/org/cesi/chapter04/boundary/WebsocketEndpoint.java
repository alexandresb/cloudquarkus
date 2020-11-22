package org.cesi.chapter04.boundary;

import org.cesi.chapter04.entity.Customer;
import org.cesi.chapter04.integration.CustomerRepository;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;

/**
 * une seule instance par connexion établie avec un pair côté client
 */

@ServerEndpoint(value="/customers", encoders = MessageEncoder.class)
public class WebsocketEndpoint {
    @Inject
    private CustomerRepository customerRepository;

    @OnMessage
    public List<Customer> addCustomer(String message, Session session){
        //JSON-B
        Jsonb jsonb = JsonbBuilder.create();
        //désérialisation du message JSON reçu en objet Customer
        Customer customer = jsonb.fromJson(message,Customer.class);

        customerRepository.createCustomer(customer);
        return customerRepository.findAll();
    }
    @OnOpen
    public void init(Session session){
        System.out.println("Websocket ouverte - id session : "+session.getId());
    }

    @OnClose
    public void close(CloseReason reason){
        System.out.println("fermeture due à : "+reason.getReasonPhrase()+ " "+reason.toString());
    }

}
