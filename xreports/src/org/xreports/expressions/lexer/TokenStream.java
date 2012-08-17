package org.xreports.expressions.lexer;

import java.util.ArrayList;
import java.util.List;

import org.xreports.expressions.lexer.Lexer.TokenType;


public class TokenStream {

	private List<Token>	m_tokenList;
	private Lexer				m_lexer;
	private String			m_inputSource;
	private int					m_currentIndex;
	private int					m_maxIndex;

  /** token standard ritornato quando si arriva a fine input: è <b>sempre</b> l'ultimo token dello stream */
  public static Token     ENDOFTEXT = new Token("", TokenType.EndOfText);
  /** token standard ritornato quando si richiede un token precedente al primo (vedi # getPreviousToken) */
  public static Token     STARTOFTEXT = new Token("", TokenType.NONE);

	
	/**
	 * Costruttore con specifica dell'input.
	 * @param sourceText testo che l'analizzatore lessicale dovrà scandire
	 * con la successiva {@link #open()}.
	 */
	public TokenStream(String sourceText) {
		m_inputSource = sourceText;
	}

  /**
   * Costruttore con specifica dell'input.
   * @param sourceText testo che l'analizzatore lessicale dovrà scandire
   * con la successiva {@link #open()}.
   */
  private TokenStream(List<Token> list) {
    m_tokenList = list;
    m_maxIndex = m_tokenList.size() - 1;
  }
	
	/**
	 * Indica se il TokenStrem è stato aperto.
	 * @return true sse è stata chiamata la {@link #open()} ma non una successiva {@link #close()}.
	 */
	public boolean isOpened() {
		return m_tokenList != null;
	}
	
	/**
	 * Chiude lo stream.
	 * Dopo la close nessun token è più disponibile e {@link #getSize()} torna 0.
	 */
	public void close() {
		if (isOpened()) {
			m_tokenList = null;
			m_lexer = null;			
		}
	}

  /**
   * Se lo stream è chiuso, lo apre semplicemente (vedi {@link #open()}),
   * altrimenti riporta la posizione all'inizio come dopo appena fatta la open.
   * @throws LexerException  in caso di errori imprevisti
   */
  public void restart() throws LexerException {
    if (isOpened()) {
      m_currentIndex = 0;
    }
    else {
      open();
    }
  }

  /**
   * Ritorna uno stream di token derivato da questo, a partire dal token 
   * corrente.
   * 
   * @return nuovo stream di token: viene copiato questo stream dal token corrente
   * fino alla fine
   * @throws LexerException se lo stream è chiuso
   */
  public TokenStream getSubStream() throws LexerException {
    return getSubStream(m_currentIndex);
  }
  
  
  /**
   * Ritorna uno stream di token derivato da questo, precisamente prendendo il token 
   * alla posizione passata e arrivando fino alla fine.
   * @param position posizione (0-based) del token da cui deve iniziare il sub-stream.
   * @return nuovo stream di token: viene copiato questo stream da token a posizione <i>i</i>
   * fino alla fine
   * @throws LexerException se l'indice di partenza è errato o lo stream è chiuso
   */
  public TokenStream getSubStream(int position) throws LexerException {
    if (!isOpened()) {
      throw new LexerException("getSubStream: stream chiuso");      
    }
    if (position < 0 || position > m_maxIndex) {
      throw new LexerException("posizione stream errata: " + position);
    }
    
    List<Token> sublist = new ArrayList<Token>();
    for (int i=position; i < m_tokenList.size(); i++) {
      sublist.add(m_tokenList.get(i).copy());
    }
    
    TokenStream ts = new TokenStream(sublist);
    return ts;
  }
  
  
  
	/**
	 * Apre lo strem, in modo da poter accedere all'elenco dei token.
	 * @throws LexerException in caso di errori imprevisti
	 */
	public void open() throws LexerException {
		try {
			m_lexer = new Lexer();
			m_lexer.setIDMode(Lexer.ID_DELIM_SQBRK);
			m_lexer.setText(m_inputSource);
			m_tokenList = new ArrayList<Token>();
			m_lexer.nextToken();
			while (!m_lexer.getCurrentToken().is(TokenType.EndOfText)) {
				m_tokenList.add(m_lexer.getCurrentToken().copy());
				m_lexer.nextToken();
			}
			m_currentIndex = 0;
			m_tokenList.add(ENDOFTEXT);
			m_maxIndex = m_tokenList.size() - 1;
		} catch (Exception e) {
			m_tokenList = null;
			m_lexer = null;
			throw new LexerException("Errore imprevisto in apertura TokenStream", e);
		}
	}

	/**
	 * Se lo stream è aperto, ritorna la quantità di token presenti.
	 * <br/>
	 * <b>Attenzione</b>: alla fine dello stream viene sempre aggiunto il token speciale
	 * {@link TokenType#EndOfText}, quindi questo metodo torna sempre la quantità di token 
	 * reali + 1.
	 * @return quantità di token presenti nello stream.
	 * @throws LexerException nel caso che lo stream sia chiuso
	 */
	public int getSize() throws LexerException {
	  if (!isOpened()) {
      throw new LexerException("TokenStream chiuso");	    
	  }
	  return m_tokenList.size();
	}

