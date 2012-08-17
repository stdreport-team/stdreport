package org.xreports.datagroup;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.engine.DataException;
import org.xreports.engine.ResolveException;
import org.xreports.engine.source.GroupEvaluator;

/**
 * Singola istanza di un {@link GroupModel}. <br/>
 * Un'istanza di {@link Group} ha le seguenti caratteristiche:
 * <ul>
 * <li>ha un {@link GroupModel} di riferimento, da cui eredita nome, campi</li>
 * <li>ha un riferimento al gruppo radice, {@link RootGroup}, padre dell'intera
 * gerarchia dei gruppi</li>
 * <li>appartiene ad una lista ( {@link GroupList}) di gruppi 'fratelli</li>
 * <li>ha una lista di gruppi figli per ogni possibile gruppo figlio definito
 * nel relativo modello</li>
 * </ul>
 * 
 * @author pier
 * 
 */
public class Group implements Serializable {

  /**
   * 
   */
  private static final long      serialVersionUID = -9049101621843462506L;

  /** gruppo progenitore, radice di tutta la gerarchia dei gruppi */
  private RootGroup              m_builder;

  /** il mio modello di gruppo */
  protected transient GroupModel m_model;

  /** identificativo univoco del mio modello di gruppo */
  private Integer                m_modelId;

  /** lista a cui appartengo */
  private GroupList              m_parentList;

  public static transient int    s_groupCount     = 0;

  /**
   * Dati dei gruppi figli di questo gruppo. <br/>
   * <b>key</b> = nome gruppo figlio <br/>
   * <b>value</b> = lista delle istanze dei gruppi, con nome=key, figli di
   * questo.
   */
  private Map<String, GroupList> m_childGroups    = new LinkedHashMap<String, GroupList>();
  /** indice gruppo 0-based (è univoco fra i suoi fratelli) */
  private int                    m_groupIndex     = 0;

  /** true sse a questo gruppo sono stati assegnati dei dati */
  protected boolean              m_hasData        = false;

  protected String               m_tag;

  /** identificativo univoco di questa istanza di gruppo */
  private int                    m_groupID        = -1;

  /**
   * Mappa nome-->valore di tutti i campi 'reali' di questo gruppo. NB: il nome
   * è sempre mantenuto in lower-case
   */
  private Map<String, DataField> m_fields         = new HashMap<String, DataField>();

  protected Group() {

  }

  /**
   * Costruttore standard.
   * 
   * @param root
   *          oggetto a capo della intera gerarchia dei gruppi
   * @param model
   *          modello di riferimento del gruppo
   * @param list
   *          lista di cui fa parte il gruppo
   */
  Group(RootGroup root, GroupModel model, GroupList list) {
    if (model == null) {
      throw new NullPointerException("GroupModel can't be null");
    }
    if (root == null) {
      throw new NullPointerException("La radice di un gruppo non puo' essere null");
    }
    _initID(root);
    setModel(model);
    m_parentList = list;
    m_builder = root;

    //inizializzo le liste dei possibili gruppi figli di questo
    for (GroupModel childModel : model.getChildModels()) {
      addChildList(childModel);
    }
  }

  /**
   * Aggiunge i campi della collection passata all'elenco dei campi chiave. <br>
   * Se uno dei campi esiste già, emette exception.
   * 
   * @param chiavi
   *          Collection dei campi da aggiungere.
   */
  public void addKeyFields(Collection<DataFieldModel> chiavi) {
    for (DataFieldModel c : chiavi) {
      addKeyField(c);
    }
  }

  /**
   * Aggiunge il campo passato all'elenco dei campi chiave di questo gruppo. <br>
   * Il campo passato viene clonato e viene aggiunta la sua copia. <br>
   * NB: la clonazione copia tutto il campo eccetto il valore.
   * 
   * @param c
   *          campo da aggiungere
   * @return campo aggiunto: è sempre un'istanza diversa dal campo passato
   */
  private DataField addKeyField(DataFieldModel c) {
    return _addField(c.newFieldInstance(), true);
  }

  private DataField addField(DataFieldModel c) {
    return _addField(c.newFieldInstance(), false);
  }

  public String getName() {
    return getModel().getName();
  }

  /**
   * Aggiunge il campo passato alla chiave o ai campi di output, controllando
   * che non esista già, sia fra i campi chiavi che i campi di output che i
   * campi automatici.
   * 
   * @param c
   *          campo da aggiungere
   * @param isKey
   *          true se è chiave, false se di output
   * @return campo aggiunto o exception se c'è già
   */
  private DataField _addField(DataField c, boolean isKey) {
    if (getField(c.getNome()) != null) {
      throw new IllegalArgumentException("Il campo '" + c.getNome() + "' esiste già nel gruppo " + getName()
          + ": non puoi aggiungerlo.");
    }
    c.setGroup(this);
    m_fields.put(c.getNome().toLowerCase(), c);
    return c;
  }

  /**
   * Restituisce una lista di oggetti DataField che hanno nome='nomeCampo' e
   * appartengono ad un gruppo figlio o discendente di questo avente nome
   * 'nomeGruppo'. L'unica differenza con il metodo
   * {@link #getChildFields(String, String)} è che questo non si ferma al primo
   * livello (i figli) ma naviga in tutti i gruppi discendenti.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio
   * @param nomeCampo
   *          nome del campo del gruppo figlio
   * @return elenco campi, oppure null se nomeGruppo o nomeCampo sono null
   * @throws GroupException
   */
  public List<DataField> getDescendantFields(String nomeGruppo, String nomeCampo) throws GroupException {
    ArrayList<DataField> list = null;
    if (nomeGruppo == null || nomeCampo == null)
      return null;
    list = new ArrayList<DataField>();
    List<DataField> tmp;
    try {
      tmp = getChildFields(nomeGruppo, nomeCampo);
      list.addAll(tmp);
    } catch (Exception e) {
      //ignoro l'assenza di figli
    }
    for (GroupList gl : m_childGroups.values()) {
      // TODO ottimizzare, si può sapere dal GroupModel se questo GroupList ha nomeGruppo come discendente
      for (Group g : gl.getInstances()) {
        tmp = g.getDescendantFields(nomeGruppo, nomeCampo);
        if (tmp != null)
          list.addAll(tmp);
      }
    }
    return list;
  }

