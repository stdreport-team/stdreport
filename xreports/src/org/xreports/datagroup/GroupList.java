/**
 * 
 */
package org.xreports.datagroup;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Evaluator;
import org.xreports.expressions.symbols.Field;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.engine.DataException;
import org.xreports.engine.ResolveException;

/**
 * Mantiene una lista di {@link Group} appartenentio allo stesso modello. Le
 * caratteristiche della classe sono:
 * <ul>
 * <li>ha un {@link GroupModel} di riferimento (vedi {@link #getModel()} ), che
 * verrà usato per creare nuove istanze con {@link #addGroupInstance(String)}</li>
 * <li>ha un riferimento al gruppo radice, {@link RootGroup}, padre dell'intera
 * gerarchia dei gruppi</li>
 * <li>appartiene ad un gruppo ( {@link #getParentGroup()} ) di cui gestisce i
 * gruppi figli istanze del modello di riferimento dato da {@link #getModel()}</li>
 * </ul>
 */
public class GroupList implements Evaluator, Serializable {
  /**
   * 
   */
  private static final long    serialVersionUID = 7110107397596002717L;
  /**
   * mappa che mantiene l'associazione chiave<->gruppo:
   * <ul>
   * <li>key: rappresentazione chiave (vedi {@link GroupModel#buildKey(Map)})</li>
   * <li>value: gruppo dati corrispondente alla chiave</li>
   * </ul>
   * */
  private Map<String, Group>   m_mappaDati      = new LinkedHashMap<String, Group>();
  /** lista di tutti i gruppi mantenuti in questa lista */
  private List<Group>          m_listaDati      = new ArrayList<Group>();

  //  private int                m_nestIndex = 0;
  //  /** usato per innestare correttamente il gruppo nel XML */
  //  private String             m_tabs      = "";

  private RootGroup            m_root;
  private Group                m_parent;

  /** modello di tutte le istanze che faranno parte di questa lista */
  private transient GroupModel m_model          = null;
  /** identificativo univoco del mio modello di gruppo */
  private Integer              m_modelId;

  /** indica se il subreport è già stato caricato */
  private boolean              m_subreportLoaded;

  public static transient int  s_listCount      = 0;

  GroupList(RootGroup root, GroupModel model, Group parent) {
    m_root = root;
    m_parent = parent;
    m_model = model;
    m_modelId = model.getModelId();
  }

  /**
   * Metodo di passaggio dei valori.
   * 
   * Se questa lista possiede un'istanza di gruppo con la stessa chiave dei
   * valori passati, le passa la mappa dei dati; altrimenti crea una nuova
   * istanza figlia e poi le passa i dati. Se il modello relativo a questa lista
   * ha il flag "all key" impostato ( {@link GroupModel#isAllKey()} ), viene
   * sempre creata un'istanza figlia, in quanto ogni record di dati è un'istanza
   * diversa.
   * 
   * @param values
   *          mappa dei dati da assegnare ai gruppi
   * @throws GroupException
   * @throws EvaluateException 
   * @throws DataException 
   */
  public void assignData(Map<String, Object> values) throws GroupException, EvaluateException {
    if (getModel().isSubreport()) {
      if (!isSubreportLoaded()) {
        caricaSubreport();        
      }
    }
    else {
      assignToGroup(values);      
    }
    
  }

  /**
   * Assegna i dati ai gruppi mantenuti da questa lista. L'assegnamento è fatto in questo modo:
   * <ol>
   *   <li>Costruisco la stringa chiave in base ai dati passati, utilizzando i dati del modello di questo GroupList</li>
   *   <li>Cerco il gruppo con la stringa passata e <br/>
   *       * se c'è, assegno a lui i dati <br/>
   *       * se non c'è, creo un nuovo gruppo e gli assegno i dati <br/>
   *   </li>
   * </ol>
   * @param values mappa nome->valore con i dati da assegnare
   * 
   * @throws GroupException
   * @throws DataException
   * @throws EvaluateException
   */
  private void assignToGroup(Map<String, Object> values) throws GroupException, EvaluateException {
    Group g = null;
    String key = null;
    boolean bAddGroup = false;
    if (getModel().isAllKey()) {
      bAddGroup = true;
    } else {
      //controllo se la chiave c'è già
      key = getModel().buildKey(values);
      if (key == null && !getModel().isNullKeyAllowed()) {
        //la chiave null non è accettata dal modello: esco senza fare nulla,
        //i dati vengono scartati
        return;
      }
      g = m_mappaDati.get(key);
      bAddGroup = (g == null);
    }

    if ( !filterEvaluate(values)) {
      //il filtro non ha accettato i dati --> esco senza fare nulla
      return;
    }

    if (bAddGroup) {
      g = addGroupInstance(key);
    }
    if (g != null) {
      g.assignData(values);
    }    
  }
  
