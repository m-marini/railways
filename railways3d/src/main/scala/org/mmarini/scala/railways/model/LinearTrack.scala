/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * Describes a linear part of trajectory
 *
 * @constructor create the segment specifying the starting point and the ending point
 *
 */
case class LinearTrack(begin: Vector2f, end: Vector2f) extends Track