  /**
   * Restituisce una lista di oggetti DataField che hanno nome='nomeCampo' e
   * appartengono ad un gruppo figlio o discendente di questo avente nome
   * 'nomeGruppo' e che soddisfa alla condizione data.
   * 
   * @param nomeGruppo
   *          nome del gruppo discendente
   * @param nomeCampo
   *          nome del campo da mettere nella lista ritornata
   * @param condition
   *          condizione che deve soddisfare il gruppo; se null, nessuna
   *          condizione viene valutata
   * @param evaluator
   *          valutatore della condizione
   * @return lista di campi il cui gruppo soddisfa le condizioni date
   * @throws GroupException
   *           in caso di errori gravi in ricerca/navigazione dei gruppi
   * @throws EvaluateException
   *           in caso di errore nella valutazione della condizione o se manca
   *           il valutatore
   * @throws ResolveException 
   */
  public List<DataField> getDescendantFields(String nomeGruppo, String nomeCampo, Symbol condition, GroupEvaluator evaluator)
      throws GroupException, EvaluateException, ResolveException {
    ArrayList<DataField> list = null;
    if (nomeGruppo == null || nomeCampo == null)
      return null;
    list = new ArrayList<DataField>();
    //prima guardo i figli diretti
    if (getModel().getChildModel(nomeGruppo) != null) {
      List<Group> children = getChildInstances(nomeGruppo, condition, evaluator);
      for (Group g : children) {
        list.add(g.getField(nomeCampo));
      }
    }
    //poi chiamo ricorsivamente getDescendantFields sui figli di questo gruppo
    for (GroupList gl : m_childGroups.values()) {
      if (getModel().getDescendantModel(nomeGruppo) != null) {
        //NB: per evitare ricorsioni inutili, controllo prima che possa esistere un gruppo 'nomeGruppo' figlio del corrente
        for (Group g : gl.getInstances()) {
          List<DataField> tmp = g.getDescendantFields(nomeGruppo, nomeCampo, condition, evaluator);
          if (tmp != null)
            list.addAll(tmp);
        }
      }
    }
    return list;
  }

  /**
   * Restituisce una lista di DataField che hanno nome='nomeCampo' e
   * appartengono ad un gruppo figlio di questo avente nome='nomeGruppo'.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio
   * @param nomeCampo
   *          nome del campo del gruppo figlio
   * @return elenco campi, oppure null se nomeGruppo o nomeCampo sono null o se
   *         nomeCampo non esiste nel gruppo richiesto
   * @throws GroupException
   *           nel caso il gruppo "nomeGruppo" non sia figlio di questo
   * @throws ResolveException 
   */
  public List<DataField> getChildFields(String nomeGruppo, String nomeCampo) throws GroupException, ResolveException {
    try {
      return getChildFields(nomeGruppo, nomeCampo, null, null);
    } catch (EvaluateException e) {
      //qui non si arriva mai!!!
      return null;
    }
  }

  /**
   * Restituisce una lista di DataField che hanno nome='nomeCampo' e
   * appartengono ad un gruppo figlio di questo avente nome='nomeGruppo'.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio
   * @param nomeCampo
   *          nome del campo del gruppo figlio
   * @return elenco campi, oppure null se nomeGruppo o nomeCampo sono null o se
   *         nomeCampo non esiste nel gruppo richiesto
   * @throws GroupException
   *           nel caso il gruppo "nomeGruppo" non sia figlio di questo
   * @throws ResolveException 
   */
  public List<DataField> getChildFields(String nomeGruppo, String nomeCampo, Symbol condition, GroupEvaluator evaluator)
      throws GroupException, EvaluateException, ResolveException {
    if (nomeGruppo == null || nomeCampo == null)
      return null;
    Collection<Group> groups = getChildInstances(nomeGruppo, condition, evaluator);
    List<DataField> list = new ArrayList<DataField>();
    for (Group g : groups) {
      DataField c = g.getField(nomeCampo);
      if (c == null) {
        return null; // la mancanza di campo non genera un'exception
      }
      list.add(c);
    }
    return list;
  }

  /**
   * Restituisce una lista di tutte le istanze del gruppo di nome
   * <i>nomeGruppo</i>, figlio di questo gruppo. NOTA BENE: l'ordine di
   * navigazione della lista è l'ordine con cui i gruppi sono stati creati che,
   * a sua volta, deriva dai dati attuali passati al gruppo. Tale ordine non è
   * necessariamente per chiave. Se si vuole l'elenco dei figli ordinati per
   * chiave, usare {@link #getFigliOrdinatiPerChiave(String)}.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio.
   * @return lista di tutte le istanze del gruppo figlio <i>nomeGruppo</i>; se
   *         non ci sono istanze, torna una lista vuota
   * @throws GroupException
   *           nel caso il gruppo "nomeGruppo" non sia figlio di questo
   */
  public List<Group> getChildInstances(String nomeGruppo) throws GroupException {
    GroupList gl = getChildList(nomeGruppo);
    return gl.getInstances();
  }

  /**
   * Restituisce una lista di tutte le istanze del gruppo di nome
   * <i>nomeGruppo</i>, figlio di questo gruppo, che soddisfano la condizione
   * data.
   * 
   * @param nomeGruppo
   *          nome gruppo richiesto, deve essere figlio di questo
   * @param condition
   *          condizione che devono rispettare i gruppi; se null, la chiamata
   *          equivale a {@link #getChildInstances(String)}
   * @param evaluator
   *          valutatore della condizione
   * @return lista dei gruppi figli di questo che soddisfano la condizione data
   * @throws GroupException
   *           nel caso il gruppo "nomeGruppo" non sia figlio di questo
   * @throws EvaluateException
   *           nel caso di errore in valutazione della condizione o mancanza di
   *           valutatore
   * @throws ResolveException 
   */
  public List<Group> getChildInstances(String nomeGruppo, Symbol condition, GroupEvaluator evaluator) throws GroupException,
      EvaluateException, ResolveException {
    if (condition == null)
      return getChildInstances(nomeGruppo);
    if (evaluator == null)
      throw new EvaluateException("Manca il valutatore della espressione " + condition.getFullText());

    GroupList gl = getChildList(nomeGruppo);
    List<Group> list = new ArrayList<Group>();
    for (Group g : gl.getInstances()) {
      Object retEval = evaluator.evaluate(condition, g);
      if (retEval instanceof Boolean) {
        if ( ((Boolean) retEval).booleanValue()) {
          list.add(g);
        }
      }
    }
    return list;
  }

  /**
   * Ritorna l'elenco di tutte le istanze del gruppo <em>nomeGruppo</em>,
   * discendente di questo.
   * 
   * @param nomeGruppo
   *          nome del gruppo discendente. Non può essere null.
   * @return lista dei gruppi discendenti di questo col nome richiesto: se non
   *         esistono gruppi viene ritornata una lista vuota
   */
  public List<Group> getDescendantInstances(String nomeGruppo) {
    List<Group> ret = new ArrayList<Group>();
    for (GroupList gl : m_childGroups.values()) {
      List<Group> groups = gl.getInstances();
      if (gl.is(nomeGruppo))
        ret.addAll(groups);
      for (Group g : groups)
        ret.addAll(g.getDescendantInstances(nomeGruppo));
    }
    return ret;
  }

  /**
   * Ritorna un'istanza del gruppo <em>nomeGruppo</em>, discendente di questo
   * con l'indice richiesto.
   * 
   * @param nomeGruppo
   *          nome del gruppo discendente. Non può essere null.
   * @param index
   *          indice 0-based dell'istanza desiderata
   * @return istanza del gruppi richiesto oppure null se non esiste
   */
  public Group getDescendantInstance(String nomeGruppo, int index) {
    List<Group> ret = getDescendantInstances(nomeGruppo);
    if (index < 0 || index >= ret.size())
      return null;
    return ret.get(index);
  }

