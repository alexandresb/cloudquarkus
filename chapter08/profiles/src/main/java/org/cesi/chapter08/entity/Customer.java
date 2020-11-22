package org.cesi.chapter08.entity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cacheable // (donnée de l'entité stockées et retrouvées dans le cache L2
@NamedQuery(name = "Customers.findAll",
query = "SELECT c FROM Customer c ORDER BY c.id",
        hints=@QueryHint(name = "org.hibernate.cacheable", value = "true"))//hint permettant de cacher le résultat de la requête SQL sous-jacente //nécessite @Cacheable
public class Customer{
    @Id
    //allocationSize = pas de l'incrémentation (on incrémente de 1)
    @SequenceGenerator(name="customerSequence",
    sequenceName = "customerId_seq",
    allocationSize = 1,
    initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerSequence")
    private Long id;

    @Column(length = 40)
    private String name;

    @Column(length = 40)
    private String surname;

    @OneToMany(mappedBy="customer")
    @JsonbTransient //liste ignorée lors de la (dé)sérialisation
    /*
    la sérialisation de cette relation bidirectionnelle entrainerait une levée d'exception
    car il y aurait tentative de désérialiser à l'infini du fait de l'aspect bi-dir
     */
    private List<Order> orders = new ArrayList<>();// côté inverse de la relation

    //méthode non présente dans le livre
    public void addOrder(Order o){
        orders.add(o);
    }
    //non présente dans le livre
    public void removeOrder(Order o){
        orders.remove(o);
    }
    //non présente dans le livre
    public List<Order> getOrders() {
        return orders;
    }

    public Long getId() {
        return id;
    }

    //nécessaire pour pouvoir assigner l'id lors de la désérialisation json->Objet Order.
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

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
