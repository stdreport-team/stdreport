/**
 * 
 */
package org.xreports.engine.source;

import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;

import org.xreports.stampa.Stampa;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

/**
 * @author pier
 * 
 */
public class DateFieldElement extends FieldElement {
  /** Nomi della controparte XML degli attributi dell'elemento "Date" */
  private static final String ATTRIB_FORMATTER = "format";
  private static final String DEFAULT_FORMAT   = "dd/MM/yyyy";

  private SimpleDateFormat    c_dateFormat;

  public DateFieldElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_FORMATTER, String.class, DEFAULT_FORMAT);
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#loadAttributes(org.xml.sax.Attributes)
   */
  @Override
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    super.loadAttributes(attrs);
    c_dateFormat = new SimpleDateFormat(getAttributeText(ATTRIB_FORMATTER));
  }

  @Override
  protected String getFormattedValue(Object value) {
    if (value == null) {
      return getDefaultValueIfNull();
    }
    return c_dateFormat.format(value);
  }

  @Override
  protected boolean isDateField() {
    return true;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_DATE;
  }

}
