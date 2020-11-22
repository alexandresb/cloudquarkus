Quarkus 1.3.4.Final- GraalVM 19.3.1 Java 8

chapter06/rest-client --->restclient_backend/hibernate

1) Utilisation de l'API REST Client Eclipse MicroProfile 3.3

- copie sous le dossier chapter06/restclient_backend du projet chapter05/hibernate 
-changement artifactId=customer

- création via IJ d'un projet Quarkus : artifactId = rest-client | groupId = org.cesi | sélection de l'extension Quarkus REST Client et RESTEasy JSON-B (pour mapping POJO/JSON)
- modif pom : utilisation Java 8 (au lieu de 11) et Quarkus 1.3.4.Final (au lieu de 1.5.1.Final)

- copie dans org.cesi.integration.chapter06 des entités Customer et Order créées pour les projets précédents.
- adaptation des entités pour les transformer en POJO de type DTO :
  * suppression de toutes les annotations JPA
  * Customer : suppression de la List<Order>  et des méthodes associées | recréation de toString() via IJ
  * Order : suppression de la référence à Customer et méthodes associées

- création de l'interface org.cesi.chapter06.integration.CustomerEndpointInterface
  * annotation de l'iface avec @org.eclipse.microprofile.rest.client.inject.RegisterRestClient pour spécifier que cette interface est l'API cliente pour invoquer de manière déclarative un back-end exposé via API REST. 
  * annotation avec @Path pour indiquer l'URI du point de terminaison REST qu'on veut invoquer | annotation avec @Produces/@Consumes pour spécifier que le contenu de la requête ou la réponse vers et du point de terminaison invoqué est au format JSON
  * déclaration des méthodes de ressources (@GET, @POST...) qu'on veut invoquer.

- création du client org.cesi.chapter06.boundary.CustomerClientService (de type service REST aussi) utilisant l'interface REST Client CustomerEndpointItf :
 * annotation de la classe avec @Path("/customersclient") pour spécifier l'URI de base de cette ressource
 * injection de CustomerEndpointItf via @Inject @RestClient
 * implémentation des méthodes @GET, etc.. pour invoquer via JSON/REST ce client
   dans le code source, l'annotation @QueryParam("id") a été oublié au niveau de la déclaration de la méthode @Delete (sans ça, ça ne fonctionne pas)

- déclaration des propriétés application.properties permettant au client REST de communiquer avec le point de terminaison distant CustomerEndpoint :
	org.cesi.chapter06.integration.CustomerEndpointItf/mp-rest/url=http://localhost:8080 (url du service distant)
	org.cesi.chapter06.integration.CustomerEndpointItf/mp-rest/scope=javax.inject.Singleton (scope du proxy client implémentant l'iface).


- déclaration du port d'écoute 9090 dans application.properties :
quarkus.http.port=9090

- copie de index.html, order.html, stylesheet.css fournis dans le code source sous src/main/resources/META-INF
- adaptation de l'URI déclaré dans le module Angular dans index.html :
On remplace customers par customersclient 
angular.module('customerManagement').constant('SERVER_URL','/customersclient')


- test fonctionnel (dans le livre on a juste un T d'intégration) :
  *lancement de la base locale dockerisé comme les fois précédentes :
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5
  * lancement du service backend customer (projet hibernate) :
(depuis le dossier hibernate) mvn install (ou compile) quarkus:dev -Dmaven.test.skip=true
  * lancement du projet client (projet rest-client) : 
(depuis le dossier rest-client) mvn compile quarkus:dev -Ddebug=5006 
<on précise le port de debug pour ne pas avoir l'erreur indiquant que le port est déjà utilisé par l'appli back-end>
  * accès à l'UI web du client : localhost:9090

- modification du code client (pas dans le livre) :
 * utilisation de @Path @PathParam sur la méthode "delete" du client CustomerClientService.delete() pour que cette méthode soit invoquée via customersclient/{id} au lieu de customersclient?id=<X> :
 * modification du code de l'UI Angular pour invoquer la délétion en utilisant un path param :
(vers ligne 60 du fichier index.html) :  url: SERVER_URL+'/'+customer.id  // ASO - pour invoquer via path param
- test via l'UI web

- de façon similaire création de l'interface OrderEndpointItf utilisant l'API REST Client MicroProfile pour invoquer le endpoint back-end OrderEndpoint  de manière déclarative et création de la ressource REST OrderClientService dans laquelle cette interface est injectée.
- configuration du client Rest dans application.properties - on utilise le scope ApplicationScoped (au lieu de javax.inject.Singleton comme dans le livre)
- dans order.html on indique l'URI de base de la ressource racine OrderClientService :
angular.module('orderManagement').constant('SERVER_URL', '/ordersclient');

-test fonctionnel via l'UI localhost:9090

2) déploiement des services dans Minishift (pas dans le livre)
objectif : utiliser le service DNS Kubernetes de Minishift pour découvrir un service.
<déploiement d'applications Java>

a) déploiement du service customer (projet chapter06/restclient_backend/hibernate)

- modif du fichier hibernate/deploy-jvm-minishift.sh (le nom customer (<=>artifactId) remplace le nom quarkus-hibernate)
<Les commentaires dans Dockerfile.jvm n'ont pas été mis à jour, ni ceux dans Dockerfile.native. 
deploy-minishift.sh (pour déploiement exe natif dans Mini) n'a pas été modifié car non utilisé>

- connection au projet quarkus_project : oc login | oc project quarkus-project

- création d'une application de type serveur PostgreSQL hébergeant la base quarkusdb (credentials = quarkus / quarkus) :
oc new-app -e POSTGRESQL_USER=quarkus -e POSTGRESQL_PASSWORD=quarkus -e POSTGRESQL_DATABASE=quarkusdb postgresql

- lancement du script depuis le dossier projet hibernate :
sh deploy-jvm-minishift.sh

- test d'accès à l'application customer : (ex route) http://customer-quarkus-project.192.168.42.90.nip.io/

b) déploiement du service rest-client

- création du fichier deploy-client-jvm-minishift.sh par copier/coller du script deploy-jvm-minishift.sh
- dans le fichier on remplace le nom customer par rest-client

- modification de application.properties pour que le client se connecte au service customer déployé précédemment en utilisant son nom DNS (customer)
- assignation du port 8080 comme port d'écoute

- déploiement du service client consommant le service customer dans Minishift : 
sh deploy-client-jvm-minishift.sh

- test fonctionnel en accédant à l'UI du service rest-client (de customer) déployé dans minishift :
(ex route) http://rest-client-quarkus-project.192.168.42.90.nip.io/

- après les tests suppression des 3 services (bdd inclue) déployés :
oc delete all --all

- suppression des images docker Minishift des projets Quarkus pour nettoyer la VM Minishift (rest-client, customer...)





  


