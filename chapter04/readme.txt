-----basic--------
Quarkus 1.3.2.Final - GraalVM 19.3.1 Java 8
projet Maven crée via le générateur quarkus.io

- l'UI Angular est un copier/coller du code source disponible sur github
-remplacement à la ligne 115 d'address par surname dans le tableau des Customers (index.html) :
<div  class="divTableHead">Customer Surname</div>

- Ajout à la main de l'extension RESTEasy JSON-B pour la (dé)sérialisation Java/Json :
 <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-jsonb</artifactId>
    </dependency>

Création de la classe de Test CustomerEndpointTest

1) exécution locale

- packaging pour lancer les tests :
mvn package

- exécuter en mod dev :
mvn compile quarkus:dev (ne lance pas les tests)

- accès à l'interface Angular :
http://localhost:8080
Pour éditer clic Edit | modifier dans les champs texte | clic sur Save.
On peut accéde à la liste brute des customers via GET http://localhost:8080/customers => on peut voir l'id et la mise en forme JSON

2) déploiement dans Minishift

-démarrer Minishift | oc login | oc project quarkus-project

- ajout de la classe NativeCustomerEndpointIT (annotée @NativeImageTest) pour les tests d'un exe natif 

- créer l'exe natif pour container :
depuis le dossier customer-service, lancer :
mvn package -Pnative -Dquarkus.native.container-build=true (cette commande ne lance pas les tests de l'exe natif).
<rappel pour tester : mvn verify -Pnative>

- création du script deploy-minishift.sh contenant les instructions pour déployer l'application dans Minishift | quarkus-project
- lancement du script sh deploy-minishift.sh

- test de l'appli via l'URL exposé par la route. Par ex : http://quarkus-customer-service-quarkus-project.192.168.42.90.nip.io/

- suppression de tous les artefacts déployés relatifs à l'appli :
oc delete all --all

--------websocket-------------
1) Customer Service
- copie du projet customer-service précédent sous le dossier websocket

- ajout dans pom.xml de l'extension Undertow Websocket via le plugin Quarkus Maven :
(depuis le dossier projet Maven) mvn quarkus:add-extension -Dextensions="quarkus-undertow-websockets"

- création et implémentation du point de terminaison WebsocketEndpoint (annoté @ServerEndpoint)

- implémentation de l'encoder permettant de sérialiser List<Customer> en réponse textuelle json de la websocket (implémente Encoder.Text<List<Customer>>)
-indiquation de l'encodeur dans l'annotation ServerEndpoint du point de terminaison : 
@ServerEndpoint(value="/customers", encoders = MessageEncoder.class)

-ajout de la configuration Quarkus autorisant le CORS dans application.properties (sours src/main/resources)

2) client websocket
- création via plugin IJ du projet Maven customer-service-fe
- MAJ du pom pour utiliser Quarkus 1.3.2 et Java 8

- copier/coller du code source original du livre pour créer META-INF/resources/index.html,stylesheet.css, functions.js(contenant le code client pour se connecter au pt de terminaison serveur Websocket)

- déclaration du port http 9080 dans application.properties (quarkus.http.port=9080)

- Ajout de l'extension quarkus-undertow pour intégrer le serveur http Undertow et utiliser l'API HttpServlet :
mvn quarkus:add-extension -Dextensions="quarkus-undertow"

-création de la servlet http AjaxHandler interceptant les requêtes GET /ajaxhandler pour retourner l'URI du point de terminaison du srv
Websocket en s'appuyer une variable Système CUSTOMER_SERVICE

3) test fonctionnel

- lancement de customer-service (serveur Websocket) :
mvn compile quarkus:dev
- lancement customer-service-fe (pair client websocket) - port debug spécifié car le port par défaut est déjà utilisé par le srv
mvn compile quarkus:dev -Ddebug=6005
<si le port debug n'est pas explicité il y a une erreur lors du lancement indiquant que le port 5005 est déjà utilisé - cela n'empêche pas le lancement de réussir>.
- ouverture de localhost:9080 - insertion de client en invoquant le server endpoint - on peut vérifier dans le client javascript de customer-service (localhost:8080) que la liste est aussi affichée.


- dans customer-service | application.properties, on indique le port 8888 & on redémarre le service (mvn quarkus:dev)
- dans le terminal de lancement de customer-service-fe, on définie la variable d'environnement CUSTOMER_SERVICE:
export CUSTOMER_SERVICE=ws://localhost:8888/customers
- depuis ce terminal on redémarre le service client customer-service-fe
-on refait les tests fonctionnels.

NOTE : côté client Javascript, à chaque insertion d'un client, la fonction javascript send_message() établieun nouvelle connexion websocket ave le point de terminaison Serveur Quarkus. Côté serveur, il y a donc à chaque fois création d'une nouvelle instance de pt de terminaison serveur. ce n'est pas optimum.
<j'ai tenté de modifier le code pour avoir une seule connexion par chargement de page) mais ça n'a pas marché.>

NOTE : en mode dev, si on change le port d'écoute http, le hot replace est déclenché à la réinvocation (ex rafraichissement web) mais le nouveau port n'est pas considéré.=> le hot replace ne permet pas de changer de port d'écoute durant le dev (du moins avec Quarkus 1.3.2.Final)












