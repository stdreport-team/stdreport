/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;


/**
 * @author pier
 *
 */
public class StringLiteral extends Symbol {
	
	public StringLiteral(Token t) {
		setPosition(t.getStartPosition());
		setText(t.getValue());
	}
	
	public Object evaluate(Evaluator evaluator) {
		return getText();
	}
	
	
	/**
	 * Imposta il testo del simbolo
	 * @param text text to set
	 */
	@Override
	public void setText(String text) {
		String s = text;
		if (s.startsWith("'")) {
			//siccome dal token mi arriva la stringa grezza, devo:
			// 1) levare gli apici iniziale e finale
			// 2) levare eventuale '\' di escape 
			s = text.length()==2 ? "" 
					: text.substring(1, text.length() - 1);
			//sostituisco "\\" con "\"
			s = s.replace("\\\\", "\\");
			
			//sostituisco "\'" con "'"
			s = s.replace("\\'", "'");
		}
		super.setText(s);			
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
