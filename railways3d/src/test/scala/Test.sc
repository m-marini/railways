
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  object A {
    def unapply(a: String): Option[(String, String)] = if (a.isEmpty()) None else Some("a", "b")
  }
  1                                               //> res0: Int(1) = 1

  A.unapply("aaa")                                //> res1: Option[(String, String)] = Some((a,b))
  "aaa" match {
    case A("a", "b") => println("ok")
    case _ => println("ko")
  }                                               //> ok
  "" match {
    case A("c", "b") => println("ok")
    case _ => println("ko")
  }                                               //> ko
  
	val f = A.unapply(_)                      //> f  : String => Option[(String, String)] = <function1>
  val A(b, c) = "aaff"                            //> b  : String = a
                                                  //| c  : String = b
  
  val A(d, e) = ""                                //> scala.MatchError:  (of class java.lang.String)
                                                  //| 	at Test$$anonfun$main$1.apply$mcV$sp(Test.scala:25)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at Test$.main(Test.scala:9)
                                                  //| 	at Test.main(Test.scala)
  /*
  "abcd" match {
    case Test("a", "b") => println("ok")
  }
*/
}