/**
 * 
 */
package org.xreports.expressions.symbols;


import org.xreports.engine.ResolveException;
import org.xreports.util.Text;

/**
 * @author pier
 *
 */
public class TextExpr extends Symbol {
		
  public TextExpr() {
    super();
  }

  	
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		//prima guardo se riesco a valutare internamente questa funzione
		Object result = internalEvaluate(evaluator);
		if (result!=null)
			return result;
		
		//se la funzione non riesco a valutarla qui, uso l'evaluator esterno
		if (evaluator!=null)
      try {
        return evaluator.evaluate(this);
      } catch (ResolveException e) {
        throw new EvaluateException(e);
      }
		throw new EvaluateException("Costante %s: non riesco a valutare.", getText());
	}
	
	protected Object internalEvaluate(Evaluator evaluator) throws EvaluateException {
		String txt = "";
		
		//indica fino a dove ho copiato i caratteri dalla stringa originale
		int lastPos = 0;
		//int currentSymbolIndex = 0;
		for (int i = 0; i < getChildNumber(); i++) {
			Symbol s = getChild(i);
			if (s.isIdentifier() || s.isLiteral()) {
				//currentSymbolIndex = i;
			}
			else {
				//prima copio i precedenti simboli
				//Symbol lastSymbol = m_childs.get(currentSymbolIndex);
				String porzione = getFullText().substring(lastPos, s.getPosition());
			  txt += porzione + s.evaluate(evaluator);
			  //txt += s.evaluate(evaluator);
				lastPos = s.getPosition() + s.getFullText().length();
			}
		}

		if (lastPos < getFullText().length())
			txt += getFullText().substring(lastPos, getFullText().length());
		return txt;
	}
	
	
	
	public void debug(int index) {
		String tabs = index==0 ? "" : Text.getChars('\t', index);
		System.out.println(tabs + getClassName() + ": " + getText()
				+ ", position " + getPosition());
		for (Symbol arg : getChildren()) {
			arg.debug(index + 1);
		}
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
