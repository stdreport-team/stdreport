/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.engine.ResolveException;


/**
 * @author pier
 *
 */
public interface Evaluator {

	public Object evaluate(Symbol symbol) throws ResolveException;
}
