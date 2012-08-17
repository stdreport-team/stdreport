/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;
import org.xreports.engine.ResolveException;


/**
 * Identificatore da valutare esternamente
 * da un oggetto Evaluator.
 * 
 */
public class Identifier extends UnsignedMathValue {
	
	public Identifier(Token t) {
		setPosition(t.getStartPosition());
		setText(t.getValue());
	}
	
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		if (evaluator!=null) {
      try {
        setPartialValue(evaluator.evaluate(this));
      } catch (ResolveException e) {
        throw new EvaluateException(e);
      }
			return getPartialValue();
		}
		throw new EvaluateException("Identificatore " + getText() + ": non riesco a valutare.");
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#isIdentifier()
	 */
	@Override
	public boolean isIdentifier() {
		return true;
	}
	
  public String toString() {
  	return "Identifier '" + getText() + "', pos=" + getPosition();
  }

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isTerminal()
   */
  @Override
  public boolean isTerminal() {
  	return true;
  }
  
}
