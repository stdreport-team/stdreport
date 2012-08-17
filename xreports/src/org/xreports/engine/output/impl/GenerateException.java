/**
 * 
 */
package org.xreports.engine.output.impl;

import org.xreports.engine.StampaException;
import org.xreports.engine.source.AbstractElement;

/**
 * @author pier
 *
 */
public class GenerateException extends StampaException {
  private static final long serialVersionUID = -1518635593868259092L;

  
  public GenerateException() {
		super();
	}

	public GenerateException(String message) {
		super(message);
	}

  public GenerateException(String format, Object... args) {
    super(String.format(format, args));
  }
	
  public GenerateException(Throwable cause, String message) {
    super(cause, message);
  }

  public GenerateException(Throwable cause) {
    super(cause);
  }
  
  public GenerateException(AbstractElement elem, String message) {
    this(elem, null, message);
  }

  public GenerateException(AbstractElement elem, Throwable cause, String message) {
    super(cause);
    buildLocalMessage(elem, cause, message);
  }

  public GenerateException(AbstractElement elem, Throwable cause) {
    super(cause);
    buildLocalMessage(elem, cause, null);
  }

  @Override
  protected String getExceptionType() {
    return "generazione";
  }  
}
