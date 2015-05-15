package org.mmarini.railways3d.model

import rx.lang.scala.Subject
import rx.lang.scala.Observable

case class Game(parms: GameParameters) {

  private val topology: Topology = null
  private var state = GameStatus(parms)
  private val _states = Subject[GameStatus]()

  /**
   *
   */
  def states: Observable[GameStatus] = _states

  /**
   *
   */
  def doSomething(parm: Any): GameStatus = {
    state = state.doSomething(parm)
    _states.onNext(state)
    state
  }
}