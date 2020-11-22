package org.cesi.chapter06;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * en local accès uniquement aux informations de santé "service Ready" : localhost:8080/health/ready
 */

@ApplicationScoped
@Readiness
public class ReadinessHealthCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("File System readiness check");
        //true si il y a un fichier tmp.lck sous /tmp
        Boolean tempFileExists = Files.exists(Paths.get("/tmp/tmp.lck"));
        if(!tempFileExists){// si pas de verrou
            responseBuilder.up();
        }else{
            responseBuilder.down().withData("error","lock detected");
        }
        return responseBuilder.build();
    }
}