  /**
   * Ritorna true se questo gruppo ha il nome passato.
   * 
   * @param name
   *          nome gruppo (case insensitive)
   */
  public boolean is(String name) {
    return getName().equalsIgnoreCase(name);
  }

  /**
   * Torna l'index.esima istanza del gruppo <em>nomeGruppo</em> figlio di questo
   * gruppo.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio
   * @param index
   *          indice istanza 0-based: 0=primo gruppo, 1=secondo gruppo, ...
   * @return istanza trovata oppure null se index è maggiore o uguale alla
   *         quantità dei figli
   * @throws GroupException
   *           nel caso nomeGruppo non sia un gruppo figlio di questo
   */
  public Group getChildInstance(String nomeGruppo, int index) throws GroupException {
    GroupList gl = getChildList(nomeGruppo);
    try {
      return gl.getInstance(index);
    } catch (Exception e) {
      //se arrivo qui, index è > della qta di figli, ritorno null senza dare exception
      return null;
    }
  }

  public boolean isAllKey() {
    return getModel().isAllKey();
  }

  public Set<String> getKeyFieldNames() {
    return getModel().getKeyFieldNames();
  }

  /**
   * Restituisce un Set che permette la navigazione ordinata dei figli, cioè se
   * eseguo<br/>
   * <tt>
   *  for(Gruppo g : gruppo.getFigliOrdinatiPerChiave("nome_gruppo", false, true))
   * </tt> <br/>
   * sono sicuro che i gruppi di nome "nome_gruppo", figli di questo gruppo,
   * sono navigati in ordine di chiave.
   * 
   * @param nomeGruppo
   *          nome del gruppo figlio di questo
   * @param caseSensitive
   *          true se il confronto fra stringhe è case-sensitive
   * @param ascending
   *          true se ordinamento crescente, false se decrescente
   * 
   * @return Set che assicura la navigazione dei figli secondo l'ordine della
   *         chiave
   * @throws CISException
   * @throws GroupException
   */
  public List<Group> getFigliOrdinatiPerChiave(String nomeGruppo, boolean caseSensitive, boolean ascending) throws GroupException {
    if (isAllKey()) {
      throw new IllegalStateException("Non si possono avere i figli ordinati per chiave, il flag 'allKey' è true");
    }

    List<Group> figli = getChildInstances(nomeGruppo);
    if (figli.size() > 0) {
      //prendo la definizione dei campi chiave dal primo figlio
      Group first = figli.get(0);
      Set<String> chiave = first.getKeyFieldNames();
      GroupComparator gruComp = new GroupComparator(chiave, caseSensitive, ascending);
      Collections.sort(figli, gruComp);
    }
    return figli;
  }

  /**
   * Restituisce una lista con tutte le istanze del gruppo figlio
   * <em>nomeGruppo</em> ordinate per i campi passati.
   * 
   * @param nomeGruppo
   *          nome del gruppo di cui si vogliono le istanze ordinate
   * @param nomiCampo
   *          elenco ordinato dei nomi di campo
   * @param caseSensitive
   *          se true (il default) il confronto fra i campi stringa è
   *          case-sensitive.
   * @param ascending
   *          true se ordinamento crescente, false se decrescente
   * 
   * @return lista con i figli ordinati
   * @throws CISException
   *           nel caso il gruppo <em>nomeGruppo</em> non esista
   * @throws GroupException
   */
  public List<Group> getOrderedChildren(String nomeGruppo, Set<String> nomiCampo, boolean caseSensitive, boolean ascending)
      throws GroupException {
    List<Group> figli = getChildInstances(nomeGruppo);
    GroupComparator gruComp = new GroupComparator(nomiCampo, caseSensitive, ascending);
    Collections.sort(figli, gruComp);
    return figli;
  }

  /**
   * Cerca quali gruppi figli di questo soddisfano le condizioni passate nella
   * mappa.
   * 
   * @param valori
   *          mappa condizioni; la mappa è nella forma <i>nome campo</i> -
   *          <i>valore</i> e l'operatore è sempre l'uguaglianza.
   * 
   * @return lista di gruppi figli che soddisfano le condizioni passate; se
   *         nessun gruppo viene trovato, viene tornata un alista di lunghezza
   *         zero.
   * @throws CISException
   *           nel caso il gruppo non nesista
   * @throws GroupException
   */
  public List<Group> findChildren(String nomeGruppo, Map<String, Object> valori) throws GroupException {
    List<Group> list = getChildInstances(nomeGruppo);
    List<Group> listOk = new ArrayList<Group>();
    for (Group g : list) {
      if (g.isEqualCampo(valori))
        listOk.add(g);
    }
    list.clear();
    return listOk;
  }

  /**
   * Indica se questo gruppo soddisfa le condizioni passate nella mappa.
   * 
   * @param valori
   *          mappa condizioni; la mappa è nella forma <i>nome campo</i> -
   *          <i>valore</i> e l'operatore è sempre l'uguaglianza.
   * 
   * @return true se questo gruppo soddisfa le condizioni passate, false
   *         altrimenti
   */
  public boolean isEqualCampo(Map<String, Object> valori) {
    for (String nome : valori.keySet()) {
      Object val = valori.get(nome);
      DataField c = getField(nome);
      if (c == null)
        return false;
      if ( !c.isNome(nome) || !c.isEqualValue(val))
        return false;
    }
    return true;
  }

  /**
   * Ritorna il DataField con il nome passato
   * 
   * @param nome
   *          nome del campo richiesto: e' case-insensitive. Attenzione: non
   *          passare null!
   * @return DataField richiesto oppure null se non esiste
   */
  public DataField getField(String nome) {
    return m_fields.get(nome.toLowerCase());
  }

  /**
   * @deprecated usare {@link #getField(String)}
   */
  public DataField getCampo(String nome) {
    return getField(nome);
  }
  
  /**
   * Reperisce la collection di tutti i DataField presenti in questo gruppo
   * 
   * @return collection dei DataField di questo gruppo: non è mai null, se non
   *         ci sono DataField ritorna una collection vuota
   */
  public Collection<DataField> getMyFields() {
    return m_fields.values();
  }

  /**
   * Reperisce la lista di tutti i DataField definiti <b>chiave</b> presenti in
   * questo gruppo
   * 
   * @return collection dei DataField chiave di questo gruppo: non è mai null,
   *         se non ci sono DataField ritorna una lista vuota
   */
  public List<DataField> getMyKeyFields() {
    List<DataField> chiavi = new LinkedList<DataField>();
    for (String nome : getKeyFieldNames()) {
      chiavi.add(getField(nome));
    }
    return chiavi;
  }

  /**
   * Controllo di esistenza campo
   * 
   * @param nomeCampo
   *          nome del campo da controllare: e' case-insensitive. Attenzione:
   *          non passare null!
   * @return true sse il campo esiste
   */
  public boolean existField(String nomeCampo) {
    return getField(nomeCampo) != null;
  }

