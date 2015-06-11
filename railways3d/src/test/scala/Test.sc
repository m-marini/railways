
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
val a=Seq("a","b","c")                            //> a  : Seq[String] = List(a, b, c)
a(0)                                              //> res0: String = a
if (a.isDefinedAt(11)) a(11) else "aaa"           //> res1: String = aaa


}