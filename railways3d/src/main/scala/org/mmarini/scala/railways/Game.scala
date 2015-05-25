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

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {

  /** Returns the initial game status */
  private val initialStatus = GameStatus(parameters)

  private val blocks = initialStatus.blocks.values.map(_.block).toSet

  /** events observable */
  private val events = app.timeObservable.map[GameStatus => GameStatus](
    p => s => s.tick(p._2))

  /** game state observable */
  private val state = stateFlow(initialStatus)(events)

  val stationController = new StationController(state, initialStatus, app.getAssetManager, app.getRootNode)

  /** Subscribe block observer */
  val blockUnsub = stationController.subscribe

  private val terrainTry = TerrainBuilder(app.getAssetManager, app.getCamera)

  /** Picking ray observable */
  private val rays =
    for { terrain <- terrainTry } yield {
      val actions = app.getInputManager.
        createActionMapping("changeView").
        doOnNext(x => logger.debug(s"action=$x")).
        filter(_.keyPressed).
        doOnNext(x => logger.debug(s"filtered action=$x"))
      val rays = app.pickRay(actions)
      rays.doOnNext(x => logger.debug(s"ray=$x"))
    }

  /** */
  private val pickingScene = for {
    terrain <- terrainTry
    obs <- rays
  } yield app.pickCollision(terrain)(obs).doOnNext(x => logger.debug(s"collision=$x"))

  /** Subscribe changeView observer */
  private val cameraController =
    pickingScene.map(new CameraController(app.getCamera, _))

  private val cameraSubscription = cameraController.flatMap(_.subscribe)

  loadBackstage
  terrainTry.foreach(app.getRootNode.attachChild)
  attachMapping

  setCameraController
  logger.debug("Completed")

  //------------------------------------------------------
  // Functions
  //------------------------------------------------------

  /** Attaches mapping */
  private def attachMapping {
    val inputManager = app.getInputManager
    inputManager.addMapping("changeState", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("changeView", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("changeView", new KeyTrigger(KeyInput.KEY_G))
    inputManager.addMapping("additionalChangeState", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE))
    inputManager.addMapping("zoomSlider", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false))
  }

  /** Sets the camera controllers up */
  private def setCameraController {

    app.getFlyByCamera().setEnabled(true)

    for {
      ctrl <- cameraController
    } ctrl.register(app.getRootNode)
  }

  /** Unsubscribes all the observers when game ends */
  private def onEnd {
    blockUnsub.unsubscribe
    cameraSubscription.foreach(_.unsubscribe)
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
      sunLight.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal())
      rootNode.addLight(sunLight)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }
  }
}
