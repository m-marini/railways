package org.mmarini.scala.jmonkey

import scala.util.Try

import org.mmarini.scala.railways.OptionObservableFactory

import com.jme3.math.Vector2f
import com.typesafe.scalalogging.LazyLogging

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.tools.SizeValue
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import org.mmarini.scala.jmonkey._
/**
 * @author us00852
 */
trait NiftyObservables extends LazyLogging {

  def niftyObs: Observable[Nifty]

  def gotoScreen(id: String): Subscription = niftyObs.subscribe(nifty => nifty.gotoScreen(id))

  /** */
  def screenByIdObs(id: String): Observable[Screen] =
    for { Some(s) <- niftyObs.map(_.screenByIdOpt(id)) } yield s

  /** */
  def screenControllerByIdObs[T <: ScreenController](id: String): Observable[T] =
    for { Some(s) <- niftyObs.map(_.screenControllerByIdOpt(id)) } yield s
}