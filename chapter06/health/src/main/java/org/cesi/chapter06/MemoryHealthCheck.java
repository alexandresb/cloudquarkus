package org.cesi.chapter06;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness //service en fonctionnement
public class MemoryHealthCheck implements HealthCheck {

    //seuil de 1024 Mo de RAM. si on met 10240L le service est Up
    private long threshold = 1024000000L; // en Ko
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Memory liveness check ");
        //mémoire vive disponible dans la JVM - mémoire disponible dans l'instance de JVM dans laquelle s'exécute le service
        long freeMemory = Runtime.getRuntime().freeMemory();
        if(freeMemory>=threshold){
            responseBuilder.up();
        }else{
            responseBuilder.down().withData("error", "Not enough free memory - restart App");
        }
        return responseBuilder.build();

    }
}
