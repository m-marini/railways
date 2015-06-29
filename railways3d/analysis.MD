
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


