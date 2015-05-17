package org.mmarini.railways3d.model

import rx.lang.scala.Observable

/**
 *
 */
object Game {

  import org.mmarini.railways3d._
  /**
   * genera un'osservabile che produce una sequenza di gioco ad ogni variazione di parametri
   */
  def createGameObservable(parms: Observable[GameParameters], events: Observable[GameTransition]): Observable[Observable[GameStatus]] = {
    parms.map(p => stateFlow(initState(p))(events))
  }

  /**
   *
   */
  def initState(parms: GameParameters): GameStatus =
    GameStatus(parms, Topology(Set()), 0f)
}