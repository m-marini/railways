/**
 *
 */
package org.mmarini.scala

/**
 * @author us00852
 */
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.NiftyControl
import com.jme3.math.Vector2f
import com.jme3.math.Ray
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.tools.SizeValue
import de.lessvoid.nifty.elements.render.ElementRenderer
import de.lessvoid.nifty.controls.Controller
import rx.lang.scala.Observable
import com.jme3.collision.CollisionResults
import com.jme3.collision.CollisionResult
import com.jme3.scene.Node
import com.jme3.renderer.Camera

package object jmonkey {

  implicit class NiftyDecorator(nifty: Nifty) {

    /** */
    def screenByIdOpt(id: String): Option[Screen] = Option(nifty.getScreen(id))
    def screenById(id: String): Screen = screenByIdOpt(id).get

    /** */
    def screenControllerByIdOpt[T <: ScreenController](id: String): Option[T] =
      for {
        scr <- screenByIdOpt(id)
        ctrl <- Option(scr.getScreenController().asInstanceOf[T])
      } yield ctrl

    def screenControllerById[T <: ScreenController](id: String): T =
      screenControllerByIdOpt(id).get

    /** Create a popup instance */
    def createPopupByIdOpt(id: String): Option[Element] = Option(nifty.createPopup(id))
    def createPopupById(id: String): Element = Option(nifty.createPopup(id)).get

    /**
     * Subscribes for popup show
     *
     * @param ctx observable with popup element, panelId and location
     */
    def showPopupAt(popup: Element, panelId: String, location: Vector2f) {
      for { pane <- Option(popup.findElementByName(panelId)) } {
        val id = popup.getId()
        pane.setConstraintX(SizeValue.px(location.getX.toInt))
        pane.setConstraintY(SizeValue.px(popup.getHeight - location.getY.toInt))
        nifty.showPopup(nifty.getCurrentScreen, id, null)
      }
    }
  }

  implicit class ScreenDecorator(screen: Screen) {

    /** Returns a nifty control observable*/
    def controlByIdOpt[T <: NiftyControl](id: String, clazz: Class[T]): Option[T] =
      Option(screen.findNiftyControl(id, clazz))

    /** Returns a nifty element observable */
    def elementByIdOpt(id: String): Option[Element] = Option(screen.findElementByName(id))

    /** */
    def redererByIdOpt[T <: ElementRenderer](id: String, clazz: Class[T]): Option[T] =
      elementByIdOpt(id).map(_.getRenderer(clazz))

    /** */
    def controllerByIdOpt[T <: Controller](id: String, clazz: Class[T]): Option[T] =
      elementByIdOpt(id).map(_.getControl(clazz))

  }

  /** Adds functionalities to the jme3 application */
  implicit class PositionMappingObservable(o: Observable[PositionMapping]) {

    /** Returns the observable of pick ray */
    def pickRay(cam: Camera): Observable[(Ray, PositionMapping)] =
      for { pm <- o } yield {
        val mousePos = pm.position
        val pos = cam.getWorldCoordinates(new Vector2f(mousePos), 0f).clone()
        val dir = cam.getWorldCoordinates(new Vector2f(mousePos), 1f).subtractLocal(pos).normalizeLocal()
        (new Ray(pos, dir), pm)
      }
  }

  implicit class RayMappingObservable(o: Observable[(Ray, PositionMapping)]) {

    /** Returns the observable of pick object */
    def pickCollision(shootables: Node): Observable[(CollisionResult, Ray, PositionMapping)] = {
      val collisions = for {
        (ray, pm) <- o
      } yield {
        val results = new CollisionResults()
        shootables.collideWith(ray, results)
        (results, ray, pm)
      }
      for {
        (cr, ray, pm) <- collisions
        if (cr.size() > 0)
      } yield (cr.getClosestCollision, ray, pm)
    }
  }

}
