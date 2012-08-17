package org.xreports.expressions.lexer;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.util.Text;

/**
 * Classe che rappresenta il singolo token ritornato dalla classe
 * <code>SQLLex</code> come elemento atomico dell'analisi lessicale.
 * <p>
 * Copyright: <b>CIS Coop<sup><font size=-2>TM</font></sup>&nbsp;2001-2002</b>
 * 
 * @author CISCoop - Pier
 * @version 1.0
 */

public class Token {

  /**
   * valore del token, per alcuni tipi di token potrebbe essere diverso da
   * {@link #m_tokenSourceVal}, normalmente è uguale
   */
  private String          m_tokenVal;

  /** testo del token come appare nel sorgente di input */
  private String          m_tokenSourceVal;
  /**
   * Tipo di questo token. E' una delle costanti TT_... definite in
   * {@link Lexer}
   **/
  private Lexer.TokenType m_tokenType;
  /**
   * Posizione del carattere iniziale del token all'interno della stringa di
   * analisi.
   **/
  private int             m_tokenPos;

  /**
   * Posizione del carattere finale del token all'interno della stringa di
   * analisi.
   **/
  private int             m_tokenEndPos;

  /**
   * tag user-defined per aggiungere attributi a questo token. La fase di
   * parsing può arricchire i token di significati particolari, determinabili
   * solo durante la fase di parsing. Gestito totalmente fuori dall'analisi
   * lessicale, il tag di un token per il Lexer non essite nemmeno.
   */
  private String          m_tag;

  Token() {
    m_tokenVal = "";
    m_tokenType = TokenType.NONE;
  }

  Token(String tokVal, TokenType tokType) {
    m_tokenVal = tokVal;
    m_tokenType = tokType;
  }

  Token(String tokVal, TokenType tokType, int tokPos) {
    m_tokenVal = tokVal;
    m_tokenType = tokType;
    m_tokenPos = tokPos;
  }

  /**
   * Restituisce una copia di questo token.
   * 
   * @return copia di <em>this</em>
   */
  public Token copy() {
    Token other = new Token(m_tokenVal, m_tokenType, m_tokenPos);
    other.setEndPosition(getEndPosition());
    other.setTokenSourceVal(getTokenSourceVal());
    other.setTag(getTag());
    return other;
  }

  /**
   * Ritorna una descrizione standard del token, comprendente il valore e la
   * descrizione del tipo.
   * 
   * @return Una stringa del tipo <strong><tt>Token '<i>valore</i>', tipo=<i>desc.tipo</i></strong>
   */
  public String toString() {
    StringBuffer out = new StringBuffer(100);
    out.append("Token '" + this.m_tokenVal + "', tipo=" + m_tokenType.getDesc());
    return out.toString();
  }

  /**
   * Valore corrente del token
   * 
   * @return la stringa che costituisce il valore del token
   */
  public String getValue() {
    return m_tokenVal;
  }

  void setValue(String aValue) throws LexerException {
    m_tokenVal = aValue;

    if (m_tokenType == TokenType.String) {
      normalizeStringLiteral();
    }
  }

  public boolean isOneOf(String... tokValues) {
    for (String val : tokValues)
      if (val.equalsIgnoreCase(getValue()))
        return true;
    return false;
  }

  public boolean isOneOf(TokenType... typeValues) {
    for (TokenType type : typeValues)
      if (type == m_tokenType)
        return true;
    return false;
  }

  /**
   * Confronta il valore di questo token con la stringa passata.
   * 
   * @param tokVal
   *          stringa con cui confrontare questo token
   * @return <tt>true</tt> se e solo se il token corrente è uguale alla stringa
   *         passata, ignorando maiuscole-minuscole.
   * @see #is(String, int)
   * @see #is(int)
   */
  public boolean is(String tokVal) {
    return tokVal.equalsIgnoreCase(this.m_tokenVal);
  }

  /**
   * Confronta il valore di questo token con il carattere passato.
   * 
   * @param tokVal
   *          stringa con cui confrontare questo token
   * @return <tt>true</tt> se e solo se il token corrente è uguale al carattere
   *         passato, ignorando maiuscole-minuscole.
   * @see #is(String)
   * @see #is(String, int)
   * @see #is(int)
   */
  public boolean is(char tokVal) {
    return String.valueOf(tokVal).equalsIgnoreCase(this.m_tokenVal);
  }

