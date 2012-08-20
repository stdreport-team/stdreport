package org.xreports.engine.output.impl.itext;

import java.io.IOException;
import java.util.List;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Watermark;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement.HAlign;
import org.xreports.engine.source.Measure;
import org.xreports.engine.source.WatermarkElement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfWriter;

public class WatermarkIText extends ElementoIText implements Watermark {

	public enum Style {
		SOLID, DASH, DOT, DASHDOT
	}

	/** altezza della linea: comprende margini e spessore linea */
	private float							c_height;

	private WatermarkElement	c_watermarkElem;

	public WatermarkIText(XReport report, WatermarkElement wElem, Elemento parent)
			throws GenerateException {
		super(report, parent);
		c_watermarkElem = wElem;
		DocumentoIText doc = (DocumentoIText) report.getDocumento();
		doc.getPageListener().addWatermark(this);
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
		try {
			DocumentoIText doc = getDocumentImpl();
			String text = c_watermarkElem.getText(getReport());
			if (text != null)
				printText(writer, doc.getDocument(), text);
			else if (c_watermarkElem.getImage() != null)
				printImage(writer, doc.getDocument());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printText(PdfWriter writer, Document document, String text)
			throws GenerateException {
		float availableWidth = document.getPageSize().getWidth();
		if (c_watermarkElem.isUseMargins()) {
			availableWidth -= document.leftMargin() + document.rightMargin();
		}
		DocumentoIText doc = getDocumentImpl();
		Font f = doc.getFontByName(c_watermarkElem.getRefFontName());
		if (f == null)
			f = doc.getDefaultFont();

		float textWidth, textHeight;

		float refPointX;

		int alignment = Element.ALIGN_CENTER;
		float angleGrades = c_watermarkElem.getRotation();
		double angleRadians = Math.abs(gradiToRadianti(angleGrades));
		double textDiagonal;
		Font fOK = f;
		String texts[] = text.split("\\n");
		if (c_watermarkElem.isFitText()) {
			// devo calcolare la dimensione della font sufficiente per riempire in
			// larghezza:
			// 1) calcolo lunghezza della linea del testo (textDiagonal)
			// 2) calcolo dimensione font per rimpire tutta la lunghezza
			textDiagonal = availableWidth / Math.cos(angleRadians);
			float sizeOK = 9999;
			for (int i = 0; i < texts.length; i++) {
				// se testo su più linee, calcolo font per ogni linea, alla fine prendo
				// la più piccola
				float textSize = getFontSizeToFit(document, texts[i], textDiagonal,
						f.getBaseFont(), 16, 250);
				if (textSize < sizeOK)
					sizeOK = textSize;
			}
			fOK = new Font(f.getBaseFont(), sizeOK, f.getStyle(), f.getColor());
			textWidth = availableWidth;
			refPointX = document.getPageSize().getWidth() / 2;
		}
		else {
			// dimensione font già fissata
			textWidth = fOK.getBaseFont().getWidthPoint(text, fOK.getSize());
			textDiagonal = textWidth / Math.cos(angleRadians);
			if (angleGrades != 0) {
				// se il testo è inclinato, è meno largo.
				// Calcolo la larghezza con la formula "width*cos(angolo)"
				textWidth = textWidth * (float) Math.cos(angleRadians);
			}
			// l'allineamento orizzontale ha senso solo se non uso fitText
			switch (c_watermarkElem.getHalign()) {
			case LEFT:
				if (c_watermarkElem.isUseMargins()) {
					refPointX = document.leftMargin();
				}
				else {
					refPointX = 0;
				}
				alignment = Element.ALIGN_LEFT;
				break;
			case RIGHT:
				refPointX = (float) (document.getPageSize().getWidth() - textWidth);
				if (c_watermarkElem.isUseMargins()) {
					refPointX -= document.rightMargin();
				}
				alignment = Element.ALIGN_LEFT;
				break;
			default:
				alignment = Element.ALIGN_CENTER;
				refPointX = document.getPageSize().getWidth() / 2;
				break;
			}
		}

		textHeight = (float) Math.abs(textDiagonal * Math.sin(angleRadians));
		float refPointY = calcRefPointY(document, textHeight,
				alignment == Element.ALIGN_CENTER);

		float leading = fOK.getSize();
		Measure xmlLeading = c_watermarkElem.getLeading();
		if (xmlLeading != null) {
			if (xmlLeading.isLines())
				leading = leading * xmlLeading.getValue();
			else
				leading = xmlLeading.getValue();
		}
		// leading += (maxAscent + maxDescent) * Math.sin(angleRadians);
		// //interlinea aumenta in proporzione all'angolo di rotazione
		float stepHeight = leading * (float) Math.cos(angleRadians);
		float stepWidth = leading * (float) Math.sin(angleRadians);
		if (texts.length > 1
				&& !(!c_watermarkElem.isFitText() && c_watermarkElem.getHalign() == HAlign.LEFT)) {
			float linesHeight = (leading * texts.length)
					* (float) Math.cos(angleRadians);
			float linesWidth = (leading * texts.length)
					* (float) Math.sin(angleRadians);
			refPointY += linesHeight / 2;
			refPointX -= linesWidth / 2 - stepWidth / 2;
		}
		Colore textColor = c_watermarkElem.getColor();
		if (textColor != null) {
			fOK = new Font(fOK.getBaseFont(), fOK.getSize());
			fOK.setColor(new BaseColor(textColor.getRed(), textColor.getGreen(),
					textColor.getBlue(), textColor.getAlpha()));
		}
		PdfContentByte cb = writer.getDirectContentUnder();
		cb.saveState();
		if (textColor != null && textColor.getAlpha() < 255) {
			PdfGState gState = new PdfGState();
			gState.setFillOpacity((float) textColor.getAlpha() / 255f);
			cb.setGState(gState);
		}
		float rotation = writer.getPageNumber() % 2 == 1 ? angleGrades
				: -angleGrades;
		for (int i = 0; i < texts.length; i++) {
			// stampo ogni linea del testo separatamente, spostando in basso il punto
			// di riferimento per ogni riga che vado a scrivere
			ColumnText.showTextAligned(cb, alignment, new Phrase(texts[i], fOK),
					refPointX + i * stepWidth, refPointY - i * stepHeight, rotation);
		}

		cb.restoreState();

		// cb.setColorFill(new BaseColor(255, 72, 0, 20));
		// cb.circle(refPointX, refPointY, 18);
		// cb.fillStroke();
		// cb.setLineWidth(0.1f);
		// cb.setColorFill(new BaseColor(0, 0, 0, 70));
		// Font fText = new Font(fOK.getBaseFont(), 7);
		// NumberFormat nf = NumberFormat.getIntegerInstance();
		// String pp = nf.format(refPointX) + ", " + nf.format(refPointY);
		// ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(pp,
		// fText),
		// refPointX, refPointY - 3, 0);
	}

	/**
	 * In base alla dimensione del documento, margini e VAlign, calcola l'atezza
	 * del punto di riferimento.
	 * 
	 * @param document
	 *          docuemnto pdf
	 * @param blockSize
	 *          altezza del blocco di testo o dell'immagine
	 * @param centered
	 *          se true, indica che l'ordinata di riferimento è centrata rispetto
	 *          a blockSize
	 * @return ordinata del punto di riferimento
	 */
	private float calcRefPointY(Document document, float blockSize,
			boolean centered) {
		float refPointY;
		switch (c_watermarkElem.getValign()) {
		case TOP:
			if (centered)
				blockSize /= 2;
			refPointY = document.getPageSize().getHeight() - blockSize;
			if (c_watermarkElem.isUseMargins()) {
				refPointY -= document.topMargin();
			}
			break;
		case BOTTOM:
			if (c_watermarkElem.isUseMargins()) {
				refPointY = document.bottomMargin();
			}
			else {
				refPointY = 0;
			}
			if (centered)
				refPointY += blockSize / 2;
			break;
		default:
			float h = document.getPageSize().getHeight();
			// if (c_watermarkElem.isUseMargins()) {
			// h -= document.topMargin() + document.bottomMargin();
			// }
			refPointY = h / 2 - blockSize / 2;
			if (refPointY < 0)
				refPointY = h / 2;
			break;
		}
		return refPointY;
	}

	/**
	 * Calcola la dimensione della font perchè il testo passato abbia una
	 * larghezza il più vicino possibile 'sizeToFit' ma non superiore.
	 * 
	 * @param text
	 *          testo di cui calcolare la larghezza
	 * @param sizeToFit
	 *          larghezza a cui deve arrivare il testo
	 * @param bf
	 *          basefont da usare
	 * @param minSize
	 *          dimensione minima della font
	 * @param maxSize
	 *          dimensione massima della font
	 * @return dimensione che deve avere la BaseFont perchè il testo passato sia
	 *         largo maxSize; NB: la dimensione ritornata è sempre compresa nel
	 *         range minSize-maxSize.
	 */
	private float getFontSizeToFit(Document document, String text,
			double sizeToFit, BaseFont bf, int minSize, int maxSize) {
		float angleGrades = c_watermarkElem.getRotation();
		double angleRadians = Math.abs(gradiToRadianti(angleGrades));
		float maxHeight = document.getPageSize().getHeight();

		float currentSize = minSize;
		float testSize = minSize;
		while (currentSize <= maxSize) {
			float size = bf.getWidthPoint(text, testSize);
			float heightOfText = testSize + (float) size
					* (float) Math.sin(angleRadians);
			if (size > sizeToFit || heightOfText > maxHeight)
				return currentSize;
			else {
				currentSize = testSize;
			}
			testSize += 0.5;
		}
		return currentSize;
	}

	/**
	 * Converte gradi in radianti
	 * 
	 * @param gradi
	 *          angolo in gradi
	 * @return angolo in radianti
	 */
	private float gradiToRadianti(double gradi) {
		return (float) (Math.PI * gradi / 180);
	}

	/**
	 * Stampa il watermark con l'immagine
	 * 
	 * @param writer
	 *          pdf writer
	 * @param document
	 *          pdf document
	 * 
	 * @throws IOException
	 *           in caso ci sia un errore in letture del file dell'immagine
	 * @throws DocumentException
	 *           nel caso ci sia un errore a basso livello in aggiunta immagine al
	 *           documento pdf
	 * @throws GenerateException
	 *           nel caso il file dell'immagine specificato nell'attributo 'image'
	 *           non sia trovato
	 */
	private void printImage(PdfWriter writer, Document document)
			throws IOException, DocumentException, GenerateException {
		String absPathImage = getReport().findResource(c_watermarkElem.getImage());
		if (absPathImage == null) {
			throw new GenerateException(c_watermarkElem,
					"Non riesco a trovare il file di immagine "
							+ c_watermarkElem.getImage());
		}
		Image img = Image.getInstance(absPathImage);
		scaleImage(img);
		rotateImage(img);

		float refPointX;
		float refPointY = calcRefPointY(document, img.getScaledHeight(), false);

		switch (c_watermarkElem.getHalign()) {
		case LEFT:
			refPointX = 0;
			if (c_watermarkElem.isUseMargins())
				refPointX = document.leftMargin();
			break;
		case RIGHT:
			refPointX = document.getPageSize().getWidth() - img.getScaledWidth();
			if (c_watermarkElem.isUseMargins())
				refPointX -= document.rightMargin();
			break;
		default:
			// centrato
			refPointX = (document.getPageSize().getWidth() - img.getScaledWidth()) / 2;
			break;
		}

		img.setAbsolutePosition(refPointX, refPointY);

		// img.setAbsolutePosition((document.getPageSize().getWidth() -
		// img.getScaledWidth()) / 2,
		// (document.getPageSize().getHeight() - img.getScaledHeight()) / 2);
		PdfContentByte cb = writer.getDirectContentUnder();
		cb.addImage(img);
	}

	private void rotateImage(Image img) {
		float angle = c_watermarkElem.getRotation();
		if (angle != 0)
			img.setRotation(gradiToRadianti(angle));
	}

	/**
	 * Ridimensiona immagine se almeno uno fra attributi 'width' e 'height' è
	 * specificato
	 * 
	 * @param img
	 *          immagine da scalare
	 */
	private void scaleImage(Image img) {
		// ========== gestione dimensioni ===========
		Measure width = c_watermarkElem.getWidth();
		Measure heigth = c_watermarkElem.getHeight();
		if (width != null && heigth != null) {
			// specificato altezza e larghezza: li scalo separatamente
			img.scaleAbsolute(width.scale(img.getWidth()),
					heigth.scale(img.getHeight()));
		} else if (width != null) {
			float ratio = 1f;
			ratio = width.isPercent() ? width.getValue() / 100f : width.getValue()
					/ img.getWidth();

			img.scaleAbsolute(ratio * img.getWidth(), ratio * img.getHeight());
		} else if (heigth != null) {
			float ratio = 1f;
			ratio = heigth.isPercent() ? heigth.getValue() / 100f : heigth.getValue()
					/ img.getHeight();
			img.scaleAbsolute(ratio * img.getWidth(), ratio * img.getHeight());
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
		return "Watermark" + getElementID();
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