  /**
   * Ritorna il primo gruppo antenato che ha il nome passato. <br>
   * Se nomeGruppoAntenato è null, torna il padre. Se non trova nessun gruppo
   * antenato con il nome dato, ritorna null.
   * 
   * @param nomeGruppoAntenato
   * @return
   */
  public Group getAncestorGroup(String nomeGruppoAntenato) {
    if (m_parentList == null)
      return null;
    Group p = m_parentList.getParentGroup();
    if (nomeGruppoAntenato == null)
      return p;

    while (p != null) {
      if (p.getName().equalsIgnoreCase(nomeGruppoAntenato))
        return p;
      if (p.getParentList() != null)
        p = p.getParentList().getParentGroup();
      else
        return null;
    }
    return null;
  }

  /**
   * Calcola il valore di un campo di tipo PERC_PADRE. Il valore è semplicemente
   * il rapporto tra questo campo e il campo di riferimento
   * 
   * @param nomeGruppoPadre
   *          nome del gruppo del campo di riferimento
   * @param nomeCampoPadre
   *          nome del campo di riferimento
   * @param nomeCampo
   *          nome del campo da calcolare, che deve appartenere a questo gruppo
   * @return
   */
  public Number percPadre(String nomeGruppoPadre, String nomeCampoPadre, String nomeCampo) {
    Group padre = getAncestorGroup(nomeGruppoPadre);
    if (padre == null)
      throw new IllegalStateException("Questo gruppo non ha un padre!");
    DataField c = padre.getField(nomeCampoPadre);
    if (c == null)
      throw new IllegalStateException("Il gruppo padre non ha un campo di nome " + nomeCampoPadre);
    DataField questo = getField(nomeCampo);
    if (questo == null)
      throw new IllegalStateException("Questo gruppo non ha un campo di nome " + nomeCampo);
    // se dipendo da un campo auto, prima lo calcolo
    if (c.isAuto())
      ((DataFieldAuto) c).calcola();
    if (questo.isAuto())
      ((DataFieldAuto) questo).calcola();
    if (isDebugMode())
      System.out.println("Calcolo percPadre: " + questo.getAsDouble() + "/" + c.getAsDouble());
    return Double.valueOf(questo.getAsDouble() / c.getAsDouble());
  }

  /**
   * Calcola il valore di un campo di tipo DIFF_PERC_PADRE. Il valore è lo
   * scostamento percentuale tra questo campo e il campo di riferimento
   * 
   * @param nomeGruppoPadre
   *          nome del gruppo del campo di riferimento
   * @param nomeCampoPadre
   *          nome del campo di riferimento
   * @param nomeCampo
   *          nome del campo da calcolare, che deve appartenere a questo gruppo
   * @return
   */
  public Number diffPercPadre(String nomeGruppoPadre, String nomeCampoPadre, String nomeCampo) {
    Group padre = getAncestorGroup(nomeGruppoPadre);
    if (padre == null)
      throw new IllegalStateException("Questo gruppo non ha un padre!");
    DataField c = padre.getField(nomeCampoPadre);
    if (c == null)
      throw new IllegalStateException("Il gruppo padre non ha un campo di nome " + nomeCampoPadre);
    DataField questo = getField(nomeCampo);
    if (questo == null)
      throw new IllegalStateException("Questo gruppo non ha un campo di nome " + nomeCampo);
    // se dipendo da un campo auto, prima lo calcolo
    if (c.isAuto())
      ((DataFieldAuto) c).calcola();
    if (questo.isAuto())
      ((DataFieldAuto) questo).calcola();
    if (isDebugMode())
      System.out.println("Calcolo diffPercPadre: " + questo.getAsDouble() + "/" + c.getAsDouble());
    return Double.valueOf( (questo.getAsDouble() - c.getAsDouble()) / questo.getAsDouble());
  }

  /**
   * Restituisce la somma del campo 'nomeCampo' appartenente al gruppo
   * 'nomeGruppo' che è figlio di questo gruppo.
   * 
   * @param nomeGruppo
   *          nome gruppo figlio di questo che contiene il campo nomeCampo
   * @param nomeCampo
   *          nome campo da sommare
   * @return somma; ritorna null se il campo non esiste; emette eccezione se non
   *         è numerico
   * @throws GroupException
   * @throws ResolveException 
   */
  public Number sommaFigli(String nomeGruppo, String nomeCampo) throws GroupException, ResolveException {
    List<DataField> list = getChildFields(nomeGruppo, nomeCampo);
    return calcFieldsSum(list);
  }

  public Number sommaFigli(String nomeGruppo, String nomeCampo, Symbol condition, GroupEvaluator evaluator) throws GroupException,
      EvaluateException, ResolveException {
    List<DataField> list = getChildFields(nomeGruppo, nomeCampo, condition, evaluator);
    return calcFieldsSum(list);
  }

  /**
   * Restituisce la somma del campo 'nomeCampo' per tutte le occorrenze del
   * gruppo discendente 'nomeGruppo'.
   * 
   * @param nomeGruppo
   *          nome gruppo figlio
   * @param nomeCampo
   *          nome campo da sommare
   * @return somma; ritorna null se il campo non esiste; emette eccezione se non
   *         è numerico
   * @throws GroupException
   *           in caso di errori gravi in ricerca/navigazione dei gruppi
   */
  public Number sommaDiscen(String nomeGruppo, String nomeCampo) throws GroupException {
    List<DataField> list = getDescendantFields(nomeGruppo, nomeCampo);
    return calcFieldsSum(list);
  }

  /**
   * Calcola la quantita di gruppi discendenti di questo, che hanno il nome
   * richiesto e soddisfano la condizione richiesta.
   * 
   * @param nomeGruppo
   *          nome del gruppo richiesto
   * @param nomeCampo
   *          nome campo da sommare
   * @param condition
   *          condizione che i gruppi devono soddisfare
   * @param evaluator
   *          oggetto che effettua la valutazione della condizione
   * @return somma del campo dato per i soli gruppi discendenti di questo che
   *         soddisfano i criteri richiesti
   * 
   * @throws EvaluateException
   *           se il valutatore incontra un errore grave nella valutazione della
   *           condizione
   * @throws GroupException
   *           in caso di errori gravi in ricerca/navigazione dei gruppi
   * @throws ResolveException 
   */
  public Number sommaDiscen(String nomeGruppo, String nomeCampo, Symbol condition, GroupEvaluator evaluator)
      throws EvaluateException, GroupException, ResolveException {
    List<DataField> list = getDescendantFields(nomeGruppo, nomeCampo, condition, evaluator);
    return calcFieldsSum(list);
  }

