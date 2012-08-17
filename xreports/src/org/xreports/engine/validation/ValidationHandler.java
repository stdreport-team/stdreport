package org.xreports.engine.validation;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SAX handler aimed to intercept and log errors
 */
public class ValidationHandler extends DefaultHandler {

  @Override
  public void error(SAXParseException e) throws SAXParseException {
    System.out.println("Errore di validazione: linea " + e.getLineNumber());
    System.out.println(e.getMessage());

    throw e;
  }

  @Override
  public void warning(SAXParseException e) throws SAXParseException {
    System.out.println("Warning: linea " + e.getLineNumber());
    System.out.println(e.getMessage());

    throw e;
  }
}
