package org.mmarini.scala.railways

import rx.lang.scala.Observable
import rx.lang.scala.Subscription

/**
 * @author us00852
 */
object ScreenNavigation {

  /** Observable of goto screen */
  private def gotoScreenObs = {
    // start-screen selection
    val btnScreenMap = Map(
      "optionsButton" -> "opts-screen",
      "startButton" -> "game-screen")

    // Start-screen
    val btnStartNavObs = for {
      ev <- GameViewAdapter.startButtonsObs
      if (btnScreenMap.contains(ev.getButton.getId))
    } yield btnScreenMap(ev.getButton.getId)

    // Option selection
    val optsConfirmObs = for {
      ev <- GameViewAdapter.optionsButtonsObs
      if (ev.getButton.getId == "ok")
    } yield "start"

    val endGameScreenObs = for { x <- GameViewAdapter.endGameButtonsObs } yield "start"

    btnStartNavObs merge
      optsConfirmObs merge
      endGameScreenObs
  }

  /** Subscription to gotoScreen */
  def gotoScreenSub: Subscription = gotoScreenObs.subscribe(id => GameViewAdapter.gotoScreen(id))

}