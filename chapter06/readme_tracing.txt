Quarkus 1.3.4.Final- GraalVM 19.3.1 Java 8

tracing distribué via OpenTracing Eclipse MicroProfile 3.3

1) doc par défaut

- copie du projet chapter06/metrics =>renommage du dossier,iml, dans pom

- suppression des commentaires, des annotations de métrique pour "alléger" (ce qui n'est pas déjà lourd), des imports inutilisés, et de la dépandance mvn "metrics"

- ajout dépendance io.quarkus:quarkus-smallrye-opentracing + reload dans menu Maven IJ pour prise en compte de la dépendance

- ajout dans applications.properties des propriétés de configuration pour communiquer avec le serveur Jaeger collectant les traces "distribuées (cf. commentaires dans applications properties).

- lancement depuis un shell d'un serveur Jaeger containerisé (lors du 1er lancement, l'image est téléchargée)
docker run -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 -p 9411:9411 jaegertracing/all-in-one:latest

-dans un autre shell lancement de la base quarkusdb containerisé (comme précédemment dans les autres projets)

-lancement de l'appli quarkus customer-service (opentracing) : mvn compile quarkus-dev

-ouverture de l'UI web Jaeger : localhost:16686
on voit qu'aucun service n'est référencé dans le menu search | menu déroulant service
- si on émet des requêtes vers notre service customer-service (opentracing),
=> quarkus-service (nom de notre appli dans Jaeger) est désormais listé dans le menu déroulant.
- On sélectionne une op dans la liste Operations (par ex : All) et on clic sur find Trace pour visualiser les traces (des requêtes vers customer-service) qui ont été émises.
- si on clique sur trace on peut visualiser des infos concernant la requête tracée.

NOTE : en mode dev, si on modifie le projet quarkus (par exemple en ajoutant des commentaires dans applications.properties) pendant que le syst de tracing fonctionne,
alors après le hot replace, une exception est levée au sein du service=> la communication  entre le tracer et le serveur Jaeger est perdue et le service est "corrompu"
Il faut relancer le microservice customer-service (opentracing) Quarkus 1.3.4.Final.

- stoppe de la bbd et du serveur Jaeger (Ctrl+C) & mvn clean


