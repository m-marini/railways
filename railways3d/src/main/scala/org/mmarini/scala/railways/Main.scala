/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ActionMapping
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.NiftyUtil
import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.system.AppSettings
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import com.jme3.input.controls.JoyAxisTrigger
import com.jme3.input.JoyInput
import rx.lang.scala.Subscriber
import rx.lang.scala.subscriptions.CompositeSubscription
import org.mmarini.scala.railways.model.GameStatus
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.scala.jmonkey.SimpleAppAdapter
import org.mmarini.scala.jmonkey.InputManagerAdapter

/**
 *
 */
object Main extends SimpleAppAdapter
    with NiftyUtil
    with InputManagerAdapter
    with LazyLogging {
  val Width = 1200
  val Height = 768

  /** Returns the observer for camera location */
  var cameraLocationObserver: Option[Observer[Vector3f]] = None

  /** Returns the observer for camera location */
  var cameraRotationObserver: Option[Observer[Quaternion]] = None

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

  def gameCtrlOpt = screenControllerById[GameController]("game-screen")
  def startCtrlOpt = screenControllerById[StartController]("start")
  def optsCtrlOpt = screenControllerById[OptionsController]("opts-screen")
  def endGameCtrlOpt = screenControllerById[EndGameController]("end-game-screen")

  def endGameObs = for {
    game <- startGameObs
    endGame <- game.endGameObs
  } yield endGame

  private def showPerfSub =
    endGameObs.subscribe {
      status =>
        for {
          ctrl <- endGameCtrlOpt
        } ctrl.show(status.performance)
    }

  /** Game observable */
  private lazy val startGameObs = {
    val gameSubj = Subject[Game]()

    val sub = for {
      opts <- optsCtrlOpt
      game <- gameCtrlOpt
    } yield {
      val obs = for {
        id <- game.screenObs
        if (id == "start")
      } yield id
      obs.subscribe(_ => gameSubj.onNext(new Game(this, opts.parameters)))
    }
    gameSubj
  }

  /** Subscription to quit command */
  private def quitSubOpt =
    for {
      ctrl <- startCtrlOpt
    } yield ctrl.buttonClickedObs.subscribe(ev =>
      if (ev.getButton.getId == "quitButton") stop)

  /** Subscription to show parameters from option screen */
  private def showParmsSubOpt =
    for {
      start <- startCtrlOpt
      opts <- optsCtrlOpt
    } yield opts.buttonClickedObs.subscribe(_ => start.show(opts.parameters))

  /** Subscription to start game */
  private def startGameSub = startGameObs.subscribe()

  private lazy val subscriptions =
    CompositeSubscription(ScreenNavigation.subscribeOpt.toArray ++
      showParmsSubOpt ++
      quitSubOpt :+
      startGameSub :+
      showPerfSub: _*)

  /** */
  override def simpleInitApp: Unit = {
    super.simpleInitApp

    setDisplayStatView(false)
    setDisplayFps(false)
    flyCam.setEnabled(false)

    // Read your XML and initialize your custom ScreenController
    for (n <- niftyOpt)
      try {
        n.fromXml("Interface/start.xml", "start")
        n.addXml("Interface/opts.xml")
        n.addXml("Interface/game.xml")
        n.addXml("Interface/endGame.xml")
      } catch {
        case ex: Exception => logger.error(ex.getMessage, ex)
      }

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

    attachMapping
    subscriptions
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
  def main(args: Array[String]): Unit = {
    val s = new AppSettings(true)
    s.setResolution(Width, Height)
    Main.setSettings(s)
    Main.setShowSettings(false)
    Main.start()
  }
}
