
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val delay = 20 milliseconds                     //> delay  : scala.concurrent.duration.FiniteDuration = 20 milliseconds

  def trigger[T](items: List[T], interval: Duration) =
    items.map(x => Observable.just(x).delay(interval)).reduce(_ ++ _)
                                                  //> trigger: [T](items: List[T], interval: scala.concurrent.duration.Duration)rx
                                                  //| .lang.scala.Observable[T]

  def events = trigger(List[Int => Int](
    x => {
      println(s"$x add 1")
      x + 1
    },
    x => {
      println(s"$x sub 2")
      x - 2
    },
    x => {
      println(s"$x mul 2")
      x * 2
    }), delay).doOnNext(x => println(s"events:$x"))
                                                  //> events: => rx.lang.scala.Observable[Int => Int]

  import org.mmarini.railways3d._

  val s2 = fold(0)(events)                        //> s2  : rx.lang.scala.Observable[Int] = rx.lang.scala.JavaConversions$$anon$2@
                                                  //| 2529c051
  s2.toBlocking.toList                            //> events:<function1>
                                                  //| 0 add 1
                                                  //| events:<function1>
                                                  //| 1 sub 2
                                                  //| events:<function1>
                                                  //| -1 mul 2
                                                  //| res0: List[Int] = List(0, 1, -1, -2)
}