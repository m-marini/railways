/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.PopupController
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.Pif
import org.mmarini.scala.railways.model.RightAngle
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Subscription
import rx.lang.scala.subscriptions.CompositeSubscription
import com.jme3.math.Quaternion
import rx.lang.scala.Subject
import scala.util.Try

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 * loads the station 3d models
 * wires the observable for camera viewpoint selections, time ticks and input actions
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {

  val TrainCameraHeight = 5f
  val TrainHeadCameraDistance = 10f
  val TrainCameraToDistance = 1f
  val TrainCameraPitch = RightAngle / 9

  init
  subscription

  private lazy val subscription =
    CompositeSubscription(cameraSubOpt.toArray ++
      trainPopupSubOpt ++
      trainStatusSubOpt ++
      performanceSubOpt ++
      messageHistorySubOpt :+
      messageDebugSub :+
      stationRenderSub :+
      vehicleRenderSub :+
      gameStatusSub: _*)

  lazy val endGameObs = Subject[GameStatus]

  // Creates the initial game status
  private lazy val initialStatus = GameStatus(parameters)

  // Creates the viewpoint map
  private lazy val viewpointMap = (for {
    v <- initialStatus.stationStatus.topology.viewpoints
  } yield (v.id -> v)).toMap

  lazy val gameStatusObs = {

    // Creates the observable of time events
    val timeEvents =
      for {
        time <- app.timeObs
      } yield (status: GameStatus) => {
        status.tick(time)
      }

    // Creates the observable of change status id
    val selectRightIdObs = createActionIdObs("selectRight")

    // Creates the observable for train command triggering
    val cmdTrainObsOpt = for {
      ctrl <- app.gameCtrlOpt
      popup <- ctrl.trainPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for {
      (ev, data) <- trigger(popupCtrl.mousePrimaryClickedObs, objectSelectIdObs)
      if (data(0) == "train")
    } yield ev.getElement.getId match {
      case "startTrain" => (status: GameStatus) => status.startTrain(data(1))
      case "stopTrain" => (status: GameStatus) => status.stopTrain(data(1))
      case "reverseTrain" => (status: GameStatus) => status.reverseTrain(data(1))
      case _ => (status: GameStatus) => status
    }

    // Create semaphore popup selection
    val semPopupResultObsOpt = for {
      ctrl <- app.gameCtrlOpt
      popup <- ctrl.semPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield trigger(popupCtrl.mousePrimaryClickedObs, objectSelectIdObs)

    // Creates the observable for junction
    val commandObsOpt = for {
      o <- semPopupResultObsOpt
    } yield for {
      (ev, data) <- o
      if (Set("junction", "track").contains(data(0)) && Set("clear", "lock").contains(ev.getElement.getId))
    } yield (ev.getElement.getId, data(0)) match {
      case ("clear", "junction") =>
        (status: GameStatus) => status.unlockJunction(data(1))(data(2).toInt)
        case ("lock", "junction") =>
        (status: GameStatus) => status.lockJunction(data(1))(data(2).toInt)
        case ("clear", "track") =>
        (status: GameStatus) => status.unlockTrack(data(1))(data(2).toInt)
        case ("lock", "track") =>
        (status: GameStatus) => status.lockTrack(data(1))(data(2).toInt)
    }

    //    trackToogleStateObs.subscribe { x => logger.debug(s"${x}") }

    // Creates block change state events
    val blockToogleStateObs =
      for {
        data <- objectSelectIdObs
        if (data(0) == "handler")
      } yield {
        (status: GameStatus) => status.toogleBlockStatus(data(1))(data(2).toInt)
      }

    val quitEventObsOpt = for {
      ctrl <- app.gameCtrlOpt
    } yield for {
      ev <- ctrl.mousePrimaryClickedObs
      if (ev.getElement.getId == "quit")
    } yield (status: GameStatus) => status.quit

    // Merges all observable to create n observable of all events
    val txObsSeq =
      cmdTrainObsOpt.toSeq ++
        quitEventObsOpt ++
        commandObsOpt :+
        blockToogleStateObs :+
        timeEvents

    val events = for {
      f <- txObsSeq.reduce((a, b) => a merge b)
    } yield (status: GameStatus) =>
      try { f(status) }
      catch {
        case t: Throwable =>
          logger.error(t.getMessage, t)
          throw t
      }

    // Creates the state observable
    stateFlow(initialStatus)(events)
  }

  // Creates the observable of change status id
  private def objectSelectIdPosObs = createActionIdObs("select")

  // Creates the observable of change status id
  private def objectSelectIdObs = for { (data, _) <- objectSelectIdPosObs } yield data

  /** Messages observer */
  private def messagesObs = {
    val msgsObs = for {
      status <- gameStatusObs
    } yield status.messages
    msgsObs.flatMapIterable { x => x }
  }

  private def performanceObs = for { status <- gameStatusObs } yield status.performance

  private def performanceSubOpt =
    for {
      ctrl <- app.gameCtrlOpt
    } yield performanceObs.subscribe(ctrl.show _)

  private def messageHistoryObs = history(messagesObs)(10)

  private def messageHistorySubOpt =
    for {
      ctrl <- app.gameCtrlOpt
    } yield messageHistoryObs.subscribe(ctrl.showMsgs _)

  private def messageDebugSub =
    messagesObs.subscribe { x => logger.debug(x.toString) }

  /** Creates observable of speed */
  private def speedObs = {
    // Creates the observable of up command transitions
    val upObs = for {
      action <- app.actionObservable("upCmd")
    } yield if (action.keyPressed) 1f else 0f

    // Creates the observable of down command transitions
    val downObs = for {
      action <- app.actionObservable("downCmd")
    } yield if (action.keyPressed) -1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObsOpt =
      for {
        gameScreen <- app.gameCtrlOpt
      } yield for {
        ev <- gameScreen.mousePrimaryClickedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "up" => 1f
        case "down" => -1f
        case _ => 0f
      }
    val buttonsReleaseObsOpt =
      for {
        gameScreen <- app.gameCtrlOpt
      } yield for {
        ev <- gameScreen.mousePrimaryReleaseObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield 0f

    mergeAll(buttonsPressObsOpt.toArray ++
      buttonsReleaseObsOpt :+
      upObs :+
      downObs: _*)
  }

  /** Creates observable of rotation speed */
  private def rotationSpeedObs = {
    // Creates the observable of left command transitions
    val leftObs = for {
      action <- app.actionObservable("leftCmd")
    } yield if (action.keyPressed) -1f else 0f

    // Creates the observable of left command transitions
    val rightObs = for {
      action <- app.actionObservable("rightCmd")
    } yield if (action.keyPressed) 1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObsOpt =
      for {
        gameScreen <- app.gameCtrlOpt
      } yield for {
        ev <- gameScreen.mousePrimaryClickedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "left" => -1f
        case "right" => 1f
      }

    val buttonsReleaseObsOpt =
      for {
        gameScreen <- app.gameCtrlOpt
      } yield for {
        ev <- gameScreen.mousePrimaryReleaseObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield 0f

    mergeAll(buttonsPressObsOpt.toArray ++
      buttonsReleaseObsOpt :+
      leftObs :+
      rightObs: _*)
  }

  /** Creates observable of rotate command */
  private def rotateObs = {
    // Creates the observable of xMouse axis and right button
    val xMouseButtonObs = for {
      (analog, action) <- trigger(app.mouseRelativeObservable("xAxis"),
        app.mouseRelativeActionObservable("rightMouseBtn"))
    } yield (analog.position.getX, action.keyPressed)

    // Filters the values of last two values with button press and
    // transforms to camera status transition 
    for {
      seq <- history(xMouseButtonObs)(2)
      if (seq.size > 1 && seq.forall(p => p._2))
    } yield (seq(0)._1 - seq(1)._1) * Pif

  }

  private def trainSeqObs = for { status <- gameStatusObs } yield status.trains.toIndexedSeq

  /** Creates observable of */
  private def trainFollowerObs = {
    // Creates the observable for train popup command triggering 
    val cmdPopupTrainObsOpt = for {
      ctrl <- app.gameCtrlOpt
      popup <- ctrl.trainPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for {
      (ev, data) <- trigger(popupCtrl.mousePrimaryClickedObs, objectSelectIdObs)
      if (ev.getElement.getId == "cameraTrain")
    } yield Option(data(1))

    // Creates the observable for train status panel triggering 
    val cmdPanelTrainObsOpt = for {
      gameCtrl <- app.gameCtrlOpt
      ctrl <- gameCtrl.trainCtrlOpt
      trainSelectionObs <- ctrl.selectionObsOpt
    } yield for {
      ((row, _), trains) <- trigger(trainSelectionObs, trainSeqObs)
    } yield Option(trains(row).id)

    val clearTrainObsOpt = for { o <- cameraSelectionObsOpt }
      yield for (_ <- o) yield None

    val trainFollowerObs = mergeAll(cmdPopupTrainObsOpt.toArray ++
      clearTrainObsOpt ++
      cmdPanelTrainObsOpt: _*)

    val x = for {
      (gameStatus, trainId) <- trigger(gameStatusObs, trainFollowerObs)
      if (!trainId.isEmpty)
    } yield {
      gameStatus.trains.find(trainId.get == _.id)
    }
    for {
      trainOpt <- x
      if (!trainOpt.isEmpty)
    } yield trainOpt.get
  }

  /** Creates observable of camera selection */
  private def cameraSelectionObsOpt =
    for {
      gameScreen <- app.gameCtrlOpt
      cameraCtrl <- gameScreen.cameraCtrlOpt
      idxObs <- cameraCtrl.selectionObsOpt
    } yield {
      for {
        (row, _) <- idxObs
      } yield {
        initialStatus.stationStatus.topology.viewpoints(row)
      }
    }

  /** Creates observable of location at */
  private def locationAtObsOpt = {
    // Camaera selected by panel
    val cameraAtObsOpt = for {
      o <- cameraSelectionObsOpt
    } yield for {
      vp <- o
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

    for { o <- cameraAtObsOpt } yield o merge cameraTrainObs
  }

  /** Creates observable of direction to */
  private def directionToObsOpt = {
    val cameraDirObsOpt = for {
      o <- cameraSelectionObsOpt
    } yield for {
      vp <- o
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

    for { o <- cameraDirObsOpt } yield o merge cameraTrainObs
  }

  /** Subscribe for camera movement */
  private def cameraSubOpt: Option[Subscription] = {
    for {
      locAtObs <- locationAtObsOpt
      dirToObs <- directionToObsOpt
    } yield {
      val (locObs, rotObs) = CameraUtils.createObservables(
        app.timeObs,
        speedObs,
        rotationSpeedObs,
        rotateObs,
        app.actionObservable("forwardCmd"),
        app.actionObservable("backwardCmd"),
        locAtObs,
        dirToObs)

      val locSub = for { o <- app.cameraLocationObserver } yield locObs.subscribe(o)
      val rotSub = for { o <- app.cameraRotationObserver } yield rotObs.subscribe(o)

      // Merges the subscriptions
      val listSub = rotSub.toArray ++ locSub

      CompositeSubscription(listSub: _*)
    }
  }

  /** Subscribes for game status changes */
  private def gameStatusSub = {
    val endObs = for {
      s <- gameStatusObs
      if (s.isFinished)
    } yield s
    endObs.subscribe(s => {
      onEnd
      endGameObs.onNext(s)
      endGameObs.onCompleted
    })
  }

  /** Subscribes for trigger pop up panels */
  private def trainPopupSubOpt =
    for {
      ctrl <- app.gameCtrlOpt
    } yield objectSelectIdPosObs.subscribe((p) => {
      val (id, pos) = p
      id(0) match {
        case "track" => ctrl.showSemaphorePopup(pos)
        case "junction" =>
          ctrl.showSemaphorePopup(pos)
        case "train" =>
          ctrl.showTrainPopup(pos)
        case _ =>
      }
    })

  /** Subscribe for train render */
  private def vehicleRenderSub = {
    // Creates train renderer transitions
    val vehicleRenderTxObs = for {
      status <- gameStatusObs
    } yield (renderer: VehicleRenderer) => renderer(status.vehicles)

    // Creates the train renderer and subscribe to it
    val vehicleRenderObs = stateFlow(VehicleRenderer(app.getAssetManager))(vehicleRenderTxObs)

    vehicleRenderObs.subscribe(renderer => {
      for (spatial <- renderer.detached) {
        app.getRootNode.detachChild(spatial)
      }
      for (spatial <- renderer.attached) {
        app.getRootNode.attachChild(spatial)
      }
      for ((vehicle, spatial) <- renderer.vehicleSpatialsSet) {
        val id = s"train ${vehicle.id}"
        val pos = new Vector3f(-vehicle.location.getX, 0, vehicle.location.getY)
        val rot = new Quaternion().fromAngleNormalAxis(vehicle.orientation, OrientationAxis)
        spatial.setUserData("id", id)
        spatial.setLocalTranslation(pos)
        spatial.setLocalRotation(rot)
      }
    })
  }

  private def stationRenderSub = {
    val txObs = for {
      status <- gameStatusObs
    } yield (renderer: StationRenderer) => renderer.change(status.stationStatus.blocks.values.toSet)

    val rendererObs = stateFlow(StationRenderer(app.getAssetManager))(txObs)

    rendererObs.subscribe(renderer => {
      for (spatial <- renderer.detached) app.getRootNode.detachChild(spatial)
      for (spatial <- renderer.attached) app.getRootNode.attachChild(spatial)
    })
  }

  private def init {

    loadViewpoints
    loadBackstage

    // Creates the terrain builder
    for { t <- TerrainBuilder.build(app.getAssetManager, app.getCamera) } {
      app.getRootNode.attachChild(t)
    }
  }

  /** Trains status subscription */
  private def trainStatusSubOpt =
    for {
      gameCtrl <- app.gameCtrlOpt
      ctrl <- gameCtrl.trainCtrlOpt
    } yield ctrl.subscribe(trainSeqObs)

  //------------------------------------------------------
  // Functions
  //------------------------------------------------------
  private def find(n: Option[Spatial])(f: (Spatial => Boolean)): Option[Spatial] =
    if (n.isEmpty) {
      None
    } else if (f(n.get)) {
      n
    } else {
      find(Option(n.get.getParent))(f)
    }

  /** Creates an observable of id of pickable 3d model */
  private def createActionIdObs(actionId: String) = {
    val pr = app.pickRay(app.actionObservable(actionId).filter(_.keyPressed))
    val idMouseOptObs =
      for { (cr, ray) <- app.pickCollision(app.getRootNode)(pr) } yield {
        val spatOpt = find(Option(cr.getGeometry))(s => s.getUserData("id") != null)
        for (spat <- spatOpt) yield (spat.getUserData[String]("id"), ray.mousePos)
      }.filterNot(_._1.isEmpty())
    for {
      idMouseOpt <- idMouseOptObs
      if (!idMouseOpt.isEmpty)
    } yield {
      val Some((id, pos)) = idMouseOpt
      (id.split(" "), pos)
    }
  }

  /** Loads viewpoints list into hud panel */
  private def loadViewpoints {
    for {
      gameCtrl <- app.gameCtrlOpt
      ctrl <- gameCtrl.cameraCtrlOpt
    } ctrl.show(initialStatus.stationStatus.topology.viewpoints.map(_.id).toList)
  }

  /** Unsubscribes all the observers when game ends */
  private def onEnd {
    subscription.unsubscribe()
    app.getRootNode.detachAllChildren
  }

  /** Creates sky */
  private def skyTry = {
    // Load sky
    val SkyboxIndex = Seq("east", "west", "north", "south", "up", "down")
    val assetManager = app.getAssetManager
    val skyTry = Try {

      val imgs = for {
        idx <- SkyboxIndex
      } yield assetManager.loadTexture(s"Textures/sky/desert_${idx}.png")

      SkyFactory.createSky(assetManager, imgs(0), imgs(1), imgs(2), imgs(3), imgs(4), imgs(5))
    }

    for { e <- skyTry.failed } logger.error(e.getMessage, e)

    skyTry
  }

  /** Creates ambient light */
  private def ambientLightTry = Try {
    val ambLight = new AmbientLight
    ambLight.setColor(ColorRGBA.White.mult(1.3f))
    ambLight
  }

  /** Creates sun light */
  private def sunLightTry = Try {
    val sunLight = new DirectionalLight
    sunLight.setColor(ColorRGBA.White.mult(1.3f));
    sunLight.setDirection(Vector3f.UNIT_XYZ.negate().normalizeLocal())

    val sunLight1 = new DirectionalLight
    sunLight1.setColor(ColorRGBA.White.mult(1.3f));
    sunLight1.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal())

    Seq(sunLight, sunLight1)
  }

  private def lightsTry = for {
    a <- ambientLightTry
    sl <- sunLightTry
  } yield sl :+ a

  /** Loads backstage of scene */
  private def loadBackstage {
    val rootNode = app.getRootNode

    for { sky <- skyTry } rootNode.attachChild(sky)

    for (e <- lightsTry.failed) logger.error(e.getMessage, e)

    for {
      lights <- lightsTry
      light <- lights
    } rootNode.addLight(light)
  }
}
