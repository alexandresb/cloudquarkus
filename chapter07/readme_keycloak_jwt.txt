Quarkus 1.3.4.Final - GraalVM 19.3.1 Java 8

1) mise en place de la sécurité utilisant le standard OIDC via Keycloak

- Préparation : copie du projet chapter07/elytron-demo et renommage comme pour les autres projets (dossier racine, iml,artifactId), suppression dans import.sql des données utilisées pour la sécurité avec elytron, suppression de la dépendance Mvn quarkus-elytron-security-jdbc, mise en commentaire de l'implémentation des méthodes @Test, suppression de la config Elytron dans application.properties, suppression du code pour observer le comportement du cache L2 dans CustomerRepository (=> éviter des logs "parasites" dans la console de sortie)

- ajout des dépendances  io.quarkus:quarkus-keycloak-authorization & quarkus-oidc

- copie du fichier de configuration du realm Kecloak quarkus-realm.json du code source du livre dans le dans chapter07/ sous le nom quarkus-realm-original.json

- depuis IJ, modification du fichier quarkus-realm-original.json :
--recherche username pour trouver la définition des 2 utilsateurs utilisés. (-> = remplacer par)
	*(à partir de ligne 297) "username": "admin"->"joe" | firstName" : "Arno"->"Joe" (Note : a les realm Roles : "user" et "admin")
	*(à partir de ligne 327) "username" : "test"->"franck" |   "firstName" : "Theo"->"franck" | "lastName" : "Tester"->"User" | "email" : "tester@localhost"->"user@localhost" (note : a le realm role "user")

--recherche de clientId pour trouver la définiton du client (sécurisé par) Keycloak  qui sera mappé avec l'application cliente "keycloak-demo"
	*(ligne 441) clientId": "quarkus-client"-> "customer-service"
--recherche des autres références au client quarkus-client pour remplacer quarkus-client par customer-service :
	*(ligne 72) :  "customer-service" : [ ]
	*(ligne 559) : "type": "urn:customer-service:resources:default"
	*ligne 588) : "defaultResourceType": "urn:customer-service:resources:default",
Notes :
les rôles realm user et admin sont respectivement définis à la L48 et L63
les mots de passe sont chiffrés

- lancement du provider keycloak containerisé :
(cf. commentaires dan le livre SafariBooks https://learning.oreilly.com/library/view/hands-on-cloud-native-applications/9781838821470/e2b4068a-6125-45f3-9797-aec69a612f2c.xhtml)
docker run --rm --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8180 -it quay.io/keycloak/keycloak:7.0.1 -b 0.0.0.0 -Djboss.http.port=8180 -Dkeycloak.profile.feature.upload_scripts=enabled

-importation de quarkus-realm-original.json dans Keycloak :
localhost:8180 | Administration Console | authentification via admin:admin | Add Realmm | sélection du fichier json | Click sur Create
On parcours la console web (Roles, Clients | customer-service, Manage users | view all users, sélection <utilisateur> | onglet Role Mappings )

- dans application.properties, mapping du service (l'application cliente) avec le client "customer-service" Keycloak défini dans le realm quarkus-realm

<les annotation d'autorisation @RolesAllowed Jakarta EE sont positionner comme pour chapter07/elytron-demo : get, post accessibles aux admin et user | put, delete qu'accessibles aux admin>

- implémentation des tests d'accès à CustomerEndpoint dans la classe CustomerEndpointTest :
	* authentification de frank, utilisateur ayant le rôle user pour obtenir un access token correspondant à l'utilisateur authentifié (principal) auprès de Keycloak
	* authentification de joe, utilisateur ayant le rôle admin pour obtenir l'access token correspondant
	* on décommente les tests et on adapte pour passer l'access token "admin" ou "user" selon les cas testés :
	remplacement de .basic(...) par .oauth2(userTokern ou adminToken)
- lancement des tests : mvn clean compile test

- de manière similaire, implémentation des tests d'accès à OrderEndpoint dans la classe OrderEndpointTest & lancement des tests

- test fonctionnel avec POSTMAN (démarrer au préalable si ce n'est fait l'application : mvn quarkus:dev)
--obtention d'un access token 
	*POST http://localhost:8180/auth/realms/quarkus-realm/protocol/openid-connect/token
	*sélection du body | X-www-form-urlencoded
	*renseignement des clés / valeurs :
		client_id:customer-service
		username:joe
		password:test
		grant_type:password
		client_secret:mysecret
	* copie de l'access token dans la réponse
	* requête GET localhost:8080/customers/[id] | ajout Entête Authorization : Bearer <token copié>

- Récupération des informations de l'utilisateur authentifié à l'exécution dans CustomerEndpoint.getAll() :
	* Injection dans CustomerEndpoint de l'interface @io.quarkus.security.identity.SecurityIdentity représentant le principal
	* utilisation de SecurityIdentity dans getAll() pour logguer les infos de l'utilisateur authentifié
- on test avec la procédure Postman ci-dessus

2) Utilisation du standard Eclipse MicroProfile JWT pour sécuriser le service
<dans le livre c'est un projet à part>

- si l'appli est en cours d'exécution, on stoppe.

- dans pom.xml : ajout de la dépendance io.quarkus:quarkus-smallrye-jwt | commentaire des dépendances quarkus pour oidc et Keycloak

- Dans quarkus-realm, on vérifie que Keycloak émet bien pour le service d'id customer-service (keycloak est l'issuer) 1 access token JWT contenant des infos sur l'utilisateurs (claims) notamment les groupes auquel il appartient pour le mapping avec les rôle logiques applicatifs définis via @RolesAllowed :
clients | customer-service | onglet Mappers | Realm roles => notamment token claims name doit être groups
NOTE : l'implémentation du protocole OIDC dans Keycloak utilise déjà un token JWT pour l'access token - c'est pour cela qu'on ne fait que vérifier.

- dans applications.properties, configuration du service JWT - on commente la configuration précédente Keycloak
<la seule chose qui change est la configuration dans application.properties et la dépendance Quarkus. on est donc plus sur du standard mais c'est tout.

- lancement des tests comme précédemment : mvn clean compile test
- test fonctionnel avec Postman comme précédemment (lancer au préalable en mode dev avec mvn quarkus:dev)

- accès programatique dans CustomerEndpoint aux informations du token JWT délivré par Keycloak via l'API Eclipse MicroProfile :
	*injection de la représentation du token JWT :
@org.eclipse.microprofile.jwt.JsonWebToken
	*injection de la valeur du claim correspondant au nom utilisateur stocké dans le token - utilisation de @Claim (annotation MicroProfile) : 
@Inject @Claim(standard = Claims.preferred_username) Optional<JsonString> username
	*injection de la liste des groupes (claim groups) stockés dans le token JWT :
@Inject @Claim(standard = Claims.groups) Optional<JsonString> groups;
	* accès aux informations fournies par le token depuis getAll()

- lancement du test fonctionnel via Postman comme précédemment et observation des logs dans le terminal IJ

