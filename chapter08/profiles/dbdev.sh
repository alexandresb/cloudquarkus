# --ulimit memlock=-1:-1 : on autorise une quantité de mémoire indéfinie à être verouillé - c'est souvent le cas pour certaines bases=>maintien de plusieurs sessions
#-it : connexion d'un shell interactif au container=>on peut voir les logs
#--rm=true destruction du container lorsqu'on en sort / on quitte
#--memory-swappiness : pas de swap mémoire pour le container
# -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus : def/créa d'un compte pour accéder à la bdd - ce compte est déclaré dans application.properties pour autoriser l'app à se connecter
#-e POSTGRES_DB=postgresDev : création d'une base nommée postgresDev
# -p 5432:5432 : mapping port hote : port du hote client avec le port dans le container
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_Dev -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=postgresDev -p 5432:5432 postgres:10.5