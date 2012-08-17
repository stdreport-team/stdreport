package org.xreports.engine.source;

import java.util.List;

import org.xreports.stampa.validation.ValidateException;

/**
 * Questa interfaccia identifica tutti gli elementi composti del sorgente del report. <br/>
 * Un elemento composto è un elemento che può avere sotto-elementi, come ad esempio i tag &lt;text&gt; e &lt;table&gt;.
 * 
 * @author pier
 * 
 */
public interface IReportElement extends IReportNode {

  /**
   * Aggiunge un Nodo alla lista dei suoi figli. Ricordiamo che solo gli {@link IReportElement} possono avere figli mentre gli
   * {@link IReportNode} no.
   * 
   * @param reportNode
   *          Nodo da inserire tra i figli di questo IReportElement
   */
  public void addChild(IReportNode reportNode) throws ValidateException;

  /**
   * Ritorna la lista dei figli di questo elemento. Se un elemento non ha figli, non dovrebbe tornare null ma una lista vuota.
   */
  public List<IReportNode> getChildren();

  /**
   * Ritorna la quantità di figli di questo elemento. 
   */
  public int getChildCount();
  
  /**
   * Ritorna il figlio di questo elemento che è alla posizione indicata
   * @param index indice filgio (0-based)
   * @return il figlio di questo elemento che è alla posizione indicata, oppure null se la posizione è errata
   */
  public IReportNode getChild(int index);
  
  /**
   * Ritorna l'indice del nodo passato fra i figli di questo elemento
   * @param node nodo figlio che si sta cercando
   * @return indice (0-based) del nodo fra i figli di questo elemento oppure -1 se non c'è
   */
  public int indexOf(IReportNode node);
  
  /**
   * Getter e Setter dell'indice dell'elemento figlio che si stava processando quando è stata sospesa la creazione della stampa
   * (tipico caso dell'elemento newpage). Serve perpoter riprendere la stampa dal punto in cui si è sospesa.
   */
  public int getProcessingChildIndex();

  public void setProcessingChildIndex(int childIndex);

  /**
   * Metodo chiamato dal parser del sorgente XML quando incontra il tag di chiusura di questo elemento. Qui ogni specifico tag può
   * fare elaborazioni personalizzate.
   */
  public void fineParsingElemento();

  /** Metodi per la generazione dell'xml di debug (per ora e eventuale XML dei dati in futuro) */
  public String getXMLAttrs();

  /**
   * Ritorna una rappresentazione testuale del tag di apertura di questo elemento 
   */
  public String getXMLOpenTag();

  /**
   * Ritorna una rappresentazione testuale del tag di chiusura di questo elemento 
   */
  public String getXMLCloseTag();
}
