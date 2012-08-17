/**
 * 
 */
package org.xreports.datagroup;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xreports.Destroyable;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Evaluator;
import org.xreports.expressions.symbols.MethodCall;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.engine.XReport;

/**
 * Modella il concetto di 'gruppo di dati'. Un gruppo è un insieme di campi
 * caratterizzati da un nome, un tipo di dato e un valore; i dati sono passati
 * tramite il metodo {@link #assignData(Map)}. Un gruppo è formato da un insieme
 * di campi che possono essere di 3 tipi:
 * <ul>
 * <li>campi chiave: i campi che identificano una singola istanza di gruppo</li>
 * <li>campi di output: i campi memorizzati all'interno del gruppo e che
 * provengono dai dati passati in input</li>
 * <li>campi automatici: campi calcolati in automatico (somma, divisione,
 * moltiplicazione, percentuale, conteggio, etc) oppure calcolati da classi
 * utente</li>
 * </ul>
 * Un modello inoltre ha un elenco di modelli figli, che si possono aggiungere
 * tramite {@link #addChildModel(String)}; il modello padre è accessibile
 * tramite {@link #getParent()}. <br/>
 * Un modello a run-time ha varie istanze rappresentate dalla classe
 * {@link Group}, ognuna con dei campi chiave univoci; un modello può avere più
 * modelli figli, e tutti formano una struttura gerarchica a capo della quale
 * c'è un oggetto {@link RootModel}, che è una sub-classe di Gruppo.
 * 
 * @author pier
 * 
 */
public class GroupModel implements Serializable, Destroyable {

  /**
   * 
   */
  private static final long            serialVersionUID  = 5322004682696359888L;

  /**
   * Mappa dei gruppi figli di questo gruppo. Ogni gruppo appartenente alla
   * mappa è l'elemento iniziale della linked list di tutte le istanze di quel
   * gruppo.
   */
  private Map<String, GroupModel>      m_childModels     = new LinkedHashMap<String, GroupModel>();

  /**
   * Mappa di tutti i campi di questo modello.
   */
  private Map<String, DataFieldModel>  m_fields          = new HashMap<String, DataFieldModel>();
  /**
   * Mappa id->DataFieldModel che mantiene una cache per chiave dei campi di
   * questo oggetto
   */
  private Map<Integer, DataFieldModel> m_fieldsCache     = new HashMap<Integer, DataFieldModel>();

  private int                          m_modelCacheId;

  /**
   * Mappa dei nomi dei campi chiave definiti per questo modello.
   */
  private Set<String>                  m_keys            = new LinkedHashSet<String>();

  /** nome di questo gruppo */
  private String                       m_name            = null;

  /** modello padre di questo */
  private GroupModel                   m_parent          = null;

  /**
   * se true, indica che tutti i campi sono chiave, quindi ogni record di dati
   * genera un nuovo gruppo
   */
  private boolean                      m_allKey          = false;

  /** modalità debug */
  private boolean                      m_debugMode       = false;

  /** id univoco di questo modello nella cache mantenuta da {@link RootModel} */
  private Integer                      m_modelId;

  /** espressione che contiene filtro di inclusione dei record */
  private Symbol                       m_filterInclude   = null;

  /** espressione che contiene filtro di inclusione dei record */
  private Evaluator                    m_filterEvaluator = null;

  /** flag di accettazione di chiavi null */
  private boolean                      m_nullKeyAllowed;

  private Symbol                       m_subreportQuery;                                           //*SUBR*
  private Stampa                       m_stampa;                                                   //*SUBR*

  /**
   * Costruttore standard di un modello di gruppo non subreport.
   * @param name nome del gruppo
   * @param parent modello padre
   */
  public GroupModel(String name, GroupModel parent) {
    setName(name);
    m_parent = parent;
    m_modelCacheId = 0;
  }

