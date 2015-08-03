
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject
import org.mmarini.scala.railways._
import scala.util.Random
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import com.jme3.math.Vector2f

object Test {

  val a = new SegmentTrack(new Vector2f(10, 10), new Vector2f(20, 20))
                                                  //> a  : org.mmarini.scala.railways.model.tracks.SegmentTrack = SegmentTrack((10
                                                  //| .0, 10.0),(20.0, 20.0))
  val b = new SegmentTrack(new Vector2f(20, 20), new Vector2f(10, 10))
                                                  //> b  : org.mmarini.scala.railways.model.tracks.SegmentTrack = SegmentTrack((20
                                                  //| .0, 20.0),(10.0, 10.0))
  a.backward.get == b                             //> res0: Boolean = true
  a.backward.get.eq(a.backward.get)               //> res1: Boolean = false
}