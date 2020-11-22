package org.cesi.chapter09;

import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 *  objet métier utilisant le pattern active record (selon moi)
 */
public class Customer {

    private Long id;
    private String name;
    private String surname;

    public Customer(Long id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }
    //pour le mapping JSON - Objet
    public Customer() { }

    /**
     *
     * @param row de type io.vertx.axle.sqlclient.Row
     * @return Customer
     * méthode permettant de créer un objet Customer depuis une Row d'un RowSet
     */
    private static Customer from(Row row) {
        return new Customer(row.getLong("id"), row.getString("name"), row.getString("surname"));
    }

    /**
     *
     * @param client de type PgPool
     * @return CompletionStage<List<Customer>> -  un CompletionStage dont le résultat est la liste des customers
     * récupération asynchrone de la liste des customers
     */
    public static CompletionStage<List<Customer>> findAll(PgPool client){
        //la requête et thenApply sont exécutées dans un thread différent du thread principal
        return client.query("SELECT id, name, surname FROM CUSTOMER ORDER BY name ASC")
                        .execute()//retourne un CompletionStage
                        .thenApply(pgRowSet->{//prend en arg le rowset retourné par la req
                            List<Customer>  customers = new ArrayList<>();
                            for(Row row : pgRowSet){//pour chaque ligne
                                //ajout d'un customer dans la liste
                                customers.add(from(row));
                            }
                            return customers;
                        });//fin thenApply
    }

    /**
     *
     * @param client de type PgPool
     * @return CompletionStage<Long> - l'id du customer créé
     * exécution async
     *  -d'une req d'insertion retournant un rowset contenant l'id du customer créé.
     *  -la tâche d'insertion finie, récupération de l'id.
     * note :insert...returning(id) est une syntaxe SQL pour Postgres. la clause returning permet de retourner des datas après insertions
     * (cf. https://www.postgresql.org/docs/9.1/sql-insert.html)
     */
    public CompletionStage<Long> create(PgPool client){
            //exécution déléguée à 1 thread séparé
            return client.preparedQuery("INSERT INTO CUSTOMER (id, name, surname) VALUES ( nextval('customerId_seq'), $1,$2) RETURNING (id)")
                .execute(Tuple.of(name, surname)) //retourne un CompletionStage contenant un jeu d'en registrement avec 1 seul enregistrement, l'id du customer inséré
                .thenApply(pgRowSet->pgRowSet.iterator().next().getLong("id"));//retourne un CompletionStage embarquant l'id du customer créé
    }
    /*
    returne "vrai" si l'update du de Customer.name/surname a réussi
    Exécution des tâches de façon asynchrone
     */
    public CompletionStage<Boolean> update(PgPool client){
        return client.preparedQuery("UPDATE CUSTOMER SET name = $1, surname = $2 WHERE id = $3")
                        .execute(Tuple.of(name,surname,id))//exécution de l'update
                        .thenApply(pgRowSet->pgRowSet.rowCount()==1);//vrai si le rowset contient une seule ligne (update réussi)
    }

    public static CompletionStage<Boolean> delete(PgPool client, Long custId){
        return client.preparedQuery("DELETE FROM CUSTOMER WHERE id = $1")
                        .execute(Tuple.of(custId))
                        .thenApply(pgRowSet->pgRowSet.rowCount()==1);//vrai si le rowset contient une seule ligne (delete réussi)

    }

    public Long getId() {
        return id;
    }

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