  /**
   * Costruttore standard di un modello di gruppo <b>subreport</b>.
   * @param name nome del gruppo
   * @param parent modello padre
   * @param querySymbol simbolo dell'attributo "query" che definisce la sorgente dati del subreport
   * @param evaluator oggetto valutatore di querySymbol: ha il compito di valutarlo a tempo debito e ritornarne
   * i dati costitutivi del subreport
   */
  public GroupModel(String name, GroupModel parent, Symbol querySymbol, Evaluator evaluator, Stampa stampa) {
    setName(name);
    m_parent = parent;
    m_modelCacheId = 0;
    m_subreportQuery = querySymbol;
    m_stampa = stampa;
  }
  
  /**
   * Indica se queto modello è un subreport
   * @return true sse questo è un modello di subreport
   */
  public boolean isSubreport() {
    return m_subreportQuery != null;
  }
  
  /**
   * Risolve l'attributo "query" di un subreport eseguendo il metodo definito e ritornando il suo risultato.
   * 
   * @return risultato della valutazione dell'attributo "query"
   * 
   * @throws EvaluateException se errori gravi in esecuzione del metodo definito in "query"
   * @throws GroupException se questo modello non è un subreport
   */
  public Object resolveSubreportData(Group g) throws GroupException, EvaluateException {
    if (isSubreport()) {
      MethodCall mc = (MethodCall)m_subreportQuery.getChild(0);
      return m_stampa.resolveValueUserCall(mc.getClassRef(), mc.getMethodRef(), g, null);
    }
    throw new GroupException("resolveSubreportData: " + getName() + " non è un subreport");
  }
  
  /**
   * Istanzia un nuovo Group basato su questo modello.
   * 
   * @param root
   *          gruppo radice di riferimento
   * @param list
   *          lista di cui fa parte il gruppo
   * @return Group creato
   */
  public Group newInstance(RootGroup root, GroupList list) {
    Group g = new Group(root, this, list);
    g.addKeyFields(getKeyFields());
    g.addFields(getNotKeyFields());
    return g;
  }

  /**
   * Ritorna true sse questo GroupModel è il RootModel oppure, se questa classe
   * è un'istanza di Group, ritorna true sse questo è un RootGroup.
   * 
   * @return true se questa classe è un RootModel o un
   */
  public boolean isRoot() {
    return false;
  }

  public Collection<GroupModel> getChildModels() {
    return m_childModels.values();
  }

  /**
   * Aggiunge i campi i cui nomi sono nell'array passato, all'elenco dei campi
   * chiave. <br>
   * I campi vengono creati senza tipo e senza impostazioni do formattazione.
   * 
   * Se uno dei campi esiste già, emette exception.
   * 
   * @param out
   *          elenco dei nomi dei campi da aggiungere.
   * @throws GroupException
   *           nel caso il campo esista già
   */
  public void addKeyFields(String[] chiavi) throws GroupException {
    for (int i = 0; i < chiavi.length; i++) {
      addKeyField(chiavi[i]);
    }
  }

  /**
   * Ritorna la quantità di campi chiave di questo gruppo
   * 
   * @return qta campi chiave
   */
  public int keyCount() {
    return m_keys.size();
  }

  public int fieldsCount() {
    return m_fields.size();
  }

  /**
   * Restituisce il campo di cui è passato il nome. Il campo può essere chiave,
   * di output o calcolato.
   * 
   * @param nome
   *          nome del campo richiesto (<b>non</b> è case-sensitive)
   * @return campo richiesto oppure null se non trovato
   */
  public DataFieldModel getCampo(String nome) {
    return m_fields.get(nome.toLowerCase());
  }

  public boolean existCampo(String nomeCampo) {
    return getCampo(nomeCampo) != null;
  }

  /**
   * Ritorna true se il campo con il nome passato è chiave in questo gruppo.
   * 
   * @param nomeCampo
   *          nome del campo (è case-insensitive)
   * @return true se il campo è chiave
   */
  public boolean isKey(String nomeCampo) {
    return m_keys.contains(nomeCampo.toLowerCase());
  }

