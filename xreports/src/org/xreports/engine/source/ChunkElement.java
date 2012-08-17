package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.validation.ValidateException;

/**
 * Elemento virtuale che ha il solo scopo di raggruppare le caratteristiche comuni a {@link SpanElement},
 * {@link FieldElement}, {@link NumberFieldElement} e {@link DateFieldElement}.
 * @author pier
 *
 */
public abstract class ChunkElement extends BookmarkableElement {
  public static final String ATTRIB_HSCALE          = "hscale";
  public static final String ATTRIB_BACKGROUNDCOLOR = "backgroundColor";
  public static final String ATTRIB_CHARSPACING     = "charSpacing";
  public static final String ATTRIB_Y               = "y";

  public ChunkElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_HSCALE, Float.class, "1");
    addAttributoMeasure(ATTRIB_CHARSPACING, "0", false, false);
    addAttributoColore(ATTRIB_BACKGROUNDCOLOR, null);
    addAttributoMeasure(ATTRIB_Y, null, false, true);
  }

  public float getHorizScale() {
    return getAttrValueAsFloat(ATTRIB_HSCALE);
  }

  public float getCharSpacing() {
    return getAttrValueAsMeasure(ATTRIB_CHARSPACING).getValue();
  }
  
  /**
   * @return lo scostamento verticale dalla linea di base
   */
  public Measure getY() {
    return getAttrValueAsMeasure(ATTRIB_Y);
  }

  /**
   * @return il nome del colore di sfondo dello span
   */
  public Colore getBackgroundColor() {
    return getAttrValueAsColore(ATTRIB_BACKGROUNDCOLOR);
  }

}
