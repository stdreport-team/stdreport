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
public class OrOperator extends Operator {

	public OrOperator(Token t) throws ParseException {
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
	 * @see ciscoop.expressions.symbols.Operator#isOR()
	 */
	@Override
	public boolean isOR() {
	  return true;
	}
}
