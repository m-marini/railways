package org.mmarini.scala.jmonkey

import rx.lang.scala.Observable
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.builder.ElementBuilder
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.builder.PanelBuilder
import collection.JavaConversions._
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.elements.render.ImageRenderer

/**
 * table-panel,center
 *   col-panel,horizontal
 *     cell-panel,vertical
 *       text/image
 *
 * @author us00852
 */
trait TableController extends MousePrimaryClickedObservable
    with NiftyUtil
    with ScreenUtil
    with ElementUtil {

  val DefaultColumnElementStyle = "panel.table-column"
  val DefaultCellStyle = "nifty-label"

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
      nifty <- niftyOpt
      screen <- screenOpt
    } {
      elem.getRenderer(classOf[ImageRenderer]).
        setImage(nifty.getRenderEngine.createImage(screen, text, false))
    }

  private var colElements = IndexedSeq[Element]()

  // change the image
  /** */
  private def resize(n: Int, m: Int) =
    for {
      nifty <- niftyOpt
      screen <- screenOpt
      parent <- elementOpt
    } yield {
      val m0 = colElements.size
      val colElements1 = if (m0 == m) {
        colElements
      } else if (m0 > m) {
        // Removes old columns
        for { col <- colElements.drop(m) } col.markForRemoval
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

      for {
        (colElement, colIdx) <- colElements1 zipWithIndex
      } {
        val cellElems = colElement.getElements
        val n0 = cellElems.size
        if (n0 > n) {
          // Removes old cells
          for { cell <- cellElems.drop(n) } cell.markForRemoval
        } else if (n0 < n) {
          // Add new cells
          val b = builder(colIdx)
          for { rowIdx <- n0 until n } {
            val id = s"${parent.getId}-${rowIdx}-${colIdx}"
            val style = cellStyle(rowIdx, colIdx)
            b.id(id)
            b.style(style)
            b.build(nifty, screen, colElement)
          }
        }
      }
      colElements1
    }

  /** */
  def setCells(cells: IndexedSeq[IndexedSeq[String]]) {
    val n = cells.size
    val m = colCount(cells)
    val colElements1 = resize(n, m)
    for {
      colElems <- colElements1
      (colElem, colIdx) <- colElems zipWithIndex
    } for {
      (cellElem, rowIdx) <- colElem.getElements zipWithIndex
    } {
      val value = cells(rowIdx)(colIdx)
      setter(rowIdx, colIdx)(cellElem, value)
    }
  }

  /** */
  def selectionObsOpt: Option[Observable[(Int, Int)]] =
    for {
      parent <- elementOpt
    } yield {
      val idxOptObs = for {
        ev <- mousePrimaryClickedObs
      } yield for {
        matcher <- s"""${parent.getId}-(\\d*)-(\\d*)""".r.findFirstMatchIn(ev.getElement.getId)
        if (matcher.groupCount == 2)
      } yield (matcher.group(1).toInt, matcher.group(2).toInt)
      for {
        x <- idxOptObs
        if (!x.isEmpty)
      } yield x.get
    }
}