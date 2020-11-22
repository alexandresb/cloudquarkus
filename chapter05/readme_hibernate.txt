Quarkus 1.3.2.Final - GraalVM 19.3.1 Java 8

-----hibernate-----
1) mise en place du code et déploiement local
- copie de chapter04/basic/customer-service et renommage du dossier projet en hibernate
- renommage de customer-service.iml en hibernate.iml (dans IJ)
- pom.xml : groupId=org.cesi.chapter05 | artifactId = hibernate-demo
- renommage racine package en org.cesi.chapter05

dans pom.xml ajout des extensions dépendances :
  *quarkus-hibernate-orm
  *quarkus-agroal - gestion par le runtime du pool de connexions
  *quarkus-JDBC postgresql - pour accéder via la SPI JDBC le SGBD PostgreSQL
App->JPA->JDBC | Agroal->driver postgre->BDD

<j'ai aussi mis à jour les commentaires des Dockerfile pour remplacer customer-service par hibernate-demo>


- Customer :
   *supression setId
   *remplacement Integer par Long pour id (IJ > refactor > type migration) | suppression du code générant un id (utilise int) dans CustomerRepository
   *Ajout des annotations de définition d'entité et de mapping JPA sur Customer

- création et annotation de l'entité Order 
note : dans le livre c'est orders, mais c'est mal choisi. Cela a été fait pour éviter une erreur DDL lors de la génération de la table
car Order est un mot clé SQL réservé. J'ai pour ma part annoté l'entité Order avec @Table(name="orders")

-définition de la relation bidirectionnelle Customer <-OneToMany-> Order(propriétaire de la rel.)
  * ajout dans Customer des méthodes addOrder/removeOrder/getOrders (non présent dans le livre) - non utilisées dans mon code

- MAJ de CustomerRepository pour utiliser l'EntityManager (on supprime entre le champ List<Customer> customers) : @Inject EM, @Transactional...

- création d'OrderRepository (@ApplicationScoped, @Inject EM, @Transactional...)

- création du point de terminaison REST OrderEnpoint.

- configuration de JDBC dans application.properties pour une connexion à la base PostgreSQL quarkusdb (creds : quarkus/quarkus)
- configuration Hibernate dans application.properties pour utiliser le mode JPA Drop & Create
- configuration de la Datasource Agroal dans application.properties
- sous création du fichier resources/import.sql contenant 2 insert dans la table customer
  ET indication du fichier import.sql dans application.properties permettant d'insérer ces 2 customers après création des tables dans la base.

- Lancement d'un container Docker PostgreSQL nommé quarkus_test avec création de la base quarkusdb et des creds quarkus/quarkus :
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5
<Note: lorsqu'on stoppe le container Ctrl+C ou docker stop quarkus_test), celui-ci est détruit car option --rm=true>


- copier/coller du code source de test du chapitre 5 dans la classe de test CustomerEndpointTest
- lancement du test :
mvn compile test

- création de l'UI Web Angular.js par copier/coller du code source du livre :
   *dans page index.html, ajout de l'hyperlien pour accéder au formulaire de gestion des orders d'un customer
   *création de la page order.html

-lancement appli et test fonctionnel :
mvn quarkus:dev
<Note: Si le code n'a pas été compilé au préalable ou s'il y a besoin de recompiler, mvn quarkus:dev lance la compile avant l'exe)>

- mise en place du caching L2
  *annotation de entité Customer avec @Cacheable
  * activations des logs SQL dans application.properties : quarkus.hibernate-orm.log.sql=true
  * (non présent dans livre) création de la méthode @GET CustomerEndpoint.findCustomer(@PathParam("customerId") Long id) pour tester @Cacheable
  * (non présent dans le livre) ajout de code dans CustomerRepository pour vérifier si les données basiques des entités sont mises en cache partagé L2 (EM.getEMF.getCache())
  * test via localhost:8080/customers/1 (en coulisse em.find)
        - si entité Customer n'est pas dans le cache L2, requête SQL générée pour récupérer l'entité - entité (ses données non collection) mise dans L2
        - si entité Customer dans cache L2, pas de SQL, l'entité est récupéré depuis le cache
  * test via localhost:8080/customers ou reload de page web localhost:8080 (en coulisse req JPQL "SELECT c FROM Customer c ORDER BY c.id")
         - A chaque requêtage de la liste des customers, ordre SQL executé.
         <Cependant les données des entités sont  cachées dans L2>
	 - si ensuite localhost:8080/customers/1, pas d'ordre SQL pour retrouver le customer 1 car mis en cache via l'ordre SQL précédent.

- mise en place du cache de requête pour Customer via l'hint @QueryHint(name = "org.hibernate.cacheable", value = "true")
- test via localhost:8080/customers ou reload de page web localhost:8080 (en coulisse req JPQL "SELECT c FROM Customer c ORDER BY c.id")
=> Lors du premier chargement, la requête SQL est exécutée ensuite non.

2) déploiement dans Minishift

- connection au projet quarkus_project : oc login | oc project quarkus-project

- création d'une application de type serveur PostgreSQL hébergeant la base quarkusdb (credentials = quarkus / quarkus) :
oc new-app -e POSTGRESQL_USER=quarkus -e POSTGRESQL_PASSWORD=quarkus -e POSTGRESQL_DATABASE=quarkusdb postgresql

-vérifier la mise en service de PostgreSQL via oc :
oc get services

- (non présent dans le livre) connaitre /tester la valeur des vars d'env POSTGRESQL hôte et port dans le namespace quarkus-project depuis la console web Minishift | terminal :
echo $POSTGRESQL_SERVICE_HOST / echo $POSTGRESQL_SERVICE_PORT
<var utiliser pour la connexion à la base dans application.properties>

2.1) déploiement d'une appli native

- création du script deploy-minishift.sh pour automatiser le déploiement d'un exe natif dans Minishift | quarkus-project
   *(non présent dans le livre) Il a fallut ajouter les props systèmes -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:19.3.1-java8  ET -Dquarkus.native.native-image-xmx=6000m pour réussir la créa de l'image native. Ca a été long à résoudre
   * pour le reste du fichier c'est juste une petite adaptation / au script du chapitre 4.

- depuis le dossier projet hibernate, lancement du script sh deploy-minishift.sh
<c'est un peu long car en premier étape, l'image Docker doit être téléchargée pour permettre de créer un exe natif destiné à un container Docker>

2.2) déploiement d'une appli java (JVM) - non présent dans le livre
- création par copier/coller du script deploy-jvm-minishift.sh et modif :
   *création de l'image native remplacée par mvn install
   * patch de la config de build avec le fichier Dockerfile.jvm

- depuis le dossier projet hibernate, lancement du script sh deploy-jvm-minishift.sh

- Pour 2.1) et 2.2) test fonctionnel comme en local mais en utilisant la route exposant le service déployé dans Minishift :
http://quarkus-hibernate-quarkus-project.192.168.42.90.nip.io/
<Si on essaie de supprimer un consumer ayant des orders associés, il y a une erreur. Il faut d'abord supprimer les orders associés. Cela est dû à la relation un-plusieurs entre Customer et Order et la façon de gérer la suppr d'un Customer.(remarque aussi valable bien évidemment pour l'exe locale)>







