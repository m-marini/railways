# Railways

Simulazione di stazione ferroviaria


# Abstract

Il gioco consiste nella gestione dei binari dei treni di una stazione ferroviaria.
Il giocatore deve creare i percorsi per instradare i treni in arrivo verso i binari della stazione,
far scendere e salire i passeggeri e poi reinstradare il treno verso la destinazione finale.


# Coordinate

Tutti gli elementi sono posizionati nel piano del terreno attraverso coordinate (x,y) in modo che l'asse x vada in direzione ovest-est e l'asse y 
in direzione sud-nord.

```

              N

            y ^
              |
              |
              |
    W    -----o----->    E
              |     x
              |
              |

              S
```


Nello spazio tridimensionale invece l'asse y rappresenta l'altitudine, l'asse x la direzione est-ovest 
e l'asse z la direzione sud-nord.


```

                         N
                y ^
                  |  z ^
                  |   /
                  |  /
                  | /
                  |/
    W   <---------o----------   E
        x        /|
                / |
               /  |
              /   |

            S
 
```

Angoli e direzioni invece sono rappresentati dall'angolo rispetto al Nord in senso orario.


```
                  N

                  y ^        ^
                    | alpha /
                    |------/
                    |     /
                    |    /
                    |   /
                    |  /
                    | /
                    |/
    W    -----------o------------>    E
                    |            x
                    |
                    |
                    |
                    |
                    |

                    S
```

L'immagine del terrain ha come orientamento:

```

              S

              |
              |
              |
    E    -----o----->    W
              |     x
              |
              |
            y V

              N
```

# Stazione

La stazione è composta da binari che permetto il transito dei treni, semafori che bloccano l'accesso a tratte di binari, deviatoi per instradare su tratte diverse i treni e marciapiedi per la salita e discesa dei passeggeri.
La topologia della stazione rappresenta tali elementi, le loro relazione e vincoli.
La topologia della stazione non varia nel tempo.

Per rappresentare la topologia di una stazione abbiamo possiamo usare due modelli: modello a grafo, e modello a blocchi.


## Modello a grafo

Nel modella a grafo ogni punto nello spazio è rappresentato da un nodo di un grafo e gli elementi di congiunzione sono gli archi tra i nodi.


### Archi

Gli archi descrivono i percorsi che un treno fa per spostarsi da un nodo all'altro (topologia). 

Abbiamo vari tipi di archi:

  * Segmento: rappresenta il binario rettilineo che congiunge due punti
  * Curva: rappresenta il binario curvo che congiunge due punti
  * Piattaforma: rappresenta il binario rettilineo con piattaforma di salita / discesa dei passeggeri.

### Nodi
 
I nodi oltre a rappresentare i punto nello spazio, descrivono anche il comportamento che il treno deve esibire durante il transito del nodo;
quindi descrivono gli percorsi validi (es. deviatoi) e attributi di delimitazione dei confini di attraversamento degli archi (es. ):
  
  * Giunzione sono punti di raccordo di due archi (a,b)
  * Semafori sono punti di raccordo di due archi che confinano anche l'accesso ai segmenti adiacenti (a,b)
  * Incrocio sono punti di raccordo di 4 archi che identificano due percorsi incrociati (a,b) e (c,d)
  * Deviatore semplice sono punti di raccordo di 3 archi che identificano due percorsi alternativi tra un arco e gli altri due (a,b) o (a,c)
  * Deviatore inglese sono punti di raccordo di 4 archi che identificano due coppie di percorsi alternative (a,c),(b,d) o (a,d), (b,c)
  * Fine binaro è un punto di raccordo con un solo arco 
  * Entrata è un punto di raccordo con un arco da dove possono solo arrivare treni
  * Uscita è un punto di raccordo con un arco da dove possono solo uscire treni


## Modello a blocchi   

Una forma alternativa di rappresentazione della stazione è il modello a blocchi. E' sempre una forma a grafo ma rispetto il precedente modello,
i diversi componenti sono rappresentati da blocchi (nodi) contenenti le descrizioni sia di percorsi possibili che di confine di attraversamento dei blocchi.

I blocchi sono connessi tra loro attraverso giunzioni adimensionali (archi) che non necessitano di descrizione topologica o funzionale ma mantengono solo la relazione tra percorsi possibili dei blocchi.
E' utile applicare il concetto di template ai blocchi, associando al template le informazioni comuni per il rendering,
i percorsi validi e la topologia dei percorsi.
Le informazioni del template vengono poi applicate ai blocchi reali in base alla posizione e orientamento dei blocchi.

