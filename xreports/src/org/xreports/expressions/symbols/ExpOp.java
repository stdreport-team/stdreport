package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.parsers.ParseException;

/**
 * Espressione aritmetica binaria in cui l'operatore è
 * quello di esponenziazione o radice quadrata
 * @author pier
 *
 */
public class ExpOp extends Operator {
	
	public ExpOp(Token t) throws ParseException {
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
	
}
