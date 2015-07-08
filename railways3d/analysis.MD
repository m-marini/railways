
# Analisi componenti

Il documento descrive l'analisi dei componenti.

## Start controller

Lo start controller gestisce l'interazione utente nello screen di start.
Cattura la selezone dei pulsanti dello screen esponendo una osservabile con l'indetificativo del
pulsante selezionato.
Espone un metodo che modofica la visualizzazione della sintesi dei parametri di gioco


## Options controller

L'options controller gestisce l'interazione utente nello screen di opzioni.
Espone un'osservabile di parametri di gioco modificata alla selezione del pulsante di conferma dello screen.

## Game controller

Il game controller gestisce lo screen di gioco.
Sequenza di partenza:

  - bind: il controller viene collegato allo screen
  - start screen: viene attivato lo screen
  - set parameters game: vengono passati i parametri di gioco

Genera lo stato iniziale di gioco e l'osservabile degli stati sucessivi.


## Simulazione

### Topologia

Tutta la simulazione è basata sulla topologia della stazione.
La topologia della stazione descrive tutti i percorsi possibili dei treni,
i vincoli tra i percorsi possibili dei treni (deviatori, incroci).
i percorsi attivi della configurazione corrente (stato dei deviatori),
i percorsi validi della configurazione correte (stato di blocco delle tratte o attraversamento di treni).

Per descrivere tutte queste informazione la topologia è divisa in blocchi.
Ogni blocco rappresenta un insieme di tratte possibili, ogni tratta possibile è associata ad un punto di giunzione del blocco.
Es.
un deviatoio ha tre punti di giunzione:
1. ingresso
2. uscita diretta
3. uscita deviata

e quattro percorsi possibili associati ogniuna a 2 giunzioni

1. tratta diretta (ingresso, uscita diretta)
2. tratta inversa (uscita diretta, ingresso)
3. tratta deviata diretta (ingresso, uscita deviata)
4. tratta deviata inversa (uscita deviata, ingresso)

Un deviatoio in un istante ha solo 2 percorsi attivi in funzione dello stato del deviatoio
1. deviatoio in diretta (tratta diretta, tratta inversa)
2. deviatoio in deviata (tratta deviata diretta e tratta deviata inversa)

Per gestire l'allocazione delle tratte dobbiamo fare riferimento a insiemi di allocazione dei percorsi possibili.
Il ragruppamento dei percorsi per allocazione deve assicurare che le tratte dei vari gruppi siano disgiunte tra loro ovvero non creino collisioni tra treni.

Es. Un deviatoio ha un solo gruppo di allocazione corrispondente a tutti i percorsi possibili (in un deviatoio può transitare un solo treno).

Un blocco di smistamento con 4 giunzioni e tratte dirette e deviate
```
    1    2
    |    |
    |\  /|
    | \/ |
    | /\ |
    |/  \|
    |    |
    3    4
```
ha quattro gruppi di allocazione in base allo stato dei deviatoi

(1-4, 2,3) se tutti i deviatoi in deviata (nel blocco può transitare un solo treno alla volta in quanto 
le tratte attraversano il punto comune di incrocio)
(1-3), (2 4) se tutti i deviatoi in diretta deviatoi (nel blocco possono transitare due treni)
(1-4) solo se deviatoi 1 e 4 in deviata
(2-3) solo se deviatoi 2 e 3 in deviata


## Iterazione di simulazione

Ad ogni ciclo di iterazione della simulazione si deve determinare: per ogni treno la nuova posizione, lo stato e  il cambio di stato della stazione (allocazione delle tratte, stato dei semafori) generato dal movimento del singolo treno.

