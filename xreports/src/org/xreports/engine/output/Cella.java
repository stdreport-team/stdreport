package org.xreports.engine.output;

public interface Cella extends Elemento {

  /**
   * Ritorna l'indice (0-based) di questa colonna all'interno della generica riga. 
   * Cioè se questa è la prima colonna delle righe ritorna 0, se è la seconda ritorna 1, etc.
   */
  public int getColIndex();

  /**
   * Ritorna l'indice (0-based) della riga a cui appartiene questa colonna. 
   * Cioè se questa colonna è nella prima riga ritorna 0, se è nella seconda ritorna 1, etc.
   */
  public int getRowIndex();
  
}
