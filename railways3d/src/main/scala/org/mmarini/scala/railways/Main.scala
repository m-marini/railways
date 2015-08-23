/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ActionMapping
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.NiftyObservables
import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.system.AppSettings
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import com.jme3.input.controls.JoyAxisTrigger
import com.jme3.input.JoyInput
import rx.lang.scala.Subscriber
import rx.lang.scala.subscriptions.CompositeSubscription
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.scala.jmonkey.SimpleAppAdapter
import org.mmarini.scala.jmonkey.InputManagerObservables
import org.mmarini.scala.jmonkey.RootNodeObservables
import com.jme3.light.DirectionalLight
import com.jme3.light.AmbientLight
import scala.util.Try
import com.jme3.math.ColorRGBA

/**
 *
 */
object Main extends SimpleAppAdapter
    with NiftyObservables
    with RootNodeObservables
    with InputManagerObservables
    with LazyLogging {
  val Width = 1200
  val Height = 768

  def mouseRelativeObs: String => Observable[AnalogMapping] = (k) =>
    for { e <- analogObservable(k) } yield {
      val p = new Vector2f(e.position.getX / Width, e.position.getY / Height).multLocal(2).subtractLocal(1f, 1f)
      AnalogMapping(e.name, e.value, p, e.tpf)
    }

  niftyObs.subscribe(nifty => {
    setDisplayStatView(false)
    setDisplayFps(false)
    flyCam.setEnabled(false)

    // Read your XML and initialize your custom ScreenController
    try {
      nifty.fromXml("Interface/start.xml", "start")
      nifty.addXml("Interface/opts.xml")
      nifty.addXml("Interface/game.xml")
      nifty.addXml("Interface/endGame.xml")
    } catch {
      case ex: Exception => logger.error(ex.getMessage, ex)
    }

    attachMapping

    val lt = lightsTry
    for { e <- lt.failed } logger.error(e.getMessage, e)
    for {
      lightSeq <- lt
      light <- lightSeq
    } getRootNode.addLight(light)
  })

  val sub1 = GameReactiveFlows.gameFlowSub
  val sub2 = ScreenNavigation.gotoScreenSub

  /** Creates ambient light */
  private def ambientLightTry = Try {
    val ambLight = new AmbientLight
    ambLight.setColor(ColorRGBA.White.mult(1.3f))
    ambLight
  }

  /** Creates sun light */
  private def sunLightTry = Try {
    val sunLight = new DirectionalLight
    sunLight.setColor(ColorRGBA.White.mult(1.3f));
    sunLight.setDirection(Vector3f.UNIT_XYZ.negate().normalizeLocal())

    val sunLight1 = new DirectionalLight
    sunLight1.setColor(ColorRGBA.White.mult(1.3f));
    sunLight1.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal())

    Seq(sunLight, sunLight1)
  }

  /** Create lights */
  private def lightsTry = for {
    a <- ambientLightTry
    sl <- sunLightTry
  } yield sl :+ a

  /** Attaches mapping */
  private def attachMapping {
    inputManager.addMapping("selectRight", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("selectMid", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE))
    inputManager.addMapping("upCmd", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W))
    inputManager.addMapping("leftCmd", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("rightCmd", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("downCmd", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S))

    inputManager.addMapping("xAxis",
      new MouseAxisTrigger(MouseInput.AXIS_X, false),
      new MouseAxisTrigger(MouseInput.AXIS_X, true))
    inputManager.addMapping("forwardCmd",
      new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false))
    inputManager.addMapping("backwardCmd",
      new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true))
  }

  /** */
  def main(args: Array[String]): Unit = {
    val s = new AppSettings(true)
    s.setResolution(Width, Height)
    Main.setSettings(s)
    Main.setShowSettings(false)
    Main.start()
  }
}