  /**
   * Ritorna true se il campo passato è chiave in questo gruppo.
   * 
   * @param campo
   *          campo da testare
   * @return true se il campo è chiave
   */
  public boolean isKey(DataFieldModel campo) {
    return isKey(campo.getNome());
  }

  /**
   * Ritorna un set ordinato (cioè navigabile dalla prima all'ultima chiave nel
   * corretto ordine) dei nomi di tutti i campi chiave.
   */
  public Set<String> getKeyFieldNames() {
    return m_keys;
  }

  /**
   * Recupera i campi del modello. <br/>
   * Nota: {@link #getFields() getFields} = {@link #getNotKeyFields()
   * getNotKeyFields} + {@link #getKeyFields() getKeyFields}
   * 
   * @return elenco di tutti i DataField di questo modello
   */
  public Collection<DataFieldModel> getFields() {
    return m_fields.values();
  }

  /**
   * Recupera i campi chiave.
   * 
   * @return tutti i DataField definiti 'chiave' di questo modello
   */
  public List<DataFieldModel> getKeyFields() {
    List<DataFieldModel> chiavi = new LinkedList<DataFieldModel>();
    for (String nome : m_keys) {
      chiavi.add(getCampo(nome));
    }
    return chiavi;
  }

  /**
   * Recupera i campi non chiave
   * 
   * @return tutti i DataField *NON* definiti 'chiave' di questo modello
   */
  public List<DataFieldModel> getNotKeyFields() {
    List<DataFieldModel> campiNC = new LinkedList<DataFieldModel>();
    for (DataFieldModel c : m_fields.values()) {
      if ( !c.isKey())
        campiNC.add(c);
    }
    return campiNC;
  }

  /**
   * Aggiunge il campo passato alla chiave o ai campi di output, controllando
   * che non esiste già, sia fra i campi chiavi che i campi di output che i
   * campi automatici.
   * 
   * @param c
   *          campo da aggiungere
   * @param isKey
   *          true se è chiave, false se di output
   * @return campo aggiunto o exception se c'è già
   * @throws GroupException
   *           nel caso il campo esista già
   */
  protected synchronized DataFieldModel _addFieldInternal(DataFieldModel c, boolean isKey) throws GroupException {
    if (getCampo(c.getNome()) != null) {
      throw new GroupException("Il campo '" + c.getNome() + "' esiste già nel gruppo " + getName() + ": non puoi aggiungerlo.");
    }
    m_modelCacheId++;
    m_fieldsCache.put(m_modelCacheId, c);
    c.setModelId(Integer.valueOf(m_modelCacheId));
    m_fields.put(c.getNome().toLowerCase(), c);
    if (isKey)
      m_keys.add(c.getNome().toLowerCase());
    return c;
  }

  /**
   * Aggiunge il campo di cui è passato il nome, all'elenco dei campi chiave di
   * questo gruppo. <br>
   * Il campo viene creato senza tipo e senza impostazioni di formattazione.
   * 
   * @param c
   *          campo da aggiungere
   * @return campo aggiunto
   * @throws GroupException
   */
  public DataFieldModel addKeyField(String name) throws GroupException {
    return _addFieldInternal(new DataFieldModel(this, name), true);
  }

  /**
   * Nome di questro gruppo. E' il nome del tag che avrà il gruppo nella
   * rappresentazione XML.
   * 
   * @return il nome di questo gruppo
   */
  public String getName() {
    return m_name;
  }

  void setName(String nome) {
    if (nome == null || nome.length() == 0)
      throw new IllegalArgumentException("Devi specificare un nome per il gruppo.");
    if ( !RootGroup.checkNome(nome))
      throw new IllegalArgumentException("Il nome '" + nome + "' specificato per il gruppo non è valido.");
    m_name = nome;
  }

