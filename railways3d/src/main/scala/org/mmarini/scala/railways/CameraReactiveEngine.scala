/**
 *
 */
package org.mmarini.scala.railways

import rx.lang.scala.Observable

import rx.lang.scala.subscriptions.CompositeSubscription
import rx.lang.scala.Subscription
import org.mmarini.scala.railways.model.GameStatus
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.CameraViewpoint
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.scene.Spatial
import scala.util.Try
import com.jme3.util.SkyFactory
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.math.Quaternion
import com.jme3.scene.CameraNode
import rx.lang.scala.Subscriber
import com.jme3.scene.control.CameraControl.ControlDirection
import org.mmarini.scala.railways.model.CameraViewpoint
import org.mmarini.scala.railways.model.Train
import org.mmarini.scala.railways.model.RightAngle
import org.mmarini.scala.railways.model.Pif
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameParameters
import de.lessvoid.nifty.Nifty
import org.mmarini.scala.jmonkey.AnalogMapping
import de.lessvoid.nifty.controls.ButtonClickedEvent
import org.mmarini.scala.jmonkey.ActionMapping
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryReleaseEvent
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent
import org.mmarini.scala.jmonkey.TableController
import scala.collection.mutable.IndexedSeq
import org.mmarini.scala.jmonkey._
import scala.math.round

/**
 * @author us00852
 */

object CameraReactiveEngine extends LazyLogging {

  val TrainCameraHeight = 5f
  val TrainHeadCameraDistance = 10f
  val TrainCameraToDistance = 1f
  val TrainCameraPitch = RightAngle / 9

  val MaxRotationSpeed = Pif / 2f
  val MaxSpeed = 120f / 3.6f
  val StepLength = 10f

}

