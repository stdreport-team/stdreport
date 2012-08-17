/**
 * 
 */
package org.xreports.expressions.symbols;

/**
 * @author pier
 *
 */
public class MathFactor extends MathExpression {

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
  	return evaluateChildren(evaluator);
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
