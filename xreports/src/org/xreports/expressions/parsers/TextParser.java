/**
 * 
 */
package org.xreports.expressions.parsers;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.lexer.TokenStream;
import org.xreports.expressions.symbols.StringLiteral;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.expressions.symbols.TextExpr;

/**
 * @author pier
 *
 */
public class TextParser extends GenericParser {

  /**
   * Costruttore con il solo testo. L'analizzatore lessicale viene costruito
   * internamente.
   * 
   * @param text
   *          testo da analizzare
   */
  public TextParser(String text) {
    super(text);
  }

  /**
   * Costruttore token stream. L'analizzatore lessicale non viene coinvolto.
   * 
   * @param text
   *          testo da analizzare
   */
  public TextParser(TokenStream stream) {
    super(stream);
  }  
  
  @Override
  protected void postParsing() {

  }  
  
  /* (non-Javadoc)
   * @see ciscoop.expressions.parsers.GenericParser#parseGrammar(ciscoop.expressions.lexer.Lexer.TokenType)
   */
  @Override
  protected Symbol parseGrammar(TokenType expectedEnd) throws ParseException, LexerException {
    TextExpr text = new TextExpr();
    text.setFullText(getText());
    while ( !getTokenStream().getCurrentToken().is(TokenType.EndOfText)) {
      Token t = getTokenStream().getCurrentToken();
      if (t.isOneOf(TokenType.Field, TokenType.Constant)) {
        text.addChild(Symbol.getInstance(t));        
      }
      else {
        StringLiteral sl = new StringLiteral(t);
        text.addChild(sl);
      }
      getTokenStream().moveNext();
    }
    return text;
  }

  
  /**
   * Riconosce le espressioni di tipo testo, per la precisione
   * la produzione <pre>TextExpr = TextPrimary | TextPrimary  TextExpr</pre>
   * 
   * @return se la produzione è riconosciuta, ne ritorna il simbolo (un TextExpr), altrimenti
   *    ritorna <b>null</b>
   */
//  private Symbol parseTextExpr() {
//    TextExpr text = new TextExpr(sourceText, 0);
//    text.setFullText(this.sourceText);
//    while ( !m_lex.getCurrentToken().is(TokenType.EndOfText)) {
//      Symbol term = parseNonStringValue(false);
//      if (term != null)
//        text.addChild(term);
//      else {
//        Token part = m_lex.nextToken();
//        if ( !part.is(TokenType.EndOfText)) {
//          TextPart textpart = new TextPart(part.getValue(), part.getStartPosition());
//          text.addChild(textpart);
//        }
//      }
//    }
//    return text;
//  }
  
  
}