  /**
   * Aggiunge al gruppo un campo con il nome e tipo dati. <br/>
   * Il campo è *NON* chiave.
   * 
   * @param name
   *          nome campo
   * @param tipo
   *          tipo del campo
   * @return il campo aggiunto
   * @throws GroupException
   *           nel caso il campo esista già
   */
  public DataFieldModel addField(String name, DataFieldModel.TipoCampo tipo) throws GroupException {
    return _addFieldInternal(new DataFieldModel(this, name, tipo), false);
  }

  /**
   * Aggiunge al gruppo un campo con il nome dato e tipo indefinito
   * {@link DataFieldModel.TipoCampo#UNKNOWN} . <br/>
   * Il campo è *NON* chiave.
   * 
   * @param name
   *          nome del campo
   * @return il campo aggiunto
   * @throws GroupException
   *           nel caso il campo esista già
   */
  public DataFieldModel addField(String name) throws GroupException {
    return addField(name, DataFieldModel.TipoCampo.UNKNOWN);
  }

  /**
   * Metodo identico ad {@link #addField(String)} con l'unica differenza che non
   * ritorna exception se il campo esiste già
   * 
   * @param name
   *          nome del campo da aggiungere
   * @return oggetto campo aggiunto oppure oggetto campo esistente se esisteva
   *         già
   */
  public DataFieldModel addFieldSafe(String name) {
    try {
      return addField(name, DataFieldModel.TipoCampo.UNKNOWN);
    } catch (GroupException e) {
      return getCampo(name);
    }
  }

  /**
   * Metodo identico ad {@link #addField(String)} con l'unica differenza che non
   * ritorna exception se il campo esiste già
   * 
   * @param name
   *          nome del campo da aggiungere
   * @return oggetto campo aggiunto oppure oggetto campo esistente se esisteva
   *         già
   */
  public DataFieldModel addFieldAutoSafe(String name) {
    try {
      return addFieldAuto(name, DataFieldModel.TipoCampo.UNKNOWN);
    } catch (GroupException e) {
      return getCampo(name);
    }
  }

  /**
   * Crea un campo calcolato con il nome e il tipo passati, e lo aggiunge a
   * questo gruppo; inoltre gli assegna lo UserCalcListener specificato nel
   * terzo parametro.
   * 
   * @param name
   *          nome del campo
   * @param tipo
   *          tipo del campo
   * @param userCalcL
   *          istanza di un oggetto che ha il compito di calcolare il valore del
   *          campo
   * @return campo aggiunto
   * @throws GroupException
   *           nel caso il campo esista già
   */
  public DataFieldAutoModel addFieldAuto(String name, DataFieldModel.TipoCampo tipo, UserCalcListener userCalcL)
      throws GroupException {
    DataFieldAutoModel cTemp = (DataFieldAutoModel) _addFieldInternal(new DataFieldAutoModel(this, name, tipo), false);
    cTemp.setUserCalc(userCalcL);
    return cTemp;
  }

  /**
   * Crea un campo calcolato con il nome e il tipo passati e lo aggiunge a
   * questo gruppo.
   * 
   * @param name
   *          nome del campo
   * @param tipo
   *          tipo del campo
   * @return campo aggiunto
   * @throws GroupException
   *           nel caso il campo esista già
   */
  public DataFieldAutoModel addFieldAuto(String name, DataFieldModel.TipoCampo tipo) throws GroupException {
    return new DataFieldAutoModel(this, name, tipo);
  }

  /**
   * Aggiunge il campo calcolato passato a questo modello.
   * 
   * @param c
   *          campo da aggiungere
   * @return campo aggiunto, lo stesso passato come argomento
   * @throws GroupException
   *           nel caso il campo esista già
   */
  DataFieldAutoModel addFieldAuto(DataFieldAutoModel c) throws GroupException {
    return (DataFieldAutoModel) _addFieldInternal(c, false);
  }

