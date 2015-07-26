
import rx.lang.scala.Observable
import scala.concurrent.duration._
import rx.lang.scala.Subject

object Test {
  class A()

  1                                               //> res0: Int(1) = 1
  new A()                                         //> res1: Test.A = Test$A@4a15d117
  new A() == new A()                              //> res2: Boolean = false
  val a = new A()                                 //> a  : Test.A = Test$A@5e526300
  val b = new A()                                 //> b  : Test.A = Test$A@72e1c560
  a == b                                          //> res3: Boolean = false
  a == a                                          //> res4: Boolean = true
  b == a                                          //> res5: Boolean = false
  a.eq(b)                                         //> res6: Boolean = false
  a.eq(a)                                         //> res7: Boolean = true
  b.eq(a)                                         //> res8: Boolean = false
  a.equals(b)                                     //> res9: Boolean = false
  a.equals(a)                                     //> res10: Boolean = true
  b.equals(a)                                     //> res11: Boolean = false
}