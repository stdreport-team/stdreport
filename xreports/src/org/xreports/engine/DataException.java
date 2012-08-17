/**
 * 
 */
package org.xreports.engine;

import org.xreports.engine.source.AbstractElement;

/**
 * @author pier
 * 
 */
public class DataException extends StampaException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1331643363553483467L;

	public DataException() {
		super();
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(Throwable t, String message) {
		super(message, t);
	}

	public DataException(Throwable t) {
		super(t);
	}

	public DataException(AbstractElement elem, Throwable cause, String message, Object... params) {
		super(cause);
		buildLocalMessage(elem, cause, message, params);
	}

	public DataException(AbstractElement elem, Throwable cause) {
		super(cause);
		buildLocalMessage(elem, cause, null);
	}

	public DataException(AbstractElement elem, String message) {
		super();
		buildLocalMessage(elem, null, message);
	}

  @Override
  protected String getExceptionType() {
    return "caricamento dati";
  }  
	
}
