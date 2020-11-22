Quarkus 1.3.4.Final- GraalVM 19.3.1 Java 8

---communication via https---

- copie du projet chapter05/hibernate et renommage pour adapter comme précédemment (package, iml, pom.xml)

1) utilisation d'1 PEM

- depuis chapter07/https, on génère la paire certificat PEM et clé privée PEM :
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem
(on entre FR pour le contry name - cucuron comme état/province & comme localisation (ville) - aso comme organisation - chap07 comme section - cesi comme nom - . (donc vide) pour email)
=> le certificat et la clé cert.pem et key.pem sont créées à la racine du projet.

- dans application.properties,
	on renseigne l'emplacement du certificat et de la clé.
	on assigne le port d'écoute https (ssl) d'Undertow


-lancement de la base PostgreSQL quarkusdb dockerisé (comme précédemment) :
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5

-lancement de l'application (mvn quarkus:dev) et accès à l'UI via https://localhost:8443 et tests fonctionnels

2) Utilisation d'un keystore

- depuis chapter07/http, génération d'un keystore en indiquant dans la commande le mot de passe (ici password)
keytool -genkey -keyalg RSA -alias quarkus -keystore keystore.jks -storepass password -validity 365 -keysize 2048
(on renseigne les infos demandées : CN=alex sbriglio, OU=aso, O=cesi, L=cucuron, ST=cucuron, C=FR - yes pour indiquer que les infos sont correctes - return pour utiliser le même mdp que celui du keystore indiqué dans la commande)
?ote : on a une alerte qu'il faut utiliser un format standard et non le format JKS proprio.
=> keystore.jks est créé à la racine du projet https

- dans application.properties,
	on commente les entrées PEM
	ajout des entrées indiquant la localisation de keystore.jks et le mot de passe
	
- relancement de l'application (Ctrl+C puis mvn quarkus:dev) et accès à l'UI via https://localhost:8443 et tests fonctionnels
Note : on doit relancer car les modifs dans application.properties pour la comm https ne sont pas pris en compte lors du hot replace