  /**
   * Restituisce la somma numerica del valore di tutti i campi presenti nella
   * lista. <br>
   * Dè Exception se anche uno solo dei campi non è numerico. <br>
   * Ritorna 0 se non ci sono campi nella lista ma la lista non è null. Se
   * invece la lista è null, ritorna null.
   * <p>
   * NB: i campi numerici empty o null vengono ignorati.
   * 
   * 
   * @param list
   *          lista dei campi
   * @return somma dei valori
   */
  public Number calcFieldsSum(List<DataField> list) {
    if (list == null) {
      return null;
    }
    Double totale = new Double(0.0);
    for (DataField c : list) {
      //devo forzare il calcolo prima di testare empty/null e anche prima di testare il tipo
      c.calcola();
      if (c.isEmptyOrNull()) {
        continue;
      }
      if ( !c.isNumeric()) {
        //il null lo considero 0, come le SUM in SQL (verificato su SQL Server 2005 con 3 testimoni)
        //        if (c.isEmptyOrNull()) {
        //          throw new IllegalArgumentException("Il campo " + c.getNomeEsteso() + " è null/empty.");
        //        }
        throw new IllegalArgumentException("Il campo " + c.getNomeEsteso() + " non è numerico.");
      }

      if (c.isEmptyNullOrZero()) {
        continue;
      }
      if (c.getTipo().equals(DataFieldModel.TipoCampo.LONG) || c.getTipo().equals(DataFieldModel.TipoCampo.INTEGER)) {
        if (totale == null) {
          totale = Double.valueOf(c.getAsLong());
        } else {
          totale = Double.valueOf(totale.longValue() + c.getAsLong());
        }
      } else if (c.getTipo().equals(DataFieldModel.TipoCampo.DOUBLE) || c.getTipo().equals(DataFieldModel.TipoCampo.BIGDECIMAL)) {
        if (totale == null) {
          totale = Double.valueOf(c.getAsDouble());
        } else {
          totale = Double.valueOf(totale.doubleValue() + c.getAsDouble());
        }
      }
    }
    return totale;
  }

  /**
   * Restituisce la media del campo 'nomeCampo' su tutte le occorrenze del
   * gruppo figlio 'nomeGruppo'.
   * 
   * @param nomeGruppo
   *          nome gruppo figlio
   * @param nomeCampo
   *          nome campo di cui calcolare la media
   * @return somma; ritorna null se il campo non esiste; emette eccezione se non
   *         è numerico
   * @throws GroupException
   * @throws ResolveException 
   */
  public Double mediaFigli(String nomeGruppo, String nomeCampo) throws GroupException, ResolveException {
    Double totale = new Double(0);
    int count = 0;
    List<DataField> list = getChildFields(nomeGruppo, nomeCampo);
    for (DataField c : list) {
      if ( !c.isNumeric()) {
        throw new IllegalArgumentException("Il campo " + c.getNomeEsteso() + " non è numerico.");
      }

      // se dipendo da altri campi auto, prima i calcolo
      if (c.isAuto()) {
        c.calcola();
      }
      totale = Double.valueOf(totale.doubleValue() + c.getAsDouble());

      count++;
    }
    if (count == 0) {
      throw new GroupException("Nessun elemento trovato su cui fare la media");
    }
    return Double.valueOf(totale.doubleValue() / count);
  }

  /**
   * Aggiunge i campi della collection passata all'elenco dei campi di output.
   * Se uno dei campi della collection esiste già fra i campi di output, lo
   * ignora.
   * 
   * @param chiavi
   *          Collection dei campi da aggiungere.
   */
  public void addFields(Collection<DataFieldModel> campi) {
    for (DataFieldModel c : campi) {
      try {
        addField(c);
      } catch (RuntimeException e) {
      }
    }
  }

  //  /**
  //   * Aggiunge i campi i cui nomi sono nell'array passato, all'elenco dei campi di output. 
  //   * I campi vengono creati senza tipo e senza impostazioni di formattazione.
  //   * 
  //   * Se uno dei campi esiste già, emette exception.
  //   * 
  //   * @param out
  //   *          elenco dei nomi dei campi da aggkiungere.
  //   */
  //  public void addCampiOutput(String[] out) {
  //    for (int i = 0; i < out.length; i++) {
  //      addField(out[i]);
  //    }
  //  }

  public RootGroup getRootGroup() {
    return m_builder;
  }

  /**
   * Imposta il valore passato al campo di questo gruppo che ha il nome passato.
   * Se il campo non esiste, non emette alcun errore e non fa nulla
   * 
   * @param nomeCampo
   *          nome del campo richiesto (<b>non</b> è case-sensitive)
   * @param value
   *          valore da assegnare
   */
  public void setValue(String nomeCampo, Object value) {
    DataField c = getField(nomeCampo);
    if (c != null)
      c.setValue(value);
  }

  /**
   * Indice (0-based) di questo gruppo all'interno del set di fratelli a cui
   * appartiene. In pratica è il numero di occorrenza del gruppo: 0=prima
   * istanza, 1=seconda istanza,... <br/>
   * 
   * @return posizione gruppo all'interno dei fratelli
   */
  public int getIndex() {
    return m_groupIndex;
  }

  /**
   * Imposta indice (0-based) di questo gruppo all'interno del set di fratelli a
   * cui appartiene.
   * 
   * @param n
   *          indice 0-based
   */
  public void setIndex(int n) {
    m_groupIndex = n;
  }

  /**
   * Dà una rappresentazione testuale facilmente leggibile dei campi chiave di
   * questo gruppo.
   * 
   * @return rappresentazione della chiave
   */
  public String keyToString() {
    StringBuilder s = new StringBuilder();
    for (String nome : getKeyFieldNames()) {
      if (s.length() > 0)
        s.append(',');
      s.append(getField(nome).getAsStringSafe());
    }
    return s.toString();
  }

  /**
   * Restituisce l'istanza di gruppo successiva all'istanza corrente, nella
   * lista che contiene tutte le istanze di un gruppo con lo stesso nome. <br>
   * Le istanze di ogni gruppo sono mantenute con una double linked list, in cui
   * il capo è puntato dal gruppo padre e le istanze successive sono puntate da
   * quella precedente.
   * 
   * @return istanza successiva di questo gruppo, oppure null se questo gruppo è
   *         l'ultima istanza della catena
   */
  public Group getNext() {
    try {
      return m_parentList.getInstance(this.getIndex() + 1);
    } catch (Exception e) {
      //se non c'è il next torno null
      return null;
    }
  }

  /**
   * Restituisce l'istanza di gruppo precedente all'istanza corrente, nella
   * lista che contiene tutte le istanze di un gruppo con lo stesso nome. <br>
   * Le istanze di ogni gruppo sono mantenute con una double linked list, in cui
   * il capo è puntato dal gruppo padre e le istanze successive sono puntate da
   * quella precedente.
   * 
   * @return istanza precedente di questo gruppo, oppure null se questo gruppo è
   *         la prima istanza della catena
   */
  public Group getPrevious() {
    try {
      return m_parentList.getInstance(this.getIndex() - 1);
    } catch (Exception e) {
      //se non c'è il next torno null
      return null;
    }
  }

  /**
   * Ritorna true se questa è la prima istanza di questo gruppo
   * 
   * @return true se questo è il primo gruppo
   */
  public boolean isFirst() {
    return m_parentList.getFirst() == this;
  }

