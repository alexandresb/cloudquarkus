Quarkus 1.3.2.Final - GraalVM 19.3.1 Java 8

Mise en place de la tolérance aux pannes avec Eclipse MicroProfile 3.3

- copie du projet chapter05/hibernate =>renommage du dossier, dans pom, etc.(comme dans les cas précédents)

- ajout dépendance io.quarkus:quarkus-smallrye-fault-tolerance + reload dans menu Maven IJ pour prise en compte de la dépendance

1) utilisation de @Timeout/@Fallback/@Retry

- suppression des instructions que j'avais ajouté pour regarder le comportement du cache L2 dans CustomerRepository.findAll/findCustomerById

- annotation de findAll() avec @org.eclipse.microprofile.faulttolerance.Timeout(250L)

-déclaration d'une variable d'instance Logger SL4J pour logguer :private static final Logger LOGGER = LoggerFactory.getLogger("CustomerRepository");

- ajout par copier/coller du code source de fallback retournant une liste statique de customers (List<Customer> findAllStatic() & buildStaticList())
<j'ai donné le nom Static aux customers John et fred pour bien visualiser l'invocation de la méthode de fallback>

- annotation de findAll() avec @Fallback(fallbackMethod = "findAllStatic")

- implémentation d'une méthode utilitaire randomSleep mettant "en pause" le thread durant une durée aléatoire entre 1 et 400 msec
- invocation de randomSleep dans findAll() pour que parfois la méthode ait une durée d'exécution dépassant le timeout spécifié (250ms)
<j'ai adapté la méthode randomSleep et son appel pour ne pas être perturber par le comportement du timeout qui peut laisser la méthode annotée se poursuivre et ne l'interrompt pas obigatoirement immédiatement>.

-démarrage / création de la base containerisée quarkusdb :
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkusdb -p 5432:5432 postgres:10.5

-lancement et test via GET ialocalhost:8080/customers
Dans le terminal les logs (terminal IJ) indiquent si le timeout a été atteint et la méthode de Fallback invoquée

-ajout de l'annotation @Retry pour retenter un certain nombre de fois l'invocation de la méthode findAll quand celle-ci lève une exception.
- test avec un timeout de 250 ms et aussi avec 3 ms (pour voir les 3 nouvelles tentatives (dans notre cas) échouées après le premier échec.

2) Utilisation de @CircuitBreaker
- ajout dans import.sql de 4 inserts dans orders pour le customer 1 (John Smith) afin de pouvoir tester via navigateur le fonctionnement du circuit breaker :
(ex d'insert) INSERT INTO orders (id, item, price, customer_id) VALUES ( nextval('orderId_seq'), 'Bike',9999,1);

- requêtage pour vérifier l'insertion : http://localhost:8080/orders?customerId=1
<Si l'appli est déjà en service en mode dev, le hot replace entraine l'insertion en base> 

- dans OrderRepository ajout d'un Logger Sl4j

- création de la méthode privée possibleFailure simulant la levée d'une exception Runtime de manière aléatoire (si flottant tiré < 0,5, RTE levée)
- invocation en début de méthode OrderRepository.findAll(Long customerId)

- ajout de l'annotation @CircuitBreaker(successThreshold = 5,requestVolumeThreshold = 4, failureRatio = 0.75,delay=10000L,failOn=RuntimeException.class)
<dans le code source du livre delay = 1000L (1sec) => 10 sec permet de mieux voir le comportement du circuitbreaker>

- <pas dans le code source du livre> pour mieux visualiser le comportement du circui tbreaker, mise en place d'un fallback :
	* création de la méthode de fallback private List<Order>FindAllOrdersStatic(Long CustomerId) qui retourne un ordre "static".
        * ajout de @Fallback sur OrderRepository.findAll(Long customerId)
sans ça lorsque la RTE est levée le navigateur affiche l'exception RTE et les logs du terminal sont "pollués" par la pile d'exception.

- test via http://localhost:8080/orders?customerId=1

3) bulkhead
- dans OrderRepository, création de la méthode logguant les commandes créées : writeSomeLogging
- annotation de cette méthode avec org.eclipse.microprofile.faulttolerance.@Asynchronous (isolation / thread) et  @Bulkhead(value = 5, waitingTaskQueue = 10)
-invocation de writeSomeLogging dans createOrder

- test fonctionnel en créant des commandes depuis l'UI graphique (localhost:8080)
=> on ne remarquera rien de special si ce n'est les erreurs simulées de timeout ou de levée d'exception. 







