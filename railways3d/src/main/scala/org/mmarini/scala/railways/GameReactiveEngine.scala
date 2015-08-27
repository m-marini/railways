package org.mmarini.scala.railways

import rx.lang.scala.Observable
import rx.lang.scala.subscriptions.CompositeSubscription
import rx.lang.scala.Subscription
import org.mmarini.scala.railways.model.GameStatus
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.CameraViewpoint
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.scene.Spatial
import scala.util.Try
import com.jme3.util.SkyFactory
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.math.Quaternion
import com.jme3.scene.CameraNode
import rx.lang.scala.Subscriber
import com.jme3.scene.control.CameraControl.ControlDirection
import org.mmarini.scala.railways.model.CameraViewpoint
import org.mmarini.scala.railways.model.Train
import org.mmarini.scala.railways.model.RightAngle
import org.mmarini.scala.railways.model.Pif
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameParameters
import de.lessvoid.nifty.Nifty
import org.mmarini.scala.jmonkey.AnalogMapping
import de.lessvoid.nifty.controls.ButtonClickedEvent
import org.mmarini.scala.jmonkey.ActionMapping
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryReleaseEvent
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent

/**
 * @author us00852
 */

object GameReactiveEngine extends LazyLogging {

  val TrainCameraHeight = 5f
  val TrainHeadCameraDistance = 10f
  val TrainCameraToDistance = 1f
  val TrainCameraPitch = RightAngle / 9

}

class GameReactiveEngine(nifty: Nifty) extends LazyLogging {

  import GameReactiveEngine._

  // ===================================================================
  // Functions
  // ===================================================================
  /** */
  def screenById(id: String): Option[Screen] = Option(nifty.getScreen(id))

  /** */
  def screenControllerById[T <: ScreenController](id: String): Option[T] =
    for {
      scr <- screenById(id)
      ctrl <- Option(scr.getScreenController().asInstanceOf[T])
    } yield ctrl

  lazy val startCtrl: StartController = screenControllerById[StartController]("start").get

  lazy val optionsCtrl: OptionsController = screenControllerById[OptionsController]("opts-screen").get

  lazy val gameCtrl: GameController = screenControllerById[GameController]("game-screen").get

  lazy val endGameCtrl: EndGameController = screenControllerById[EndGameController]("end-game-screen").get

  // ===================================================================
  // Controllers
  // ===================================================================

  def trainCtrlOpt = None
  //  for {
  //    gc <- gameCtrlOpt
  //    ctrl <- gc.controllerById("trainPanel", classOf[TrainController])
  //  } yield ctrl

  def msgCtrlOpt = None
  //    for {
  //    gc <- gameCtrlOpt
  //    ctrl <- gc.controllerById("messagesPanel", classOf[MessageController])
  //  } yield ctrl

  // ===================================================================
  // Observables
  // ===================================================================

  /** Observable of goto screen */
  lazy val screenNavigationObs = {
    // start-screen selection
    val btnScreenMap = Map(
      "optionsButton" -> "opts-screen",
      "startButton" -> "game-screen")

    // Start-screen
    val btnStartNavObs = for {
      ev <- startButtonsObs
      if (btnScreenMap.contains(ev.getButton.getId))
    } yield btnScreenMap(ev.getButton.getId)

    // Option selection
    val optsConfirmObs = for {
      ev <- optionsButtonsObs
      if (ev.getButton.getId == "ok")
    } yield "start"

    val endGameScreenObs = for { x <- endGameButtonsObs } yield "start"

    btnStartNavObs merge
      optsConfirmObs merge
      endGameScreenObs
  }

  lazy val cameraCtrlObs: Observable[CameraController] = gameCtrl.controllerByIdObs("cameraPanel", classOf[CameraController])

  lazy val startButtonsObs: Observable[ButtonClickedEvent] = startCtrl.buttonClickedObs

  lazy val optionsButtonsObs: Observable[ButtonClickedEvent] = optionsCtrl.buttonClickedObs

  lazy val endGameButtonsObs: Observable[ButtonClickedEvent] = endGameCtrl.buttonClickedObs

