/**
 *
 */
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
import com.jme3.renderer.Camera
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
import org.mmarini.scala.jmonkey.TableController
import scala.collection.mutable.IndexedSeq
import org.mmarini.scala.jmonkey._
import scala.math.round
import com.jme3.math.Vector2f
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.controls.dynamic.PopupCreator

/**
 * @author us00852
 */

object GameReactiveEngine extends LazyLogging {

  val TrainCameraHeight = 5f
  val TrainHeadCameraDistance = 10f
  val TrainCameraToDistance = 1f
  val TrainCameraPitch = RightAngle / 9

  val PanelMaxMessageCount = 10
}

class GameReactiveEngine(nifty: Nifty) extends LazyLogging {

  import GameReactiveEngine._

  // ======================================================
  // Subscriptions
  // ======================================================

  /** Composes all subscriptions */
  def subscribeForGameFlow {
    /** Subscribes for gotoScreen */
    screenNavigationObs.subscribe(nifty.gotoScreen _)

    /** Subscription to quit command */
    startButtonsObs.filter(_.getButton.getId == "quitButton").subscribe { _ => Main.stop }

    /** Subscribes for parameter changes to startParameters */
    gameParamsObs.subscribe(parms => startCtrl.show(parms))

    /** Subscribes for camera movements */
    cameraTranslationObs.subscribe(location => cameraNode.setLocalTranslation(location))
    cameraRotationObs.subscribe(rotation => cameraNode.setLocalRotation(rotation))

    subscribeForSpatialManagement

    /** Subscribes for camera viewpoint changes to camera panel */
    (cameraCtrlObs combineLatest viewpointObs).subscribe(_ match {
      case (ctrl, viewpoints) =>
        val cells = for { v <- viewpoints } yield { IndexedSeq("", v.id) }
        ctrl.setCell(cells.toIndexedSeq)
    })

    /** Subscribes for messages panel content */
    trainsPanelObs.subscribe(_ match {
      case (ctrl, cells) => ctrl.setCell(cells)
    })

    /** Subscribes for messages panel content */
    messagePanelObs.subscribe(_ match {
      case (ctrl, cells) => ctrl.setCell(cells)
    })

    /** Subscribes for performance change to panel */
    performanceObs.subscribe(gameCtrl.show _)

    showPopupObs.subscribe(_ match {
      case (popup, panelId, position) => nifty.showPopupAt(popup, panelId, position)
    })
  }

  /** Subscribe for spatial management */
  private def subscribeForSpatialManagement {

    /** Subscribes for translations of spatials */
    translateSpatialObs.subscribe(_ match {
      case (spatial, translation) => spatial.setLocalTranslation(translation)
    })

    /** Subscribes for rotations of spatials */
    rotateSpatialObs.subscribe(_ match {
      case (spatial, rotation) => spatial.setLocalRotation(rotation)
    })

    /** Subscribes for user data settings of spatials */
    setUserDataSpatialObs.subscribe(_ match {
      case (spatial, key, value) => spatial.setUserData(key, value)
    })

    /** Subscribes for attach spatials to root */
    Main.detachFromRootSub(detachFromRootObs)

    /** Subscribes for attach spatials to root */
    Main.attachToRootSub(attachToRootObs)
  }

  // ===================================================================
  // Functions
  // ===================================================================

  private lazy val startCtrl: StartController = nifty.screenControllerById[StartController]("start")

  private lazy val optionsCtrl: OptionsController = nifty.screenControllerById[OptionsController]("opts-screen")

  private lazy val gameCtrl: GameController = nifty.screenControllerById[GameController]("game-screen")

  private lazy val endGameCtrl: EndGameController = nifty.screenControllerById[EndGameController]("end-game-screen")

  private lazy val trainPopup = nifty.createPopup("trainPopup")

  private lazy val semPopup = nifty.createPopup("semPopup")

  /** Creates sky */
  private lazy val sky = {
    val EeastIndex = 0
    val WestIndex = 1
    val NorthIndex = 2
    val SouthIndex = 3
    val UpIndex = 4
    val DownIndex = 5

    // Load sky
    val SkyboxIndex = Seq("east", "west", "north", "south", "up", "down")
    val assetManager = Main.getAssetManager
    val skyTry = Try {
      val imgs = for {
        idx <- SkyboxIndex
      } yield assetManager.loadTexture(s"Textures/sky/desert_${idx}.png")
      SkyFactory.createSky(assetManager,
        imgs(EeastIndex),
        imgs(WestIndex),
        imgs(NorthIndex),
        imgs(SouthIndex),
        imgs(UpIndex),
        imgs(DownIndex))
    }

    for { e <- skyTry.failed } logger.error(e.getMessage, e)
    skyTry.toOption.toSeq
  }

