package org.cesi.chapter03;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContainerService {
    public String getContainerId(){
        //getenv() retourne une map (Map<String,String>) contenant les popriétés système
        return System.getenv().getOrDefault("HOSTNAME","inconnu");
    }
}
