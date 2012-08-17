/**
 * 
 */
package org.xreports.expressions.parsers;

import org.xreports.expressions.symbols.Symbol;
import org.xreports.util.Text;

/**
 * @author pier
 *
 */
public class TreePrinter {
  private StringBuilder c_tree;
  
  private int c_vertPos;
  private int c_horizPos;
  
  
  
  public String printTree(Symbol s) {
    c_tree = new StringBuilder();
    c_vertPos = 0;
    printSymbol(s, 0, false);
    return c_tree.toString();
  }
  
  private void printSymbol(Symbol s, int horizPos, boolean newLine) {
    String nodeText = s.getClassName() + " [" + s.getText() + "]";
    if (horizPos == 0) {
      //root
      c_tree.append(nodeText);      
    }
    else {
      if (newLine) {
        String spaces = Text.getChars(' ', horizPos - 8);
        c_tree.append("\n" + spaces + "|\n" + spaces + "+--");              
      }
      c_tree.append("-->" + nodeText);      
    }
    horizPos += nodeText.length() + 5; 
    int qtaChild = 0;
    for (Symbol child : s.getChildren()) {
      qtaChild++;
      printSymbol(child, horizPos, qtaChild>1);
    }
  }
  
  
  
}
