
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

Ad ogni ciclo di iterazione della simulazione si deve determinare: per ogni treno la nuova posizione, lo stato e  il cambio di stato della stazione (allocazione delle tratte, stato dei semafori) generato dal movimento del singolo treno.

Il movimento di un treno provoca anche il cambio di stato della stazione, conseguentemente il cambio dei percorsi dei treni e quindi il cambio di stato dei treni.
Di conseguenza ogni qualvolta si cambia la poszione dei un treno è necessario ricalcolare i path e aggiornare lo stato dei treni.
Si pone il problema di quale ordinamento applicare al cambio di stato.
Se un treno cambia posizione il path del treno cambia (si occupa la tratta in testa e si libera la tratta in coda).
Le tratte impattate possono appartenere anche ad altri treni (la tratta occupata può appartenere ad un blocco con percorsi che si incrociano interrompendo di fatto questi con 
semafori di blocco occupato, le stesse considerazioni valgono per la tratta liberata con effetto contrario di liberare invece blocchi occupati).


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

