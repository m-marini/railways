package org.mmarini.scala.jmonkey

import com.jme3.input.InputManager
import rx.lang.scala.Observable
import com.jme3.input.controls.AnalogListener
import rx.lang.scala.Subscription
import rx.lang.scala.Observer
import com.jme3.input.controls.ActionListener
import com.jme3.math.Vector2f

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

/**
 * @author us00852
 */
trait InputManagerObservables extends NiftyObservables {
  def getInputManager: InputManager

  /** Creates an observable of actions */
  def actionObservable(names: String*): Observable[ActionMapping] =
    Observable.create((observer: Observer[ActionMapping]) => {
      val l = new ActionListener() {
        def onAction(name: String, keyPressed: Boolean, tpf: Float) {
          observer.onNext(ActionMapping(name, keyPressed, getInputManager.getCursorPosition.clone(), tpf))
        }
      }
      getInputManager.addListener(l, names: _*)
      Subscription {
        getInputManager.removeListener(l)
      }
    })

  /** Creates an observable of actions */
  def analogObservable(names: String*): Observable[AnalogMapping] =
    Observable.create((observer: Observer[AnalogMapping]) => {
      val l = new AnalogListener() {
        def onAnalog(name: String, value: Float, tpf: Float) {
          observer.onNext(AnalogMapping(name, value, getInputManager.getCursorPosition.clone(), tpf))
        }
      }
      getInputManager.addListener(l, names: _*)
      Subscription {
        getInputManager.removeListener(l)
      }
    })

}