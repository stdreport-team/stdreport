package org.xreports.engine;

import java.util.HashMap;
import java.util.List;

import org.xreports.Destroyable;
import org.xreports.datagroup.Group;
import org.xreports.datagroup.GroupException;
import org.xreports.datagroup.GroupModel;
import org.xreports.datagroup.RootGroup;
import org.xreports.datagroup.RootModel;
import org.xreports.dmc.SimpleList;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.stampa.DataException;
import org.xreports.stampa.NoDataException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.source.AbstractElement;
import org.xreports.stampa.source.CellElement;
import org.xreports.stampa.source.ChartElement;
import org.xreports.stampa.source.GroupElement;
import org.xreports.stampa.source.IReportElement;
import org.xreports.stampa.source.IReportNode;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

/**
 * Contiene tutte le informazioni del singolo report, che riguardano sia
 * l'elemento nel sorgente del report (il {@link GroupElement} ), sia i dati
 * associati (query e RootModel).
 * 
 * @author pier
 * 
 */
public class ReportInfo implements Destroyable {
  protected final String                ERR_NODATA           = "Impossibile generare il report: nessun dato disponibile";

  protected XReport                      m_stampa             = null;

  private String                        m_name               = null;
  private String                        m_query              = null;
  private List<HashMap<String, Object>> c_datalist           = null;
  protected RootModel                   m_rootModel          = null;
  protected GroupElement                m_groupElem          = null;

  private static final String           DUMMY_GROUP          = "dum_my";
  
  /** quantità di esecuzioni di questo subreport */
  private int                           m_executionCount     = 0;
  /**
   * quantità di millisecondi nella esecuzione delle query, cumulativo di tutte
   * le esecuzioni di questo subreport
   */
  private double                        m_executionTime      = 0;

  private long                          m_lastExecutionStart = 0;

  public ReportInfo(XReport stp, RootModel rootModel, GroupElement groupElem) throws ValidateException {
    if (groupElem == null) {
      throw new ValidateException("L'elemento <group> del report non può essere null!");
    }
    m_groupElem = groupElem;
    m_name = groupElem.getName();
    if (rootModel == null) {
      throw new ValidateException("Il modello del report" + m_groupElem.toString() + " non può essere null!");
    }
    m_rootModel = rootModel;
    m_stampa = stp;
  }

  public String getName() {
    return m_name;
  }

  /**
   * @return il testo sql della query da eseguire per caricare il report
   */
  public String getQuery() {
    return m_query;
  }

  /**
   * @param mQuery
   *          the m_query to set
   */
  protected void setQuery(String mQuery) {
    m_query = mQuery;
  }

  public RootModel getRootModel() {
    return m_rootModel;
  }

  /**
   * Ritorna l'elemento radice di questo report. L'elemento radice è sempre un
   * elemento <tt>group</tt> fittizio.
   * 
   */
  public GroupElement getGroupElem() {
    return m_groupElem;
  }

  public RootGroup getRootGroup() {
    return m_rootModel.getRootGroup();
  }

  public boolean isMainReport() {
    return false;
  }

  /**
   * Reperisce il testo della query e lo esegue, caricandone i dati nelle
   * strutture dati del subreport.
   * 
   * @return quantità di righe caricate
   * @throws DataException
   *           nel caso di errori in caricamento dei dati dalla query
   * @throws EvaluateException
   *           nel caso di errori nel reperimento del testo della query
   */
  @SuppressWarnings("unchecked")
  public int caricaDati() throws DataException, EvaluateException {
    Object query = m_groupElem.resolveQueryAttribute();
    if (query == null) {
      throw new DataException(m_groupElem.toString() + ": la query risulta null.");
    }
    try {
      loadDataStart();
      if (query instanceof String) {
        setQuery(query.toString());
        return loadQuery(query.toString());
      } else if (query instanceof SimpleList) {
        return loadList((SimpleList) query);
      } else if (query instanceof List<?>) {
        //lista dati fornita dal metodo utente: non posso azzerarla 
        return loadDataList((List<HashMap<String, Object>>)query, false);
      }      
    } catch (DataException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new DataException(m_groupElem, e, "errore inaspettato in caricamento subreport");
    } finally {
      loadDataEnd();
    }

    throw new DataException(m_groupElem.toString() + ": la query risulta di un tipo non riconosciuto " + query.getClass().getName());
  }

  protected void loadDataStart() {
    m_lastExecutionStart = System.currentTimeMillis();
  }

