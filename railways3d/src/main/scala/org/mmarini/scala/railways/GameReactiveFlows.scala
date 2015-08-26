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

/**
 * @author us00852
 */
object GameReactiveFlows extends LazyLogging {

  val TrainCameraHeight = 5f
  val TrainHeadCameraDistance = 10f
  val TrainCameraToDistance = 1f
  val TrainCameraPitch = RightAngle / 9

  // ======================================================
  // Observables
  // ======================================================

  /** Creates the parameters generators from options panel */
  lazy val gameParamsObs: Observable[GameParameters] =
    GameViewAdapter.optionsCtrlObs.map(ctrl => {
      val parmsObs = for {
        _ <- GameViewAdapter.optionsButtonsObs
        parms <- ctrl.readParametersObs
      } yield parms
      OptionsController.DefaultParms +: parmsObs
    }).flatten

  /** Creates the initial game status triggered by the start of game screen */
  lazy val initialGameStatusObs: Observable[GameStatus] =
    GameViewAdapter.gameScreenObs.filter(_._1 == "start").
      withLatest(gameParamsObs)(
        (_, parms) => GameStatus(parms))

  /** Creates the observable of viewpoints */
  lazy val viewpointObs: Observable[Seq[CameraViewpoint]] =
    for { status <- initialGameStatusObs } yield status.stationStatus.topology.viewpoints

  /** Creates observable of camera selection */
  lazy val cameraSelectionObs: Observable[CameraViewpoint] =
    GameViewAdapter.cameraPanelObs.
      withLatest(viewpointObs)(
        (idx, viewpoints) => viewpoints(idx._1))

  /** Creates the observable  of backstage of scene loading */
  lazy val backstageObs: Observable[Spatial] = {
    val bo = for {
      status <- initialGameStatusObs
    } yield Observable.from(backstage(status))
    GameViewAdapter.cameraNodeObs merge bo.flatten
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
      action <- GameViewAdapter.upCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Creates the observable of down command transitions
    val downObs = for {
      action <- GameViewAdapter.downCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- GameViewAdapter.gameMouseClickedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "up" => 1f
        case "down" => -1f
        case _ => 0f
      }
    val buttonsReleaseObs =
      for {
        ev <- GameViewAdapter.gameMouseReleasedObs
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
      action <- GameViewAdapter.leftCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Creates the observable of left command transitions
    val rightObs = for {
      action <- GameViewAdapter.rightCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- GameViewAdapter.gameMouseClickedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "left" => -1f
        case "right" => 1f
      }

    val buttonsReleaseObs =
      for {
        ev <- GameViewAdapter.gameMouseReleasedObs
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
      (GameViewAdapter.xRelativeAxisObs withLatest GameViewAdapter.selectRightActionObs)(
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
      GameViewAdapter.timeObs,
      cameraSpeedObs,
      cameraRotationSpeedObs,
      cameraRotationObs,
      GameViewAdapter.forwardAnalogObs,
      GameViewAdapter.backwardAnalogObs,
      cameraTranslationObs,
      cameraDirectionObs)

  // ======================================================
  // Subscriptions
  // ======================================================

  /** Subscription to quit command */
  private def quitSub: Subscription = GameViewAdapter.quitSub(
    GameViewAdapter.startButtonsObs.filter(_.getButton.getId == "quitButton"))

  /** Subscribes for parameter changes to startParameters */
  private def startPanelSub: Subscription =
    (GameViewAdapter.startCtrlObs combineLatest gameParamsObs).subscribe(
      _ match { case (ctrl, parms) => ctrl.show(parms) })

  /** Subscribes for camera viewpoint changes to camera panel */
  private def cameraPanelSub: Subscription =
    (GameViewAdapter.cameraCtrlObs combineLatest viewpointObs).subscribe(_ match {
      case (ctrl, viewpoints) =>
        val cells = for { v <- viewpoints } yield { IndexedSeq("", v.id) }
        ctrl.setCell(cells.toIndexedSeq)
    })

  /** Subscribes for attach spatials to root */
  private def attachToRootSub: Subscription =
    GameViewAdapter.attachToRootSub(attachToRootObs)

  /** Subscribes for attach spatials to root */
  private def detachFromRootSub: Subscription =
    GameViewAdapter.detachFromRootSub(detachFromRootObs)

  /** Subscribes for camera movements */
  private def cameraMovementSub = {
    val (locObs, rotObs) = cameraMovementObs

    CompositeSubscription(
      cameraTranslationSub(locObs),
      cameraRotationSub(rotObs))
  }

  /** Subscribes for camera translations */
  private def cameraTranslationSub(obs: Observable[Vector3f]): Subscription =
    (obs combineLatest GameViewAdapter.cameraNodeObs).subscribe(_ match {
      case (location, cameraNode) => cameraNode.setLocalTranslation(location)
    })

  /** Subscribes for camera rotations */
  private def cameraRotationSub(obs: Observable[Quaternion]): Subscription =
    (obs combineLatest GameViewAdapter.cameraNodeObs).subscribe(_ match {
      case (rotation, cameraNode) => cameraNode.setLocalRotation(rotation)
    })

  /** Composes all subscriptions */
  def gameFlowSub = {
    CompositeSubscription(
      startPanelSub,
      cameraPanelSub,
      detachFromRootSub,
      attachToRootSub,
      cameraMovementSub,
      quitSub)
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