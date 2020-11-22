package org.cesi.chapter06.integration;
import javax.json.bind.annotation.JsonbTransient;


public class Order {

    private Long id;
    private String item;
    private Long price;

    public Long getId() {
        return id;
    }
    //nécessaire pour pouvoir assigner l'id
    public void setId(Long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", item='" + item + '\'' +
                ", price=" + price +
                '}';
    }
}
