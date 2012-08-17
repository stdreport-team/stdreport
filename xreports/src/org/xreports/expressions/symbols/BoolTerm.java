/**
 * 
 */
package org.xreports.expressions.symbols;

/**
 * @author pier
 *
 */
public class BoolTerm extends BoolExpression {

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
  //valuto il primo figlio che sicuramente è un BoolValue
    Object val = evaluateChild(evaluator);
    setPartialValue(val);
    if (getChildNumber() > 1) {
      //devo valutare il secondo figlio che è un BoolTermOpt
    	setPartialValue(getChild(1).evaluate(evaluator));      
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
