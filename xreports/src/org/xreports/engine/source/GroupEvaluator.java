/**
 * 
 */
package org.xreports.engine.source;

import org.xreports.datagroup.Group;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.engine.ResolveException;

/**
 * Interfaccia per oggetti che valutano simboli nel contesto di un gruppo
 * 
 * @author pier
 *
 */
public interface GroupEvaluator {

  /**
   * Valuta un simbolo nel contesto di un gruppo.
   * 
   * Se un simbolo non è previsto, ritorna null.
   * 
   * @param symbol
   *          simbolo da valutare
   * @param group
   *          istanza del gruppo in cui deve essere valutato il simbolo
   *          
   * @return valore risultato della valutazione di symbol nel contesto di group; torna null se 
   * il simbolo non può essere valutato
   * 
   * @throws ResolveException nel caso di errore grave in valutazione
   */  
  public Object evaluate(Symbol symbol, Group group) throws ResolveException;

}
