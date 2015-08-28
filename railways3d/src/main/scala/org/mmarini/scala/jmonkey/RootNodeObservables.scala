/**
 *
 */
package org.mmarini.scala.jmonkey

import com.jme3.scene.Node
import rx.lang.scala.Observable
import com.jme3.scene.Spatial
import rx.lang.scala.Subscriber
import rx.lang.scala.Subscription

/**
 * @author us00852
 */
trait RootNodeObservables {
  def getRootNode: Node

  def attachToRootSub(obs: Observable[Spatial]): Subscription = obs.subscribe(
    spatial => getRootNode.attachChild(spatial))

  def detachFromRootSub(obs: Observable[Spatial]): Subscription = obs.subscribe(
    spatial => getRootNode.attachChild(spatial))

  def detachAllFromRootSub(obs: Observable[Any]): Subscription = obs.subscribe(
    _ => getRootNode.detachAllChildren())

}
