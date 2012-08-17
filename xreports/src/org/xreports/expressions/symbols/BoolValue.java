/**
 * 
 */
package org.xreports.expressions.symbols;

/**
 * @author pier
 *
 */
public class BoolValue extends BoolExpression {

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
		Object val = null; 
  	if (getChildNumber()==2) {
  	  //two child--> not operator
  		val = getChild(1).evaluate(evaluator);
  		if (val instanceof Boolean) {
  		  boolean result = !((Boolean)val).booleanValue();
  			setPartialValue(Boolean.valueOf(result));
  		}
  		else {
  			throw new EvaluateException("Mi aspettavo un risultato di tipo booleano per l'espressione %s", getChild(1).getText());  			
  		}
  	}
  	else {
  		setPartialValue(evaluateChild(evaluator));
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
