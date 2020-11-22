package org.cesi.chapter10.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

/*
@RegisterForReflexion permet explicitement est à spécifier quand on veut exécuter une image native d'une appli utilisant la réflexion
dans le cas de notre appli, il y a mapping Entité/JSON avec JSON-B qui utilise en coulisse la réflexion.
Or lors de la création d'une image native, l'arbre des appels créé ne contient pas les méthodes non explicitement utilisées dans le code
Par exemple, un constructeur sans argument, des setters.
Par conséquent le moteur JSON-B renverra une erreur lors du mapping
car la réflexion en coulisse ne trouvera pas de constructeur sans arg dispo.
Avec cette annotation, on spécifie de ne pas supprimer de méthodes potentiellement utilisables par la rélfexion
cf. https://quarkus.io/guides/writing-native-applications-tips#registering-for-reflection
 */
@RegisterForReflection
//opération de vente ou achat sur 1 stock d'entreprise
public class Operation {
    public static final int SELL=0;
    public static final int BUY=1;

    private int type; //prendra la valeur SELL ou BUY
    private Company company;
    private int amount;

    public Operation() {//pour le mapping JSON-B
    }

    public Operation(int type, Company company, int amount) {
        this.type = type;
        this.company = company;
        this.amount = amount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
