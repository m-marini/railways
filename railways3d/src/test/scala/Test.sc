
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  val a = IndexedSeq(10f, 20f, 30f)               //> a  : IndexedSeq[Float] = Vector(10.0, 20.0, 30.0)
  a.isDefinedAt(-1)                               //> res0: Boolean = false
}