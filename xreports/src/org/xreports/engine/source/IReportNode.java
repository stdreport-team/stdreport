package org.xreports.engine.source;

/**
 * Rappresenta l'interfaccia per tutti gli elementi significativi del report XML. <br/>
 * Ogni nodo del report pu� essere o un elemento composto, cio� che pu� contenere altri elementi, o un elemento semplice cio� che
 * non pu� contenere altri elementi. <br/>
 * Esempi di elementi composti sono i tag &lt;text&gt; e &lt;table&gt; , mentre elementi semplici sono ad esempio i tag
 * &lt;field&gt; e i blocchi di testo libero non racchiusi da alcun tag.
 * 
 * <br>
 * Vedi {@link AbstractNode}.
 * 
 */
public interface IReportNode extends Generable {
  /**
   * Getter e setter per recuperare/settare il padre di questo {@link IReportNode}. L'unico Node a non avere padre � l'
   * {@link IReportNode} radice.
   */
  public void setParent(IReportElement reportElement);

  public IReportElement getParent();

  /**
   * Permette di recuperare {@link IReportElement} padre/antenato pi� vicino che � di classe "classe"
   * 
   * @param classe
   *          classe Java richiesta
   * @return elemtno trovato oppure null se non trovato
   */
  public IReportElement closest(Class<? extends IReportElement> classe);

  /**
   * Indica se l'oggetto in questione � un Element oppure un Node.
   * Un element � un Node con in pi� la possibilit� di avere figli (sia Node
   * che Element).
   * 
   * @return <b>true</b> se � un Element, <b>false</b> se � un Node
   */
  public boolean isElement();

  public void setDebugData(boolean debugMode);

  public boolean isDebugData();

  /** Metodi per la generazione dell'xml di debug (per ora e eventuale XML dei dati in futuro) */
  public String toXML();
}
