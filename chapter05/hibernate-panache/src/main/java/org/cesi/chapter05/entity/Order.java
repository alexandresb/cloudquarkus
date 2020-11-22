package org.cesi.chapter05.entity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

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
public class Order extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "orderSequence",
            sequenceName = "orderId_seq",
            allocationSize = 1,
            initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderSequence")
    public Long id;
    @Column(length = 40)
    public String item;
    public Long price;

    @ManyToOne
    @JoinColumn(name="customer_id")
    @JsonbTransient //liste ignorée lors de la (dé)sérialisation
    public Customer customer; //côté propriétaire

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", item='" + item + '\'' +
                ", price=" + price +
                '}';
    }
}