  //  def trainPanelObs: Observable[(Int, Int)] = {
  //    val opt = for {
  //      c <- trainCtrlOpt
  //    } yield c.selectionObs
  //    opt.getOrElse(Observable.never)
  //  }

  lazy val cameraPanelObs: Observable[(Int, Int)] = cameraCtrlObs.map(_.selectionObsOpt).flatten

  lazy val gameScreenObs: Observable[(String, ScreenControllerAdapter)] = gameCtrl.screenEventObs

  lazy val xRelativeAxisObs: Observable[AnalogMapping] = Main.mouseRelativeObs("xAxis")

  lazy val gameMouseClickedObs: Observable[NiftyMousePrimaryClickedEvent] = gameCtrl.mousePrimaryClickedObs

  lazy val gameMouseReleasedObs: Observable[NiftyMousePrimaryReleaseEvent] = gameCtrl.mousePrimaryReleaseObs

  lazy val cameraNode: CameraNode = {
    val camNode = new CameraNode("Motion cam", Main.getCamera)
    camNode.setControlDir(ControlDirection.SpatialToCamera)
    camNode.setEnabled(true)
    camNode
  }

  def timeObs: Observable[Float] = Main.timeObs

  lazy val selectActionObs: Observable[ActionMapping] = Main.actionObservable("select")
  lazy val selectMidActionObs: Observable[ActionMapping] = Main.actionObservable("selectMid")
  lazy val selectRightActionObs: Observable[ActionMapping] = Main.actionObservable("selectRight")
  lazy val upCommandActionObs: Observable[ActionMapping] = Main.actionObservable("upCmd")
  lazy val downCommandActionObs: Observable[ActionMapping] = Main.actionObservable("downCmd")
  lazy val leftCommandActionObs: Observable[ActionMapping] = Main.actionObservable("leftCmd")
  lazy val rightCommandActionObs: Observable[ActionMapping] = Main.actionObservable("rightCmd")

  lazy val xAxisAnalogObs: Observable[AnalogMapping] = Main.analogObservable("xAxis")
  lazy val forwardAnalogObs: Observable[AnalogMapping] = Main.analogObservable("forwardCmd")
  lazy val backwardAnalogObs: Observable[AnalogMapping] = Main.analogObservable("backwardCmd")

  // ======================================================
  // Observables
  // ======================================================

  /** Creates the parameters generators from options panel */
  lazy val gameParamsObs: Observable[GameParameters] = {
    val parmsObs = for {
      _ <- optionsButtonsObs
      parm <- optionsCtrl.readParametersObs
    } yield parm
    OptionsController.DefaultParms +: parmsObs
  }

  /** Creates the initial game status triggered by the start of game screen */
  lazy val initialGameStatusObs: Observable[GameStatus] =
    gameScreenObs.
      filter(_._1 == "start").
      withLatest(gameParamsObs)(
        (_, parms) => GameStatus(parms))

  /** Creates the observable of viewpoints */
  lazy val viewpointObs: Observable[Seq[CameraViewpoint]] =
    for { status <- initialGameStatusObs } yield status.stationStatus.topology.viewpoints

  /** Creates observable of camera selection */
  lazy val cameraSelectionObs: Observable[CameraViewpoint] =
    cameraPanelObs.
      withLatest(viewpointObs)(
        (idx, viewpoints) => viewpoints(idx._1))

  /** Creates the observable  of backstage of scene loading */
  lazy val backstageObs: Observable[Spatial] = {
    val bo = for {
      status <- initialGameStatusObs
    } yield Observable.from(backstage(status))
    cameraNode +: bo.flatten
  }

  /** Creates the observable of game status transitions */
  lazy val gameTransitionsObs: Observable[GameStatus => GameStatus] = {
    val initialGameTxObs = for { status <- initialGameStatusObs } yield (_: GameStatus) => status
    Observable.just((s: GameStatus) => s) merge initialGameTxObs
  }

  /** Creates the game status observable */
  lazy val gameStatusObs: Observable[GameStatus] =
    gameTransitionsObs.statusFlow(initialGameStatusObs.take(1))

  initialGameStatusObs.trace("==> initialGameStatusObs")
  gameTransitionsObs.trace("==> gameTransitionsObs")
  gameParamsObs.trace("==> gameParamsObs")
  gameStatusObs.trace("==> gameStatusObs")

