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

  def startCtrlObs = Main.screenControllerByIdObs[StartController]("start")

  def optionsCtrlObs = Main.screenControllerByIdObs[OptionsController]("opts-screen")

  def gameCtrlObs = Main.screenControllerByIdObs[GameController]("game-screen")

  def endGameCtrlObs = Main.screenControllerByIdObs[EndGameController]("end-game-screen")

  def cameraCtrlObs = {
    val opt = for { c <- gameCtrlObs } yield c.controllerByIdObs("cameraPanel", classOf[CameraController])
    opt.flatten
  }

  def startButtonsObs: Observable[ButtonClickedEvent] =
    for {
      c <- startCtrlObs
      ev <- c.buttonClickedObs
    } yield ev

  def optionsButtonsObs: Observable[ButtonClickedEvent] =
    for {
      c <- optionsCtrlObs
      ev <- c.buttonClickedObs
    } yield ev

  def endGameButtonsObs: Observable[ButtonClickedEvent] =
    for {
      c <- endGameCtrlObs
      ev <- c.buttonClickedObs
    } yield ev

  //  def trainPanelObs: Observable[(Int, Int)] = {
  //    val opt = for {
  //      c <- trainCtrlOpt
  //    } yield c.selectionObs
  //    opt.getOrElse(Observable.never)
  //  }

  def cameraPanelObs: Observable[(Int, Int)] = {
    val opt = for {
      c <- cameraCtrlObs
    } yield c.selectionObsOpt
    opt.flatten
  }

  def gameScreenObs: Observable[(String, ScreenControllerAdapter)] = {
    val opt = for { c <- gameCtrlObs } yield c.screenEventObs
    opt.flatten
  }

  def xRelativeAxisObs = Main.mouseRelativeObs("xAxis")

  def gameMouseClickedObs: Observable[NiftyMousePrimaryClickedEvent] = {
    val opt = for { c <- gameCtrlObs } yield c.mousePrimaryClickedObs
    opt.flatten
  }

  def gameMouseReleasedObs: Observable[NiftyMousePrimaryReleaseEvent] = {
    val opt = for { c <- gameCtrlObs } yield c.mousePrimaryReleaseObs
    opt.flatten
  }

  val cameraNodeObs = {
    val camNodeObs = for { _ <- niftyObs } yield {
      val camNode = new CameraNode("Motion cam", Main.getCamera)
      camNode.setControlDir(ControlDirection.SpatialToCamera)
      camNode.setEnabled(true)
      camNode
    }
    camNodeObs.first.cache(1)
  }

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