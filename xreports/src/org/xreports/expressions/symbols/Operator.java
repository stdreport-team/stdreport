/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;


/**
 * @author pier
 *
 */
public class Operator extends Symbol {
  /** operatore di addizione */
	public static final String PLUS = "+";
  /** operatore di sottrazione */
	public static final String MINUS = "-";
  /** operatore di moltiplicazione */
	public static final String MULT = "*";
  /** operatore di divisione */
	public static final String DIV = "/";
  /** operatore resto della divisione */
	public static final String MODULO = "%";
	/** operatore di potenza */
	public static final String EXP = "^";

  /** operatore di addizione (char) */
  public static final char PLUS_C = '+';
  /** operatore di sottrazione (char) */
  public static final char MINUS_C = '-';
  /** operatore di moltiplicazione (char) */
  public static final char MULT_C = '*';
  /** operatore di divisione (char) */
  public static final char DIV_C = '/';
  /** operatore resto della divisione (char)*/
  public static final char MODULO_C = '%';
  /** operatore di potenza (char)*/
  public static final char EXP_C = '^';
	
	
	public static final String AND = "&&";
	public static final String OR = "||";
	public static final String NOT = "!";
	
	public static final String EQ = "=";
	public static final String NEQ = "!=";
	public static final String GT = ">";
	public static final String GTE = ">=";
	public static final String LT = "<";
	public static final String LTE = "<=";
	
	public Operator(Token t, TokenType type) throws ParseException {
	  super(t, type);
	}
	

	/**
	 * Ritorna true se è un operatore unario.
	 * Attualmente sono tutti binari eccetto il not logico ({@link #NOT})
	 * e il meno unario ( {@link UnaryMinus}).
	 */
	public boolean isUnary() {
		return false; 
	}
	
	/**
	 * Ritorna true se è un operatore aritmetico
	 */
	public boolean isMathOperator() {
		return false; 
	}
	
	/**
	 * Ritorna true se è un operatore aritmetico di somma.
	 * Gli operatori previsti sono {@link #PLUS} e {@link #MINUS}.
	 */
	public boolean isSumOperator() {
		return false; 
	}
	
	/**
	 * Ritorna true se è un operatore aritmetico di moltiplicazione.
	 * Gli operatori previsti sono {@link #MULT} per la moltiplicazione,
	 * {@link #DIV} per la divisione e {@link #MODULO} per il resto della divisione.
	 * @return
	 */
	public boolean isMultOperator() {
		return false; 
	}

	/**
	 * Ritorna true se è uno degli operatori relazionali: <ul>
	 *  <li>{@link #NEQ}: diverso
	 *  <li>{@link #EQ}: uguale
	 *  <li>{@link #GT}: maggiore
	 *  <li>{@link #GTE}: maggiore o uguale
	 *  <li>{@link #LT}: minore
	 *  <li>{@link #LTE}: monore o uguale
	 * </ul>
	 */
	public boolean isRelational() {
		return false; 
	}

  /**
   * Ritorna true se è uno dei 3 operatori booleani: 
   * AND ({@link #AND}), OR ({@link #OR}) o NOT ({@link #NOT}) 
   */	
	public boolean isLogical() {
		return false; 
	}

  /**
   * Ritorna true se è l'operatore logico OR ({@link #AND}) 
   */
	public boolean isAND() {
		return false; 
	}

  /**
   * Ritorna true se è l'operatore logico OR ({@link #OR}) 
   */
	public boolean isOR() {
		return false; 
	}

	/**
	 * Ritorna true se è l'operatore logico NOT ({@link #NOT}) 
	 */
	public boolean isNOT() {
		return false; 
	}
	
	/* (non-Javadoc)
	 * @see ciscoop.expressions.symbols.Symbol#evaluate()
	 */
	@Override
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		return null;
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
