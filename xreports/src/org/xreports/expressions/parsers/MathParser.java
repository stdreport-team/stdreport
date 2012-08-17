package org.xreports.expressions.parsers;

import org.xreports.expressions.lexer.Lexer;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.lexer.TokenStream;
import org.xreports.expressions.symbols.Field;
import org.xreports.expressions.symbols.MathExprOpt;
import org.xreports.expressions.symbols.MathFactor;
import org.xreports.expressions.symbols.MathFactorOpt;
import org.xreports.expressions.symbols.MathTerm;
import org.xreports.expressions.symbols.MathTermOpt;
import org.xreports.expressions.symbols.MathValue;
import org.xreports.expressions.symbols.Operator;
import org.xreports.expressions.symbols.RootMathExpression;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.expressions.symbols.UnaryMinus;
import org.xreports.expressions.symbols.UnsignedMathValue;

public class MathParser extends GenericParser {

  /**
   * Costruttore con il solo testo. L'analizzatore lessicale viene costruito
   * internamente.
   * 
   * @param text
   *          testo da analizzare
   */
  public MathParser(String text) {
    super(text);
  }

  /**
   * Costruttore token stream. L'analizzatore lessicale non viene coinvolto.
   * 
   * @param text
   *          testo da analizzare
   */
  public MathParser(TokenStream stream) {
    super(stream);
  }
  
  
  @Override
  protected void postParsing() {

  }

  @Override
  protected Symbol parseGrammar(TokenType expectedEnd) throws ParseException, LexerException {
    //getTokenStream().moveNext();
    RootMathExpression rme = mathExpr();
    if (expectedEnd != null && expectedEnd != TokenType.Any) {
      if ( !getTokenStream().getCurrentToken().is(expectedEnd)) {
        throw new ParseException("Mi aspettavo " + expectedEnd, getTokenStream().getCurrentToken());
      }      
    }
    //return getTreeRoot();
    return rme;
  }

