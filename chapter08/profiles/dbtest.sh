# cf. dbdev.sh pour 1 description complète des flags
# -e PGPORT=6432 : permet de définir explicitement le port d'écoute de la base postgresTest
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_Test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=postgresTest -e PGPORT=6432 -p 6432:6432 postgres:10.5