  /** Creates the observable of station renderer */
  lazy val stationRenderObs = {
    val txObs = for {
      status <- gameStatusObs
    } yield (renderer: StationRenderer) => renderer.change(status.stationStatus.blocks.values.toSet)
    txObs.statusFlow(StationRenderer(Main.getAssetManager))
  }

  /** Creates the observable of attach spatial */
  lazy val attachToRootObs: Observable[Spatial] = {
    backstageObs
    val stationAttacheObs = for {
      stationRend <- stationRenderObs
      spatial <- Observable.from(stationRend.attached)
    } yield spatial
    backstageObs merge stationAttacheObs
  }

  /** Creates the observable of attach spatial */
  lazy val detachFromRootObs: Observable[Spatial] = {
    val stationDetachObs = for {
      stationRend <- stationRenderObs
      spatial <- Observable.from(stationRend.detached)
    } yield spatial
    stationDetachObs
  }

  lazy val trainFollowerObs: Observable[Train] =
    Observable.never

  /** Creates the observable of camera translation */
  lazy val cameraTranslationObs: Observable[Vector3f] = {
    // Camera selected by panel
    val cameraAtObs = for {
      vp <- cameraSelectionObs
    } yield vp.location

    // Camera located at train
    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      location <- train.locationAt(TrainHeadCameraDistance)
    } yield new Vector3f(
      -location.getX,
      TrainCameraHeight,
      location.getY)

    val cameraTrainObs = for {
      locOpt <- cameraTrainOptObs
      if (!locOpt.isEmpty)
    } yield locOpt.get

