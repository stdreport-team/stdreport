/**
 * 
 */
package org.xreports.engine.output.impl.itext;

import org.xreports.engine.source.Border;
import org.xreports.engine.source.CellElement;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * @author pier
 * 
 */
public class CellBorderEvent implements PdfPCellEvent {
  private CellElement    m_elem;
  private DocumentoIText m_doc;
  private BorderIText    m_borderDraw = null;

  public CellBorderEvent(CellElement elem, DocumentoIText doc) {
    m_elem = elem;
    m_doc = doc;
    m_borderDraw = new BorderIText(m_doc);
  }

  /**
   * Disegna il bordo 'custom' attorno alla cella passata
   * 
   * @see com.itextpdf.text.pdf.PdfPCellEvent#cellLayout(com.itextpdf.text.pdf.PdfPCell, com.itextpdf.text.Rectangle,
   *      com.itextpdf.text.pdf.PdfContentByte[])
   */
  @Override
  public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvases) {
    if (m_elem == null) {
      return;
    }
    PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
    Border border = m_elem.getBorder();
    if (border.hasBorder()) {
      m_borderDraw.draw(border, rect, cb);
    }
    border = m_elem.getBorderTop();
    if (border.hasBorder()) {
      m_borderDraw.draw(border, rect, cb);
    }
    border = m_elem.getBorderBottom();
    if (border.hasBorder()) {
      m_borderDraw.draw(border, rect, cb);
    }
    border = m_elem.getBorderLeft();
    if (border.hasBorder()) {
      m_borderDraw.draw(border, rect, cb);
    }
    border = m_elem.getBorderRight();
    if (border.hasBorder()) {
      m_borderDraw.draw(border, rect, cb);
    }
  }

}
