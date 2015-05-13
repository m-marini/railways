package org.mmarini.railways3d.model

import scala.math.sqrt
import scala.math.atan2
import scala.math.sin
import scala.math.cos

/**
 *
 */
case class Complex(re: Float, im: Float = 0) {

  /**
   *
   */
  def unary_- : Complex = Complex(-re, -im)

  /**
   *
   */
  def conj: Complex = Complex(re, -im)

  /**
   *
   */
  def sqr: Float = re * re + im * im

  /**
   *
   */
  def length: Float = sqrt(sqr).toFloat

  /**
   *
   */
  def phase: Float = atan2(im, re).toFloat

  /**
   *
   */
  def +(other: Complex): Complex = Complex(re + other.re, im + other.im)

  /**
   *
   */
  def -(other: Complex): Complex = this + -other

  /**
   *
   */
  def *(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)

  /**
   *
   */
  override def toString: String =
    if (im > 0) s"$re +$im i"
    else if (im < 0) s"$re $im i"
    else re.toString
}

/**
 *
 */
object Complex {
  
  /**
   *
   */
  def versor(phase: Float) = {
    Complex(cos(phase).toFloat, sin(phase).toFloat)
  }
}