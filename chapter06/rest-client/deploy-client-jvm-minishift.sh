#création du jar (runner) - les tests sont esquivés pour avoir à éviter de lancer le container docker PostgreSQL
mvn install -DskipTests=true

# création de la config du build binaire
oc new-build --binary --name=rest-client -l app=rest-client

# ajout du Dockerfile pour image native au fichier de configuration du build
oc patch bc/rest-client -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"src/main/docker/Dockerfile.jvm"}}}}'

# création du build dans Minishift depuis le dossier courant (dossier projet racine hibernate)
oc start-build rest-client --from-dir=. --follow

# Création de l'application à partir du build
oc new-app --image-stream=rest-client:latest

# Création de la route
oc expose svc/rest-client
