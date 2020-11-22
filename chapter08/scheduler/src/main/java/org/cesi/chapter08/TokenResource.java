package org.cesi.chapter08;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/token")
public class TokenResource {
    @Inject
    TokenGenerator tokenGenerator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TokenResource() {
        //les logs indiquent le nom du thread dans lequel l'exe a lieu
        logger.info("exécution du constructeur TokenResource()");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getToken() {
        logger.info("exécution de la méthode GET geToken()");
        return tokenGenerator.getToken();
    }


}