  /**
   * Produzione <b>inziale</b> della grammatica:
   * <tt style="margin-left: 10px;"> MathExpr ::= MathTerm  MathExprOpt</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected RootMathExpression mathExpr() throws LexerException, ParseException {
    RootMathExpression rme = new RootMathExpression();
    rme.addChild(mathTerm());
    rme.addChild(mathExprOpt());
    return rme;
  }

  /**
   * Produzione:
   * <tt style="margin-left: 10px;"> MathTerm ::= MathFactor  MathTermOpt</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathTerm mathTerm() throws LexerException, ParseException {
    MathTerm term = new MathTerm();
    term.addChild(mathFactor());
    term.addChild(mathTermOpt());
    return term;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> MathExprOpt ::= sumOp MathExpr MathExprOpt  |  &#923; </tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathExprOpt mathExprOpt() throws LexerException, ParseException {
    MathExprOpt meo = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case MathOp:
        if (getTokenStream().getCurrentToken().isOneOf("+", "-")) {
          meo = new MathExprOpt(getTokenStream().getCurrentToken());
          meo.addChild(Symbol.getInstance(getTokenStream().getCurrentToken()));
          consumeToken();
          meo.addChild(mathTerm());
          meo.addChild(mathExprOpt());
        }
        break;
      default:
        break;
    }
    return meo;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> MathTermOpt ::= multOp MathFactor MathTermOpt  |  &#923; </tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathTermOpt mathTermOpt() throws LexerException, ParseException {
    MathTermOpt mto = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case MathOp:
        if (getTokenStream().getCurrentToken().isOneOf(Operator.MULT, Operator.DIV, Operator.MODULO)) {
          mto = new MathTermOpt(getTokenStream().getCurrentToken());
          mto.addChild(Symbol.getInstance(getTokenStream().getCurrentToken()));          
          consumeToken();
          mto.addChild(mathFactor());
          mto.addChild(mathTermOpt());
        }
        break;
      default:
        break;
    }
    return mto;
  }

  /**
   * Produzione:
   * <tt style="margin-left: 10px;"> MathFactor ::= MathValue  MathFactorOpt</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathFactor mathFactor() throws LexerException, ParseException {
    MathFactor factor = new MathFactor();
    factor.addChild(mathValue());
    factor.addChild(mathFactorOpt());
    return factor;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> MathFactorOpt ::= expOp MathValue MathFactorOpt  |  &#923; </tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathFactorOpt mathFactorOpt() throws LexerException, ParseException {
    MathFactorOpt mfo = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case MathOp:
        if (getTokenStream().getCurrentToken().isOneOf(Operator.EXP)) {
          mfo = new MathFactorOpt(getTokenStream().getCurrentToken());
          mfo.addChild(Symbol.getInstance(getTokenStream().getCurrentToken()));          
          consumeToken();
          mfo.addChild(mathValue());
          mfo.addChild(mathFactorOpt());
        }
        break;
      default:
        break;
    }
    return mfo;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> MathValue ::=MathValueSign  UnsignedMathValue</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected MathValue mathValue() throws LexerException, ParseException {
    MathValue val = new MathValue();
    val.addChild(mathValueSign());
    val.addChild(unsignedMathValue());
    return val;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> MathValueSign ::= "-"  | &#923;</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected UnaryMinus mathValueSign() throws LexerException, ParseException {
    UnaryMinus mvs = null;
    if (getTokenStream().getCurrentToken().is("-")) {
      if (getTokenStream().getPreviousToken().is(TokenType.MathOp)) {
        throw new ParseException(getTokenStream().getCurrentToken());
      }
      mvs = new UnaryMinus(getTokenStream().getCurrentToken());
      consumeToken();
    }
    return mvs;
  }

  /**
   * Produzione
   * <p style="margin-left: 20px;">
   * <tt> MathTerm ::= number  |  MethodCall  |  Field  |  Constant  
   *         |  "(" MathExpr  ")" | identifier ExternalValueId</tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol unsignedMathValue() throws LexerException, ParseException {
  	final String errMess = "Mi aspettavo un numero, una costante, un method-call, un identificatore, un field o una '('";
    Symbol umv = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case MethodCall:
      case Field:
      case Number:
        umv = (UnsignedMathValue)Symbol.getInstance(getTokenStream().getCurrentToken());
        consumeToken();
        break;
      case Constant:
        umv = Symbol.getInstance(getTokenStream().getCurrentToken());
        consumeToken();
        break;
      case Identifier:
      	if (getTokenStream().getNextToken().is("[")) {
      		umv = qualifiedField();
      	}
      	else if (getTokenStream().getNextToken().is("(")) {
      		umv = function(getTokenStream());
      	}
      	else {
      		umv = (UnsignedMathValue)Symbol.getInstance(getTokenStream().getCurrentToken());
          consumeToken();
      	}
        break;
      case ParT:
        if (getTokenStream().getCurrentToken().is("(")) {
          consumeToken();
          umv = new UnsignedMathValue();
          umv.addChild(mathExpr());
          mathParTClosed();
          umv.setText("(" + umv.getText() + ")");
        } 
        else {
          throw new ParseException(errMess, getTokenStream().getCurrentToken());        	
        }
        break;
      default:
        throw new ParseException(errMess, getTokenStream().getCurrentToken());
    }
    return umv;
  }

  /**
   * Produzione
   * <p style="margin: 0 0 2 20;">
   * <tt> QualifiedField   ::= identifier [QualifiedExpr] NoGroupField</tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol qualifiedField() throws LexerException, ParseException {
    //mi aspetto di essere sull'identificatore che è il nome del gruppo
    String groupName = getTokenStream().getCurrentToken().getValue();
    int position = getTokenStream().getCurrentToken().getStartPosition();
    consumeToken();
    //mi aspetto di essere sul "["
    consumeToken();
    //ora devo riconoscere l'espressione dentro le "[]"
    Symbol qualExpr = qualifiedExpr();
    //mi aspetto di essere sul "]"
    consumeToken();
    Field field = noGroupField();
    String fieldName = field!=null ? field.getField() : null;
    Field f = new Field(groupName, fieldName, qualExpr, position);
    return f;
  }

  /**
   * Produzione
   * <p style="margin: 0 0 2 20;">
   * <tt> NoGroupField  ::=  "#"identifier  |  &#923; </tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Field noGroupField() throws LexerException, ParseException {
    if ( getTokenStream().getCurrentToken().is(TokenType.Field)) {
      if ( getTokenStream().getCurrentToken().getTokenSourceVal().startsWith(""+Lexer.FIELD_MODIFIER)) {
        Field f = (Field)Symbol.getInstance(getTokenStream().getCurrentToken());
        consumeToken();
        return f;
      }
    }
    return null;
  }
  
  
  /**
   * Produzione
   * <p style="margin: 0 0 2 20;">
   * <tt> QualifiedExpr  ::=  MathExpr  |  BoolExpr </tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol qualifiedExpr() throws LexerException, ParseException {
    Symbol expr = nonTextExpr(getTokenStream(), "]");
    if (expr == null) {
      throw new ParseException("Qualified expression non riconosciuta", getTokenStream().getCurrentToken());    	
    }
    if ( !getTokenStream().getCurrentToken().is("]")) {
      throw new ParseException("Mi aspettavo una ']'", getTokenStream().getCurrentToken());
    }
    return expr;
  }

  protected void mathParTClosed() throws LexerException, ParseException {
    if (getTokenStream().getCurrentToken().is(")")) {
      //System.out.println("mathParTClosed ')'");
      getTokenStream().moveNext();
      return;
    }
    throw new ParseException("Mi aspettavo una ')'", getTokenStream().getCurrentToken());
  }

  private void consumeToken() throws LexerException, ParseException {
    getTokenStream().moveNext();
  }

}
