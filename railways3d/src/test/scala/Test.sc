
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {

  abstract class Autoref(id: String) {
    def unapply: Option[(String)] = Some((id))
    def other: Autoref
    override def toString: String = id
  }

  case class MyNode(id: String) extends Autoref(id) {
    lazy val other = new Autoref(s"other of $id") {
      def other = MyNode.this
    }
  }

  val a = MyNode("A")
  a.other
  a.other.other
  a.other match {
    case Autoref(x) => x.id
    case _ => "bho"
  }
}