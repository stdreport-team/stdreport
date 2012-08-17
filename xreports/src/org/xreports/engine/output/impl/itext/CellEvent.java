/**
 * 
 */
package ciscoop.stampa.output.impl.itext;

import org.xreports.engine.Stampa;
import org.xreports.engine.output.impl.GenerateException;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;

/**
 * @author pier
 * 
 */
public class CellEvent implements PdfPCellEvent {
  private LineaIText m_linea;
  private Stampa     m_stampa;

  public CellEvent(XReport stampa, LineaIText image) {
    m_linea = image;
    m_stampa = stampa;
  }

  public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
    try {
      if (m_linea != null)
        m_linea.draw(rect);
    } catch (GenerateException e) {
      m_stampa.addErrorMessage("Errore grave in disegno linea " + m_linea, e);
    }
  }

}