  @SuppressWarnings("unchecked")
  private void caricaSubreport() throws GroupException, EvaluateException {
    Object queryResult = getModel().resolveSubreportData(getParentGroup());
    if (queryResult == null || isSubreportLoaded()) {
      return;
    }
    try {
      if (queryResult instanceof String) {
        loadQuery(queryResult.toString());
      } else if (queryResult instanceof List<?>) {
        //lista dati fornita dal metodo utente: non posso azzerarla 
        loadDataList((List<HashMap<String, Object>>) queryResult, false);
      }
      else {
        throw new GroupException("Nel subreport %s il simbolo query ritorna un tipo di oggetto non previsto: %s", 
            getModel().getName(), queryResult.getClass().getName());        
      }
      m_subreportLoaded = true;
    } catch (GroupException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupException(e, "errore inaspettato in caricamento subreport %s", getModel().getName());
    } 
  }

  private int loadQuery(String sql) throws GroupException {
    int recordsInDB = 0;
    try {
      List<HashMap<String, Object>> righe = getModel().getStampa().getDatabase().getRowsAsMap(sql);
      loadDataList(righe, true);
      righe.clear();
    } catch (GroupException e) {
      throw e;
    } catch (Exception e) {
      throw new GroupException(e, "Errore in caricamento dei dati SQL");
    }
    return recordsInDB;
  }

  /**
   * Carica i dati sotto forma di lista di mappe nella struttura dati
   * gruppi/campi.
   * 
   * @param righe
   *          dati da caricare
   * @param azzeraDati
   *          flga che indica se azzerare i dati (per recuperare memoria); la
   *          lista e ogni mappa viene azzerata
   * @return quantità di record caricati
   * @throws DataException
   */
  protected void loadDataList(List<HashMap<String, Object>> righe, boolean azzeraDati) throws GroupException {
    try {
      for (HashMap<String, Object> riga : righe) {
        assignToGroup(riga);
        if (azzeraDati)
          riga.clear();
      }
    } catch (GroupException e) {
      throw e;
    } catch (Exception e) {
      throw new GroupException(e, "Errore in caricamento dei dati SQL");
    }
    if (azzeraDati)
      righe.clear();
  }

  private synchronized Group addGroupInstance(String key) {
    Group g = getModel().newInstance(m_root, this);
    m_listaDati.add(g);
    m_mappaDati.put(key, g);
    g.setIndex(m_listaDati.size() - 1);
    return g;
  }

  /**
   * dati correntemente in analisi da parte del filtro: servono per la
   * valutazione dei campi
   */
  private Map<String, Object> m_currentValues = null;

  /**
   * Metodo chiamato ad ogni assegnamento di dati: se è stato impostato un
   * filtro, ritorna true se i dati correnti soddisfano la condizione, false
   * altrimenti.
   * 
   * @param values
   *          valori su cui opera il filtro
   * @return true se filtro soddisfatto, false altrimenti; in questo secondo
   *         caso i dati non vengono messi in nessun gruppo e scartati
   * @throws GroupException
   *           nel caso di problemi nella valutazione del filtro
   */
  private boolean filterEvaluate(Map<String, Object> values) throws GroupException {
    boolean bInclude = true;
    if (getModel().getFilterInclude() != null) {
      try {
        m_currentValues = values;
        Object ret = getModel().getFilterInclude().evaluate(this);
        if (ret instanceof Boolean) {
          bInclude = ((Boolean) ret).booleanValue();
        } else {
          if (ret == null) {
            throw new GroupException("assignData, errore in valutazione filtro: invece di un boolean ho un risultato null");
          }
          throw new GroupException("assignData, errore in valutazione filtro: invece di un boolean ho un risultato "
              + ret.getClass().getName());
        }
      } catch (Exception e) {
        throw new GroupException("assignData, errore in valutazione filtro: " + e.getMessage());
      }
    }
    m_currentValues = null;
    return bInclude;
  }

  //  public int getNestIndex() {
  //    return m_nestIndex;
  //  }
  //
  //  void setNestIndex(int n) {
  //    m_nestIndex = n;
  //    m_tabs = ciscoop.util.Text.getChars('\t', m_nestIndex);
  //  }
  //
  //  public String getTabs() {
  //    return m_tabs;
  //  }

  /**
   * Ritorna true se questa lista di gruppi è di istanze del gruppo col nome
   * passato.
   * 
   * @param name
   *          nome gruppo
   */
  public boolean is(String name) {
    return getModel().getName().equalsIgnoreCase(name);
  }

  /**
   * Ritorna la lista di tutte le istanze di gruppo mantenute da questo
   * GroupList
   * 
   * @return lista dei gruppi mantenuta da questo GroupList: se non ci sono
   *         gruppi torna una lista vuota
   */
  public List<Group> getInstances() {
    return m_listaDati;
  }

