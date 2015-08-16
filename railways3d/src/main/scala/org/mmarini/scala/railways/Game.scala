/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.PopupController
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.Pif
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
  val TrainCameraHeight = 3f

  init
  subscription

  // Creates the initial game status
  private lazy val initialStatus = GameStatus(parameters)

  // Creates the observable of change status id
  private lazy val objectSelectIdPosObs = createActionIdObs("select")

  // Creates the observable of change status id
  private lazy val objectSelectIdObs = for { (data, _) <- objectSelectIdPosObs } yield data

  // Creates the viewpoint map
  private lazy val viewpointMap = (for {
    v <- initialStatus.stationStatus.topology.viewpoints
  } yield (v.id -> v)).toMap

  /** Creates observable of speed */
  private lazy val speedObsOpt = {
    // Creates the observable of up command transitions
    val upObs = for {
      action <- app.actionObservable("upCmd")
    } yield if (action.keyPressed) 1f else 0f

    // Creates the observable of down command transitions
    val downObs = for {
      action <- app.actionObservable("downCmd")
    } yield if (action.keyPressed) -1f else 0f

    // Create observable of pressed visual buttons
    val buttonsObsOpt =
      for {
        gameScreen <- app.controllerById[GameController]("game-screen")
      } yield for {
        e <- gameScreen.buttonsObservable
        if (Set("up", "down").contains(e._1))
      } yield e match {
        case ("up", true) => 1f
        case ("down", true) => -1f
        case _ => 0f
      }

    for (buttonsObs <- buttonsObsOpt) yield upObs merge downObs merge buttonsObs
  }

  /** Creates observable of rotation speed */
  private lazy val rotationSpeedObsOpt = {
    // Creates the observable of left command transitions
    val leftObs = for {
      action <- app.actionObservable("leftCmd")
    } yield if (action.keyPressed) -1f else 0f

    // Creates the observable of left command transitions
    val rightObs = for {
      action <- app.actionObservable("rightCmd")
    } yield if (action.keyPressed) 1f else 0f

    // Create observable of pressed visual buttons
    val buttonsObsOpt =
      for {
        gameScreen <- app.controllerById[GameController]("game-screen")
      } yield for {
        e <- gameScreen.buttonsObservable
        if (Set("left", "right").contains(e._1))
      } yield e match {
        case ("left", true) => -1f
        case ("right", true) => 1f
        case _ => 0f
      }

    for (buttonsObs <- buttonsObsOpt) yield leftObs merge rightObs merge buttonsObs
  }

  /** Creates observable of rotate command */
  private lazy val rotateObs = {
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

  /** Creates observable of */
  private lazy val trainFollowerObs = {
    // Creates the observable for train command triggering
    val cmdTrainObsOpt = for {
      ctrl <- app.controllerById[GameController]("game-screen")
      popup <- ctrl.trainPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for {
      (cmd, data) <- trigger(popupCtrl.buttons, objectSelectIdObs)
      if (cmd == "camera")
    } yield Option(data(1))

    val clearTrainObsOpt = for { o <- cameraSelectionObsOpt }
      yield for (_ <- o) yield None

    val trainFollowerObs = mergeAll(cmdTrainObsOpt.toArray ++ clearTrainObsOpt: _*)

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
  private lazy val cameraSelectionObsOpt =
    for {
      gameScreen <- app.controllerById[GameController]("game-screen")
    } yield for {
      id <- gameScreen.cameraSelected if (viewpointMap.contains(id))
    } yield viewpointMap(id)

  /** Creates observable of location at */
  private lazy val locationAtObsOpt = {

    val cameraAtObsOpt = for {
      o <- cameraSelectionObsOpt
    } yield for {
      vp <- o
    } yield vp.location

    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      location <- train.headLocation
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
  private lazy val directionToObsOpt = {
    val cameraDirObsOpt = for {
      o <- cameraSelectionObsOpt
    } yield for {
      vp <- o
    } yield vp.direction

    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      angle <- train.headDirection
    } yield new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y).mult(Vector3f.UNIT_Z)

    val cameraTrainObs = for {
      locOpt <- cameraTrainOptObs
      if (!locOpt.isEmpty)
    } yield locOpt.get

    for { o <- cameraDirObsOpt } yield o merge cameraTrainObs
  }

  /** Creates observable of elapsed time */
  private lazy val timeObs =
    for { (_, time) <- app.timeObservable } yield time

  /** Subscribe for camera movement */
  private lazy val cameraSubOpt: Option[Subscription] = {
    for {
      locAtObs <- locationAtObsOpt
      dirToObs <- directionToObsOpt
      speedObs <- speedObsOpt
      rotSpeedObs <- rotationSpeedObsOpt
    } yield {
      val (locObs, rotObs) = CameraUtils.createObservables(
        timeObs,
        speedObs,
        rotSpeedObs,
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

  lazy val gameStatusObs = {

    // Creates the observable of time events
    val timeEvents =
      for {
        time <- timeObs
      } yield (status: GameStatus) => status.tick(time)

    // Creates the observable of change status id
    val selectRightIdObs = createActionIdObs("selectRight")

    // Creates the observable for train command triggering
    val cmdTrainObsOpt = for {
      ctrl <- app.controllerById[GameController]("game-screen")
      popup <- ctrl.trainPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for {
      (cmd, data) <- trigger(popupCtrl.buttons, objectSelectIdObs)
      if (data(0) == "train")
    } yield cmd match {
      case "start" => (status: GameStatus) => status.startTrain(data(1))
      case "stop" => (status: GameStatus) => status.stopTrain(data(1))
      case "reverse" => (status: GameStatus) => status.reverseTrain(data(1))
      case _ => (status: GameStatus) => status
    }

    // Create semaphore popup selection
    val semPopupResultObsOpt = for {
      ctrl <- app.controllerById[GameController]("game-screen")
      popup <- ctrl.semPopupOpt
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield trigger(popupCtrl.buttons, objectSelectIdObs)

    // Creates the observable for junction
    val commandObsOpt = for {
      o <- semPopupResultObsOpt
    } yield for {
      (cmd, data) <- o
      if (Set("junction", "track").contains(data(0)) && Set("clear", "lock").contains(cmd))
    } yield (cmd, data(0)) match {
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

    // Merges all observable to create n observable of all events
    val txObsSeq =
      cmdTrainObsOpt.toSeq ++
        commandObsOpt :+
        blockToogleStateObs :+
        timeEvents

    val events = txObsSeq.reduce((a, b) => a merge b)

    // Creates the state observable
    stateFlow(initialStatus)(events)
  }

  /** Subscribes for game status changes */
  private lazy val gameStatusSub = gameStatusObs.subscribe

  /** Subscribes for trigger popup panels */
  private lazy val trainPopupSubOpt =
    for {
      ctrl <- app.controllerById[GameController]("game-screen")
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

  /** Subscribe for Train Render */
  private lazy val trainRenderSub = {
    // Creates train renderer transitions
    val trainRendTransitions = for {
      status <- gameStatusObs
    } yield (renderer: TrainRenderer) => renderer.render(status.vehicles)

    // Creates the train renderer and subscribe to it
    val trainRendObs = stateFlow(TrainRenderer(
      app.getRootNode(),
      app.getAssetManager()))(trainRendTransitions)

    trainRendObs.subscribe
  }

  private lazy val stationRenderSub = {
    // Creates the station renderer
    val stationRend = new StationRenderer(
      initialStatus.stationStatus.blocks.values.map(_.block).toSet,
      app.getAssetManager,
      app.getRootNode)
    gameStatusObs.subscribe(status => stationRend.change(status.stationStatus))
  }

  private def init {

    loadViewpoints
    loadBackstage

    // Creates the terrain builder
    for { t <- TerrainBuilder.build(app.getAssetManager, app.getCamera) } {
      app.getRootNode.attachChild(t)
    }
  }

  private lazy val subscription = {
    val all = cameraSubOpt.toArray ++
      trainPopupSubOpt :+
      stationRenderSub :+
      trainRenderSub :+
      gameStatusSub

    CompositeSubscription(all: _*)
  }

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
    app.controllerById[GameController]("game-screen").foreach(_.show(
      initialStatus.stationStatus.topology.viewpoints.map(_.id).toList))
  }

  /** Unsubscribes all the observers when game ends */
  def onEnd {
    subscription.unsubscribe()
  }

  /** Loads backstage of scene */
  private def loadBackstage {
    // Load sky
    val rootNode = app.getRootNode
    try {

      object SkyboxIndex extends Enumeration {
        val East, West, North, South, Up, Down = Value
      }

      import SkyboxIndex._
      implicit def skyboxIndexToInt(idx: SkyboxIndex.Value): Int = idx.id

      val assetManager = app.getAssetManager
      val imgs = (for (idx <- SkyboxIndex.values) yield {
        val tex = assetManager.loadTexture(s"Textures/sky/desert_${idx.toString.toLowerCase}.png")
        (idx -> tex)
      }).toMap

      val sky = SkyFactory.createSky(assetManager, imgs(East), imgs(West), imgs(North), imgs(South), imgs(Up), imgs(Down))

      rootNode.attachChild(sky)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }

    // Create ambient light
    try {
      val ambLight = new AmbientLight
      ambLight.setColor(ColorRGBA.White.mult(1.3f))
      rootNode.addLight(ambLight)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }

    // Create sun light
    try {
      val sunLight = new DirectionalLight
      sunLight.setColor(ColorRGBA.White.mult(1.3f));
      sunLight.setDirection(Vector3f.UNIT_XYZ.negate().normalizeLocal())
      rootNode.addLight(sunLight)
      val sunLight1 = new DirectionalLight
      sunLight1.setColor(ColorRGBA.White.mult(1.3f));
      sunLight1.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal())
      rootNode.addLight(sunLight1)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }
  }
}
