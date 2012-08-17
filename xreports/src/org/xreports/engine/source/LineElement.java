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
import org.xreports.stampa.output.Linea;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;

/**
 * @author pier
 * 
 */
public class LineElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi del Tag "line" */
  private static final String ATTRIB_LENGTH      = "length";
  private static final String ATTRIB_THICKNESS   = "thickness";
  private static final String ATTRIB_COUNT       = "count";
  private static final String ATTRIB_COLOR       = "refColor";
  private static final String ATTRIB_USEMARGINS  = "useMargins";
  private static final String ATTRIB_STYLE       = "style";
  private static final String ATTRIB_POSITION    = "position";
  private static final String ATTRIB_X           = "x";
  private static final String ATTRIB_Y           = "y";

  public static final String  POS_ABSOLUTE       = "absolute";
  public static final String  POS_RELATIVE       = "relative";

  public static final String  STYLE_SOLID        = "solid";
  public static final String  STYLE_DASH         = "dash";
  public static final String  STYLE_DOT          = "dot";
  public static final String  STYLE_DASHDOT      = "dashdot";

  public static final String  HALIGN_LEFT        = "left";
  public static final String  HALIGN_CENTER      = "center";
  public static final String  HALIGN_RIGHT       = "right";
  public static final String  VALIGN_TOP         = "top";
  public static final String  VALIGN_MIDDLE      = "middle";
  public static final String  VALIGN_BOTTOM      = "bottom";

  private static final int    DEFAULT_COUNT      = 1;
  private static final String DEFAULT_MARGINTOP = "4";
  private static final String DEFAULT_MARGINBOT = "0";
  public static final float   DEFAULT_THICKNESS = 0.5f;

  public LineElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);    
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributoMeasure(ATTRIB_THICKNESS, String.valueOf(DEFAULT_THICKNESS), false, false);
    addAttributoMeasure(ATTRIB_LENGTH, "100%", true, false);
    addAttributoMeasure(ATTRIB_X, "0", false, false);
    addAttributoMeasure(ATTRIB_Y, "0", false, false);
    addAttributo(ATTRIB_COUNT, Integer.class, String.valueOf(DEFAULT_COUNT));
    addAttributoColore(ATTRIB_COLOR, Colore.NERO.getName());
    addAttributo(ATTRIB_USEMARGINS, Boolean.class, "true");
    addAttributoMeasure(ATTRIB_MARGIN_TOP, String.valueOf(DEFAULT_MARGINTOP), false, false);
    addAttributoMeasure(ATTRIB_MARGIN_BOTTOM, String.valueOf(DEFAULT_MARGINBOT), false, false);
    addAttributo(ATTRIB_STYLE, String.class, LineElement.STYLE_SOLID.toUpperCase());
    addAttributo(ATTRIB_POSITION, String.class, POS_RELATIVE);    
  }
  
  @Override
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    super.loadAttributes(attrs);
    Measure top = getAttrValueAsMeasure(ATTRIB_MARGIN_TOP);
    if (top.getValue() < 0 ) {
      throw new ValidateException(this, "per l'attributo marginTop non sono ammessi valori negativi.");
    }
    Measure bot = getAttrValueAsMeasure(ATTRIB_MARGIN_BOTTOM);
    if (bot.getValue() < 0 ) {
      throw new ValidateException(this, "per l'attributo marginBottom non sono ammessi valori negativi.");
    }
  }
  
  
//  private void parseAttributes(Attributes attrs) throws ValidateException {
//    attrib_thickness = leggiMeasure(ATTRIB_THICKNESS, attrs, "0.5");
//    attrib_length = leggiMeasure(ATTRIB_LENGTH, true, attrs, "100%");
//    attrib_x = leggiMeasure(ATTRIB_X, attrs, "0");
//    attrib_y = leggiMeasure(ATTRIB_Y, attrs, "0");
//    attrib_count = Text.toInt(leggiAttributo(ATTRIB_COUNT, attrs), DEFAULT_COUNT);    
//    attrib_color = leggiAttributo(ATTRIB_COLOR, attrs);
//    attrib_useMargins = Text.toBoolean(leggiAttributo(ATTRIB_USEMARGINS, attrs), true);    
//    attrib_margineSup = leggiMeasure(ATTRIB_MARGIN_TOP, attrs, DEFAULT_MARGINESUP);
//    if (attrib_margineSup.getValue() < 0) {
//      throw new ValidateException(this, "per l'attributo margineSup non sono ammessi valori negativi.");
//    }
//    attrib_margineInf = leggiMeasure(ATTRIB_MARGIN_BOTTOM, attrs, DEFAULT_MARGINEINF);
//    if (attrib_margineInf.getValue() < 0) {
//      throw new ValidateException(this, "per l'attributo margineInf non sono ammessi valori negativi.");
//    }
//    
//    attrib_style = leggiAttributo(ATTRIB_STYLE, attrs);
//    attrib_position = leggiAttributo(ATTRIB_POSITION, attrs, POS_RELATIVE);
//  }

  public float getMarginTop() {
    return getAttrValueAsMeasure(ATTRIB_MARGIN_TOP).getValue();
  }

  public float getMarginBottom() {
    return getAttrValueAsMeasure(ATTRIB_MARGIN_BOTTOM).getValue();
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
        Linea linea = stampa.getFactoryElementi().creaLinea(stampa, this, padre);
        linea.fineGenerazione();
        listaElementi.add(linea);
      }
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLCloseTag());
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  /**
   * Lunghezza della linea. Non ritorna mai null in quanto, se assente nel sorgente, viene sempre ritornata la misura di default,
   * che è <b>100%</b> .
   */
  public Measure getLength() {
    return getAttrValueAsMeasure(ATTRIB_LENGTH);
  }

  /**
   * Restituisce lo spessore della linea in punti. Non ritorna mai null in quanto, se assente nel sorgente, viene sempre ritornata
   * la misura di default, che è <b>0,5</b> punti.
   * 
   */
  public float getThickness() {
    return getAttrValueAsMeasure(ATTRIB_THICKNESS).getValue();
  }

  /**
   * Ritorna la quantità di linee da disegnare. Il default è 1.
   */
  public int getCount() {
    return getAttrValueAsInteger(ATTRIB_COUNT);
  }

  /**
   * @return the c_color
   */
  public String getColor() {
    return getAttrValueAsColore(ATTRIB_COLOR).getName();
  }

  /**
   * @return the c_useMargins
   */
  public boolean isUseMargins() {
    return getAttrValueAsBoolean(ATTRIB_USEMARGINS);
  }

  /**
   * @return the c_style
   */
  public String getStyle() {
    return getAttrValue(ATTRIB_STYLE).toString();
  }

  /**
   * @return the c_position
   */
  public boolean isAbsolutePosition() {
    return getAttrValue(ATTRIB_POSITION).toString().equalsIgnoreCase(POS_ABSOLUTE);
  }

  /**
   * @return the x
   */
  public float getX() {
    return getAttrValueAsMeasure(ATTRIB_X).getValue();
  }

  /**
   * @return the y
   */
  public float getY() {
    return getAttrValueAsMeasure(ATTRIB_Y).getValue();
  }

  @Override
  public String getTagName() {
    return "line";
  }

  /* (non-Javadoc)
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
    return true;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return true;
  }

  @Override
  public boolean canChildren() {
    return false;
  }
}
