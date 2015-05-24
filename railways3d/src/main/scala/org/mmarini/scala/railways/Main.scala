/**
 *
 */
package org.mmarini.scala.railways

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import com.jme3.input.event.MouseButtonEvent
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.MouseInput
import com.jme3.input.controls.MouseAxisTrigger
import org.mmarini.scala.jmonkey.ApplicationOps
import com.jme3.input.InputManager

/**
 *
 */
object Main extends SimpleApplication with LazyLogging {

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  val _time = Subject[(SimpleApplication, Float)]()

  /** */
  override def simpleInitApp: Unit = {
    setDisplayStatView(false)
    //    setDisplayFps(false)
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

    flyCam.setDragToRotate(true)
    wireUp
  }

  /** */
  private def wireUp {
    for {
      start <- controller[StartController]("start")
      opts <- controller[OptionsController]("opts-screen")
      game <- controller[GameController]("game-screen")
    } {

      // GameParameter observable
      val gpo = stateFlow(opts.parameters)(opts.confirmed.map(_ => _ => opts.parameters))

      // Bind gpo to start controller observer
      gpo.subscribe(start.gameParameterObserver)

      // Bind gpo to start screen
      gpo.subscribe(_ => {
        nifty.foreach(_.gotoScreen("start"))
      })

      // Bind option button to options screen
      start.selection.
        filter(_ == "optionsButton").
        subscribe(_ => for (n <- nifty) n.gotoScreen("opts-screen"))

      // Bind start button to game screen
      start.selection.
        filter(_ == "startButton").
        subscribe(_ => for (n <- nifty) n.gotoScreen("game-screen"))

      // Bind quit button to game exit
      start.selection.
        filter(_ == "quitButton").
        subscribe(_ => stop)

      // Bind gpo to triggered start
      val gameStart = trigger(start.selection.
        filter(_ == "startButton"), gpo).map(t => (this, t._2))

      gameStart.subscribe(game.gameStarterObserver)
    }
  }

  /** */
  def timeObservable: Observable[(SimpleApplication, Float)] = _time

  /** */
  override def simpleUpdate(tpf: Float) {
    _time.onNext((this, tpf))
  }

  /** */
  private def nifty = niftyDisplay.map(n => n.getNifty())

  /** */
  private def screen(id: String) = nifty.map(n => n.getScreen(id))

  /** */
  private def controller[T](id: String) =
    screen(id).map(n => n.getScreenController().asInstanceOf[T])

  /** */
  def main(args: Array[String]): Unit = {
    //    Main.setShowSettings(false)
    Main.start()
  }
}
