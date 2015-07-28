
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject
import org.mmarini.scala.railways._
import scala.util.Random

object Test {
  val s = 1 to 3                                  //> s  : scala.collection.immutable.Range.Inclusive = Range(1, 2, 3)
  try {
    shuffle(s, Random)
  } catch {
    case e: Throwable => e.printStackTrace
  }                                               //> res0: Any = Vector(3, 2, 1)
}