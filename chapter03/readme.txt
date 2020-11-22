- utilise Quarkus 1.3.2 et Graal 19.3.1
- projet Maven créé avec la commande :
mvn io.quarkus:quarkus-maven-plugin:1.0.0.Final:create -DprojectGroupId=org.cesi.chapter02 -DprojectArtifactId=hello-rest -DclassName="org.cesi.chapter02.SimpleRest" -Dpath="/helloworld"

-pour tester :
mvn test ou mvn install => l'application java (jar) est construite dans les 2 cas.

-Lancer en mode dev : mvn quarkus:dev
-Lancer en mode "prod" : java -jar target/hello-okd-1.0-SNAPSHOT-runner.jar

test fonctionnel : GET http://localhost:8080/containerId => réponse retourné : service s'exécutant dans inconnu

1) exécuter l'application JVM dans un container
<les instructions sont données dans le fichier Dockerfile.jvm>
-créer une image docker d'une application JVM :
(depuis le dossier racine du projet hello-okd)docker build -f src/main/docker/Dockerfile.jvm -t quarkus/hello-okd-jvm .


-exécution du container : docker run -i --rm -p 8080:8080 quarkus/hello-okd-jvm

test fonctionnel GET retourne un message de type : service s'exécutant dans <container id>

2) exécuter l'application native dans un container
<les instructions sont données dans le fichier Dockerfile.jvm>
-installer native-image : gu install native-image

- créer un exe natif dans (adapté à) un container (x) :
mvn package -Pnative -Dquarkus.native.container-build=true (option permettant de créer un exe natif pour un container)
Note : cette op nécessite le lancement d'un container préalable pour pouvoir créé une appli native pour un container.

-créer l'image docker contenant l'appli native :
docker build -f src/main/docker/Dockerfile.native -t quarkus/hello-okd-native . (op rapide).

-exécuter le container : docker run -i --rm -p 8080:8080 quarkus/hello-okd-native

-test fonctionnel GET comme en 1)

3) créer et déployer dans Minishift
<test avec Mini utilisant KVM>
Note: plud de détail dans OpenShift.docx

<nécessite que l'exe natif pour container ait été au préalable créé.Ce qui est le cas : cf. (x) - ne nécessite pas la création d'image Docker préalable en local>

- on se place par ex sous hello-okd
- se logguer via oc en tant que developer

-création du projet /espace de nom quarkus-project :
oc new-project quarkus-project

- création d'un build binaire hello-build dans Mini :
oc new-build --binary --name=quarkus-hello-okd -l app=quarkus-hello-okd

-ajout du Dockerfile.native au build :
 oc patch bc/quarkus-hello-okd -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"hello-okd/src/main/docker/Dockerfile.native"}}}}'

-lancement de la création du binaire dans Minishift :
oc start-build quarkus-hello-okd --from-dir=. --follow

-création de l'application minishift à partir de l'image stream :
oc new-app --image-stream=quarkus-hello-okd:latest

-création de la route pour accéder à l'appli depuis l'extérieur :
oc expose svc/quarkus-hello-okd

test de la fonctionnalité GET :
http://<route>/containerId (attention sensibilité à la casse)
ex : http://quarkus-hello-okd-quarkus-project.192.168.42.90.nip.io/containerId

la sortie indique le pod dans lequel l'application s'exécute, par ex : service s'exécutant dans quarkus-hello-okd-1-zvhsn

4) scaling horizontal
-modification de la deployment configuration (dc) pour limiter de la mémoire utilisée par un pod à 50 MB :
oc set resources dc/quarkus-hello-okd --limits=memory=50M

- upscale de l'application à 10 instances de pod :
oc scale --replicas=10 dc/quarkus-hello-okd
<rappel : même si l'upscaling est rapide pour arriver à 10 pods - il faut un moment pour que l'appli soit accessible depuis l'ext>

-test de charge avec cURL :
for i in {1..100}; do curl http://quarkus-hello-okd-quarkus-project.192.168.42.90.nip.io/containerId; echo ""; done;
<test avec 1000 req effectué aussi>

-suppression de toutes les ressources en spécifiant (-l) le label (app=quarkus-hello-okd)
oc delete all -l app=quarkus-hello-okd
ici <=> oc delete all --all (car 1 seule appli déployée)










