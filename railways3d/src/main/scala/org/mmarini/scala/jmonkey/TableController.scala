/**
 *
 */
package org.mmarini.scala.jmonkey

import rx.lang.scala.Observable
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.builder.ElementBuilder
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.builder.PanelBuilder
import collection.JavaConversions._
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.elements.render.ImageRenderer
import rx.lang.scala.Subscription

/**
 * table-panel,center
 *   col-panel,horizontal
 *     cell-panel,vertical
 *       text/image
 *
 * @author us00852
 */
object TableController {

  val DefaultColumnElementStyle = "panel.table-column"
  val DefaultCellStyle = "nifty-label"

}

class TableController extends ControllerAdapter
    with MousePrimaryClickedObservable
    with NiftyObservables
    with ScreenObservables {

  import TableController._

  /** */
  def headerOpt: Option[Int => String] = None

  /** */
  def headerStyle: Int => String = _ => DefaultCellStyle

  /** */
  def columnStyle: Int => String = _ => DefaultColumnElementStyle

  /** */
  def cellStyle: (Int, Int) => String = (_, _) => DefaultCellStyle

  def colCount: IndexedSeq[IndexedSeq[String]] => Int = cells => if (cells.isEmpty) 0 else cells(0).size

  def builder: (Int) => ElementBuilder = _ => new TextBuilder

  def setter: (Int, Int) => (Element, String) => Unit = (_, _) => textSetter

  val dummySetter: (Element, String) => Unit = (_, _) => {}

  val textSetter: (Element, String) => Unit = (elem, text) => elem.getRenderer(classOf[TextRenderer]).setText(text)

  val imageSetter: (Element, String) => Unit = (elem, text) =>
    for {
      nifty <- niftyObs
      screen <- screenObs
    } {
      elem.getRenderer(classOf[ImageRenderer]).
        setImage(nifty.getRenderEngine.createImage(screen, text, false))
    }

  private var colElements = IndexedSeq[Element]()

  /** Resizes the columns of table */
  private def resizeColumns(m: Int,
    nifty: Nifty,
    screen: Screen,
    parent: Element): IndexedSeq[Element] = {
    val m0 = colElements.size
    if (m0 == m) {
      colElements
    } else if (m0 > m) {
      // Removes old columns
      for { col <- colElements.drop(m) } {
        col.markForRemoval
      }
      colElements.take(m)
    } else {
      // Add new colums
      val b = new PanelBuilder
      for (idx <- m0 until m) yield {
        val style = columnStyle(idx)
        b.style(style)
        b.build(nifty, screen, parent)
      }
    }
  }

  /** Resizes the rows of table */
  private def resizeRows(colElements1: IndexedSeq[Element],
    n: Int,
    nifty: Nifty,
    screen: Screen,
    parent: Element): IndexedSeq[Element] = {
    val headerSize = headerOpt.size
    val hb = new TextBuilder
    for {
      (colElement, colIdx) <- colElements1 zipWithIndex
    } {
      val cellElems = colElement.getElements
      for {
        header <- headerOpt
        if (cellElems.isEmpty())
      } {
        // Create header
        val head = header(colIdx)
        val style = headerStyle(colIdx)
        hb.style(style)
        hb.text(head)
        hb.build(nifty, screen, colElement)
      }

      val pid = Option(parent.getId).getOrElse(parent.hashCode().toString())
      val n0 = cellElems.size
      val nn = n + headerSize
      if (n0 > nn) {
        // Removes old cells
        for { cell <- cellElems.drop(nn) } {
          cell.markForRemoval
        }
      } else if (n0 < nn) {
        // Add new cells
        val b = builder(colIdx)
        for { rowIdx <- n0 - headerSize until n } {
          val id = s"$pid-$rowIdx-$colIdx"
          val style = cellStyle(rowIdx, colIdx)
          b.id(id)
          b.style(style)
          b.build(nifty, screen, colElement)
        }
      }
    }
    colElements1
  }

  /** Resizes all table */
  private def resize(nifty: Nifty,
    screen: Screen,
    parent: Element,
    n: Int,
    m: Int): IndexedSeq[Element] =
    resizeRows(
      resizeColumns(m, nifty, screen, parent),
      n, nifty, screen, parent)

  /** Sets cell content */
  private def setCells(nifty: Nifty,
    screen: Screen,
    parent: Element,
    cells: IndexedSeq[IndexedSeq[String]]) {
    val n = cells.size
    val m = colCount(cells)
    val colElements1 = resize(nifty, screen, parent, n, m)
    for {
      (colElem, colIdx) <- colElements1 zipWithIndex
    } {
      val elems = colElem.getElements
      for {
        (cellElem, rowIdx) <- elems.drop(headerOpt.size) zipWithIndex
      } {
        val value = cells(rowIdx)(colIdx)
        setter(rowIdx, colIdx)(cellElem, value)
      }
    }
    colElements = colElements1
  }

  /** Subscribes for cell values */
  def setCell(cells: IndexedSeq[IndexedSeq[String]]): Subscription = {
    val ctxObs = for {
      nifty <- niftyObs
      screen <- screenObs
      parent <- elementObs
    } yield (nifty, screen, parent)
    ctxObs.subscribe(_ match {
      case (nifty, screen, parent) => setCells(nifty, screen, parent, cells)
    })
  }

  /** */
  def selectionObsOpt: Observable[(Int, Int)] = {
    val idxOptObs = for {
      parent <- elementObs
      ev <- mousePrimaryClickedObs
    } yield {
      val pid = Option(parent.getId).getOrElse(parent.hashCode().toString())
      for {
        matcher <- s"$pid-(\\d*)-(\\d*)".r.findFirstMatchIn(ev.getElement.getId)
        if (matcher.groupCount == 2)
      } yield (matcher.group(1).toInt, matcher.group(2).toInt)
    }
    for { idxOpt <- idxOptObs if (!idxOpt.isEmpty) } yield idxOpt.get
  }
}
