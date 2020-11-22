package org.cesi.chapter06.entity;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

/**
 * Lors de la génération de table, si le fournisseur de persistance utilise le nom de l'entité Order
 * il y aura une erreur DDL car order est un mot clé réservé SQL. Il ne pourra donc créer une table nommée order.
 * (il est interdit qu'une table ait pour nom un mot clé SQL)
 *  On spécifie donc @Table(name="orders") sur l'entité pour que la table générée soit orders.
 * C'est pour cela que par simplification le livre a utilisé une Entité Orders
 */
@Entity
@Table(name = "Orders")
@NamedQuery(name = "Orders.findAll",
        query = "SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.item")
@NamedQuery(name = "Orders.countAll",
query = "SELECT count(o) FROM Order o")
public class Order {
    @Id
    @SequenceGenerator(
            name = "orderSequence",
            sequenceName = "orderId_seq",
            allocationSize = 1,
            initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderSequence")
    private Long id;
    @Column(length = 40)
    private String item;
    private Long price;

    @ManyToOne
    @JoinColumn(name="customer_id")
    @JsonbTransient //liste ignorée lors de la (dé)sérialisation
    private Customer customer; //côté propriétaire

    public Long getId() {
        return id;
    }
    //nécessaire pour pouvoir assigner l'id lors de la désérialisation json->Objet Order.
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
