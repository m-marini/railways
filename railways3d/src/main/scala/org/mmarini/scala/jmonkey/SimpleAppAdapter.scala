package org.mmarini.scala.jmonkey

import com.jme3.app.SimpleApplication
import de.lessvoid.nifty.Nifty
import com.jme3.niftygui.NiftyJmeDisplay
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventAnnotationProcessor.Subscriber
import rx.lang.scala.Observable
import rx.lang.scala.Subscription

/**
 * @author us00852
 */
class SimpleAppAdapter extends SimpleApplication {
  var niftyDisplayOpt: Option[NiftyJmeDisplay] = None
  var niftyOpt: Option[Nifty] = None

  val timeObs: Subject[Float] = Subject()

  /** */
  override def simpleInitApp: Unit = {
    val nd = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort)
    niftyDisplayOpt = Option(nd)
    niftyOpt = Option(nd.getNifty)

    // attach the Nifty display to the gui view port as a processor
    guiViewPort.addProcessor(nd)
  }

  override def simpleUpdate(tpf: Float) {
    timeObs.onNext(tpf)
  }

}