/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.CheckBox
import de.lessvoid.nifty.controls._
import de.lessvoid.nifty.controls.Slider
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import org.mmarini.scala.jmonkey.SelectionObservable
import org.mmarini.scala.jmonkey.DefaultScreenController

/**
 * @author us00852
 *
 */
class EndGameController extends AbstractAppState
  with DefaultScreenController
  with SelectionObservable
  with LazyLogging
