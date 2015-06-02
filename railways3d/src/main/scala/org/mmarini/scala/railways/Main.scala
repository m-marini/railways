/**
 *
 */
package org.mmarini.scala.railways

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.system.AppSettings
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.MouseInput
import org.mmarini.scala.jmonkey.ActionMapping

/**
 *
 */
object Main extends SimpleApplication with LazyLogging {

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  val _time = Subject[(SimpleApplication, Float)]()

  var actions: Map[String, Observable[ActionMapping]] = Map()

  /** */
  override def simpleInitApp: Unit = {
    setDisplayStatView(false)
    //    setDisplayFps(false)
    niftyDisplay = Some(new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort))

    // Read your XML and initialize your custom ScreenController
    for (n <- nifty) {
      try {
        n.fromXml("Interface/start.xml", "start")
        n.addXml("Interface/opts.xml")
        n.addXml("Interface/game.xml")
      } catch {
        case ex: Exception => logger.error(ex.getMessage, ex)
      }
    }

    // attach the Nifty display to the gui view port as a processor
    for (nd <- niftyDisplay) {
      guiViewPort.addProcessor(nd)
    }

    flyCam.setDragToRotate(true)

    actions = (for (k <- Set("changeState", "changeView", "additionalChangeState", "zoomSlider")) yield {
      val obs = inputManager.createActionMapping(k)
      (k -> obs)
    }).toMap

    wireUp

    attachMapping
  }

  /** */
  private def wireUp {
    for {
      start <- controller[StartController]("start")
      opts <- controller[OptionsController]("opts-screen")
      game <- controller[GameController]("game-screen")
    } {

      // start screen selection
      start.selection.subscribe(_ match {
        case "optionsButton" => nifty.foreach(_.gotoScreen("opts-screen"))
        case "startButton" => nifty.foreach(_.gotoScreen("game-screen"))
        case "quitButton" => stop
      })

      // Option selection
      opts.confirmed.subscribe(_ => {
        nifty.foreach(_.gotoScreen("start"))
        start.show(opts.parameters)
      })

      // game controller behaviour
      game.screenObservable.subscribe(_ match {
        case "start" => new Game(this, opts.parameters)
      })
    }
  }

  /** Attaches mapping */
  private def attachMapping {
    inputManager.addMapping("changeState", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("changeView", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("changeView", new KeyTrigger(KeyInput.KEY_G))
    inputManager.addMapping("additionalChangeState", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE))
    inputManager.addMapping("zoomSlider", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false))
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
  def controller[T](id: String): Option[T] =
    screen(id).flatMap(n => Some(n.getScreenController().asInstanceOf[T]))

  /** */
  def main(args: Array[String]): Unit = {
    val Width = 1200
    val Height = 768
    val s = new AppSettings(true)
    s.setResolution(Width, Height)
    Main.setSettings(s)
    Main.setShowSettings(false)
    Main.start()
  }
}
