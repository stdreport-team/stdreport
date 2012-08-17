package org.xreports.engine.source;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.datagroup.GroupModel;
import org.xreports.exceptions.CISException;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.Stampa.GenerationStatus;
import org.xreports.stampa.output.Documento;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;
import org.xreports.util.Text;

public class GroupElement extends AbstractElement {

  /**
   * valore predefinito per specificare che tutti i campi sono chiave (vedi
   * {@link #isAllKey()})
   */
  public static final String  ALL_KEYS                  = "*";

  /** Nomi della controparte XML degli attributi del Tag "group" */
  private static final String ATTRIB_NAME               = "name";
  public static final String  ATTRIB_KEYS               = "keys";
  private static final String ATTRIB_ORDER              = "order";
  private static final String ATTRIB_ORDERBYKEY         = "orderByKey";
  private static final String ATTRIB_ORDERDIR           = "orderdir";
  private static final String ATTRIB_CASESENSITIVEORDER = "caseSensitiveOrder";
  private static final String ATTRIB_QUERY              = "query";
  private static final String ATTRIB_FILTER             = "filter";
  public static final String  ATTRIB_NULLKEY            = "nullKey";

  /** ordinamento crescente */
  public static final String  ORDER_ASC                 = "asc";
  /** ordinamento decrescente */
  public static final String  ORDER_DESC                = "desc";

  /** nome riservato per gruppo speciale */
  public static final String  RESERVED_THIS             = "this";
  public static final String  RESERVED_PARENT           = "parent";

  private List<String>        attrib_keys;
  private List<String>        attrib_order;

  /**
   * Indice del gruppo che si stava processando quando è accaduto un evento
   * newpage
   */
  private int                 c_processingGroupIndex    = 0;

  private String              c_subreport               = null;

