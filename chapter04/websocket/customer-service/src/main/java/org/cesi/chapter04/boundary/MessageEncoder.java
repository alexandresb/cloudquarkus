package org.cesi.chapter04.boundary;

import org.cesi.chapter04.entity.Customer;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringWriter;
import java.util.List;

public class MessageEncoder implements Encoder.Text<List<Customer>> {
    @Override
    public String encode(List<Customer> customers) throws EncodeException {
        //création du tableau JSON-B contenant les customers
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for(Customer c : customers){
            jsonArrayBuilder.add(Json.createObjectBuilder()
                                            .add("name",c.getName())
                                                .add("surname",c.getSurname()));
        }
        JsonArray jsonArray = jsonArrayBuilder.build();
        //création d'un buffer
        StringWriter buffer = new StringWriter();
        //écriture du tableau JSON dans le buffer
        Json.createWriter(buffer).writeArray(jsonArray);
        //retour de la représentation String du buffer.
        return buffer.toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
