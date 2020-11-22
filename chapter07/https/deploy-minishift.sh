#création de l'image native pour container - c'est l'exe qui sera déployé dans Minishift
#les tests sont esquivés pour avoir à éviter de lancer le container docker PostgreSQL
mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:19.3.1-java8 -Dquarkus.native.native-image-xmx=6000m -DskipTests=true

# création de la config du build binaire
oc new-build --binary --name=quarkus-hibernate -l app=quarkus-hibernate

# ajout du Dockerfile pour image native au fichier de configuration du build
oc patch bc/quarkus-hibernate -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"src/main/docker/Dockerfile.native"}}}}'

# création du build dans Minishift depuis le dossier courant (dossier projet racine hibernate)
oc start-build quarkus-hibernate --from-dir=. --follow

# Création de l'application à partir du build
oc new-app --image-stream=quarkus-hibernate:latest

# Création de la route
oc expose svc/quarkus-hibernate

#Note : 
#ajout des propriétés systèmes -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:19.3.1-java8  ET -Dquarkus.native.native-image-xmx=6000m. 
#Cela permet la création de l'exe natif pour container Docker en s'appuyant sue une image ubi-minimal contenant Quarkus 19.3.1, l'outil native-image et Java 8 au lieu de Java 11 et en allouant 6Go de RAM au tas Java dans le container où l'exe natif pour container est créé.
#En effet la création, avec GraalVM 19.3.1-Java11", d'un exe natif intégrant hibernate consomme plus de ressources mémoire qu'avec Java 8 et échoue avec les options par défaut. 
#C'est pour cela qu'on spécifie explicitement l'utilisation d'une image "Java 8" nécessitant moins de ressources mémoires et l'allocation e 6 Go de RAM car même si Java 8 nécessite moins de mémoire, il faut quand même lui allouer plus de mémoire pour éviter l'echec de création de l'exe natif. 
#on utilise 6Go de Ram pour ne pas dépasser les capacités mémoires de docker run.
