package org.cesi.chapter06;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.*;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.Socket;

/**
 * HealthCheck est une interface fonctionnelle définissant la méthode abstraite call
 * Notes :
 * interface annotée avec l'annotation informative @FunctionalInterface
 * interface fonctionnelle = 1 seule méthode abstraite définie
 *
 * accès via <host>:<port>/health (ex : localhost:8080/health)
 */

//une annotation "health check" est obligatoire pour que ce pt de terminaison de santé soit exposé
@Health//déprécié dans Eclipse Microprofile 3.3 (depuis 2.0)
//@Readiness // service prêt à servir des clients (requêtes client) = prêt
//@Liveness //service en fonctionnement normal = vivant
@ApplicationScoped
public class DBHealthCheck implements HealthCheck {
    //Quarkus conseille de déclarer un accès de niveau package plutôt que private pour les champs injectés.
    @ConfigProperty(name = "db.host")
    private String host;
    @ConfigProperty(name = "db.port")
    private  Integer port;
    @Override
    public HealthCheckResponse call() {
        //création d'un builder
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");
        try{
            serverListening(host,port);
            //si la communication avec  serveur de BD fonctionne, on indique que le service est accessible / en fonctionnement (up)
            responseBuilder.up();
            //sinon
        }catch (Exception e){
            //on indique l'impossibilité de communiquer avec le service de BD (service inaccessible)
            responseBuilder.down().withData("error",e.getMessage());

        }
        return responseBuilder.build();//construction de la réponse retournée
    }
    //Etablissement de la communication avec le serveur de BD via Socket
    private void serverListening(String host, int port) throws IOException {
        //création de la socket (interface de comm) vers le SGBD
        Socket socket = new Socket(host, port);
        //fermeture de la ressource
        socket.close();
    }
}