  //  /**
  //   * Aggiunge il campo passato all'elenco dei campi di output di questo gruppo. <br>
  //   * Il campo passato viene clonato e viene aggiunta la sua copia. <br/>
  //   * NB: la clonazione copia tutto il campo eccetto il valore.
  //   * <br/>
  //   * NB: il campo *NON* è chiave.
  //   * 
  //   * @param c
  //   *          campo da aggiungere
  //   * @return campo aggiunto: è sempre un'istanza diversa dal campo passato
  //   */
  //  public DataFieldModel addField(DataFieldModel c) {
  //    // faccio una copia del campo: copia tutto eccetto valore
  //    DataFieldModel clone = null;
  //    try {
  //      clone = (DataFieldModel) c.clone();
  //    } catch (CloneNotSupportedException e) {
  //      throw new IllegalStateException("Il campo '" + c.getNomeEsteso() + "' non può essere clonato.");
  //    }
  //    return _addFieldInternal(clone, false);
  //  }

  /**
   * Distrugge questo oggetto eliminando tutti i riferimenti a oggetti esterni e
   * azzerando le strutture dati interne.
   */
  @Override
  public void destroy() {
//    if (m_debugMode)
//      System.out.println("Distruggo " + this.toString());
    //distruggo i miei campi
    if (m_fields != null) {
      for (DataFieldModel c : m_fields.values()) {
        c.destroy();
      }
      m_fields.clear();
      m_fields = null;      
    }
    if (m_keys != null) {
      m_keys.clear();
      m_keys = null;
    }

    //distruggo i miei sottogruppi e tutte le loro istanze
    if (m_childModels != null) {
      for (GroupModel gru : m_childModels.values()) {
        gru.destroy();
      }
      m_childModels.clear();
      m_childModels = null;      
    }

    //azzero i riferimenti a oggetti esterni
    m_parent = null;
  }

  /**
   * Imposta modalità debug. Se modalità debug è true, vengono emessi su
   * standard output messaggi informativi durante varie operazioni.
   * 
   * @param debugMode
   *          modalità debug
   */
  public void setDebugMode(boolean debugMode) {
    m_debugMode = debugMode;
  }

  /**
   * Restituisce modalità debug corrente.
   */
  public boolean isDebugMode() {
    return m_debugMode;
  }

  /**
   * Ritorna il gruppo modello padre di questo.
   */
  public GroupModel getParent() {
    return m_parent;
  }

  public String getPath() {
    String path = "";
    if (getParent() != null)
      path = getParent().getPath() + ".";
    path += getName();
    return path;
  }

  /**
   * Aggiunge un modello figlio di questo col nome passato.
   * 
   * @param name
   *          nome del modello figlio; deve essere univoco fra i modelli figli
   *          di questo.
   * @return modello creato; è un modello vuoto, senza definizione di campi
   */
  public GroupModel addChildModel(String name, Symbol querySymbol, Evaluator evaluator, Stampa stampa) {
    if (m_childModels.containsKey(name.toLowerCase()))
      throw new IllegalArgumentException("Il gruppo '" + name + "' esiste già come gruppo figlio di '" + getName() + "'");
    GroupModel g = new GroupModel(name, this, querySymbol, evaluator, stampa);
    g.setDebugMode(isDebugMode());
    Integer cacheId = addModelToCache(g);
    g.setModelId(cacheId);
    m_childModels.put(name.toLowerCase(), g);
    return g;
  }

  public GroupModel addChildModel(String name) {
    return addChildModel(name, null, null, null);        
  }
  
  
  /**
   * Aggiunge il modello passato alla cache mantenuta dal root model.
   * 
   * @param model
   *          modello da aggiungere
   * @return id univoco di questo modello nella cahce; ritorna null se il
   *         modello non viene aggiunto perchè già esistente
   */
  protected synchronized Integer addModelToCache(GroupModel model) {
    return getParent().addModelToCache(model);
  }

