
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  val a = Seq(1, 2, 3)                            //> a  : Seq[Int] = List(1, 2, 3)

  a.foldLeft("")((s, t) => s + t)                 //> res0: String = 123
  a.foldRight("")((t, s) => s + t)                //> res1: String = 321
}