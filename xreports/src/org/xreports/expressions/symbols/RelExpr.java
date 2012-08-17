/**
 * 
 */
package org.xreports.expressions.symbols;


/**
 * @author pier
 *
 */
public class RelExpr extends BoolExpression {

  public RelExpr(int position) {
    setPosition(position);
  }
  
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
    Object val1 = getChild(0).evaluate(evaluator);
    Operator op = (Operator)getChild(1);
    //Object val2 = getChild(2).evaluate(evaluator);
    Object result = evaluateBinaryBoolOp(evaluator, val1, op, getChild(2));
    setPartialValue(result);  
    return result;
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
    return true;
  }
	
}
