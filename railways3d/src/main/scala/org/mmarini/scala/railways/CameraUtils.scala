package org.mmarini.scala.railways

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model._
import scala.math.sin
import scala.math.cos
import rx.lang.scala.Observable
import rx.lang.scala.Subject

/**
 * @author us00852
 */
object CameraUtils extends LazyLogging {

  val MaxRotationSpeed = Pif / 2f
  val MaxSpeed = 120f / 3.6f
  val StepLength = 10f

  def createObservables(
    timeObs: Observable[Float],
    speedObs: Observable[Float],
    rotationSpeedObs: Observable[Float],
    rotateObs: Observable[Float],
    stepForwardObs: Observable[Any],
    stepBackwordObs: Observable[Any],
    locationAtObs: Observable[Vector3f],
    directionToObs: Observable[Vector3f]): (Observable[Vector3f], Observable[Quaternion]) = {

    val rotTimeTxObs = for {
      (dt, speed) <- trigger(timeObs, rotationSpeedObs, Some(0f))
      if (speed != 0 && dt != 0)
    } yield (direction: Vector3f) => rotateByTime(direction, speed, dt)

    val dirToTxObs = for {
      dir <- directionToObs
    } yield (_: Vector3f) => dir

    val rotObs = for {
      angle <- rotateObs
    } yield (direction: Vector3f) => rotate(direction, angle)

    val directionObs = stateFlow(Vector3f.UNIT_Z)(rotTimeTxObs merge dirToTxObs merge rotObs)

    val dirSpeedObs: Observable[(Vector3f, Float)] = directionObs.combineLatest(speedObs)

    val moveTimeTxObs = for {
      (dt, (direction, speed)) <- trigger(timeObs, dirSpeedObs, Some(Vector3f.UNIT_Z, 0f))
    } yield (location: Vector3f) => moveByTime(location, direction, speed, dt)

    val locAtTxObs = for { location <- locationAtObs } yield (_: Vector3f) => location

    val forwardTxObs = for {
      (_, direction) <- trigger(stepForwardObs, directionObs, Some(Vector3f.UNIT_Z))
    } yield (location: Vector3f) => step(location, direction)

    val backwardTxObs = for {
      (_, direction) <- trigger(stepBackwordObs, directionObs, Some(Vector3f.UNIT_Z))
    } yield (location: Vector3f) => step(location, direction.negate())

    val locationObs = stateFlow(Vector3f.ZERO)(moveTimeTxObs merge
      locAtTxObs merge
      forwardTxObs merge
      backwardTxObs)

    val rotationObs = for { dir <- directionObs } yield rotation(dir)

    (locationObs, rotationObs)
  }

  /**
   * Computes the location given:
   * @param location
   * @param direction
   * @param speed
   * @param dt time interval
   */
  private def moveByTime(location: Vector3f, direction: Vector3f, speed: Float, dt: Float): Vector3f =
    location.add(direction.clone().setY(0).multLocal(speed * MaxSpeed * dt))

  /**
   * Computes the direction given:
   * @param direction
   * @param speed rotation speed
   * @param dt time interval
   */
  private def rotateByTime(direction: Vector3f, speed: Float, dt: Float): Vector3f =
    new Quaternion().fromAngleAxis(speed * MaxRotationSpeed * dt,
      Vector3f.UNIT_Y.clone().negate).mult(direction)

  /** Computes the forward location of a step */
  private def step(location: Vector3f, direction: Vector3f): Vector3f =
    location.add(direction.clone().setY(0).multLocal(StepLength))

  /**
   * Computes the camera rotation given:
   * @param direction
   */
  private def rotation(direction: Vector3f): Quaternion = {
    val o = new Quaternion
    o.lookAt(direction, Vector3f.UNIT_Y)
    o
  }

  /** Computes the direction rotated by an angle */
  private def rotate(direction: Vector3f, angle: Float): Vector3f =
    new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y.clone().negate).mult(direction)

}