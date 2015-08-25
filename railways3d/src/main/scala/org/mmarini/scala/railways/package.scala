/**
 *
 */
package org.mmarini.scala

/**
 * @author us00852
 *
 */
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.Subscription
import rx.lang.scala.Observer
import rx.lang.scala.subscriptions.CompositeSubscription
import com.jme3.app.Application
import com.jme3.input.controls.AnalogListener
import com.jme3.input.controls.ActionListener
import scala.util.Try
import com.jme3.input.controls.Trigger
import com.jme3.scene.Spatial
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.math.Ray
import com.jme3.scene.Node
import com.jme3.collision.CollisionResults
import com.jme3.collision.CollisionResult
import com.jme3.input.InputManager
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import com.typesafe.scalalogging.LazyLogging
import scala.util.Random
import org.slf4j.Marker

/** */
package object railways extends LazyLogging {

  val OrientationAxis = Vector3f.UNIT_Y

  /** Shuffles a sequence */
  def shuffle[T](seq: IndexedSeq[T])(random: Random): IndexedSeq[T] = {
    val n = seq.length
    if (n <= 1) {
      seq
    } else {
      (0 until n - 1).foldLeft(seq)((seq, i) => {
        val j = random.nextInt(n - i) + i
        if (i != j) {
          seq.updated(j, seq(i)).updated(i, seq(j))
        } else {
          seq
        }
      })
    }
  }

  /** Returns an observable of single option selection */
  def singleSelection[T]: Observable[ListBoxSelectionChangedEvent[T]] => Observable[T] = (obs) =>
    for {
      e <- obs
      if (!e.getSelection.isEmpty)
    } yield e.getSelection.get(0)
}
