package org.mmarini.scala.railways

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model._
import scala.math.sin
import scala.math.cos

/**
 * @author us00852
 */
case class FreeCameraStatus(
  location: Vector3f = Vector3f.ZERO,
  direction: Vector3f = Vector3f.UNIT_Z,
  rotationSpeed: Float = 0f,
  speed: Float = 0f)
    extends CameraStatus with LazyLogging {

  val MaxRotationSpeed = Pif / 2f
  val MaxSpeed = 20f / 3.6f

  /** Creates a new status with elapsed time */
  override def tick(tpf: Float) = {
    val ds = direction.clone().setY(0).multLocal(speed * MaxSpeed * tpf)
    val newLoc = location.add(ds)
    val newDir =
      new Quaternion().fromAngleAxis(rotationSpeed * MaxRotationSpeed * tpf,
        Vector3f.UNIT_Y.clone().negate).mult(direction)

    FreeCameraStatus(newLoc,
      newDir,
      rotationSpeed,
      speed)
  }

  override val orientation = {
    val o = new Quaternion
    o.lookAt(direction, Vector3f.UNIT_Y)
    o
  }

  /** Creates a new status with a rotation speed */
  override def setRotationSpeed(speed: Float) =
    FreeCameraStatus(location,
      direction,
      speed,
      this.speed)

  /** Creates a new status with speed */
  override def setSpeed(speed: Float) =
    FreeCameraStatus(location,
      direction,
      rotationSpeed,
      speed)

  /** Creates a new status */
  override def setViewAt(location: Vector3f, direction: Vector3f) =
    FreeCameraStatus(location,
      direction,
      rotationSpeed,
      speed)

}

object FreeCameraStatus {
}