  protected void loadDataEnd() {
    m_executionCount++;
    long now = System.currentTimeMillis();
    m_executionTime += now - m_lastExecutionStart;
  }

  protected String getDescription() {
    String name = getName();
    if (name.endsWith(XMLSchemaValidationHandler.DEFAULTGROUP_POSTFIX)) {
      name = name.substring(0, name.length() - XMLSchemaValidationHandler.DEFAULTGROUP_POSTFIX.length());
    }
    return isMainReport() ? "Report principale" : "Subreport " + name;
  }

  public String getExecutionSummary() {
    String sOut = "";
    if ( !isMainReport()) {
      sOut = "Quantità esecuzioni: " + m_executionCount;
    }

    sOut += "; Tempo di caricamento totale:" + (m_executionTime / 1000);
    return sOut;
  }

  /**
   * Carica il testo sql passato nelle strutture dati del subreport. <br/>
   * Crea i gruppi di dati (oggetti {@link Group}) popolando il
   * {@link RootModel} del subreport.
   * 
   * @param sql
   *          testo sql da eseguire per avere i dati
   * @return quantità di righe caricate
   * @see #getRootModel()
   * @throws DataException
   *           nel caso non ci siano dati o ci siano stati errori in caricamento
   */
  protected int loadQuery(String sql) throws DataException {
    m_rootModel.setDebugMode(m_stampa.isDebugMode());
    int recordsInDB = 0;
    try {
      m_stampa.addDebugMessage("Esecuzione query per caricamento dati:\n  " + sql);
      List<HashMap<String, Object>> righe = m_stampa.getDatabase().getRowsAsMap(sql);
      recordsInDB = loadDataList(righe, true);
      righe.clear();
    } catch (DataException e) {
      throw e;
    } catch (Exception e) {
      throw new DataException(e, "Errore in caricamento dei dati SQL");
    }
    return recordsInDB;
  }

  /**
   * Carica i dati sotto forma di lista di mappe nella struttura dati gruppi/campi.
   * @param righe dati da caricare
   * @param azzeraDati flga che indica se azzerare i dati (per recuperare memoria); la lista e ogni mappa viene azzerata
   * @return quantità di record caricati
   * @throws DataException
   */
  protected int loadDataList(List<HashMap<String, Object>> righe, boolean azzeraDati) throws DataException {
    m_rootModel.setDebugMode(m_stampa.isDebugMode());
    int recordsInDB = 0;
    try {
      m_stampa.addDebugMessage("Dati in caricamento, #righe=" + righe.size());
      int modulo = (Math.min(m_stampa.getMaxNumRecords(), righe.size())) / 10;
      if (modulo == 0) {
        modulo = righe.size();
      }
      if (righe.size() == 0 && isMainReport()) {
        //la exception la do solo sul main report
        throw new NoDataException(ERR_NODATA);
      }
      int recCount = 1;
      //azzero i dati che potrebbero esserci i precedenti istanze del subreport
      m_rootModel.clearData();
      for (HashMap<String, Object> rigas : righe) {
        if (recCount == 1 || recCount % modulo == 0) {
          m_stampa.addDebugMessage("Carico record " + recCount);
        }
        m_rootModel.assignData(rigas);
        if (recCount >= m_stampa.getMaxNumRecords()) {
          break;
        }
        if (azzeraDati)
          rigas.clear();
        recCount++;
      }
      recordsInDB = righe.size();
      if (isMainReport()) {
        m_rootModel.getRootGroup().setTag("Main Report");
      } else {
        if (m_rootModel.getRootGroup() != null) {
          m_rootModel.getRootGroup().setTag(getName());
        }
      }
    } catch (NoDataException e) {
      throw e;
    } catch (Exception e) {
      throw new DataException(e, "Errore in caricamento dei dati SQL");
    }
    if (azzeraDati)
      righe.clear();
    return recordsInDB;    
  }
  
  
  public int loadList(SimpleList list) throws DataException {
    //qui c'è il codice solo per caricare dalla simplelist
    //il codice per caricare con la query è nella super classe
    m_rootModel.setDebugMode(m_stampa.isDebugMode());
    if ( !list.isLoaded()) {
      list.setMaxNumRecords(m_stampa.getMaxNumRecords());
      if ( !list.load()) {
        throw new DataException("Errore in caricamento dati con la lista");
      }
    }
    int recordsInDB = list.getCount();
    m_stampa.addInfoMessage("Associata SimpleList, qta records " + recordsInDB);
    if (recordsInDB == 0) {
      throw new NoDataException(ERR_NODATA);
    }
    try {
      //azzero i dati che potrebbero esserci i precedenti istanze del subreport
      m_rootModel.clearData();
      m_rootModel.assignSimpleList(list, m_stampa.getMaxNumRecords());
    } catch (GroupException e) {
      throw new DataException("Errore in caricamento dei dati SQL: " + e.toString());
    }
    return recordsInDB;
  }

