
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {

  abstract class Autoref(id: String) {
    def other: Autoref
    override def toString: String = id
  }

  case class MyNode(id: String) extends Autoref(id) {
   lazy val other = new Autoref(s"other of $id") {
      def other = MyNode.this
    }
  }

  val a = MyNode("A")                             //> a  : Test.MyNode = A
  a.other                                         //> res0: Test.Autoref{def other: Test.MyNode} = other of A
  a.other.other                                   //> res1: Test.MyNode = A
}