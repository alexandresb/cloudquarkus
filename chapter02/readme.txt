---Quarkus 1.0.0.Final - GraalVM 19.2.1 CE----
1) création du projet avec Maven :
se placer dans le dossier chapter02.
lancer la commande mvn :
mvn io.quarkus:quarkus-maven-plugin:1.0.0.Final:create -DprojectGroupId=org.cesi.chapter02 -DprojectArtifactId=hello-rest -DclassName="org.cesi.chapter02.SimpleRest" -Dpath="/helloworld"

=>l'application créée (en Avril 2020) utilisera Quarkus & quarkus-maven-plugin 1.3.2.Final  (pas la version 1.0.0.Final) 
Donc mvn io.quarkus:quarkus-maven-plugin:1.0.0.Final:create...
<=> mvn io.quarkus:quarkus-maven-plugin:1.0.0.Final:create...
<=> mvn io.quarkus:quarkus-maven-plugin:create

Pour utiliser 1.0.0.Final, il faut modifier à la main le pom.xml
Si on change la version dans le pom.xml, pour qu'IJ prenne en compte le changement, il faut faire un reimport du projet dans la fenêtre Maven d'IJ.

la version de Java (8 ou 11) déclarée dans le pom.xml dépend de la version Java livrée avec GraalVM.

exécuter l'application :
se placer dans hello-rest et lancer : mvn compile quarkus:dev

test GET du service : http://localhost:8080/helloworld

2) création du projet avec Gradle :
<Gradle doit installé - version conseillée 6.2.2>
Note : quarkus-maven-plugin:1.0.0.Final ne fonctionne pas.

lancer la commande mvn en spécifiant que l'outil est Gradle
mvn io.quarkus:quarkus-maven-plugin:1.3.2.Final:create -DprojectGroupId=org.si.chapter02 -DprojectArtifactId=hello-rest -DclassName="org.cesi.chapter02.SimpleRest" -Dpath="/helloworld" -DbuildTool=gradle

se placer dans le dossier hello-rest et lancer avec :
./gradlew quarkusDev

3) création du projet via https://code.quarkus.io/
création d'un projet utilisant :
	Quarkus et du plugin Quarkus & plugin 1.3.2.Final
	Java 11

4) création d'image native
**Note : 
Quarkus 1.3.2.Final ne peut pas utiliser GraalVM 19.2.1 pour générer une image native du service REST.
Quarkus 1.3.2.Final supporte officiellement GraalVM 19.3.1 et 20.0.0
cependant Quarkus peut compiler exécuter une application Java REST "simple" avec GraalVM 19.2.1 car Java 8 est utilisé.

créer un exécutable natif : mvn package -Pnative (depuis le dossier racine du projet)
Exécuter un exé natif : target/hello-rest-1.0-SNAPSHOT-runner((depuis le dossier racine du projet)
connaitre la mémoire utilisée par l'exe natif : ps -o pid,rss,command -p $(pgrep -f hello-rest)
la commande listée contient le motif hello-rest

test d'intégration de l'image native : mvn verify -Pnative




