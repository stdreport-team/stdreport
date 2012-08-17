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
public class UnaryMinus extends Operator {

	public UnaryMinus(Token t) throws ParseException {
	  super(t, TokenType.MathOp);
	}

	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isMathOperator()
	 */
	@Override
	public boolean isMathOperator() {
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
