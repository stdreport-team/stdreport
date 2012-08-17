/**
 * 
 */
package org.xreports.expressions.symbols;

import java.math.BigDecimal;

/**
 * @author pier
 *
 */
public class MathValue extends MathExpression {

  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#evaluate(ciscoop.expressions.symbols.Evaluator)
   */
  @Override
  public Object evaluate(Evaluator evaluator) throws EvaluateException {
		Object val = null; 
  	if (getChildNumber()==2) {
  	  //two child --> unary minus
  		val = getChild(1).evaluate(evaluator);
  		if (val instanceof Double) {
  			setPartialValue(Double.valueOf(-((Double)val).doubleValue()));
  		}
  		else if (val instanceof BigDecimal) {
        setPartialValue(Double.valueOf(-((BigDecimal)val).doubleValue()));
      }
  		else if (val instanceof Number) {
  			setPartialValue(-((Number)val).longValue());  			
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
