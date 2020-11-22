**basé sur copie du projet hibernate**

- renommage fichier hibernate.iml en hibernate-panache.iml depuis IJ
-pom.xml/artifactId=>hibernate-panache (à la place de hibernate-demo)
-mise à jour des commentaires Dockerfile pour la consistance du projet

- ajout dans pom.xml de l'extension quarkus-hibernate-orm-panache
- suppression de l'extension quarkus-hibernate-orm qui est inutile car l'extension panache ajoute aussi les dépencances Hibernate 

-transformation des entités pour utiliser Panache
  * extension de io.quarkus.hibernate.orm.panache.PanacheEntityBase
  * champs publiques
  * suppression getters et setters (et des méthodes ajoutées par moi dans Customer)
  * suppression des requêtes nommées (dans le livre elles sont présentes mais pas dans le code source du livre).

-mise à jour des repository pour utiliser Panache (suppr EM, utilisation des méthodes statiques EntityBase, remplacement des getters et setters par utilisation champ)

-mise à jour OrderEndpoint.update(Order order) : remplacement order.getId() dans la méthode par order.id

-lancement du container PostgreSQL <commande identique à celle pour l'exe locale du projet hibernate>
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5

-lancement en mode dev : mvn compile quarkus:dev (ne lance pas les TU)
-test fonctionnels via l'UI comme pour le projet hibernate (localhost:8080)

-déploiment de l'exe natif dans Minishift (cf. readme_hibernate.txt) :
créa app PostgreSQL dans Minishift | lancement script deploy-minishift.sh
<les scripts de déploiement dans Minishift n'ont pas besoin d'être modifiés=> l'app déployée est tjs nommée quarkus-hibernate>

- comme précédemment nettoyage des ressources Minishift (oc delete all --all) et suppression de l'image docker Minishift de l'appli.

