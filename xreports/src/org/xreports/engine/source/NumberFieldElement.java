package org.xreports.engine.source;

import java.text.NumberFormat;
import java.util.Locale;

import org.xml.sax.Attributes;

import org.xreports.engine.XReport;
import org.xreports.engine.validation.ValidateException;
import org.xreports.util.Text;

/**
 * @author pier
 * 
 */
public class NumberFieldElement extends FieldElement {
  /** Nomi della controparte XML degli attributi del Tag "number" */
  private static final String ATTRIB_MAXDEC       = "maxdec";
  private static final String ATTRIB_MINDEC       = "mindec";
  private static final String ATTRIB_MAXINT       = "maxint";
  private static final String ATTRIB_MININT       = "minint";
  private static final String ATTRIB_SEP_MIGLIAIA = "separatoreMigliaia";
  private static final String ATTRIB_TYPE         = "type";

  private static final String TYPE_DECIMAL        = "decimal";
  private static final String TYPE_INTEGER        = "integer";
  private static final String TYPE_CURRENCY       = "currency";
  private static final String MAXDEC_DEFAULT      = "2";
  private static final String MINDEC_DEFAULT      = "0";
  private static final String MAXCUR_DEFAULT      = "2";
  private static final String MINCUR_DEFAULT      = "2";
  private static final String MAXINT_DEFAULT      = "0";
  private static final String MININT_DEFAULT      = "1";

  private NumberFormat        c_numberFormat;

  public NumberFieldElement(XReport stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_TYPE, String.class, TYPE_INTEGER);

    addAttributo(ATTRIB_MAXDEC, Integer.class, MAXDEC_DEFAULT);
    addAttributo(ATTRIB_MINDEC, Integer.class, MINDEC_DEFAULT);
    addAttributo(ATTRIB_MAXINT, Integer.class, MAXINT_DEFAULT);
    addAttributo(ATTRIB_MININT, Integer.class, MININT_DEFAULT);

    addAttributo(ATTRIB_SEP_MIGLIAIA, Boolean.class);

    c_numberFormat = NumberFormat.getNumberInstance(Locale.ITALY);
    c_numberFormat.setParseIntegerOnly(false);
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#loadAttributes(org.xml.sax.Attributes)
   */
  @Override
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    super.loadAttributes(attrs);
    /** FIXME: BUG per il tipo currency! */

    if (isAttrNull(ATTRIB_SEP_MIGLIAIA)) {
      if (getAttributeText(ATTRIB_TYPE).equalsIgnoreCase(TYPE_CURRENCY)) {
        setAttributeValue(ATTRIB_SEP_MIGLIAIA, "true");
      } else {
        setAttributeValue(ATTRIB_SEP_MIGLIAIA, "false");
      }
    }
    applyFormat();
  }

  private void applyFormat() {
    int maxdec = getAttrValueAsInteger(ATTRIB_MAXDEC);
    int mindec = getAttrValueAsInteger(ATTRIB_MINDEC);
    int maxint = getAttrValueAsInteger(ATTRIB_MAXINT);
    int minint = getAttrValueAsInteger(ATTRIB_MININT);
    String type = getAttributeText(ATTRIB_TYPE);
    if (type.equalsIgnoreCase(TYPE_DECIMAL)) {
      if (maxdec > 0) {
        c_numberFormat.setMaximumFractionDigits(maxdec);
      } else {
        c_numberFormat.setMaximumFractionDigits(Integer.parseInt(MAXDEC_DEFAULT));
      }
      if (mindec > 0) {
        c_numberFormat.setMinimumFractionDigits(mindec);
      } else {
        c_numberFormat.setMinimumFractionDigits(Integer.parseInt(MINDEC_DEFAULT));
      }
    } else if (type.equalsIgnoreCase(TYPE_CURRENCY)) {
      if (maxdec > 0) {
        c_numberFormat.setMaximumFractionDigits(maxdec);
      } else {
        c_numberFormat.setMaximumFractionDigits(Integer.parseInt(MAXCUR_DEFAULT));
      }
      if (mindec > 0) {
        c_numberFormat.setMinimumFractionDigits(mindec);
      } else {
        c_numberFormat.setMinimumFractionDigits(Integer.parseInt(MINCUR_DEFAULT));
      }
    } else if (type.equalsIgnoreCase(TYPE_INTEGER)) {
      c_numberFormat.setParseIntegerOnly(true);
    }
    if (maxint > 0) {
      c_numberFormat.setMaximumIntegerDigits(maxint);
    }
    if (minint > 0) {
      c_numberFormat.setMinimumIntegerDigits(minint);
    }
    c_numberFormat.setGroupingUsed(getAttrValueAsBoolean(ATTRIB_SEP_MIGLIAIA));
  }

  @Override
  protected String getFormattedValue(Object value) {
    if (value == null) {
      String def = getDefaultValueIfNull();
      if (Text.isNumeric(def)) {
        if (isIntero()) {
          value = Text.toInteger(def);
        } else {
          value = new Double(Text.toDouble(def, 0.0));
        }
      } else {
        //nel caso il valore di default per null non sia intero, lo emetto così com'è
        return def;
      }
    }
    return c_numberFormat.format(value);
  }

  @Override
  protected boolean isNumberField() {
    return true;
  }

  @Override
  public String getTagName() {
    return "number";
  }

}
