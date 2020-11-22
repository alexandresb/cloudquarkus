#création du jar (runner) - les tests sont esquivés pour avoir à éviter de lancer le container docker PostgreSQL
mvn install -DskipTests=true

# création de la config du build binaire
oc new-build --binary --name=quarkus-hibernate -l app=quarkus-hibernate

# ajout du Dockerfile pour image native au fichier de configuration du build
oc patch bc/quarkus-hibernate -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"src/main/docker/Dockerfile.jvm"}}}}'

# création du build dans Minishift depuis le dossier courant (dossier projet racine hibernate)
oc start-build quarkus-hibernate --from-dir=. --follow

# Création de l'application à partir du build
oc new-app --image-stream=quarkus-hibernate:latest

# Création de la route
oc expose svc/quarkus-hibernate
