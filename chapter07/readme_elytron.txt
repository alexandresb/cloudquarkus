Quarkus 1.3.4.Final- GraalVM 19.3.1 Java 8

- copie du projet chapter05/hibernate et renommage comme pour les autres projets (dossier racine, iml, base du package, artifactId), et remplacement version Quarkus par 1.3.4.Final

- ajout de la dépendance Elytron (gestionnaire de sécurité Java EE) io.quarkus:quarkus-elytron-security-jdbc

- ajout en début d'import.sql (sous resources) de la table et d'un jeu de donnés utilisés par Elytron pour sécuriser l'application.

- configuration d'Elytron dans application.properties :on indique qu'on utilise l'authentification JDBC et on configure le mapper Elytron qui va "créer" le principal représentant l'utilisateur authentifié

- annotation de la classe OrderEndpoint avec l'annotation Java EE @javax.annotation.security.RolesAllowed({"admin","user"})=> les utilisateurs admin et user peuvent invoquer toutes les méthodes de ressources
- annotation des méthodes @DELETE et @PUT avec @RolesAllowed("admin") : l'accès à ces méthodes est redéfini, seuls les admin peuvent les invoquer (seuls les requêtes contenant une identité admin peuvent accéder à ces ressources).

- mise en place de la classe de Test (CustomerEndpointTest) pour tester la sécurité du pt de terminaison CustomerEndpoint (les tests on été mis en place et lancés au fur et à mesure) :
<les tests ont été adaptés et ne suivent pas exactement ceux du livre
les tests de OrderEndpoint dans CustomerEndpointTest sont mis en commentaires>
la base containerisé quarkusdb doit être lancé pour l'exécution des tests -cf readme précédents)
	* test qu'un "user" (frank) peut lister tous les customers
	* test qu'un "admin" (joe) peut lister tous les customers
	* test qu'un "user" peut créer un Customer (j'ai aussi testé pour "admin" en modifiant le code du test)
	* test qu'un "admin" peut retrouver un customer donné en fonction de son id
	* test qu'un "admin" peut modifier un customer
	* test qu'un "user" n'est pas autorisé à supprimer un customer
	* test qu'un "admin" est autorisé à supprimer un customer
- lancement des tests : mvn [clean] compile test

- création de la classe de test @QuarkusTest OrderEndpointTest pour tester OrderEndpoint sécurisé.
<les tests du point de terminaison Order (crées pour chapter05) initialement dans la classe CustomerEndpointTest sont déplacés dans OrderEndpoint et modifier pour intégrer les infos d'authentification>.
- lancement des tests : mvn [clean] compile test

<Les tables et séquences de la base sont recréées à chaque cycle de test (mvn test). Par contre ces objets et les données de la base sont maintenus pendant toute la séquence de test.>

- test fonctionnel (pas dans le livre) :
	* ajout dans application.properties la propriété activant l'auth http basique : quarkus.http.auth.basic=true (je pense que c'est utile que pour le test via navigateur mais je ne suis pas sûr - à tester)
	*lancement de l'appli en mode dev : mvn quarkus:dev
	* dans le navigateur : GET http://localhost:8080/customers saisie un login et mdp valides dans la pop-up d'authentification
	* dans Postman : ouvrir le menu Authorization | sélectionner basic authentication | saisir un login et mdp valides | configurer et lancer la requête.
	pour le test on peut lancer une simple requête GET

	

