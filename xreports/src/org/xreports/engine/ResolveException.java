package org.xreports.engine;

import org.xreports.engine.source.AbstractElement;

/**
 * Tipo di eccezione innescata per errori in valutazione espressioni durante
 * l'esecuzione del report.
 * @author pier
 *
 */
public class ResolveException extends StampaException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ResolveException() {
    super();
  }
  public ResolveException(Throwable cause) {
    super(cause);
  }

  public ResolveException(String message) {
    super(message);
  }

  public ResolveException(String message, Object... params) {
    super(String.format(message, params));
  }
  
  public ResolveException(Throwable cause, String message, Object... params) {
    super(cause, String.format(message, params));
  }

  
  public ResolveException(AbstractElement elem, Throwable cause, String message, Object... params) {
    super(cause);
    buildLocalMessage(elem, cause, message, params);
  }
  
  public ResolveException(AbstractElement elem, Throwable cause) {
    super(cause);
    buildLocalMessage(elem, cause, null);
  }
  
  public ResolveException(AbstractElement elem, String message) {
    super();
    buildLocalMessage(elem, null, message);
  }

  public ResolveException(AbstractElement elem, String message, Object... params) {
    super();
    buildLocalMessage(elem, null, message, params);
  }
  
  @Override
  protected String getExceptionType() {
    return "resolve";
  }  
  
}
