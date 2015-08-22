/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ControllerAdapter
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import org.mmarini.scala.jmonkey.ScreenAdapter

import com.typesafe.scalalogging.LazyLogging

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class TabbedController extends ControllerAdapter
    with MousePrimaryClickedObservable
    with ScreenAdapter
    with LazyLogging {

  private val contentTabMap = Map(
    "cameraTab" -> "cameraPanel",
    "trainTab" -> "trainPanel",
    "messageTab" -> "messagesPanel",
    "performanceTab" -> "performancePanel")

  private val subscriptions = changeTabSubOpt

  private def selectTab(tabId: String) {
    // hides or shows tab and content
    if (contentTabMap.contains(tabId)) {
      for {
        (tabId1, contentId) <- contentTabMap
        tabElem <- elementById(tabId1)
        contentElem <- elementById(contentId)
      } if (tabId == tabId1) {
        tabElem.setStyle("panel.selectedTab")
        contentElem.show
      } else {
        tabElem.setStyle("panel.unselectedTab")
        contentElem.hide
      }
    }
  }

  private def changeTabSubOpt =
    mousePrimaryClickedObs.subscribe(event => {
      selectTab(event.getElement.getId)
    })

}
