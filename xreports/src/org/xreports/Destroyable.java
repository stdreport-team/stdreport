package org.xreports;

public interface Destroyable {

  /**
   * Ha il compito di eliminare tutti i riferimenti a oggetti esterni, 
   * per facilitare il compito al GC e liberare prima la memoria occupata.
   */
  public void destroy();
}
