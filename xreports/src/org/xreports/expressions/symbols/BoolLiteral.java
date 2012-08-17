/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.parsers.ParseException;

/**
 * @author pier
 *
 */
public class BoolLiteral extends BoolExpression {
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	private Boolean m_value = null;
	
	public BoolLiteral(Token t) throws ParseException {
	  super(t, TokenType.BoolLiteral);
		if (t.getValue().equalsIgnoreCase(TRUE)) {
			m_value = Boolean.TRUE;
		}
		else if (t.getValue().equalsIgnoreCase(FALSE)) {
			m_value = Boolean.FALSE;
		}
	}
	
	
	public Object evaluate(Evaluator evaluator) {
	  setPartialValue(m_value);
		return m_value;
	}
	
	
	public boolean isLiteral() {
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
