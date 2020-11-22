Quarkus 1.3.2.Final - GraalVM 19.3.1 Java 8

Mise en place de Health check avec Eclipse MicroProfile 3.3

1) Manipulation de l'API MicroProfile

- génération du projet depuis https://code.quarkus.io/ : groupId : org.cesi.chapter06 - artifactId: health - coche de Cloud | SmallRye Health
- Depuis IJ, modif pom.xml : remplacement Java 11 par Java 8 | remplacement version Quarkus 1.4.2.Final par Quarkus 1.3.2.Final

- Création de la classe DBHealthCheck vérifiant si la base Quarkus est en service (up) - implémente l'interface fonctionnelle org.eclipse.microprofile.health.HealthCheck

-annotation avec @Health(déprécié) ou @Readiness ou @Liveness pour que les infos de santé soient exposées.

- création de la méthode privée serverListening créant une socket vers le SGBD PostgreSQL

- Dans application.properties, ajout des var d'env db.host et db.port correspondant respectivement à l'hôte hébergeant le serveur PostgreSQL et à son port d'écoute 

- injection (@ConfigProperties) des propriétés host et port du SGBD déclarées dans application.properties

- implémentation de la méthode HealthCheck.call vérifiant si le SGBD est en service ou non

- démarrage du SGBD :
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5

- lancement en mode dev du service health

- accès à localhost:8080/health regroupant toutes les infos / tous les indicateurs de santé exposées/ disponibles pour ce service.

- création d'un pt de terminaison @Readiness indiquant si le service (Health) est prêt pour servir les clients
Ici on simule que le service est dispo pour servir des clients si il n'y pas un fichier verrou sous tmp.

- accès à localhost:8080/health (regroupe ttes les infos de santé remontées) ou localhost:8080/health/ready (affiche que les infos de santé correspondant aux implémentations HealthCheck annotées @Readiness).
=>pour tester le service down du fait du SF non prêt/non dispo on crée un fichier /tmp/tmp.lck avec du texte dedans (gedit tmp/tmp.lck - texte puis enregistrement).

- création d'un pt de terminaison @Liveness indiquant si le service (Health) est en vie.

accès à localhost:8080/health (regroupe ttes les infos de santé remontées) ou localhost:8080/health/live (affiche que les infos de santé correspondant aux implémentations HealthCheck annotées @Liveness).
Ici on teste que la mémoire libre de la JVM dans laquelle s'exécute le service est >= à 1024 mo (ce n'est pas le cas).

2) Déploiement dans Minishift

- Connexion au namespace quarkus-project :
oc login puis oc project quarkus-project

- création de la base quarkusdb dans Minishift pas exposée à l'ext :
oc new-app -e POSTGRESQL_USER=quarkus -e POSTGRESQL_PASSWORD=quarkus -e POSTGRESQL_DATABASE=quarkusdb postgresql

- création sous le dossier health du script de déploiement deploy-minishift.sh qui construit une image native,la déploie dans Minishift sous le nom quarkus-microprofile
(copier/coller du script du livre)
- lancement :
sh deploy-minishift.sh
<l'étape la plus longue est la créa de l'exe natif. Les autres étapes sont rapides>

- ouverture de la console web pour connaitre la route (adresse pour accéder depuis l'ext) et accès aux infos /indicateurs de santé :
(par exemple)
http://quarkus-microprofile-quarkus-project.192.168.42.90.nip.io/health
Note : on peut ouvrir une comm socket à la BDD et il y a assez de mémoire libre (>= 1024 Mo - cf. note plus bas) => tous les indicateurs sont up

- mise en place des sondes Kubernetes permettant de prendre automatiquement des mesures si le container n'est pas prêt (readiness) à recevoir des requêtes clients et/ou si il n'est pas en exécution (courantes) / pas en vie (liveness)  :
depuis la console web, Applications | Deployments | déploiement quarkus-microprofile | Actions | Edit Health Checks
  * readiness probe - si les indicateurs health/ready sont DOWN aucune requête n'est routée vers l'app :
   Add Readiness Probe | Path : health/ready - pour le reste on laisse les défauts - Save (on peut ajouter la sonde liveness avant Save)
  * liveness probe - si les indicateurs health/live sont DOWN le pod est stoppé :
  Add Liveness Probe | Path : health/live - pour le reste on laisse les défauts - Save
Notes :
l'ajout des sondes entraine un redéploiement du pod.
on peut ajouter / déploiement une sonde Readiness et une sonde Liveness

- ajout d'un fichier verrou pour que le service passe en état non prêt :
  * obtention du pod en exé : oc get pods | repérer le pod quarkus-microprofile avec statut running (ex : quarkus-microprofile-2-wdrfk)
  * ouverture d'une session dans le pod : oc rsh quarkus-microprofile-2-wdrfk
    pour fermer la session/sortir du pod : exit;
  * création du fichier /tmp/tmp.lck : touch /tmp/tmp.lck
  => un message indique que l'application n'a pas passer la vérification Readiness -l'app ne peut recevoir de requêtes clientes 
     Si on veut accéder à l'app depuis l'ext (http://quarkus-microprofile-quarkus-project.192.168.42.90.nip.io/health), on a une réponse indiquant application non dispo.
  <pour fermer la session/sortir du pod : exit;>
  *visualiser les évènements dans le projet pour voir l'évènement indiquant que le service n'a pas passer la vérification readiness (readiness probe failed) :
  oc get events | dernier évènement de la liste.
  *suppression du fichier verrou
  =>l'appli redevient disponible (prête à recevoir les req clients).

Note concernant Le test liveness qui vérifie si la mémoire dispo (libre) dans l'env d'exécution est supérieure à 1024 Mo :
Minishift a été démarré avec 2 Go de Ram (défaut). cette mémoire est celle dispo pour l'exe des services déployés - c'est la mémoire attribuée à l'env d'exécution (runtime). la base postgresql et le microservice quakus-microprofile consomment peu de mémoire. Il y a donc au moins 1024 Mo (1 Go) libre dans l'env d'exécution.
Parc conséquent la sonde Liveness n'échoue pas car elle récupère un indicateur de mémoire UP.

- suppression de toutes les ressources déployées (base et microservice)  : oc delete all --all 








