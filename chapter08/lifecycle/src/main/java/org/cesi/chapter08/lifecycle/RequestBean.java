package org.cesi.chapter08.lifecycle;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import java.sql.SQLException;

@RequestScoped
public class RequestBean {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    void StartOnContextInit(@Observes @Initialized(ApplicationScoped.class) Object event) {

            logger.info("context d'application initialisé");

    }
}
