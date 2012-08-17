package ciscoop.stampa.output.impl.itext;

import java.util.List;

import org.xreports.engine.Stampa;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Marginbox;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.MarginboxElement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class MarginboxIText extends ElementoIText implements Marginbox {

  public enum Style {
    SOLID, DASH, DOT, DASHDOT
  }

  /** altezza della linea: comprende margini e spessore linea */
  private float            c_height;

  private Stampa           c_stampa;
  private MarginboxElement c_marginboxElem;

  public MarginboxIText(XReport stampa, MarginboxElement lineElem, Elemento padre) throws GenerateException {
    setParent(padre);
    c_stampa = stampa;
    c_marginboxElem = lineElem;
    DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
    doc.getPageListener().setMarginbox(this);
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
    PdfContentByte cb = writer.getDirectContentUnder();

    BaseColor baseColor;
    Colore col = c_marginboxElem.getColore();
    DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
    if (col == null) {
      baseColor = BaseColor.BLACK;
    } else {
      baseColor = doc.getColorByName(col.getName());
      if (baseColor == null) {
        throw new GenerateException(c_marginboxElem, "Colore non definito: " + col.getName());
      }
    }

    cb.saveState();
    
    LineaIText.applyLineStyle(cb, c_marginboxElem.getThickness().getValue(), 
        LineaIText.Style.valueOf(c_marginboxElem.getStyle().toUpperCase()),
        baseColor);
    
    Document docItext = doc.getDocument();    
    float dist = c_marginboxElem.getDist().getValue();
    c_height = docItext.getPageSize().getHeight() - docItext.topMargin() - docItext.bottomMargin() - dist*2;
    float width = docItext.getPageSize().getWidth() - docItext.rightMargin() - docItext.leftMargin() - dist*2;

    cb.rectangle(docItext.leftMargin() + dist, docItext.bottomMargin() + dist, 
        width, c_height);    
    cb.stroke();
    
    cb.restoreState();
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi
  }

  @Override
  public Object getContent(Elemento padre) throws GenerateException {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#flush(ciscoop.stampa.output.Documento)
   */
  @Override
  public void flush(Documento doc) throws GenerateException {
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Marginbox" + getElementID();
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#fineGenerazione()
   */
  @Override
  public void fineGenerazione() throws GenerateException {
  }
}
