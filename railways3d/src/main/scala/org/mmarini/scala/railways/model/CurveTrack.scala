/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * Describes a curve part of trajectory
 *
 * @constructor create the curve specifying the center,
 *             the radius, the starting angle and the ending angle
 *
 */
case class CurveTrack(center: Vector2f, radius: Float, begin: Float, end: Float) extends Track
