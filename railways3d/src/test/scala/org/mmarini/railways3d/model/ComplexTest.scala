package org.mmarini.railways3d.model

import org.scalatest.PropSpec
import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import scala.math.sqrt
import scala.math.atan2
import scala.math.Pi
import org.scalacheck.Gen

class ComplexTest extends PropSpec with Matchers with PropertyChecks {

  val Epsilon = 1e-7f

  property("Complex negation should invert re and im") {
    forAll {
      (re: Float, im: Float) =>
        val x = -Complex(re, im)
        x.re should be(-re)
        x.im should be(-im)
    }
  }

  property("Complex conjugate should invert im") {
    forAll {
      (re: Float, im: Float) =>
        val x = Complex(re, im).conj
        x.re should be(re)
        x.im should be(-im)
    }
  }

  property("Complex sqr should be ...") {
    forAll {
      (re: Float, im: Float) =>
        val x = Complex(re, im).sqr
        x should be(re * re + im * im)
    }
  }

  property("Complex length should be ...") {
    forAll {
      (re: Float, im: Float) =>
        val x = Complex(re, im).length
        x should be(sqrt(re * re + im * im).toFloat)
    }
  }

  property("Complex toString should be ...") {
    forAll {
      (re: Float, im: Float) =>
        val x = Complex(re, im).toString
        if (im > 0f)
          x should be(re + " +" + im + " i")
        else if (im < 0)
          x should be(re + " " + im + " i")
        else
          x should be(re.toString)
    }
  }

  property("Complex add should be ...") {
    forAll {
      (re1: Float, im1: Float, re2: Float, im2: Float) =>
        val x = Complex(re1, im1) + Complex(re2, im2)
        x.re should be(re1 + re2)
        x.im should be(im1 + im2)
    }
  }

  property("Complex sub should be ...") {
    forAll {
      (re1: Float, im1: Float, re2: Float, im2: Float) =>
        val x = Complex(re1, im1) - Complex(re2, im2)
        x.re should be(re1 - re2)
        x.im should be(im1 - im2)
    }
  }

  property("Complex mul should be ...") {
    forAll {
      (re1: Float, im1: Float, re2: Float, im2: Float) =>
        whenever(!(re1 * re2 - im1 * im2).isNaN &&
          !(re1 * im2 + re2 * im1).isNaN) {
          val x = Complex(re1, im1) * Complex(re2, im2)
          x.re should be(re1 * re2 - im1 * im2)
          x.im should be(re1 * im2 + re2 * im1)
        }
    }
  }

  property("Complex phase should be ...") {
    forAll {
      (re: Float, im: Float) =>
        val x = Complex(re, im).phase
        x should be(atan2(im, re).toFloat)
    }
  }

  property("Complex versor should be same phase") {
    val phaseGen = Gen.choose[Float](-Pif, Pif)
    forAll(phaseGen) {
      (phase: Float) =>
        val x = Complex.versor(phase)
        x.phase should be(phase +- Epsilon)
        x.sqr should be(1f +- Epsilon)
    }
  }

  property("phase of positive real should be 0") {
    forAll {
      (re: Float) =>
        whenever(re > 0f) {
          val x = Complex(re).phase
          x should be(0f)
        }
    }
  }

  property("phase of negative real should be Pi") {
    forAll {
      (re: Float) =>
        whenever(re < 0f) {
          val x = Complex(re).phase
          x should be(Pif)
        }
    }
  }

  property("phase of positive immaginary should be Pi/2") {
    forAll {
      (im: Float) =>
        whenever(im > 0f) {
          val x = Complex(0f, im).phase
          x should be(Pif / 2)
        }
    }
  }

  property("phase of negative immaginary should be -Pi/2") {
    forAll {
      (im: Float) =>
        whenever(im < 0f) {
          val x = Complex(0f, im).phase
          x should be(-Pif / 2)
        }
    }
  }

}