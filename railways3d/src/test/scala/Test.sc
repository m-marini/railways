
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

object Test extends LazyLogging {

  val t = Subject[String]()                       //> t  : rx#95.lang#19927.scala#19951.Subject#19958[String#1523803] = rx.lang.sc
                                                  //| ala.subjects.PublishSubject@68b9ec2b

  val tx = for { value <- t } yield (status: String) => value + status
                                                  //> tx  : rx#95.lang#19927.scala#19951.Observable#20207[String#1523803 => String
                                                  //| #300] = rx.lang.scala.JavaConversions$$anon$2@1d51a1a1

  val s0 = ""                                     //> s0  : String#300 = ""
  val s = stateFlow(s0)(tx)                       //> s  : rx#95.lang#19927.scala#19951.Observable#20207[String#300] = rx.lang.sca
                                                  //| la.JavaConversions$$anon$2@677951c9

  val s1 = trigger(t, s, Some(s0))                //> s1  : rx#95.lang#19927.scala#19951.Observable#20207[(String#1523803, String#
                                                  //| 300)] = rx.lang.scala.JavaConversions$$anon$2@1ab95774

  s.subscribe(x => logger.debug(s"s=$x"))         //> res0: rx#95.lang#19927.scala#19951.Subscription#20579 = rx.lang.scala.Subscr
                                                  //| iption$$anon$2@58d3d0d4
  s1.subscribe(x => logger.debug(s"s1=$x"))       //> res1: rx#95.lang#19927.scala#19951.Subscription#20579 = rx.lang.scala.Subscr
                                                  //| iption$$anon$2@949919e

  t.onNext("A")                                   //> 17:24:24.120 [main] DEBUG Test$ - s=A
                                                  //| 17:24:24.138 [main] DEBUG Test$ - s1=(A,A)
  t.onNext("B")                                   //> 17:24:24.138 [main] DEBUG Test$ - s=BA
                                                  //| 17:24:24.139 [main] DEBUG Test$ - s1=(B,BA)
}
                                                  