package org.xreports.engine.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

import org.xreports.Destroyable;
import org.xreports.datagroup.DataField;
import org.xreports.datagroup.DataFieldModel;
import org.xreports.datagroup.Group;
import org.xreports.datagroup.GroupException;
import org.xreports.datagroup.GroupList;
import org.xreports.datagroup.GroupModel;
import org.xreports.datagroup.RootModel;
import org.xreports.expressions.symbols.BoolExpression;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Evaluator;
import org.xreports.expressions.symbols.Field;
import org.xreports.expressions.symbols.Function;
import org.xreports.expressions.symbols.MethodCall;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.ResolveException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.StampaException;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.output.impl.itext.DocumentoIText;
import org.xreports.stampa.output.impl.itext.PageListener;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

/**
 * Rappresenta un elemento del report XML significativo per il sistema. <br>
 * Può essere solamente un nodo di tipo elemento ( {@link TextElement},
 * {@link GroupElement},...) e non un nodo di tipo testo {@link TextNode} La
 * classe che genera tali elementi è {@link XMLSchemaValidationHandler}. Ogni
 * elemento ha un parent, accessibile tramite {@link #getParent()}; il nodo
 * radice della gerarchia ha {@link #getParent()}==null ed è accessibile tramite
 * {@link Stampa#getElemRadice()}.
 * 
 */
public abstract class AbstractElement extends AbstractNode implements IReportElement, Evaluator, GroupEvaluator, Destroyable {

  public static final String     ATTRIB_MARGIN_LEFT   = "marginLeft";
  public static final String     ATTRIB_MARGIN_BOTTOM = "marginBottom";
  public static final String     ATTRIB_MARGIN_TOP    = "marginTop";
  public static final String     ATTRIB_MARGIN_RIGHT  = "marginRight";

  /** funzione predefinita di conteggio */
  public static final String     FUNZ_COUNT           = "count";
  /** funzione predefinita di conteggio di tutte le occorrenze */
  public static final String     FUNZ_COUNTALL        = "countAll";
  /** funzione predefinita di conteggio di valori distinti */
  public static final String     FUNZ_COUNTDIST       = "countDistinct";
  /**
   * funzione predefinita di conteggio di valori distinti di tutte le occorrenze
   */
  public static final String     FUNZ_COUNTDISTALL    = "countDistinctAll";
  /** funzione predefinita di somma */
  public static final String     FUNZ_SUM             = "sum";
  /** funzione predefinita di somma di tutte le occorrenze */
  public static final String     FUNZ_SUMALL          = "sumAll";
  /** funzione predefinita di max sui campi */
  public static final String     FUNZ_MAX             = "maxvalue";
  /** funzione predefinita di max sui campi in tutte le occorrenze */
  public static final String     FUNZ_MAXALL          = "maxvalueAll";
  /** funzione predefinita di min sui campi */
  public static final String     FUNZ_MIN             = "minvalue";
  /** funzione predefinita di min sui campi in tutte le occorrenze */
  public static final String     FUNZ_MINALL          = "minvalueAll";
  /** funzione prev(field) */
  public static final String     FUNZ_PREV            = "prev";
  /** funzione next(field) */
  public static final String     FUNZ_NEXT            = "next";
  /**
   * funzione predefinita recnum() che calcola il progressivo all'interno del
   * gruppo
   */
  public static final String     FUNZ_RECNUM          = "recnum";
  /** funzione predefinita pagenum() che ritorna il numero corrente di pagina */
  public static final String     FUNZ_PAGENUM         = "pagenum";
  /** funzione che indica se un campo esiste oppure no */
  public static final String     FUNZ_EXIST           = "exist";
  /** funzione che torna il valore corrente di un campo */
  public static final String     FUNZ_CURRENT         = "current";
  /** funzione che torna il valore corrente di un campo a inizio pagina */
  public static final String     FUNZ_CURRENTSTART    = "currentStart";
  /**
   * funzione che torna true sse il record corrente è il primo nel gruppo
   * corrente
   */
  public static final String     FUNZ_FIRST           = "first";
  /**
   * funzione che torna true sse il record corrente è l'ultimo nel gruppo
   * corrente, oppure l'indice 0-based dell'ultimo gruppo nelle qualified
   * expression
   */
  public static final String     FUNZ_LAST            = "last";
  /**
   * funzione valida solo nelle qualified expression: torna l'indice del record
   * corrente
   */
  private Map<String, Attributo> m_attributi          = null;

  public enum HAlign {
    LEFT, CENTER, RIGHT, JUSTIFIED
  }

  public enum VAlign {
    TOP, MIDDLE, BOTTOM
  }

  public static final String  ATTRIB_VISIBLE         = "visible";
  public static final String  ATTRIB_REFFONT         = "refFont";
  public static final String  ATTRIB_VALIGN          = "valign";
  public static final String  ATTRIB_HALIGN          = "halign";

  public static final String  ATTRIB_BORDER          = "border";
  public static final String  ATTRIB_BORDERTOP       = ATTRIB_BORDER + "Top";
  public static final String  ATTRIB_BORDERBOTTOM    = ATTRIB_BORDER + "Bottom";
  public static final String  ATTRIB_BORDERLEFT      = ATTRIB_BORDER + "Left";
  public static final String  ATTRIB_BORDERRIGHT     = ATTRIB_BORDER + "Right";

  public static final String  TAG_VALUE              = "VAL";
  public static final String  TAG_BOOLEAN            = "BOOL";
  public static final String  TAG_TEXT               = "TEXT";

  public static final String  DEFAULT_HALIGN         = "left";
  public static final String  DEFAULT_VALIGN         = "middle";

  private VAlign              c_valign;
  private HAlign              c_halign;

  private Stampa              c_stampa;
  private GroupModel          c_model;
  private Group               c_dataGroup;

  /**
   * durante la valutazione di una qualified expression di un campo, è la lista
   * dei gruppi su cui si deve valutare l'indice
   */
  private List<Group>         c_qualifiedGroups;
  /**
   * durante la valutazione di una qualified expression di un campo, è il gruppo
   * corrente solo nel caso che il gruppo richiesto sia quello corrente o un
   * antenato
   */
  private Group               c_qualifiedCurrent;

  /** Lista degli elementi figli di questo Tag */
  protected List<IReportNode> c_elementiFigli        = null;

  /** Indice del figlio che si sta processando */
  private int                 c_processingChildIndex = 0;

