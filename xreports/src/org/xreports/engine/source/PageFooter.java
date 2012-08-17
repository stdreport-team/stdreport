/**
 * 
 */
package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

/**
 * @author pier
 * 
 */
public class PageFooter extends PageElement {
  public static final String DEFAULT_VALIGN = "top";

  public PageFooter(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_PAGEFOOTER;
  }

  @Override
  public boolean isHeader() {
    return false;
  }

  @Override
  public boolean isFooter() {
    return true;
  }

  @Override
  public boolean canChildren() {
    return true;
  }
}
