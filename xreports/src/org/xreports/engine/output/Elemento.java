package org.xreports.engine.output;

import java.util.List;

import org.xreports.engine.output.impl.GenerateException;


public interface Elemento {

  /**
   * Metodo di utilit� per aggiungere una lista di figli invece che un unico figlio.
   * 
   * @param children
   *          elementi figli
   * @throws GenerateException
   */
  public void addElements(List<Elemento> children) throws GenerateException;

  /**
   * Metodo utilizzato per aggiungere un elemento figlio a questo. Un elemento, durante la fase di generazione dell'output, chiama
   * questo metodo (oppure il corrispondente {@link #addElements(List)} per aggiungere gli elementi di output figli a se
   * stesso. L'elenco degli elementi di output � ritornato dal metodo
   * {@link Generable#generate(ciscoop.util.builder.Gruppo, ciscoop.stampa.Stampa, Elemento)} di tutti gli elementi di input che
   * implementano l'interfaccia {@link Generable}.
   * 
   * @param child
   *          elemento da aggiungere come figlio di questo
   * @throws GenerateException
   *           nel caso di impossibilit� o errori gravi in aggiunta
   */
  public void addElement(Elemento child) throws GenerateException;

  /**
   * Ritorna l'elemento che fisicamente sar� aggiunto al suo padre nella gerarchia degli oggetti finali. <br>
   * Normalmente questo metodo � chiamato dal padre di questo elemento durante il metodo {@link #addElement(Elemento)}. Se lo
   * specifico elemento � composto da una gerarchia di oggetti, questo metodo dovr� tornare quello top-level, in quanto sar� quello
   * aggiunto al suo padre. <br>
   * NOTA BENE: questo elemento pu� anche tornare null, nel caso non corrisponda ad alcun elemento fisico
   * 
   * @param parent
   *          elemento padre; questo pu� servire in alcuni elementi quando a seconda del padre
   *          a cui deve essere aggiunto il figlio, venga tornato un tipo di elemento o un altro (vedi TabellaIText) 
   * @throws GenerateException
   *           nel caso ci siano errori gravi in creazione elemento
   */
  public Object getContent(Elemento parent) throws GenerateException;

  /**
   * Metodo chiamato dal documento appena l'elemento � stato aggiunto al documento stesso.
   * 
   * @param doc
   *          documento a cui � stato aggiunto l'elemento
   * @throws GenerateException
   */
  public void flush(Documento doc) throws GenerateException;

  /**
   * Ritorna identificativo univoco di questo elemento nell'ambito dell'intero documento.
   */
  public String getUniqueID();

  /**
   * Ritorna l'elemento padre di questo, impostato con {@link #setParent(Elemento)}. L'elemento radice, {@link Documento}, ha parent
   * <var>null</var>.
   */
  public Elemento getParent();

  /**
   * Imposta l'elemento padre. E' possibile reperirlo poi con {@link #getParent()}.
   * 
   * @param elem
   *          elemento padre da impostare
   */
  public void setParent(Elemento elem);

  /**
   * Metodo chiamato dall'elemento contenitore di questo per informarlo che la generazione di questo elemento � terminata e sta per
   * essere aggiunto al padre. Il padre pu� essere un altro elemento o il documento stesso.
   */
  public void fineGenerazione() throws GenerateException;
}
