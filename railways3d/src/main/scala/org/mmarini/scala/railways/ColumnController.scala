/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ListBox
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import scala.util.Try
import de.lessvoid.nifty.elements.Element
import com.jme3.math.Vector2f
import org.mmarini.scala.jmonkey.ScreenObservable
import org.mmarini.scala.jmonkey.AbstractScreenController
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import org.mmarini.scala.jmonkey.MousePrimaryReleaseObservable
import org.mmarini.scala.jmonkey.ListBoxSelectionChangedObservable
import rx.lang.scala.Observable
import org.mmarini.scala.railways.model.TrainMessage
import org.mmarini.scala.railways.model.Train
import org.mmarini.scala.railways.model.MovingTrain
import org.mmarini.scala.railways.model.StoppingTrain
import org.mmarini.scala.railways.model.StoppedTrain
import org.mmarini.scala.railways.model.WaitForPassengerTrain
import org.mmarini.scala.railways.model.WaitingForTrackTrain
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.jmonkey.JmeController
import org.mmarini.scala.jmonkey.NiftyUtil
import org.mmarini.scala.jmonkey.ScreenUtil
import collection.JavaConversions._
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.builder.ImageBuilder
import scala.math.min
import scala.math.max
import de.lessvoid.nifty.builder.ElementBuilder

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
trait ColumnController
    extends NiftyUtil
    with ScreenUtil
    with MousePrimaryClickedObservable
    with LazyLogging {

  def selectedIndexObs: Observable[Int] = {
    val idxOptObs = for {
      x <- mousePrimaryClickedObs
    } yield {
      val element = x.getElement
      val idxOpt = for {
        matcher <- """row-(\d*)-.*""".r.findFirstMatchIn(element.getId)
      } yield matcher.group(1).toInt
      idxOpt
    }
    for {
      x <- idxOptObs
      if (!x.isEmpty)
    } yield x.get
  }

  def showColumn[T](elemId: String,
    builder: (T) => ElementBuilder,
    setter: (Element, T) => Unit,
    rows: Seq[T]) {
    for {
      elem <- elementById(elemId)
      nifty <- niftyOpt
      screen <- screenOpt
    } {
      val n = rows.size
      val children = elem.getElements
      val current = children.size
      val removing = max(current - n, 0)
      val changing = min(n, current)

      for { i <- current - removing until current }
        children(i).markForRemoval

      for { (elem, t) <- children zip rows }
        setter(elem, t)

      for { t <- rows.drop(changing) } {
        builder(t).build(nifty, screen, elem)
      }
    }
  }
}
