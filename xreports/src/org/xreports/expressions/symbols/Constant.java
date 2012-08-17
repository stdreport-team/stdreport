/**
 * 
 */
package org.xreports.expressions.symbols;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.engine.ResolveException;

/**
 * Costante valutabile direttamente.
 * 
 */
public class Constant extends Symbol {
	
  /**
   * Costruttore standard con il token sorgente
   * 
   * @param t token che rappresenta questo simbolo
   * 
   * @throws ParseException in caso il token passato sia del tipo imprevisto o sia errato
   */
	public Constant(Token t) throws ParseException {
	  super(t, TokenType.Constant);
	}
	
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		String text = getText();
		Date adesso = new Date();
		if (text.equals("today")) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(adesso);
		}
		else if (text.equals("now")) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return sdf.format(adesso);
		}
		else if (text.equals("time")) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			return sdf.format(adesso);
		}
		
		if (evaluator!=null)
      try {
        return evaluator.evaluate(this);
      } catch (ResolveException e) {
        throw new EvaluateException(e);
      }
		else
			throw new EvaluateException("Costante " + getText() + ": non riesco a valutare.");
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
	}
	
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isTerminal()
   */
  @Override
  public boolean isTerminal() {
  	return true;
  }
	
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isConcrete()
   */
  @Override
  public boolean isConcrete() {
    return true;
  }
  
}
