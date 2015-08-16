/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.DefaultScreenController
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls._
import org.mmarini.scala.jmonkey.ButtonClickedObservable

/**
 * @author us00852
 *
 */
class EndGameController extends AbstractAppState
  with DefaultScreenController
  with ButtonClickedObservable
  with LazyLogging
