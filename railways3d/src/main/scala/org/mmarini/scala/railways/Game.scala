/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameStatus
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging
import sun.org.mozilla.javascript.internal.ast.Yield
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import de.lessvoid.nifty.Size
import de.lessvoid.nifty.tools.SizeValue
import org.mmarini.scala.jmonkey.PopupController
import de.lessvoid.nifty.controls.Controller
import rx.lang.scala.Subscription

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

  // Creates the initial game status
  val initialStatus = GameStatus(parameters)

  private val subscriptions = createSubscriptions

  private def createSubscriptions = {

    // Creates the observable of time events
    val timeEvents =
      for { (_, time) <- app.timeObservable } yield (status: GameStatus) => status.tick(time)

    // Creates the observable of change status id
    val selectRightIdObs = createActionIdObs("selectRight")

    // Creates the observable of change status id
    val selectIdObs = createActionIdObs("select")

    // Subscribes for trigger popup panels
    val trainPopupTriggerSubOpt =
      for {
        ctrl <- app.controllerById[GameController]("game-screen")
      } yield selectIdObs.subscribe((p) => {
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

    // Creates the observable for train command triggering
    val cmdTrainObsOpt = for {
      ctrl <- app.controllerById[GameController]("game-screen")
      popup <- ctrl.trainPopup
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for { (cmd, data) <- trigger(popupCtrl.buttons, selectIdObs.map(_._1)) } yield cmd match {
      case "start" => (status: GameStatus) => status.startTrain(data(1))
      case "stop" => (status: GameStatus) => status.stopTrain(data(1))
      case "reverse" => (status: GameStatus) => status.reverseTrain(data(1))
      case _ => (status: GameStatus) => status
    }

    // Creates the observable for train command triggering
    val cmdJunctionObsOpt = for {
      ctrl <- app.controllerById[GameController]("game-screen")
      popup <- ctrl.semPopup
      popupCtrl <- Option(popup.findControl(popup.getId, classOf[PopupController]))
    } yield for { (cmd, data) <- trigger(popupCtrl.buttons, selectIdObs.map(_._1)) } yield cmd match {
      case "clear" => (status: GameStatus) => status.unlockJunction(data(1))(data(2).toInt)
      case "lock" => (status: GameStatus) => status.lockJunction(data(1))(data(2).toInt)
      case _ => (status: GameStatus) => status
    }

    // Creates block change state events
    val blockToogleStateObs =
      for {
        (data, _) <- selectIdObs if (data(0) == "handler")
      } yield {
        (status: GameStatus) => status.toogleBlockStatus(data(1))(data(2).toInt)
      }

    // Merges all observable to create n observable of all events
    val txObsSeq =
      cmdTrainObsOpt.toSeq ++
        cmdJunctionObsOpt.toSeq :+
        blockToogleStateObs :+
        timeEvents

    val events = txObsSeq.reduce((a, b) => a merge b)

    // Creates the state observable
    val state = stateFlow(initialStatus)(events)

    // Creates the station renderer
    val stationRend = new StationRenderer(
      initialStatus.stationStatus.blocks.values.map(_.block).toSet,
      app.getAssetManager,
      app.getRootNode)

    // Creates the viewpoint map
    val viewpointMap = initialStatus.stationStatus.topology.viewpoints.map(v => (v.id, v)).toMap

    // Creates the camera controller
    val cameraController = new CameraController(app.getCamera, app.getAssetManager, app.getRootNode)

    loadViewpoints
    loadBackstage

    // Creates the terrain builder
    for { t <- TerrainBuilder.build(app.getAssetManager, app.getCamera) } {
      app.getRootNode.attachChild(t)
    }

    // Creates train renderer transitions
    val trainRendTransitions = for (status <- state) yield (renderer: TrainRenderer) => renderer.render(status.vehicles)

    // Creates the train renderer and subscribe to it
    val trainRendObs = stateFlow(TrainRenderer(
      app.getRootNode(),
      app.getAssetManager()))(trainRendTransitions)

    val trainRendSub = trainRendObs.subscribe

    // Subscribes for status change
    val statusSub = state.subscribe(status => stationRend.change(status.stationStatus))

    // Subscribes for camera change
    val subCameraChangeSub =
      for { gameScreen <- app.controllerById[GameController]("game-screen") } yield {
        gameScreen.cameraSelected.subscribe(_ match {
          case Some(id) =>
            val vp = viewpointMap(id)
            cameraController.change(vp)
          case _ =>
        })
      }

    trainPopupTriggerSubOpt.toSeq ++
      subCameraChangeSub :+
      trainRendSub :+
      statusSub
  }

  //  /** Subscribe changeView observer */
  //  private val cameraController =
  //    pickingScene.map(new CameraController(app.getCamera, _, app.getAssetManager, app.getRootNode))
  //
  //  private val cameraSubscription = cameraController.map(_.subscribe)
  //
  //  terrainTry.foreach(app.getRootNode.attachChild)
  //
  //  setCameraController

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
    val pr = app.pickRay(app.action(actionId).filter(_.keyPressed))
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
    for (s <- subscriptions) {
      s.unsubscribe()
    }
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
