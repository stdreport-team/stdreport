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
public class PageHeader extends PageElement {
  public PageHeader(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_PAGEHEADER;
  }

  @Override
  public boolean isHeader() {
    return true;
  }

  @Override
  public boolean isFooter() {
    return false;
  }

  @Override
  public boolean canChildren() {
    return true;
  }
}
