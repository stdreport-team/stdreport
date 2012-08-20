/**
 * 
 */
package org.xreports.engine.output.impl.itext;

import org.xreports.engine.XReport;
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
	private LineaIText	m_linea;
	private XReport			m_report;

	public CellEvent(XReport report, LineaIText image) {
		m_linea = image;
		m_report = report;
	}

	public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
		try {
			if (m_linea != null)
				m_linea.draw(rect);
		} catch (GenerateException e) {
			m_report.addErrorMessage("Errore grave in disegno linea " + m_linea, e);
		}
	}

}
