Quarkus 1.3.2.Final & 1.3.4.Final- GraalVM 19.3.1 Java 8

exposition des métriques avec l'API Metrics Eclipse MicroProfile 3.3

- copie du projet chapter05/hibernate =>renommage du dossier,iml, dans pom et renommage du package "de base" (org.cesi.chapter06)

- ajout dépendance io.quarkus:quarkus-smallrye-metrics + reload dans menu Maven IJ pour prise en compte de la dépendance

-annotation de CustomerEndpoint.getAll() avec @org.eclipse.microprofile.metrics.annotation.Counted(description = "Customer list count",absolute = true)

-si non démarrée, démarrage de la base dockerisé quarkusdb
-lancement est accès à L'UI ou à localhost:8080/customers pour charger la liste des customers et déclencher le comptage du nombre d'invocation de getAll

- accès à l'ensemble des métriques dont métriques systèmes : localhost:8080/metrics
- accès aux métriques spécifiques de l'application : localhost:8080/metrics/applications

- annotation de CustomerEndpoint.getAll() avec @Timed(name = "timerCheck", description = "How much time it takes to get the Customer list", unit = MetricUnits.MILLISECONDS)  = métrique de type timer pour avoir des informations sur le temps d'exécution  pour obtenir la liste de tous les clients

- accès comme ci-dessus

- utilisation d'une jauge personnalisée permettant de connaitre le nombre total de commandes (orders) :
  * ajout d'une requête nommée sur Order comptant le nb total d'orders
  * ajout de la méthode OrderRepository.countAll retournant le nombre total des commandes (utilise la requête nommée)
  * ajout de la méthode OrderEndpoint.getTotalNumberOfOrder() (invoquant countAll) annotée avec @Gauge(name = "peakOfOrders", unit = MetricUnits.NONE, description = "Highest number of orders", absolute = true)
  * ajout de l'annotation @ActivateRequestContext sur OrderEndpoint.getTotalNumberOfOrder() pour que la jauge soit exportée. c'est une des solution de contournement pour Quarkus 1.3.2./1.3.4.Final afin que l'export de la jauge fonctionne quand JPA est utilisé. Sinon la jauge n'est pas exportée.

- accès comme ci-dessus
  


