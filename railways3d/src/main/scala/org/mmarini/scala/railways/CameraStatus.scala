package org.mmarini.scala.railways

import com.jme3.math.Vector3f
import com.jme3.math.Quaternion

/**
 * @author us00852
 */
trait CameraStatus {
  /** Returns the location of camera */
  def location: Vector3f

  /** Returns the orientation of camera */
  def orientation: Quaternion

  /** Creates a new status with a rotation speed */
  def setRotationSpeed(speed: Float): CameraStatus = this

  /** Creates a new status with speed */
  def setSpeed(speed: Float): CameraStatus = this

  /** Creates a new status */
  def setViewAt(location: Vector3f, direction: Vector3f): CameraStatus = this

  /** Creates a new status */
  def rotate(angle: Float): CameraStatus = this

  /** Creates a new status with elapsed time */
  def tick(tpf: Float) = this

  /** Creates a new status with step forward */
  def stepForward: CameraStatus = this

  /** Creates a new status with step backward */
  def stepBackward: CameraStatus = this

}