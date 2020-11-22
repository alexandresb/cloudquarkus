package org.cesi.chapter06.integration;

import javax.json.bind.annotation.JsonbTransient;

import java.util.ArrayList;
import java.util.List;


public class Customer{

    //allocationSize = pas de l'incrémentation (on incrémente de 1)

    private Long id;


    private String name;


    private String surname;


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
                '}';
    }
}
