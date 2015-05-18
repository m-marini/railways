/**
 *
 */
package org.mmarini.railways3d

import org.mmarini.railways3d.model.GameParameters
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observer
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.Vector3f

/**
 * @author us00852
 *
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {
  val gameStarterObserver = Observer((o: (Main.type, GameParameters)) => {
    o match {
      case (app, parms) =>
        val game = new Game(app, parms)
    }
  })
}