  /**
   * Ritorna true se questa è l'ultima istanza di questo gruppo
   * 
   * @return true se questo è l'ultimo gruppo
   */
  public boolean isLast() {
    return m_parentList.getLast() == this;
  }

  /**
   * Ritorna il fratello di questo gruppo con l'indice dato
   * 
   * @param index
   *          indice (0-based) dell'istanza di gruppo richiesta
   * @return fratello di questo gruppo con l'indice dato
   */
  public Group getSiblingByIndex(int index) {
    int count = index - getIndex();
    if (count > 0) {
      //il gruppo è successivo, navigo in avanti
      Group nav = this;
      while (count > 0 && nav != null) {
        nav = nav.getNext();
        count--;
      }
      return nav;
    } else if (count < 0) {
      //il gruppo è precedente, navigo indietro
      Group nav = this;
      while (count < 0) {
        nav = nav.getPrevious();
        count++;
      }
      return nav;
    } else {
      //il gruppo è proprio questo!!
      return this;
    }
  }

  /**
   * Distrugge questo oggetto eliminando tutti i riferimenti a oggetti esterni e
   * azzerando le strutture dati interne.
   */
  public void destroy() {
    //distruggo i miei sottogruppi e tutte le loro istanze
    for (GroupList gru : m_childGroups.values()) {
      gru.destroy();
    }
    m_childGroups.clear();
    m_childGroups = null;

    //azzero i riferimenti a oggetti esterni
    m_builder = null;
    m_model = null;
    m_builder = null;
  }

  /**
   * Ritorna true sse il valore corrente di tutti i campi chiave di questo
   * gruppo sono null. Ritorna true anche se il gruppo non ha campi chiave.
   * 
   * @return boolean vedi sopra
   */
  private boolean hasEmptyKey() {
    if (isAllKey()) {
      //se ho il flag 'allKey', ho la chiave empty *SOLO SE* non ho alcun dato
      return !hasData();
    }
    for (String nome : getKeyFieldNames()) {
      DataField c = getField(nome);
      if ( !c.isEmpty())
        return false;
    }
    return true;
  }

  public int keyCount() {
    return getModel().keyCount();
  }

  public int fieldsCount() {
    return getModel().fieldsCount();
  }

  public boolean isKey(String nomeCampo) {
    return getModel().isKey(nomeCampo);
  }

  /**
   * Ritorna true se la mappa dei valori passati contiene *almeno* tutti i campi
   * chiave definiti in questo gruppo e se sono tutti diversi da null. <br/>
   * Cioè se questo gruppo ha definito i campi chiave A,B e C, se la mappa
   * contiene almeno i campi A,B e C e sono tutti diversi da null, il metodo
   * torna true.
   * 
   * @param values
   *          valori da confrontare
   * @return vedi sopra
   */
  boolean keyApplies(Map<String, Object> values) {
    int numChiavi = keyCount();
    int numChiaviOK = 0;
    for (Map.Entry<String, Object> e : values.entrySet()) {
      if (isKey(e.getKey())) {
        if (e.getValue() != null)
          numChiaviOK++;
      }
    }
    // nei valori passati manca qualche campo chiave o è null...
    return (numChiaviOK == numChiavi);
  }

  /**
   * Ritorna true sse tutti i campi chiave contengono lo stesso valore dei dati
   * passati. Ritorna false se la HashMap passata non contiene tutti i campi
   * chiave.
   * 
   * @param values
   *          valori da confrontare
   * @return vedi sopra
   */
  boolean isSameKey(Map<String, Object> values) {
    if (hasEmptyKey())
      return false;
    int numChiavi = keyCount();
    int numChiaviOK = 0;
    for (Map.Entry<String, Object> e : values.entrySet()) {
      String nome = e.getKey().toLowerCase();
      if (isKey(nome)) {
        DataField c = getField(nome);
        if ( !c.isEqualValue(e.getValue())) {
          return false;
        }
        numChiaviOK++;
      }
    }
    if (numChiaviOK != numChiavi) {
      return false; // nei valori passati manca qualche campo chiave...
    }
    return true;
  }

  /**
   * Copia i valori passati nei DataField definiti in questo gruppo. Dopo la
   * chiamata a questo metodo, questa istanza di gruppo ha il metodo
   * {@link #hasData()} che ritornerà sicuramente true.
   * 
   * @param values
   *          mappa <b>nome->valore</b> dei campi da assegnare a questo gruppo.
   */
  protected void copyValues(Map<String, Object> values) {
    m_hasData = true;
    if (fieldsCount() == 0)
      return;
//    for (DataField f : getMyFields()) {
//      if ( !f.isAuto()) {
//        if (values.containsKey(f.getNome().toLowerCase())) {
//          Object value = values.get(f.getNome().toLowerCase());
//          f.setValue(value);          
//        }
//        if (values.containsKey(f.getNome())) {
//          Object value = values.get(f.getNome());
//          f.setValue(value);          
//        }
//      }
//    }
    for (String valueName : values.keySet()) {
      DataField f = m_fields.get(valueName.toLowerCase());
      if (f != null && !f.isAuto()) {
        f.setValue(values.get(valueName));   
      }
    }
  }

  /**
   * Aggiunge i valori passati ai campi *NON* chiave e *NON* auto che hanno il
   * flag 'addValue'=true e che sono numerici. Se un campo è empty, il valore
   * viene semplicemente assegnato, anche se il campo non è numerico.
   * 
   * @param values
   *          Map dei valori da assegnare ai campi del gruppo
   */
  protected void addValues(Map<String, Object> values) {
    m_hasData = true;
    if (fieldsCount() == 0)
      return;
    for (DataField f : getMyFields()) {
      if ( !f.isAuto() && !isKey(f.getNome())) {
        Object value = values.get(f.getNome());
        if (f.isEmpty()) {
          f.setValue(value);
        } else if (f.isNumeric() && f.isAddValue()) {
          f.addValue(value);
        }
      }
    }
  }

  //  private void calcFields() {
  //    //non calcolo i campi se la chiave c'è ma è vuota
  //    if (hasEmptyKey() && keyCount() > 0)
  //      return;
  //    for (DataField c : getMyFields()) {
  //      try {
  //        c.calcola();
  //      } catch (RuntimeException e1) {
  //        e1.printStackTrace();
  //        throw new IllegalArgumentException("Errore nel calcolo del campo " + c.getNomeEsteso() + ": " + e1.getMessage());
  //      }
  //    }
  //  }

  /**
   * Ritorna il path assoluto di questa istanza di questo gruppo. Il formato è
   * così:
   * 
   * <pre>
   *    gruppoA[idA].gruppoB[idB].gruppoC[idC]....gruppo<i>N</i>[id<i>N</i>]
   * </pre>
   * 
   * dove gruppoA, gruppoB,... sono i nomi dei gruppi della gerarchia che arriva
   * fino a questo gruppo e fra le parentesi [] c'è l'indice del gruppo fra i
   * suoi fratelli
   */
  public String getPath() {
    String path = "";
    if (getParentList() != null)
      path = getParentList().getPath() + ".";
    path += getName() + "[" + getIndex() + "]";
    return path;
  }

