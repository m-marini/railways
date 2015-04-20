/**
 *
 */
package org.mmarini.railways3d

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import org.bushe.swing.event.EventTopicSubscriber
import de.lessvoid.nifty.NiftyEvent

/**
 *
 */
object JME3Utils {

  /**
   *
   */
  def subscribe[T](nifty: Nifty, screen: Screen, elementId: String, eventClass: Class[T], f: (String, T) => Unit): EventTopicSubscriber[T] = {
    val s = new EventTopicSubscriber[T] {
      def onEvent(id: String, t: T) {
        f(id, t)
      }
    }
    nifty.subscribe(screen, elementId, eventClass, s)
    s
  }

  /**
   *
   */
  def subscribeById[T](nifty: Nifty, screenId: String, elementId: String, eventClass: Class[T], f: (String, T) => Unit): EventTopicSubscriber[T] =
    subscribe(nifty, nifty.getScreen(screenId), elementId, eventClass, f)

  /**
   *
   */
  def unsubscribe(nifty: Nifty, elementId: String, subscriber: EventTopicSubscriber[_]): JME3Utils.type = {
    nifty.unsubscribe(elementId, subscriber)
    this
  }

  /**
   *
   */
  def publish(nifty: Nifty, id: String, event: NiftyEvent[_]): JME3Utils.type = {
    nifty.publishEvent(id, event)
    this
  }
}