  /**
   * Costruisce la stringa che rappresenta tutti i campi chiave per questo
   * modello. <br/>
   * Se i valori passati non hanno uno o più campi chiave per questo modello,
   * viene ritornato null. <br/>
   * Se i campi chiave invece ci sono tutti, viene tornata una stringa univoca,
   * cioè:
   * <ul>
   * <li>se 2 chiavi sono diverse <--> le due stringhe sono diverse</li>
   * <li>se 2 chiavi sono uguali <--> le due stringhe sono uguali</li>
   * </ul>
   * 
   * @param values
   *          riga dei dati da cui costruire la stringa della chiave
   * @return stringa che rappresenta univocamente la chiave, oppure null se
   *         alcuni campi chiave mancano o sono null; nel caso
   *         {@link #isNullKeyAllowed()} sia true, non ritorna null se alcuni
   *         campi chiave sono null, ma solo se sono assenti dalla mappa dei
   *         valori.
   */
  public String buildKey(Map<String, Object> values) {
    StringBuffer sb = new StringBuffer();
    for (String key : m_keys) {
      if ( !values.containsKey(key)) {
        return null;
      }
      Object obj = values.get(key);
      if (obj != null)
        sb.append(obj.toString());
      else {
        //caso di campo chiave esistente ma con valore null
        if (isNullKeyAllowed()) {
          //campi null ammessi: non faccio nulla
          //poi viene aggiunto il separatore del campo
        } else {
          return null;
        }
      }
      sb.append('\u0000');
    }
    return sb.substring(0, sb.length() - 1);
  }

  /**
   * Indica se questo gruppo ha impostato il falg di 'allKey' (cioè 'tutti i
   * campi chiave'). Se si, ogni record di dati genera un nuovo gruppo
   * automaticamente.
   * 
   * @return il flag 'allKey'
   */
  public boolean isAllKey() {
    return m_allKey;
  }

  /**
   * Imposta il flag 'allKey'
   * 
   * @param allKey
   *          the allKey to set
   */
  public void setAllKey(boolean allKey) {
    m_allKey = allKey;
  }

  public boolean is(String name) {
    return getName().equalsIgnoreCase(name);
  }

  /**
   * Ritorna un gruppo antenate di questo con il nome dato. Il gruppo corrente
   * non è contemplato nella ricerca.
   * 
   * @param nomeGruppoAntenato
   *          nome gruppo cercato
   * @return gruppo antenato con il nome richiesto oppure null se non trovato
   */
  public GroupModel getAncestorGroup(String nomeGruppoAntenato) {
    if (nomeGruppoAntenato == null || m_parent == null)
      return m_parent;

    GroupModel gm = m_parent;
    while (gm != null) {
      if (gm.is( (nomeGruppoAntenato)))
        return gm;
      gm = gm.getParent();
    }
    return null;
  }

  /**
   * Ritorna il primo modello antenato di questo che ha come modello figlio
   * quello con il nome dato. Il gruppo corrente non è contemplato nella
   * ricerca.
   * 
   * @param childName
   *          nome che deve avere il modello figlio
   * @return primo modello antenato che ha come figlio <i>childName</i>, oppure
   *         null se non trovato
   */
  public GroupModel getAncestorWithChild(String childName) {
    if (childName == null || getParent() == null)
      return null;

    GroupModel gm = getParent();
    while (gm != null) {
      if (gm.getChildModel(childName) != null)
        return gm;
      gm = gm.getParent();
    }
    return null;
  }

  /**
   * Ritorna il modello radice, progenitore di tutti i GroupModel di questa
   * gerarchia.
   */
  public RootModel getRootModel() {
    if (isRoot())
      return (RootModel) this;
    GroupModel gm = m_parent;
    while (gm != null) {
      if (gm.isRoot())
        return (RootModel) gm;
      gm = gm.getParent();
    }
    return null;
  }

