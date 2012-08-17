package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.stampa.ResolveException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Cella;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.source.Border.BorderStyle;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

import org.xreports.expressions.symbols.EvaluateException;

public class CellElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi dell'elemento "cell" */
  public static final String ATTRIB_COLSPAN         = "colspan";
  public static final String ATTRIB_ROWSPAN         = "rowspan";

  public static final String ATTRIB_PADDING         = "padding";
  public static final String ATTRIB_PADDINGTOP      = ATTRIB_PADDING + "Top";
  public static final String ATTRIB_PADDINGBOTTOM   = ATTRIB_PADDING + "Bottom";
  public static final String ATTRIB_PADDINGLEFT     = ATTRIB_PADDING + "Left";
  public static final String ATTRIB_PADDINGRIGHT    = ATTRIB_PADDING + "Right";
  public static final String ATTRIB_MINHEIGHT       = "minHeight";
  public static final String ATTRIB_FIXEDHEIGHT     = "fixedHeight";
  public static final String ATTRIB_BACKGROUNDCOLOR = "backgroundColor";
  public static final String ATTRIB_ROTATION        = "rotation";
  public static final String ATTRIB_REPEAT          = "repeat";

  public static final String DEFAULT_COLSPAN        = "1";
  public static final String DEFAULT_ROWSPAN        = "1";
  public static final String DEFAULT_BORDER         = "0";
  public static final String DEFAULT_ROTATION       = "0";
  public static final String DEFAULT_PADDING        = "0";

  public CellElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  /**
   * Costruttore di una cella vuota con tutti gli attributi di default
   * 
   * @throws ValidateException
   * @throws StampaException
   */
  public CellElement(int lineNum, int colNum) throws ValidateException {
    super(null, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();

    addAttributo(ATTRIB_ROTATION, Integer.class, DEFAULT_ROTATION);
    addAttributo(ATTRIB_COLSPAN, Integer.class, DEFAULT_COLSPAN);
    addAttributo(ATTRIB_ROWSPAN, Integer.class, DEFAULT_ROWSPAN);
    addAttributo(ATTRIB_MINHEIGHT, Measure.class, null);
    addAttributo(ATTRIB_FIXEDHEIGHT, Measure.class, null);
    addAttributo(ATTRIB_BACKGROUNDCOLOR, String.class, null);

    addAttributoBorder(ATTRIB_BORDER, DEFAULT_BORDER, Border.BOX);
    addAttributoBorder(ATTRIB_BORDERLEFT, DEFAULT_BORDER, Border.LEFT);
    addAttributoBorder(ATTRIB_BORDERRIGHT, DEFAULT_BORDER, Border.RIGHT);
    addAttributoBorder(ATTRIB_BORDERTOP, DEFAULT_BORDER, Border.TOP);
    addAttributoBorder(ATTRIB_BORDERBOTTOM, DEFAULT_BORDER, Border.BOTTOM);

    addAttributo(ATTRIB_PADDING, Measure.class, DEFAULT_PADDING);
    addAttributo(ATTRIB_PADDINGLEFT, Measure.class, DEFAULT_PADDING);
    addAttributo(ATTRIB_PADDINGRIGHT, Measure.class, DEFAULT_PADDING);
    addAttributo(ATTRIB_PADDINGTOP, Measure.class, DEFAULT_PADDING);
    addAttributo(ATTRIB_PADDINGBOTTOM, Measure.class, DEFAULT_PADDING);

    addAttributo(ATTRIB_REPEAT, String.class);
  }

  @Override
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    super.loadAttributes(attrs);

    if (existAttr(ATTRIB_FIXEDHEIGHT) && existAttr(ATTRIB_MINHEIGHT)) {
      throw new ValidateException(this,
          "non si possono specificare in una cella ambedue gli attributi 'minHeight' e 'fixedHeight'.");
    }

    final String excBorder = "Lo stile 'rounded' è ammesso solo per la proprietà 'border'";
    Border b = getBorderBottom();
    if (b.getStyle() == BorderStyle.ROUNDED) {
      throw new ValidateException(this, excBorder);
    }
    b = getBorderTop();
    if (b.getStyle() == BorderStyle.ROUNDED) {
      throw new ValidateException(this, excBorder);
    }
    b = getBorderLeft();
    if (b.getStyle() == BorderStyle.ROUNDED) {
      throw new ValidateException(this, excBorder);
    }
    b = getBorderRight();
    if (b.getStyle() == BorderStyle.ROUNDED) {
      throw new ValidateException(this, excBorder);
    }
  }

  public int getRotation() {
    return getAttrValueAsInteger(ATTRIB_ROTATION).intValue();
  }

  public int getColspan() {
    return getAttrValueAsInteger(ATTRIB_COLSPAN).intValue();
  }

  public int getRowspan() {
    return getAttrValueAsInteger(ATTRIB_ROWSPAN).intValue();
  }

  public Border getBorder() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDER);
    if ( !b.hasBorder()) {
      b = getParentTable().getGridBorder();
    }
    return b;
  }

  private TableElement getParentTable() {
    IReportElement elem = getParent();
    while (elem != null && ! (elem instanceof TableElement)) {
      elem = elem.getParent();
    }

    return (TableElement) elem;
  }

  public Border getBorderLeft() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDERLEFT);
    if (b != null) {
      return b;
    }
    return getBorder();
  }

  public Border getBorderRight() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDERRIGHT);
    if (b != null) {
      return b;
    }
    return getBorder();
  }

  public Border getBorderTop() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDERTOP);
    if (b != null) {
      return b;
    }
    return getBorder();
  }

  public Border getBorderBottom() {
    Border b = getAttrValueAsBorder(ATTRIB_BORDERBOTTOM);
    if (b != null) {
      return b;
    }
    return getBorder();
  }

  public Measure getPadding() {
    Measure m = getAttrValueAsMeasure(ATTRIB_PADDING);
    if (m.isZero()) {
      m = getParentTable().getCellPadding();
    }
    return m;
  }

  public Measure getPaddingLeft() {
    Measure p = getAttrValueAsMeasure(ATTRIB_PADDINGLEFT);
    if (p.isZero()) {
      p = getParentTable().getCellPaddingLeft();
    }
    if (p.isZero()) {
      p = getPadding();
    }
    return p;
  }

  public Measure getPaddingRight() {
    Measure p = getAttrValueAsMeasure(ATTRIB_PADDINGRIGHT);
    if (p.isZero()) {
      p = getParentTable().getCellPaddingRight();
    }
    if (p.isZero()) {
      p = getPadding();
    }
    return p;
  }

  public Measure getPaddingTop() {
    Measure p = getAttrValueAsMeasure(ATTRIB_PADDINGTOP);
    if (p.isZero()) {
      p = getParentTable().getCellPaddingTop();
    }
    if (p.isZero()) {
      p = getPadding();
    }
    return p;
  }

  public Measure getPaddingBottom() {
    Measure p = getAttrValueAsMeasure(ATTRIB_PADDINGBOTTOM);
    if (p.isZero()) {
      p = getParentTable().getCellPaddingBottom();
    }
    if (p.isZero()) {
      p = getPadding();
    }
    return p;
  }

  /**
   * @return l'altezza minima della cella
   */
  public Measure getMinHeight() {
    return getAttrValueAsMeasure(ATTRIB_MINHEIGHT);
  }

  /**
   * @return il nome del colore di sfondo della cella
   */
  public String getBackgroundColor() {
    return getAttributeText(ATTRIB_BACKGROUNDCOLOR);
  }

  /**
   * @return l'altezza fissa della cella
   */
  public Measure getFixedHeight() {
    return getAttrValueAsMeasure(ATTRIB_FIXEDHEIGHT);
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      salvaStampaGruppo(stampa, gruppo);
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        if (isDebugData()) {
          stampa.debugElementOpen(this);
        }
        if ( !isAttrNull(ATTRIB_REPEAT)) {
          List<Group> groups = gruppo.getDescendantInstances(getAttributeText(ATTRIB_REPEAT));
          for (Group g : groups) {
            Cella cella = generateCella(g, stampa, padre);
            cella.fineGenerazione();
            listaElementi.add(cella);
          }
        } else {
          Cella cella = generateCella(gruppo, stampa, padre);
          cella.fineGenerazione();
          listaElementi.add(cella);
        }
        if (isDebugData()) {
          stampa.debugElementClose(this);
        }

      }
      return listaElementi;
    } catch (ResolveException e) {
      e.printStackTrace();
      throw new GenerateException(this, e, "Errore grave in generazione cella");
    }
  }

  private Cella generateCella(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    Cella cella = stampa.getFactoryElementi().creaCella(stampa, this, padre);
    for (IReportNode reportElem : c_elementiFigli) {
      List<Elemento> listaFiglio = reportElem.generate(gruppo, stampa, cella);
      for (Elemento elementoPDF : listaFiglio) {
        cella.addElement(elementoPDF);
      }
    }
    return cella;
  }

  @Override
  public void fineParsingElemento() {

  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_CELL;
  }

  /**
   * @return il nome del gruppo su cui si deve ripetere la cella
   */
  public String getRepeat() {
    return getAttributeText(ATTRIB_REPEAT);
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }

  /**
   * Ritorna l'elementa table a cui appartiene questa cella.
   * 
   * @return il tag tablet antenato più vicino oppure null se non trovato (il
   *         che non dovrebbe mai succedere!)
   */
  public TableElement getTableElement() {
    return getParentTable(getParent());
  }

  private TableElement getParentTable(IReportElement elem) {
    if (elem == null) {
      return null;
    }
    if (elem instanceof TableElement) {
      return (TableElement) elem;
    }
    return getParentTable(elem.getParent());
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
    return true;
  }
}