Per mantenere coerente la topologia dei blocchi interconnessi è necessario verificare la coerenza della composizione
dei vari blocchi(lunghezze e orientamenti).


## Modello a grafo vs modello a blocchi
 
Il modello a grafo separa gli aspetti topologici da quelli funzionali rispettivamente negli archi e nei nodi semplificando l'implementazione.
Per contro il modello a grafo rende complesso il rendering grafico dei componenti in quanto dipendende da ambedue gli aspetti.
Il modello a blocchi mantiene uniti i due aspetti e semplifica il rendering grafico.
Per questi motivi si è scelto di utilizzare il modello a blocchi.


# Blocchi

I blocchi sono costituti da punti di connessione (`junction`) e identificate da un numero.
Ogni giunzione può essere connessa ad un altra giunzione attraverso un percorso (`route`).
Queste connessioni dipendono dallo stato del blocco e possono variare nel tempo (es. devatoio, sezione di smistamento).

I percorsi sono costituiti da insiemi ordinati di binari di 3 tipi:

  * Binari rettilinei (`Segment`)
  * Binari curvi (`Curve`)
  * Piattaforme (`Platform`)

Oltre alle connessioni tra giunzioni il blocco mantiene anche le proprietà di transitabilità (`clear`) dei percorsi in modo
che due treni non possano collidere tra loro o il giocatore possa manovrare in sicurezza il blocchi (`lock`).

## Rendering blocchi

Il rendereing dei blocchi dipende dallo stato dei blocchi.
Quando un blocco è particolaremnte complesso con molti stati possibili (es. switching yard), è utile semplificare
il rendering utilizzando elementi 3D componibili.
Per ogni possibile configurazione dobbiamo associare un insieme di elementi 3D elementari.

### Identificatori

Per ogni elemento di un blocco con il relativo stato sono definiti vari identificatori:

  * `elementId` identifica un singolo elemento 3D con il relativo stato.
  * `templateId` identifica il modello 3D da utilizzare per il rendering dell'elemento.
  * `selectionId` identifica se il blocco è interattivo e il tipo di selezione.

Sono previste 3 tipologie di selezione

  * `handler` permette di cambiare la configurazione di un blocco (es. deviatoio diretto o deviato).
  * `junction` permette di cambiare lo stato di blocco di una giunzione (libera o blocca il semaforo della giunzine)-
  * `track` permette di cambiare lo stato di blocco delle giunzioni alle estremità (libera o blocca tutta la tratta).

## Tratta

```

J0  --------------  J1

```

Un blocco di un binario con un percorso arbitrario è composto da un elemento che rappresenta le rotaie della tratta (curve e/o tratti lineari) e gli elementi dei semafori agli estremi, per ogni possibile stato del semaforo (verde o rosso), quindi un totale di 5 elementi.

  * seg-track
  * seg-0-green
  * seg-0-red
  * seg-1-green
  * seg-1-red

Una giunzine può essere libera (verde) solo se non c'è alcun treno in transito nel blocco e se non è stata bloccata dall'operatore.

Sono possibili 4 diverse stati del blocco:

  * J0 verde, J1 verde
  * J0 verde, J1 rossa
  * J0 rossa, J1 verde
  * J0 rossa, J1 rossa


## Deviatoio semplice

```
              -------  J2
             /
            /
J0  -------o---------  J1


```

Un blocco con un deviatoio a sx è composto invece da 10 elementi:

  * swi-l-str-hand (Handler diretto)
  * swi-l-div-hand (Handler deviato sx)
  * swi-l-str (Deviatoio diretto sx)
  * swi-l-div (Deviatoio deviato sx)
  * swi-l-0-green (Semaforo in entrata verde)
  * swi-l-0-red (Semaforo in entrata rosso)
  * swi-l-1-green (Semaforo diretta verde)
  * swi-l-1-red (Semaforo diretta rosso)
  * swi-l-2-green (Semaforo deviata verde)
  * swi-l-2-red (Semaforo deviata rosso)

Sono possibili 8 diversi stati del blocco:

  * Diretto, J0 verde, J1 verde, J2 rossa
  * Diretto, J0 verde, J1 rossa, J2 rossa
  * Diretto, J0 rossa, J1 verde, J2 rossa
  * Diretto, J0 rossa, J1 rossa, J2 rossa
  * Deviato, J0 verde, J1 rossa, J2 verde
  * Deviato, J0 verde, J1 rossa, J2 rossa
  * Deviato, J0 rossa, J1 rossa, J2 verde
  * Deviato, J0 rossa, J1 rossa, J2 rossa


## Curva standard

