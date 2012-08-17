package org.xreports.engine.source;

import java.util.List;

import org.xreports.datagroup.Group;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;

public interface Generable {

  /**
   * Da implementare per generare l'elemento finale corrisponde all'elemento che implementa questa interfaccia. <br>
   * Ad esempio se sono in un elemento {@link TableElement}, l'implementazione del generatore corrente deve generare un suo oggetto
   * specifico, ad esempio una tabella PDF o Excel (vedi {@link Tabella}), che andrà a formare il documento di output.
   * 
   * @param gruppo
   *          gruppo corrente; il gruppo di partenza è un {@link RootGroup}.
   * @param stampa
   *          riferimento all'oggetto principale che genera il PDF; può servire per risolvere costanti globali.
   * 
   * @return elemento creato; tale elemento viene poi aggiunto al documento al momento opportuno
   * @throws GenerateException
   *           nel caso di incongruenze o impossibilità nella risoluzione di valori.
   */
  public List<Elemento> generate(Group gruppo, XReport report, Elemento padre) throws GenerateException;

}
