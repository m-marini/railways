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
import com.jme3.input.InputManager
import org.mmarini.scala.jmonkey.ApplicationOps
import com.jme3.renderer.Camera

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
class CameraController(camera: Camera, collisions: Observable[CollisionResult]) extends LazyLogging {

  /** Returns the camera controller */
  val cameraController: Try[MotionEvent] = createCameraController

  private val changeViewObservable: Observable[Vector3f] = createChangeView

  private val cameraObserver: Try[Observer[Vector3f]] = createObserver

  /** creates the subscription that manages the camera */
  def subscribe(): Try[Subscription] =
    for {
      observer <- cameraObserver
    } yield changeViewObservable.subscribe(observer)

  //
  //      rootNode.attachChild(camNode)
  //    path.enableDebugShape(assetManager, rootNode)
  //    cameraMotionControl.setLookAt(target, Vector3f.UNIT_Y)
  //

  /** Creates camera controller */
  private def createCameraController: Try[MotionEvent] = Try {
    val camNode = new CameraNode("Motion cam", camera)
    camNode.setControlDir(ControlDirection.SpatialToCamera)

    val path = new MotionPath
    path.setCycle(true)
    path.setCurveTension(0.5f)

    val cameraController = new MotionEvent(camNode, path, 10f)
    cameraController.setLoopMode(LoopMode.Loop)
    cameraController.setDirectionType(MotionEvent.Direction.Path)

    cameraController
  }

  /**
   * Creates the observer of change view point.
   * Sets the motion path of camera to go to the view point and activate the animation of camera
   */
  private def createChangeView: Observable[Vector3f] =
    collisions.map(_.getContactPoint)

  /** Moves camera to a target point */

  /**
   * Creates the observer of change view point.
   * Sets the motion path of camera to go to the view point and activate the animation of camera
   */
  private def createObserver: Try[Observer[Vector3f]] =
    for {
      controller <- cameraController
    } yield Observer((target: Vector3f) => {
      val path = controller.getPath()
      path.addWayPoint(target)
      controller.setLookAt(target, Vector3f.UNIT_Y)
      controller.setEnabled(true)
      controller.play()
    })
}
