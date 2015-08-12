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
import org.mmarini.scala.jmonkey.NiftyUtil
import rx.lang.scala.Observer
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.AnalogMapping
import com.jme3.math.Vector2f

/**
 *
 */
object Main extends SimpleApplication with LazyLogging with NiftyUtil {
  val Width = 1200
  val Height = 768

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  val _time = Subject[(SimpleApplication, Float)]()

  /** Returns the action observable by name */
  def actionObservable: String => Observable[ActionMapping] = (k) => inputManager.createActionMapping(k)

  /** Returns the analog observable by name */
  def analogObservable: String => Observable[AnalogMapping] = (k) => inputManager.createAnalogMapping(k)

  def mouseRelativeObservable: String => Observable[AnalogMapping] = (k) =>
    for { e <- analogObservable(k) } yield {
      val p = new Vector2f(e.position.getX / Width, e.position.getY / Height).multLocal(2).subtractLocal(1f, 1f)
      AnalogMapping(e.name, e.value, p, e.tpf)
    }
    
  def mouseRelativeActionObservable: String => Observable[ActionMapping] = (k) =>
    for { e <- actionObservable(k) } yield {
      val p = new Vector2f(e.position.getX / Width, e.position.getY / Height).multLocal(2).subtractLocal(1f, 1f)
      ActionMapping(e.name, e.keyPressed, p, e.tpf)
    }

  /** Returns the observer for camera location */
  var cameraLocationObserver: Option[Observer[Vector3f]] = None

  /** Returns the observer for camera location */
  var cameraRotationObserver: Option[Observer[Quaternion]] = None

  /** */
  override def simpleInitApp: Unit = {
    setDisplayStatView(false)
    setDisplayFps(false)
    niftyDisplay = Option(new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort))

    nifty = for {
      nd <- niftyDisplay
      n <- Option(nd.getNifty)
    } yield n

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

    flyCam.setEnabled(false)
    val camNode = new CameraNode("Motion cam", cam)
    rootNode.attachChild(camNode)
    camNode.setControlDir(ControlDirection.SpatialToCamera)
    camNode.setEnabled(true)

    cameraLocationObserver = Some(Observer((location: Vector3f) => {
      camNode.setLocalTranslation(location)
    }))

    cameraRotationObserver = Some(Observer((rotation: Quaternion) => {
      camNode.setLocalRotation(rotation)
    }))

    wireUp

    attachMapping

  }

  /** */
  private def wireUp {
    for {
      start <- controllerById[StartController]("start")
      opts <- controllerById[OptionsController]("opts-screen")
      game <- controllerById[GameController]("game-screen")
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
    inputManager.addMapping("selectRight", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("selectMid", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE))
    inputManager.addMapping("upCmd", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W))
    inputManager.addMapping("leftCmd", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("rightCmd", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("downCmd", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S))
    inputManager.addMapping("rightMouseBtn", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))

    inputManager.addMapping("xAxis",
      new MouseAxisTrigger(MouseInput.AXIS_X, false),
      new MouseAxisTrigger(MouseInput.AXIS_X, true))
    inputManager.addMapping("forwardCmd",
      new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false))
    inputManager.addMapping("backwardCmd",
      new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true))
  }

  /** */
  def timeObservable: Observable[(SimpleApplication, Float)] = _time

  /** */
  override def simpleUpdate(tpf: Float) {
    _time.onNext((this, tpf))
  }

  /** */
  def main(args: Array[String]): Unit = {
    val s = new AppSettings(true)
    s.setResolution(Width, Height)
    Main.setSettings(s)
    Main.setShowSettings(false)
    Main.start()
  }
}
