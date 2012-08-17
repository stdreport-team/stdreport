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
public class NumberLiteral extends UnsignedMathValue {
	private Double m_doubleValue;
	private Long m_longValue;
	private boolean m_decimal = false;
	
	public NumberLiteral(Token t) throws ParseException {
	  super(t, TokenType.Number);
		try {
			m_longValue = Long.valueOf(getText());
			m_doubleValue = m_longValue.doubleValue();
		} catch (NumberFormatException e) {
			//is not a long!
			m_doubleValue = Double.valueOf(getText());
			m_longValue = m_doubleValue.longValue();
			m_decimal = true;
		}			
	}
	
	/**
	 * True if this literal has a decimal part.
	 */
	public boolean isDecimal() {
		return m_decimal;
	}
	
	public long getAsLong() {
		return m_longValue;
	}

	public double getAsDouble() {
		return m_doubleValue;
	}
	
	public Object evaluate(Evaluator evaluator) {
		if (isDecimal())
			return m_doubleValue;
		else
			return m_longValue;
	}

	public boolean isLiteral() {
		return true;
	}
	
	public boolean isNumberLiteral() {
		return true;
	}
	
	
}
