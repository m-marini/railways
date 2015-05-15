
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val delay = 2000 milliseconds                   //> delay  : scala.concurrent.duration.FiniteDuration = 2000 milliseconds

  val events = (Observable.just("inc").delay(delay) ++
    Observable.just("dec").delay(delay)).doOnNext(x => println(s"events:$x"))
                                                  //> events  : rx.lang.scala.Observable[String] = rx.lang.scala.JavaConversions$$
                                                  //| anon$2@3456337e

  val f = (e: (String, Int)) => e match {
    case ("inc", s) => s + 1
    case ("dec", s) => s - 1
  }                                               //> f  : ((String, Int)) => Int = <function1>

  import org.mmarini.railways3d._
  val s2 = fold[Int, String](f)(0)_               //> s2  : rx.lang.scala.Observable[String] => rx.lang.scala.Observable[Int] = <f
                                                  //| unction1>

  val s3 = s2(events)                             //> s3  : rx.lang.scala.Observable[Int] = rx.lang.scala.subjects.PublishSubject@
                                                  //| 6623a0d3
  s3.
    toBlocking.toList                             //> events:inc
                                                  //| events:dec
                                                  //| res0: List[Int] = List(1, 0)
}