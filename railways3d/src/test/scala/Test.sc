
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscriber
import rx.lang.scala.Subscription
import scala.concurrent.duration._
import rx.lang.scala.Subject
import org.mmarini.scala.railways._
import scala.util.Random
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import com.jme3.math.Vector2f
import com.typesafe.scalalogging.LazyLogging
import scala.util.Try
import com.sun.activation.registries.LogSupport

object Test extends LazyLogging {
  "Start"                                         //> res0: String("Start") = Start

  def logSub[T](o: Observable[T]) = o.subscribe(
    x => logger.debug(x.toString),
    e => logger.error(e.getMessage, e),
    () => logger.debug("onComplete"))             //> logSub: [T](o: rx.lang.scala.Observable[T])rx.lang.scala.Subscription


  val a = Observable.create[Observable[Int]](o => {
    for (i <- 1 to 3) { o.onNext(Observable.just(i)) }
    o.onCompleted()
    Subscription()
  })                                              //> a  : rx.lang.scala.Observable[rx.lang.scala.Observable[Int]] = rx.lang.scala
                                                  //| .JavaConversions$$anon$2@223017cd

val b = a.flatten                                 //> b  : rx.lang.scala.Observable[Int] = rx.lang.scala.JavaConversions$$anon$2@6
                                                  //| 41f89e1
  logSub(b)                                       //> 12:27:13.494 [main] DEBUG Test$ - 1
                                                  //| 12:27:13.497 [main] DEBUG Test$ - 2
                                                  //| 12:27:13.497 [main] DEBUG Test$ - 3
                                                  //| 12:27:13.497 [main] DEBUG Test$ - onComplete
                                                  //| res1: rx.lang.scala.Subscription = rx.lang.scala.Subscription$$anon$2@37e67d
                                                  //| 34
}
                                                  