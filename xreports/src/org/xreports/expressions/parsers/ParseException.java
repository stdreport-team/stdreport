package org.xreports.expressions.parsers;

import org.xreports.expressions.lexer.Token;

/**
 * Classe che rappresenta le eccezioni che possono avvenire durante il parsing.
 * <p>Copyright:    <b>CIS Coop<sup><font size=-2>TM</font></sup>&nbsp;2001-2002</b>
 * @author CISCoop - Pier
 * @version 1.0
 */

public class ParseException extends Exception {
	/** Per serializzazione */
	private static final long serialVersionUID = -4921151835534509625L;
	private String m_szMessage;
	private int m_nPosition = -1;
  private String m_outMessage;
  private Token m_unexpectedToken;


	public ParseException() {
		super();
		m_szMessage = null;
		buildMessage();
	}

	public ParseException(String s) {
		this(s, -1);
	}

	public ParseException(String s, int position) {
		this(s, position, null);
	}

  public ParseException(String s, int position, Throwable cause) {
    super(s, cause);
    m_szMessage = s;
    m_nPosition = position ;
    buildMessage();
  }

  public ParseException(String s, Token unexpectedToken) {
    super(s);
    m_szMessage = s;
    m_nPosition = unexpectedToken.getStartPosition();
    m_unexpectedToken = unexpectedToken;
    buildMessage();
  }

  public ParseException(Token unexpectedToken) {
    this(null, unexpectedToken);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage() {
    return m_outMessage;
  }

  private void buildMessage() {
    StringBuffer out;
    if (m_unexpectedToken == null) {
      out = new StringBuffer("Errore di sintassi non precisato");
    }
    else {
      out  = new StringBuffer("'" + m_unexpectedToken.getValue() + "' imprevisto");
    }
    if (m_nPosition >= 0) {
      out.append(" a posizione " + m_nPosition); 
    }
    if ((this.m_szMessage!= null) && (this.m_szMessage.length() > 0)) {
      out.append(": " + m_szMessage);
    }
    if (getCause() != null) {
      out.append(" (causato da " + getCause().toString() + ")");      
    }
    m_outMessage = out.toString();
  }
  
	/**
	 * Posizione (0-based) del punto in cui è avvenuto l'errore sintattico (in cui 
	 * inizia il token errato).
	 * @return indice del carattere nel testo di input che ha causato l'errore sintattico
	 */
  public int getPosition() {
    return m_nPosition;
  }
}