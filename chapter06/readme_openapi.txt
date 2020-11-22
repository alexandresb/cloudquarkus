Quarkus 1.3.4.Final- GraalVM 19.3.1 Java 8

Documentation en ligne avec OpenAPI Eclipse MicroProfile 3.3 et Swagger

1) doc par défaut

- copie du projet chapter06/metrics =>renommage du dossier,iml, dans pom

- ajout dépendance io.quarkus:quarkus-smallrye-openapi + reload dans menu Maven IJ pour prise en compte de la dépendance

-lancement (mvn compile quarkus:dev) et visualisation de la doc YAML OpenAPI pour les points de terminaison REST : 
 * curl http://localhost:8080/openapi
 * Firefox : la doc YAML affichée est téléchargé sous /tmp
 
- accès à l'UI Swagger : http://localhost:8080/swagger-ui
  * test : clic sur GET /customers | try Out | Execute

2) Customisation de la doc :

(au fur et  à mesure des des ajouts, on accède à la doc /openapi et à l'UI /swagger-ui)

- annotation de CustomerEndpoint et OrderEndpoint avec @Tag(name=...,description=...) pour personnaliser les attributs tags de la doc OpenAPI générée.

- annotation de CustomerEndpoint.getAll() avec @Operation(operationId = "all", description = "getting all customers") pour ajouter un id à l'opération et une description

- annotation de CustomerEndpoint.getAll() avec @APIResponse(responseCode = "200", description = "Successful response") pour décrire la réponse avec le code 200.

-< présent dans livre> annotation avec @RequestBody de du paramètre Customer de la méthode @POST de CustomerEndpoint pour personnaliser la doc du request body de l'op mappée ave la méthode.
public Response createCustomer(@RequestBody(description = "the new customer",required = true) Customer customer) 

- annotation du paramètre de la méthode @Delete de CustomerEndpoint avec @Parameter pour customiser la doc du paramètre de requête :
public Response delete(@Parameter(description = "The customer to delete", required = true) @QueryParam("id") Long customerId)

-< présent dans livre> personnalisation de la doc de la réponse pour la méthode @PUT de OrderEndpoint en annotant avec 2 @APIResponse pour docuementer la réponse 204 et 404 :
@APIResponse(description = "update an order of the given customer's info", responseCode = "204")
    @APIResponse(description = "the id of the order is not valid", responseCode = "404")