	/**
	 * Ritorna la posizione (0-based) del token corrente. All'inizio
	 * e dopo una {@link #restart()} la posizione è 0.
	 * 
	 * @return  posizione (0-based) del token corrente
	 * 
	 * @throws LexerException nel caso che lo stream sia chiuso
	 */
  public int getCurrentPosition() throws LexerException {
    if (!isOpened()) {
      throw new LexerException("TokenStream chiuso");     
    }
    return m_currentIndex;
  }
	
	
	/**
	 * Sposta in avanti il puntatore al token corrente e ne ritorna il token corrispondente.
	 * @return token presente alla posizione successiva.
	 * @throws LexerException nel caso lo stream sia chiuso o si sia già raggiunta la fine 
	 * dello stream
	 * @see #isEndOfStream()
	 * @see #isOpened()
	 */
	public Token moveNext() throws LexerException {
		if (!isOpened()) {
			throw new LexerException("TokenStream chiuso");
		}
		if (m_currentIndex < m_maxIndex) {
			m_currentIndex++;
			return m_tokenList.get(m_currentIndex);
		}
		throw new LexerException("Raggiunta la fine del TokenStream");
	}

  /**
   * Sposta il puntatore al token alla posizione richiesta.
   *  
   * @param position posizione (0-based) a cui spostare la posizione corrente
   * @throws LexerException nel caso lo stream sia chiuso o la posizione richiesta sia errata
   * @see #isOpened()
   * @see #getCurrentPosition()
   */
  public void moveTo(int position) throws LexerException {
    if (!isOpened()) {
      throw new LexerException("TokenStream chiuso");
    }
    if (position < 0 || position >= m_tokenList.size()) {
      throw new LexerException("Posizione richiesta fuori range");
    }
    m_currentIndex = position;
  }
	
	/**
	 * Accede al token corrente. A differenza della {@link #moveNext()},
	 * non sposta il puntatore alla posizione corrente
	 * @return il token alla posizione corrente.
	 * @throws LexerException in caso di stream chiuso
	 */
	public Token getCurrentToken() throws LexerException {
		if (!isOpened()) {
			throw new LexerException("TokenStream chiuso");
		}
		return m_tokenList.get(m_currentIndex);
	}

	/**
	 * Accesso diretto ad un token dello stream tramite indice
	 * @param index indice (0-based) nell'input
	 * @return token alla posizione richiesta
	 * @throws LexerException in caso di stream chiuso o indice errato
	 * 
	 * @see #getSize()
	 * @see #isEndOfStream()
	 * @see #isOpened()
	 */
  public Token getTokenAt(int index) throws LexerException {
    if (!isOpened()) {
      throw new LexerException("TokenStream chiuso");
    }
    try {
      return m_tokenList.get(index);
    } catch (Exception e) {
      throw new LexerException("getTokenAt, indice token errato: " + index, e);
    }
  }
	
	/**
	 * Accede al token successivo a quello corrente. A differenza della {@link #moveNext()},
   * non sposta il puntatore alla posizione corrente.
   * Se si è alla fine dello stream, ritorna sempre il token di 
   * fine stream ( {@link TokenType#EndOfText}).
	 * @return token successivo a quello corrente
	 * @throws LexerException in caso di stream chiuso
	 */
	public Token getNextToken() throws LexerException {
		if (!isOpened()) {
			throw new LexerException("TokenStream chiuso");
		}
		if (isEndOfStream()) {
			return ENDOFTEXT;
		}
		return m_tokenList.get(m_currentIndex + 1);
	}

  /**
   * Accede al token precedente a quello corrente. Se si è all'inizio
   * dello stream (fatta la {@link #open() open} ma mai la {@link #moveNext() moveNext})
   * ritorna sempre un token di 
   * tipo {@link TokenType#NONE}.
   * @return token precedente a quello corrente
   * @throws LexerException in caso di stream chiuso
   */
  public Token getPreviousToken() throws LexerException {
    if (!isOpened()) {
      throw new LexerException("TokenStream chiuso");
    }
    if (m_currentIndex==0) {
      return STARTOFTEXT;
    }
    return m_tokenList.get(m_currentIndex - 1);
  }
	
	/**
	 * Indica se si è arrivati a fine stream.
	 * @return true sse il puntatore punta all'ultimo token dello stream
	 */
	public boolean isEndOfStream() {
		return m_currentIndex == m_maxIndex;
	}
	
	@Override
	public String toString() {
	  String out = "TokenStream@" + hashCode() + " [" + m_inputSource + "] ";
	  if (!isOpened()) {
	    return out + " closed";
	  }
	  out += " opened:";
	  int i = 0;
	  for (Token t : m_tokenList) {
			out += "\n\t ";
			if (i == m_currentIndex) {
				out += "* " + t;
			}
			out += t;
			i++;
	  }
	  return out;
	}
}
