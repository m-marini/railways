/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters

import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging

import rx.lang.scala.Observer

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {
  /** Returns the game start observer */
  val gameStarterObserver = Observer((o: (Main.type, GameParameters)) => {
    o match {
      case (app, parms) =>
        val game = new Game(app, parms)
    }
  })
}