  /**
   * Costruttore oggetto.
   * 
   * @param attrs
   *          attributi: qui vengono gestiti solo quelli comuni
   * @param lineNum
   *          linea in cui appare il tag nel sorgente XML
   * @param colNum
   *          colonna in cui appare il tag nel sorgente XML
   */
  public AbstractElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    c_elementiFigli = new LinkedList<IReportNode>();
    loadAttributes(attrs);
    copyFromParentAttributes();
  }

  /**
   * Costruttore oggetto con riferimento s Stampa.
   * 
   * @param attrs
   *          attributi: qui vengono gestiti solo quelli comuni
   * @param lineNum
   *          linea in cui appare il tag nel sorgente XML
   * @param colNum
   *          colonna in cui appare il tag nel sorgente XML
   */
  public AbstractElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    c_elementiFigli = new LinkedList<IReportNode>();
    c_stampa = stampa;
    loadAttributes(attrs);
    copyFromParentAttributes();
  }

  private void copyFromParentAttributes() {
    if (getRefFontName() == null && getParent() != null) {
      setRefFontName( ((AbstractElement) getParent()).getRefFontName());
    }
    if (getHalign() == null && getParent() != null) {
      setHalign( ((AbstractElement) getParent()).getHalign());
    }
  }

  @Override
  public void setParent(IReportElement elemParent) {
    super.setParent(elemParent);
    copyFromParentAttributes();
  }

  /**
   * Questo metodo serve per inizializzare gli attributi, cioè aggiungere alla
   * lista degli attributi mantenuta da AbstractElement, gli attributi specifici
   * del particolare elemento. Questo metodo viene automaticamente chiamato dal
   * costruttore di AbstractElement. <br/>
   * Nelle classi che ereditano da AbstractElement, la <tt>intAttrs()</tt> deve
   * avere come prima istruzione
   * 
   * <pre>
   * super.initAttrs()
   * </pre>
   */
  protected void initAttrs() {
    if (m_attributi != null) {
      //già inizializzati
      return;
    }
    m_attributi = new HashMap<String, Attributo>();
    addAttributo(ATTRIB_VISIBLE, String.class, null, TAG_BOOLEAN);
    addAttributo(ATTRIB_REFFONT, String.class);
    addAttributo(ATTRIB_HALIGN, String.class);
    addAttributo(ATTRIB_VALIGN, String.class, DEFAULT_VALIGN);
  }

  /**
   * Questo metodo viene automaticamente chiamato dal costruttore di
   * AbstractElement. Legge gli attributi dalla collection ritornata dal
   * validatore XML e valorizza gli attributi inizializzati nella
   * {@link #initAttrs()}. <br>
   * Nelle classi che ereditano da AbstractElement, la <tt>loadAttributes</tt>
   * può essere ridefinita nel caso serva una validazione/controllo particolare
   * su alcuni attributi; in tal caso il metodo deve avere come prima istruzione
   * 
   * <pre>
   * super.loadAttributes(attrs)
   * </pre>
   * 
   * @param attrs
   *          collection di attributi provenienti dal parser XML
   * @throws ValidateException
   *           nel caso un attributo non abbia il formato/tipo richiesto.
   */
  protected void loadAttributes(Attributes attrs) throws ValidateException {
    if (m_attributi == null) {
      initAttrs();
    }
    for (Attributo a : m_attributi.values()) {
      a.load(attrs);
    }

    c_halign = identificaHAlign(getAttributeText(ATTRIB_HALIGN));
    c_valign = identificaVAlign(getAttributeText(ATTRIB_VALIGN));
  }

  public void setAttributeValue(String name, String value) throws ValidateException {
    if (m_attributi.containsKey(name)) {
      m_attributi.get(name).setValue(value);
    }
  }

  /**
   * Ritorna il valore stringa dell'attributo
   * 
   * @param name
   *          nome dell'attributo
   * @return valore stringa (cioè esattamente quello che c'è specificato nel
   *         XML)
   */
  public String getAttributeText(String name) {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getText();
    }
    return null;
  }

  /**
   * Ritorna true sse l'attributo non esiste nel sorgente e non è stato passato
   * alcun valore di default.
   * 
   * @param name
   *          nome qattributo
   * @return vedi sopra
   */
  public boolean isAttrNull(String name) {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).isNull();
    }
    return true;
  }

  /**
   * Ritorna true se l'attributo è stato definito nel sorgente XML
   * 
   * @param name
   *          nome attributo
   * @return vedi sopra
   */
  public boolean existAttr(String name) {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).exists();
    }
    return false;
  }

  public Integer getAttrValueAsInteger(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Number) {
        return Integer.valueOf( ((Number) v).intValue());
      }
    }
    return null;
  }

  public Float getAttrValueAsFloat(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Number) {
        return Float.valueOf( ((Number) v).floatValue());
      } else if (v instanceof Measure) {
        return ((Measure) v).getValue();
      }
    }
    return null;
  }

  public Margini getAttrValueAsMargini(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Margini) {
        return (Margini) v;
      }
    }
    return null;
  }

  public Boolean getAttrValueAsBoolean(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Boolean) {
        return (Boolean) v;
      }
    }
    return null;
  }

  /**
   * Ritorna un attributo come misura.
   * 
   * @param name
   *          nome attributo
   * @return attributo come oggetto Measure, oppure null se attributo non è
   *         stato specificato o non è del tipo Measure
   */
  public Measure getAttrValueAsMeasure(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Measure) {
        return (Measure) v;
      }
    }
    return null;
  }

  public Border getAttrValueAsBorder(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Border) {
        return (Border) v;
      }
    }
    return null;
  }

  public Colore getAttrValueAsColore(String name) {
    if (m_attributi.containsKey(name)) {
      Object v = m_attributi.get(name).getObjValue();
      if (v instanceof Colore) {
        return (Colore) v;
      }
    }
    return null;
  }

  /**
   * Nel caso l'attributo coinvolga il parsing di una espressione, ritorna il
   * root symbol dell'albero sintattico dell'espressione analizzata.
   * 
   * @param name
   *          nome dell'attributo
   * @return radice dell'albero sintattico, oppure null se l'attributo come
   *         valore non ha un'espressione oppure l'espressione ha un errore
   *         sintattico
   */
  public Symbol getAttrSymbol(String name) {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getExpressionValue();
    }
    return null;
  }

  /**
   * Ritorna l'attributo di cui è dato il nome
   * 
   * @param name
   *          nome attributo (case sensitive)
   * @return attributo cercato oppure null se non esiste
   */
  public Attributo getAttributo(String name) {
    return m_attributi.get(name);
  }

  /**
   * Ritorna il valore dell'attributo come istanza della classe definita come
   * suo tipo. Un attributo long tornerà un oggetto Long, uno stringa un oggetto
   * String, ...
   * 
   * @param name
   *          nome attributo
   * @return valore attributo convertito nella classe specifica corrispondente
   *         al suo tipo
   */
  public Object getAttrValue(String name) {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getObjValue();
    }
    return null;
  }

  /**
   * Ritorna il valore dell'attributo valutando l'espressione in esso contenuta.
   * 
   * @param name
   *          nome attributo
   * @return valore booleano dell'espressione oppure <b>null</b> se l'attributo
   *         non è stato definito
   * @throws GenerateException
   *           nel caso l'attributo non sia un'espressione da valutare oppure
   *           nel caso la valutazione dia errore
   */
  public Boolean getExpressionAsBoolean(String name) throws ResolveException {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getExpressionAsBoolean(this);
    }
    return null;
  }

  /**
   * Ritorna il valore dell'attributo valutando l'espressione in esso contenuta.
   * 
   * @param name
   *          nome attributo
   * @return valore numerico dell'espressione oppure <b>null</b> se l'attributo
   *         non è stato definito
   * @throws GenerateException
   *           nel caso l'attributo non sia un'espressione da valutare oppure
   *           nel caso la valutazione dia errore
   */
  public Number getExpressionAsNumber(String name) throws ResolveException {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getExpressionAsNumber(this);
    }
    return null;
  }

  /**
   * Ritorna il valore dell'attributo valutando l'espressione in esso contenuta.
   * 
   * @param name
   *          nome attributo
   * @return valore stringa dell'espressione oppure <b>null</b> se l'attributo
   *         non è stato definito
   * @throws GenerateException
   *           nel caso l'attributo non sia un'espressione da valutare oppure
   *           nel caso la valutazione dia errore
   */
  public String getExpressionAsString(String name) throws ResolveException {
    if (m_attributi.containsKey(name)) {
      return m_attributi.get(name).getExpressionAsString(this);
    }
    return null;
  }

  /**
   * Aggiunge un attributo.
   * 
   * @param nome
   *          nome attributo
   * @param classeDato
   *          classe dell'attributo
   * @param valoreDefault
   *          valore di default in formato stringa
   * @return attributo aggiunto
   */
  public Attributo addAttributo(String nome, Class<?> classeDato, String valoreDefault) {
    Attributo a = new Attributo(nome, classeDato, valoreDefault);
    m_attributi.put(nome, a);
    return a;
  }

  /**
   * Aggiunge un attributo di tipo Border.
   * 
   * @param nome
   *          nome attributo
   * @param valoreDefault
   *          valore di default in formato stringa
   * @param borderPosition
   *          posizione bordo
   * @return attributo aggiunto
   */
  public Attributo addAttributoBorder(String nome, String valoreDefault, int borderPosition) {
    Attributo a = new Attributo(nome, valoreDefault, borderPosition);
    m_attributi.put(nome, a);
    return a;
  }

  /**
   * Aggiunge un attributo di tipo Border.
   * 
   * @param nome
   *          nome attributo
   * @param valoreDefault
   *          valore di default in formato stringa
   * @param borderPosition
   *          posizione bordo
   * @return attributo aggiunto
   */
  public Attributo addAttributoMeasure(String nome, String valoreDefault, boolean percent, boolean lines) {
    Attributo a = new Attributo(nome, Measure.class, valoreDefault);
    a.setMeasureLines(lines);
    a.setMeasurePercent(percent);
    m_attributi.put(nome, a);
    return a;
  }

  /**
   * Aggiunge un attributo di tipo Colore.
   * 
   * @param nome
   *          nome attributo
   * @param valoreDefault
   *          nome del colore di default
   * @return attributo aggiunto
   */
  public Attributo addAttributoColore(String nome, String coloreDefault) {
    Attributo a = new Attributo(nome, Colore.class, coloreDefault, c_stampa);
    m_attributi.put(nome, a);
    return a;
  }

  /**
   * Aggiunge un attributo.
   * 
   * @param nome
   *          nome attributo
   * @param classeDato
   *          classe dell'attributo
   * @param valoreDefault
   *          valore di default in formato stringa
   * @param expressionTag
   *          imposta l'attributo come espressione da valutare e gli assegna
   *          questo tag
   * @return attributo aggiunto
   */
  public Attributo addAttributo(String nome, Class<?> classeDato, String valoreDefault, String expressionTag) {
    Attributo a = addAttributo(nome, classeDato, valoreDefault);
    a.setTaggedExpression(true, expressionTag);
    return a;
  }

  /**
   * Aggiunge un attributo con valore di default = null.
   * 
   * @param nome
   *          nome attributo
   * @param classeDato
   *          classe dell'attributo
   * @return attributo aggiunto
   */
  public Attributo addAttributo(String nome, Class<?> classeDato) {
    return addAttributo(nome, classeDato, null);
  }

  /**
   * Indica se questo elemento deve essere generato in output. Se l'attributo
   * <var>visible</var> è presente nel tag del sorgente XML, questo metodo torna
   * la valutazione dell'attributo; se non è presente, viene ritornato
   * <b>true</b>
   * 
   * @return true sse questo tag deve venire visualizzato in output
   * @throws GenerateException
   *           nel caso di valore di ritorno errato della user call
   * @throws EvaluateException
   *           nel caso di errori gravi in chiamata user call
   */
  public boolean isVisible() throws ResolveException {
    if (isAttrNull(ATTRIB_VISIBLE)) {
      // caso in cui l'attributo visible non è specificato: 
      // visible è sempre true
      return true;
    }
    try {
      if (getAttrSymbol(ATTRIB_VISIBLE) != null) {
        Object ret = getAttrSymbol(ATTRIB_VISIBLE).evaluate(this);
        if (ret instanceof Boolean) {
          return ((Boolean) ret).booleanValue();
        }
      }
    } catch (EvaluateException e) {
      throw new ResolveException(this, e);
    }
    throw new ResolveException(this, "L'espressione specificata per attributo 'visible' non è booleana!");
  }

  /**
   * Metodo che deve venire chiamato da tutti gli elementi che implementano
   * l'attributo standard <var>visible</var>. Questo metodo controlla che
   * <var>visible</var> sia un valore fra:
   * <ul>
   * <li>la costante <b>true</b>
   * <li>la costante <b>false</b>
   * <li>una user call, cioè un'espressione del tipo <tt>class.method</tt>
   * </ul>
   * Nel terzo caso, quanso viene chiamato il metodo {@link #isVisible()}, verrà
   * valutata la user class e passato indietro il valore tornato da essa.
   * 
   * @param visible
   *          valore stringa dell'attributo <var>visible</var> dell'elemento nel
   *          sorgente XML
   * @throws ValidateException
   *           nel caso l'attributo non sia un tipo di espressione ammessa
   */
  /*
   * protected void handleVisible(String visible) throws ValidateException { if
   * (visible != null) { // if (visible.equalsIgnoreCase("true") ||
   * visible.equalsIgnoreCase("false")) { // m_visible =
   * Boolean.valueOf(visible.toLowerCase()); // } // else { ExpressionParser ep
   * = new ExpressionParser(visible); if (!ep.parse()) { throw new
   * ValidateException(this.toString() +
   * "; il valore dell'attributo 'visible' non è un'espressione valida: " +
   * ep.getErrorDesc()); } m_visibleSymbol = ep.getExpression().getTreeRoot();
   * m_visibleSymbol.setTag(TAG_BOOLEAN); } }
   */

  /**
   * Metodo di utilità da chiamare da tutti gli elementi concreti per salvare
   * gli oggetti Stampa e Gruppo correnti. Questo metodo è da richiamare
   * all'inizio del metodo
   * {@link #generate(Group, Stampa, ciscoop.stampa.output.Elemento)}.
   * 
   * @param stampa
   * @param gruppo
   */
  protected void salvaStampaGruppo(Stampa stampa, Group gruppo) {
    c_stampa = stampa;
    c_dataGroup = gruppo;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#childs()
   */
  @Override
  public List<IReportNode> getChildren() {
    return c_elementiFigli;
  }

  @Override
  public void addChild(IReportNode reportElem) throws ValidateException {
    c_elementiFigli.add(reportElem);
    reportElem.setParent(this);
  }

  public int getProcessingChildIndex() {
    return c_processingChildIndex;
  }

  public void setProcessingChildIndex(int childIndex) {
    c_processingChildIndex = childIndex;
  }

  @Override
  public boolean isElement() {
    return true;
  }

  /**
   * Ritorna true sse questo elemento può avere nodi/elementi figli
   * @return true se può avere figli
   */
  public abstract boolean canChildren(); 
  
  /**
   * Cerca il primo elemento figlio di questo che ha il nome dato ed è della
   * classe data.
   * 
   * @param clazz
   *          classe che deve avere l'elemento
   * @param name
   *          nome dell'elemento
   * @return elemento trovato oppure null se non trovato
   */
  public AbstractElement getChildElement(Class<? extends AbstractElement> clazz, String name) {
    for (IReportNode node : c_elementiFigli) {
      if (node instanceof AbstractElement) {
        AbstractElement elem = (AbstractElement) node;
        if (elem.getClass() == clazz && elem.getName().equalsIgnoreCase(name)) {
          return elem;
        }
      }
    }
    return null;
  }

  @Override
  public Object evaluate(Symbol symbol) throws ResolveException {
    return evaluate(symbol, getGroup());
  }

  /**
   * Metodo usato per valutare alcuni simboli. Qui vengono valutati:
   * <ul>
   * <li>le <b>costanti</b> (valutazione delegata a oggetto {@link Stampa}.</li>
   * <li>i <b>campi</b> (field): viene valutato il campo a partire dal gruppo
   * corrente ({@link #getGroup()}).</li>
   * <li>le chiamate a <b>metodi di user classes</b></li>
   * <li>le funzioni di aggregazione sui campi <b>sum</b> e <b>count</b></li>
   * <li>le funzioni standard <b>sum()</b>, <b>recnum()</b> e <b>pagenum()</b></li>
   * </ul>
   * Se un simbolo non è previsto, ritorna null.
   * 
   * @param symbol
   *          simbolo da valutare
   * @param group
   *          istanza del gruppo corrente in cui deve essere valutato il simbolo
   */
  public Object evaluate(Symbol symbol, Group group) throws ResolveException {
    //salvo il gruppo corrente
    Group oldGroup = getGroup();
    try {
      //cambio il gruppo corrente col gruppo che mi hanno passato: questo permetterà di passare il gruppo
      //alle chiamate ricorsive di questo metodo, nel caso l'espressione coinvolga più valutazioni
      setGroup(group);
      Stampa stampa = getStampa();
      if (symbol.isField()) {
        return calcValue((Field) symbol, group);
      } else if (symbol.isConstant() && stampa != null) {
        return resolveConstant(symbol);
      } else if (symbol.isMethodCall() && stampa != null) {
        return resolveMethodCall((MethodCall) symbol, group);
      } else if (symbol.isUnaryFunction()) {
        return evaluateUnaryFunction((Function) symbol, group);
      } else if (symbol.isNullaryFunction()) {
        return evaluateNullaryFunction((Function) symbol, group);
      } else if (symbol.isIdentifier()) {
        return symbol.evaluate(null);
      } else if (symbol instanceof BoolExpression) {
        return symbol.evaluate(this);
      }

      return null;
    } catch (ResolveException e) {
      throw e;
    } catch (Exception e) {
      throw new ResolveException(this, e, "Errore grave in valutazione %s", symbol);
    } finally {
      //rimetto il gruppo corrente che c'era prima
      setGroup(oldGroup);
    }
  }

  private Object evaluateNullaryFunction(Function symbol, Group group) throws ResolveException {
    if (symbol.is(FUNZ_RECNUM)) {
      if (group != null) {
        return new Integer(group.getIndex() + 1);
      }
      return Integer.valueOf(0);
    } else if (symbol.is(FUNZ_PAGENUM)) {
      return getCurrentPage();
    } else if (symbol.isOneOf(FUNZ_COUNT)) {
      return conteggioThis(group);
    } else if (symbol.isOneOf(FUNZ_FIRST)) {
      return execFirst(symbol, group);
    } else if (symbol.isOneOf(FUNZ_CURRENT)) {
      if (c_qualifiedCurrent != null) {
        return c_qualifiedCurrent.getIndex();
      } else {
        return null;
      }
    } else if (symbol.isOneOf(FUNZ_LAST)) {
      return execLast(symbol, group);
    }
    return null;
  }

  private Object evaluateUnaryFunction(Function symbol, Group group) throws ResolveException {
    if (symbol.is(FUNZ_EXIST)) {
      return esiste((Function) symbol, group);
    }
    if (symbol.is(FUNZ_CURRENT)) {
      return current((Function) symbol, group, false);
    }
    if (symbol.is(FUNZ_CURRENTSTART)) {
      return current((Function) symbol, group, true);
    }
    Object value = null;
    if (symbol.isOneOf(FUNZ_COUNT, FUNZ_COUNTALL)) {
      value = conteggio((Function) symbol, group, symbol.is(FUNZ_COUNTALL));
    } else if (symbol.isOneOf(FUNZ_SUM, FUNZ_SUMALL)) {
      value = somma((Function) symbol, group, symbol.is(FUNZ_SUMALL));
    } else if (symbol.isOneOf(FUNZ_PREV, FUNZ_NEXT)) {
      value = fieldAccess((Function) symbol, group);
    } else if (symbol.isOneOf(FUNZ_MAX, FUNZ_MAXALL, FUNZ_MIN, FUNZ_MINALL)) {
      value = minmax((Function) symbol, group, symbol.isOneOf(FUNZ_MAXALL, FUNZ_MINALL));
    } else if (symbol.isOneOf(FUNZ_COUNTDIST, FUNZ_COUNTDISTALL)) {
      value = countDistinct((Function) symbol, group, symbol.is(FUNZ_COUNTDISTALL));
    }
    if (value != null) {
      if (getName() != null) {
        DataField c = group.getField(getName());
        c.setValue(value);
      }
      return value;
    }

    return null;
  }

  public String getName() {
    return null;
  }

  /**
   * Ritorna il RootModel (modello radice) progenitore del gruppo col nome
   * passato. Si assume che il gruppo appartenenga ad un <b>subreport</b>
   * definito dentro il gruppo in cui è definito questo elemento. <br>
   * Nota bene: il subreport può essere ad un qualsiasi livello di nesting, cioè
   * un subreport di un subreport di un subreport di...
   * 
   * @param nomeGruppo
   *          nome del group model richiesto
   * @return GroupModel richiesto oppure null se non trovato
   */
  private RootModel getSubReportModel(String nomeGruppo) {
    GroupModel model = null;
    List<GroupElement> subreports = getDescendantSubreports(getEnclosingGroup(this));
    if (subreports != null) {
      for (GroupElement tagGroup : subreports) {
        if (tagGroup.getName().equals(nomeGruppo)) {
          model = tagGroup.getGroupModel();
          break;
        }

        model = tagGroup.getGroupModel().getDescendantModel(nomeGruppo);
        if (model != null) {
          break;
        }
      }
      if (model != null) {
        return model.isRoot() ? (RootModel) model : model.getRootModel();
      }
    }

    //non trovato
    return null;
  }

  /**
   * Esegue la funzione <var>first</var>. La esegue nelle due varianti
   * possibili:
   * <ul>
   * <li>senza argomenti: ritorna un valore booleano che indica se l'istanza
   * corrente del gruppo è la prima</li>
   * <li>con 1 argomento 'field': torna il valore del campo nel primo gruppo</li>
   * </ul>
   * 
   * @param function
   *          simbolo che rappresenta la funzione nell'albero sintattico
   * @param gruppo
   *          gruppo corrente
   * @return risultato funzione <var>first</var>
   * @throws EvaluateException
   *           nel caso la versione con 1 argomento abbia argomenti errati
   */
  private Object execFirst(Function function, Group gruppo) throws ResolveException {
    int nChild = function.getChildren().size();
    if (nChild == 0) {
      return gruppo.isFirst();
    } else {
      return fieldAccess(function, gruppo);
    }
  }

  /**
   * Esegue la funzione <var>last</var>. La esegue nelle due varianti possibili:
   * <ul>
   * <li>senza argomenti: ritorna un valore booleano che indica se l'istanza
   * corrente del gruppo è l'ultima, oppure se è usato in una qualified
   * expression, ritorna l'indice 0-based dell'ultimo gruppo.</li>
   * <li>con 1 argomento 'field': torna il valore del campo nell'ultimo gruppo</li>
   * </ul>
   * 
   * @param function
   *          simbolo che rappresenta la funzione nell'albero sintattico
   * @param gruppo
   *          gruppo corrente
   * @return risultato funzione <var>last</var>
   * @throws EvaluateException
   *           nel caso la versione con 1 argomento abbia argomenti errati
   */
  private Object execLast(Function function, Group gruppo) throws ResolveException {
    int nChild = function.getChildren().size();
    if (nChild == 0) {
      if (c_qualifiedGroups != null) {
        return c_qualifiedGroups.size() - 1;
      } else {
        return gruppo.isLast();
      }
    } else {
      return fieldAccess(function, gruppo);
    }
  }

  /**
   * Esegue le funzioni di accesso ai campi del set corrente di gruppo. Le funzioni sono:
   * <ul>
   *   <li><var>prev(#field)</var>: accesso al campo <var>#field</var> del gruppo precedente</li>
   *   <li><var>next(#field)</var>: accesso al campo <var>#field</var> del gruppo successivo</li>
   *   <li><var>first(#field)</var>: accesso al campo <var>#field</var> del primo gruppo</li>
   *   <li><var>last(#field)</var>: accesso al campo <var>#field</var> dell'ultimo gruppo</li>
   * </ul>
   * NB: le funzioni <var>next</var> e <var>prev</var>, nel caso facciano riferimento ad un gruppo inesistente,
   * non emettono eccezione ma ritornano <b>null</b>.
   * 
   * @param function funzione da valutare
   * @param gruppo istanza del gruppo corrente
   * @return valutazione funzione
   * @throws EvaluateException nel caso la funzione non abbia 1 argomento di tipo field, senza specifica del gruppo
   */
  private Object fieldAccess(Function function, Group gruppo) throws ResolveException {
    String fName = function.getFunctionName();
    if (function.getChildren().size() != 1) {
      throw new ResolveException(this, "La funzione '%s' deve avere uno e un solo parametro", fName);
    }
    Symbol arg = function.getChildren().get(0);
    if ( !arg.isField()) {
      throw new ResolveException(this, "La funzione '%s' ammette solo un parametro field", fName);
    }
    Field f = (Field) arg;
    if (f.getGroup() != null && !f.getGroup().equals(gruppo.getName())) {
      throw new ResolveException(this, "La funzione '%s' può agire solo sul gruppo corrente", fName);
    }
    //ok, adesso sono sicuro che l'argomento è a posto!
    int i = gruppo.getIndex();
    if (function.getFunctionName().equals(FUNZ_NEXT)) {
      i++;
    } else if (function.getFunctionName().equals(FUNZ_PREV)) {
      i--;
    } else if (function.getFunctionName().equals(FUNZ_FIRST)) {
      i = 0;
    } else if (function.getFunctionName().equals(FUNZ_LAST)) {
      i = gruppo.getParentList().getCount() - 1;
    }

    if (i < 0 || i >= gruppo.getParentList().getCount())
      return null;
    Group target = gruppo.getParentList().getInstance(i);
    if (target.existField(f.getField()))
      return target.getField(f.getField()).getValue();
    
    return null;
  }

  /**
   * Assume che la funzione passata sia una richiesta di somma. Valuta
   * l'argomento della funzione passata e ne somma tutte le occorrenze.
   * 
   * @param function
   *          funzione da valutare (è una somma)
   * @param gruppo
   *          gruppo corrente in cui si valuta la funzione
   * @param all
   *          se true, deve fare il conteggio relativo alla funzione
   *          <tt>sumAll</tt>, altrimenti alla funzione <tt>sum</tt>
   * @return oggetto risultato della valutazione
   * @throws EvaluateException
   *           nel caso sia impossibile la valutazione
   */
  private Object somma(Function function, Group gruppo, boolean all) throws ResolveException {
    Symbol arg = function.getChildren().get(0);
    if (arg.isField()) {
      String nomeGruppo = ((Field) arg).getGroup();
      String nomeCampo = ((Field) arg).getField();
      // startGroup: gruppo di partenza da cui calcolo la somma 
      Group startGroup = null;
      Number sumResult = null;
      if (nomeCampo != null) {
        try {
          Field f = (Field) arg;
          if (f.getQualifiedExpression() != null) {
            if ( ! (f.getQualifiedExpression() instanceof BoolExpression)) {
              throw new ResolveException(this,
                  "Dentro la funzione di somma ci deve essere un'espressione booleana dopo il nome del gruppo");
            }
          }
          if (nomeGruppo != null && !nomeGruppo.equalsIgnoreCase(gruppo.getName())) {
            //espressione del tipo "sum(gruppo#campo)"
            startGroup = getAggregateFunctionStartGroup(nomeGruppo, gruppo, all);
          } else {
            //espressione del tipo "sum(#campo)" oppure "sum(gruppo#campo)" dove gruppo è uguale al nome del gruppo corrente
            //gruppo assente: significa sommare 'nomeCampo' sul gruppo corrente
            if (all) {
              startGroup = gruppo.getRootGroup();
            } else {
              startGroup = gruppo.getParent();
            }
            nomeGruppo = gruppo.getName();
          }
          if (f.getQualifiedExpression() != null) {
            sumResult = startGroup.sommaDiscen(nomeGruppo, nomeCampo, f.getQualifiedExpression(), this);
          } else {
            sumResult = startGroup.sommaDiscen(nomeGruppo, nomeCampo);
          }
          if (sumResult == null) {
            //se arrivo qui, non esiste alcuna istanza del gruppo da sommare: ritorno 0
            return Integer.valueOf(0);
          }
          return sumResult;
        } catch (ResolveException e) {
          throw e;
        } catch (Exception e) {
          throw new ResolveException(this, e, "Errore in calcolo somma");
        }
      }
    }
    //arrivo qui se come argomento di sum() non c'è un campo (oppure una specifica di gruppo senza nomecampo dopo)
    throw new ResolveException(this, "Dentro la funzione di somma ci deve essere la specifica di un campo");
  }

  /**
   * Clacolo delle funzioni maxvalue/minvale e maxvalueAll/minvalueAll .
   * 
   * @param function
   *          funzione da calcolare
   * @param gruppo
   *          gruppo corrente
   * @param all
   *          se true, indica che è una funzione "all"
   * @return risultato del calcolo; è sempre una istanza di
   *         <tt>java.lang.Number</tt>
   * @throws EvaluateException
   *           in caso di errore grave in valutazione qualified expression o
   *           sintassi inaccettabile
   */
  private Object minmax(Function function, Group gruppo, boolean all) throws ResolveException {
    Symbol arg = function.getChildren().get(0);
    if (arg.isField()) {
      String nomeGruppo = ((Field) arg).getGroup();
      String nomeCampo = ((Field) arg).getField();
      // startGroup: gruppo di partenza da cui calcolo la somma 
      Group startGroup = null;
      if (nomeCampo != null) {
        try {
          Field f = (Field) arg;
          if (f.getQualifiedExpression() != null) {
            if ( ! (f.getQualifiedExpression() instanceof BoolExpression)) {
              throw new ResolveException(this,
                  "Dentro la funzione maxvalue ci deve essere un'espressione booleana dopo il nome del gruppo");
            }
          }
          List<DataField> fieldList = null;
          if (nomeGruppo != null && !nomeGruppo.equalsIgnoreCase(gruppo.getName())) {
            //espressione del tipo "maxvalue(gruppo#campo)"
            startGroup = getAggregateFunctionStartGroup(nomeGruppo, gruppo, all);
          } else {
            //espressione del tipo "maxvalue(#campo)" oppure "maxvalue(gruppo#campo)" dove gruppo è uguale al nome del gruppo corrente
            //gruppo assente: significa min/max 'nomeCampo' sul gruppo corrente
            if (all) {
              startGroup = gruppo.getRootGroup();
            } else {
              startGroup = gruppo.getParent();
            }
            nomeGruppo = gruppo.getName();
          }
          if (f.getQualifiedExpression() != null) {
            fieldList = startGroup.getDescendantFields(nomeGruppo, nomeCampo, f.getQualifiedExpression(), this);
          } else {
            fieldList = startGroup.getDescendantFields(nomeGruppo, nomeCampo);
          }
          if (fieldList == null) {
            //se arrivo qui, non esiste alcuna istanza del gruppo da sommare: ritorno 0
            return Integer.valueOf(0);
          }
          return calcMinMax(fieldList, function.getFunctionName().startsWith(FUNZ_MAX));
        } catch (ResolveException e) {
          throw e;
        } catch (Exception e) {
          throw new ResolveException(this, e, "Errore in calcolo somma");
        }
      }
    }
    //arrivo qui se come argomento di maxvalue() non c'è un campo (oppure una specifica di gruppo senza nomecampo dopo)
    throw new ResolveException(this, "Dentro la funzione di somma ci deve essere la specifica di un campo");
  }

  private Number calcMinMax(List<DataField> fieldList, boolean calcMax) throws ResolveException {
    Number minmax = null;
    for (DataField c : fieldList) {
      //devo forzare il calcolo prima di testare empty/null e anche prima di testare il tipo
      c.calcola();
      if (c.isEmptyOrNull()) {
        continue;
      }
      if ( !c.isNumeric()) {
        throw new ResolveException(this, "Il campo %s non è numerico.", c.getNomeEsteso());
      }

      if (c.isEmptyNullOrZero()) {
        continue;
      }
      if (c.getTipo().equals(DataFieldModel.TipoCampo.LONG) || c.getTipo().equals(DataFieldModel.TipoCampo.INTEGER)) {
        if (minmax == null) {
          minmax = c.getAsLongSafe();
        } else {
          if (calcMax) {
            minmax = Math.max(minmax.longValue(), c.getAsLongSafe());
          } else {
            minmax = Math.min(minmax.longValue(), c.getAsLongSafe());
          }
        }
      } else if (c.getTipo().equals(DataFieldModel.TipoCampo.DOUBLE) || c.getTipo().equals(DataFieldModel.TipoCampo.BIGDECIMAL)) {
        if (minmax == null) {
          minmax = c.getAsDoubleSafe();
        } else {
          if (calcMax) {
            minmax = Math.max(minmax.doubleValue(), c.getAsDoubleSafe());
          } else {
            minmax = Math.min(minmax.doubleValue(), c.getAsDoubleSafe());
          }
        }
      }
    }
    return minmax;
  }

  /**
   * Ha il compito di cercare l'istanza di gruppo da cui partire per utilizzare
   * una funzione di aggregazione. L'algoritmo è il seguente:
   * <ol>
   * <li>cerco fra i sottogruppi del gruppo corrente (<tt>grpCorr</tt>) se
   * esiste un gruppo di nome <tt>nomeGruppo</tt>. Se non lo trovo vado al passo
   * successivo, altrimenti torno <tt>grpCorr</tt>.</li>
   * <li>cerco fra i sottoreport di <tt>grpCorr</tt> se esiste un subreport con
   * il gruppo di nome <tt>nomeGruppo</tt>. Se non lo trovo vado al passo
   * successivo, altrimenti torno il root group del subreport.</li>
   * <li>cerco fra gli antenati di <tt>grpCorr</tt> se esiste un gruppo che ha
   * come figlio un gruppo di nome <tt>nomeGruppo</tt>. Se lo trovo torno il
   * gruppo antenato trovato, altrimenti torno null.
   * </ol>
   * 
   * @param nomeGruppo
   *          nome del gruppo richiesto
   * @param grpCorr
   *          gruppo corrente
   * @param all
   *          se true, indica che voglio tutte le istanze del gruppo richiesto
   * @return gruppo di partenza per l'esecuzione delle funzioni di aggregazione
   *         (count/sum/...); torna null se non trova alcun gruppo con le
   *         condizioni richieste
   */
  private Group getAggregateFunctionStartGroup(String nomeGruppo, Group grpCorr, boolean all) {
    // startGroup: gruppo di partenza da cui partirà il calcolo della funzione 
    Group startGroup = null;
    //flag per segnalare che il gruppo richiesto esiste ma non ha dati: questo può
    //succedere solo se il gruppo richiesto è in un subreport successivo che quindi
    //non ha ancora i dati caricati
    boolean bNoData = false;
    if (all) {
      //funzione xxxAll: devo sommare tutto, quindi parto dal root group
      startGroup = grpCorr.getRootGroup();
      //FIXME manca da considerare il caso in cui il gruppo è in un subreport
    } else {
      GroupModel model = grpCorr.getModel().getDescendantModel(nomeGruppo);
      if (model == null) {
        RootModel root = getSubReportModel(nomeGruppo);
        if (root != null) {
          //il gruppo richiesto è in un subreport: imposto il gruppo di partenza
          //al root group del subreport in cui si trova il gruppo che cerco, 
          //in questo modo la successiva chiamata a startGroup.sommaDiscen avrà successo
          startGroup = root.getRootGroup();
          if (startGroup == null) {
            bNoData = true;
          }
        }
      } else {
        startGroup = grpCorr;
      }
      if (startGroup == null && !bNoData) {
        //discendente non trovato: provo fra gli antenati
        startGroup = grpCorr.getAncestorWithChild(nomeGruppo);
        //        GroupModel parentModel = gruppo.getAncestorWithChild(nomeGruppo);
        //        if (parentModel != null) {
        //          startGroup = gruppo.getAncestorGroup(parentModel.getName());
        //        }
      }
    }
    return startGroup;
  }

  private Object conteggio(Function function, Group gruppo, boolean all) throws ResolveException {
    Symbol arg = function.getChildren().get(0);
    try {
      if (arg.isIdentifier()) {
        String nomeGruppo = arg.getText();
        Group startGroup = getAggregateFunctionStartGroup(nomeGruppo, gruppo, all);
        if (startGroup == null) {
          // se il RootGroup è null significa che non ancora caricato alcun dato, quindi il count è 0
          return new Integer(0);
        }
        return new Integer(startGroup.getDescendantGroupCount(nomeGruppo));
      } else if (arg.isField()) {
        Field f = (Field) arg;
        if (f.getField() != null)
          throw new EvaluateException("Dentro la funzione di conteggio non ci può essere un nome di campo");
        if (f.getQualifiedExpression() instanceof BoolExpression) {
          Group startGroup = getAggregateFunctionStartGroup(f.getGroup(), gruppo, all);
          return new Integer(startGroup.getDescendantGroupCount(f.getGroup(), f.getQualifiedExpression(), this));
        } else {
          throw new ResolveException(this,
              "Dentro la funzione di conteggio ci deve essere un'espressione booleana dopo il nome del gruppo");
        }
      }
    } catch (Exception e) {
      throw new ResolveException(this, e, "Errore imprevisto in calcolo conteggio");
    }
    throw new ResolveException(this, "Dentro la funzione di conteggio ci può essere solo un nome di gruppo");
  }

  //  /**
  //   * Assume che la funzione passata sia una richiesta di conteggio. Valuta
  //   * l'argomento della funzione passata e ne conta le occorrenze.
  //   * 
  //   * @param function
  //   *          funzione da valutare (è un conteggio)
  //   * @return oggetto risultato della valutazione
  //   * @throws EvaluateException
  //   *           nel caso sia impossibile la valutazione
  //   */
  //  private Object conteggioOLD(Function function, Group gruppo) throws EvaluateException {
  //    Symbol arg = function.getChildren().get(0);
  //    if (arg.isIdentifier()) {
  //      String nomeGruppo = arg.getText();
  //      GroupModel model = gruppo.getModel().getDescendantModel(nomeGruppo);
  //      if (model == null) {
  //        RootModel root = getSubReportModel(nomeGruppo);
  //        if (root != null) {
  //          model = root.getDescendantModel(nomeGruppo);
  //        }
  //        if (model == null) {
  //          throw new EvaluateException("Funzione conteggio: il gruppo '" + nomeGruppo + "' non esiste come discendente di '"
  //              + gruppo.getName() + "' (" + this.toString() + ")");
  //        }
  //
  //        gruppo = root.getRootGroup();
  //      }
  //      try {
  //        if (gruppo == null) {
  //          // se il RootGroup è null significa che non ancora caricato alcun dato, quindi il count è 0
  //          return new Integer(0);
  //        }
  //        return new Integer(gruppo.getDescendantGroupCount(arg.getText()));
  //      } catch (Exception e) {
  //        throw new EvaluateException("Errore in calcolo conteggio: " + e.getMessage());
  //      }
  //    }
  //    throw new EvaluateException("Dentro la funzione di conteggio ci può essere solo un nome di gruppo");
  //  }

  /**
   * Implementa la funzione countDistinct
   * 
   * @param function
   * @param gruppo
   * @return
   * @throws EvaluateException
   */
  private Integer countDistinct(Function function, Group gruppo, boolean all) throws ResolveException {
    Symbol arg = function.getChildren().get(0);
    Group startGroup = null;
    if (arg.isField()) {
      String nomeGruppo = ((Field) arg).getGroup();
      String nomeCampo = ((Field) arg).getField();
      if (nomeGruppo != null) {
        startGroup = getAggregateFunctionStartGroup(nomeGruppo, gruppo, all);
      }
      try {
        //in list mantengo la lista di tutti i DataField che devo conteggiare
        Collection<DataField> list = null;
        if (nomeGruppo == null) {
          GroupList listaGruppi = gruppo.getParentList();
          list = new ArrayList<DataField>();
          for (Group g : listaGruppi.getInstances()) {
            list.add(g.getField(nomeCampo));
          }
        } else {
          list = startGroup.getDescendantFields(nomeGruppo, nomeCampo);
        }
        //per contare i valori distinti, semplicemente creo un Set in modo da eliminare i doppioni
        Set<Object> values = new HashSet<Object>();
        for (DataField f : list) {
          if ( !f.isNull())
            values.add(f.getValue());
        }

        return new Integer(values.size());
      } catch (GroupException e) {
        throw new ResolveException(this, e, "Errore in calcolo funzione %s", function.getFunctionName());
      }
    }
    throw new ResolveException(this, "Dentro la funzione %s ci deve essere la specifica di un campo", function.getFunctionName());
  }

  //  /**
  //   * Implementa la funzione countDistinct
  //   * 
  //   * @param function
  //   * @param gruppo
  //   * @return
  //   * @throws EvaluateException
  //   */
  //  private Integer countDistinctOLD(Function function, Group gruppo) throws EvaluateException {
  //    Symbol arg = function.getChildren().get(0);
  //    if (arg.isField()) {
  //      String nomeGruppo = ((Field) arg).getGroup();
  //      String nomeCampo = ((Field) arg).getField();
  //      if (nomeGruppo != null) {
  //        // guardo se esiste il gruppo specificato
  //        GroupModel model = gruppo.getModel().getDescendantModel(nomeGruppo);
  //        if (model == null) {
  //          RootModel root = getSubReportModel(nomeGruppo);
  //          if (root == null) {
  //            throw new EvaluateException("Funzione somma: il gruppo '" + nomeGruppo + "' non esiste come discendente di '"
  //                + gruppo.getName() + "' (" + this.toString() + ")");
  //          }
  //
  //          gruppo = root.getRootGroup();
  //        }
  //      }
  //      try {
  //        Collection<DataField> list = null;
  //        if (nomeGruppo == null) {
  //          GroupList listaGruppi = gruppo.getParentList();
  //          list = new ArrayList<DataField>();
  //          for (Group g : listaGruppi.getInstances()) {
  //            list.add(g.getField(nomeCampo));
  //          }
  //        } else {
  //          list = gruppo.getDescendantFields(nomeGruppo, nomeCampo);
  //        }
  //        Set<Object> values = new HashSet<Object>();
  //        for (DataField f : list) {
  //          if ( !f.isNull())
  //            values.add(f.getValue());
  //        }
  //
  //        return new Integer(values.size());
  //      } catch (GroupException e) {
  //        throw new EvaluateException("Errore in calcolo somma: " + e.getMessage());
  //      }
  //    }
  //    throw new EvaluateException("Dentro la funzione countDistinct ci deve essere la specifica di un campo");
  //  }

  /**
   * Assume che la funzione passata sia una richiesta di esistenza. Valuta
   * l'argomento della funzione passata e controlla se esiste.
   * 
   * @param function
   *          funzione da valutare (esistenza di un campo)
   * @return oggetto risultato della valutazione
   * @throws EvaluateException
   *           nel caso sia impossibile la valutazione
   */
  private Object esiste(Function function, Group gruppo) throws ResolveException {
    Symbol arg = function.getChildren().get(0);
    if (arg.isField()) {
      Field f = (Field) arg;
      String nomeGruppo = f.getGroup();
      Group ilGruppo = gruppo;
      if (nomeGruppo != null) {
        ilGruppo = gruppo.getAncestorGroup(nomeGruppo);
        if (ilGruppo == null) {
          throw new ResolveException(this, "Funzione esistenza: il gruppo '%s' non esiste come antenato di '%s'", nomeGruppo, gruppo.getName());
        }
      }
      if ( !ilGruppo.existField(f.getField())) {
        return Boolean.FALSE;
      }
      DataField c = ilGruppo.getField(f.getField());

      return Boolean.valueOf( !c.isEmpty());
    }
    throw new ResolveException(this, "Dentro la funzione di conteggio ci deve essere un nome di campo");
  }

  private Object current(Function function, Group gruppo, boolean start) throws ResolveException {
    try {
      if (c_stampa.getDocumento() instanceof DocumentoIText) {
        DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
        PageListener pl = doc.getPageListener();
        Symbol arg = function.getChildren().get(0);
        if (start) {
          return pl.getCurrentStartValue(arg.getFullText());
        }

        return pl.getCurrentValue(arg.getFullText());
        //				throw new EvaluateException(
        //						"Dentro la funzione di current deve esserci un campo con il nome di gruppo");
      }
      return null;
    } catch (GenerateException e) {
      throw new ResolveException(this, e, "Errore grave in valutazione funzione '%s'", function.getFunctionName());
    }

  }

  /**
   * Implementazione della funzione <code>count()</code>.
   * 
   * @param gruppo
   *          gruppo corrente
   * @return qta istanze del gruppo corrente
   */
  private Object conteggioThis(Group gruppo) {
    if (gruppo != null) {
      return Integer.valueOf(gruppo.getCount());
    }

    return Integer.valueOf(0);
  }

  /**
   * Dato un gruppo e un nome di campo, ne ritorna il valore calcolandolo dal
   * gruppo passato. Versione semplificata per campi con attributo name.
   * 
   * @param fieldName
   *          nome del campo
   * @param gruppo
   *          istanza corrente del gruppo in cui viene valutato il valore del
   *          campo richiesto
   * @return valore calcolato; se il risultato della valutazione è null, viene
   *         ritornato proprio null !
   * @throws EvaluateException
   *           in caso di mancanza di dati
   */
  protected Object calcValue(String fieldName, Group gruppo) throws EvaluateException {
    if (gruppo == null) {
      throw new EvaluateException("Impossibile valutare il campo " + fieldName + ": non ho un gruppo corrente.");
    }
    String nomeGruppo = gruppo.getName();
    if ( !gruppo.hasData()) {
      throw new EvaluateException("Impossibile valutare il campo " + fieldName
          + ": non ci sono dati nell'istanza corrente del gruppo " + nomeGruppo);
    }
    DataField campo = gruppo.getField(fieldName);
    if (campo == null || (campo.isEmpty()) && !campo.isAuto()) {
      //NB: se un campo è empty e non è un campo automatico, significa che l'ho definito
      //    nel sorgente ma nella query non esiste: non do exception ma un valore con segnalazione.
      return "?? " + nomeGruppo + "#" + fieldName + " non trovato ??";
    }

    campo.calcola();
    if (campo.isEmptyOrNull()) {
      return null;
    }

    return campo.getValue();
  }

  /**
   * Dato un gruppo e un campo, ne ritorna il valore calcolandolo dal gruppo
   * corrente.
   * 
   * @param field
   *          simbolo corrispondente al campo field da valutare
   * @param gruppo
   *          istanza corrente del gruppo in cui viene valutato il valore del
   *          campo richiesto
   * 
   * @return valore calcolato; se il risultato della valutazione è null, viene
   *         ritornato proprio null !
   * @throws EvaluateException
   *           in caso di mancanza di dati
   */
  protected Object calcValue(Field field, Group gruppo) throws ResolveException {
    if (gruppo == null) {
      throw new ResolveException(this, "Impossibile valutare %s: non ho un gruppo corrente.", field);
    }
    String nomeGruppo = field.getGroup();
    String nomeCampo = field.getField();
    if (nomeGruppo == null) {
      nomeGruppo = "";
    }
    Group okGroup = gruppo;
    boolean withCondition = field.getQualifiedExpression() != null;
    if (nomeGruppo.length() > 0) {
      okGroup = getAncestorGroup(gruppo, nomeGruppo);
    }
    if (withCondition) {
      if (field.getQualifiedExpression() instanceof BoolExpression) {
        throw new ResolveException(this, "Qualified expression booleana imprevista");
      }
      if (okGroup == null) {
        //nel caso di qualified fields, il gruppo può essere cercato anche nei discendenti.
        //Arrivo qui quando 'nomeGruppo' NON è un gruppo antenato del corrente,
        //Quindi intanto mi prendo la lista di tutte le istanze del gruppo discendente 'nomegruppo'; poi quando ho valutato
        //la qualified expression, andrò a prendere l'istanza giusta
        c_qualifiedGroups = getDescendantGroups(gruppo, nomeGruppo);
        c_qualifiedCurrent = null;
      } else {
        //Arrivo qui quando 'nomeGruppo' è un gruppo antenato del corrente o il corrente.
        //Quindi intanto mi prendo la lista di tutte le istanze del gruppo 'nomegruppo'; poi quando ho valutato
        //la qualified expression, andrò a prendere l'istanza giusta       
        c_qualifiedGroups = okGroup.getParentList().getInstances();
        c_qualifiedCurrent = okGroup;
      }
      if (c_qualifiedGroups != null) {
        try {
          //valuto la qualified expression solo se ho trovato dei gruppi!
          Object valIndex = field.getQualifiedExpression().evaluate(this);
          int groupIndex = -1;
          if (valIndex instanceof Number) {
            groupIndex = ((Number) valIndex).intValue();
          } else {
            throw new ResolveException(this, "La qualified expression non è numerica");
          }
          if (groupIndex < 0 || groupIndex >= c_qualifiedGroups.size()) {
            throw new ResolveException(this, "Il gruppo %s di indice richiesto %i non esiste", nomeGruppo, groupIndex);
          } else {
            okGroup = c_qualifiedGroups.get(groupIndex);
          }
        } catch (ResolveException ee) {
          throw ee;
        } catch (Exception ex) {
          throw new ResolveException(this, ex);
        } finally {
          c_qualifiedGroups = null;
          c_qualifiedCurrent = null;
        }
      }
    }
    if (okGroup == null) {
      throw new ResolveException(this, "Non riesco a trovare il gruppo %s", nomeGruppo);
    }
    if ( !okGroup.hasData()) {
      throw new ResolveException(this, "Non ci sono dati nell'istanza corrente del gruppo %s", nomeGruppo);
    }
    DataField campo = okGroup.getField(nomeCampo);
    if (campo == null || (campo.isEmpty()) && !campo.isAuto()) {
      //NB: se un campo è empty e non è un campo automatico, significa che l'ho definito
      //    nel sorgente ma nella query non esiste: non do exception ma un valore con segnalazione.
      return "?? " + nomeGruppo + "#" + nomeCampo + " non trovato ??";
    }

    campo.calcola();
    if (campo.isEmptyOrNull()) {
      return null;
    }

    return campo.getValue();
  }

  /**
   * Cerca il gruppo antenato col nome richiesto a partire dal gruppo passato
   * come primo parametro. <br/>
   * La ricerca è fatta in modo che vengono anche scanditi i gruppi di subreport
   * che includono l'eventuale subreport corrente, fino ad arrivare al main
   * report.
   * 
   * @param gruppo
   *          gruppo di partenza; se è già quello giusto, viene tornato lui
   * @param nomeGruppo
   *          nome gruppo richiesto
   * @return gruppo cercato oppure null se non trovato
   */
  public Group getAncestorGroup(Group gruppo, String nomeGruppo) {
    if (gruppo == null || nomeGruppo == null) {
      return null;
    }

    if (nomeGruppo.length() > 0) {
      Group foundGroup = gruppo;
      if ( !foundGroup.is(nomeGruppo)) {
        foundGroup = gruppo.getAncestorGroup(nomeGruppo);
      }
      if (foundGroup == null) {
        //non ho trovato il gruppo: potrei essere in un subreport
        //e quindi lo cerco in un subreport padre o nel main report
        GroupElement subreport = getEnclosingSubreport(this);
        if (subreport != null) {
          GroupElement parentGroup = getEnclosingGroup(subreport);
          return getAncestorGroup(parentGroup.getGroup(), nomeGruppo);
        }
      } else {
        return foundGroup;
      }
    }
    return null;
  }

  /**
   * Ritorna una lista di tutte le istanze del gruppo richiesto, comprendendo
   * anche i subreport racchiusi nell'elemento corrente
   * 
   * @param gruppo
   *          istanza di gruppo corrente
   * @param nomeGruppo
   *          nome del gruppo richiesto
   * @return lista delle istanze del gruppo nomeGruppo, discendenti del gruppo
   *         corrente
   */
  public List<Group> getDescendantGroups(Group gruppo, String nomeGruppo) {
    if (gruppo == null || nomeGruppo == null) {
      return null;
    }
    List<Group> groups = null;
    if (nomeGruppo.length() > 0) {
      groups = gruppo.getDescendantInstances(nomeGruppo);
      if (groups.size() == 0) {
        //non ho trovato il gruppo: cerco nei subreports
        List<GroupElement> subreports = getDescendantSubreports(this);
        if (subreports != null) {
          for (GroupElement subreport : subreports) {
            if (subreport.getGroup() != null) {
              groups.addAll(subreport.getGroup().getDescendantInstances(nomeGruppo));
            }
          }
        }
      }
    }
    return groups;
  }

  /**
   * Dato un qualsiasi elemento (tag) del report, ritorna l'elemento group
   * antenato più vicino che corrisponde ad un subreport.
   * 
   * @param elem
   *          tag di partenza
   * @return subreport più vicino oppure null se non trovato
   */
  private GroupElement getEnclosingSubreport(IReportElement elem) {
    if (elem == null) {
      return null;
    }
    IReportElement padre = elem.getParent();
    if (padre == null) {
      return null;
    }
    if (padre instanceof GroupElement) {
      GroupElement grElem = (GroupElement) padre;
      if (grElem.getSubreportName() != null) {
        return grElem;
      }
    }
    return getEnclosingSubreport(padre);
  }

  /**
   * Dato un qualsiasi elemento (tag) del report, ritorna l'elemento group
   * antenato più vicino. <br/>
   * A differenza della {@link #getEnclosingSubreport(IReportElement)}, il
   * gruppo trovato può anche non essere un subreport.
   * 
   * @param elem
   *          tag di partenza
   * @return tag group antenato più vicino oppure null se non trovato
   */
  public GroupElement getEnclosingGroup(IReportElement elem) {
    if (elem == null) {
      return null;
    }
    IReportElement padre = elem.getParent();
    if (padre == null) {
      return null;
    }
    if (padre instanceof GroupElement) {
      return (GroupElement) padre;
    }
    return getEnclosingGroup(padre);
  }

  //  /**
  //   * Dato un qualsiasi elemento (tag) del report, ritorna l'elemento group che
  //   * definisce un subreport con il nome dato, discendente dell'elemento dato. <br/>
  //   * 
  //   * @param nomeGruppo
  //   *          nome del subreport desiderato; se null, torna il primo subreport
  //   *          che trova
  //   * @param elem
  //   *          tag di partenza
  //   * @return tag GroupElement che è il subreport discendente più vicino oppure
  //   *         null se non trovato
  //   */
  //  private GroupElement getDescendantSubreport(IReportElement elem, String nomeGruppo) {
  //    if (elem == null) {
  //      return null;
  //    }
  //
  //    for (IReportNode node : elem.getChildren()) {
  //      if (node instanceof GroupElement) {
  //        GroupElement gr = (GroupElement) node;
  //        boolean bSame = nomeGruppo != null ? gr.getName().equals(nomeGruppo) : true;
  //        if (bSame && gr.getQueryAttribute() != null) {
  //          return gr;
  //        }
  //      }
  //      if (node.isElement()) {
  //        GroupElement found = getDescendantSubreport((IReportElement) node, nomeGruppo);
  //        if (found != null) {
  //          return found;
  //        }
  //      }
  //    }
  //    return null;
  //  }

  /**
   * Ritorna tutti i subreport racchiusi nell'elemento passato. Non si ferma ai
   * subreport di primo livello ma ricorsivamente naviga tutti gli elementi
   * figli dell'elemento passato e ritorna tutti i subreport trovati a qualsiasi
   * livello.
   * 
   * @param elem
   *          elemento di partenza che racchiude i subreport richiesti
   * @return lista dei tag group (GroupElement) che definiscono il subreport
   */
  private List<GroupElement> getDescendantSubreports(IReportElement elem) {
    if (elem == null) {
      return null;
    }

    List<GroupElement> sub = new LinkedList<GroupElement>();
    for (IReportNode node : elem.getChildren()) {
      if (node instanceof GroupElement) {
        GroupElement gr = (GroupElement) node;
        if (gr.getQueryAttribute() != null) {
          sub.add(gr);
        }
      }
      if (node.isElement()) {
        sub.addAll(getDescendantSubreports((IReportElement) node));
      }
    }
    return sub;
  }

  /**
   * Valuta l'identificatore rappresentato dal simbolo passato
   * 
   * @param symbol
   *          identificatore semplice
   * @return oggetto risultato della valutazione
   * @throws ValidateException 
   * @throws EvaluateException 
   * @throws EvaluateException
   *           nel caso sia impossibile la valutazione
   */
  private Object resolveConstant(Symbol symbol) throws ResolveException, ValidateException, EvaluateException {
    //e' un identificatore --> è una costante 
    return getStampa().resolveParameter(symbol);
  }

  private Integer getCurrentPage() throws ResolveException {
    try {
      int currentPageNumber = getStampa().getDocumento().getCurrentPageNumber();
      return new Integer(currentPageNumber);
    } catch (GenerateException e) {
      throw new ResolveException(this, e, "Errore imprevisto in getCurrentPage()");
    }
  }

  //  /**
  //   * Ritorna il valore formattato del campo di nome {@link #getName()}. Se il
  //   * campo non c'è, ritorna opportuna exception.
  //   * 
  //   * @param symbol
  //   *          simbolo corrispondente al parsing dell'attributo <b>value</b> di
  //   *          questo campo.
  //   * @throws EvaluateException
  //   *           nel caso il campo non si trovi nel gruppo
  //   */
  //  private Object getValoreCampo(Symbol symbol, Group gruppo) throws EvaluateException {
  //    //è un campo auto, si calcola da solo
  //    if (getName() == null) {
  //      throw new EvaluateException("Non riesco a valutare " + symbol.getText() + ": non hai specificato l'attributo name ("
  //          + this.toString() + ")");
  //    }
  //    DataField c = gruppo.getField(getName());
  //    if (c != null) {
  //      return c.format();
  //    }
  //
  //    throw new EvaluateException("Non riesco a valutare " + symbol.getText() + ": campo " + getName() + " non trovato.");
  //  }

  /**
   * Valuta la chiamata a methodo rappresentata dal simbolo passato. A seconda
   * del tag associato al simbolo, viene risolto il metodo in un modo o
   * nell'altro.
   * 
   * @param symbol
   *          chiamata a metodo parsata
   * @return oggetto risultato della valutazione
   * @throws EvaluateException
   *           nel caso sia impossibile la valutazione
   */
  protected Object resolveMethodCall(MethodCall symbol, Group gruppo) throws EvaluateException {
    try {
      if (symbol.getTag() == null || symbol.getTag().equals(TAG_VALUE)) {
        return getStampa().resolveValueUserCall(symbol.getClassRef(), symbol.getMethodRef(), gruppo, this);
      } else if (symbol.getTag().equals(TAG_BOOLEAN)) {
        String name = this instanceof FieldElement ? ((FieldElement) this).getName() : null;
        return getStampa().resolveBooleanUserCall(symbol.getClassRef(), symbol.getMethodRef(), gruppo, name, this);
      } else {
        throw new EvaluateException("Non riesco a determinare il tipo di chiamata per " + symbol.getText());
      }
    } catch (StampaException e) {
      throw new EvaluateException("Non riesco a risolvere la chiamata " + symbol.getText() + ": " + e.toString());
    }
  }

  /**
   * Ritorna una rappresentazione del tipo:
   * 
   * <pre>
   *   Elemento xxx, linea l, colonna c
   * </pre>
   * 
   * dove linea e colonna è la posizione del tag nel sorgente.
   */
  @Override
  public String toString() {
    String out = "Elemento " + getTagName();
    out += getNodeLocation();
    //    if (getLineNum() > 0) {
    //      out += ", linea " + getLineNum();
    //    }
    //    if (getColumnNum() > 0) {
    //      out += ", colonna " + getColumnNum();
    //    }
    return out;
  }

  //	/**
  //	 * @param p_visible the visible to set
  //	 */
  //	protected void setVisible(Boolean p_visible) {
  //		m_visible = p_visible;
  //	}

  /**
   * @return l'oggetto Stampa salvato in precedenza con la
   *         {@link #salvaStampaGruppo(Stampa, Group)}
   */
  public Stampa getStampa() {
    return c_stampa;
  }

  public GroupModel getGroupModel() {
    return c_model;
  }

  /**
   * Imposta il modello di questo gruppo.
   */
  public void setGroupModel(GroupModel gruppo) {
    c_model = gruppo;
  }

  protected HAlign identificaHAlign(String value) throws ValidateException {
    try {
      if (value == null) {
        return null;
      }
      HAlign align = HAlign.valueOf(value.toUpperCase());
      return align;
    } catch (Exception e) {
      throw new ValidateException(this, e, "Errore nell'attributo halign, valore non previsto (%s)", value);
    }
  }

  protected VAlign identificaVAlign(String value) throws ValidateException {
    try {
      if (value == null) {
        return null;
      }
      VAlign align = VAlign.valueOf(value.toUpperCase());
      return align;
    } catch (Exception e) {
      throw new ValidateException(this, e, "Errore nell'attributo valign, valore non previsto (%s)", value);
    }
  }

  public VAlign getValign() {
    return c_valign;
  }

  public HAlign getHalign() {
    return c_halign;
  }

  public void setHalign(HAlign hAlign) {
    c_halign = hAlign;
  }

  /**
   * Attributo standard "refFont". <tt>refFont</tt> può essere definito su ogni
   * elemento del body di una stampa e se non viene definito viene recuperato in
   * cascata dagli elementi antenati
   */
  public String getRefFontName() {
    return getAttributeText(ATTRIB_REFFONT);
  }

  /**
   * Attributo standard "refFont". <tt>refFont</tt> può essere definito su ogni
   * elemento del body di una stampa e se non viene definito viene recuperato in
   * cascata dagli elementi antenati.
   */
  public void setRefFontName(String refFontName) {
    try {
      Attributo a = m_attributi.get(ATTRIB_REFFONT);
      if (a != null) {
        a.setValue(refFontName);
      }
    } catch (ValidateException e) {
      //intenzionalmente ignorato
    }
  }

  public String getVisible() {
    return getAttributeText(ATTRIB_VISIBLE);
  }

  @Override
  public String getXMLAttrs() {
    StringBuilder szXMLAttrs = new StringBuilder();
    for (Attributo a : m_attributi.values()) {
      if (a.exists()) {
        szXMLAttrs.append(" " + a.getNome() + "=\"" + a.getText() + "\"");
      }
    }
    return szXMLAttrs.toString();
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
    return "</" + getTagName() + ">";
  }

  /**
   * @return l'oggetto {@link Group} salvato in precedenza con la
   *         {@link #salvaStampaGruppo(Stampa, Group)} oppure quello impostato
   *         con la {@link #setGroup(Group)}. <br/>
   *         Se questo elemento è dentro il gruppo <tt>'pippo'</tt> (cioè
   *         'pippo' è il nome del gruppo più vicino a questo elemento),
   *         getGroup() ritorna sempre una istanza del Group di nome 'pippo'.
   *         Infatti questo elemento è generato <em>n</em> volte, una per ogni
   *         istanza del gruppo 'pippo', quindi getGroup() ritorna la i.esima
   *         istanza di tale gruppo.
   */
  public Group getGroup() {
    return c_dataGroup;
  }

  /**
   * Imposta il gruppo di dati corrente per questo elemento.
   * 
   * @param p_group
   *          the c_dataGroup to set
   */
  public void setGroup(Group p_group) {
    c_dataGroup = p_group;
  }

  /**
   * Aggiunge tutti i campi riferiti negli attributi in cui è possibile
   * specificarli (attualmente <tt>visible</tt> e <tt>value</tt> ). In questo
   * modo non si obbliga il creatore del report ad aggiungerli esplicitamente. <br/>
   * Questo metodo deve venire chiamato dopo la fase di parsing e validazione
   * per mettere a posto i gruppi prima della generazione vera e propria.
   * 
   * @param gruppo
   *          è il gruppo corrente a partire dal quale si valuta l'appartenenza
   *          del campo. Inizialmente è il gruppo radice.
   * @throws ValidateException
   */
  public void addAllFields(GroupModel gruppo) {
    if (this instanceof FieldElement) {
      FieldElement f = (FieldElement) this;
      addAllFields(f.getExpressionTree(), gruppo);
    } else if (this instanceof ChartElement) {
      ChartElement chart = (ChartElement) this;
      addAllFields(chart.getCategoryTree(), gruppo);
      addAllFields(chart.getValueTree(), gruppo);
      addAllFields(chart.getSerieTree(), gruppo);
    } else if (this instanceof IfElement) {
      IfElement ifElem = (IfElement) this;
      addAllFields(ifElem.getTestSymbol(), gruppo);
    } else if (this instanceof ImageElement) {
      ImageElement imageElem = (ImageElement) this;
      addAllFields(imageElem.getSrcSymbol(), gruppo);
    }
    if (this instanceof GroupElement) {
      GroupElement g = (GroupElement) this;
      if (g.getFilterSymbol() != null)
        addAllFields(g.getFilterSymbol(), gruppo);
    }
    if (getAttrSymbol(ATTRIB_VISIBLE) != null) {
      addAllFields(getAttrSymbol(ATTRIB_VISIBLE), gruppo);
    }
  }

  /**
   * Naviga l'albero sintattico di cui è passata la radice, e aggiunge tutti i
   * Field che incontra. Se un Field c'è già non è errore, perchè è già stato
   * aggiunto da un altro campo e quindi va bene.
   * 
   * @param symbol
   *          radice dell'albero sintattico dell'espressione
   * @param gruppo
   *          gruppo a cui aggiungere il campo o da cui cercare il gruppo
   *          antenato a cui aggiungere il campo
   * @throws GenerateException
   *           nel caso il nome di gruppo sia errato
   */
  private void addAllFields(Symbol symbol, GroupModel gruppo) {
    if (symbol == null) {
      return;
    }

    if (symbol.isField()) {
      addField((Field) symbol, gruppo);
    } else if (symbol.isFunction()) {
      Function fun = (Function) symbol;
      if (fun.is(FUNZ_EXIST)) {
        // TODO perchè questo? fare un commento...
        return;
      }

      //      for (Symbol arg : fun.getArguments()) {
      //        addAllFields(arg, gruppo);
      //      }
    }

    for (Symbol arg : symbol.getChildren()) {
      addAllFields(arg, gruppo);
    }

    //    else if (symbol.isOperation()) {
    //      MathBinaryExpr oper = (MathBinaryExpr) symbol;
    //      for (Symbol arg : oper.getChildren()) {
    //        addAllFields(arg, gruppo);
    //      }
    //    } else if (symbol instanceof BoolExpr) {
    //      if (symbol instanceof BoolNotExpr) {
    //        addAllFields( ((BoolNotExpr) symbol).getTerm(), gruppo);
    //      } else if (symbol instanceof BoolBinaryExpr) {
    //        for (Symbol arg : ((BoolBinaryExpr) symbol).getOperands()) {
    //          addAllFields(arg, gruppo);
    //        }
    //      }
    //    } else if (symbol instanceof TextExpr) {
    //      TextExpr textExpr = (TextExpr) symbol;
    //      for (Symbol child : textExpr.getChilds()) {
    //        if (child instanceof Field) {
    //          addField((Field) child, gruppo);
    //        }
    //      }
    //    }
  }

  /**
   * Aggiunge il Field al gruppo passato o ad un suo antenato
   * 
   * @param f
   *          Field da aggiungere
   * @param gruppo
   *          gruppo corrente a cui aggiungere il campo; se il Field passato si
   *          riferisce ad un altro gruppo assumo che sia un suo antenato, lo
   *          cerco e gli aggiungo il Field.
   * 
   * @throws GenerateException
   *           nel caso il gruppo non esista
   */
  private void addField(Field f, GroupModel gruppo) {
    if (f.getGroup() == null) {
      gruppo.addFieldSafe(f.getField());
    } else {
      GroupModel gruppoSuo = gruppo.getAncestorGroup(f.getGroup());
      if (gruppoSuo == null) {
        //				throw new ValidateException("Non ho trovato nessun gruppo di nome '" 
        //						+ f.getGroup() + "' antenato del gruppo " + gruppo.getNome());

        //non ho trovato il gruppo antenato, provo il discendente
        gruppoSuo = gruppo.getDescendantModel(f.getGroup());
      }
      if (gruppoSuo != null && f.getField() != null) {
        try {
          gruppoSuo.addField(f.getField());
        } catch (Exception e) {
          //se c'è già, va bene così...
        }
      }
    }
  }

  /**
   * Ritorna il nome del tag di questo elemento
   */
  public abstract String getTagName();

  /**
   * Indica se questo elemento è concreto, se cioè corrisponde direttamente ad
   * un contenuto che va a finire sul documento di output. <br/>
   * Ad esempio i tag <tt>text</tt> e <tt>table</tt> sono concreti, mentre i tag
   * <tt>if</tt> e <tt>group</tt> no.
   * 
   * @return true sse questo elemento è concreto
   */
  public abstract boolean isConcreteElement();

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#getChild(int)
   */
  @Override
  public IReportNode getChild(int index) {
    if (c_elementiFigli == null)
      return null;

    if (index < 0 || index >= c_elementiFigli.size())
      return null;

    return c_elementiFigli.get(index);
  }

  public boolean removeChild(IReportNode child) {
    if (c_elementiFigli == null)
      return false;

    return c_elementiFigli.remove(child);
  }

  /**
   * Sostituisce il figlio oldChild con il simbolo newChild. Se oldChild non
   * viene trovato, il metodo non fa nulla. Questo metodo setta anche il parent
   * di newChild nel caso la sostituzione venga fatta.
   * 
   * @param oldChild
   *          figlio da rimpiazzare
   * @param newChild
   *          figlio da sostituire al posto di oldChild
   * 
   * @return oldChild se è effettivamente figlio di questo nodo, altrimenti null
   */
  public IReportNode replaceChild(IReportNode oldChild, IReportNode newChild) {
    for (int i = 0; i < c_elementiFigli.size(); i++) {
      if (c_elementiFigli.get(i) == oldChild) {
        c_elementiFigli.set(i, newChild);
        newChild.setParent(this);
        return oldChild;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see
   * ciscoop.stampa.source.IReportElement#indexOf(ciscoop.stampa.source.IReportNode
   * )
   */
  @Override
  public int indexOf(IReportNode node) {
    if (c_elementiFigli == null)
      return -1;

    return c_elementiFigli.indexOf(node);
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#getChildCount()
   */
  @Override
  public int getChildCount() {
    if (c_elementiFigli == null)
      return 0;
    return c_elementiFigli.size();
  }

  public void setStampa(Stampa stampa) {
    c_stampa = stampa;
  }

  @Override
  public void destroy() {
    super.destroy();
    c_valign = null;
    c_halign = null;
    c_stampa = null;
    c_model = null;
    c_dataGroup = null;

    if (c_qualifiedGroups != null) {
      c_qualifiedGroups.clear();
      c_qualifiedGroups = null;
    }
    c_qualifiedCurrent = null;

    if (c_elementiFigli != null) {
      for (IReportNode f : c_elementiFigli) {
        if (f instanceof Destroyable) {
          ((Destroyable) f).destroy();
        }
      }
      c_elementiFigli.clear();
      c_elementiFigli = null;
    }
  }

  /**
   * Ritorna il primo figlio di questo nodo che è un {@link TextNode} o un  {@link ChunkElement}.
   * @return primo figlio text/chunk oppure null se non trovato
   */
  public AbstractNode getFirstChunknode() {
    AbstractNode n = null;
    for (int k = 0; k < c_elementiFigli.size(); k++) {
      IReportNode node = c_elementiFigli.get(k);
      if (node instanceof TextNode || node instanceof ChunkElement) {
        n = (AbstractNode)node;
      }
      else if (node instanceof AbstractElement && ((AbstractElement)node).canChildren()) {
        n = ((AbstractElement)node).getFirstChunknode();
      }
      if (n != null)
        break;
    }    
    return n;
  }
  
  /**
   * Ritorna il primo elemento di tipo chunk (span o field) oppure text che appare successivamente
   * nel sorgente dopo l'elemento passato
   * @param child elemento di riferimento
   * @return primo elemento chunk/text dopo child, oppure null se non trovato
   */
  public AbstractNode getNextChunknode(AbstractElement child) {
    int i = indexOf(child);
    if (i == -1)
      return null;
    AbstractNode n = null;
    for (int k = i+1; k < c_elementiFigli.size(); k++) {
      IReportNode node = c_elementiFigli.get(k);
      if (node instanceof TextNode) {
        n = (TextNode)node;
      } else if (node instanceof ChunkElement) {
        n = (ChunkElement)node;        
      }
      else if (node instanceof AbstractElement && ((AbstractElement)node).canChildren()) {
        n = ((AbstractElement)node).getFirstChunknode();
      }
      if (n != null)
        break;
    }
    if (n == null && getParent() != null)
      n = ((AbstractElement)getParent()).getNextChunknode(this);
    return n;
  }
  
}