  /**
   * Confronta il valore di questo token con la stringa passata e il tipo con il
   * tipo passato.
   * 
   * @param tokVal
   *          stringa con cui confrontare il valore di questo token
   * @param tokType
   *          tipo con cui confrontare il tipo di questo token
   * @return <tt>true</tt> se e solo se il token corrente è uguale alla stringa
   *         passata, ignorando maiuscole-minuscole e dello stesso tipo del tipo
   *         passato.
   * @see #is(String)
   * @see #is(int)
   */
  public boolean is(String tokVal, TokenType tokType) {
    return (tokType == m_tokenType) && is(tokVal);
  }

  /**
   * Confronta il tipo di questo token con il tipo passato.
   * 
   * @param tokType
   *          tipo con cui confrontare il tipo di questo token
   * @return <tt>true</tt> se e solo se il token corrente è dello stesso tipo
   *         del tipo passato.
   * @see #is(String, int)
   * @see #is(String)
   */
  public boolean is(TokenType tokType) {
    return (tokType == m_tokenType);
  }

  /**
   * Ritorna il tipo di questo token.
   * 
   * @return vedi sopra
   */
  public TokenType getType() {
    return m_tokenType;
  }

  /**
   * @param tokenType
   *          the tokenType to set
   * @throws LexerException 
   */
  void setTokenType(Lexer.TokenType tokenType) throws LexerException {
    m_tokenType = tokenType;
    if (m_tokenType == TokenType.String) {
      normalizeStringLiteral();
    }
  }

  /**
   * Posizione del carattere iniziale di questo token nel testo sorgente
   * 
   * @return posizione token (1-based)
   */
  public int getStartPosition() {
    return m_tokenPos;
  }

  /**
   * Posizione del carattere finale di questo token nel testo sorgente
   * 
   * @return posizione del carattere finale del token nella stringa di input (0-based)
   */
  public int getEndPosition() {
    return m_tokenEndPos;
  }

  void setEndPosition(int endPos) {
    m_tokenEndPos = endPos;
  }

  /**
   * Imposta la posizione del carattere iniziale di questo token nel testo sorgente
   * @param p_tokenPos posizione iniziale 0-based
   */
  void setTokenPos(int p_tokenPos) {
    m_tokenPos = p_tokenPos;
  }

  private void normalizeStringLiteral() throws LexerException {
    StringBuilder sb = new StringBuilder();
    if (m_tokenVal != null && m_tokenVal.length() > 0) {
      char delimiter = m_tokenVal.charAt(0);
      if (delimiter != '\'' && delimiter != '"') {
        //già normalizzato, non faccio nulla
        return;
      }
      //sb.append(delimiter);
      for (int i = 1; i < m_tokenVal.length() - 1; i++) {
        char c = m_tokenVal.charAt(i);
        char cNext = m_tokenVal.charAt(i + 1);
        if (c == '\\') {
          if (cNext == '\\' || cNext == delimiter) {
            sb.append(cNext);
            i++;
          } else {
            throw new LexerException("String literal " + m_tokenVal + ": carattere \\ errato a posizione " + i);
          }
        } else {
          sb.append(c);
        }
      }
      //sb.append(delimiter);
      m_tokenVal = sb.toString();
    }
  }

  /**
   * Testo del token come appare nel sorgente di input. Per alcuni tipi di token
   * è diverso da {@link #getValue()} (ad esempio StringLiteral).
   * 
   * @return Testo del token come appare nel sorgente di input
   */
  public String getTokenSourceVal() {
    return m_tokenSourceVal;
  }

  void setTokenSourceVal(String sourceVal) {
    m_tokenSourceVal = sourceVal;
  }
  
  /**
   * tag user-defined per aggiungere attributi a questo token. La fase di
   * parsing può arricchire i token di significati particolari, determinabili
   * solo durante la fase di parsing. Gestito totalmente fuori dall'analisi
   * lessicale, il tag di un token per il Lexer non essite nemmeno.
   * @return informazioni aggiuntive su questo token, aggiunte con la {@link #setTag(String)}
   */
  public String getTag() {
    return m_tag;
  }

  /**
   * Permette di impostare un tag per questo token.
   * @param tag testo del tag
   * @see #getTag()
   */
  public void setTag(String tag) {
    m_tag = tag;
  }
  
  public boolean isWithTag(String tag) {
    return Text.isValue(tag, m_tag, true);
  }
}
