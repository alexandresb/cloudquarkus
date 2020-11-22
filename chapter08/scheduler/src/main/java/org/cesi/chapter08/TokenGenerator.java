package org.cesi.chapter08;


import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
//un bean Application scoped est instancié 2 fois du fait de la créa d'un proxy CDI.
//un bean @dependent ou un composant @Singleton n'est instancié qu'une fois car pas de proxy créé
@ApplicationScoped // si cette annotation n'est pas présente TokenGenerator définit un bean @Dependent
public class TokenGenerator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TokenGenerator() {
        logger.info("exécution du constructeur TokenGenerator()");
    }
    //exécutée qu'une fois pour l'instance du bean Application scoped créé - pas pour l'instanciation correspondant au proxy
    @PostConstruct
     void init(){
        logger.info("exécution PostConstruct dans");
    }

    private String token;

    public String getToken() {
        logger.info("exécution de getToken()");
        return token;
    }


    @Scheduled(every = "1s")
    void generateToken(){
        logger.info(" @Scheduled generateToken déclenchée");
        token = UUID.randomUUID().toString();

    }
    //syntaxe des expressions cron utilise celle de Quartz : http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
    //déclenchée toutes les 30 sec.
    @Scheduled(cron = "0/30 * * * * ?")
    void generatePeriodicMessage(){
        logger.info(" @Scheduled(cron=...) generatePeriodicMessage() déclenchée");
    }
}
