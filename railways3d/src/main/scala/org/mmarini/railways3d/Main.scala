/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ButtonClickedEvent
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.events.ElementShowEvent
import de.lessvoid.nifty.NiftyDefaults
import org.mmarini.railways3d.model.GameParameters
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.railways3d.model.GameParameters
import org.mmarini.railways3d.model.GameParameters
import rx.lang.scala.Observer
import rx.lang.scala.Observable
import rx.lang.scala.Subject

/**
 *
 */
object Main extends SimpleApplication with LazyLogging {

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  /**
   *
   */
  def sampled[T, S](trigger: Observable[T], value: Observable[S]): Observable[(T, S)] = {
    val r = Subject[(T, S)]()
    var v: Option[S] = None
    value.subscribe((s) => v = Some(s))
    trigger.subscribe(
      (t) => if (!v.isEmpty) r.onNext((t, v.get)),
      (ex) => r.onError(ex),
      r.onCompleted)
    r
  }

  private val startPaneObserver = Observer((id: String) => id match {
    case "optionsButton" => for (n <- nifty) n.gotoScreen("opts-screen")
    case "quitButton" => stop
    case "startButton" => for (n <- nifty) n.gotoScreen("game-screen")
    case t => logger.info(s"selected $t")
  })

  private val optsPaneObserver = Observer((id: String) =>
    for (n <- nifty)
      n.gotoScreen("start"))

  /**
   *
   */
  @Override
  def simpleInitApp: Unit = {
    setDisplayStatView(false)
    setDisplayFps(false)
    niftyDisplay = Some(new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort))

    // Read your XML and initialize your custom ScreenController
    for (n <- nifty) {
      n.fromXml("Interface/start.xml", "start")
      n.addXml("Interface/opts.xml")
      n.addXml("Interface/game.xml")
    }

    // attach the Nifty display to the gui view port as a processor
    for (nd <- niftyDisplay) {
      guiViewPort.addProcessor(nd)
    }

    // disable the fly cam
    flyCam.setDragToRotate(true)

    wireUp
  }

  /**
   *
   */
  private def wireUp {
    for {
      start <- controller[StartController]("start")
      opts <- controller[OptionsController]("opts-screen")
      game <- controller[GameController]("game-screen")
    } {
      start.selection.subscribe(startPaneObserver)
      opts.completed.subscribe(optsPaneObserver)
      opts.gameParameters.subscribe(start.gameParameter)
      val startGameObs = sampled(
        start.selection.filter(_ == "startButton"),
        opts.gameParameters).
        map(m => (this, new GameHandler(m._2)))
      startGameObs.subscribe(game.gameStarter)
    }
  }

  /**
   *
   */
  private def nifty = niftyDisplay.map(n => n.getNifty())

  /**
   *
   */
  private def screen(id: String) = {
    nifty.map(n => n.getScreen(id))
  }

  /**
   *
   */
  private def controller[T](id: String) =
    screen(id).map(n => n.getScreenController().asInstanceOf[T])

  /**
   *
   */
  def main(args: Array[String]): Unit = {
    Main.start()
  }
}
