
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
  """row-(\d*)-.*""".r.findFirstMatchIn("rowa-113-aaa").get.group(1)
                                                  //> java.util.NoSuchElementException: None.get
                                                  //| 	at scala.None$.get(Option.scala:347)
                                                  //| 	at scala.None$.get(Option.scala:345)
                                                  //| 	at Test$$anonfun$main$1.apply$mcV$sp(Test.scala:16)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at Test$.main(Test.scala:15)
                                                  //| 	at Test.main(Test.scala)

}
                                                  