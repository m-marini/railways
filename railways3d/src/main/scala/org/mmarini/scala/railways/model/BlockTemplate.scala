/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** Produces trajectory depending on configuration of block */
trait BlockTemplate {
}

/** The incoming trains come in this BlockTemplate */
case object Entry extends BlockTemplate {
}

/** The exiting trains go out this BlockTemplate */
case object Exit extends BlockTemplate {
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object Segment extends BlockTemplate {
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object Platform extends BlockTemplate {
}

/** */
case object LeftHandSwitch extends BlockTemplate {
}

/** */
case object RightHandSwitch extends BlockTemplate {
}