  /**
   * Ritorna il path assoluto di questa istanza di questo gruppo. Il formato è
   * così:
   * 
   * <pre>
   *    gruppoA[keyA].gruppoB[keyB].gruppoC[keyC]....gruppo<i>N</i>[key<i>N</i>]
   * </pre>
   * 
   * dove gruppoA, gruppoB,... sono i nomi dei gruppi della gerarchia che arriva
   * fino a questo gruppo e fra le parentesi [] c'è il valore di tutti i campi
   * chiave del gruppo
   */
  public String getKeysPath() {
    String path = "";
    if (getParentList() != null)
      path = getParentList().getKeysPath();
    path += getName() + "[" + getKeyValue() + "]";
    return path;
  }

  public String getKeyValue() {
    if (isAllKey()) {
      return String.valueOf(getIndex());
    }
    String keys = "";
    for (DataField key : getMyKeyFields()) {
      if (keys.length() > 0)
        keys += ",";
      keys += key.getNome() + "=" + key.getValue();
    }
    return keys;
  }

  /**
   * Indica se queto gruppo è un subreport
   * 
   * @return true sse questo è un subreport
   */
  public boolean isSubreport() {
    return getModel().isSubreport();
  }

  /**
   * Metodo di passaggio dei valori.
   * 
   * Se questo gruppo ha la stessa chiave dei valori passati, la prende in
   * carico copiandone i valori e passandola ai gruppi figli. Se non ha la
   * stessa chiave la passa al gruppo fratello (se non esiste, prima lo crea).
   * 
   * @param values
   * @throws GroupException
   * @throws EvaluateException 
   * @throws DataException 
   */
  public void assignData(Map<String, Object> values) throws GroupException, EvaluateException {
    copyValues(values);
    for (GroupList list : m_childGroups.values()) {
      list.assignData(values);
    }
  }



  /**
   * Indica se questo gruppo ha dati. Un gruppo è vuoto (senza dati) se il
   * metodo next() non è mai stato chiamato. Un gruppo vuoto genera come XML una
   * stringa vuota.
   * 
   * @return true se questo gruppo ha dei dati, false altrimenti.
   */
  public boolean hasData() {
    return m_hasData;
  }

  protected void setBuilder(RootGroup root) {
    m_builder = root;
  }

  //  public StringBuffer toXML() {
  //    if ( !hasData())
  //      return new StringBuffer("");
  //    StringBuffer sXML = new StringBuffer();
  //
  //    String tabs = m_parentList == null ? "" : m_parentList.getTabs();
  //
  //    sXML.append('\n' + tabs + "<" + getName() + " index=\"" + m_groupIndex + "\"");
  //    // if (getParent() != null)
  //    // sXML.append(" parent=\"" + getParent().getNome() + getParent().getGroupIndex() + "\"");
  //    sXML.append(">");
  //    calcFields();
  //    sXML.append(toXMLCampi());
  //    sXML.append(toXMLGruppi());
  //    sXML.append('\n' + tabs + "</" + getName() + ">");
  //    return sXML;
  //  }
  //
  //  /**
  //   * Emette l'XML di tutti i campi definiti in questo gruppo, cioè i campi
  //   * chiave, i campi di output e i campi automatici.
  //   * 
  //   * @return
  //   */
  //  private String toXMLCampi() {
  //    StringBuffer sXML = new StringBuffer();
  //    String tabs = m_parentList != null ? m_parentList.getTabs() : "";
  //    tabs += '\t';
  //    for (DataField c : getMyFields()) {
  //      sXML.append('\n' + tabs + c.toXML());
  //    }
  //    return sXML.toString();
  //  }
  //
  //  private StringBuffer toXMLGruppi() {
  //    StringBuffer sXML = new StringBuffer();
  //    // loop sui gruppi figli
  //    for (GroupList list : m_childGroups.values()) {
  //      sXML.append(list.toXML());
  //    }
  //    return sXML;
  //  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String tag = m_tag == null || m_tag.length() == 0 ? "" : " (" + m_tag + ")";

