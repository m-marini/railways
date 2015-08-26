package org.mmarini.scala.railways

import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.Subscription
import com.jme3.collision.CollisionResult
import com.jme3.math.Ray
import com.jme3.collision.CollisionResults
import com.jme3.math.Vector2f
import com.typesafe.scalalogging.LazyLogging

/**
 * @author us00852
 */

trait PositionMapping {
  def position: Vector2f
}

trait Mapping {
  def name: String
  def tpf: Float
}

case class ActionMapping(name: String, keyPressed: Boolean, position: Vector2f, tpf: Float) extends Mapping with PositionMapping
case class AnalogMapping(name: String, value: Float, position: Vector2f, tpf: Float) extends Mapping with PositionMapping

case class RayMapping(ray: Ray, mousePos: Vector2f)
