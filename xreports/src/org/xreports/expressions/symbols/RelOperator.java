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
public class RelOperator extends Operator {

	public RelOperator(Token t) throws ParseException {
		super(t, TokenType.RelationalOp);
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isRelational()
	 */
	@Override
	public boolean isRelational() {
	  return true;
	}
}
