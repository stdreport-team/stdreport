package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.Destroyable;
import org.xreports.engine.validation.ValidateException;

/**
 * Rappresenta un qualsiasi nodo del report XML significativo per il sistema. <br>
 * Può essere sia un nodo di tipo elemento ( {@link TextElement},
 * {@link GroupElement},...) che un nodo di tipo testo ( {@link TextNode} ). La
 * classe che genera tali elementi è {@link XMLSchemaValidationHandler}. Ogni
 * elemento ha un parent, accessibile tramite {@link #getParent()}; il nodo
 * radice della gerarchia ha {@link #getParent()}==null ed è accessibile tramite
 * {@link Stampa#getElemRadice()}. <br>
 * Ogni nodo possiede una font di riferimento che viene applicata definendo
 * l'attributo refFont nell
 * 
 */
public abstract class AbstractNode implements IReportNode, Destroyable {
  /**
   * Il padre di questo nodo. Ogni nodo possiede un padre a parte il nodo radice
   */
  protected IReportElement c_elemParent  = null;
  /** Specifica se siamo in modalità di debug dei dati */
  private boolean          c_debugData   = false;

  /** posizione del tag nel sorgente */
  private int              m_lineNum     = 0;
  private int              m_columnNum   = 0;
  private String           m_includeFile = null;

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
  public AbstractNode(Attributes attrs, int lineNum, int colNum) {
    setPosition(lineNum, colNum);
  }

  public AbstractNode(String testo, int lineNum, int colNum) {
    setPosition(lineNum, colNum);
  }

  /**
   * Torna il valore dell'attributo col nome passato dall'elenco degli attributi
   * passato.
   * 
   * @param szNomeAttributo
   *          nome attributo da leggere
   * @param attrs
   *          elenco attributi
   * @return valore attributo oppure null se non esiste
   */
  protected String leggiAttributo(String szNomeAttributo, Attributes attrs) {
    return attrs.getValue(szNomeAttributo);
  }

  /**
   * Legge l'attributo col nome passato e lo converte in oggetto {@link Measure}
   * . Se l'attributo non esiste viene tornato un oggetto Measure costruito con
   * il valore di default passato.
   * 
   * @param szNomeAttributo
   *          nome dell'attributo da leggere
   * @param percent
   *          true/false: indica se sono ammesse misure percentuali
   * @param lines
   *          true/false: indica se sono ammesse misure in linee
   * @param attrs
   *          collection degli attributi da cui leggere
   * @param defaultMeasure
   *          valore di default con cui costruire l'oggetto Measure nel caso
   *          l'attributo non esista; se questo valore è null e l'attributo non
   *          esiste, viene tornato null.
   * @return oggetto Measure corrispondente all'attributo richiesto
   * @throws ValidateException
   *           in caso la specifica della misura sia sintatticamente errata
   */
  protected Measure leggiMeasure(String szNomeAttributo, boolean percent, boolean lines, Attributes attrs, String defaultMeasure)
      throws ValidateException {
    String attr = leggiAttributo(szNomeAttributo, attrs);
    Measure m = null;
    if (attr != null) {
      m = new Measure(attr, lines, percent);
    } else {
      if (defaultMeasure != null) {
        m = new Measure(defaultMeasure);
      }
    }

    return m;

  }

  /**
   * Legge l'attributo col nome passato e lo converte in oggetto {@link Measure}
   * . Se l'attributo non esiste viene tornato un oggetto Measure costruito con
   * il valore di default passato. Non sono ammesse misure percentuali nè di
   * linee.
   * 
   * @param szNomeAttributo
   *          nome dell'attributo da leggere
   * @param attrs
   *          collection degli attributi da cui leggere
   * @param defaultMeasure
   *          valore di default con cui costruire l'oggetto Measure nel caso
   *          l'attributo non esista; se questo valore è null e l'attributo non
   *          esiste, viene tornato null.
   * @return oggetto Measure corrispondente all'attributo richiesto
   * @throws ValidateException
   *           in caso la specifica della misura sia sintatticamente errata
   */
  protected Measure leggiMeasure(String szNomeAttributo, Attributes attrs, String defaultMeasure) throws ValidateException {
    return leggiMeasure(szNomeAttributo, false, false, attrs, defaultMeasure);
  }

  /**
   * Legge l'attributo col nome passato e lo converte in oggetto {@link Measure}
   * . Se l'attributo non esiste viene tornato un oggetto Measure costruito con
   * il valore di default passato. Non sono ammesse misure di linee.
   * 
   * @param szNomeAttributo
   *          nome dell'attributo da leggere
   * @param percent
   *          true/false: indica se sono ammesse misure percentuali
   * @param attrs
   *          collection degli attributi da cui leggere
   * @param defaultMeasure
   *          valore di default con cui costruire l'oggetto Measure nel caso
   *          l'attributo non esista; se questo valore è null e l'attributo non
   *          esiste, viene tornato null.
   * @return oggetto Measure corrispondente all'attributo richiesto
   * @throws ValidateException
   *           in caso la specifica della misura sia sintatticamente errata
   */
  protected Measure leggiMeasure(String szNomeAttributo, boolean percent, Attributes attrs, String defaultMeasure)
      throws ValidateException {
    return leggiMeasure(szNomeAttributo, percent, false, attrs, defaultMeasure);
  }

