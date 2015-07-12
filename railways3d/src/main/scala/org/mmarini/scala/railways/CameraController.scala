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
import org.mmarini.scala.railways.model.PlatformTemplate
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
import com.jme3.math.Vector2f
import com.jme3.scene.Node
import com.fasterxml.jackson.databind.util.RootNameLookup
import com.jme3.asset.AssetManager
import com.jme3.scene.control.CameraControl
import org.mmarini.scala.railways.model._

/**
 * Handles the movement of the camera to the target viewpoint
 */
class CameraController(camera: Camera, assetManager: AssetManager, node: Node) extends LazyLogging {

  // Creates the path
  private val path = new MotionPath
  path.setCycle(true)

  // Create the camera node
  private val camNode = new CameraNode("Motion cam", camera)

  // Creates the camera controller
  private val cameraController = new MotionEvent(camNode, path, 3f)

  path.setCycle(false)
  path.addWayPoint(new Vector3f)

  node.attachChild(camNode)
  camNode.setControlDir(ControlDirection.SpatialToCamera)
  camNode.setLocalTranslation(0, 2, 0)
  camNode.setEnabled(true)

  cameraController.setLoopMode(LoopMode.DontLoop)
  cameraController.setLookAt(new Vector3f, Vector3f.UNIT_Y)

  /** Sets the motion path of camera to go to the view point and activate the animation of camera */
  def setView(target: Vector3f) {
    try {
      logger.debug(s"target at $target")
      if (path.getNbWayPoints() >= 2) {
        path.removeWayPoint(0)
      }
      path.addWayPoint(target)
      path.enableDebugShape(assetManager, node)
      path.setCurveTension(1f)
      //        cameraController.setLookAt(target, Vector3f.UNIT_Y)
      cameraController.setRotation(new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y))
      cameraController.setDirectionType(MotionEvent.Direction.Path)
      cameraController.setCurrentWayPoint(path.getNbWayPoints() - 2)
      cameraController.play()
    } catch {
      case e: Exception => logger.error(e.getMessage, e)
    }
  }

  /** Set the view to a camera viewpoint */
  def change(target: CameraViewpoint) {
    camNode.setLocalTranslation(target.location)
    camNode.setLocalRotation(target.direction)
  }
}