  /**
   * Metodo che ha il compito di effettuare controlli ed elaborazioni possibili
   * solo dopo la costruzione di tutti gli elementi del report.
   * 
   * @throws ValidateException
   *           in caso di errori rilevati
   */
  public void postValidate() throws ValidateException {
    //se questo report non ha gruppi, gliene mettiamo uno fittizio
    if (getGroupCount() == 0) {
      pushGruppoFittizio();
    }
    //Aggiungiamo in automatico tutti i campi cosi' che uno non debba aggiungerseli a mano
    addFields(m_groupElem, m_rootModel);
    //controllo refFont su tutti gli elementi
    testEsistenzaFont();
    //FIXME purgeTextNodes
    //purgeTextNodes(getGroupElem());
  }

  /**
   * Analizza tutti gli elementi dell'albero sorgente e verifica se l'attributo
   * refFont specifica una font esistente. Senza questo test, la generazione del
   * documento potrebbe dare dei NullPointerException sugli elementi con font
   * errata.
   * 
   * @throws ValidateException
   *           nel caso un elemento abbia <tt>refFont</tt> errato.
   */
  private void testEsistenzaFont() throws ValidateException {
    GroupElement radice = getGroupElem();
    for (IReportNode node : radice.getChildren()) {
      if (node instanceof AbstractElement) {
        testRefFontElem((AbstractElement) node);
      }
    }
  }

//  private void purgeTextNodes(AbstractElement elem) throws ValidateException {
//    List<Integer> delete = new ArrayList<Integer>();
//    if (elem.getChildCount() > 1) {
//      for (int i=0; i < elem.getChildCount(); i++) {
//        IReportNode child = elem.getChild(i);
//        if (child instanceof TextElement) {
//          TextElement te = (TextElement)child;
//          if (te.isFittizio() && te.isOnlySpace()) {
//            if ( i > 0 && i < elem.getChildCount() - 1) {
//              AbstractElement elemPrec = (AbstractElement)elem.getChild(i - 1);
//              AbstractElement elemSucc = (AbstractElement)elem.getChild(i + 1);
//              if (elemPrec.isBlockElement() && elemSucc.isBlockElement()) {
//                delete.add(i);
//              }
//            }
//          }
//        }
//      }
//      for (Integer index : delete) {
//        elem.removeChild(elem.getChild(index));
//      }
//      for (int i=0; i < elem.getChildCount(); i++) {
//        if (elem.getChild(i).isElement())
//          purgeTextNodes((AbstractElement)elem.getChild(i));
//      }
//    }
//  }
  
  
  /**
   * Metodo ricorsivo che testa la correttezza del parametro <tt>refFont</tt> su
   * un elemento e sui suoi figli.
   * 
   * @param padre
   *          elemento di partenza
   * @throws ValidateException
   *           nel caso un elemento abbia <tt>refFont</tt> errato.
   */
  private void testRefFontElem(AbstractElement padre) throws ValidateException {
    String fontName = padre.getRefFontName();
    if (fontName == null)
      padre.setRefFontName(m_stampa.getDefaultFont().getName());
    else if (m_stampa.getFontByName(fontName) == null) {
      throw new ValidateException(padre + ", la font specificata non esiste: " + fontName);
    }
    for (IReportNode node : padre.getChildren()) {
      if (node instanceof AbstractElement) {
        testRefFontElem((AbstractElement) node);
      }
    }
  }

  /**
   * Ha il compito di aggiungere un gruppo fittizio come figlio del gruppo
   * radice predefinito.
   * 
   * @throws ValidateException
   *           exception che può arrivare dai metodi di creazione elemento
   */
  private void pushGruppoFittizio() throws ValidateException {
    GroupElement radice = getGroupElem();
    GroupModel modelFittizio = radice.getGroupModel().addChildModel(DUMMY_GROUP);
    modelFittizio.setAllKey(true);
    modelFittizio.setNullKeyAllowed(true);
    GroupElement gruppoFittizio = new GroupElement(DUMMY_GROUP, modelFittizio);
    gruppoFittizio.setAttributeValue(GroupElement.ATTRIB_KEYS, GroupElement.ALL_KEYS);
    gruppoFittizio.setAttributeValue(GroupElement.ATTRIB_NULLKEY, "true");
    radice.setChildGroup(gruppoFittizio);
  }

