package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.stampa.ResolveException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.Immagine;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;

public class ImageElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi del Tag "image" */
  private static final String ATTRIB_HEIGHT = "height";
  private static final String ATTRIB_WIDTH  = "width";
  private static final String ATTRIB_SRC    = "src";
  private static final String ATTRIB_X      = "x";
  private static final String ATTRIB_Y      = "y";

  public static final String DEFAULT_BORDER = "0";
  
  public ImageElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();

    addAttributo(ATTRIB_SRC, String.class, null, TAG_TEXT);    
    addAttributoMeasure(ATTRIB_X, null, false, false);
    addAttributoMeasure(ATTRIB_Y, null, false, false);
    addAttributoMeasure(ATTRIB_HEIGHT, null, true, false);
    addAttributoMeasure(ATTRIB_WIDTH, null, true, false);
    addAttributoBorder(ATTRIB_BORDER, DEFAULT_BORDER, Border.BOX);    
  }
  
  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    salvaStampaGruppo(stampa, gruppo);
    try {
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLOpenTag());
      }
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        Immagine immagine = stampa.getFactoryElementi().creaImmagine(stampa, this, padre);
        immagine.fineGenerazione();
        listaElementi.add(immagine);
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
   * Ritorna il path dell'immagine. Se l'attributo è composto da una espressione,
   * viene ritornato il risultato della valutazione.
   * @throws GenerateException 
   */
  public String getSrc() throws ResolveException {
    return getExpressionAsString(ATTRIB_SRC);
  }

  /**
   * Ritorna il simbolo top level dell'espressione contenuta nell'attributo <tt>src</tt>.
   * @throws GenerateException 
   */
  public Symbol getSrcSymbol() {
    return getAttrSymbol(ATTRIB_SRC);
  }
  
  public Measure getWidth() {
    return getAttrValueAsMeasure(ATTRIB_WIDTH);
  }

  public Measure getHeight() {
    return getAttrValueAsMeasure(ATTRIB_HEIGHT);
  }

  public Measure getPosX() {
    return getAttrValueAsMeasure(ATTRIB_X);
  }

  public Measure getPosY() {
    return getAttrValueAsMeasure(ATTRIB_Y);
  }

  public Border getBorder() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDER);
    return b;
  }
  
  
  @Override
  public void fineParsingElemento() {
  }

  @Override
  public String getTagName() {
    return "image";
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#evaluate(ciscoop.expressions.symbols.Symbol)
   */
  @Override
  public Object evaluate(Symbol symbol) throws ResolveException {
    if (symbol.isConstant() || symbol.isField()) {
      return super.evaluate(symbol); 
    }
    return symbol.getText(); 
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#evaluate(ciscoop.expressions.symbols.Symbol, ciscoop.datagroup.Group)
   */
  @Override
  public Object evaluate(Symbol symbol, Group group) throws ResolveException {
    if (symbol.isConstant() || symbol.isField()) {
      return super.evaluate(symbol, group); 
    }
    return symbol.getText(); 
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
    return false;
  }

  @Override
  public boolean canChildren() {
    return false;
  }
}