  /** Creates the backstage */
  private def backstage(status: GameStatus): Seq[Spatial] = {
    // Creates the terrain builder
    val terrainTry = TerrainBuilder.build(Main.getAssetManager, Main.getCamera)
    sky ++ terrainTry.toOption
  }

  /** Finds first parent node by selector*/
  private def findParent(nodeOpt: Option[Spatial], f: Spatial => Boolean): Option[Spatial] = {
    def findParentLoop(nodeOpt: Option[Spatial]): Option[Spatial] =
      nodeOpt match {
        case None => None
        case Some(node) if (f(node)) => nodeOpt
        case Some(node) => findParentLoop(Option(node.getParent))
      }
    findParentLoop(nodeOpt)
  }

  // ===================================================================
  // Decompositions
  // ===================================================================

  /** Creates the camera reactive engine */
  private lazy val cameraReactiveEngine = new CameraReactiveEngine(
    timeObs,
    cameraSelectionObs,
    upCommandActionObs,
    downCommandActionObs,
    leftCommandActionObs,
    rightCommandActionObs,
    selectRightActionObs,
    xRelativeAxisObs,
    forwardAnalogObs,
    backwardAnalogObs,
    gameMouseClickedObs,
    gameMouseReleasedObs)

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

    val endGameScreenObs = for { x <- endGameButtonsObs } yield "start"

