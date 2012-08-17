/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Chart;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;

/**
 * @author pier
 * 
 */
public class ChartElement extends AbstractElement {
  /** Nomi degli attributi di questo tag */
  private static final String ATTRIB_HEIGHT          = "height";
  private static final String ATTRIB_WIDTH           = "width";
  private static final String ATTRIB_TYPE            = "type";
  private static final String ATTRIB_TITLE           = "title";
  private static final String ATTRIB_SUBTITLE        = "subtitle";
  private static final String ATTRIB_X               = "x";
  private static final String ATTRIB_Y               = "y";
  private static final String ATTRIB_VALUE           = "value";
  private static final String ATTRIB_CATEGORY        = "category";
  private static final String ATTRIB_SERIE           = "serie";
  private static final String ATTRIB_GROUP           = "group";
  private static final String ATTRIB_SHOWLEGEND      = "showLegend";
  private static final String ATTRIB_SHOWLABEL       = "showLabel";
  private static final String ATTRIB_LEGENDTEXT      = "legendText";
  private static final String ATTRIB_LABELTEXT       = "labelText";
  private static final String ATTRIB_DOMAINTEXT      = "domainText";
  private static final String ATTRIB_VALUESTEXT      = "valuesText";
  private static final String ATTRIB_ORIENTATION     = "barOrientation";
  private static final String ATTRIB_BACKGROUNDCOLOR = "backgroundColor";
  private static final String ATTRIB_MAXCATEGORIES   = "maxCategories";
  private static final String ATTRIB_MINCATEG_VAL    = "minCategoryValue";
  private static final String ATTRIB_SUMOTHER        = "sumOther";
  private static final String ATTRIB_OTHERLABEL      = "otherLabel";

  public enum GraphType {
    bar, bar3D, pie, pie3D, line, line3D
  }

