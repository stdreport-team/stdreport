package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.parsers.ParseException;


public abstract class BoolExpression  extends Symbol {

  
  public BoolExpression() {
    super();
  }
  
  public BoolExpression(Token t, TokenType type) throws ParseException {
    super(t, type);
  }
  
}