  /**
   * Torna il valore dell'attributo col nome passato dall'elenco degli attributi
   * passato. Se l'attributo non esiste, torna il valore di default.
   * 
   * @param szNomeAttributo
   *          nome attributo da leggere
   * @param attrs
   *          elenco attributi
   * @param defValue
   *          valore tornato in caso di attributo inesistente
   */
  protected String leggiAttributo(String szNomeAttributo, Attributes attrs, String defValue) {
    String out = attrs.getValue(szNomeAttributo);
    if (out == null) {
      out = defValue;
    }
    return out;
  }

  @Override
  public void setParent(IReportElement elemParent) {
    c_elemParent = elemParent;
  }

  @Override
  public IReportElement getParent() {
    return c_elemParent;
  }

  @Override
  public IReportElement closest(Class<? extends IReportElement> classe) {
    if (c_elemParent == null || c_elemParent.getClass().equals(classe)) {
      return c_elemParent;
    }
    return c_elemParent.closest(classe);
  }

  @Override
  public boolean isElement() {
    return false;
  }

  @Override
  public void setDebugData(boolean debugData) {
    c_debugData = debugData;
  }

  @Override
  public boolean isDebugData() {
    return c_debugData;
  }

  @Override
  public String toXML() {
    return null;
  }

  /**
   * Imposta la posizione del tag nel sorgente XML. Util per messaggi
   * informativi e di errore
   * 
   * @param line
   *          numero riga in cui compare il tag
   * @param column
   *          numero colonna in cui compare il tag
   */
  protected void setPosition(int line, int column) {
    m_lineNum = line;
    m_columnNum = column;
  }

  /**
   * Numero di colonna in cui è dichiarato questo tag
   */
  public int getColumnNum() {
    return m_columnNum;
  }

  /**
   * Numero di linea in cui è dichiarato questo tag
   */
  public int getLineNum() {
    return m_lineNum;
  }

  /**
   * Ritorna una descrizione testuale della posizione riga/colonna del tag nel
   * sorgente XML.
   * 
   * @return posizione tag in forma "riga,colonna"
   */
  public String getNodeLocation() {
    if (m_lineNum != 0) {
      //return " posizione " + m_lineNum + "," + m_columnNum;
      String out = " (linea " + m_lineNum;
      if (m_includeFile != null) {
        out += ", file '" + m_includeFile + "'";
      }
      out += ") ";
      return out;
    }

    return "";
  }

  /**
   * Ritorna il file a cui appartiene questo nodo, se proviene da un file
   * incluso con il tag <tt>&lt;include&gt;</tt>. Se questo tag non appartiene
   * ad alcun file incluso, ritorna null.
   */
  public String getIncludeFile() {
    return m_includeFile;
  }

  /**
   * Imposta il file di appartenenza di questo nodo.
   * 
   * @param mIncludeFile
   *          path completo del file
   */
  public void setIncludeFile(String mIncludeFile) {
    m_includeFile = mIncludeFile;
  }

  /**
   * Ritorna il nodo fratello di questo e *immediatamente* precedente.
   * 
   * @return nodo fratello *immediatamente* precedente; se è il primo figlio o
   *         non ha fratelli, torna null.
   */
  public AbstractNode getPreviousSibling() {
    int i = getParent().indexOf(this);
    if (i <= 0)
      return null;
    return (AbstractNode) (getParent().getChild(i - 1));
  }

  /**
   * Indica se l'elemento fa parte del contenuto della pagina e incide sul
   * flusso del testo.
   * <p>
   * Gli elementi quali <tt>text</tt>, <tt>field</tt>, <tt>table</tt> sono
   * content-element, mentre elementi quali <tt>marginbox</tt>,
   * <tt>pageHeader</tt>, <tt>pageFooter</tt>,... no.
   * </p>
   * 
   * @return true se l'elemento fa parte del contenuto della pagina e incide sul
   *         flusso del testo.
   */
  public abstract boolean isContentElement();

  /**
   * Indica se questo elemento è un block level element o meno.
   * <ul>
   * <li><b>block element</b>: elemento che va scritto a capo: gli elementi
   * prima e dopo sono su righe diverse (esempi: <tt>text</tt>, <tt>table</tt>
   * ,...).
   * 
   * <li><b>inline element</b>: elemento inline: gli elementi prima e dopo sono
   * contigui a questo (esempi: <tt>field</tt>, <tt>image</tt>, blocco
   * testo,...).
   * </ul>
   * 
   * @return true se questo elemento è un block level element
   */
  public abstract boolean isBlockElement();

  /**
   * Cerca l'antenato più vicino definito come "concreto", cioè che ha
   * {@link #isConcreteElement()}=true. Anche il padre è compreso nella ricerca.
   * 
   * @return antenato concreto più vicino oppure null se non trovato
   */
  public AbstractElement getConcreteParent() {
    if (getParent() == null)
      return null;

    AbstractElement parent = (AbstractElement) getParent();
    while ( !parent.isConcreteElement()) {
      parent = (AbstractElement) parent.getParent();
      if (parent == null)
        break;
    }
    return parent;
  }

  @Override
  public void destroy() {
    c_elemParent = null;
    m_includeFile = null;
  }
}
