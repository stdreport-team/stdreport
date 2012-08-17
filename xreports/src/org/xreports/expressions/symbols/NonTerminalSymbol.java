package org.xreports.expressions.symbols;

import java.math.BigDecimal;


public abstract class NonTerminalSymbol extends Symbol {

  
  
	@Override
	public boolean isTerminal() {
		return false;
	}	
	
	/**
	 * Se questo simbolo ha un numero massimo di figli, deve ritornare tale massimo
	 * con questa funzione. Se non ha un numero massimo, deve tornare 0.
	 * @return
	 */
	public int getChildMaxNumber() {
	  return 0;
	}

	/**
	 * Indica se questo simbolo ha già raggiunto il limite massimo di figli ammessi
	 * @return true sse ha già tutti i figli
	 */
	public boolean isMaxChildReached() {
	  return getChildMaxNumber() > 0 && getChildNumber() >= getChildMaxNumber();
	}
	
	
  protected Object evaluateChild(Evaluator evaluator) throws EvaluateException {   
    if (getChildNumber()==0) {
      return null;
    }
    return getChild(0).evaluate(evaluator);
  }	
  
  
  protected Number evaluateBinaryMathOp(Evaluator evaluator, Object o1, String op, Object o2) throws EvaluateException {    
    if (!(o1 instanceof Number)) {
      throw new EvaluateException("Il primo operando dell'espressione " + getText() + " non è numerico: '" + o1 + "'");
    }
    if (!(o2 instanceof Number)) {
      throw new EvaluateException("Il secondo operando dell'operatore '" + op + "' nell'espressione " + getText() + " non è numerico: '" + o2 + "'");
    }
    boolean bDecimal = false;
    bDecimal = (o1 instanceof Double) || (o2 instanceof Double)
         || (o1 instanceof BigDecimal) || (o2 instanceof BigDecimal);

    Double d1 = null, d2 = null;
    Long l1 = null, l2 = null;
    if (bDecimal) {
      d1 = Double.valueOf(((Number)o1).doubleValue());
      d2 = Double.valueOf(((Number)o2).doubleValue());
    } else {
      l1 = Long.valueOf(((Number)o1).longValue());
      l2 = Long.valueOf(((Number)o2).longValue());
    }
      
    if ( op.equals(Operator.PLUS) ) {
      if (bDecimal)
        return new Double(d1.doubleValue() + d2.doubleValue());
      else
        return new Long(l1.longValue() + l2.longValue());
    }
    else if ( op.equals(Operator.MINUS) ) {
      if (bDecimal)
        return new Double(d1.doubleValue() - d2.doubleValue());
      else
        return new Long(l1.longValue() - l2.longValue());
    }
    else if ( op.equals(Operator.MULT) ) {
      if (bDecimal)
        return new Double(d1.doubleValue() * d2.doubleValue());
      else
        return Long.valueOf(l1.longValue() * l2.longValue());
    }
    else if ( op.equals(Operator.DIV) ) {
      if (bDecimal)
        return new Double(d1.doubleValue() / d2.doubleValue());
      else
        return new Double(l1.doubleValue() / l2.doubleValue());
    }
    else if ( op.equals(Operator.MODULO) ) {
      if (bDecimal)
        return new Double(d1.doubleValue() % d2.doubleValue());
      else
        return new Long(l1.longValue() % l2.longValue());
    }
    else if ( op.equals(Operator.EXP) ) {
      if (bDecimal)
        return new Double(Math.pow(d1.doubleValue(), d2.doubleValue()));
      else 
        return new Double(Math.pow(l1.doubleValue(), l2.doubleValue()));
    }
    else {
      throw new EvaluateException("Operatore non riconosciuto");
    }    
  }
  
  protected Object evaluatePartialOp(Evaluator evaluator) throws EvaluateException {
//    Object result = null;
    
    Object val1 = getPreviousSibling().getPartialValue();
    String op = getChild(0).getText();
    Object val2 = getChild(1).evaluate(evaluator);    
    setPartialValue(evaluateBinaryMathOp(evaluator, val1, op, val2));
    if (getChildNumber() > 2) {
      //produzione   Op YYY MathXXXOpt   
      setPartialValue(getChild(2).evaluate(evaluator));
    }
    return getPartialValue();
    
    
//    if (getParent() instanceof RootMathExpression) {
//      Symbol prevSibling = getPreviousSibling();
//      if (prevSibling==null) {
//        throw new EvaluateException("Previous sibling null!");
//      }
//      Object val1 = prevSibling.evaluate(evaluator);
//      String op = getChild(0).getText();
//      Object val2 = getChild(1).evaluate(evaluator);
//      setPartialValue(evaluateBinaryOp(evaluator, val1, op, val2));
//    }
//    else {
//      //se arrivo qui il mio padre deve essere per forza un'altra MathXXXOpt
//      Object superPartialValue = getParent().getPartialValue();
//      String op = getChild(0).getText();
//      Object val2 = getChild(1).evaluate(evaluator);
//      setPartialValue(evaluateBinaryOp(evaluator, superPartialValue, op, val2));      
//    }
//    if (getChildNumber() > 2) {
//      //produzione   Op YYY MathXXXOpt   
//      result = getChild(2).evaluate(evaluator);
//    }
//    else {
//      //produzione   SumOp MathTerm     
//      result = getPartialValue();
//    }
//    return result;
  }
  
  
}
