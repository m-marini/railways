/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ActionMapping
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.AnalogMapping
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

/**
 *
 */
object Main extends SimpleApplication with LazyLogging with NiftyUtil {
  val Width = 1200
  val Height = 768

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  /** Returns the observer for camera location */
  var cameraLocationObserver: Option[Observer[Vector3f]] = None

  /** Returns the observer for camera location */
  var cameraRotationObserver: Option[Observer[Quaternion]] = None

  val _time = Subject[(SimpleApplication, Float)]()

  lazy val screenNavigationSubrOpt = for { n <- nifty } yield Subscriber((screenId: String) => n.gotoScreen(screenId))

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

  /** Observable of goto screen */
  private lazy val gotoScreenObs = {
    // start-screen selection
    val btnScreenMap = Map(
      "optionsButton" -> "opts-screen",
      "startButton" -> "game-screen")

    // Start-screen
    val btnStartNavObsOpt = for {
      start <- controllerById[StartController]("start")
    } yield for {
      ev <- start.buttonClickedObs
      if (btnScreenMap.contains(ev.getButton.getId))
    } yield btnScreenMap(ev.getButton.getId)

    // Option selection
    val optsConfirmObsOpt = for {
      opts <- controllerById[OptionsController]("opts-screen")
    } yield for {
      ev <- opts.buttonClickedObs
      if (ev.getButton.getId == "ok")
    } yield "start"

    val endGameScreenObsOpt = for {
      ctrl <- controllerById[EndGameController]("end-game-screen")
    } yield for {
      x <- ctrl.buttonClickedObs
    } yield "start"

    val endGameObs = for {
      game <- gameObs
      _ <- game.endGameObs
    } yield "end-game-screen"

    mergeAll(btnStartNavObsOpt.toArray ++
      optsConfirmObsOpt ++
      endGameScreenObsOpt :+
      endGameObs: _*)
  }

  /** Game observable */
  private lazy val gameObs = {
    val gameSubj = Subject[Game]()

    val sub = for {
      opts <- controllerById[OptionsController]("opts-screen")
      game <- controllerById[GameController]("game-screen")
    } yield {
      val obs = for {
        id <- game.screenObs
        if (id == "start")
      } yield id
      obs.subscribe(_ => gameSubj.onNext(new Game(this, opts.parameters)))
    }
    gameSubj
  }

  /** Subscription to gotoScreen */
  private lazy val gotoScreenSubOpt = for {
    n <- nifty
  } yield gotoScreenObs.subscribe((id) =>
    try {
      n.gotoScreen(id)
    } catch {
      case t: Throwable => logger.error(t.getMessage, t)
    })

  /** Subscription to quit command */
  private lazy val quitSubOpt = {
    val obsOpt = for {
      start <- controllerById[StartController]("start")
    } yield for {
      ev <- start.buttonClickedObs
      if (ev.getButton.getId == "quitButton")
    } yield ev.getButton.getId

    for { o <- obsOpt } yield o.subscribe(_ => stop)
  }

  /** Subscription to show parameters from option screen */
  private lazy val showParmsSubOpt =
    for {
      start <- controllerById[StartController]("start")
      opts <- controllerById[OptionsController]("opts-screen")
    } yield opts.buttonClickedObs.subscribe(_ => start.show(opts.parameters))

  /** Subscription to start game */
  private lazy val startGameSub = gameObs.subscribe()

  private lazy val subscriptions =
    CompositeSubscription((gotoScreenSubOpt.toArray ++
      showParmsSubOpt ++
      quitSubOpt :+
      startGameSub): _*)

  /** */
  override def simpleInitApp: Unit = {
    setDisplayStatView(false)
    setDisplayFps(false)
    val nd = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort)
    val n = nd.getNifty

    nifty = Option(n)

    // Read your XML and initialize your custom ScreenController
    try {
      n.fromXml("Interface/start.xml", "start")
      n.addXml("Interface/opts.xml")
      n.addXml("Interface/game.xml")
      n.addXml("Interface/endGame.xml")
    } catch {
      case ex: Exception => logger.error(ex.getMessage, ex)
    }

    // attach the Nifty display to the gui view port as a processor
    guiViewPort.addProcessor(nd)

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
  lazy val timeObservable: Observable[(SimpleApplication, Float)] = _time

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
