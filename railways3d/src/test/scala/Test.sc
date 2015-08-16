
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

  def hot[S,T](init: T)(f: Observable[T => S]): Observable[S] = {
    val subj = Subject[S]
    var acc: T = init
    var count = 0
    var sub: Option[Subscription] = None

    val r = Observable.create[T] { x =>
      {
        count = count + 1
        if (count == 1) {
          sub = Some(f.subscribe((y: T => T) => {
            acc = y(acc)
            logger.debug(s"process ${acc}")
            subj.onNext(acc)
          }))
        }
        logger.debug(s"subscribe $count")
        Subscription {
          count = count - 1
          if (count == 0)
            for (s <- sub) {
              logger.debug(s"unsubscribe tx")
              s.unsubscribe
            }
          logger.debug(s"unsubscribe")
        }
      }
    }
    r
  }


  def stateFlow[T](init: T)(f: Observable[T => T]): Observable[T] = {
    val subj = Subject[T]
    var acc: T = init
    var count = 0
    var sub: Option[Subscription] = None

    val r = Observable.create[T] { x =>
      {
        count = count + 1
        if (count == 1) {
          sub = Some(f.subscribe((y: T => T) => {
            acc = y(acc)
            logger.debug(s"process ${acc}")
            subj.onNext(acc)
          }))
        }
        logger.debug(s"subscribe $count")
        Subscription {
          count = count - 1
          if (count == 0)
            for (s <- sub) {
              logger.debug(s"unsubscribe tx")
              s.unsubscribe
            }
          logger.debug(s"unsubscribe")
        }
      }
    }
    r
  }

  val tx = Subject[Int => Int]()

  val s = stateFlow(0)(tx)

  val sub1 = s.subscribe(x => logger.debug(s"subscription 1 = ${x}"))

  tx.onNext(_ + 1)

  tx.onNext(_ * 2)

  val sub2 = s.subscribe(x => logger.debug(s"subscription 2 = ${x}"))

  sub1.unsubscribe()
  sub1.unsubscribe()

  tx.onNext(_ * 4)

  sub2.unsubscribe()

  tx.onNext(_ * 5)
}
                                                  