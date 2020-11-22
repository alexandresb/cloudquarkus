Quarkus 1.8.1.Final GraalVM 20.1.0 Java 11

=> programmation / archi reactive

-----core--------
=>utilisation de l'API Core Vert.x

- copie du projet chapter04/basic/customer-service sous core
- adaptation: modifs pom.xml (versions Java et Quarkus et plugin surefire, groupId, artifactId), base des paquetages en org.cesi.chapter09
- mvn clean pour nettoyer les artefacts générés pour l'exo du chap 4

- intégration de l'extension Quarkus Eclipse Vert.x  via ajout de la dépendance Vert.x dans pom.xml io.quarkus:quarkus-vertx

- ajout dans application.properties de la propriété définissant l'emplacement où le fichier customer.json sera créé : file.path = /tmp/customer.json

dans CustomerRepository :
- injection d'une instance managée io.vertx.core.Vertx : @Inject Vertx vertx
- injection de la propriété de configuration du chemin du fichier customer.json : @ConfigProperty(name = "file.path") String path;

- création de la méthode CompletionStage<String> writeFile() pour écrire un tableau Json contenant les customers dans customer.json : utilisation d'un CompletableFuture<String>, de JSON-P, de Vert.x (dont handler)
-création de la méthode CompletionStage<String> readFile() lisant dans le fichier customer.json : création d'un timer temporel "SingleEvent" pour retarder d'1 sec le reste de l'exe, lecture du contenu du fichier JSON, et assignation de ce contenu dans un CompletableFuture

- dans CustomerEndpoint ajout de 2 méthode GET pour écrire et lire dans le fichier customer.json

dans index.html :
- ajout du code JavaScript Angular pour lancer l'écriture dans le fichier et pour récupérer les données lues dans le fichier - ajout de la fonction privée pour convertir en chaine json
- ajout des composants graphiques HTML : boutons pour lancer l'écriture / lecture et pour afficher les messages. ajout juste avant la dernière </div>

- lancement : mvn quarkus:dev -> accès UI : localhost:8080 -> ajouts de customers puis clic sur btns write file puis read file.
- arrêt

-ajout de loggers dans le Endpoint et le repository pour suivre la gestion des threads dans un environement Quarkus
=> on remarque que :
	--les méthodes du EP et du repository s'exécutent dans des  worker threads gérés par un executor
	--la pile d'invocation EP->repository est associé à 1 thread donné (ex : executor-thread-1)
	--les handlers vert.x s'exécutent dans des threads séparé managés par Vert.x (ex :vert.x-eventloop-thread-5)
Note : en inspectant le code source du noyau Quarkus (module runtime), la gestion des (worker) threads au sein de Quarkus est implémentée en interne avec l'API de concurrence Java SE (java.util.concurrent). De même le toolkit Vert.x utilise cette API Java SE pour la gestion des threads.

- arrêt
- mvn clean pour "libérer de l'espace disque" en vue de la sauvegarde du code

-----Rxjava--------
=>utilisation de l'API RxJava de Vert.x (dépréciée - utiliser Mutiny de SmallRye)
copie du projet mvn customer-service sous core dans rxjava | artifactId = rxjava & refresh dans la fenêtre Mvn d'IJ pour qu'IJ prenne en compte le changement.

- dans application.properties, indication de l'emplacement d'un fichier csv : file.path = /tmp/customer.csv
- définition dans Customer d'un constructeur sans args et d'un Constructeur Customer(id, name, surname)
- redéfinition de toString() dans Customer pour l'affichage

Modification de CustomerRepository :
- remplacement des imports io.vertx.core et io.vertx.core.Buffer par "io.vertx.reactivex.core" pour utiliser l'implémentation Java de ReactiveX (RxJava) fournie par Vert.x
- modification de la méthode writeFile() pour utiliser le pattern observateur afin d'écrire chaque Customer dans un fichier csv :i
	--instanciation d'un Observable emettant séquenciellement les customer de CustomerList :Observable.fromIterable
	--transformation au format CSV des items de type Customer Obervable.map
	-- abonnement de callbacks onNext, onError, on Complete pour stocker les items au format csv dans un buffer (onNext) et écrire le buffer dans le fichier csv (onComplete utilisant io.vertx.reactivex.core.Vertx.fileSystem())
- modification de la méthode readFile() pour lire le fichier csv en utilisant le pattern Observateur afin de l'écrire au format JSON
		--lecture du Fichier csv et émission d'un seul item correspondant au contenu bufferisé : vertx.fileSystem().rxReadFile(path)
		-- transformation de l'item (émis par Single) en un tableau d'items correspondant à chaque ligne du fichier :
			flatMapObservable(buffer->Observable.fromArray(buffer.toString().split("\n")))
		-- suppression de la ligne entête csv : skip(1)
		-- transformation de chaque ligne (item) en un String[] : map(record-> record.split(","))
		-- création d'un item de type Customer à partir de chaque String[] : map(recordArray-> new Customer(Integer.parseInt(recordArray[0]),recordArray[1],recordArray[2]))
		-- abonnement du Suscriber interceptant/lisant chaque item de type Customer et les stockant dans un StringBuffer (onNext) dont le contenu sera retourné via CompletableFuture une fois la séquence intégralement lue (onComplete) : .subscribe
- utilisation d'un logger pour suivre la gestion des threads dans une application Reactive s'exécutant dans un environnement Quarkus

- On annote la méthode CustomerEndpoint.readFile() avec @Produces(MediaType.TEXT_PLAIN) car la valeur retournée par readFile n'est pas au format JSON

