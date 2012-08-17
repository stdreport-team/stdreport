/**
 * 
 */
package org.xreports.expressions.symbols;


/**
 * Identificatore da valutare esternamente
 * da un oggetto Evaluator.
 * 
 */
public class TextPart extends Symbol {
	
	public TextPart(String text, int position) {
		setPosition(position);
		setText(text);
	}
	
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		return getText();
	}
	
	
  public String toString() {
  	return "TextPart '" + getText() + "', pos=" + getPosition();
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
