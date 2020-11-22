package org.cesi.chapter08.lifecycle;


import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.h2.server.TcpServer;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import java.sql.SQLException;

//cf. https://quarkus.io/guides/lifecycle

/** ordre de levée des evnts :
 * Initialized(AppolicationScoped.class)->StartupEvent -> ShutdownEvent ->@Destroyed(ApplicationScoped.class)
 */
@ApplicationScoped //ce n'est pas obligé de définir un bean CDI pour pouvoir utiliser les gestionnaires de démarrage CDI et Quarkus
public class DBLifeCycleBean {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    //serveur H2
    private Server h2Server;


    //méthode exécuté lorsque l'évènement CDI @Initialized(@ApplicationScoped.class)
    //indiquant que le contexte d'application est initialisé
    // est levé
    void StartOnContextInit(@Observes @Initialized(ApplicationScoped.class) Object event) {

        try {
            h2Server = Server.createTcpServer("-tcpPort", "19092", "-tcpAllowOthers").start();
            logger.info("H2 database started in TCP server mode on Port "+h2Server.getPort());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //méthode exécutée lorsque l'application démarre. Déclenchée lorsque StartupEvent est levé
    //StartupEvent est levée après l'évènement "@Initialized(ApplicationScoped.class)"
    //Evnmt StartupEvent levé (juste) avant que l'app a fini de démarrer / est en service
    void onStart(@Observes  StartupEvent event){
        logger.info("l'application démarre");
    }

    //s'éxécute toujours avant une méthode interceptant l'evnmt CDI "@Destroyed(ApplicationScoped.class)"
    //s'execute lorsque l'app est en cours d'arrêt.
    void onStop(@Observes ShutdownEvent event){
        if(h2Server !=null){
            h2Server.stop();
            logger.info("le serveur H2 est stoppé");
            h2Server = null;
        }

        logger.info("l'application est en court d'arrêt");
    }
    //non présent dans le code du livre
    void onDestroy(@Observes @Destroyed(ApplicationScoped.class) Object event){
        logger.info("le contexte d'application est détruit");
    }
}
