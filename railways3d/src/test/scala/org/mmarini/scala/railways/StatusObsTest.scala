/**
 *
 */
package org.mmarini.scala.railways

import org.mockito.Mockito._
import org.mockito.Mockito.inOrder
import org.scalatest.Matchers
import org.scalatest.PropSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.PropertyChecks

import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import rx.lang.scala.Subscription

/** Test */
class StatusObsTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  trait Mocker {
    def onNext(x: Int): Unit
    def onError(x: Throwable): Unit
    def onCompleted: Unit
  }
  property("status should do something") {

    val trans = Subject[Int => Int]

    val a = trans.statusFlow(0)

    val f = mock[Mocker]
    val obsr1 = Observer(f.onNext, f.onError, f.onCompleted _)

    a.subscribe(obsr1)

    trans.onNext(x => x)
    trans.onNext(x => x + 1)

    val g = mock[Mocker]
    val obsr2 = Observer(g.onNext)

    a.subscribe(obsr2)

    trans.onNext(x => x + 1)

    verify(f).onNext(0)
    verify(f).onNext(1)
    verify(f).onNext(2)
    verifyNoMoreInteractions(f)

    verify(g).onNext(2)
    verifyNoMoreInteractions(g)

  }

  property("status should do something1") {

    val trans = Subject[Int => Int]
    val init = Subject[Int]

    val a = trans.statusFlowWithInitObs(init)

    val f = mock[Observer[Int]]

    a.subscribe(Observer(f.onNext, f.onError, f.onCompleted _))

    trans.onNext(x => x + 1)

    val g = mock[Mocker]

    a.subscribe(Observer(g.onNext, g.onError, g.onCompleted _))

    init.onNext(0)
    init.onCompleted

    trans.onNext(x => x + 1)
    trans.onCompleted

    val h = mock[Mocker]

    a.subscribe(Observer(h.onNext, h.onError, h.onCompleted _))

    verify(f).onNext(1)
    verify(f).onCompleted
    verifyNoMoreInteractions(f)

    verify(g).onNext(1)
    verify(g).onCompleted
    verifyNoMoreInteractions(g)

    verify(h).onCompleted
    verifyNoMoreInteractions(h)

  }

  property("status should do something2") {

    val trans = Subject[Int => Int]
    val init = Subject[Int]

    val a = trans.statusFlowWithInitObs(init)

    val f = mock[Observer[Int]]

    a.subscribe(Observer(f.onNext, f.onError, f.onCompleted _))

    init.onNext(0)

    trans.onNext(x => x + 1)
    trans.onNext(x => x + 1)

    init.onNext(10)
    init.onCompleted

    trans.onNext(x => x + 1)
    trans.onNext(x => x + 1)

    trans.onCompleted

    verify(f).onNext(1)
    verify(f).onNext(2)
    verify(f).onNext(3)
    verify(f).onNext(4)
    verify(f).onNext(11)
    verify(f).onNext(12)
    verify(f).onCompleted

    verifyNoMoreInteractions(f)
  }

  property("status should do something3") {

    val trans = Subject[Int => Int]
    val init = Subject[Int]

    val a = trans.statusFlowWithInitObs(init, _ == 2)

    val f = mock[Observer[Int]]

    a.subscribe(Observer(f.onNext, f.onError, f.onCompleted _))

    init.onNext(0)

    trans.onNext(x => x + 1)
    trans.onNext(x => x + 1)

    init.onNext(10)
    init.onCompleted

    trans.onNext(x => x + 1)
    trans.onNext(x => x + 1)

    trans.onCompleted

    verify(f).onNext(1)
    verify(f).onNext(2)
    verify(f).onNext(11)
    verify(f).onNext(12)
    verify(f).onCompleted

    verifyNoMoreInteractions(f)
  }
}
