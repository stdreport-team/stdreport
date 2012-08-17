/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.Marginbox;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;

/**
 * @author pier
 * 
 */
public class MarginboxElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi del Tag "line" */
  private static final String ATTRIB_THICKNESS  = "thickness";
  private static final String ATTRIB_COLOR      = "refColor";
  private static final String ATTRIB_STYLE      = "style";
  private static final String ATTRIB_DIST       = "dist";

  public static final float   DEFAULT_THICKNESS = 0.2f;

  public MarginboxElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributoMeasure(ATTRIB_THICKNESS, String.valueOf(DEFAULT_THICKNESS), false, false);
    addAttributo(ATTRIB_STYLE, String.class, LineElement.STYLE_SOLID.toUpperCase());
    addAttributoColore(ATTRIB_COLOR, Colore.NERO.getName());
    addAttributoMeasure(ATTRIB_DIST, "0", false, false);
  }

  public Colore getColore() {
    return getAttrValueAsColore(ATTRIB_COLOR);
  }

  /**
   * Restituisce lo spessore della linea in punti. Non ritorna mai null in
   * quanto, se assente nel sorgente, viene sempre ritornata la misura di
   * default, che è {@link #DEFAULT_THICKNESS} punti.
   * 
   */
  public Measure getThickness() {
    return getAttrValueAsMeasure(ATTRIB_THICKNESS);
  }

  /**
   * Restituisce la distanza della linea dal margine. Non ritorna mai null in
   * quanto, se assente nel sorgente, viene sempre ritornato il valore 0.
   * 
   */
  public Measure getDist() {
    return getAttrValueAsMeasure(ATTRIB_DIST);
  }
  
  /**
   * Restituisce il nome dello stile della linea. Vedi
   * {@link LineElement#STYLE_DASH}, {@link LineElement#STYLE_DASHDOT},... <br/>
   * Se non specificato, il default è {@link LineElement#STYLE_SOLID}
   */
  public String getStyle() {
    return getAttrValue(ATTRIB_STYLE).toString();
  }

  @Override
  public void fineParsingElemento() {
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLOpenTag());
      }

      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        Marginbox mbox = stampa.getFactoryElementi().creaMarginbox(stampa, this, padre);
        mbox.fineGenerazione();
        listaElementi.add(mbox);
      }
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLCloseTag());
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  @Override
  public String getTagName() {
    return "marginbox";
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return false;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }

  @Override
  public boolean canChildren() {
    return false;
  }
}
