package org.xreports.engine.output;

public interface Cella extends Elemento {

  /**
   * Ritorna l'indice (0-based) di questa colonna all'interno della generica riga. 
   * Cio� se questa � la prima colonna delle righe ritorna 0, se � la seconda ritorna 1, etc.
   */
  public int getColIndex();

  /**
   * Ritorna l'indice (0-based) della riga a cui appartiene questa colonna. 
   * Cio� se questa colonna � nella prima riga ritorna 0, se � nella seconda ritorna 1, etc.
   */
  public int getRowIndex();
  
}
