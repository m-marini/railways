package org.mmarini.scala.jmonkey

import com.jme3.input.InputManager
import rx.lang.scala.Observable
import com.jme3.input.controls.AnalogListener
import rx.lang.scala.Subscription
import rx.lang.scala.Observer
import com.jme3.input.controls.ActionListener

/**
 * @author us00852
 */
trait InputManagerObservables extends NiftyObservables {
  def getInputManager: InputManager

  /** Creates an observable of actions */
  def actionObservable(names: String*): Observable[ActionMapping] = {
    val o = for {
      _ <- niftyObs
    } yield Observable.create((observer: Observer[ActionMapping]) => {
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
    o.flatten
  }

  /** Creates an observable of actions */
  def analogObservable(names: String*): Observable[AnalogMapping] = {
    val o = for {
      _ <- niftyObs
    } yield Observable.create((observer: Observer[AnalogMapping]) => {
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
    o.flatten
  }

}