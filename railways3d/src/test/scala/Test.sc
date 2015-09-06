
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
  val x = 11                                      //> x  : Int = 11
  
  val bits = for (i <- 0 to 3) yield
  x / (1<<i) % 2                                  //> bits  : scala.collection.immutable.IndexedSeq[Int] = Vector(1, 1, 0, 1)
  
  bits.reverse                                    //> res1: scala.collection.immutable.IndexedSeq[Int] = Vector(1, 0, 1, 1)
}
                                                  