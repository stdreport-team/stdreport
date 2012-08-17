/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.engine.ResolveException;


/**
 * @author pier
 *
 */
public class MethodCall extends UnsignedMathValue {

	private String m_class;
	private String m_method;
	
	/**
	 * Costruttore standard tramite token
	 * @param t token che rappresenta questo MethodCall
	 * 
	 * @throws ParseException in caso di token errato/imprevisto
	 */
	public MethodCall(Token t) throws ParseException {
	  super(t, TokenType.MethodCall);
		int i = t.getValue().indexOf('.');
		if (i > 0) {
			m_class = t.getValue().substring(0, i);
			m_method = t.getValue().substring(i+1);
		}			
		else {
      throw new ParseException(
          "MethodCall ha sintassi errata", t);		  
		}
	}
	
	/**
	 * Ritorna il nome della classe di questo method call.
	 * Se ad esempio ho il method call 
	 *    <tt>myclass.mymethod</tt>,
	 * <br/> 
	 * {@link #getClassRef()} ritorna "myclass".
	 * 
	 * @return il nome della classe
	 */
	public String getClassRef() {
		return m_class;
	}

  /**
   * Ritorna il nome del metodo di questo method call.
   * Se ad esempio ho il method call  
   *   <tt>myclass.mymethod</tt>, 
   * <br/>  
   * {@link #getMethodRef()} ritorna "mymethod".
   * 
   * @return il nome della classe
   */
	public String getMethodRef() {
		return m_method;
	}
	
	
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		if (evaluator!=null)
      try {
        return evaluator.evaluate(this);
      } catch (ResolveException e) {
        throw new EvaluateException(e);
      }
    else
			throw new EvaluateException("MethodCall %s: non riesco a valutare.", getText());
	}

  public String toString() {
  	return "MethodCall " + getText();
  }

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isMethodCall()
   */
  @Override
  public boolean isMethodCall() {
  	return true;
  }

	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return true;
	}
  
}