    sb.append("Gruppo " + getName() + tag + "[" + keyToString());
    sb.append("][index=" + m_groupIndex + "]");
    return sb.toString();
  }

  /**
   * Restituisce modalità debug corrente.
   */
  public boolean isDebugMode() {
    return getModel().isDebugMode();
  }

  /**
   * Restituisce la quantità di occorrenze del gruppo di cui è passato il nome.
   * Il gruppo può essere ad un qualsiasi livello di discendenza, (figli,
   * nipoti,...). <br/>
   * Questo metodo può essere lento in quanto naviga tutta la struttura
   * sottostante a questo gruppo.
   * 
   * @param nomeGruppo
   *          nome del gruppo da cercare.
   * 
   * @return quantità occorrenze del gruppo di nome 'nome'
   */
  public int getDescendantGroupCount(String nomeGruppo) {
    int count = 0;
    for (GroupList gl : m_childGroups.values()) {
      if (gl.is(nomeGruppo))
        return gl.getCount();
      for (Group g : gl.getInstances())
        count += g.getDescendantGroupCount(nomeGruppo);
    }
    return count;
  }

  /**
   * Calcola la quantita di gruppi discendenti di questo, che hanno il nome
   * richiesto e soddisfano la condizione richiesta
   * 
   * @param nomeGruppo
   *          nome del gruppo richiesto
   * @param condition
   *          condizione che i gruppi devono soddisfare
   * @param evaluator
   *          oggetto che effettua la valutazione della condizione
   * @return quantità di gruppi discendenti di questo che soddisfano i criteri
   *         richiesti
   * @throws EvaluateException
   *           se il valutatore incontra un errore grave nella valutazione della
   *           condizione
   * @throws ResolveException 
   */
  public int getDescendantGroupCount(String nomeGruppo, Symbol condition, GroupEvaluator evaluator) throws EvaluateException, ResolveException {
    int count = 0;
    for (GroupList gl : m_childGroups.values()) {
      if (gl.is(nomeGruppo)) {
        //ok, l'ho trovato
        for (Group g : gl.getInstances()) {
          Object retEval = evaluator.evaluate(condition, g);
          if (retEval instanceof Boolean) {
            if ( ((Boolean) retEval).booleanValue()) {
              count++;
            }
          }
        }
        return count;
      }
      for (Group g : gl.getInstances()) {
        count += g.getDescendantGroupCount(nomeGruppo, condition, evaluator);
      }
    }
    return count;
  }

  /**
   * Ritorna la quantità di istanze di questo gruppo
   */
  public int getCount() {
    return m_parentList.getCount();
  }

  /**
   * Cerca tutti i campi del gruppo dato, con il nome dato e valore uguale al
   * valore passato.
   * 
   * @param nomeGruppo
   *          gruppo a cui deve appartenere il campo cercato. Se null il metodo
   *          esce con null.
   * @param nomeCampo
   *          nome del campo cercato. Se null il metodo esce con null.
   * @param valore
   *          valore che deve avere il campo cercato
   * @return lista campi trovati con i criteri passati
   */
  public ArrayList<Group> findGruppi(String nomeGruppo, String nomeCampo, Object valore) {
    if (nomeGruppo == null || nomeCampo == null)
      return null;

    RootGroup root = getRootGroup();
    Map<Integer, Group> mappa = new HashMap<Integer, Group>();
    root._findGruppi(mappa, nomeGruppo, nomeCampo, valore);
    return new ArrayList<Group>(mappa.values());
  }

  /**
   * Aggiunge ricorsivamente alla lista passata ('list') il campo 'nomeCampo'
   * appartenente al gruppo 'nomeGruppo' avente valore uguale al valore passato. <br/>
   * Cerca prima fra i campi di questo gruppo, poi nei gruppi fratelli, poi nei
   * gruppi figli effettuando la ricorsione in maniera breadth-first.
   * 
   * @param nomeGruppo
   *          gruppo a cui deve appartenere il campo cercato
   * @param nomeCampo
   *          nome del campo cercato
   * @param valore
   *          valore che deve avere il campo cercato
   * @return elenco campi trovati con i criteri passati
   */
  Map<Integer, Group> _findGruppi(Map<Integer, Group> list, String nomeGruppo, String nomeCampo, Object valore) {
    if (getName().equalsIgnoreCase(nomeGruppo)) {
      Group g = this;
      _addCampoCond(list, g.getField(nomeCampo), valore);
      while (g.getNext() != null) {
        g = g.getNext();
        g._findGruppi(list, nomeGruppo, nomeCampo, valore);
      }
    }

    for (GroupList gl : m_childGroups.values()) {
      for (Group g : gl.getInstances()) {
        g._findGruppi(list, nomeGruppo, nomeCampo, valore);
      }
    }
    return list;
  }

  /**
   * Ritorna l'oggetto che gestisce la lista dei gruppi figli di questo gruppo,
   * col nome passato.
   * 
   * @param nomeGruppo
   *          nome gruppo di cui si richiede la lista
   * @return GroupList che gestisce la lista dei gruppi con tale nome
   * @throws GroupException
   *           nel caso nomeGruppo non sia un gruppo figlio di questo
   */
  public GroupList getChildList(String nomeGruppo) throws GroupException {
    GroupList gl = m_childGroups.get(nomeGruppo.toLowerCase());
    if (gl == null)
      throw new GroupException("Non esiste il gruppo '" + nomeGruppo + "' figlio del gruppo " + getName());
    return gl;
  }

  private void _addCampoCond(Map<Integer, Group> list, DataField c, Object valore) {
    if (c != null && c.isEqualValue(valore))
      list.put(new Integer(c.getGroup().getID()), c.getGroup());
  }

  /**
   * Ritorna lista dei fratelli successivi a questo, questo escluso.
   * 
   * @return lista fratelli successivi
   */
  public List<Group> getNextSiblings() {
    return m_parentList.getSublist(getIndex(), m_parentList.getCount() - 1);
  }

  /**
   * @return the model
   */
  public GroupModel getModel() {
    if (m_model == null) {
      m_model = getRootGroup().getModelFromCache(m_modelId);
    }
    return m_model;
  }

  protected void setModel(GroupModel model) {
    m_model = model;
    m_modelId = model.getModelId();
  }

  /**
   * Ritorna la lista dei gruppi a cui appartiene questo gruppo. Tale lista
   * contiene quindi questo gruppo e tutti i suoi fratelli.
   * 
   * @return lista dei gruppi in cui è questo gruppo; non è mai null
   */
  public GroupList getParentList() {
    return m_parentList;
  }

  /**
   * Ritorna il gruppo padre di questo.
   */
  public Group getParent() {
    return m_parentList.getParentGroup();
  }

  /**
   * Ritorna la mappa con tutti i gruppi figli di questo. <br/>
   * La mappa è fatta così:
   * <ul>
   * <li><b>key</b> = nome gruppo figlio</li>
   * <li><b>value</b> = lista delle istanze dei gruppi, con nome=key, figli di
   * questo.</li>
   * </ul>
   */
  protected Map<String, GroupList> getChildLists() {
    return m_childGroups;
  }

  /**
   * Crea e aggiunge una lista (vuota) di gruppi figli di questo che
   * rispecchiano il GroupModel passato. <br/>
   * Il RootGroup usato per i gruppi figli creati è quello dato da
   * {@link #getRootGroup()}.
   * 
   * @param model
   *          modello dei gruppi figli
   */
  protected void addChildList(GroupModel model) {
    addChildList(model, m_builder);
  }

  /**
   * Crea e aggiunge una lista (vuota) di gruppi figli di questo che
   * rispecchiano il GroupModel passato.
   * 
   * @param model
   *          modello dei gruppi figli
   * @param root
   *          gruppo radice a cui si riferisce tutta la gerarchia dei gruppi
   */
  protected void addChildList(GroupModel model, RootGroup root) {
    GroupList gl = new GroupList(root, model, this);
    m_childGroups.put(model.getName().toLowerCase(), gl);
  }

  /**
   * @return il tag associato a questo gruppo
   */
  public String getTag() {
    return m_tag;
  }

  /**
   * Associa una stringa qualsiasi a questo gruppo. La stringa non è utilizzata
   * in alcun modo da questa classe: è un modo per associare informazioni a
   * questa istanza utilizzabili da codice esterno.
   * 
   * @param mTag
   *          contenuto del tag (può essere null)
   */
  public void setTag(String mTag) {
    m_tag = mTag;
  }

  synchronized void _initID(RootGroup builder) {
    m_groupID = builder.nextID();
  }

  /**
   * Identificativo univoco di questo gruppo fra tutte le istanze di tutti i
   * gruppi facenti capo alla stesso ListBuilder.
   * 
   * @return id univoco di questa istanza
   */
  public int getID() {
    return m_groupID;
  }

  /**
   * Ritorna true sse questo Group è il RootGroup. Equivale a:
   * <tt>this instanceof RootGroup</tt>
   * 
   * @return true se questo oggetto è il RootGroup, cioè il capo di tutta la
   *         gerarchia dei gruppi
   */
  public boolean isRoot() {
    return false;
  }

  /**
   * Ritorna il primo gruppo antenato di questo che ha come gruppo figlio quello
   * con il nome dato. Il gruppo corrente non è contemplato nella ricerca.
   * 
   * @param childName
   *          nome che deve avere il modello figlio
   * @return primo modello antenato che ha come figlio <i>childName</i>, oppure
   *         null se non trovato
   */
  public Group getAncestorWithChild(String childName) {
    if (childName == null || getParent() == null)
      return null;

    Group gm = getParent();
    while (gm != null) {
      if (gm.getChildModel(childName) != null)
        return gm;
      gm = gm.getParent();
    }
    return null;
  }

  public GroupModel getChildModel(String modelName) {
    return getModel().getChildModel(modelName);
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    s_groupCount++;
    out.defaultWriteObject();
  }

}
