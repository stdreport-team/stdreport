/**
 * 
 */
package org.xreports.stampa;

import org.xreports.stampa.source.AbstractElement;

/**
 * @author pier
 * 
 */
public class StampaException extends Exception {
  private static final long serialVersionUID = -1518635593868259092L;

  private String c_localMessage;
  
  public StampaException() {
    super();
  }

  public StampaException(String message) {
    super(message);
  }

  public StampaException(String message, Object... params) {
    super(String.format(message, params));
  }
  
  public StampaException(Throwable cause, String message) {
    super(message, cause);
  }

  public StampaException(Throwable cause) {
    super(cause);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage() {
    if (c_localMessage == null) {
      return super.getMessage();      
    }
    return c_localMessage;
  }

  
  protected void buildLocalMessage(AbstractElement elem, Throwable cause, String message, Object... params) {
    String source = null;
    try {
      source = elem.getStampa().getReportSource().getAbsolutePath();
    } catch (Exception exx) {}
    if (source==null) {
      source = "";
    }
    else {
      source = "Sorgente " + source + ": ";
    }
    String text = source + "errore grave in " + getExceptionType() + " " + elem.toString();    
    if (message != null) {
      if (params != null)
        message = String.format(message, params);
      text += ": " + message;
    }    
    if (cause != null) {
      text += " (causato da " + cause.toString() + ")";
    }    
    setLocalMessage(text);
  }
  
  /**
   * Ritorna il nome user-friendly di questa eccezione.
   */
  protected String getExceptionType() {
    return "stampa";
  }
    
  
  /**
   * Ritorna il valore del messaggio locale.
   * Il messaggio locale è il messaggio personalizzato impostato dalla
   * exception nel costruttore. Se non è impostato, si userà
   * il messaggio standard {@link #getMessage()}.
   * @return
   */
  public String getLocalMessage() {
    return c_localMessage;
  }

  /**
   * Imposta il messaggio locale di questa exception
   * @param localMessage testo messaggio
   * 
   * @see #getLocalMessage()
   */
  protected void setLocalMessage(String localMessage) {
    c_localMessage = localMessage;
  }
  
}
