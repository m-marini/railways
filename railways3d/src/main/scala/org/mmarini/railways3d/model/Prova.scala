package org.mmarini.railways3d.model

import rx.lang.scala.Observable

/**
 *
 */
object Prova {

  import org.mmarini.railways3d._
  /**
   * genera un'osservabile che produce una sequenza di gioco ad ogni variazione di parametri
   */
  def createGameObservable(parms: Observable[GameParameters], events: Observable[Any]): Observable[Observable[GameStatus]] =
    parms.map(p => fold[Any, GameStatus] {
      case (event: String, status) => status.doSomething(event)
      case (event: Int, status) => status.doSomething(event)
    }(GameStatus(p))(events))
}