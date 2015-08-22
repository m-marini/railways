package org.mmarini.scala.railways

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
    val btnStartNavObsOpt = for {
      start <- GameViewAdapter.startCtrlOpt
    } yield for {
      ev <- start.buttonClickedObs
      if (btnScreenMap.contains(ev.getButton.getId))
    } yield btnScreenMap(ev.getButton.getId)

    // Option selection
    val optsConfirmObsOpt = for {
      opts <- GameViewAdapter.optionsCtrlOpt
    } yield for {
      ev <- opts.buttonClickedObs
      if (ev.getButton.getId == "ok")
    } yield "start"

    val endGameScreenObsOpt = for {
      ctrl <- GameViewAdapter.endGameCtrlOpt
    } yield for {
      x <- ctrl.buttonClickedObs
    } yield "start"

    val toEndGameScreenObs = for { _ <- Main.endGameObs } yield "end-game-screen"

    mergeAll(btnStartNavObsOpt.toArray ++
      optsConfirmObsOpt ++
      endGameScreenObsOpt :+
      toEndGameScreenObs: _*)
  }

  /** Subscription to gotoScreen */
  def subscribeOpt = Main.gotoScreenSubOpt(gotoScreenObs)
}