  /**
   * Ritorna il GroupModel strettamente figlio di questo e con il nome dato
   * 
   * @param modelName
   *          nome richiesto
   * @return GroupModel figlio con nome dato oppure null se non esiste
   */
  public GroupModel getChildModel(String modelName) {
    if (modelName == null)
      return null;

    for (GroupModel figlio : m_childModels.values()) {
      if (figlio.is(modelName))
        return figlio;
    }
    return null;
  }

  /**
   * Ritorna il GroupModel discendente di questo e con il nome dato.
   * 
   * @param nomeGruppo
   *          nome richiesto
   * @return GroupModel discendente con nome dato oppure null se non esiste
   */
  public GroupModel getDescendantModel(String nomeGruppo) {
    if (nomeGruppo == null)
      return null;

    GroupModel gm = getChildModel(nomeGruppo);
    if (gm == null) {
      for (GroupModel figlio : m_childModels.values()) {
        gm = figlio.getDescendantModel(nomeGruppo);
        if (gm != null)
          return gm;
      }
    }
    return gm;
  }

  /**
   * @return il m_filterInclude
   */
  public Symbol getFilterInclude() {
    return m_filterInclude;
  }

  /**
   * @param mFilterInclude
   *          the m_filterInclude to set
   */
  public void setFilterInclude(Symbol mFilterInclude) {
    m_filterInclude = mFilterInclude;
  }

  /**
   * @return il m_filterEvaluator
   */
  public Evaluator getFilterEvaluator() {
    return m_filterEvaluator;
  }

  /**
   * @param mFilterEvaluator
   *          the m_filterEvaluator to set
   */
  public void setFilterEvaluator(Evaluator mFilterEvaluator) {
    m_filterEvaluator = mFilterEvaluator;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "GroupModel " + m_name;
  }

  /**
   * @return true se il modello accetta chiavi null
   */
  public boolean isNullKeyAllowed() {
    return m_nullKeyAllowed;
  }

  /**
   * Imposta un flag che indica se nelle future istanze di questo modello i
   * gruppi accettano una chiave null. <br/>
   * Il default è che le chiavi non possono essere null. <br/>
   * Il flag vale non solo per l'intera chiave, ma anche per <i>singoli campi
   * chiave</i>: cioè se una chiave è composta da più di un campo, normalmente
   * tutti devono essere diversi da null per venire accettata; con questo flag a
   * true, invece, anche i singoli campi chiave null sono accettati.
   * 
   * @param nullKeyAllowed
   *          true se chiave null accettata
   */
  public void setNullKeyAllowed(boolean nullKeyAllowed) {
    m_nullKeyAllowed = nullKeyAllowed;
  }

  /**
   * Ritorna l'id univoco di questo modello nella cache mantenuta da
   * {@link RootModel}.
   */
  public Integer getModelId() {
    return m_modelId;
  }

  protected void setModelId(Integer modelId) {
    m_modelId = modelId;
  }

  /**
   * Ritorna il DataFieldModel il cui id è passato. Ogni GroupModel mantiene una
   * cache per id dei suoi modelli di campo.
   * 
   * @param id
   *          id univoco del DataFieldModel di questo modello (vedi
   *          {@link DataFieldModel#getModelId()})
   * 
   * @return DataField richiesto oppure null se non trovato
   */
  public DataFieldModel getFieldModelFromCache(Integer id) {
    return m_fieldsCache.get(id);
  }

  public Stampa getStampa() {
    return m_stampa;
  }

  public void reset() {
    for (GroupModel m : m_childModels.values())  {
      m.reset();
    }
    m_childModels.clear();
    m_fields.clear();
    m_fieldsCache.clear();
    m_modelCacheId = 0;
    m_keys.clear();

    
  }
  
}
