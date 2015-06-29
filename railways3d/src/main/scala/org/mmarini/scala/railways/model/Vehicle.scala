/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math._

/**
 * @author us00852
 *
 */
trait Vehicle {
  /** Returns the id of vehicle */
  def id: String

  /** Returns the location of vehicle */
  def location: Vector2f

  /** Returns the orientation of vehicle */
  def orientation: Float

  /** Creates a new vehicle positioned by a vector from current */
  def moveBy(space: Vector2f): Vehicle
}

case class Coach(id: String, location: Vector2f, orientation: Float) extends Vehicle {

  /** Creates a new vehicle positioned by a vector from current */
  def moveBy(space: Vector2f): Vehicle = Coach(id, location.add(space), orientation)
}

case class Head(id: String, location: Vector2f, orientation: Float) extends Vehicle {

  /** Creates a new vehicle positioned by a vector from current */
  def moveBy(space: Vector2f): Vehicle = Head(id, location.add(space), orientation)
}

case class Tail(id: String, location: Vector2f, orientation: Float) extends Vehicle {

  /** Creates a new vehicle positioned by a vector from current */
  def moveBy(space: Vector2f): Vehicle = Tail(id, location.add(space), orientation)
}

object Coach {

  /** */
  def apply(id: String, route: TrainRoute, distance: Float): Option[Coach] = {
    for {
      p1 <- route.locationAt(distance - AxisDistance)
      p2 <- route.locationAt(distance - CoachLength + AxisDistance)
    } yield {
      val dir = p2.subtract(p1)
      val p0 = p1.add(dir.negate.mult(AxisDistance / dir.length))
      val orientation = atan2(dir.x, -dir.y).toFloat
      Coach(id, p0, orientation)
    }
  }
}

object Head {

  /** */
  def apply(id: String, route: TrainRoute, distance: Float): Option[Head] = {
    for {
      p1 <- route.locationAt(distance - AxisDistance)
      p2 <- route.locationAt(distance - CoachLength + AxisDistance)
    } yield {
      val dir = p2.subtract(p1)
      val p0 = p1.add(dir.negate.mult(AxisDistance / dir.length))
      val orientation = atan2(dir.x, -dir.y).toFloat
      Head(id, p0, orientation)
    }
  }
}

object Tail {

  /** */
  def apply(id: String, route: TrainRoute, distance: Float): Option[Tail] = {
    for {
      p1 <- route.locationAt(distance - AxisDistance)
      p2 <- route.locationAt(distance - CoachLength + AxisDistance)
    } yield {
      val dir = p2.subtract(p1)
      val p0 = p1.add(dir.negate.mult(AxisDistance / dir.length))
      val orientation = atan2(dir.x, -dir.y).toFloat
      Tail(id, p0, orientation)
    }
  }
}
