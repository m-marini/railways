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
import scala.math.tan
import com.jme3.math.Vector2f
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.controls.dynamic.PopupCreator
import rx.lang.scala.subjects.AsyncSubject
import org.mmarini.scala.railways.model.TrainMessage
import org.mmarini.scala.railways.model.TrainEnteredMsg
import org.mmarini.scala.railways.model.TrainExitedMsg
import org.mmarini.scala.railways.model.TrainStartedMsg
import org.mmarini.scala.railways.model.TrainReloadedMsg
import org.mmarini.scala.railways.model.TrainExitedMsg
import org.mmarini.scala.railways.model.TrainStoppedMsg
import org.mmarini.scala.railways.model.TrainWaitForReloadMsg
import org.mmarini.scala.railways.model.TrainWaitForTrackMsg
import rx.lang.scala.Subject
import com.jme3.scene.Node
import java.util.ResourceBundle
import org.mmarini.scala.railways.model.MovingTrain
import org.mmarini.scala.railways.model.StoppedTrain
import org.mmarini.scala.railways.model.WaitingForTrackTrain
import org.mmarini.scala.railways.model.WaitForPassengerTrain
import org.mmarini.scala.railways.model.StoppingTrain
import org.mmarini.scala.railways.model.StoppedTrain
import org.mmarini.scala.railways.model.WaitingForTrackTrain
import org.mmarini.scala.railways.model.WaitForPassengerTrain
import org.mmarini.scala.railways.model.CameraViewpoint
import org.mmarini.scala.railways.model.CameraHeight

/**
 * @author us00852
 */

object GameReactiveEngine extends LazyLogging {

  val PanelMaxMessageCount = 10
  val TrainSortingDefaultPriority = 10
}

class GameReactiveEngine(nifty: Nifty) extends LazyLogging {

  import GameReactiveEngine._

  val bundle = Main.bundle

  /** Train status classes sorted by priority */
  private val classPriorityMap = Seq[Class[_ <: Train]](
    classOf[StoppedTrain],
    classOf[WaitingForTrackTrain],
    classOf[WaitForPassengerTrain],
    classOf[StoppingTrain]).zipWithIndex.toMap

  // ======================================================
  // Subscriptions
  // ======================================================

  /** Composes all subscriptions */
  def subscribeForGameFlow {
    /** Subscribes for gotoScreen */
    screenNavigationObs.subscribe(nifty.gotoScreen _)

    /** Subscription to quit command */
    startCtrl.mousePrimaryClickedObs.filter(_.getElement.getId == "quitButton").subscribe { _ => Main.stop }

    /** Subscribes for parameter changes to startParameters */
    gameParamsObs.subscribe(parms => startCtrl.show(parms))

    /** Subscribes for camera movements */
    cameraTranslationObs.subscribe(location => cameraNode.setLocalTranslation(location))
    cameraRotationObs.subscribe(rotation => cameraNode.setLocalRotation(rotation))

    subscribeForSpatialManagement

    performanceEndGameObs.subscribe(performance => endGameCtrl.show(performance))

    /** Subscribes for camera viewpoint changes to camera panel */
    (cameraCtrlObs combineLatest viewpointObs).subscribe(_ match {
      case (ctrl, viewpoints) =>
        val cells = for { v <- viewpoints } yield { IndexedSeq("", cameraName(v.id)) }
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
      case (popup, panelId, position) =>
        nifty.showPopupAt(popup, panelId, position)
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
      case (spatial, key, value) =>
        spatial.setUserData(key, value)
    })

    /** Subscribes for attach spatials to root */
    detachObs.subscribe(_ match {
      case (node, spatial) =>
        node.detachChild(spatial)
    })

    detachAllObs.subscribe(_.detachAllChildren)

    /** Subscribes for attach spatials to root */
    attachObs.subscribe(_ match {
      case (node, spatial) =>
        logger.debug("attach {} to {}", spatial, node)
        node.attachChild(spatial)
    })

  }

  // ===================================================================
  // Functions
  // ===================================================================

  private lazy val startCtrl: StartController = nifty.screenControllerById[StartController]("start")

  private lazy val optionsCtrl: OptionsController = nifty.screenControllerById[OptionsController]("opts-screen")

  private lazy val gameCtrl: GameController = nifty.screenControllerById[GameController]("game-screen")

  private lazy val endGameCtrl: EndGameController = nifty.screenControllerById[EndGameController]("end-game-screen")

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

