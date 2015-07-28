/**
 *
 */
package org.mmarini.scala.railways

import org.scalatest.PropSpec
import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.PropertyChecks
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.inOrder
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.concurrent.Promise
import rx.lang.scala.Subject
import rx.lang.scala.Subscription

/** Test */
class StatusObsTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  trait Mocker {
    def onNext(x: Int): Unit
    def onComplete: Unit
  }
  property("status should do something") {

    val trans = Subject[Int => Int]

    val a = stateFlow(0)(trans)

    val f = mock[Mocker]
    val obsr1 = Observer(f.onNext)

    a.subscribe(obsr1)

    trans.onNext(x => x)
    trans.onNext(x => x + 1)

    val g = mock[Mocker]
    val obsr2 = Observer(g.onNext)

    a.subscribe(obsr2)
    
    import org.mockito.Mockito._
    trans.onNext(x => x + 1)

    verify(f).onNext(0)
    verify(f).onNext(1)
    verify(f).onNext(2)
    verifyNoMoreInteractions(f)

    verify(g).onNext(2)
    verifyNoMoreInteractions(g)

  }
}