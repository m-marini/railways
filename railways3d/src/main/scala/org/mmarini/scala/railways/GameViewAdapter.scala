package org.mmarini.scala.railways

import com.jme3.app.SimpleApplication
import org.mmarini.scala.jmonkey.NiftyUtil
import org.mmarini.scala.jmonkey.ScreenAdapter
import de.lessvoid.nifty.screen.ScreenController

/**
 * @author us00852
 */
object GameViewAdapter extends NiftyUtil {

  def niftyOpt = Main.niftyOpt

  def startCtrlOpt = screenControllerById[StartController]("start")
  def optionsCtrlOpt = screenControllerById[OptionsController]("opts-screen")
  def gameCtrlOpt = screenControllerById[GameController]("game")
  def endGameCtrlOpt = screenControllerById[EndGameController]("end-game-screen")
  def cameraCtrlOpt = for { ctrl <- gameCtrlOpt } yield ctrl.controllerById("cameraPanel", classOf[CameraController])
  def trainCtrlOpt = for { ctrl <- gameCtrlOpt } yield ctrl.controllerById("trainPanel", classOf[TrainController])
  def msgCtrlOpt = for { ctrl <- gameCtrlOpt } yield ctrl.controllerById("messagesPanel", classOf[MessageController])
}