    cameraAtObs merge cameraTrainObs
  }

  /** Creates the observable of camera translation */
  lazy val cameraDirectionObs: Observable[Vector3f] = {
    val cameraDirObsOpt = for {
      vp <- cameraSelectionObs
    } yield vp.direction

    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      angle <- train.directionAt(TrainHeadCameraDistance, TrainCameraToDistance)
    } yield new Quaternion().
      fromAngleNormalAxis(angle, Vector3f.UNIT_Y).
      mult(new Quaternion().fromAngleNormalAxis(TrainCameraPitch, Vector3f.UNIT_X)).
      mult(Vector3f.UNIT_Z)

    val cameraTrainObs = for {
      locOpt <- cameraTrainOptObs
      if (!locOpt.isEmpty)
    } yield locOpt.get
    cameraDirObsOpt merge cameraTrainObs
  }

  /** Creates the observable of camera translation */
  lazy val cameraSpeedObs: Observable[Float] = {
    // Creates the observable of up command transitions
    val upObs = for {
      action <- upCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Creates the observable of down command transitions
    val downObs = for {
      action <- downCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- gameMouseClickedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "up" => 1f
        case "down" => -1f
        case _ => 0f
      }
    val buttonsReleaseObs =
      for {
        ev <- gameMouseReleasedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield 0f

    buttonsPressObs merge
      buttonsReleaseObs merge
      upObs merge
      downObs
  }

  /** Creates the observable of camera translation */
  lazy val cameraRotationSpeedObs: Observable[Float] = {
    // Creates the observable of left command transitions
    val leftObs = for {
      action <- leftCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Creates the observable of left command transitions
    val rightObs = for {
      action <- rightCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- gameMouseClickedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "left" => -1f
        case "right" => 1f
      }

    val buttonsReleaseObs =
      for {
        ev <- gameMouseReleasedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield 0f

    buttonsPressObs merge
      buttonsReleaseObs merge
      leftObs merge
      rightObs
  }

  /** Creates the observable of camera translation */
  lazy val cameraRotationObs: Observable[Float] = {
    // Creates the observable of xMouse axis and right button
    val xMouseButtonObs =
      (xRelativeAxisObs withLatest selectRightActionObs)(
        (analog, action) => (analog.position.getX, action.keyPressed))

    // Filters the values of last two values with button press and
    // transforms to camera status transition 
    for {
      seq <- xMouseButtonObs.history(2)
      if (seq.size > 1 && seq.forall(p => p._2))
    } yield (seq(0)._1 - seq(1)._1) * Pif

  }

  lazy val cameraMovementObs =
    CameraUtils.createObservables(
      timeObs,
      cameraSpeedObs,
      cameraRotationSpeedObs,
      cameraRotationObs,
      forwardAnalogObs,
      backwardAnalogObs,
      cameraTranslationObs,
      cameraDirectionObs)

  // ======================================================
  // Subscriptions
  // ======================================================

  /** Subscription to gotoScreen */
  def screenNavigationSub: Subscription = screenNavigationObs.subscribe(nifty.gotoScreen _)

  //  def endGamePerfSub(obs: Observable[GamePerformance]): Subscription = obs.subscribe(
  //    performance => for (c <- endGameCtrlOpt) yield c.show(performance))
  //
  //  def performancePanelSub(obs: Observable[GamePerformance]): Subscription = obs.subscribe(
  //    performance => for { c <- gameCtrlOpt } yield c.show(performance))
  //
  //  def msgsPanelSub(obs: Observable[Iterable[String]]): Subscription = obs.subscribe(
  //    msgs => for { c <- msgCtrlOpt } yield c.show(msgs))
  //
  //  def cameraPanelSub(obs: Observable[Seq[String]]): Subscription = obs.subscribe()
  //  //    names => for { c <- cameraCtrlOpt } c.show(names))
  //
  //  def trainPanelSub(obs: Observable[IndexedSeq[IndexedSeq[String]]]): Subscription = obs.subscribe(
  //    cells => for { c <- trainCtrlOpt } c.setCells(cells))

  /** Subscription to quit command */
  private def quitAppSub: Subscription =
    startButtonsObs.filter(_.getButton.getId == "quitButton").subscribe { _ => Main.stop }

  /** Subscribes for parameter changes to startParameters */
  private def startPanelSub: Subscription =
    gameParamsObs.subscribe(parms => startCtrl.show(parms))

  /** Subscribes for camera viewpoint changes to camera panel */
  private def cameraPanelSub: Subscription =
    (cameraCtrlObs combineLatest viewpointObs).subscribe(_ match {
      case (ctrl, viewpoints) =>
        val cells = for { v <- viewpoints } yield { IndexedSeq("", v.id) }
        ctrl.setCell(cells.toIndexedSeq)
    })

  /** Subscribes for attach spatials to root */
  private def attachToRootSub: Subscription = Main.attachToRootSub(attachToRootObs)

  /** Subscribes for attach spatials to root */
  private def detachFromRootSub: Subscription = Main.detachFromRootSub(detachFromRootObs)

  /** Subscribes for camera movements */
  private def cameraMovementSub = {
    val (locObs, rotObs) = cameraMovementObs

    CompositeSubscription(
      locObs.subscribe(location => cameraNode.setLocalTranslation(location)),
      rotObs.subscribe(rotation => cameraNode.setLocalRotation(rotation)))
  }

  /** Composes all subscriptions */
  def gameFlowSub = {
    CompositeSubscription(
      screenNavigationSub,
      startPanelSub,
      cameraPanelSub,
      detachFromRootSub,
      attachToRootSub,
      cameraMovementSub,
      quitAppSub)
  }

  // ======================================================
  // Function
  // ======================================================

  /** Creates sky */
  private lazy val sky = {
    // Load sky
    val SkyboxIndex = Seq("east", "west", "north", "south", "up", "down")
    val assetManager = Main.getAssetManager
    val skyTry = Try {
      val imgs = for {
        idx <- SkyboxIndex
      } yield assetManager.loadTexture(s"Textures/sky/desert_${idx}.png")
      SkyFactory.createSky(assetManager, imgs(0), imgs(1), imgs(2), imgs(3), imgs(4), imgs(5))
    }

    for { e <- skyTry.failed } logger.error(e.getMessage, e)
    skyTry.toOption.toSeq
  }

  /** Create the backstage */
  private def backstage(status: GameStatus): Seq[Spatial] = {
    // Creates the terrain builder
    val terrainTry = TerrainBuilder.build(Main.getAssetManager, Main.getCamera)
    sky ++ terrainTry.toOption
  }
}