package org.cesi.chapter05.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * utilisation de PanacheEntityBase car on définit notre stratégie de génération de l'ID
 *
 * Mise en oeuvre du pattern Active Record
 */

@Entity
@Cacheable // (donnée de l'entité stockées et retrouvées dans le cache L2
public class Customer extends PanacheEntityBase {
    @Id
    //allocationSize = pas de l'incrémentation (on incrémente de 1)
    @SequenceGenerator(name="customerSequence",
    sequenceName = "customerId_seq",
    allocationSize = 1,
    initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerSequence")
    public Long id;

    @Column(length = 40)
    public String name;

    @Column(length = 40)
    public String surname;

    @OneToMany(mappedBy="customer")
    @JsonbTransient //liste ignorée lors de la (dé)sérialisation
    /*
    la sérialisation de cette relation bidirectionnelle entrainerait une levée d'exception
    car il y aurait tentative de désérialiser à l'infini du fait de l'aspect bi-dir
     */
    public List<Order> orders = new ArrayList<>();// côté inverse de la relation

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", orders=" + orders +
                '}';
    }
}
