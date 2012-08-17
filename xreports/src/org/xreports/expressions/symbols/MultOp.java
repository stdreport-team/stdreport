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
public class MultOp extends Operator {

	public MultOp(Token t) throws ParseException {
    super(t, TokenType.MathOp);
	}

	@Override
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		return null;
	}

	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isMathOperator()
	 */
	@Override
	public boolean isMathOperator() {
	  return true;
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Operator#isMultOperator()
	 */
	@Override
	public boolean isMultOperator() {
	  return true;
	}
}
