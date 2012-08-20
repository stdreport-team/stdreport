package org.xreports.engine.output.impl.itext;

import java.text.NumberFormat;
import java.util.List;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Rulers;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.RulersElement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class RulersIText extends ElementoIText implements Rulers {

	public enum Style {
		SOLID, DASH, DOT, DASHDOT
	}

	/** altezza della linea: comprende margini e spessore linea */
	private float					c_height;

	private RulersElement	c_rulersElem;

	public RulersIText(XReport report, RulersElement lineElem, Elemento parent)
			throws GenerateException {
		super(report, parent);
		c_rulersElem = lineElem;
		DocumentoIText doc = (DocumentoIText) report.getDocumento();
		doc.getPageListener().setRulers(this);
	}

	@Override
	public float calcAvailWidth() {
		return 0;
	}

	@Override
	public float getHeight() {
		return c_height;
	}

	/**
	 * Effettua fisicamente il disegno della linea secondo i parametri impostati
	 * 
	 * @throws GenerateException
	 */
	public void draw(PdfWriter writer) throws GenerateException {
		float dist = c_rulersElem.getStep().getValue();
		if (dist <= 0) {
			return;
		}

		PdfContentByte cb = writer.getDirectContentUnder();
		BaseColor baseColor;
		Colore col = c_rulersElem.getColore();
		boolean showText = c_rulersElem.isShowText();
		DocumentoIText doc = getDocumentImpl();
		if (col == null) {
			baseColor = BaseColor.BLACK;
		} else {
			baseColor = doc.getColorByName(col.getName());
			if (baseColor == null) {
				throw new GenerateException(c_rulersElem, "Colore non definito: "
						+ col.getName());
			}
		}

		cb.saveState();

		try {
			LineaIText.applyLineStyle(cb, c_rulersElem.getThickness().getValue(),
					LineaIText.Style.valueOf(c_rulersElem.getStyle().toUpperCase()),
					baseColor);

			Document docItext = doc.getDocument();

			float heiDoc = docItext.getPageSize().getHeight();
			float widDoc = docItext.getPageSize().getWidth();

			BaseFont font = BaseFont.createFont(BaseFont.COURIER, BaseFont.WINANSI,
					true);

			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(1);
			nf.setMinimumFractionDigits(1);

			// linee orizzontali
			float hPos = 0;
			while (hPos < heiDoc) {
				if (hPos > 0) {
					cb.moveTo(0, hPos);
					cb.lineTo(widDoc, hPos);
					if (showText) {
						cb.beginText();
						cb.setFontAndSize(font, 6);
						cb.showTextAligned(Element.ALIGN_LEFT, nf.format(hPos), 10,
								hPos + 1, 0);
						cb.endText();
					}
				}
				hPos += dist;
			}
			// linee verticali
			float vPos = 0;
			while (vPos < widDoc) {
				if (vPos > 0) {
					cb.moveTo(vPos, 0);
					cb.lineTo(vPos, heiDoc);
					if (showText) {
						cb.beginText();
						cb.setFontAndSize(font, 6);
						cb.showTextAligned(Element.ALIGN_CENTER, nf.format(vPos), vPos,
								heiDoc - 10, 0);
						cb.endText();
					}
				}
				vPos += dist;
			}
			cb.stroke();
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenerateException("Errore grave in disegno rulers: "
					+ e.toString());
		} finally {
			cb.restoreState();
		}
	}

	@Override
	public void addElement(Elemento figlio) throws GenerateException {
		// E' un elemento finale non è possibile aggiungere altri elementi
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
	 */
	@Override
	public void addElements(List<Elemento> figli) throws GenerateException {
		// E' un elemento finale non è possibile aggiungere altri elementi
	}

	@Override
	public Object getContent(Elemento padre) throws GenerateException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.output.Elemento#flush(ciscoop.stampa.output.Documento)
	 */
	@Override
	public void flush(Documento doc) throws GenerateException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.output.Elemento#getUniqueID()
	 */
	@Override
	public String getUniqueID() {
		return "Marginbox" + getElementID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
	 */
	@Override
	public boolean isBlock() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.impl.itext.ElementoIText#fineGenerazione()
	 */
	@Override
	public void fineGenerazione() throws GenerateException {
	}
}
