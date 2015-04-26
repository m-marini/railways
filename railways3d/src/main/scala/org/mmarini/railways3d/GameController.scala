/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.state.AbstractAppState
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.railways3d.model.GameParameters
import scala.collection.parallel.mutable.ParMap
import org.mmarini.railways3d.model.GameParameters
import de.lessvoid.nifty.controls.ButtonClickedEvent
import com.typesafe.scalalogging.LazyLogging
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.scene.shape.Box
import com.jme3.math.ColorRGBA
import com.jme3.asset.AssetManager
import com.jme3.app.SimpleApplication

/**
 * @author us00852
 *
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {
  private var screen: Option[Screen] = None
  private var handler: Option[GameHandler] = None

  /**
   *
   */
  override def bind(nifty: Nifty, screen: Screen) {
    logger.debug("bind ...")
    super.bind(nifty, screen)
    this.screen = Some(screen)
  }

  /**
   *
   */
  def startGame(app: SimpleApplication, handler: GameHandler) {
    this.handler = Some(handler)
    val player = new Geometry("Player", new Box(1, 1, 1))

    val mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", ColorRGBA.Blue)

    player.setMaterial(mat)

    app.getRootNode.attachChild(player)
  }
}
