/**
 * 
 */
package org.xreports.expressions.symbols;

/**
 * @author pier
 *
 */
public class RootBoolExpression extends BoolExpression {

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
    if (getChildNumber()==0) {
      return null;
    }
    //valuto il primo figlio che sicuramente è un BoolTerm
    Object result = getChild(0).evaluate(evaluator);
    setPartialValue(result);
    if (getChildNumber() > 1) {
      //devo valutare il secondo figlio che è un BoolExprOpt
      result = getChild(1).evaluate(evaluator);      
    }
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
    return false;
  }
	
}