Il movimento di un treno provoca anche il cambio di stato della stazione, conseguentemente il cambio dei percorsi dei treni e quindi il cambio di stato dei treni.
Di conseguenza ogni qualvolta si cambia la poszione dei un treno è necessario ricalcolare i path e aggiornare lo stato dei treni.
Si pone il problema di quale ordinamento applicare al cambio di stato.
Se un treno cambia posizione cambiano lo stato di occupazione delle tratte (si occupa la tratta in testa e si libera la tratta in coda).
Le tratte impattate possono appartenere anche ad percorsi di altri treni (la tratta occupata può appartenere ad un blocco con percorsi che si incrociano interrompendo di fatto questi con 
semafori di blocco occupato, le stesse considerazioni valgono per la tratta liberata con effetto contrario di liberare invece blocchi occupati).
Posto che il cambio di occupazione interessa solo le tratte con incroci e che il cambio di percorsi non provoca ulteriori blocchi (si tratta di cambiare ii percorsi potenzali dei treni non la posizione o l'occupazione di ulteriori tratte) per ogni treno possiamo aggiornare solo lo stato di allocazione delle tratte interessate e i percorsi di eventuali treni che incrociano le tratte interessate.

Es. Il treno 1 si sposta occupando la tratta A e liberando la tratta B.
	Si deveno ridurre i percorsi dei treni che incrociano la tratta A e allungare i percorsi dei treni che incrocano la tratta B.


### Aggiornamento dello stato dei blocchi

Per ogni treno si estraggono le tratte occupate dalla coda del treno alla testa del treno.
Tutti i blocchi contenenti le tratte occupate devono essere posti in stato blocco.


### Aggiornamento dei percorsi dei treni

Una volta impostato lo stato dei blocchi si calcolano i percorsi dei treni partendo dalla coda di ogniuno e procedendo in avanti fino ad arrivare al primo blocco occupato da un altro treno o, nel caso il treno non sia carico, alla fine di una tratta di una piattaforma. 


### Treno in movimento

Si calcola la nuova velocità del treno in base alla tratta (frenata o accelerazione o mantenimento) e si calcola la nuova posizione del treno.

Se la posizione è fuori dalla tratta corrente il treno deve fermarsi in attesa della semaforo
oppure cancellato se in una tratta di uscita e si genera il relativo avviso.

La velocità massima del treno è di 140 Km/h
```
v_max = 38.889 m/s

L'accelerazione del treno è determinata dalla velocità:
```
a = (vt - v) / dt
```
`vt` è la velocità da raggiungere
`v` è la velocità del treno

l'accelereazione è comunque limitata superiormente dalla forza del treno (400 KN) e dal peso (642 t)
```
a_max = 400. / 642. = 0.623053 m/s^2
```
La decelerazione invece è limitata dalla capactà di frenata:
```
a_min = -1.929 m/sec^2
```
Lo spazio di frenata risulta essere:
```
s = 1/2 v^2 / a
```
Lo spazio di frenata alla velocità massima è:
```
s_max = 392 m
```

Per calcolare la velocià da raggiungere si deve tener conto dello spazio di frenata si calcola la velocità massima dettata dalla distanza dal tratto da percorrere:
```
vm = sqrt(2 a s)
```
questa velocità limitata al valore massimo sarà la velocità da raggiungere.
La velocità sarà invece limitata inferiormente al valore minimo di 1.8 km/h per permettere una fermata precisa
```
vmin = 0.5 m/s
```



### Treno in frenata
Si calcola la nuova velocità del treno (frenata)

Se la velocità è 0 il treno passa in stato di fermo e si genera un avviso altrimenti e si calcola la nuova posizione del treno.

Se la nuova posizione è fuori dalla tratta corrente il treno deve fermarsi alla fine della tratta corrente.

### Treno in attesa passeggeri

Se il tempo trascorso in attesa passeggeri è superiore al tempo di salita si genera un avviso e si mette in stato di fermo.

### Treno in uscita

Il treno in uscita occupa la tratta di uscita per la relativa lunghezza di uscita che limita la frequenza di uscita dei treni (frequenza max = velocità max/ lunghezza).

## Generazione di nuovi treni

Per ogni entrata libera (non occupata da altri treni) casualmente con la frequenza probabile di entrata determinata dal livello di difficoltà si genera un nuovo treno e un avviso.

## Cambio di stato della stazione

Il cambio di stato della stazione può avvenire per uno spostamento dei un treno o per azione dell'utente (user actions: sblocco tratte, spostamento scambiatori).
Il cambio di stato produce il ricalcolo dei percorsi correnti dei treni ed eventualmente il cambio d stato degli stessi se in attesa di semaforo. 

Il cambio di stato di un treno (user actions: stop, inversione, start) non cambia lo stato della stazione.

