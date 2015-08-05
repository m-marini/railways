# FRP

  * Le variabili possono essere sostituite con osservabili.

Le osservabili rappresentano la variabilità nel tempo di proprietà del sistema.
Invece di mantenere il valore della proprietà in un ogni istante permettono di gestire
le transizioni di valore nel tempo.

## Composizione di osservabili

Le osservabili possono essere composte per creare nuove osservabili.
Ad esempio supponiamo di avere un'osservabile che identifica la temperatura in gradi centigradi
possiamo creare una nuova osservabile che rappresenta la temperatura in farenheit.

## combineLatest

Permette di combinare n osservabili generando un'osservabile che si modifica ogni volta che 
varia una qualsiasi osservabile dipendente.

## sample

Permette di combinare 2 osservabili generando un'osservabile che varia quando varia generando una
coppia di valori della prima e dell'ultimo valore della seconda


