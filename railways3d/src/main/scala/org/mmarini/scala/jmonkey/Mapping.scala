/**
 *
 */
package org.mmarini.scala.jmonkey

import com.jme3.math.Vector2f
import com.jme3.input.controls.AnalogListener
import com.jme3.input.InputManager
import scala.util.Try
import rx.lang.scala.Subscription
import rx.lang.scala.Observer
import rx.lang.scala.Observable
import com.jme3.input.controls.Trigger
import com.jme3.input.controls.ActionListener
import com.jme3.collision.CollisionResult
import com.jme3.math.Ray
import com.jme3.collision.CollisionResults
import com.jme3.app.Application
import com.jme3.scene.Node
import com.typesafe.scalalogging.LazyLogging

/** */

trait PositionMapping {
  def position: Vector2f
}

trait Mapping {
  def name: String
  def tpf: Float
}

case class ActionMapping(name: String, keyPressed: Boolean, position: Vector2f, tpf: Float) extends Mapping with PositionMapping
case class AnalogMapping(name: String, value: Float, position: Vector2f, tpf: Float) extends Mapping with PositionMapping

/** Adds functionalities to the jme3 inputManager */
class InputManagerOps(val inputManager: InputManager) extends LazyLogging {

  /** Creates an observable of actions */
  def createActionMapping(names: String*): Observable[ActionMapping] =
    Observable.create((observer: Observer[ActionMapping]) => {
      val l = new ActionListener() {
        def onAction(name: String, keyPressed: Boolean, tpf: Float) {
          observer.onNext(ActionMapping(name, keyPressed, inputManager.getCursorPosition.clone(), tpf))
        }
      }
      inputManager.addListener(l, names: _*)
      Subscription {
        inputManager.removeListener(l)
      }
    })

  /** Creates an observable of actions */
  def createAnalogMapping(names: String*): Observable[AnalogMapping] =
    Observable.create((observer: Observer[AnalogMapping]) => {
      val l = new AnalogListener() {
        def onAnalog(name: String, value: Float, tpf: Float) {
          observer.onNext(AnalogMapping(name, value, inputManager.getCursorPosition.clone(), tpf))
        }
      }
      inputManager.addListener(l, names: _*)
      Subscription {
        inputManager.removeListener(l)
      }
    })

}

case class RayMapping(ray: Ray, mousePos: Vector2f)

/** Adds functionalities to the jme3 application */
class ApplicationOps(val app: Application) {

  /** Returns the observable of pick ray */
  def pickRay(o: Observable[PositionMapping]): Observable[RayMapping] =
    for { _ <- o } yield {
      val mousePos = app.getInputManager.getCursorPosition
      val cam = app.getCamera
      val pos = cam.getWorldCoordinates(new Vector2f(mousePos), 0f).clone()
      val dir = cam.getWorldCoordinates(new Vector2f(mousePos), 1f).subtractLocal(pos).normalizeLocal()
      RayMapping(new Ray(pos, dir), new Vector2f(mousePos))
    }

  /** Returns the observable of pick object */
  def pickCollision(shootables: Node)(o: Observable[RayMapping]): Observable[(CollisionResult, RayMapping)] = {
    val collisions = for { rayMapping <- o } yield {
      val results = new CollisionResults()
      shootables.collideWith(rayMapping.ray, results)
      (results, rayMapping)
    }
    for {
      (cr, ray) <- collisions
      if (cr.size() > 0)
    } yield (cr.getClosestCollision(), ray)
  }
}


