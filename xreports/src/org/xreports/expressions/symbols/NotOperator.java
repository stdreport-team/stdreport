/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;


/**
 * @author pier
 *
 */
public class NotOperator extends Operator {

	public NotOperator(Token t) throws ParseException {
		super(t, TokenType.LogicalOp);
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isLogical()
	 */
	@Override
	public boolean isLogical() {
	  return true;
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isNOT()
	 */
	@Override
	public boolean isNOT() {
	  return true;
	}
	
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Operator#isUnary()
   */
  @Override
  public boolean isUnary() {
    return true;
  }
	
}