  /**
   * Ritorna la index.esima istanza del gruppo mantenuto da quesla lista.
   * @param index indice gruppo
   * @return gruppo richiesto; se l'indice è errato emette una {@link IndexOutOfBoundsException} .
   */
  public Group getInstance(int index) {
    return m_listaDati.get(index);
  }

  /**
   * Ritorna la prima istanza di gruppo mantenuta da questo GroupList.
   * 
   * @return il primo gruppo di questa lista
   */
  public Group getFirst() {
    if (m_listaDati.size() > 0) {
      return m_listaDati.get(0);
    }
    return null;
  }

  /**
   * Ritorna l'ultimo gruppo, cioè quello alla fine della linked list che
   * mantiene tutte le istanze di questo tipo di gruppo.
   * 
   * @return l'ultimo gruppo
   */
  public Group getLast() {
    if (m_listaDati.size() > 0) {
      return m_listaDati.get(m_listaDati.size() - 1);
    }
    return null;
  }

  /**
   * Quantità totale di istanze di questa tipologia di gruppo.
   * 
   * @return qta istanze
   */
  public int getCount() {
    return m_listaDati.size();
  }

  public List<Group> getSublist(int fromIndex, int toIndex) {
    try {
      return m_listaDati.subList(fromIndex, toIndex);
    } catch (Exception e) {
      //se gli indici sono errati, per adesso torno semplicemente lista vuota
      return new ArrayList<Group>();
    }
  }

  public String getPath() {
    String path = "";
    if (m_parent != null)
      path = m_parent.getPath() + ".";
    return path;
  }

  public String getKeysPath() {
    String path = "";
    if (m_parent != null)
      path = m_parent.getKeysPath() + ".";
    return path;
  }

  /**
   * Ritorna il gruppo padre di questa lista.
   */
  public Group getParentGroup() {
    return m_parent;
  }

  public void destroy() {
    for (Group g : m_mappaDati.values()) {
      g.destroy();
    }
    m_mappaDati.clear();
    m_mappaDati = null;
    m_listaDati.clear();
    m_listaDati = null;
    m_root = null;
    m_parent = null;
    m_model = null;
  }

  //  public StringBuffer toXML() {
  //    StringBuffer sXML = new StringBuffer();
  //    for (Group g : m_listaDati) {
  //      sXML.append(g.toXML());
  //    }
  //    return sXML;
  //  }

  public GroupModel getModel() {
    if (m_model == null) {
      m_model = getParentGroup().getRootGroup().getModelFromCache(m_modelId);
    }
    return m_model;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "GroupList " + getModel().getName() + ", #elems " + m_listaDati.size();
  }

  /*
   * (non-Javadoc)
   * @see
   * ciscoop.expressions.symbols.Evaluator#evaluate(ciscoop.expressions.symbols
   * .Symbol)
   */
  @Override
  public Object evaluate(Symbol symbol) throws ResolveException {
    //Si deve usare questa classe per valutare i campi. Infatti solo un subset
    //dei campi è accettabile. Ad esempio non si possono usare campi con la 
    //specifica del gruppo: i campi sono solo di questo gruppo.
    //Inoltre i valori attuali dei campi ancora non sono assegnati al gruppo, 
    //quindi uso la mappa che mi arriva direttamente dalla query.

    try {
      if (symbol.isField()) {
        Field field = (Field) symbol;
        return calcFieldValue(field);
      }

      Evaluator eval = getModel().getFilterEvaluator();
      if (symbol.isConstant()) {
        return eval.evaluate(symbol);
      } else if (symbol.isMethodCall()) {
        return eval.evaluate(symbol);
      }
    } catch (GroupException e) {
      throw new ResolveException(e);
    }

    return null;
  }

  private Object calcFieldValue(Field field) throws GroupException {
    String nomeGruppo = field.getGroup();
    if (nomeGruppo == null) {
      nomeGruppo = "";
    }
    if (nomeGruppo.length() > 0) {
      throw new GroupException("%s: non si può specificare un nome di gruppo in un campo che fa parte del filtro.", field.toString());
    }
    if (m_currentValues == null) {
      //qui non si dovrebbe mai arrivare ma... non si sa mai!
      throw new GroupException("%s: errore imprevisto: i dati sono null!", field.toString());
    }

    String nomeCampo = field.getField();
    if ( !m_currentValues.containsKey(nomeCampo)) {
      nomeCampo = nomeCampo.toLowerCase();
      if ( !m_currentValues.containsKey(nomeCampo)) {
        nomeCampo = nomeCampo.toUpperCase();
        if ( !m_currentValues.containsKey(nomeCampo)) {
          throw new GroupException("%s: il campo definito nel filtro non è presente nei dati di input.", field.toString());
        }
      }
    }
    Object value = m_currentValues.get(nomeCampo);

    return value;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    s_listCount++;
    out.defaultWriteObject();
  }

  public boolean isSubreportLoaded() {
    return m_subreportLoaded;
  }
}
