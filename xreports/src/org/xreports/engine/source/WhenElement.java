/**
 * 
 */
package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;

/**
 * @author pier
 * 
 */
public class WhenElement extends IfElement {

  public WhenElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    parseAttributes(attrs);
  }

  private void parseAttributes(Attributes attrs) {

  }

  @Override
  public void fineParsingElemento() {

  }

  @Override
  public String getXMLAttrs() {
    String szXMLAttrs = super.getXMLAttrs();

    return szXMLAttrs;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_WHEN;
  }

}
