/**
 *
 */
package org.mmarini.scala.railways

import scala.util.Try
import org.mmarini.scala.railways.model.BlockStatus
import org.mmarini.scala.railways.model.BlockTemplate
import org.mmarini.scala.railways.model.Entry
import org.mmarini.scala.railways.model.EntryStatus
import org.mmarini.scala.railways.model.Exit
import org.mmarini.scala.railways.model.ExitStatus
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.Platform
import org.mmarini.scala.railways.model.PlatformStatus
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.terrain.geomipmap.TerrainLodControl
import com.jme3.terrain.geomipmap.TerrainQuad
import com.jme3.terrain.heightmap.ImageBasedHeightMap
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observer
import com.jme3.collision.CollisionResult
import rx.lang.scala.Subscription
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.MouseInput
import com.jme3.cinematic.events.MotionEvent
import com.jme3.animation.LoopMode
import com.jme3.cinematic.MotionPath
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.input.ChaseCamera
import scala.util.Random
import scala.collection.immutable.Vector
import rx.lang.scala.Observable
import com.jme3.scene.Spatial
import com.jme3.scene.control.CameraControl
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import com.jme3.scene.Geometry

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

  // Creates the event observable
  private val timeEvents = app.timeObservable.map[GameStatus => GameStatus](
    p => s => s.tick(p._2))

  private val changeStatus =
    for { cs <- app.actions.get("changeState") } yield {
      val pr = app.pickRay(cs.filter(_.keyPressed))
      app.pickCollision(app.getRootNode)(pr).
        map(_.getGeometry).
        map(stationRend.findByGeo).
        filterNot(_.isEmpty).
        map(_.get).
        map[GameStatus => GameStatus](id => s => {
          s.changeBlockStatus(id)
        })
    }

  private val changeFreedom =
    for { cs <- app.actions.get("additionalChangeState") } yield {
      val pr = app.pickRay(cs.filter(_.keyPressed))
      app.pickCollision(app.getRootNode)(pr).
        map(_.getGeometry).
        map(stationRend.findByGeo).
        filterNot(_.isEmpty).
        map(_.get).
        map[GameStatus => GameStatus](id => s => {
          s.changeBlockFreedom(id)
        })
    }

  val events = (timeEvents :: changeStatus.toList ::: changeFreedom.toList).reduce((a, b) => a.merge(b))

  // Creates the state observable
  private val state = stateFlow(initialStatus)(events)

  // Create the station renderer
  private val stationRend = new StationRenderer(
    initialStatus.blocks.values.map(_.block).toSet,
    app.getAssetManager,
    app.getRootNode)

  // Creates the viewpoint map
  private val viewpointMap = initialStatus.topology.viewpoints.map(v => (v.id, v)).toMap

  // Creates the camera controller
  private val cameraController = new CameraController(app.getCamera, app.getAssetManager, app.getRootNode)

  loadViewpoints
  loadBackstage

  // Creates the terrain builder
  private val terrainTry = TerrainBuilder.build(app.getAssetManager, app.getCamera)

  terrainTry.foreach(app.getRootNode.attachChild)

  // Creates train renderer transitions
  private val trainRendTransitions = state.map((status: GameStatus) => {
    (renderer: TrainRenderer) =>
      {
        renderer.render(status.trains)
      }
  })

  // Creates the train renderer and subscribe to it
  private val trainRendSub = stateFlow(TrainRenderer(app.getRootNode(), app.getAssetManager()))(trainRendTransitions).subscribe

  // Subscribes for status change
  private val statusSub = state.subscribe(status => {
    stationRend.change(status)
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

  logger.debug("Completed")

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
      initialStatus.topology.viewpoints.map(_.id).toList))
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