La curva standard ha un angolo di 360/24 = 15 gradi, raggio di 35 *1/2 * 1/sin(15) = 67,615 m
Combinando mezza curva a sx e mezza curva a dx otteniamo una traslazione del punto iniziale di 1 segmento (35 m) e di 
4,608 m laterali (distanza minima tra due binari).


# Gioco

Il gioco è una sequenza di stati generati dallo scorre del tempo di simulazione e dall'interazione del giocatore.

## Storyboard

### Start 

Alla partenza appare la finestra di start.
Il giocatore vede una sintesi della configurazione di gioco corrente (stazione, livello di gioco, durata) e i bottoni di selezione delle attività da eseguire.

  * Start
  * Opzioni
  * Hall of Fame
  * Quit

Alla partenza del gioco è necessario caricare tutte le risorse 3D, poi generare il modello della stazione contenete la
topologia, creare lo stato iniziale del gioco e eseguire la simulazione.

Lo stato del gioco cambia per effeto del tempo e delle interazione del giocatore e quindi rappresentato da un'osservabile.


### Modifica delle opzioni di gioco

L'utente seleziona il pulsante Opzioni e si passa allo screen di dettaglio delle opzioni.

L'utente può modificare le varie opzioni:

  - Stazione (diverse topologie di stazione)
  - Livello di difficoltà (frequenza di arrivo dei treni)
  - Durata del gioco
  - Volume audio 
  - Automatismo dei semafori

L'utente seleziona il pulsante di conferma e ritorna al pannello di partenza.


### Inizio del gioco

L'utente seleziona il pulsante Start e si passa allo screen di gioco.

Lo screen di gioco visualizza la vista di default della stazione e il pannello di controllo del gioco.


### Cambio della visuale

Nel pannello di controllo sono elencate le telecamere di controllo predefinite.
E' possibile selezionare varie viste
  
  * Interno treno: ripresa dalla testa del treno (todo)
  * Posizione attuale di un treno: Ripresa fissa nella posizione corrente del treno e in direzine dello stesso (todo)
  * Posizione da camere predefinite (binari, smistamento, linee di entrata e uscita) (todo)
  * Viste legate al mouse o al touch (todo)

Il giocatore seleziona una telecamera e la vista principale riprende la situazione dalla telecamera selezionata.

Le viste legate al mouse o al touch sono attive solo nel caso la camera non sia legata ad un treno.

  
## Uso del mouse

  * Tasto sx del mouse su un treno si attiva il pop up menu per
    * fermare,
    * far partire,
    * invertire la marcia,
    * spostare la visuale nel treno
    * selezionare un percorso automatico verso una destinazione.
  * Tasto sx del mouse su un semaforo si attiva il pop up menu per
    * liberare
    * bloccare il semaforo
  * Tasto sx del mouse su un deviatore si cambia lo stato del deviatore
  * Tasto sx del mouse su un binario si attiva il pop-up menu per
    * liberare i semafori della tratta
    * bloccare i semafori della tratta
  * Tasto sx del mouse su un punto della vista si indirizza la camera nel punto determinato.
  * Tasto dx del mouse si ruota la camera a dx o sx, su o giu (con angolazione limitata)
  * Ruota del mouse avanza o retrocede la camera nella direzione della stessa


## Uso del touch

  * Tap come il tasto sx del mouse
  * Le rotazione e avanzamenti della vista sono fatti con 4 tasti di navigazione semitrasparenti sulla parte inferiore dello schermo (non è possibile modificare l'angolazione su/giu).

*Gesture da definire*


## Elementi 3D interattivi

Nella scena sono presenti oggetti sensibili all'interazione con l'utente, selezionando un elemento lo si aziona immediatamente se è possibile solo un'azione oppure appare un pop-up menu con le possibili azioni rappresentate da icone selezionabili:

  * Veicoli: la selezione di un veicolo di un treno permette di frenare un treno in corsa,
    far ripartire un treno fermo o in frenata e invertire il senso di marcia di un treno fermo
  * Semafori: la selezione di un semaforo consente di invertire lo stato di blocco del semaforo
  * Deviatoi: la selezione di un deviatoio permette di invertirne lo stato
  * Binari: la selezione di un binario permette di sbloccare o bloccare i semafori alle giunzioni del binario
  * Elemento neutro: la selezione di un element neutro (sfondo) sposta la camera per riprendere l'elemento selezionato


## Notifiche di gioco

Durante il gioco vengono notificati eventi d'interesse attraverso messaggi:

  * Treno in arrivo 
  * Treno in attesa ad un semaforo
  * Inizio scarico/carico
  * Scarico/carico completati
  * Treno uscito