  /**
   * Ritorna la quantità di gruppi di questo report, cioè la quantità di
   * elementi &lt;group&gt; definiti in questo report (escludendo il gruppo
   * radice che c'è sempre ed è automaticamente messo dal sistema).
   * 
   * @return quantità di elementi &lt;group&gt; definiti in questo report
   */
  public int getGroupCount() {
    GroupElement radice = getGroupElem();
    int count = 0;
    for (IReportNode node : radice.getChildren()) {
      if (node instanceof AbstractElement) {
        count += getGroupCount((AbstractElement) node);
      }
    }
    return count;
  }

  private int getGroupCount(AbstractElement elem) {
    int count = 0;
    if (elem instanceof GroupElement) {
      count++;
    }
    for (IReportNode node : elem.getChildren()) {
      if (node instanceof AbstractElement) {
        count += getGroupCount((AbstractElement) node);
      }
    }
    return count;
  }

  /**
   * Aggiunge ricorsivamente tutti i campi ai rispettivi gruppi in modo
   * automatico.
   * 
   * @see AbstractElement#addAllFields(GroupModel)
   * 
   * @param elem
   *          elemento corrente i cui eventuali riferimenti ai campi sono da
   *          aggiungere
   * @param gruppo
   *          gruppo corrente
   */
  private void addFields(IReportElement elem, GroupModel gruppo) {
    if (elem instanceof AbstractElement) {
      GroupModel groupRepeat = null;
      if (elem instanceof CellElement) {
        CellElement cell = (CellElement) elem;
        if (cell.getRepeat() != null) {
          groupRepeat = gruppo.getDescendantModel(cell.getRepeat());
        }
      } else if (elem instanceof ChartElement) {
        ChartElement chart = (ChartElement) elem;
        if (chart.getChartGroup() != null) {
          groupRepeat = gruppo.getDescendantModel(chart.getChartGroup());
        }
      }
      if (groupRepeat != null) {
        gruppo = groupRepeat;
      }
      ((AbstractElement) elem).addAllFields(gruppo);
    }
    GroupModel gruppoCorrente = gruppo;
    for (IReportNode figlio : elem.getChildren()) {
      if (figlio instanceof GroupElement) {
        if (isMainReport() && ((GroupElement) figlio).getSubreportName() != null) {
          //è il group radice di un sottoreport, lo salto
        } else if (isMainReport() && ((GroupElement) figlio).getQueryAttribute() != null) {
          //è il group "reale" di un sottoreport, lo salto
        } else {
          gruppoCorrente = ((GroupElement) figlio).getGroupModel();
        }
      }
      if (figlio instanceof IReportElement) {
        addFields((IReportElement) figlio, gruppoCorrente);
      }
    }
  }

  /**
   * Ritrna l'istanza che si sta processando correntemente del gruppo di cui è
   * dato il nome
   * 
   * @param name
   *          nome del gruppo richiesto
   * @return istanza del gruppo corrente o null se non trovata
   */
  public Group getCurrent(String name) {
    GroupElement g;
    if (m_groupElem.getName().equalsIgnoreCase(name)) {
      g = m_groupElem;
    } else {
      g = (GroupElement) m_groupElem.getChildElement(GroupElement.class, name);
    }
    if (g == null) {
      return null;
    }

    return g.getGroup();
  }

  public void setDataList(List<HashMap<String, Object>> dataList) {
    c_datalist = dataList;
  }

  public List<HashMap<String, Object>> getDataList() {
    return c_datalist;
  }

  public void reset() {
    if (m_rootModel != null) {
      m_rootModel.reset();
    }    
//    if (m_groupElem != null) {      
//      m_groupElem.reset();
//    }
    if (c_datalist != null) {
      for (HashMap<String, Object> r : c_datalist) {
        r.clear();
      }
      c_datalist.clear();
      c_datalist = null;
    }
  }
  
  @Override
  public void destroy() {
    m_stampa = null;
    if (c_datalist != null) {
      for (HashMap<String, Object> r : c_datalist) {
        r.clear();
      }
      c_datalist.clear();
      c_datalist = null;
    }
    if (m_rootModel != null) {
      m_rootModel.destroy();
      m_rootModel = null;
    }
    if (m_groupElem != null) {      
      m_groupElem.destroy();
      m_groupElem = null;
    }
  }

}
