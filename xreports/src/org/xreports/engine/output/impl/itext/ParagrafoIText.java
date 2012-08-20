package org.xreports.engine.output.impl.itext;

import java.util.List;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Paragrafo;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement.HAlign;
import org.xreports.engine.source.Measure;
import org.xreports.engine.source.TextElement;

import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

public class ParagrafoIText extends ElementoIText implements Paragrafo {
  /** interlinea di default: se un paragrafo (o Phrase) non ha specificata un interlinea,
   *  iText guarda la misura della sua font e la moltiplica per questo valore.
   */
  public static final float DEFAULT_LEADING = 1.5f;
  
  
  private Paragraph   paragraph  = null;

  private TextElement c_textElem = null;
  private float       c_marginTop;
  private float       c_calcMinHeight;

  public ParagrafoIText(XReport report, TextElement textElem, Elemento parent) throws GenerateException {
  	super(report, parent);
    c_textElem = textElem;
    paragraph = creaParagraph(report, textElem);
    c_marginTop = textElem.getSpazioPrima();
  }

  @Override
  public String toString() {
    return getUniqueID() + " (" + c_textElem + ")";
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Par" + getElementID();
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    Object obj = figlio.getContent(this);
    if (obj != null) {
      //if (obj instanceof Chunk) {
      //synchronized (this) {
      //((Chunk)obj).setGenericTag(figlio.getUniqueID());
      paragraph.add((Element) obj);
      //}
      //}
      ElementoIText iElem = (ElementoIText)figlio;
      if (iElem.isBlock()) {
        c_calcMinHeight += iElem.getHeight();
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    for (Elemento figlio : figli) {
      addElement(figlio);
    }
  }

  @Override
  public Object getContent(Elemento padre) throws GenerateException {
    return paragraph;
  }

  /**
   * 
   * @return Interlinea del paragrafo
   */
  public float getLeading() {
    if (paragraph != null) {
      return paragraph.getLeading();
    }
    return 0;
  }

  private Paragraph creaParagraph(XReport stampa, TextElement textElem) throws GenerateException {
    final float interlinea = 1.3f;

    Paragraph par = new Paragraph();
    DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
    par.setFont(doc.getFontByName(textElem.getRefFontName()));
    if (par.getFont() == null) {
      stampa.addWarningMessage("Attenzione: l'elemento " + textElem.toString() + " risulta senza font");
    } else {
      Measure leading = textElem.getInterlinea();
      if (leading != null) {
        if (leading.isLines()) {
          par.setLeading(par.getFont().getCalculatedLeading(leading.getValue()));
        } else {
          par.setLeading(leading.getValue());
        }
      } else {
        par.setLeading(par.getFont().getCalculatedLeading(interlinea));
      }
    }
    if (textElem.getSpazioPrima() > 0) {
      par.setSpacingBefore(textElem.getSpazioPrima());
    }
    if (textElem.getSpazioDopo() > 0) {
      par.setSpacingAfter(textElem.getSpazioDopo());
    }
    if (textElem.getSpazioSinistra() > 0) {
      par.setIndentationLeft(textElem.getSpazioSinistra());
    }
    if (textElem.getSpazioDestra() > 0) {
      par.setIndentationRight(textElem.getSpazioDestra());
    }

    par.setFirstLineIndent(textElem.getFirstLineIndent());
    HAlign hAlign = textElem.getHalign();
    if (hAlign != null) {
      par.setAlignment(getHAlignForItext(hAlign));
    }

    return par;
  }

  @Override
  public void flush(Documento documento) throws GenerateException {

  }

  @Override
  public float calcAvailWidth() {
    float maxWidth = ((ElementoIText) getParent()).calcAvailWidth();
    float indent = paragraph.getIndentationLeft() + paragraph.getIndentationRight();

    return maxWidth - indent;
  }
  
  @Override
  public float getHeight() {
    return c_calcMinHeight;
  }

  public float getMarginTop() {
    return c_marginTop;
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return true;
  }
}
