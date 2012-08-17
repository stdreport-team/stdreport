/**
 * 
 */
package org.xreports.engine.validation;

import org.xreports.engine.*;
import org.xreports.engine.source.AbstractElement;

/**
 * @author pier
 * 
 */
public class ValidateException extends StampaException {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public ValidateException() {
    super();
  }

  public ValidateException(String message) {
    super(message);
  }

  public ValidateException(String message, Object... params) {
    super(String.format(message, params));
  }
  
  public ValidateException(Throwable cause, String message, Object... params) {
    super(cause, String.format(message, params));
  }

  
  public ValidateException(AbstractElement elem, Throwable cause, String message, Object... params) {
    super(cause);
    buildLocalMessage(elem, cause, message, params);
  }
  
  public ValidateException(AbstractElement elem, Throwable cause) {
    super(cause);
    buildLocalMessage(elem, cause, null);
  }
  
  public ValidateException(AbstractElement elem, String message) {
    super();
    buildLocalMessage(elem, null, message);
  }

  public ValidateException(AbstractElement elem, String message, Object... params) {
    super();
    buildLocalMessage(elem, null, message, params);
  }
  
  @Override
  protected String getExceptionType() {
    return "validazione";
  }  
  
}
