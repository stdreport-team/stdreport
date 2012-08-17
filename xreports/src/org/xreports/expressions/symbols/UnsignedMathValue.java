/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;

/**
 * Classe padre di {@link NumberLiteral}, {@link MethodCall},
 * {@link Constant}, {@link Identifier},...
 * @author pier
 *
 */
public class UnsignedMathValue extends Symbol {

  public UnsignedMathValue() {
    super();
  }
  
  public UnsignedMathValue(Token t, TokenType type) throws ParseException {
    super(t, type);
  }
  
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
	 */
	@Override
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		if (getChildNumber()==1 && getChild(0) instanceof RootMathExpression) {
			//questo simbolo rappresenta un'espressione fra parentesi
			setPartialValue(getChild(0).evaluate(evaluator));
		}
		return getPartialValue();
	}

	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return false;
	}

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isConcrete()
   */
  @Override
  public boolean isConcrete() {
    return false;
  }
	
}
