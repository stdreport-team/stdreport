package org.xreports.expressions.parsers;

import org.xreports.expressions.lexer.Lexer;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.lexer.TokenStream;
import org.xreports.expressions.symbols.BoolExprOpt;
import org.xreports.expressions.symbols.BoolLiteral;
import org.xreports.expressions.symbols.BoolTerm;
import org.xreports.expressions.symbols.BoolTermOpt;
import org.xreports.expressions.symbols.BoolValue;
import org.xreports.expressions.symbols.NotOperator;
import org.xreports.expressions.symbols.RelExpr;
import org.xreports.expressions.symbols.RelOperator;
import org.xreports.expressions.symbols.RootBoolExpression;
import org.xreports.expressions.symbols.Symbol;

public class BoolParser extends GenericParser {

  public static final String UNARY_MINUS = "UNARY_MINUS";

  /**
   * Costruttore con il solo testo. L'analizzatore lessicale viene costruito
   * internamente.
   * 
   * @param text
   *          testo da analizzare
   */
  public BoolParser(String text) {
    super(text);
  }

  /**
   * Costruttore token stream. L'analizzatore lessicale non viene coinvolto.
   * 
   * @param text
   *          testo da analizzare
   */
  public BoolParser(TokenStream stream) {
    super(stream);
  }  
  
  @Override
  protected void postParsing() {

  }

  @Override
  protected Symbol parseGrammar(TokenType expectedEnd) throws ParseException, LexerException {
    //getTokenStream().moveNext();
    RootBoolExpression rbe = boolExpr();
    if (expectedEnd != null && expectedEnd != TokenType.Any) {
	    if ( !getTokenStream().getCurrentToken().is(expectedEnd)) {
	      throw new ParseException("Mi aspettavo " + expectedEnd, getTokenStream().getCurrentToken());
	    }
    }
    //return getTreeRoot();
    return rbe;
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
  protected RootBoolExpression boolExpr() throws LexerException, ParseException {
    RootBoolExpression rbe = new RootBoolExpression();
    rbe.addChild(boolTerm());
    rbe.addChild(boolExprOpt());
    return rbe;
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
  protected BoolTerm boolTerm() throws LexerException, ParseException {
    BoolTerm term = new BoolTerm();
    term.addChild(boolValue());
    term.addChild(boolTermOpt());
    return term;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> BoolExprOpt ::= orOp BoolTerm BoolExprOpt  |  &#923; </tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected BoolExprOpt boolExprOpt() throws LexerException, ParseException {
    BoolExprOpt meo = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case LogicalOp:
        if (getTokenStream().getCurrentToken().is("||")) {
          meo = new BoolExprOpt(getTokenStream().getCurrentToken());
          meo.addChild(Symbol.getInstance(getTokenStream().getCurrentToken()));
          consumeToken();
          meo.addChild(boolTerm());
          meo.addChild(boolExprOpt());
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
  protected BoolTermOpt boolTermOpt() throws LexerException, ParseException {
    BoolTermOpt bto= null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case LogicalOp:
        if (getTokenStream().getCurrentToken().is("&&")) {
          bto = new BoolTermOpt(getTokenStream().getCurrentToken());
          bto.addChild(Symbol.getInstance(getTokenStream().getCurrentToken()));          
          consumeToken();
          bto.addChild(boolValue());
          bto.addChild(boolTermOpt());
        }
        break;
      default:
        break;
    }
    return bto;  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> BoolValue ::= RelExpr | NotOpt BoolPrimary</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol boolValue() throws LexerException, ParseException {
    Symbol s = relExpr();
    if (s == null) {    
      s = new BoolValue();
      s.addChild(notOpt());
      s.addChild(boolPrimary());
    }
    return s;
  }

  /**
   * Produzione
   * <tt style="margin-left: 10px;"> RelExpr ::= NonBoolExpr RelOp NonBoolExpr </tt>
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected RelExpr relExpr() throws LexerException, ParseException {
    RelExpr re = null;
    int posBackup = getTokenStream().getCurrentPosition();
    try {
    	
      //expression sinistra
    	Symbol eLeft = nonBoolExpr(getTokenStream(), Lexer.getRelOperators());
    	if (eLeft != null && getTokenStream().getCurrentToken().is(TokenType.RelationalOp)) {
        //operatore relazionale
        RelOperator relop = new RelOperator(getTokenStream().getCurrentToken());
        getTokenStream().moveNext();
        //expression destra
        Symbol eRight = nonBoolExpr(getTokenStream(), (String[])null);
        if (eRight != null) {
          //ok parsing RelExpr finito!
          re = new RelExpr(eLeft.getPosition());
          re.addChild(eLeft);
          re.addChild(relop);
          re.addChild(eRight);        	
        }
    	}
      
    } catch (Exception e) {
      //ignoro exception: parsing andato male
      e.printStackTrace();
    }
    if (re == null) {
    	//for some reason not recognized: backtrack to saved initial position
    	getTokenStream().moveTo(posBackup);
    }
    return re;
  }
  
  /**
   * Produzione
   * <tt style="margin-left: 10px;"> NotOpt ::= "!"  | &#923;</tt>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol notOpt() throws LexerException, ParseException {
    Symbol s = null;
    if (getTokenStream().getCurrentToken().is("!")) {
      NotOperator notOp = new NotOperator(getTokenStream().getCurrentToken());
      consumeToken();
      return notOp;
    }
    return s;
  }

  /**
   * Produzione
   * <p style="margin-left: 20px;">
   * <tt> BoolPrimary ::= BoolLiteral  |  MethodCall  |  Field  |  Constant  | Function
   *         |  "(" BoolExpr  ")"</tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol boolPrimary() throws LexerException, ParseException {
  	final String errMess = "Mi aspettavo true/false, una costante, un method-call, un identificatore, un field o una '('";
    Symbol umv = null;
    switch (getTokenStream().getCurrentToken().getType()) {
      case MethodCall:
      case Constant:
      case Field:
        umv = Symbol.getInstance(getTokenStream().getCurrentToken());
        consumeToken();
        break;
      case BoolLiteral:
    		umv = new BoolLiteral(getTokenStream().getCurrentToken());
        consumeToken();
        break;
      case Identifier:
      	if (getTokenStream().getNextToken().is("(")) {
      		umv = function(getTokenStream());
      	}
      	else {
      		umv = Symbol.getInstance(getTokenStream().getCurrentToken());
          consumeToken();
      	}
        break;
      case ParT:
        if (getTokenStream().getCurrentToken().is("(")) {
          consumeToken();
          umv = boolExpr(getTokenStream(), (String[])null);
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
   * <tt> QualifiedField  ::=  "["QualifiedExpr"]"   |  "["QualifiedExpr"]"  SimpleField</tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol noGroupField() throws LexerException, ParseException {
    if ( getTokenStream().getCurrentToken().is(TokenType.Field)) {
      if ( getTokenStream().getCurrentToken().getValue().startsWith(""+Lexer.FIELD_MODIFIER)) {
        return Symbol.getInstance(getTokenStream().getCurrentToken());
      }
    }
    return null;
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