  public ChartElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_WIDTH, Measure.class, "8cm");
    addAttributo(ATTRIB_HEIGHT, Measure.class, "6cm");
    addAttributo(ATTRIB_X, Measure.class, "0");
    addAttributo(ATTRIB_Y, Measure.class, "0");
    addAttributo(ATTRIB_TYPE, String.class, GraphType.pie.toString());
    addAttributo(ATTRIB_GROUP, String.class);
    addAttributo(ATTRIB_SHOWLEGEND, Boolean.class, "false");
    addAttributo(ATTRIB_SHOWLABEL, Boolean.class, "true");
    addAttributo(ATTRIB_LEGENDTEXT, String.class, "{0}");
    addAttributo(ATTRIB_LABELTEXT, String.class, "{2}");
    addAttributo(ATTRIB_DOMAINTEXT, String.class, "");
    addAttributo(ATTRIB_VALUESTEXT, String.class, "");
    addAttributo(ATTRIB_ORIENTATION, String.class, "");
    addAttributo(ATTRIB_BACKGROUNDCOLOR, String.class, "");
    addAttributoMeasure(ATTRIB_MARGIN_BOTTOM, null, false, false);
    addAttributoMeasure(ATTRIB_MARGIN_TOP, null, false, false);

    addAttributo(ATTRIB_VALUE, String.class, null, TAG_VALUE);
    addAttributo(ATTRIB_CATEGORY, String.class, null, TAG_VALUE);
    addAttributo(ATTRIB_SERIE, String.class, null, TAG_VALUE);

    addAttributo(ATTRIB_TITLE, String.class, null, TAG_TEXT);
    addAttributo(ATTRIB_SUBTITLE, String.class, null, TAG_TEXT);
    addAttributo(ATTRIB_MAXCATEGORIES, Number.class, "0", TAG_VALUE);
    addAttributo(ATTRIB_MINCATEG_VAL, Number.class, null, TAG_VALUE);
    addAttributo(ATTRIB_SUMOTHER, Boolean.class, "false");
    addAttributo(ATTRIB_OTHERLABEL, String.class, "");
  }

  @Override
  public void fineParsingElemento() {
  }

  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    try {
      if (isDebugData()) {
        System.out.println("[STRUTTURA] " + this.toString());
      }
      salvaStampaGruppo(stampa, gruppo);
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        Chart chart = stampa.getFactoryElementi().creaChart(stampa, this, padre);
        chart.fineGenerazione();
        listaElementi.add(chart);
      }
      if (isDebugData()) {
        System.out.println("[STRUTTURA] >> FINE " + this.toString());
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  /**
   * Restituisce a lunghezza in punti definita per il grafico.
   */
  public float getHeight() {
    //return attrib_height.getValue();
    Measure m = getAttrValueAsMeasure(ATTRIB_HEIGHT);
    return m != null ? m.getValue() : null;
  }

  /**
   * Restituisce il valore dell'attributo <code>maxCategories</code>. E' la
   * quantità massima di categorie da visualizzare
   * 
   * @throws EvaluateException
   * @throws ResolveException 
   */
  public int getMaxCategories() throws ResolveException {
    Number i = getExpressionAsNumber(ATTRIB_MAXCATEGORIES);
    if (i != null)
      return i.intValue();

    return 0;
  }

  /**
   * Restituisce il valore dell'attributo <code>minCategoryValue</code>. E' il
   * valore minimo che deve avere una categoria per essere visualizzata.
   * 
   * @throws EvaluateException
   */
  public double getMinCategoryValue() throws ResolveException {
    Number i = getExpressionAsNumber(ATTRIB_MINCATEG_VAL);
    if (i != null)
      return i.doubleValue();

    return Double.MIN_VALUE;
  }

  /**
   * Ritorna il valore dell'attributo <code>sumOther</code>. Indica se devono
   * essere sommati in una categoria a parte le categorie escluse da
   * maxCategories e/o minCategoryValue.
   */
  public boolean isSumOther() {
    return getAttrValueAsBoolean(ATTRIB_SUMOTHER);
  }

  /**
   * Ritorna il valore dell'attributo <code>sumOther</code>. Indica se devono
   * essere sommati in una categoria a parte le categorie escluse da
   * maxCategories e/o minCategoryValue.
   */
  public String getOtherLabel() {
    Object l = getAttrValue(ATTRIB_OTHERLABEL);
    if (l != null)
      return l.toString();
    return null;
  }

  /**
   * Restituisce a larghezza in punti definita per il grafico.
   * 
   */
  public float getWidth() {
    //return attrib_width.getValue();
    Measure m = getAttrValueAsMeasure(ATTRIB_WIDTH);
    return m != null ? m.getValue() : null;
  }

  /**
   * @return il tipo del grafico
   */
  public GraphType getType() {
    //return GraphType.valueOf(attrib_type);
    return GraphType.valueOf(getAttributeText(ATTRIB_TYPE));
  }

  /**
   * @return the x
   */
  public float getX() {
    Measure m = getAttrValueAsMeasure(ATTRIB_X);
    return m != null ? m.getValue() : null;
  }

  /**
   * @return the y
   */
  public float getY() {
    Measure m = getAttrValueAsMeasure(ATTRIB_Y);
    return m != null ? m.getValue() : null;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_CHART;
  }

  /**
   * @return il c_valueTree
   */
  public Symbol getValueTree() {
    //return c_valueTree;
    return getAttrSymbol(ATTRIB_VALUE);
  }

  /**
   * @return il c_categoryTree
   */
  public Symbol getCategoryTree() {
    //return c_categoryTree;
    return getAttrSymbol(ATTRIB_CATEGORY);
  }

  public Symbol getSerieTree() {
    //return c_categoryTree;
    return getAttrSymbol(ATTRIB_SERIE);
  }

  /**
   * @return il gruppo su cui ciclare per i valori del grafico
   */
  public String getChartGroup() {
    //return attrib_group;
    return getAttributeText(ATTRIB_GROUP);
  }

  /**
   * @return titolo del grafico
   * @throws EvaluateException
   */
  public String getTitle() throws EvaluateException {
    Symbol s = getAttrSymbol(ATTRIB_TITLE);
    if (s != null)
      return String.valueOf(s.evaluate(this));
    return null;
  }

  /**
   * 
   * @return la desccrizione dell'asse orizzontale (solo per bar chart)
   */
  public String getDomainText() {
    return getAttributeText(ATTRIB_DOMAINTEXT);
  }

  /**
   * 
   * @return la desccrizione dell'asse verticale (solo per bar chart)
   */
  public String getValuesText() {
    return getAttributeText(ATTRIB_VALUESTEXT);
  }

  /**
   * 
   * @return il testo da mettere nella legenda
   */
  public String getLegendText() {
    return getAttributeText(ATTRIB_LEGENDTEXT);
  }

  /**
   * 
   * @return il testo da mettere nell'etichetta associata alle singole categorie
   */
  public String getLabelText() {
    return getAttributeText(ATTRIB_LABELTEXT);
  }

  /**
   * 
   * @return true se si deve visualizzare la legenda delle categorie sotto il
   *         grafico
   */
  public boolean isShowLegend() {
    return getAttrValueAsBoolean(ATTRIB_SHOWLEGEND).booleanValue();
  }

  public boolean isBarOrientationVertical() {
    return getAttributeText(ATTRIB_ORIENTATION).equalsIgnoreCase("verticale");
  }

  /**
   * 
   * @return true se si deve visualizzare l'etichetta dei valori delle categorie
   */
  public boolean isShowLabel() {
    return getAttrValueAsBoolean(ATTRIB_SHOWLABEL).booleanValue();
  }

  /**
   * @return sotto-titolo del grafico
   * @throws EvaluateException
   */
  public String getSubtitle() throws EvaluateException {
    Symbol s = getAttrSymbol(ATTRIB_SUBTITLE);
    if (s != null)
      return String.valueOf(s.evaluate(this));
    return null;
  }

  public String getBackgroundColor() {
    return getAttributeText(ATTRIB_BACKGROUNDCOLOR);
  }

  /**
   * @return margine inferiore
   */
  public float getMarginBottom() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_BOTTOM);
    if (f != null) {
      return f.floatValue();
    }

    return 0f;
  }

  /**
   * @return margine Superiore
   */
  public float getMarginTop() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_TOP);
    if (f != null) {
      return f.floatValue();
    }
    return 0f;
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
    return true;
  }

  /*
   * (non-Javadoc)
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
