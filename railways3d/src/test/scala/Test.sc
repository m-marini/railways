
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

object Test extends LazyLogging {
  val a = Try {
    throw new IllegalArgumentException
  }                                               //> a  : scala.util.Try[Nothing] = Failure(java.lang.IllegalArgumentException)
  val b = Try {
    2
  }                                               //> b  : scala.util.Try[Int] = Success(2)

  val c =for {
    va <- a
    vb <- b
  } yield Seq(va,vb)                              //> c  : scala.util.Try[Seq[Int]] = Failure(java.lang.IllegalArgumentException)
  
}
                                                  