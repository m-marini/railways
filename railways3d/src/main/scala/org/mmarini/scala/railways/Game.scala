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
  private val initialStatus = GameStatus(parameters)

  // Creates the observable of time events
  private val timeEvents =
    for { (_, time) <- app.timeObservable } yield (status: GameStatus) => status.tick(time)

  // Creates the observable of change status id
  private val additionalChangeStateIdObs =
    for { cs <- app.actions.get("additionalChangeState") } yield {
      val pr = app.pickRay(cs.filter(_.keyPressed))
      val idOptOvs =
        for { cr <- app.pickCollision(app.getRootNode)(pr) } yield {
          val spatOpt = find(Option(cr.getGeometry))(s => s.getUserData("id") != null)
          for (spat <- spatOpt) yield spat.getUserData[String]("id")
        }.filterNot(_.isEmpty())
      for (idOpt <- idOptOvs if (!idOpt.isEmpty)) yield idOpt.get
    }

  // Creates the observable of change status id
  private val changeStateIdObs =
    for { cs <- app.actions.get("changeState") } yield {
      val pr = app.pickRay(cs.filter(_.keyPressed))
      val idOptOvs =
        for { cr <- app.pickCollision(app.getRootNode)(pr) } yield {
          val spatOpt = find(Option(cr.getGeometry))(s => s.getUserData("id") != null)
          for (spat <- spatOpt) yield spat.getUserData[String]("id")
        }.filterNot(_.isEmpty())
      for (idOpt <- idOptOvs if (!idOpt.isEmpty)) yield idOpt.get
    }

  // Creates train change state events
  private val trainToogleStateObs = {
    for { obs <- additionalChangeStateIdObs } yield {
      for {
        id <- obs if (id.startsWith("train,"))
      } yield {
        val trainId = id.split(",")(1)
        (status: GameStatus) => status.toogleTrainStatus(trainId)
      }
    }
  }

  // Creates train change state events
  private val trainReverseObs = {
    for { obs <- changeStateIdObs } yield {
      for {
        id <- obs if (id.startsWith("train,"))
      } yield {
        val trainId = id.split(",")(1)
        (status: GameStatus) => status.reverseTrain(trainId)
      }
    }
  }

  // Creates block change state events
  private val blockToogleLockObs = {
    for { obs <- additionalChangeStateIdObs } yield {
      for {
        id <- obs if (id.startsWith("block,"))
      } yield {
        val blockId = id.split(",")(1)
        (status: GameStatus) => status.toogleLock(blockId)
      }
    }
  }

  // Creates block change state events
  private val blockToogleStateObs = {
    for { obs <- changeStateIdObs } yield {
      for {
        id <- obs if (id.startsWith("block,"))
      } yield {
        val blockId = id.split(",")(1)
        (status: GameStatus) => status.toogleBlockStatus(blockId)
      }
    }
  }

  private def dump(head: String)(n: Option[Spatial]) {
    n match {
      case Some(s) =>
        logger.debug("{} name={}", head, s.getName)
        logger.debug("{} id={}", head, s.getUserData("id"))
        dump(head + " ")(Option(s.getParent))
      case _ =>
    }
  }

  private def find(n: Option[Spatial])(f: (Spatial => Boolean)): Option[Spatial] =
    if (n.isEmpty) {
      None
    } else if (f(n.get)) {
      n
    } else {
      find(Option(n.get.getParent))(f)
    }

  // Merges all observable to create n observable of all events
  val events = (timeEvents ::
    blockToogleLockObs.toList :::
    blockToogleStateObs.toList :::
    trainToogleStateObs.toList :::
    trainReverseObs.toList).reduce((a, b) => a.merge(b))

  // Creates the state observable
  private val state = stateFlow(initialStatus)(events)

  // Creates the station renderer
  private val stationRend = new StationRenderer(
    initialStatus.stationStatus.blocks.values.map(_.block).toSet,
    app.getAssetManager,
    app.getRootNode)

  // Creates the viewpoint map
  private val viewpointMap = initialStatus.stationStatus.topology.viewpoints.map(v => (v.id, v)).toMap

  // Creates the camera controller
  private val cameraController = new CameraController(app.getCamera, app.getAssetManager, app.getRootNode)

  loadViewpoints
  loadBackstage

  // Creates the terrain builder
  private val terrainTry = TerrainBuilder.build(app.getAssetManager, app.getCamera)

  terrainTry.foreach(app.getRootNode.attachChild)

  // Creates train renderer transitions
  private val trainRendTransitions = state.map((status: GameStatus) =>
    (renderer: TrainRenderer) => renderer.render(status.vehicles))

  // Creates the train renderer and subscribe to it
  private val trainRendSub = stateFlow(TrainRenderer(app.getRootNode(), app.getAssetManager()))(trainRendTransitions).subscribe

  // Subscribes for status change
  private val statusSub = state.subscribe(status => {
    stationRend.change(status.stationStatus)
  })

  // Subscribes for camera change
  private val subCameraChange =
    for { gameScreen <- app.controller[GameController]("game-screen") } yield {
      gameScreen.cameraSelected.subscribe(_ match {
        case Some(id) =>
          val vp = viewpointMap(id)
          cameraController.change(vp)
        case _ =>
      })
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

  /** Loads viewpoints list into hud panel */
  private def loadViewpoints {
    app.controller[GameController]("game-screen").foreach(_.show(
      initialStatus.stationStatus.topology.viewpoints.map(_.id).toList))
  }

  /** Unsubscribes all the observers when game ends */
  def onEnd {
    statusSub.unsubscribe
    subCameraChange.foreach(_.unsubscribe)
    trainRendSub.unsubscribe
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
        val tex = assetManager.loadTexture(s"Textures/sky/ref-sky_${idx.toString.toLowerCase}.png")
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