  /**
   * Costruttore standard usato dal validatore dell'input quando trova i tag
   * &lt;group&gt;.
   * 
   * @param attrs
   *          attributi del tag
   * @param lineNum
   *          linea in cui appare il tag nel sorgente
   * @param colNum
   *          colonna in cui appare il tag nel sorgente
   * @throws ValidateException
   *           nel caso di valori errati negli attributi
   */
  public GroupElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_NAME, String.class);
    addAttributo(ATTRIB_KEYS, String.class);
    addAttributo(ATTRIB_ORDER, String.class);
    addAttributo(ATTRIB_ORDERBYKEY, Boolean.class, "false");
    addAttributo(ATTRIB_ORDERDIR, String.class, ORDER_ASC);
    addAttributo(ATTRIB_QUERY, String.class, null, TAG_VALUE);
    addAttributo(ATTRIB_FILTER, String.class, null, TAG_BOOLEAN);
    addAttributo(ATTRIB_CASESENSITIVEORDER, Boolean.class, "false");
    addAttributo(ATTRIB_NULLKEY, Boolean.class, "false");
  }

  /*
   * (non-Javadoc)
   * @see
   * ciscoop.stampa.source.AbstractElement#loadAttributes(org.xml.sax.Attributes
   * )
   */
  @Override
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    super.loadAttributes(attrs);
    if (getName() != null) {
      if ( !Text.isOnlyCharType(getName(), Text.CHAR_DIGITS + Text.CHAR_LETTERS)) {
        throw new ValidateException(this, "Nel nome di un gruppo puoi usare solo cifre o lettere");
      }
      if (Text.isOneOf(getName(), RESERVED_THIS, RESERVED_PARENT)) {
        throw new ValidateException(this, "La parola " + getName() + " e' riservata, non puoi usarla come nome di gruppo");
      }
    }

    if (existAttr(ATTRIB_ORDER) && existAttr(ATTRIB_ORDERBYKEY)) {
      throw new ValidateException(this, "Non puoi usare ambedue gli attributi: " + ATTRIB_ORDER + "," + ATTRIB_ORDERBYKEY);
    }

    attrib_keys = campiToList(getAttributeText(ATTRIB_KEYS));
    attrib_order = campiToList(getAttributeText(ATTRIB_ORDER));
  }

  /*
   * (non-Javadoc)
   * @see
   * ciscoop.stampa.source.AbstractElement#setAttributeValue(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setAttributeValue(String name, String value) throws ValidateException {
    super.setAttributeValue(name, value);
    if (name.equals(ATTRIB_KEYS)) {
      attrib_keys = campiToList(getAttributeText(ATTRIB_KEYS));
    } else if (name.equals(ATTRIB_ORDER)) {
      attrib_order = campiToList(getAttributeText(ATTRIB_ORDER));
    }
  }

  /**
   * Costruttore usato solo per i gruppi radice. I gruppi radice sono gruppi
   * speciali etc etc...
   * 
   * @param name
   * @param model
   * @throws ValidateException
   */
  public GroupElement(String name, GroupModel model) throws ValidateException {
    super(null, 0, 0);
    setAttributeValue(ATTRIB_NAME, name);
    setGroupModel(model);
  }

  public void setChildGroup(GroupElement elem) throws ValidateException {
    for (IReportNode node : c_elementiFigli) {
      elem.addChild(node);
    }
    c_elementiFigli.clear();
    addChild(elem);
  }

  /**
   * Ritorna la chiamata del metodo definita nell'qattributo <tt>query</tt>; in
   * pratica se questo metodo torna un valore diverso da null, questo
   * GroupElement è un subreport.
   * 
   * @return valore attributo <tt>query</tt>; torna null se attributo non
   *         definito
   */
  public String getQueryAttribute() {
    return getAttributeText(ATTRIB_QUERY);
  }

  /**
   * In caso di subreport, cerca l'attributo <tt>query</tt> e lo valuta nel
   * contesto corrente, quindi chiama la funzione utente (user class) collegata
   * a tale attributo e ne ritorna il risultato.
   * 
   * @return risultato valutazione attributo <tt>query</tt>.
   * @throws EvaluateException
   *           in caso di impossibilità a valutare o errori in valutazione
   */
  public Object resolveQueryAttribute() throws EvaluateException {
    GroupElement groupTag = (GroupElement) getChildElement(GroupElement.class, getSubreportName());
    if (groupTag.getQuerySymbol() != null) {
      return groupTag.getQuerySymbol().evaluate(this);
    }
    return null;
  }

  /**
   * Trasforma un elenco di campi separati da spazio in una lista di stringhe
   * 
   * @param szValoreAttributo
   *          stringa formata da sottostringhe separate da uno spazio
   * @return lista delle stringhe
   */
  private List<String> campiToList(String szValoreAttributo) {
    if (szValoreAttributo == null) {
      return null;
    }
    List<String> list = new LinkedList<String>();
    Scanner scanner = new Scanner(szValoreAttributo);
    scanner.useDelimiter(" ");
    while (scanner.hasNext()) {
      String szToken = scanner.next();
      list.add(szToken);
    }
    return list;
  }

  /**
   * Nome del gruppo
   */
  @Override
  public String getName() {
    return getAttributeText(ATTRIB_NAME);
  }

  /**
   * Nome dei campi chiave per spezzare questo gruppo
   */
  public List<String> getKeys() {
    return attrib_keys;
  }

  /**
   * Elenco dei nomi dei campi di ordinamento del gruppo. Corrisponde
   * all'attributo {@link #ATTRIB_ORDER}: se non è stato specificato ritorna
   * null.
   */
  public List<String> getOrderFields() {
    return attrib_order;
  }

  /**
   * True se l'ordinamento specificato è per i campi chiave. Corrisponde
   * all'attributo {@link #ATTRIB_ORDERBYKEY}: se non è stato specificato
   * ritorna false.
   */
  public boolean isOrderByKey() {
    return getAttrValueAsBoolean(ATTRIB_ORDERBYKEY);
  }

  /**
   * True se l'ordinamento specificato è crescente
   */
  public boolean isOrderAsc() {
    return getAttributeText(ATTRIB_ORDERDIR).equals(ORDER_ASC);
  }

  public boolean isCaseSensitiveOrder() {
    return getAttrValueAsBoolean(ATTRIB_CASESENSITIVEORDER);
  }

  public boolean isNullKeyAllowed() {
    return getAttrValueAsBoolean(ATTRIB_NULLKEY);
  }

  /**
   * Aggiungo i campi specificati per l'ordinamento come campi di output:
   * infatti devono esistere per forza nel gruppo per poterlo poi ordinare. Se
   * ci sono già, non fa nulla.
   */
  private void assignOrderby() {
    if (attrib_order != null) {
      for (String campo : attrib_order) {
        getGroupModel().addFieldSafe(campo);
      }
    }
  }

  private void assignFilter() {
    Symbol filter = getAttrSymbol(ATTRIB_FILTER);
    if (filter != null) {
      getGroupModel().setFilterInclude(filter);
      getGroupModel().setFilterEvaluator(this);
    }
  }

  /**
   * Ritorna true se questo gruppo ha specificata la chiave con
   * <tt>keys="*"</tt>. <br/>
   * In questo caso ogni occorrenza dei dati è una rottura e quindi un nuovo
   * gruppo.
   */
  public boolean isAllKey() {
    if (attrib_keys != null && attrib_keys.size() == 1) {
      return attrib_keys.get(0).equals(ALL_KEYS);
    }
    return false;
  }

  /**
   * Ritorna la lista di tutte le istanze di questo gruppo, figlie del gruppo
   * padre corrente, ordinate secondo le richieste contenute nel tag, e cioè:
   * <ul>
   * <li>se è specificato attributo <b>order</b>, usa quell'elenco dei campi</li>
   * <li>se non è specificato <b>order</b> ma è specificato keys=
   * {@link #ALL_KEYS}, non usa nessun ordinamento: semplicemente l'ordinamento
   * dei dati come arrivati dal database</li>
   * <li>se non è specificato <b>order</b> ma è specificato keys!=
   * {@link #ALL_KEYS}, usa l'ordinamento dei campi chiave</li>
   * </ul>
   * 
   * @param padre
   *          padre corrente
   * @param name
   *          nome del gruppo figlio che viene richiesto
   * @throws CISException
   *           in caso di errori nell'ordinamento
   */
  private List<Group> getFigliOrdinati(Group padre, String name) throws GenerateException {
    try {
      if (padre == null) {
        return null;
      }
      List<Group> orderedList = null;
      if (attrib_order != null) {
        //converto lista in set sequenziale
        LinkedHashSet<String> orderFields = new LinkedHashSet<String>();
        for (String orderFieldName : attrib_order) {
          orderFields.add(orderFieldName);
        }
        orderedList = padre.getOrderedChildren(name, orderFields, true, isOrderAsc());
      } else {
        if (isOrderByKey() && !isAllKey()) {
          //ordino per i campi chiave
          orderedList = padre.getFigliOrdinatiPerChiave(name, true, isOrderAsc());
        } else {
          //ordinamento naturale, come arrivano i record dalla sorgente dati
          orderedList = padre.getChildInstances(name);
        }
      }

      int i = 0;
      for (Group g : orderedList) {
        g.setIndex(i++);
      }
      return orderedList;
    } catch (Exception e) {
      throw new GenerateException("Errore in costruzione lista figli: " + e.getMessage(), e);
    }
  }

  private List<Group> getFigliOrdinati(Group padre) throws GenerateException {
    return getFigliOrdinati(padre, getName());
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    // Sono un tag group quindi cambio il contesto e passo il mio gruppo
    // parametro 'gruppo' = Group corrente globale, che deve essere un'istanza
    // del gruppo padre di questo
    List<Group> listaGruppiOrdinata = null;
    List<Elemento> listaElementi = new LinkedList<Elemento>();
    //System.out.println("generazione " + this + ": " + gruppo + "  (pagina " + stampa.getDocumento().getCurrentPageNumber() + ")");
    try {
      //      if (getSubreportName() != null) {
      //        //SONO ALL'INIZIO DI UN SUBREPORT!!!
      //        salvaStampaGruppo(stampa, gruppo);
      //        ReportInfo questoSubreport = stampa.getSubreport(getName());
      //        stampa.pushSubreportGeneration(questoSubreport);
      //        int qtaRec = questoSubreport.caricaDati();
      //        if (qtaRec > 0) {
      //          listaGruppiOrdinata = new ArrayList<Group>();
      //          listaGruppiOrdinata.add(questoSubreport.getRootGroup());
      //        }
      //      } else {
      if (getGroup() != null && getGroup().isRoot()) {
        // arrivo qui solo sul gruppo fittizio 'radice'. Tale GroupElement ha
        // come gruppo iniziale il RootGroup
        // che non corrisponde a nessun dato reale: come figli quindi ritorno
        // una lista con il solo RootGroup
        listaGruppiOrdinata = new ArrayList<Group>();
        listaGruppiOrdinata.add(getGroup());
      } else {
        // arrivo qui nei GroupElement corrispondenti a tag <group> reali del
        // report. A questo punto devo creare una lista di istanze di questo 
        // gruppo, ordinate in modo opportuno
        listaGruppiOrdinata = getFigliOrdinati(gruppo);
      }
      //      }

      if (isDebugData()) {
        stampa.debugElementOpen(this);
      }
      if (listaGruppiOrdinata != null) {
        for (int i = c_processingGroupIndex; i < listaGruppiOrdinata.size(); i++) {
          // con questa setGroup indico che sto gestendo la i.esima istanza di
          // questo gruppo, che passerò ai miei elementi figli
          setGroup(listaGruppiOrdinata.get(i));
          if (isVisible()) {
            listaElementi.addAll(createOutputForGroup(getGroup(), stampa, padre));
            c_processingGroupIndex = i;
            if (stampa.getGenerationStatus() == GenerationStatus.GOTO_NEXTPAGE) {
              return listaElementi;
            }
          }
        }
      } else {
        if (padre instanceof Documento && gruppo == null) {
          //report senza gruppi e senza dati
          listaElementi.addAll(createOutputForGroup(getGroup(), stampa, padre));
        }
      }
    } catch (GenerateException e) {
      throw e;
    } catch (Exception e) {
      throw new GenerateException("Eccezione: " + e.toString(), e);
    }

    stampa.setGenerationStatus(GenerationStatus.END_PDFGEN);
    c_processingGroupIndex = 0;
    if (isDebugData()) {
      stampa.debugElementClose(this);
    }
    return listaElementi;
  }

  @Override
  public String getXMLOpenTag() {
    String szXML = "<" + getTagName();
    szXML += getXMLAttrs();
    szXML += ">";
    return szXML;
  }

  @Override
  public String getXMLCloseTag() {
    return "</" + XMLSchemaValidationHandler.ELEMENTO_GROUP + ">";
  }

  private List<Elemento> createOutputForGroup(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    List<Elemento> listaFinaleGruppo = new LinkedList<Elemento>();
    List<Elemento> listaElementi = new LinkedList<Elemento>();
    if (stampa.isDebugMode()) {
      if (gruppo != null) {
        stampa.addDebugMessage("PDF gruppo " + gruppo.getName() + "[" + gruppo.getKeyValue() + "]");
      } else {
        stampa.addDebugMessage("PDF gruppo null");
      }
    }
    IReportNode reportElem = null;
    for (int i = getProcessingChildIndex(); i < c_elementiFigli.size(); i++) {
      reportElem = c_elementiFigli.get(i);
      if ( (gruppo != null && gruppo.hasData()) || gruppo == null) {
        listaElementi = reportElem.generate(gruppo, stampa, padre);
        for (Elemento elem : listaElementi) {
          elem.fineGenerazione();
          listaFinaleGruppo.add(elem);
        }
        if (stampa.getGenerationStatus() == GenerationStatus.MET_NEWPAGE) {
          //Setto l'indice all'elemento dopo il newpage!
          setProcessingChildIndex(i + 1);
          stampa.setGenerationStatus(GenerationStatus.GOTO_NEXTPAGE);
          return listaFinaleGruppo;
        }
        if (stampa.getGenerationStatus() == GenerationStatus.GOTO_NEXTPAGE) {
          setProcessingChildIndex(i);
          return listaFinaleGruppo;
        }
      }
    }
    // Finiti di processare i figli resetto processingChildIndex per 
    //un eventuale prossimo gruppo da elaborare 
    setProcessingChildIndex(0);

    return listaFinaleGruppo;
  }

  @Override
  public void fineParsingElemento() {
    assignOrderby();
    assignFilter();
  }

  @Override
  public String toString() {
    String out = " name=" + getName();
    return getTagName() + out + super.getNodeLocation();
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_GROUP;
  }

  /**
   * @return il c_subreport
   */
  public String getSubreportName() {
    return c_subreport;
  }

  /**
   * @param cSubreport
   *          the c_subreport to set
   */
  public void setSubreportName(String cSubreport) {
    c_subreport = cSubreport;
  }

  /**
   * @return il m_querySymbol
   */
  public Symbol getQuerySymbol() {
    return getAttrSymbol(ATTRIB_QUERY);
  }

  /**
   * @return il simbolo top level dell'espressione contenuta nell'attributo
   *         <tt>filter</tt>
   */
  public Symbol getFilterSymbol() {
    return getAttrSymbol(ATTRIB_FILTER);
  }

  /**
   * @return il c_processingGroupIndex
   */
  public int getProcessingGroupIndex() {
    return c_processingGroupIndex;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }

  //  /*
  //   * (non-Javadoc)
  //   * @see ciscoop.stampa.source.AbstractElement#addChild(ciscoop.stampa.source.
  //   * IReportNode)
  //   */
  //  @Override
  //  public void addChild(IReportNode reportElem) throws ValidateException {
  //    AbstractNode node = (AbstractNode)reportElem; 
  //    if (node.isContentElement()) {
  //      if ( !node.isBlockElement()) {
  //        if (c_textFittizio == null) {
  //          c_textFittizio = new TextElement(null, getLineNum(), getColumnNum());
  //          super.addChild(c_textFittizio);          
  //        }
  //        c_textFittizio.addChild(reportElem);
  //        return;
  //      }
  //      //elemento block
  //      if (c_textFittizio != null)
  //        c_textFittizio.addChild(reportElem);
  //      else
  //        super.addChild(reportElem);
  //    }
  //    else {
  //      //caso di elemento non "content", ad esempio pageHeader, marginBox,...
  //      super.addChild(reportElem);      
  //    }
  //  }

  @Override
  public boolean canChildren() {
    // TODO Auto-generated method stub
    return true;
  }
}
