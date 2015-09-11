/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.NiftyDecorator
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
import com.jme3.light.DirectionalLight
import com.jme3.light.AmbientLight
import scala.util.Try
import com.jme3.math.ColorRGBA
import org.mmarini.scala.jmonkey.AnalogMapping
import org.mmarini.scala.jmonkey.NiftyObservables
import org.mmarini.scala.jmonkey.InputManagerObservables
import java.util.ResourceBundle
import java.util.MissingResourceException
import com.jme3.app.FlyCamAppState
import com.jme3.material.Material
import com.jme3.scene.Node
import com.jme3.scene.Geometry
import com.sun.java.swing.plaf.motif.resources.motif
import com.jme3.cinematic.MotionPath
import com.jme3.input.ChaseCamera
import com.jme3.scene.Spatial
import scala.math._

/**
 *
 */
object JmeTestApp extends SimpleAppAdapter
    with NiftyObservables
    with InputManagerObservables
    with LazyLogging {
  val Width = 1200
  val Height = 768
  val Period = 3f

  lazy val subjectObs = (for (_ <- niftyObs) yield {
    logger.debug("Create subject")
    assetManager.loadModel("Textures/fgn.blend").clone
  }).share

  subjectObs.subscribe(s => rootNode.attachChild(s))

  lazy val timerTxObs = for (dt <- timeObs) yield { (t0: Float) =>
    {
      val t1 = t0 + dt
      val t1mod = IEEEremainder(t1, Period).toFloat
      t1mod
    }
  }
  lazy val timerObs = timerTxObs.statusFlow(0f)

  lazy val subjectMoveObs = timerObs.withLatest(subjectObs)((t, spatial) => {
    val x = 10 * sin(t * 2 * Pi / Period).toFloat
    val y = 10 * cos(t * 2 * Pi / Period).toFloat
    (spatial, new Vector3f(x, 0, y))
  })
  //  subjectMoveObs.trace("move ")

  subjectMoveObs.subscribe(_ match {
    case (spatial, loc) => spatial.setLocalTranslation(loc)
  })

  subjectObs.subscribe(s => {
    val chaseCam = new ChaseCamera(cam, s, inputManager)
    chaseCam.setSmoothMotion(true)
    chaseCam.setDefaultDistance(15f)
    chaseCam.setTrailingEnabled(true)
    chaseCam.setDragToRotate(true)
    chaseCam.setRotationSpeed(1)
  })

  niftyObs.subscribe(nifty => {

    flyCam.setEnabled(false)
    //    flyCam.setDragToRotate(true)

    cam.setLocation(new Vector3f(0f, 3f, 10f))
    val sunLight = new DirectionalLight
    sunLight.setColor(ColorRGBA.White.mult(1.3f));
    sunLight.setDirection(Vector3f.UNIT_XYZ.negate.normalizeLocal)
    rootNode.addLight(sunLight)

    val sunLight1 = new DirectionalLight
    sunLight1.setColor(ColorRGBA.White.mult(0.8f));
    sunLight1.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal)
    rootNode.addLight(sunLight1)

    val s1 = assetManager.loadModel("Textures/bgn.blend").clone
    rootNode.attachChild(s1)

    //    val mat = assetManager.loadMaterial("Materials/pavement.j3m")
    //
    //    s.setMaterial(mat)

    //    s.asInstanceOf[Node].
    //      getChild(0).asInstanceOf[Node].
    //      getChild(0).asInstanceOf[Geometry].
    //      getMesh.scaleTextureCoordinates(new Vector2f(8f, 300f))

  })

  /** */
  def main(args: Array[String]): Unit = {
    val s = new AppSettings(true)
    //    s.setResolution(Width, Height)
    //    JmeTestApp.setSettings(s)
    JmeTestApp.setShowSettings(false)
    JmeTestApp.start()
  }
}