- On teste : mvn quarkus:dev | ajout de customers | écriture dans fichier csv et lecture de fichier
<note : lors du déploiement, on a 1 message indiquant que l'utilisation dans Quarkus de ReactiveX /RxJava fournie par Vert.x est dépréciée et qu'il est conseillé d'utiliser plutôt Vert.x Mutiny : io.vertx.reactivex.core.Vertx` is deprecated  and will be removed in a future version - it is recommended to switch to `io.vertx.mutiny.core.Vertx>
=> concernant la gestion des threads, 
	--dans CustomerRepository.writeFile, la chaine d'Observation RxJava s'exécute dans le même thread que l'appelant. Seul le handler Vert.x s'exécute dans un thread séparé.
	--dans CustomerRepository.readFile, rxReadFile est asynchrone, il y a donc un thread séparé pour son exécution donc pour l'exe de la chaine d'observation.
        =>cela est dû au fait que RxJava n'utilise pas par défaut l'asynchronisme pour l'observation / la réactivité

- Dans CustomerRepository.writeFile(), on définit un ordonnanceur pour que les méthodes de l'observateur s'exécutent de façon asynchrone via assignation d'un thread managé par l'ordonnanceur : .subscribeOn(Schedulers.io()) <ce n'est pas dans le livre>
- On teste fonctionnellement comme précédemment.
=> concernant la gestion des threads , l'Observable s'exécute dans un thread managé par Quarkus, les méthodes onNext,onComplete s'exécutent dans un thread managé par le Scheduler RxJava, et le handler dans un thread géré par Vert.x 

- stoppe et mvn clean (pour économiser espace disque)

----utilisation d'un event bus Vert.x avec Axle Vert.x pour découpler--------
on utilise le même projet rxjava/customer-service
note : l'API Axle Vert.x est dépréciée comme l'API RxJava Vert.x => utiliser plutôt Mutiny

Dans CustomerEndpoint,
- injection de io.vertx.axle.core.eventbus.EventBus
- Implémentation de la méthode @GET call(customerId) envoyant selon le pattern Request-Response un message dans le bus, à 1 adresse indiquée
<note : EventBus.send(adresse, message) est déprécié. Il faut utiliser à la place request>

- création d'un bean Application scoped CustomerService contenant une méthode handler @io.quarkus.vertx.ConsumeEvent (enregistrée à l'adresse indiquée) consommant les messages routés à cette adresse.

- positionnement de logger.info dans le code pour suivre la gestion des threads par Quarkus
=> le worker thread http dans lequel s'exécute la méthode GET n'est pas bloqué par le traitement métier (envoi consommation du message et reponse à l'envoyeur). 
Ce Thread est libéré avant que le traitement métier soit terminé. Lorsque le traitement est terminé, la réponse (embarqué dans 1 CompletionStage) est retournée au client web.
=> la méthode handler consommatrice @ConsumeEvent (consommant le message envoyé) et CompletionStage.thenApply (traitement du message de réponse) s'exécutent dans des threads Vert.x différents.
=>Par contre les méthodes thenApply chainées s'exécutent dans le même thread Vert.x. ce thread est bloqué tant que le message de réponse n'est pas retourné par le consommateur.

- dans index.html, ajout :
	 --d'un bouton "Call" après le bouton "remove"
	 --d'une fonction pour déclencher 1 GET /call?id=<customerId> quand clic sur le bouton et d'une fonction pour afficher la réponse dans 1 fenêtre d'alerte

test fonctionnel : mvn quarkus:dev | ajout d'au moins 1 customer et clic sur le bouton Call
<note : lorsqu'on change l'index.html, la modif est appliquée mais pas de hot restart (Files changed but restart not needed - notified extensions in: 0.015s)>

- pour poursuivre l'étude de l'asynchronisme, ajout d'un logger en fin de la méthode CustomerRepository.writeFile() et readFile
(on poursuit les tests fonctionnels)
=>le thread principal dans lequel s'exécute la méthode CustomerRepository.writeFile()/ou readFile() , n'est pas bloqué par le traitement d'écriture (ou de lecture) du fichier : traitement asynchrone

----Reactive Streams Server Sent Event (SSE) avec Vert.x et l'API Reactive d'Eclipse MicroProfile--------
on utilise le même projet rxjava/customer-service

- Création de la classe org.cesi.chapter09.StreamingEndpoint annotée @Path("/streaming")
- injection de io.vertx.axle.core.Vertx pour mettre en place le streaming SSE
- injection de CustomerRepository pour retrouver des "stats" concernant les customers qui seront poussées vers le client web (index.html)

- création de la méthode @GET stream() publiant "en continue" de façon asynchrone des events/data aux clients ayant souscrit (ici page web),
	- dont le type de retour est org.reactivestreams.Publisher<String> 
	- annotée @Produces(MediaType.SERVER_SENT_EVENTS) car le serveur(le endpoint) va pousser des datas vers les clients web
	- mise en place du Publisher qui va retourner une séquence de données aux clients web (subscribers)
- dans la méthode stream() création du Publisher emettants toutes les 2 sec des données statistiques  sur les customers "formatées" :
 ReactiveStreams.fromPublisher(vertx.periodicStream(2000).toPublisher()).map(l->String.format("...")).buildRs()
 	=> retourne un Publisher Eclipse MicroProfile de type org.reactivestreams.PublisherBuilder<String>

dans index.html,
- création d'une fonction stats() récupérant les messages publiés par StreamingEndoint
- création d'un bouton permettant de déclencher l'écoute des events publiés par le serveur
- création d'un container pour afficher les stats.

- test fonctionnel :  mvn quarkus:dev | clic sur stats (avant ou après avoir ajouter 1 ou n customers) => les events "apparaissent au fur à mesure toutes les 2 secs
- mvn clean







