
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

val ko=Option(null)                               //> ko  : Option[Null] = None
val t=Option(true)                                //> t  : Option[Boolean] = Some(true)
val f=Option(false)                               //> f  : Option[Boolean] = Some(false)

t.contains(true)                                  //> res0: Boolean = true
f.contains(true)                                  //> res1: Boolean = false
ko.contains(true)                                 //> res2: Boolean = false
t.contains(false)                                 //> res3: Boolean = false
f.contains(false)                                 //> res4: Boolean = true
ko.contains(false)                                //> res5: Boolean = false
}