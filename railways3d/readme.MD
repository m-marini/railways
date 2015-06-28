
# Stazione

La stazione è l'insieme di elementi e relazioni che compongono la topologia della stazione.
La stazione è invariante nel tempo.

# Coordinate

Le coordinate piane x-y sono organizzate in modo che l'asse x vada in direzione ovest-est e l'asse y 
in direzione sud-nord.

Nello spazio tridimensionale invece l'asse y rappresenta l'altitudine, l'asse x la direzione est-ovest 
e l'asse z la direzione sud-nord.  

## Modello a grafo
Possiamo rappresentare la stazione come un grafo dove i nodi sono punti nello spazio e gli archi sono gli elementi di congiunzione dei punti.


### Archi

Gli archi descrivono le connessione tra nodi.
Queste connessioni descrivono i percorsi che un treno fa per spostarsi da un nodo all'altro (topologia). 

Abbiamo vari tipi di archi:

 * Segmento rappresenta il binario rettilineo che congiunge due punti
 * Curva rappresenta il binario curvo che congiunge due punti
 * Piattaforma rappresenta il binario rettilineo con piattaforma di salita / discesa dei passeggeri.

### Nodi
 
I nodi condividono vari archi che portano ad altri nodi.
I nodi descrivono anche i percorsi valide tra gli archi connessi al nodo e attributi di delimitazione dei confini di attraversamento degli archi:
  
  * Giunzione sono punti di raccordo di due archi (a,b)
  * Semafori sono punti di raccordo di due archi che confinano anche l'accesso ai segmenti adiacenti (a,b)
  * Incrocio sono punti di raccordo di 4 archi che identificano due percorsi incrociati (a,b) e (c,d)
  * Deviatore semplice sono punti di raccordo di 3 archi che identificano due percorsi alternativi tra un arco e gli altri due (a,b) o (a,c)
  * Deviatore inglese sono punti di raccordo di 4 archi che identificano due coppie di percorsi alternative (a,c),(b,d) o (a,d), (b,c)
  * Fine binaro è un punto di raccordo con un solo arco 
  * Entrata è un punto di raccordo con un arco da dove possono solo arrivare treni
  * Uscita è un punto di raccordo con un arco da dove possono solo uscire treni


## Modello a blocchi   

Una forma alternativa di rappresentazione della stazine p il modello a blocchi.
I diversi componenti sono rappresentati da blocchi contenenti le descrizioni sia di percorsi possibili che di confine di attraversamento dei blocchi.
I blocchi sono connessi tra loro attraverso giunzioni adimensionali che non necessitano di descrizione topologica o funzionale ma mantengono solo
la relazione tra percorsi possibili dei blocchi.
E' utile applicare il concetto di template ai blocchi, associando al template le informazioni comuni per il rendering,
i percorsi validi e la topologia dei percorsi.
Le informazioni del template vengono poi applicate ai blocchi reali in base alla posizione o orientamento dei blocchi.
Per manenere però coerente la topologia dei blocchi interconnessi è necessario verificare la compatibilità della composizione
dei vari blocchi(lunghezze e orientamenti).


## Modello a grafo vs modello a blocchi
 
Il modello a grafo separa gli aspetti topologici da quelli funzionali rispettivamente negli archi e nei nodi semplificando l'implementazione.
Per contro il modello a grafo rende complesso il rendering grafico dei componenti in quanto dipendende da ambedue gli aspetti.  
Il modello a blocchi mantiene uniti i due aspetti e semplifica il rendering grafico.


## Blocchi

I blocchi sono costituti da punti di connessione, percorsi validi che legano i punti di conessione in base allo stato del blocco.
I percorsi sono costituiti da insiemi ordinati di binari di due tipi:
  * Binari rettilinei
  * Binari curvi


## Gioco

Il gioco è una sequenza di stati generati dallo scorre del tempo di simulazione e dall'interazione del giocatore.
E' quindi descritto da un'osservabile.

  
## Start del gioco

Alla partenza del gioco è necessario caricare tutte le risorse 3D, poi generare il modello della stazione contenete la
topologia, creare lo stato iniziale del gioco e eseguire la simulazione.

Lo stato del gioco cambia durante il tempo e quindi è un'osservabile.
La topologia della stazione è contenuta nello stato del gioco.

## Gestione della posizione della camera

E' possibile selezionare varie viste
  
  * Interno treno (todo)
  * Esterno treno (todo)
  * Posizione attuale di un treno (todo)
  * Posizione di camere predefinite (binari, scambi, altro) (todo)
  * Viste legate al mouse o al touch (todo)

Le viste legate al mouse o al touch sono attive solo nel caso la camera non sia legata ad un treno.

## Uso del mouse

Tasto dx del mouse su un punto della vista si indirizza la camera nel punto determinato.
Tasto dx del mouse su un treno si seleziona la vista esterna dal treno.

Tasto sx del mouse su un treno si cambia lo stato del treno (start/stop).
Tasto sx + ctrl del mouse su un treno si attiva automaticamente il percorso verso una direzione selezionabile con pop up
Tasto sx del mouse su un semaforo si libero o blocca un semaforo
Tasto sx del mouse su un deviatore si cambia lo stato del deviatore

Tasto centrale del mouse o sx + shift su un treno si cambia la direzione del treno (reverse).
Tasto centrale del mouse o sx + shift su un binario si attivano/disattivano i semafori del percorso.

## Curva

La curva standard ha un angolo di 360/24 = 15 gradi, raggio di 35 *1/2 * 1/sin(15) = 67,615 m
Combinando una curva a sx e una curva a dx otteniamo una traslazione del punto iniziale di 1 segmento (35 m) e di 
4,608 m laterali (passo tra due binari).

  