    btnStartNavObs merge
      optsConfirmObs merge
      endGameScreenObs
  }

  // Option selection
  val optsConfirmObs = for {
    ev <- optionsButtonsObs
    if (ev.getButton.getId == "ok")
  } yield "start"

  lazy val cameraCtrlObs: Observable[CameraController] = gameCtrl.controllerByIdObs("cameraPanel", classOf[CameraController])

  lazy val trainsCtrlObs: Observable[TrainController] = gameCtrl.controllerByIdObs("trainPanel", classOf[TrainController])

  lazy val messagesCtrlObs: Observable[TableController] = gameCtrl.controllerByIdObs("messagesPanel", classOf[TableController])

  lazy val startButtonsObs: Observable[ButtonClickedEvent] = startCtrl.buttonClickedObs

  lazy val optionsButtonsObs: Observable[ButtonClickedEvent] = optionsCtrl.buttonClickedObs

  lazy val endGameButtonsObs: Observable[ButtonClickedEvent] = endGameCtrl.buttonClickedObs

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

  /** Creates the game status observable */
  lazy val gameStatusObs: Observable[GameStatus] = {
    /** Creates the observable of game status transitions */
    val gameTransitionsObs: Observable[GameStatus => GameStatus] = {

      val initialGameTxObs = for { status <- initialGameStatusObs }
        yield (_: GameStatus) => status

      val timeGameTxObs = for { time <- timeObs.dropUntil(initialGameStatusObs) }
        yield (status: GameStatus) => status.tick(time)

      // Creates block change state events
      val blockToogleTxObs =
        for {
          (_, Seq("handler", id, handler)) <- pickedObjectIdObs
        } yield (status: GameStatus) => status.toogleBlockStatus(id)(handler.toInt)

      // Creates selection of train pop up commands
      val trainPopupCtrl = Option(trainPopup.findControl(trainPopup.getId, classOf[PopupController])).get

      // Creates train command transition
      val trainTxOptObs = for {
        (ev, (_, Seq("train", trainId))) <- trainPopupCtrl.mousePrimaryClickedObs withLatest pickedObjectIdObs
      } yield ev.getElement.getId match {
        case "startTrain" => Some((status: GameStatus) => status.startTrain(trainId))
        case "stopTrain" => Some((status: GameStatus) => status.stopTrain(trainId))
        case "reverseTrain" => Some((status: GameStatus) => status.reverseTrain(trainId))
        case _ => None
      }
      val trainTxObs = for (Some(f) <- trainTxOptObs) yield f

      Observable.just((s: GameStatus) => s) merge
        initialGameTxObs merge
        timeGameTxObs merge
        blockToogleTxObs merge
        trainTxObs
    }

    gameTransitionsObs.statusFlow(initialGameStatusObs.take(1))
  }

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

    backstageObs merge
      stationAttacheObs merge
      vehicleAttachObs
  }

  /** Creates the observable of attach spatial */
  lazy val detachFromRootObs: Observable[Spatial] = {
    val stationDetachObs = for {
      stationRend <- stationRenderObs
      spatial <- Observable.from(stationRend.detached)
    } yield spatial

    stationDetachObs merge
      vehicleDetachObs
  }

  /** Creates the observable of camera movements */
  private lazy val (cameraTranslationObs, cameraRotationObs) = cameraReactiveEngine.cameraMovementObs

  /** Creates the observable of performance changes */
  private lazy val performanceObs = gameStatusObs.map(_.performance)

  /** Creates the observable of vehicles renderer */
  private lazy val vehicleRenderObs = {

    // Creates vehicle renderer transitions
    val vehicleRenderTxObs = for {
      status <- gameStatusObs
    } yield (renderer: VehicleRenderer) => renderer(status.vehicles)

    vehicleRenderTxObs.statusFlow(VehicleRenderer(Main.getAssetManager))
  }

  /** Creates the observable of vehicles detach */
  private lazy val vehicleDetachObs = for {
    render <- vehicleRenderObs
    veichle <- Observable.from(render.detached)
  } yield veichle

  /** Creates the observable of vehicles attach */
  private lazy val vehicleAttachObs = for {
    render <- vehicleRenderObs
    veichle <- Observable.from(render.attached)
  } yield veichle

  /** Creates the observable of vehicle translations */
  private lazy val vehicleTranslateObs = for {
    render <- vehicleRenderObs
    (vehicle, spatial) <- Observable.from(render.vehicleSpatialsSet)
  } yield (spatial, new Vector3f(-vehicle.location.getX, 0, vehicle.location.getY))

  /** Creates the observable of vehicles rotations */
  private lazy val vehicleRotateObs = for {
    render <- vehicleRenderObs
    (vehicle, spatial) <- Observable.from(render.vehicleSpatialsSet)
  } yield (spatial, new Quaternion().fromAngleNormalAxis(vehicle.orientation, OrientationAxis))

  /** Creates the observable of vehicle user data settings */
  private lazy val vehicleSetDataObs = for {
    render <- vehicleRenderObs
    (vehicle, spatial) <- Observable.from(render.vehicleSpatialsSet)
  } yield (spatial, "id", s"train ${vehicle.id}")

  /** Creates the observable of spatial translations */
  private lazy val translateSpatialObs = vehicleTranslateObs

  /** Creates the observable of spatial rotations */
  private lazy val rotateSpatialObs = vehicleRotateObs

  /** Creates the observable of spatial user data setting */
  private lazy val setUserDataSpatialObs: Observable[(Spatial, String, String)] = Observable.empty

  /** Creates the observable of messages */
  private lazy val messageObs = for {
    status <- gameStatusObs
    msg <- Observable.from(status.messages)
  } yield msg

  /** Creates the observable of message panel content */
  private lazy val messagePanelObs = for {
    ctrl <- messagesCtrlObs
    msgs <- messageObs.history(PanelMaxMessageCount)
  } yield (ctrl, msgs.map(m => IndexedSeq(m.toString)).toIndexedSeq)

  /** Creates the observer of train sequence */
  private lazy val trainsSeqObs = for { status <- gameStatusObs } yield status.trains.toIndexedSeq

  /** Creates the observer of train panel content */
  private lazy val trainsPanelObs =
    for {
      ctrl <- trainsCtrlObs
      trains <- trainsSeqObs
    } yield {
      val cells = for { t <- trains.toIndexedSeq }
        yield IndexedSeq(
        t.id.toUpperCase,
        t.exitId.toUpperCase,
        "---",
        f"${round(3.6f * t.speed).toInt}%d")
      (ctrl, cells)
    }

  /** Creates the observable of picked object by selection */
  private lazy val pickedObjectIdObs = createPickedObjectParmsObs(selectActionObs)

  /** Creates an observable of picked 3d model parameters */
  private def createPickedObjectParmsObs(actionObs: Observable[ActionMapping]): Observable[(Vector2f, Seq[String])] = {
    // Creates pick ray observable
    val rayObs = actionObs.filter(_.keyPressed).pickRay(Main.getCamera)
    // Creates collision observable
    val collisionObs = rayObs.pickCollision(Main.getRootNode)
    // Creates pick object parameters observable
    val objParmsObs = for {
      (collision, ray, pm) <- collisionObs
    } yield {
      for {
        spat <- findParent(Option(collision.getGeometry), s => !Option(s.getUserData("id")).isEmpty)
        data <- Option(spat.getUserData[String]("id"))
      } yield (pm.position, data.split(" ").toSeq)
    }
    // Filters non empty items
    for { Some((pos, id)) <- objParmsObs } yield (pos, id)
  }

  /** Creates observable of show popup panels */
  private lazy val showPopupObs: Observable[(Element, String, Vector2f)] =
    /** Subscribes for pop up panels */
    pickedObjectIdObs.map {
      case (pos, "track" +: _) => (semPopup, "semPane", pos)
      case (pos, "junction" +: _) => (semPopup, "semPane", pos)
      case (pos, "train" +: _) => (trainPopup, "semPane", pos)
    }

}
