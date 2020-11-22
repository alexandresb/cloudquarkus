# création de la config du build binaire
oc new-build --binary --name=quarkus-customer-service -l app=quarkus-customer-service

# ajout du Dockerfile pour image native au fichier de configuration du build
oc patch bc/quarkus-customer-service -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"src/main/docker/Dockerfile.native"}}}}'

# création du build dans Minishift depuis le dossier courant (dossier racine customer-service)
oc start-build quarkus-customer-service --from-dir=. --follow

# Création de l'application à partir du build
oc new-app --image-stream=quarkus-customer-service:latest

# Création de la route
oc expose svc/quarkus-customer-service
