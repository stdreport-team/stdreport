/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;

/**
 * @author pier
 *
 */
public class MathValueSign extends Symbol {

  public MathValueSign(Token t) {
    setPosition(t.getStartPosition());
    setText(t.getValue());
  }
  
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
    return null;
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
