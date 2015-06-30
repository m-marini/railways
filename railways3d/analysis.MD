
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

### Treno in movimento

Si calcola la nuova velocità del treno in base alla tratta (frenata o accelerazione o mantenimento) e si calcola la nuova posizione del treno.

Se la posizione è fuori dalla tratta corrente il treno deve fermarsi in attesa della semaforo
oppure cancellato se in una tratta di uscita e si genera il relativo avviso.

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

