#Création d'un build depuis une source locale (build binaire) - build pour 1 app quarkus-kafka (tag app=quarkus-kafka)
oc new-build --binary --name=amqp-trading -l app=amqp-trading
#Ajout de l'emplacement pour constuire une image Docker contenant l'exe natif
oc patch bc/amqp-trading -p "{\"spec\":{\"strategy\":{\"dockerStrategy\":{\"dockerfilePath\":\"src/main/docker/Dockerfile.native\"}}}}"
#Création de l'image Stream dans OpenShift en uploadant les artefacts depuis le dossier courant
oc start-build amqp-trading --from-dir=. --follow
# Création dans OpenShift de l'application depuis l'image stream
oc new-app --image-stream=amqp-trading:latest
# création d'une route pour accéder à l'application quarkus-kafka depuis l'exterieur.
oc expose service amqp-trading
