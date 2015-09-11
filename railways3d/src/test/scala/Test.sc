
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

  val y = Observable.just(1).delay(Duration("100 millis"))
                                                  //> y  : rx.lang.scala.Observable[Int] = rx.lang.scala.JavaConversions$$anon$2@3
                                                  //| ea4e3ae
  
  val x = y.traced("y").share                     //> x  : rx.lang.scala.Observable[Int] = rx.lang.scala.JavaConversions$$anon$2@1
                                                  //| e7bf4ff
  x.trace("x1")                                   //> 21:45:10.321 [main] DEBUG o.m.s.r.package$ObservableFactory - f4ff.1.f521 x1
                                                  //|  subscribe
                                                  //| 21:45:10.329 [main] DEBUG o.m.s.r.package$ObservableFactory - e3ae.1.3ece y 
                                                  //| subscribe
 	x.trace("x2")                             //> 21:45:10.334 [main] DEBUG o.m.s.r.package$ObservableFactory - f4ff.1.cb8c x2
                                                  //|  subscribe
 	
 	
  Thread.sleep(500)                               //> 21:45:10.440 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - e3ae.1.3ece y onNext 1
                                                  //| 21:45:10.443 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.f521 x1 onNext 1
                                                  //| 21:45:10.445 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.cb8c x2 onNext 1
                                                  //| 21:45:10.446 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - e3ae.1.3ece yon Completed
                                                  //| 21:45:10.447 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.f521 x1on Completed
                                                  //| 21:45:10.449 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.f521 x1 unsubscribe
                                                  //| 21:45:10.451 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.cb8c x2on Completed
                                                  //| 21:45:10.452 [RxComputationThreadPool-3] DEBUG o.m.s.r.package$ObservableFac
                                                  //| tory - f4ff.1.cb8c x2 unsubscribe
                                                  //| 21:45:10.453 [RxComputationThreadPool-3] DEBUG o.m.s.r.
                                                  //| Output exceeds cutoff limit.
}
                                                  