class CameraReactiveEngine(
    timeObs: => Observable[Float],
    cameraSelectionObs: => Observable[CameraViewpoint],
    upCommandActionObs: => Observable[ActionMapping],
    downCommandActionObs: => Observable[ActionMapping],
    leftCommandActionObs: => Observable[ActionMapping],
    rightCommandActionObs: => Observable[ActionMapping],
    selectRightActionObs: => Observable[ActionMapping],
    xRelativeAxisObs: => Observable[AnalogMapping],
    forwardAnalogObs: => Observable[AnalogMapping],
    backwardAnalogObs: => Observable[AnalogMapping],
    gameMouseClickedObs: => Observable[NiftyMousePrimaryClickedEvent],
    gameMouseReleasedObs: => Observable[NiftyMousePrimaryReleaseEvent]) extends LazyLogging {

  import CameraReactiveEngine._

  // ======================================================
  // Observables
  // ======================================================

  /** Creates the movement observable */
  lazy val cameraMovementObs = {

    val rotTimeTxObs = for {
      (dt, speed) <- timeObs.withLatest(0f +: rotationSpeedObs)
      if (speed != 0 && dt != 0)
    } yield (direction: Vector3f) => rotateByTime(direction, speed, dt)

    val dirToTxObs = for { dir <- directionToObs } yield (_: Vector3f) => dir

    val rotObs = for { angle <- rotateObs } yield (direction: Vector3f) => rotate(direction, angle)

    val directionObs =
      (rotTimeTxObs merge dirToTxObs merge rotObs).statusFlow(Vector3f.UNIT_Z)

    val dirSpeedObs: Observable[(Vector3f, Float)] = directionObs.combineLatest(speedObs)

    //[Float, (Vector3f, Float), Vector3f => Vector3f]
    val moveTimeTxObs = timeObs.withLatest((Vector3f.UNIT_Z, 0f) +: dirSpeedObs)(
      (dt: Float, x: (Vector3f, Float)) => x match {
        case (direction, speed) => (location: Vector3f) => moveByTime(location, direction, speed, dt)
      })

    val locAtTxObs = for { location <- translationObs } yield (_: Vector3f) => location

    val forwardTxObs = forwardAnalogObs.withLatest(Vector3f.UNIT_Z +: directionObs)(
      (_, direction) => (location: Vector3f) => step(location, direction))

    val backwardTxObs = backwardAnalogObs.withLatest(Vector3f.UNIT_Z +: directionObs)(
      (_, direction) => (location: Vector3f) => step(location, direction.negate))

    val locationObs =
      (moveTimeTxObs merge
        locAtTxObs merge
        forwardTxObs merge
        backwardTxObs).statusFlow(Vector3f.ZERO)

    val rotationObs = for { dir <- directionObs } yield rotation(dir)

    (locationObs, rotationObs)
  }

  private lazy val trainFollowerObs: Observable[Train] =
    Observable.never

  /** Creates the observable of camera translation */
  private lazy val translationObs: Observable[Vector3f] = {
    // Camera selected by panel
    val cameraAtObs = for {
      vp <- cameraSelectionObs
    } yield vp.location

    // Camera located at train
    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      location <- train.locationAt(TrainHeadCameraDistance)
    } yield new Vector3f(
      -location.getX,
      TrainCameraHeight,
      location.getY)

    val cameraTrainObs = for {
      locOpt <- cameraTrainOptObs
      if (!locOpt.isEmpty)
    } yield locOpt.get

    cameraAtObs merge cameraTrainObs
  }

  /** Creates the observable of camera direction */
  private lazy val directionToObs: Observable[Vector3f] = {
    val cameraDirObsOpt = for {
      vp <- cameraSelectionObs
    } yield vp.direction

    val cameraTrainOptObs = for {
      train <- trainFollowerObs
    } yield for {
      angle <- train.directionAt(TrainHeadCameraDistance, TrainCameraToDistance)
    } yield new Quaternion().
      fromAngleNormalAxis(angle, Vector3f.UNIT_Y).
      mult(new Quaternion().fromAngleNormalAxis(TrainCameraPitch, Vector3f.UNIT_X)).
      mult(Vector3f.UNIT_Z)

    val cameraTrainObs = for {
      locOpt <- cameraTrainOptObs
      if (!locOpt.isEmpty)
    } yield locOpt.get
    cameraDirObsOpt merge cameraTrainObs
  }

  /** Creates the observable of camera speed */
  private lazy val speedObs: Observable[Float] = {
    // Creates the observable of up command transitions
    val upObs = for {
      action <- upCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Creates the observable of down command transitions
    val downObs = for {
      action <- downCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- gameMouseClickedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "up" => 1f
        case "down" => -1f
        case _ => 0f
      }
    val buttonsReleaseObs =
      for {
        ev <- gameMouseReleasedObs
        if (Set("up", "down").contains(ev.getElement.getId))
      } yield 0f

    buttonsPressObs merge
      buttonsReleaseObs merge
      upObs merge
      downObs
  }

  /** Creates the observable of speedo of camera rotation */
  private lazy val rotationSpeedObs: Observable[Float] = {
    // Creates the observable of left command transitions
    val leftObs = for {
      action <- leftCommandActionObs
    } yield if (action.keyPressed) -1f else 0f

    // Creates the observable of left command transitions
    val rightObs = for {
      action <- rightCommandActionObs
    } yield if (action.keyPressed) 1f else 0f

    // Create observable of pressed visual buttons
    val buttonsPressObs =
      for {
        ev <- gameMouseClickedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield ev.getElement.getId match {
        case "left" => -1f
        case "right" => 1f
      }

    val buttonsReleaseObs =
      for {
        ev <- gameMouseReleasedObs
        if (Set("left", "right").contains(ev.getElement.getId))
      } yield 0f

    buttonsPressObs merge
      buttonsReleaseObs merge
      leftObs merge
      rightObs
  }

  /** Creates the observable of camera rotation */
  private lazy val rotateObs: Observable[Float] = {
    // Creates the observable of xMouse axis and right button
    val xMouseButtonObs =
      (xRelativeAxisObs withLatest selectRightActionObs)(
        (analog, action) => (analog.position.getX, action.keyPressed))

    // Filters the values of last two values with button press and
    // transforms to camera status transition
    for {
      seq <- xMouseButtonObs.history(2)
      if (seq.size > 1 && seq.forall(p => p._2))
    } yield (seq(0)._1 - seq(1)._1) * Pif

  }

  // ===================================================================
  // Functions
  // ===================================================================

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
