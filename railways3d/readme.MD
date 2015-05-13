# Modello Railways


## Stazione

La stazione è l'insieme di elementi e relazioni che compongono la topologia della stazione.
La stazione è invariante nel tempo.
Possiamo rappresentare la stazione come un grafo dove i nodi sono punti nello spazio e gli archi sono gli elementi di congiunzione dei punti.


## Archi

Abbiamo vari tipi di archi:

 * Segmento rappresenta il binario rettilineo che congiunge due punti
 * Curva rappresenta il binario curvo che congiunge due punti
 * Piattaforma rappresenta il binario rettilineo con piattaforma di salita / discesa dei passeggeri.

 ## Nodi
 
  I nodi condividono vari archi che portano ad altri nodi e possono essere:
  
  * Giunzione sono punti di raccordo di due archi (a,b)
  * Semafori sono punti di raccordo di due archi che confinano anche l'accesso ai segmenti adiacenti (a,b)
  * Incrocio sono punti di raccordo di 4 archi che identificano due percorsi incrociati (a,b)(c,d)
  * Deviatore semplice sono punti di raccordo di 3 archi che identificano due percorsi alternativi tra un arco e gli altri due (a,b) o (a,c)
  * Deviatore inglese sono punti di raccordo di 4 archi che identificano due coppie di percorsi alternative (a,c),(b,d) o (a,d), (b,c)
  * Fine binaro è un punto di raccordo con un solo arco 
  * Entrata è un punto di raccordo con un arco da dove possono solo arrivare treni
  * Uscita è un punto di raccordo con un arco da dove possono solo uscire treni
   
  