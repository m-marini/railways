/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ControllerAdapter
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import org.mmarini.scala.jmonkey.ScreenObservables
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Subscription

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class TabbedController extends ControllerAdapter
    with MousePrimaryClickedObservable
    with ScreenObservables
    with LazyLogging {

  private val contentTabMap = Map(
    "cameraTab" -> "cameraPanel",
    "trainTab" -> "trainPanel",
    "messageTab" -> "messagesPanel",
    "performanceTab" -> "performancePanel")

  private val subscriptions = mousePrimaryClickedObs.subscribe(event => {
    selectTab(event.getElement.getId)
  })

  private def selectTab(tabId: String) {
    // hides or shows tab and content
    if (contentTabMap.contains(tabId)) {
      for {
        (tabId1, contentId) <- contentTabMap
      } {
        (elementByIdObs(tabId1) combineLatest elementByIdObs(contentId)).subscribe(_ match {
          case (tabElem, contentElem) if (tabId == tabId1) =>
            tabElem.setStyle("panel.selectedTab")
            contentElem.show
          case (tabElem, contentElem) =>
            tabElem.setStyle("panel.unselectedTab")
            contentElem.hide
        })
      }
    }
  }
}