  /** Creates the terrain */
  private lazy val terrainOpt = TerrainBuilder.build(Main.getAssetManager, Main.getCamera).toOption

  /** Creates the backstage */
  private def backstage(status: GameStatus): Seq[Spatial] = {
    // Creates the terrain builder
    val s = Main.getAssetManager.loadModel(s"Textures/${status.parameters.stationName.toLowerCase}.blend").clone
    sky ++ terrainOpt :+ s
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

  private def messageText(msg: TrainMessage): String =
    bundle(msg.getClass.getName).format(msg.trainId)

  /** Returns the name of camera */
  private def cameraName(id: String): String = bundle(s"camera.$id")

  /** returns the name of block */
  private def blockName(id: String): String = bundle(s"block.$id")

  /** Returns the style name of image of light for the given train status */
  private def trainLightStyle(train: Train): String = train match {
    case _: StoppedTrain => "image.light-red"
    case _: WaitingForTrackTrain => "image.light-yellow"
    case _: WaitForPassengerTrain => "image.light-green"
    case _ => "image.light-off"
  }

  // ===================================================================
  // Decompositions
  // ===================================================================

  private lazy val cameraNode: CameraNode = {
    val camNode = new CameraNode("Motion cam", Main.getCamera)
    camNode.setControlDir(ControlDirection.SpatialToCamera)
    camNode.setEnabled(true)
    camNode
  }

  /** Creates the camera reactive engine */
  private lazy val cameraReactiveEngine = new CameraReactiveEngine(
    timeObs,
    cameraSelectionObs,
    followedTrainObs,
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
  private lazy val screenNavigationObs = {
    // start-screen selection
    val btnScreenMap = Map(
      "optionsButton" -> "opts-screen",
      "startButton" -> "game-screen")

    // Start-screen
    val btnStartNavObs = for {
      ev <- startCtrl.mousePrimaryClickedObs
      if (btnScreenMap.contains(ev.getElement.getId))
    } yield btnScreenMap(ev.getElement.getId)

    //    val endGameScreenObs = for { _ <- endGameButtonsObs } yield "start"
    val endGameScreenObs = for { _ <- endGameCtrl.mousePrimaryClickedObs } yield "start"

    val endGameGotoObs = for { _ <- performanceEndGameObs } yield "end-game-screen"

    btnStartNavObs merge
      optsConfirmObs merge
      endGameScreenObs merge
      endGameGotoObs
  }

  // Option selection
  private val optsConfirmObs = for {
    ev <- optionsButtonsObs
    if (ev.getButton.getId == "ok")
  } yield "start"

  private lazy val cameraCtrlObs: Observable[CameraController] = gameCtrl.controllerByIdObs("cameraPanel", classOf[CameraController])

  private lazy val trainsCtrlObs: Observable[TrainController] = gameCtrl.controllerByIdObs("trainPanel", classOf[TrainController])

  private lazy val messagesCtrlObs: Observable[TableController] = gameCtrl.controllerByIdObs("messagesPanel", classOf[TableController])

  private lazy val startButtonsObs: Observable[ButtonClickedEvent] = startCtrl.buttonClickedObs

  private lazy val optionsButtonsObs: Observable[ButtonClickedEvent] = optionsCtrl.buttonClickedObs

  private lazy val endGameButtonsObs: Observable[ButtonClickedEvent] = endGameCtrl.buttonClickedObs

  private lazy val cameraPanelObs: Observable[(Int, Int)] = cameraCtrlObs.map(_.selectionObsOpt).flatten

  private lazy val gameScreenObs: Observable[(String, ScreenControllerAdapter)] = gameCtrl.screenEventObs

  private lazy val xRelativeAxisObs: Observable[AnalogMapping] = Main.mouseRelativeObs("xAxis")

  private lazy val gameMouseClickedObs: Observable[NiftyMousePrimaryClickedEvent] = gameCtrl.mousePrimaryClickedObs

  private lazy val gameMouseReleasedObs: Observable[NiftyMousePrimaryReleaseEvent] = gameCtrl.mousePrimaryReleaseObs

  private def timeObs: Observable[Float] = Main.timeObs

  private lazy val selectActionObs: Observable[ActionMapping] = Main.actionObservable("select")
  private lazy val selectMidActionObs: Observable[ActionMapping] = Main.actionObservable("selectMid")
  private lazy val selectRightActionObs: Observable[ActionMapping] = Main.actionObservable("selectRight")
  private lazy val upCommandActionObs: Observable[ActionMapping] = Main.actionObservable("upCmd")
  private lazy val downCommandActionObs: Observable[ActionMapping] = Main.actionObservable("downCmd")
  private lazy val leftCommandActionObs: Observable[ActionMapping] = Main.actionObservable("leftCmd")
  private lazy val rightCommandActionObs: Observable[ActionMapping] = Main.actionObservable("rightCmd")

  private lazy val xAxisAnalogObs: Observable[AnalogMapping] = Main.analogObservable("xAxis")
  private lazy val forwardAnalogObs: Observable[AnalogMapping] = Main.analogObservable("forwardCmd")
  private lazy val backwardAnalogObs: Observable[AnalogMapping] = Main.analogObservable("backwardCmd")

  /** Creates the parameters generators from options panel */
  private lazy val gameParamsObs: Observable[GameParameters] = {
    val parmsObs = for {
      _ <- optionsButtonsObs
      parm <- optionsCtrl.readParametersObs
    } yield parm
    OptionsController.DefaultParms +: parmsObs
  }

  /** Creates the initial game status triggered by the start of game screen */
  private lazy val initialGameStatusObs: Observable[GameStatus] = {
    val initObs = gameScreenObs.
      filter(_._1 == "start").
      withLatest(gameParamsObs)(
        (_, parms) => try {
          GameStatus(parms)
        } catch {
          case t: Throwable =>
            logger.error(t.getMessage, t)
            throw t
        })
    initObs.share
  }

  /** Creates the observable of viewpoints */
  private lazy val viewpointObs: Observable[Seq[CameraViewpoint]] =
    for { status <- initialGameStatusObs } yield status.stationStatus.topology.viewpoints

  /** Creates observable of camera selection */
  private lazy val cameraSelectionObs: Observable[CameraViewpoint] = {
    val defaultViewObs = viewpointObs.map(_(0))
    val selectionObs = cameraPanelObs.
      withLatest(viewpointObs)(
        (idx, viewpoints) => viewpoints(idx._1))

    val YDir = 0f //tan(RightAngle / 9).toFloat
    val CameraDistance = 10f
    val objParmsObs = for {
      (collision, ray) <- pickedNoIdObs
    } yield {
      val contactPoint = collision.getContactPoint
      val direction = ray.getDirection
      val viewpoint = contactPoint.subtract(direction.mult(8f)).setY(2.5f)
      val viewDir = direction.setY(0).normalize.setY(-YDir).normalize
      CameraViewpoint("temp", viewpoint, viewDir)
    }
    defaultViewObs merge selectionObs merge objParmsObs
  }

  /** Creates the observable  of backstage of scene loading */
  private lazy val backstageObs: Observable[Spatial] = {
    for {
      status <- initialGameStatusObs
      spatial <- Observable.from(cameraNode +: backstage(status))
    } yield spatial
  }

  /** Creates the game status observable */
  private lazy val gameStatusObs: Observable[GameStatus] = {
    /** Creates the observable of game status transitions */
    val gameTransitionsObs: Observable[GameStatus => GameStatus] = {

      val timeGameTxObs = for { time <- timeObs }
        yield (status: GameStatus) => try {
        status.tick(time)
      } catch {
        case t: Throwable =>
          logger.error(t.getMessage, t)
          throw t
      }

      // Creates block change state events
      val blockToogleTxObs =
        for {
          (_, Seq("handler", id, handler)) <- pickedObjectIdObs
        } yield (status: GameStatus) => try {
          status.toogleBlockStatus(id)(handler.toInt)
        } catch {
          case t: Throwable =>
            logger.error(t.getMessage, t)
            throw t
        }

      // Creates train command transition
      val trainTxOptObs = for {
        cmdParms <- trainCmdObs
      } yield cmdParms match {
        case ("startTrain", trainId) => Some((status: GameStatus) => try {
          status.startTrain(trainId)
        } catch {
          case t: Throwable =>
            logger.error(t.getMessage, t)
            throw t
        })
        case ("stopTrain", trainId) => Some((status: GameStatus) => try { status.stopTrain(trainId) } catch {
          case t: Throwable =>
            logger.error(t.getMessage, t)
            throw t
        })
        case ("reverseTrain", trainId) => Some((status: GameStatus) => try { status.reverseTrain(trainId) } catch {
          case t: Throwable =>
            logger.error(t.getMessage, t)
            throw t
        })
        case _ => None
      }
      val trainTxObs = for (Some(f) <- trainTxOptObs) yield f

      // Creates the observable for junction
      val semTxOptObs = for {
        ctrl <- semPopupCtrlObs
        (ev, (_, objType +: objId +: idxStr +: _)) <- ctrl.mousePrimaryClickedObs withLatest pickedObjectIdObs
        idx = idxStr.toInt
      } yield (ev.getElement.getId, objType) match {
        case ("clear", "junction") =>
          Some((status: GameStatus) => status.unlockJunction(objId)(idx))
        case ("lock", "junction") =>
          Some((status: GameStatus) => status.lockJunction(objId)(idx))
        case ("clear", "track") =>
          Some((status: GameStatus) => status.unlockTrack(objId)(idx))
        case ("lock", "track") =>
          Some((status: GameStatus) => status.lockTrack(objId)(idx))
        case _ => None
      }
      val semTxObs = for (Some(f) <- semTxOptObs) yield f

      val quitTxObs = for {
        ev <- gameMouseClickedObs
        if (ev.getElement.getId == "quit")
      } yield (status: GameStatus) => status.quit

      timeGameTxObs merge
        blockToogleTxObs merge
        trainTxObs merge
        semTxObs merge
        quitTxObs
    }

    gameTransitionsObs.statusFlowWithInitObs(initialGameStatusObs, _.isFinished)
  }

  /** Creates the observable of station renderer */
  private lazy val stationRenderObs = {
    val gameTxObs = for {
      status <- gameStatusObs
    } yield (renderer: StationRenderer) => try {
      renderer.change(status.stationStatus.blocks.values.toSet)
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        logger.error(t.getMessage, t)
        throw t
    }
    val cleanTxObs = performanceEndGameObs.map(_ => (renderer: StationRenderer) => renderer.change(Set()))
    (gameTxObs merge cleanTxObs).statusFlow(StationRenderer(Main.getAssetManager))
  }

  /** Creates the observable of attach spatial */
  private lazy val attachObs: Observable[(Node, Spatial)] = {
    val bObs = backstageObs.map((Main.getRootNode, _))

    val stationAttacheObs = for {
      stationRend <- stationRenderObs
      spatial <- Observable.from(stationRend.attached)
    } yield spatial

    (backstageObs merge
      stationAttacheObs merge
      vehicleAttachObs).map((Main.getRootNode, _))
  }

  /** Creates the observable of attach spatial */
  private lazy val detachObs: Observable[(Node, Spatial)] = {
    val stationDetachObs = for {
      stationRend <- stationRenderObs
      spatial <- Observable.from(stationRend.detached)
    } yield spatial

    (stationDetachObs merge
      vehicleDetachObs).map((Main.getRootNode, _))
  }

  /** Creates the observable of attach spatial */
  private lazy val detachAllObs: Observable[Node] =
    performanceEndGameObs.map(_ => Main.getRootNode)

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
  private lazy val setUserDataSpatialObs: Observable[(Spatial, String, String)] =
    vehicleSetDataObs

  /** Creates the observable of messages */
  private lazy val messageObs = for {
    status <- gameStatusObs
    msg <- Observable.from(status.messages)
  } yield msg

  /** Creates the observable of message panel content */
  private lazy val messagePanelObs = for {
    ctrl <- messagesCtrlObs
    msgs <- messageObs.history(PanelMaxMessageCount)
  } yield (ctrl, msgs.map(m => IndexedSeq(messageText(m))).toIndexedSeq)

  /** Creates the observer of train sequence */
  private lazy val trainsSeqObs = for {
    status <- gameStatusObs
  } yield {
    val trains = status.trains.toIndexedSeq.sortWith(
      (a, b) => {
        val aIdx = classPriorityMap.getOrElse(a.getClass, TrainSortingDefaultPriority)
        val bIdx = classPriorityMap.getOrElse(b.getClass, TrainSortingDefaultPriority)
        if (aIdx < bIdx) {
          true
        } else if (aIdx > bIdx) {
          false
        } else if (a.creationTime < b.creationTime) {
          true
        } else if (a.creationTime > b.creationTime) {
          false
        } else {
          a.id < b.id
        }
      })
    for {
      train <- trains
    } yield (train, status.trainHeadBlock(train))
  }

  /** Creates the observer of train panel content */
  private lazy val trainsPanelObs =
    for {
      ctrl <- trainsCtrlObs
      trains <- trainsSeqObs
    } yield {
      val cells = for { (t, blockOpt) <- trains } yield {
        IndexedSeq(
          trainLightStyle(t),
          t.id.toUpperCase,
          blockName(t.exitId).toUpperCase,
          blockOpt.map(block => blockName(block.id)).getOrElse("---").toUpperCase,
          f"${round(3.6f * t.speed).toInt}%d")
      }
      (ctrl, cells)
    }

  /** Creates picked object with optional parameters observable */
  private val objParmsObs = {
    val rayObs = {
      selectActionObs.filter(_.keyPressed).pickRay(Main.getCamera)
    }
    // Creates collision observable
    val collisionObs = rayObs.pickCollision(Main.getRootNode)
    // Creates pick object parameters observable
    val objObs = for {
      (collision, ray, pm) <- collisionObs
    } yield {
      val idObjOpt = for {
        spat <- findParent(Option(collision.getGeometry), s => !Option(s.getUserData("id")).isEmpty)
        data <- Option(spat.getUserData[String]("id"))
      } yield (pm.position, data.split(" ").toSeq)
      (collision, ray, idObjOpt)
    }
    objObs.share
  }

  /** Creates the observable of picked object by selection */
  private lazy val pickedObjectIdObs =
    // Filters non empty items
    for { (_, _, Some((pos, id))) <- objParmsObs } yield (pos, id)

  /** Creates the observable of train pop up */
  private lazy val trainPopupObs = {
    val subj = AsyncSubject[Element]
    gameCtrl.niftyObs.map(_.createPopup("trainPopup")).subscribe(subj)
    subj
  }

  private lazy val pickedNoIdObs =
    // Filters non empty items
    for { (collision, ray, None) <- objParmsObs } yield (collision, ray)

  /** Creates the observable of semaphore pop up */
  private lazy val semPopupObs = {
    val subj = AsyncSubject[Element]
    gameCtrl.niftyObs.map(_.createPopup("semPopup")).subscribe(subj)
    subj
  }

  /** Creates observable of show pop up panels */
  private lazy val showPopupObs: Observable[(Element, String, Vector2f)] = {
    /** Subscribes for pop up panels */
    val optObs = for {
      trainPopup <- trainPopupObs
      semPopup <- semPopupObs
      pickedObj <- pickedObjectIdObs
    } yield pickedObj match {
      case (pos, "track" +: _) => Some((semPopup, "semPane", pos))
      case (pos, "junction" +: _) => Some((semPopup, "semPane", pos))
      case (pos, "train" +: _) => Some((trainPopup, "trainPane", pos))
      case _ => None
    }
    for (Some(data) <- optObs) yield data
  }

  /** Creates the observable of train pop up controller */
  private lazy val trainPopupCtrlObs = trainPopupObs.map(_.getControl(classOf[PopupController]))

  /** Creates the observable of semaphore pop up controller */
  private lazy val semPopupCtrlObs = semPopupObs.map(_.getControl(classOf[PopupController]))

  /** Creates the observable of train command by pop up menu */
  val trainCmdObs = for {
    popup <- trainPopupObs
    ctrl = popup.getControl(classOf[PopupController])
    (ev, (_, "train" +: trainId +: _)) <- ctrl.mousePrimaryClickedObs withLatest pickedObjectIdObs
  } yield (ev.getElement.getId, trainId)

  /** Creates observable of followed train */
  private lazy val followedTrainObs = {
    // Creates the observable for train pop up command triggering
    val cmdPopupTrainObsOpt = for {
      ("cameraTrain", trainId) <- trainCmdObs
    } yield Option(trainId)

    /** Creates the observable of train selected on train panel */
    val selectedTrainObs = for {
      ctrl <- trainsCtrlObs
      ((row, _), trains) <- ctrl.selectionObsOpt withLatest trainsSeqObs
    } yield Some(trains(row)._1.id)

    /** Generate the observable of no followed train */
    val clearTrainObs = cameraSelectionObs.map(_ => None)

    /** Merges all the observables */
    val followedTrainIdObs = cmdPopupTrainObsOpt merge clearTrainObs merge selectedTrainObs

    val trainOptObs = for {
      (gameStatus, Some(trainId)) <- gameStatusObs withLatest followedTrainIdObs
    } yield gameStatus.trains.find(trainId == _.id)
    for (Some(train) <- trainOptObs) yield train
  }

  /** Creates the observable of performance when game ends */
  private lazy val performanceEndGameObs = gameStatusObs.filter(_.isFinished).map(_.performance)
}
