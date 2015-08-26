package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.MousePrimaryReleaseObservable

import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import com.jme3.scene.Spatial
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import de.lessvoid.nifty.controls.ButtonClickedEvent
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryReleaseEvent
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection

/**
 * Gathers all the elements of game view in terms of controllers, observables and subscriptions
 *
 * It acts as a facade for the access to all components of game view
 *
 * @author us00852
 */
object GameViewAdapter {

  def niftyObs = Main.niftyObs

  // ===================================================================
  // Controllers
  // ===================================================================

  def trainCtrlOpt = None
  //  for {
  //    gc <- gameCtrlOpt
  //    ctrl <- gc.controllerById("trainPanel", classOf[TrainController])
  //  } yield ctrl

  def msgCtrlOpt = None
  //    for {
  //    gc <- gameCtrlOpt
  //    ctrl <- gc.controllerById("messagesPanel", classOf[MessageController])
  //  } yield ctrl

  // ===================================================================
  // Observables
  // ===================================================================
  lazy val startCtrlObs = Main.screenControllerByIdObs[StartController]("start").latest

  lazy val optionsCtrlObs = Main.screenControllerByIdObs[OptionsController]("opts-screen").latest

  lazy val gameCtrlObs = Main.screenControllerByIdObs[GameController]("game-screen").latest

  lazy val endGameCtrlObs = Main.screenControllerByIdObs[EndGameController]("end-game-screen").latest

  lazy val cameraCtrlObs = onFirstFlattenObs(gameCtrlObs)(_.controllerByIdObs("cameraPanel", classOf[CameraController]))

  lazy val startButtonsObs: Observable[ButtonClickedEvent] = onFirstFlattenObs(startCtrlObs)(_.buttonClickedObs)

  lazy val optionsButtonsObs: Observable[ButtonClickedEvent] = onFirstFlattenObs(optionsCtrlObs)(_.buttonClickedObs)

  lazy val endGameButtonsObs: Observable[ButtonClickedEvent] = onFirstFlattenObs(endGameCtrlObs)(_.buttonClickedObs)

  //  def trainPanelObs: Observable[(Int, Int)] = {
  //    val opt = for {
  //      c <- trainCtrlOpt
  //    } yield c.selectionObs
  //    opt.getOrElse(Observable.never)
  //  }

  lazy val cameraPanelObs: Observable[(Int, Int)] = onFirstFlattenObs(cameraCtrlObs)(_.selectionObsOpt)

  lazy val gameScreenObs: Observable[(String, ScreenControllerAdapter)] = onFirstFlattenObs(gameCtrlObs)(_.screenEventObs)

  def xRelativeAxisObs = Main.mouseRelativeObs("xAxis")

  lazy val gameMouseClickedObs: Observable[NiftyMousePrimaryClickedEvent] = onFirstFlattenObs(gameCtrlObs)(_.mousePrimaryClickedObs)

  lazy val gameMouseReleasedObs: Observable[NiftyMousePrimaryReleaseEvent] = onFirstFlattenObs(gameCtrlObs)(_.mousePrimaryReleaseObs)

  val cameraNodeObs = onFirstObs(niftyObs)(_ => {
    val camNode = new CameraNode("Motion cam", Main.getCamera)
    camNode.setControlDir(ControlDirection.SpatialToCamera)
    camNode.setEnabled(true)
    camNode
  })

  def timeObs: Observable[Float] = Main.timeObs

  def selectActionObs = Main.actionObservable("select")
  def selectMidActionObs = Main.actionObservable("selectMid")
  def selectRightActionObs = Main.actionObservable("selectRight")
  def upCommandActionObs = Main.actionObservable("upCmd")
  def downCommandActionObs = Main.actionObservable("downCmd")
  def leftCommandActionObs = Main.actionObservable("leftCmd")
  def rightCommandActionObs = Main.actionObservable("rightCmd")

  def xAxisAnalogObs = Main.analogObservable("xAxis")
  def forwardAnalogObs = Main.analogObservable("forwardCmd")
  def backwardAnalogObs = Main.analogObservable("backwardCmd")

  // ===================================================================
  // Subscriptions
  // ===================================================================

  def attachToRootSub: Observable[Spatial] => Subscription = Main.attachToRootSub _

  def detachFromRootSub: Observable[Spatial] => Subscription = Main.detachFromRootSub _

  def detachAllFromRootSub: Observable[Any] => Subscription = Main.detachAllFromRootSub _

  def quitSub(obs: Observable[Any]): Subscription = obs.subscribe(_ => { Main.stop })

  //  def endGamePerfSub(obs: Observable[GamePerformance]): Subscription = obs.subscribe(
  //    performance => for (c <- endGameCtrlOpt) yield c.show(performance))
  //
  //  def performancePanelSub(obs: Observable[GamePerformance]): Subscription = obs.subscribe(
  //    performance => for { c <- gameCtrlOpt } yield c.show(performance))
  //
  //  def msgsPanelSub(obs: Observable[Iterable[String]]): Subscription = obs.subscribe(
  //    msgs => for { c <- msgCtrlOpt } yield c.show(msgs))
  //
  //  def cameraPanelSub(obs: Observable[Seq[String]]): Subscription = obs.subscribe()
  //  //    names => for { c <- cameraCtrlOpt } c.show(names))
  //
  //  def trainPanelSub(obs: Observable[IndexedSeq[IndexedSeq[String]]]): Subscription = obs.subscribe(
  //    cells => for { c <- trainCtrlOpt } c.setCells(cells))

  def gotoScreen = Main.gotoScreen _
}