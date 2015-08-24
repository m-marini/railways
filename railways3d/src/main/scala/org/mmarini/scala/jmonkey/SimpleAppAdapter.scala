package org.mmarini.scala.jmonkey

import com.jme3.app.SimpleApplication
import de.lessvoid.nifty.Nifty
import com.jme3.niftygui.NiftyJmeDisplay
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventAnnotationProcessor.Subscriber
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import org.mmarini.scala.railways.ObservableFactory

/**
 * @author us00852
 */
class SimpleAppAdapter extends SimpleApplication {

  private val bindObs = Subject[(NiftyJmeDisplay)]()

  val niftyDisplayObs = ObservableFactory.storeValueObs(bindObs)

  def niftyObs: Observable[Nifty] = for { nd <- niftyDisplayObs } yield nd.getNifty

  val timeObs: Subject[Float] = Subject()

  /** */
  override def simpleInitApp: Unit = {
    val nd = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort)
    guiViewPort.addProcessor(nd)
    bindObs.onNext(nd)
    bindObs.onCompleted()
  }

  override def simpleUpdate(tpf: Float) {
    timeObs.onNext(tpf)
  }
}