package org.xreports.engine;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;

import org.xreports.Destroyable;
import org.xreports.datagroup.DataField;
import org.xreports.datagroup.DataFieldModel;
import org.xreports.datagroup.Group;
import org.xreports.datagroup.GroupList;
import org.xreports.datagroup.GroupModel;
import org.xreports.datagroup.RootGroup;
import org.xreports.datagroup.RootModel;
import org.xreports.dmc.SimpleList;
import org.xreports.errors.MessHandler;
import org.xreports.exceptions.CISException;
import org.xreports.exceptions.PDCException;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.parsers.GenericParser;
import org.xreports.expressions.parsers.GenericParser.ParserType;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Evaluator;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.jbb.JBB;
import org.xreports.pdc.Attributi;
import org.xreports.pdc.Attributo;
import org.xreports.pdc.Lista;
import org.xreports.pdc.PDC;
import org.xreports.pdc.PDCStatus;
import org.xreports.pdc.Relazione;
import org.xreports.pdc.Relazione1_1;
import org.xreports.pdc.Relazione1_N;
import org.xreports.stampa.engine.MainReportInfo;
import org.xreports.stampa.engine.Parameter;
import org.xreports.stampa.engine.ReportInfo;
import org.xreports.stampa.engine.TabbedFileReader;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.output.Documento;
import org.xreports.stampa.output.FactoryCreator;
import org.xreports.stampa.output.FactoryElementi;
import org.xreports.stampa.output.Rettangolo;
import org.xreports.stampa.output.StileCarattere;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.source.AbstractElement;
import org.xreports.stampa.source.FieldElement;
import org.xreports.stampa.source.GroupElement;
import org.xreports.stampa.source.MarginboxElement;
import org.xreports.stampa.source.Margini;
import org.xreports.stampa.source.Measure;
import org.xreports.stampa.source.PageFooter;
import org.xreports.stampa.source.PageHeader;
import org.xreports.stampa.source.RulersElement;
import org.xreports.stampa.source.TextNode;
import org.xreports.stampa.source.WatermarkElement;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLIncludeValidationHandler;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;
import org.xreports.stampa.validation.XMLSchemaValidationHandler.DocumentAttributes;
import org.xreports.stampa.validation.XMLutil;
import org.xreports.util.CharsetToolkit;
import org.xreports.util.FileUtil;
import org.xreports.util.Text;

public class Stampa implements Evaluator, Destroyable {
  private Logger             c_logger;

  /**
   * path di base del namespace di stdReport: le versioni successive alla 1,
   * aggiungono all'url il numero di versione
   */
  public static final String NAMESPACE_BASE             = "http://www.ciscoop.sm/stampa";              //$NON-NLS-1$

  /** character set UTF-8; costante per la {@link #setSourceEncoding(String)} */
  public static final String CHARSET_UTF8               = "UTF-8";                                     //$NON-NLS-1$
  /**
   * character set windows 1252; costante per la
   * {@link #setSourceEncoding(String)}
   */
  public static final String CHARSET_CP1252             = "windows-1252";                              //$NON-NLS-1$
  /**
   * character set ISO Latin 1 (ISO-8859-1); costante per la
   * {@link #setSourceEncoding(String)}
   */
  public static final String CHARSET_ISOLatin1          = "ISO-8859-1";                                //$NON-NLS-1$

  /** Quantità massima predefinita di record da caricare per generare il report */
  public static final int    DEFAULT_MAX_NUMRECORDS     = 10000;

  public static final String DEFAULT_NODATA_MESSAGE     = "Non ci sono dati, il report risulta vuoto."; //$NON-NLS-1$

  /** versione corrente del motore di stampa */
  public static final String VERSION                    = "2.0";                                       //$NON-NLS-1$

  /** è il numero di versione più alto del XML Schema correntemente supportato */
  public static final int    MAX_XMLSCHEMA_VERSION      = 2;

  /**
   * prefisso che hanno tutti gli inner parameters
   */
  public static final String INNERPARAM_PREFIX          = "report.";                                   //$NON-NLS-1$

  /**
   * nome del parametro <b>stampa.version</b> che indica la versione corrente
   * del motore di stampa
   */
  public static final String INNERPARAM_VERSION         = INNERPARAM_PREFIX + "version";               //$NON-NLS-1$

  /**
   * nome del parametro <b>stampa.source.fullpath</b> che indica il path
   * completo del file sorgente corrente
   */
  public static final String INNERPARAM_SOURCE_FULLPATH = INNERPARAM_PREFIX + "source.fullpath";       //$NON-NLS-1$
  /**
   * nome del parametro <b>stampa.source.name</b> che indica il solo nome del
   * file sorgente corrente
   */
  public static final String INNERPARAM_SOURCE_NAME     = INNERPARAM_PREFIX + "source.name";           //$NON-NLS-1$

  /** Stati di generazione della stampa */
  public enum GenerationStatus {
    START_PDFGEN, MET_NEWPAGE, GOTO_NEXTPAGE, CONTINUE_PDFGEN, END_PDFGEN;
  }

  /**
   * I permessi supportati sul documento
   */
  public enum DocumentPermission {
    /** permesso di stampare */
    PRINT,
    /** permesso di copiare contenuto */
    COPY,
    /** permesso di modificare contenuto */
    MODIFY;
  }

  //Variabili globali per la VALIDAZIONE
  /** File .xml con il sorgente del report */
  private File                             c_fileXMLSource             = null;
  /** File .xml con il sorgente del report con gli elementi 'include' elaborati */
  private File                             c_fileXMLSourceWithIncludes = null;

  /** Input Stream con il sorgente del report */
  private InputStream                      c_streamXMLSource           = null;
  /** nome file .xsd per la validazione xml-schema */
  private String                           c_fileXMLSchema             = null;
  /** versione del xml-schema usato per la validazione */
  private Integer                          c_XMLSchemaVersion          = null;
  /** nome file .xsd per la validazione xml-schema */
  private boolean                          c_XMLSchemaDevelopment      = false;
  /** Directory di output per il risultato della validazione */
  private File                             c_outputDir                 = null;
  /** Directory temporanea per eventuale deposito file temporanei */
  private File                             c_tempDir                   = null;

  // ----- gestione NoDataException -----
  private boolean                          c_ShowMessageWithNoData     = false;
  private String                           c_NoDataMessage             = DEFAULT_NODATA_MESSAGE;
  private boolean                          c_NoData                    = false;

  // ----- proprietà del documento -----
  private String                           c_documentTitle;
  private String                           c_documentSubject;
  private String                           c_documentAuthor;
  private Symbol                           c_documentTitle_symbol;
  private Symbol                           c_documentSubject_symbol;
  private Symbol                           c_documentAuthor_symbol;
  private boolean                          c_documentBookmarksOpened;

  //Variabili globali per la GENERAZIONE
  /** Directory dove andare a cercare le risorse (font, immagini,...) */
  private File                             c_resourcesDir              = null;
  /** Nome del file di output */
  private String                           m_outputFileName            = null;
  /** Nome del file di output */
  private boolean                          m_autoOutputFileName        = true;

  /** OutputStream contenente il risultato della generazione */
  private ByteArrayOutputStream            c_baOutputStream            = null;

  /**
   * Mappa delle costanti definite tramite la
   * {@link #addParameter(String, Object)}
   */
  private Map<String, Parameter>           m_parameters                = new HashMap<String, Parameter>();
  /**
   * Mappa delle costanti di sistema, in sola lettura
   */
  private Map<String, Object>              m_sysParameters             = new HashMap<String, Object>();
  /** Mappa delle user classes; key=name, value=nome completo della classe */
  private Map<String, String>              m_userClasses               = new HashMap<String, String>();
  /** Mappa delle istanze delle user classes; key=name, value=istanza creata */
  private Map<String, Object>              m_userInstances             = new HashMap<String, Object>();
  /** Mappa dei possibili Colori da utilizzare nella stampa */
  private Map<String, Colore>              c_mappaColori               = new HashMap<String, Colore>();
  /** Mappa dei colori builtin */
  private Map<String, Colore>              c_builtinColors             = new HashMap<String, Colore>();
  /** Mappa dei possibili Font da utilizzare nella stampa */
  private Map<String, StileCarattere>      c_mappaFont                 = new HashMap<String, StileCarattere>();
  /** Mappa informazioni di quando vengono inclusi altri file */
  private Map<Integer, IncludeInfo>        c_mappaIncludes             = new HashMap<Integer, IncludeInfo>();

  /** Variabili di default del documento */
  private StileCarattere                   c_defaultFont;
  private Documento                        c_documento;
  private Margini                          c_docMargini;
  private Rettangolo                       c_docRettangolo;
  private byte[]                           c_docPassword;
  private Map<DocumentPermission, Boolean> c_docPermissions;
  private GenerationStatus                 c_pdfGenStatus              = GenerationStatus.START_PDFGEN;

  private PageHeader                       c_pageHeader                = null;
  private PageFooter                       c_pageFooter                = null;

  private SimpleDateFormat                 m_dtlogger;
  //private SimpleDateFormat                 m_filedate;

  private int                              c_maxNumRecords             = DEFAULT_MAX_NUMRECORDS;

  /** Nome del motore da usare per generare il file di output */
  private String                           c_factoryName               = null;
  private FactoryElementi                  c_factoryElementi           = null;

  /** query per la lettura dei dati ({@link #setDataQuery(String)} */
  private String                           c_szQuery                   = null;
  /** dati forniti direttamente ({@link #setDataList(List)} */
  private List<HashMap<String, Object>>    c_datalist                  = null;
  /**
   * nome del file a campi delimitati che contiene i dati (
   * {@link #setDataInputFile(String)}
   */
  private String                           c_dataInputFile             = null;
  /**
   * dati forniti con una lista ({@link #setDataSimpleList(SimpleList)} /
   * {@link #setDataFullList(Lista)} )
   */
  private SimpleList                       c_simplelist                = null;
  /** database in uso per tutti gli accessi ai dati */
  private JBB                              c_database                  = null;

  //  private Deque<ReportInfo>                c_stackReports              = new ArrayDeque<ReportInfo>();
  //  private Map<String, ReportInfo>          c_subReports                = new HashMap<String, ReportInfo>();
  private MainReportInfo                   c_mainReport;
  private RootModel                        c_rootModel;

  private String                           c_sourceEncoding            = null;

  /** Variabili utilizzate per la gestione del debug */
  private boolean                          c_debugMode                 = false;
  private boolean                          c_debugData                 = false;
  private File                             c_fileDebug                 = null;
  private BufferedWriter                   c_outfile                   = null;

  private MarginboxElement                 c_marginBox;
  private RulersElement                    c_rulers;
  private List<WatermarkElement>           c_watermarks;

  public Stampa() {
    init();
  }

  /**
   * Fase di inizializzazione dell'oggetto stampa
   * 
   */
  private void init() {
    m_dtlogger = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    //m_filedate = new SimpleDateFormat("yyyy-MM-dd");

    c_docPermissions = new HashMap<Stampa.DocumentPermission, Boolean>();

    c_baOutputStream = new ByteArrayOutputStream();

    setupDefaultFont();

    String tempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
    c_tempDir = new File(tempDir);
    initBuiltinColors();
    initSystemParameters();
    initParameters();

    c_watermarks = new ArrayList<WatermarkElement>();
  }

  private void setupDefaultFont() {
    c_mappaFont.clear();
    StileCarattere fontDiDefault = getDefaultFont();
    try {
      addStile(fontDiDefault);
    } catch (Exception e) {
      //qui non può avvenire l'eccezione perchè la mappa è vuota!
    }
  }

  /**
   * Permette di riportare l'oggetto allo stato iniziale che si ha dopo
   * l'istanziazione pulendolo da tutto quanto fatto in una precedente creazione
   * di report. In questo modo si può riutilizzare per stampare un altro report
   * ed evitare la fase di inizializzazione.
   * 
   * @throws StampaException
   *           nel caso l'ggetto sia già stato distrutto usando la
   *           {@link #destroy()}
   * 
   */
  public void reset() throws StampaException {
    checkDestroyed();

    c_mainReport.destroy();
    c_mainReport = null;
    if (c_rootModel != null) {
      c_rootModel.destroy();
      c_rootModel = null;      
    }
    
    c_documento = null;    
    
    c_docPermissions.clear();
    c_watermarks.clear();

    c_baOutputStream = new ByteArrayOutputStream();
    setupDefaultFont();
    initParameters();

    setAutoOutputFileName(false);
    setDatabase(null);
    setDataFullList(null);
    setDataInputFile(null);
    setDataList(null);
    setDataQuery(null);
    setDataSimpleList(null);
    setDebugData(false);
    setDebugMode(false);
    setDocMargini(null);
    setDocSize(null);
    setDocumentAuthor(null);
    setDocumentPassword(null);
    setDocumentSubject(null);
    setDocumentTitle(null);
    setLogger(null);
    setMaxNumRecords(DEFAULT_MAX_NUMRECORDS);
    setNoDataMessage(DEFAULT_NODATA_MESSAGE);
    setOutputDir(null);
    setOutputEngine(null);
    setOutputFileName(null);

    c_fileXMLSource = null;
    c_streamXMLSource = null;

    setResourcesDir(null);
    setShowMessageWithNoData(false);
    setSourceEncoding(null);
  }

  private void checkDestroyed() throws PreprocessingException {
    if (m_parameters == null)
      throw new PreprocessingException(Msgs.get("Stampa.OBJ_DESTROYED")); //$NON-NLS-1$
  }

  private void initBuiltinColors() {
    c_builtinColors.clear();
    for (Colore col : Colore.getBuiltinColors()) {
      c_builtinColors.put(col.getName(), col);
    }
  }

  /**
   * Inizializza le costanti di sistema
   */
  private void initSystemParameters() {
    for (Object p : System.getProperties().keySet()) {
      m_sysParameters.put(p.toString().toLowerCase(), System.getProperty(p.toString()));
    }
    for (String envProp : System.getenv().keySet()) {
      if ( !m_sysParameters.containsKey(envProp)) {
        m_sysParameters.put(envProp.toLowerCase(), System.getenv(envProp));
      }
    }
  }

  private void initParameters() {
    try {
      m_parameters.clear();
      addInnerParameter(INNERPARAM_VERSION, VERSION);
    } catch (StampaException e) {
    }
  }

  /**
   * Metodo che esegue la validazione del sorgente del report tramite XML Schema
   * opportuno. <br>
   * Mentre esegue la validazione effettua anche il parsing degli elementi e ne
   * costruisce la struttura intermedia. Il parsing è fatto attraverso un nostro
   * handler, {@link XMLSchemaValidationHandler}, fornito al parser.
   * <p>
   * E' tale classe che costruisce la struttura degli elementi, che si può
   * navigare a partire dalla radice data dal metodo
   * {@link ReportInfo#getGroupElem()} chiamata sull'oggetto ritornato da
   * {@link #getMainReport()}.
   * </p>
   * 
   * @return l'oggetto usato per validare il sorgente; questo oggetto è poi
   *         usato internamente per leggere alcune proprietà determinate dal
   *         sorgente e necessarie per le successive fasi.
   */
  private XMLSchemaValidationHandler validate() throws ValidateException {
    //Dove salvare il risultato della validazione schematron ( che altro non è che il file xml "report.xml" )
    if (c_outputDir == null) {
      throw new ValidateException(Msgs.get("Stampa.OUTDIR_NOTSPEC")); //$NON-NLS-1$
    }
    if ( !c_outputDir.exists()) {
      throw new ValidateException(Msgs.get("Stampa.OUTDIR_NOTEXISTS"), c_outputDir.toString()); //$NON-NLS-1$
    }
    if ( !c_outputDir.isDirectory() || !c_outputDir.canRead()) {
      throw new ValidateException(Msgs.get("Stampa.OUTDIR_INVALID"), c_outputDir.toString());
    }

    XMLSchemaValidationHandler handler = new XMLSchemaValidationHandler(this);
    try {
      //Validazione xml-schema e caricamento struttura intermedia degli elementi
      InputStream XMLSchemaIS = getClass().getResourceAsStream(c_fileXMLSchema);
      XMLutil.saxParserValidation(new FileInputStream(c_fileXMLSourceWithIncludes), XMLSchemaIS, handler);
      //Operazioni da effettuare dopo la validazione
      postValidate(handler);
    } catch (ValidateException e) {
      throw e;
    } catch (SAXException sax) {
      if (sax.getException() instanceof ValidateException) {
        throw (ValidateException) (sax.getException());
      }
      throw new ValidateException(sax.getMessage(), sax);
    } catch (Exception e) {
      throw new ValidateException(e, "Errore inaspettato durante la validazione di %s", handler.getCurrentTagDescription()); //$NON-NLS-1$
    }
    return handler;
  }

  private class IncludeInfo {
    private String ii_pathName  = null;
    private int    ii_lineCount = 0;
    private int    ii_lineStart = 0;

    IncludeInfo(String path, int lineStart, int lineCount) {
      ii_lineCount = lineCount;
      ii_lineStart = lineStart;
      ii_pathName = path;
    }

    /**
     * @return qta linee del file incluso
     */
    public int getLinesCount() {
      return ii_lineCount;
    }

    /**
     * @return linea del file originale in cui è l'include
     */
    public int getStartLine() {
      return ii_lineStart;
    }

    /**
     * @return path completo del file incluso
     */
    public String getPathName() {
      return ii_pathName;
    }
  }

  public void addIncludeFile(String pathName, int lineStart, int lineCount) {
    IncludeInfo ii = new IncludeInfo(pathName, lineStart, lineCount);
    c_mappaIncludes.put(new Integer(lineStart), ii);
  }

  /**
   * Se la linea passata fa parte di un file incluso col tag <tt>include</tt> ne
   * ritorna l'oggetto IncludeInfo associato, altrimenti ritorna null
   * 
   * @param line
   *          numero di linea nel file risultante dall'aggiunta degli include
   * @return IncludeInfo se la linea fa parte di un include, null altrimenti
   */
  private IncludeInfo getIncludeInfoByLine(int line) {
    for (IncludeInfo ii : c_mappaIncludes.values()) {
      if (ii.getStartLine() <= line) {
        if (ii.getStartLine() + ii.getLinesCount() >= line) {
          return ii;
        }
      }
    }
    return null;
  }

  public String getIncludeFile(int line) {
    IncludeInfo include = getIncludeInfoByLine(line);
    if (include != null)
      return include.getPathName();

    return null;
  }

  public int getOriginalLineNumber(int line) {
    IncludeInfo include = getIncludeInfoByLine(line);
    if (include != null) {
      return line - include.getStartLine() + 1;
    }

    //cerco tutti gli include precedenti a line e sottraggo le
    //righe aggiunte da ogni include
    int righeAggiunte = 0;
    for (IncludeInfo ii : c_mappaIncludes.values()) {
      if (ii.getStartLine() < line) {
        righeAggiunte += ii.getLinesCount() - 1; //sottraggo 1 perchè la riga con il tag <include> originale la conto
      }
    }

    line -= righeAggiunte;

    return line;
  }

  /**
   * Operazioni da eseguire dopo la validazione
   * 
   * @throws ValidateException
   * @throws EvaluateException
   * @throws DataException
   */
  private void postValidate(XMLSchemaValidationHandler handler) throws ValidateException, EvaluateException, DataException {
    //    //Aggiungiamo in automatico tutti i campi cosi' che uno non debba aggiungerseli a mano
    //    for (ReportInfo report : c_subReports.values()) {
    //      report.postValidate();
    //    }

    if (getDataSourcesCount() == 0) {
      //solo se non mi hanno specificato una sorgente dati via codice,
      //assegno la query dal XML
      if (Text.isValue(handler.getQuery())) {
        GenericParser parser = GenericParser.getInstance(ParserType.TEXT_EXPRESSION, handler.getQuery());
        String text = null;
        try {
          Symbol root = parser.parse();
          text = String.valueOf(root.evaluate(new QueryEvaluator()));
        } catch (LexerException e) {
          throw new ValidateException(e, Msgs.get("Stampa.QUERY_LEXERROR")); //$NON-NLS-1$
        } catch (ParseException e) {
          throw new EvaluateException(e, Msgs.get("Stampa.QUERY_PARSEERROR")); //$NON-NLS-1$
        }
        if (text != null) {
          setDataQuery(text);
        }
      }
    }

    setMainReport(handler.getRootGroupElement(), getRootModel());

    getMainReport().postValidate();
    setDocumentProperties(handler);
    adjustHeaders(handler);
  }

  private void adjustHeaders(XMLSchemaValidationHandler handler) throws ValidateException {
    c_pageHeader = handler.getDocumentAttributes().getPageHeader();
    c_pageFooter = handler.getDocumentAttributes().getPageFooter();

    Measure header = getPageHeader() != null ? getPageHeader().getHeight() : null;
    Measure footer = getPageFooter() != null ? getPageFooter().getHeight() : null;
    //altezzaUtile = altezza foglio esclusi i margini
    float altezzaUtile = getDocSize().getHeight() - getDocMargini().getTop().getValue() - getDocMargini().getBottom().getValue();

    Margini docMargins = getDocMargini();
    float hPt = 0;
    float fPt = 0;
    //MODIFICA TOP MARGIN DEL DOCUMENTO
    if (header != null) {
      if (header.isPercent())
        hPt = (altezzaUtile * header.getValue()) / 100f;
      else
        hPt = header.getValue();
    }

    if (hPt > 0) {
      docMargins.setTop(new Measure(hPt + docMargins.getTop().getValue()));
    }

    //MODIFICA BOTTOM MARGIN DEL DOCUMENTO
    if (footer != null) {
      if (footer.isPercent())
        fPt = (altezzaUtile * footer.getValue()) / 100f;
      else
        fPt = footer.getValue();
    }
    if (fPt > 0) {
      docMargins.setBottom(new Measure(fPt + docMargins.getBottom().getValue()));
    }

    if (hPt + fPt > getDocSize().getHeight()) {
      AbstractElement elem = getPageHeader() != null ? getPageHeader() : getPageFooter();
      throw new ValidateException(elem, "La dimensione di pageHeader/Footer e' maggiore dell'altezza della intera pagina"); //$NON-NLS-1$
    }
  }

  private class QueryEvaluator implements Evaluator {

    @Override
    public Object evaluate(Symbol symbol) throws ResolveException {
      if (symbol.isConstant()) {
        try {
          return resolveParameter(symbol);
        } catch (Exception e) {
          return ""; //$NON-NLS-1$
        }
      }
      throw new ResolveException("Impossibile valutare il simbolo %s nel testo della query", symbol); //$NON-NLS-1$
    }

  }

  private void setDocumentProperties(XMLSchemaValidationHandler handler) throws EvaluateException, ValidateException {
    DocumentAttributes doc = handler.getDocumentAttributes();
    //controllo se l'elemento document è stato definito
    if (doc == null)
      return;

    c_rulers = doc.getRulers();
    c_marginBox = doc.getMarginBox();
    for (WatermarkElement we : doc.getWatermarks())
      c_watermarks.add(we);

    if (getDocMargini() == null) {
      setDocMargini(doc.getMargini());
    }
    if (getDocSize() == null) {
      setDocSize(doc.getDocSize());
    }
    if (getDocumentAuthor() == null) {
      setDocumentAuthor(doc.getAuthor());
    }
    if (getDocumentSubject() == null) {
      setDocumentSubject(doc.getSubject());
    }
    if (getDocumentTitle() == null) {
      setDocumentTitle(doc.getTitle());
    }

    if (doc.getBookmarksOpened() != null) {
      c_documentBookmarksOpened = doc.getBookmarksOpened().equalsIgnoreCase("true"); //$NON-NLS-1$
    }

    Map<DocumentPermission, Boolean> sourcePerms = doc.getPermissions();
    //ora qui imposto i permessi derivanti dal sorgente XML solo se non sono stati specificati via codice
    if (sourcePerms != null) {
      for (DocumentPermission sourcePerm : sourcePerms.keySet()) {
        Boolean codePermValue = getPermission(sourcePerm);
        if (codePermValue == null)
          addPermission(sourcePerm, sourcePerms.get(sourcePerm));
      }
    }
  }

  //  /**
  //   * Ha il compito di:
  //   * <ul>
  //   * <li>levare i TextNode composti solo da spazi e che non sono figli di
  //   * <tt>text/span</tt></li>
  //   * <li>aggiungere <tt>text</tt> fittizi quando si incontrano text node non di
  //   * solo spazi ma che non hanno <tt>text</tt> come padre</li>
  //   * </ul>
  //   * 
  //   * @param level
  //   * @param elem
  //   * @throws ValidateException
  //   */
  //  private void adjustTextNodes(int level, IReportNode elem) throws ValidateException {
  //    String pref = Text.getChars('\t', level);
  //    System.out.println(pref + elem);
  //    if (elem instanceof TextNode) {
  //      TextNode tn = (TextNode) elem;
  //      AbstractElement parent = tn.getConcreteParent();
  //      if (tn.isSpace()) {
  //        if (parent == null) {
  //          // figlio di body
  //          System.out.println(tn + " con parent null");
  //          AbstractElement immediateParent = (AbstractElement) elem.getParent();
  //          immediateParent.removeChild(tn);
  //        } else {
  //          if ( ! (parent instanceof TextElement || parent instanceof SpanElement)) {
  //            parent.removeChild(tn);
  //          }
  //        }
  //      } else {
  //        //text not space
  //        if ( ! (parent instanceof TextElement)) {
  //          TextElement fittizio = new TextElement(null, tn.getLineNum(), tn.getLineNum());
  //          parent.replaceChild(tn, fittizio);
  //          tn.setParent(fittizio);
  //        }
  //      }
  //    }
  //
  //    //continuo la navigazione in cerca di altri elementi da aggiustare    
  //    if (elem instanceof AbstractElement) {
  //      AbstractElement ae = (AbstractElement) elem;
  //      for (IReportNode child : ae.getChildren())
  //        adjustTextNodes(level + 1, child);
  //    }
  //  }

  /**
   * Crea il report effettuando in successione queste fasi:
   * <ol>
   * <li><b>Preprocessing</b>: controlla validità del'oggetto e legge l'XML
   * sorgente per determinare la versione corretta del file xmlschema da
   * applicare per la validazione; gestisce il tag <code>include</code></li>
   * <li><b>Validation</b>: validazione sorgente con XMLSchema. Durante la
   * validazione XMLSchema viene anche costruita la struttura intermedia degli
   * elementi del report (lo scheletro)</li>
   * <li><b>Data loading</b>: caricamento dei dati, anche dei sub-reports</li>
   * <li><b>Generation</b>: generazione file di output ciclando in maniera
   * ricorsiva sullo scheletro della struttura e sui dati</li>
   * </ol>
   * 
   * @throws PreprocessingException
   *           per errori nella fase di preprocessing
   * @throws ValidateException
   *           per errori nella fase di validazione
   * @throws DataException
   *           per errori in fase di acquisizione dei dati
   * @throws GenerateException
   *           per errori in fase di generazione dell'output
   */
  public void creaReport() throws GenerateException, ValidateException, DataException, PreprocessingException {
    double start, t1, t2;
    String phaseName = null;
    final String phaseStartMessage = "Fase di %s..."; //$NON-NLS-1$
    final String phaseErrorMessage = "Errore in fase di %s"; //$NON-NLS-1$

    StringBuffer sb = new StringBuffer(300);
    String output = getOutputFile() == null ? " (output stream) " : getOutputFile().getPath(); //$NON-NLS-1$
    sb.append(String.format("Creazione report %s terminata.", output)); //$NON-NLS-1$ //$NON-NLS-2$
    List<TimeSlot> times = new ArrayList<TimeSlot>();

    start = System.currentTimeMillis();

    try {
      phaseName = "preprocessing"; //$NON-NLS-1$
      addDebugMessage(phaseStartMessage, phaseName);
      preProcessing();
    } catch (PreprocessingException ex) {
      addErrorMessage(ex, phaseErrorMessage, phaseName);
      throw ex;
    } catch (Exception ex) {
      PreprocessingException pex = new PreprocessingException(ex, "Errore imprevisto."); //$NON-NLS-1$
      addErrorMessage(pex, phaseErrorMessage, phaseName);
      throw pex;
    }
    t1 = System.currentTimeMillis();
    times.add(new TimeSlot(phaseName, (t1 - start) / 1000d));

    XMLSchemaValidationHandler handler = null;
    try {
      phaseName = "validation"; //$NON-NLS-1$
      addDebugMessage(phaseStartMessage, phaseName);
      handler = validate();
    } catch (ValidateException e) {
      addErrorMessage(e, phaseErrorMessage, phaseName);
      throw e;
    } catch (Exception ex) {
      ValidateException vex = new ValidateException(ex, "Errore imprevisto."); //$NON-NLS-1$
      addErrorMessage(vex, phaseErrorMessage, phaseName);
      throw vex;
    }
    t2 = System.currentTimeMillis();
    times.add(new TimeSlot(phaseName, (t2 - t1) / 1000d));
    t1 = t2;

    try {
      phaseName = "data loading"; //$NON-NLS-1$
      addDebugMessage(phaseStartMessage, phaseName);
      loadData(handler);
      handler.destroy(); //free some memory
    } catch (DataException e) {
      addErrorMessage(e, phaseErrorMessage, phaseName);
      throw e;
    } catch (Exception e) {
      DataException dex = new DataException(e, "Errore imprevisto."); //$NON-NLS-1$
      addErrorMessage(dex, phaseErrorMessage, phaseName);
      throw dex;
    }
    t2 = System.currentTimeMillis();
    times.add(new TimeSlot(phaseName, (t2 - t1) / 1000d));
    t1 = t2;

    try {
      phaseName = "generation"; //$NON-NLS-1$
      addDebugMessage(phaseStartMessage, phaseName);
      generaOutput();
    } catch (GenerateException e) {
      addErrorMessage(e, phaseErrorMessage, phaseName);
      throw e;
    } catch (Exception e) {
      GenerateException gex = new GenerateException(e, "Errore in creazione documento di output."); //$NON-NLS-1$
      addErrorMessage(gex, phaseErrorMessage, phaseName);
      throw gex;
    }
    t2 = System.currentTimeMillis();
    times.add(new TimeSlot(phaseName, (t2 - t1) / 1000d));
    t1 = t2;

    times.add(new TimeSlot("Tempo TOTALE", (t2 - start) / 1000d));
    for (TimeSlot ts : times) {
      sb.append(ts.toString());
    }

    closeDebugFile();

    addDebugMessage(sb.toString());
  }

  private class TimeSlot {
    double time;
    String phase;

    public TimeSlot(String ph, double t) {
      time = t;
      phase = ph;
    }

    @Override
    public String toString() {
      return String.format("\n%s: %.3f secs", Text.padLeft(phase, 20), time);
    }
  }

  /**
   * Controlla se esiste il sorgente del report; se non esiste emette exeption.
   * 
   * @throws StampaException
   *           nel caso il sorgente non sia stato specificato in alcun modo
   */
  private void checkSource() throws PreprocessingException {
    if (c_fileXMLSource == null) {
      throw new PreprocessingException(Msgs.get("Stampa.SOURCE_NOTSPEC")); //$NON-NLS-1$
    }
  }

  /**
   * In questa fase si fanno vari controlli per verificare se l'oggetto Stampa è
   * a posto, poi si va a recuperare dinamicamente il file XMLSchema da
   * utilizzare e si effettuano eventuali sostituzioni dei tag import.
   * 
   * @throws PreprocessingException
   *           in caso di impossibilità a trovare il file xml schema e/o ad
   *           effettuare l'importazione di file esterni inclusi
   */
  private void preProcessing() throws PreprocessingException {
    try {
      checkDestroyed();

      initDebugFile();

      checkSource();

      SAXBuilder builder = new SAXBuilder();

      Document doc = builder.build(c_fileXMLSource);
      Element elem = doc.getRootElement();
      caricaFileValidazioneDinamicamente(elem);

      handleInclude();
    } catch (PreprocessingException e) {
      throw e;
    } catch (Exception e) {
      throw new PreprocessingException(e, "Errore grave in preprocessing"); //$NON-NLS-1$
    }

    //    XPath importXPath = XPath.newInstance("//import");
    //    importXPath.addNamespace(elem.getNamespace());
    //    List lista = importXPath.selectNodes(elem);
  }

  private void handleInclude() throws PreprocessingException {
    try {
      c_fileXMLSourceWithIncludes = null;
      String nomeTemp = "ReportOrig_" + Text.getRandomString(5, Text.CHAR_DIGITS | Text.CHAR_LETTERS) + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
      c_fileXMLSourceWithIncludes = new File(c_tempDir, nomeTemp);
      //FileUtil.copy(new FileInputStream(c_fileXMLSource), new FileOutputStream(outTemp)); 

      if (c_sourceEncoding == null) {
        // se l'encoding non mi è stato passato, cerco di indovinarlo io!
        Charset guessedCharset = CharsetToolkit.guessEncoding(c_fileXMLSource, 4096);
        c_sourceEncoding = guessedCharset.name();
      }
      //encoding = "ISO-8859-1";

      List<String> lines = FileUtil.readLines(new FileInputStream(c_fileXMLSource), c_sourceEncoding);
      //		  if (encoding.equals("UTF-8") && lines.size() > 0) { 
      //levo il BOM (i primi 3 byte del file) dalla prima riga: il BOM è nel primo char della prima riga 
      //		  	String primaRiga = lines.remove(0);
      //		  	primaRiga = primaRiga.substring(1);
      //		  	lines.add(0, primaRiga);
      //		  }

      XMLIncludeValidationHandler handler = new XMLIncludeValidationHandler(this, c_fileXMLSourceWithIncludes, lines,
          c_sourceEncoding);
      InputStream is = new FileInputStream(c_fileXMLSource);
      XMLutil.saxParserValidation(is, null, handler);
      is.close();
    } catch (Exception e) {
      throw new PreprocessingException(e, "Errore grave in gestione include"); //$NON-NLS-1$
    }

  }

  /**
   * Metodo chiamato durante la fase di preprocessing, analizza il root element
   * &lt;stampa&gt; per
   * <ul>
   * <li>determinare la versione del file di validazione da usare</li>
   * <li>caricare dinamicamente l'XML Schema da usare per la validazione</li>
   * </ul>
   * 
   * @param elem
   *          elemento JDom corrispondente al tag &lt;stampa&gt; del sorgente
   *          XML del report
   * @throws PreprocessingException
   *           in caso di impossibilità a trovare il file XML Schema o per
   *           eccezioni impreviste
   */
  private void caricaFileValidazioneDinamicamente(Element elem) throws PreprocessingException {
    try {
      //String szVersion = elem.getAttributeValue("version");
      String nameSpace = elem.getNamespaceURI();
      if ( !Text.isValue(nameSpace)) {
        throw new PreprocessingException("Nel sorgente non è definito il namespace nell'elemento stampa"); //$NON-NLS-1$
      }
      if ( !nameSpace.startsWith(NAMESPACE_BASE)) {
        throw new PreprocessingException("Il namespace nell'elemento stampa non inizia per " + NAMESPACE_BASE); //$NON-NLS-1$
      }

      if (nameSpace.equalsIgnoreCase(NAMESPACE_BASE)) {
        c_XMLSchemaVersion = 1;
      } else {
        String nsSuffix = nameSpace.substring(NAMESPACE_BASE.length());
        if (nsSuffix.charAt(0) != '/' || nsSuffix.length() == 1) {
          throw new PreprocessingException("Namespace dell'elemento stampa non valido"); //$NON-NLS-1$
        }
        c_XMLSchemaVersion = Text.toInt(nsSuffix.substring(1), -1);
        if (c_XMLSchemaVersion < 0 || c_XMLSchemaVersion > MAX_XMLSCHEMA_VERSION) {
          throw new PreprocessingException("Namespace dell'elemento stampa ha una versione non supportata"); //$NON-NLS-1$
        }
      }

      c_XMLSchemaDevelopment = Text.toBoolean(elem.getAttributeValue("development"), false); //$NON-NLS-1$
      String pathVersion = "validation/"; //$NON-NLS-1$
      if (c_XMLSchemaDevelopment) {
        c_fileXMLSchema = pathVersion + "stampadev" + c_XMLSchemaVersion + ".xsd"; //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        pathVersion += "stable/version" + c_XMLSchemaVersion; //$NON-NLS-1$
        c_fileXMLSchema = pathVersion + "/stampa.xsd"; //$NON-NLS-1$
      }
      InputStream XMLSchemaIS = getClass().getResourceAsStream(c_fileXMLSchema);
      if (XMLSchemaIS == null) {
        throw new PreprocessingException("Non trovo il file xsd di validazione XMLSchema " + c_fileXMLSchema); //$NON-NLS-1$
      }
      XMLSchemaIS.close();
      addInfoMessage("Utilizzo XML Schema " + c_fileXMLSchema); //$NON-NLS-1$

      if (c_outputDir == null) {
        if (c_tempDir != null) {
          c_outputDir = c_tempDir;
        }
      }
    } catch (PreprocessingException e) {
      throw e;
    } catch (Exception e) {
      throw new PreprocessingException(e, "Errore grave in caricamento XML Schema"); //$NON-NLS-1$
    }
  }

  private void initDebugFile() throws PreprocessingException {
    try {
      if (isDebugData()) {
        if (c_fileDebug == null) {
          String df = getOutputFileName();
          c_fileDebug = new File(getOutputDir(), df + ".txt"); //$NON-NLS-1$
        }
        c_outfile = new BufferedWriter(new FileWriter(c_fileDebug));
        c_outfile.write("Debug di " + getReportSource() + " iniziato il " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch (Exception e1) {
      throw new PreprocessingException(e1, "Impossibile creare il file di log"); //$NON-NLS-1$
    }
  }

  private void closeDebugFile() {
    try {
      if (c_outfile != null) {
        c_outfile.write("\n Debug di " + getReportSource() + " finito il " + new Date()); //$NON-NLS-1$ //$NON-NLS-2$
        c_outfile.close();
      }
    } catch (Exception e1) {
      //ignoro volutamente qualsiasi eccezione in chiusura file di debug
    }
  }

  /**
   * Ritorna la quantità di sorgenti dati specificate.
   * 
   * @return quantità di sorgenti dati specificate.
   */
  private int getDataSourcesCount() {
    int dataSources = 0;
    if (c_szQuery != null)
      dataSources++;
    if (c_simplelist != null)
      dataSources++;
    if (c_datalist != null)
      dataSources++;
    if (c_dataInputFile != null)
      dataSources++;
    return dataSources;
  }

  /**
   * Carica i dati dalla sorgente indicata.
   * 
   * @throws DataException
   *           nel caso non ci sia la sorgente dati o ci sono errori nella
   *           lettura dei dati
   */
  private void loadData(XMLSchemaValidationHandler handler) throws DataException {
    int dataSources = getDataSourcesCount();

    if (dataSources > 1) {
      throw new DataException("E' stata specificata piu' di una sorgente di dati!"); //$NON-NLS-1$
    } else if (dataSources == 0) {
      //throw new DataException("Mancano i dati: specifica la lista o la query");
      return;
    }

    try {
      c_NoData = false;
      c_mainReport.caricaDati();
      //serializzaDati();
      //addDebugMessage("dati caricati:\n" + c_mainReport.getRootGroup().toXML());
    } catch (NoDataException e) {
      c_NoData = true;
      if ( !isShowMessageWithNoData()) {
        throw e;
      }
    } catch (DataException e) {
      throw e;
    }
  }

  @SuppressWarnings("unused")
  private void serializzaDati() {
    FileOutputStream fos = null;
    ObjectOutputStream out = null;
    SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm"); //$NON-NLS-1$
    try {
      String tempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
      String filename = "xData" + sdf.format(new Date()); //$NON-NLS-1$
      String file = FileUtil.joinPaths(tempDir, filename + ".ser"); //$NON-NLS-1$
      System.out.println("**** SERIALIZZO IN " + file + " ****"); //$NON-NLS-1$ //$NON-NLS-2$
      fos = new FileOutputStream(file);
      out = new ObjectOutputStream(fos);
      out.writeObject(c_mainReport.getRootGroup());
      out.close();
      System.out.println(" #GRUPPI=" + Group.s_groupCount); //$NON-NLS-1$
      System.out.println("  #LISTE=" + GroupList.s_listCount); //$NON-NLS-1$
      System.out.println("  #CAMPI=" + DataField.s_fieldCount); //$NON-NLS-1$
      File f = new File(file);
      System.out.println("  #bytes=" + f.length()); //$NON-NLS-1$

      RootModel rm = c_mainReport.getRootGroup().getModel().getRootModel();
      System.out.println("#stringf=" + getStringFieldsCount(rm)); //$NON-NLS-1$
      System.out.println("#fieldmod=" + getFieldsCount(rm)); //$NON-NLS-1$

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int getStringFieldsCount(GroupModel gm) {
    int totdfm = 0;
    for (DataFieldModel dfm : gm.getFields()) {
      if (dfm.isString())
        totdfm++;
    }
    for (GroupModel child : gm.getChildModels()) {
      totdfm += getStringFieldsCount(child);
    }

    return totdfm;
  }

  private int getFieldsCount(GroupModel gm) {
    int totdfm = gm.fieldsCount();
    for (GroupModel child : gm.getChildModels()) {
      totdfm += getFieldsCount(child);
    }

    return totdfm;
  }

  /**
   * Imposta il nome del motore di rendering dell'output. Tale nome deve
   * corrisponde ad un motore registrato nel file
   * {@link FactoryCreator#FACTORY_PROPERTIES_FILE}
   * 
   * @param engineName
   *          nome motore (ad esempio "itext")
   */
  public void setOutputEngine(String engineName) {
    c_factoryName = engineName;
  }

  /**
   * Ritorna il nome del motore registrato con la
   * {@link #setOutputEngine(String)}. Se non viene impostato nulla, in fase di
   * creazione viene usato il motore di default, che è
   * {@value FactoryCreator#DEFAULT_FACTORY_NAME}.
   * 
   * @return
   */
  public String getOutputEngine() {
    return c_factoryName;
  }

  /**
   * Genera il pdf utilizzando i dati caricati in precedenza e il parsing del
   * sorgente xml del report. Il file di output è nella cartella indicata da
   * {@link #getOutputDir()} e il nome del file è dato da
   * {@link #getOutputFileName()}. Il default del nome del file, se non
   * specificato diversamente, è uguale a {@link #getReportSource()} con
   * estensione cambiata in .pdf.
   * 
   * @throws Exception
   */
  private void generaOutput() throws Exception {
    /** Creiamo il factory che deve generare i nostri elementi */
    if (c_factoryElementi == null) {
      if (c_factoryName != null) {
        c_factoryElementi = FactoryCreator.getFactory(c_factoryName);
      } else {
        c_factoryElementi = FactoryCreator.getDefaultFactory();
      }
    }

    try {
      // NB il nero è un colore già definito in partenza 
      addColor(Colore.NERO);
    } catch (Exception e) {
      //Non lanciamo eccezione di proposito
    }

    c_documento = getDocumento();
    c_documento.setMarginboxElement(c_marginBox);
    c_documento.setRulersElement(c_rulers);
    for (WatermarkElement we : c_watermarks)
      c_documento.addWatermarkElement(we);
    c_documento.inizioDocumento();

    c_pdfGenStatus = GenerationStatus.START_PDFGEN;
    if ( !c_NoData) {
      //report con dati
      getMainReport().generate();
    } else {
      //report senza dati
      getMainReport().generateEmptyDoc();
    }

    c_documento.fineDocumento();

    if (getOutputFile() != null) {
      FileOutputStream fos;
      try {
        fos = new FileOutputStream(getOutputFile());
        if (c_baOutputStream.size() > 0) {
          c_baOutputStream.writeTo(fos);
        }
        fos.close();
      } catch (FileNotFoundException e) {
        throw new GenerateException("Non riesco a creare il file di output " + getOutputFile(), e); //$NON-NLS-1$
      }
    }

    try {
      boolean deleteOK = c_fileXMLSourceWithIncludes.delete();
      if ( !deleteOK) {
        addWarningMessage("Non sono riuscito a cancellare " + c_fileXMLSourceWithIncludes.getAbsolutePath()); //$NON-NLS-1$
      } else {
        addInfoMessage("Cancellazione di " + c_fileXMLSourceWithIncludes.getAbsolutePath() + " riuscita."); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch (Exception e) {
      addWarningMessage("Errore in cancellazione " + c_fileXMLSourceWithIncludes.getAbsolutePath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Dato il nome di un metodo e della sua userclass, risolve la chiamata ad un
   * metodo che ritorna un valore. Fa le seguenti cose:
   * <ol>
   * <li>Se la user class non è mai stata istanziata, la istanzia</li>
   * <li>Cerca nella istanza della user class il metodo con il nome passato e
   * con la signature corretta</li>
   * <li>chiama il metodo trovato passando i corretti parametri</li>
   * </ol>
   * La signature richiesta è di questo tipo:<br/>
   * &nbsp;&nbsp;&nbsp; <b>
   * <tt>public Object <em>methodName</em>(Group,Stampa)</tt></b>
   * 
   * @param userClass
   *          nome della user class
   * @param methodName
   *          nome del metodo
   * @param gruppo
   *          istanza del gruppo corrente durante la chiamata al metodo
   * @param elem
   *          elemento del report da cui viene richiesta l'esecuzione di una
   *          funzione utente
   * 
   * @return valore di ritorno della chiamata del metodo
   * @throws EvaluateException
   *           nel caso non si trovi il metodo richiesto
   * @throws StampaException
   *           nel caso non si riesca ad istanziare la user class
   */
  public Object resolveValueUserCall(String userClass, String methodName, Group gruppo, AbstractElement elem)
      throws EvaluateException {
    Object userClassInst = getUserClassInstance(userClass);
    Method m = null;
    boolean withElem = false;
    try {
      m = userClassInst.getClass().getMethod(methodName, Group.class, this.getClass());
    } catch (Exception e) {
      try {
        m = userClassInst.getClass().getMethod(methodName, Group.class, this.getClass(), AbstractElement.class);
        withElem = true;
      } catch (Exception e2) {
        throw new EvaluateException("Non trovo il metodo " + methodName + " con la corretta signature"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    try {
      if (m.getReturnType() == null || m.getReturnType().getName().equalsIgnoreCase("void")) { //$NON-NLS-1$
        throw new EvaluateException("Il metodo " + methodName + " deve tornare un Object, invece torna void."); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (withElem) {
        return m.invoke(userClassInst, gruppo, this, elem);
      } else {
        return m.invoke(userClassInst, gruppo, this);
      }
    } catch (InvocationTargetException e) {
      String msg = e.getCause() != null ? e.getCause().toString() : e.toString();
      throw new EvaluateException("Il metodo " + methodName + " della user class " + userClass + " ha provocato un errore: " + msg); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch (EvaluateException ev) {
      throw ev;
    } catch (Exception e) {
      throw new EvaluateException("Non trovo il metodo " + methodName + " con la corretta signature"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Dato il nome di un metodo e della sua userclass, risolve la chiamata ad un
   * metodo che ritorna un booleano. Fa le seguenti cose:
   * <ol>
   * <li>Se la user class non è mai stata istanziata, la istanzia</li>
   * <li>Cerca nella istanza della user class il metodo con il nome passato e
   * con la signature corretta</li>
   * <li>chiama il metodo trovato passando i corretti parametri</li>
   * </ol>
   * La signature richiesta è di questo tipo:<br/>
   * &nbsp;&nbsp;&nbsp; <b>
   * <tt>public Boolean <em>methodName</em>(Group,Stampa)</tt></b> <br/>
   * oppure di questo<br/>
   * &nbsp;&nbsp;&nbsp; <b>
   * <tt>public Boolean <em>methodName</em>(Group,Stampa,String)</tt></b>
   * <p>
   * In questo secondo caso, nell'ultimo parametro viene passato
   * <b>nomeCampo</b>
   * </p>
   * 
   * @param userClass
   *          nome della user class
   * @param methodName
   *          nome del metodo
   * @param gruppo
   *          istanza del gruppo corrente durante la chiamata al metodo
   * @param nomeCampo
   *          (opzionale) nome del campo da cui viene fatta la chiamata
   * @return valore di ritorno della chiamata del metodo
   * @throws EvaluateException
   *           nel caso non si trovi il metodo richiesto
   * @throws StampaException
   *           nel caso non si riesca ad istanziare la user class
   */
  public Object resolveBooleanUserCall(String userClass, String methodName, Group gruppo, String nomeCampo, AbstractElement elem)
      throws EvaluateException, StampaException {
    Object userClassInst = getUserClassInstance(userClass);
    Method m = null;
    boolean usaNome = false;
    boolean withElem = false;
    try {
      m = userClassInst.getClass().getMethod(methodName, Group.class, this.getClass());
    } catch (Exception e) {
      try {
        m = userClassInst.getClass().getMethod(methodName, Group.class, this.getClass(), String.class);
        usaNome = true;
      } catch (Exception e1) {
        try {
          m = userClassInst.getClass().getMethod(methodName, Group.class, this.getClass(), AbstractElement.class);
          withElem = true;
        } catch (Exception e2) {
          throw new EvaluateException("Non trovo il metodo " + methodName + " con la corretta signature"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }

    try {
      if (m.getReturnType() != Boolean.class) {
        throw new EvaluateException("Il metodo " + methodName + " deve tornare un Boolean, invece torna void."); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (withElem) {
        return m.invoke(userClassInst, gruppo, this, elem);
      }
      if (usaNome) {
        return m.invoke(userClassInst, gruppo, this, nomeCampo);
      }
      return m.invoke(userClassInst, gruppo, this);
    } catch (Exception e) {
      Throwable ex = e.getCause() != null ? e.getCause() : e;
      throw new EvaluateException("Errore utente in esecuzione del metodo " + methodName + ": " + ex.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Dato un simbolo generato dal parser delle espressioni, lo valuta usando il
   * suo valore come nome di un parametro.
   * 
   * @param symbol
   *          simbolo da valutare
   * @return risultato della valutazione
   * @throws ValidateException
   *           nel caso il valore del parametro non sia valido
   * @throws ResolveException
   *           se parametro inesistente
   * @throws EvaluateException 
   */
  public Object resolveParameter(Symbol symbol) throws ResolveException, ValidateException, EvaluateException {
    String name = symbol.getText();
    if (existParameter(name)) {
      return getParameterValue(name);
    }
    throw new ResolveException("Non esiste un parametro con il nome %s", name); //$NON-NLS-1$
  }

  /**
   * Ritorna la query principale del report.
   */
  public String getDataQuery() {
    return c_szQuery;
  }

  /**
   * Imposta la query per il caricamento dei dati. E' necessario impostare anche
   * il database con {@link #setDatabase(JBB)}. <br>
   * E' in alternativa a {@link #setDataSimpleList(SimpleList)} e
   * {@link #setDataInputFile(String)}. <br/>
   * NOTA BENE: questa chiamata sovrascriverà una eventuale query definita nel
   * report
   * 
   * @param szQuery
   *          query da eseguire per estrarre i dati del report
   */
  public void setDataQuery(String szQuery) {
    c_szQuery = szQuery;
  }

  /**
   * @deprecated utilizzare {@link #setDataQuery(String)}
   * @param szQuery
   *          query da eseguire per estrarre i dati del report
   */
  public void setQuery(String szQuery) {
    setDataQuery(szQuery);
  }

  public void setDataList(List<HashMap<String, Object>> dataList) {
    c_datalist = dataList;
  }

  /**
   * Lista di mappe utilizzata per i dati del report.
   */
  public List<HashMap<String, Object>> getDataList() {
    return c_datalist;
  }

  /**
   * Ritorna la SimpleList utilizzata per i dati
   */
  public SimpleList getDataSimpleList() {
    return c_simplelist;
  }

  /**
   * Imposta la lista per il caricamento dei dati. Se la lista <b>non</b> è già
   * caricata, Verrà caricata al momento opportuno. <br>
   * E' in alternativa a {@link #setDataQuery(String)}.
   * 
   * @param simpleList
   *          lista che contiene i dati
   */
  public void setDataSimpleList(SimpleList simpleList) {
    c_simplelist = simpleList;
  }

  /**
   * @deprecated utilizzare {@link #setDataSimpleList(SimpleList)}
   * @param simpleList
   *          lista che contiene i dati
   */
  public void setLista(SimpleList simpleList) {
    setDataSimpleList(simpleList);
  }

  /**
   * @deprecated utilizzare {@link #setDataFullList(Lista)}
   * @param lista
   *          lista che contiene i dati
   */
  public void setLista(Lista lista) {
    setDataFullList(lista);
  }

  /**
   * Imposta la lista per il caricamento dei dati. Se la lista contiene delle
   * relazioni, viene 'appiattita' internamente in una simplelist.
   * 
   * @param lista
   *          lista che contiene i dati
   */
  public void setDataFullList(Lista lista) {
    try {
      if (lista==null) {
        c_simplelist = null;
      }
      else {
        SimpleList simpleList = flattenLista(lista);
        c_simplelist = simpleList;        
      }
    } catch (CISException e) {
      e.printStackTrace();
    }
  }

  /**
   * Ritorna la font di default, cioè quella usata negli elementi del sorgente
   * XML del report che non hanno una specifica di font diretta (attributo
   * 'refFont') o indiretta (font ereditata da un elemento antenato). <br>
   * Tale font è la font definita con l'attributo <tt>default</tt> =
   * <b>true</b>, se c'è; altrimenti è una font di default del sistema.
   * 
   * @return font di default.
   */
  public StileCarattere getDefaultFont() {
    if (c_defaultFont == null) {
      c_defaultFont = StileCarattere.getStileDefault();
    }
    return c_defaultFont;
  }

  /**
   * Ritorna una definizione di colore dato il suo nome logico.
   * 
   * @param fontName
   *          attributo <tt>name</tt> dell'elemento <tt>&lt;color&gt;</tt> nel
   *          sorgente XML del report
   * @return colore associato alla font oppure null se non trovato
   */
  public Colore getColorByName(String colorName) {
    if (colorName == null)
      return null;
    Colore c = c_mappaColori.get(colorName);
    if (c == null) {
      //non trovato nei colori user-defined: provo con i colori builtin
      c = c_builtinColors.get(colorName);
    }
    return c;
  }

  /**
   * Copia i colori builtin nella lista di quelli user defined. Se un colore
   * user-defined ha lo stesso nome di un colore builtin, tengo quello
   * user-defined e scarto il builtin.
   */
  private void mergeBuiltinColors() {
    for (Colore builtin : c_builtinColors.values()) {
      if ( !c_mappaColori.containsKey(builtin.getName()))
        c_mappaColori.put(builtin.getName(), builtin);
    }
  }

  /**
   * Ritorna una definizione di font dato il suo nome logico.
   * 
   * @param fontName
   *          attributo <tt>name</tt> dell'elemento <tt>&lt;font&gt;</tt> nel
   *          sorgente XML del report
   * @return stile associato alla font oppure null se non trovato
   */
  public StileCarattere getFontByName(String fontName) {
    return c_mappaFont.get(fontName);
  }

  public FactoryElementi getFactoryElementi() {
    return c_factoryElementi;
  }

  /**
   * Restituisce il documento corrente su cui viene fisicamente generata la
   * stampa. Se non è stato ancora creato, lo crea subito.
   * 
   * @throws GenerateException
   *           in caso di errori in creazione documento
   * @throws EvaluateException
   */
  public Documento getDocumento() throws GenerateException {
    if (c_documento == null) {
      mergeBuiltinColors();
      c_documento = getFactoryElementi().creaDocumento(this, null, null);
    }
    return c_documento;
  }

  /**
   * Impostazione dei margini del foglio di output
   * 
   * @param m
   *          specifica dei margini nei 4 lati.
   */
  public void setDocMargini(Margini m) {
    c_docMargini = m;
  }

  /**
   * Imposta la password di protezione per l'apertura del documento
   * 
   * @param p
   *          password; se null o stringa vuota, resetta la password, cioè il
   *          documento non è protetto in apertura
   */
  public void setDocumentPassword(String p) {
    if (p == null || p.length() == 0)
      c_docPassword = null;
    else
      c_docPassword = p.getBytes();
  }

  public byte[] getDocumentUserPassword() {
    return c_docPassword;
  }

  /**
   * Aggiunge la specifica di un permesso
   * 
   * @param perm
   *          permesso da impostare
   * @param allow
   *          valore del permesso: true=permesso concesso, false=permesso negato
   */
  public void addPermission(DocumentPermission perm, boolean allow) {
    c_docPermissions.put(perm, Boolean.valueOf(allow));
  }

  /**
   * Ritorna il valore del permesso richiesto.
   * 
   * @param perm
   *          permesso richiesto
   * @return {@link Boolean#TRUE} se permesso accordato, {@link Boolean#FALSE}
   *         se negato, <b>null</b> se non specificato
   */
  public Boolean getPermission(DocumentPermission perm) {
    return c_docPermissions.get(perm);
  }

  /**
   * Rimuove il permesso specificato.<br/>
   * 
   * Dopo questa chiamata si avrà <code>getPermission(perm)==null</code>.
   * 
   * @param perm
   *          permesso da eliminare
   * @return valore che aveva il permesso oppure null se non esisteva
   */
  public Boolean removePermission(DocumentPermission perm) {
    return c_docPermissions.remove(perm);
  }

  /**
   * Azzera tutte le specifiche di permessi esistenti
   */
  public void clearPermissions() {
    c_docPermissions.clear();
  }

  /**
   * Imposta la dimensione del formato del foglio di output. La dimensione è
   * data dalle misure del rettangolo passato.
   * 
   * @param r
   *          rettangolo con le misure esterne del foglio di output
   */
  public void setDocSize(Rettangolo r) {
    c_docRettangolo = r;
  }

  /**
   * Ritorna i margini del documento di output. L'implementazione del generatore
   * dovrà leggere questa impostazione e creare un documento fisico
   * possibilmente con i margini specificati.
   */
  public Margini getDocMargini() {
    return c_docMargini;
  }

  /**
   * Ritorna la dimensione del documento di output. L'implementazione del
   * generatore dovrà leggere questa dimensione e creare un documento fisico
   * possibilmente con queste dimensioni.
   */
  public Rettangolo getDocSize() {
    return c_docRettangolo;
  }

  /**
   * Ritorna true se esiste una definizione di stile carattere col nome passato.
   * 
   * @param name
   *          nome stile
   */
  public boolean existStileCarattere(String name) {
    return c_mappaFont.containsKey(name);
  }

  /**
   * Aggiunge uno stile di carattere all'elenco degli stili.
   * 
   * @param stileCar
   *          stile da aggiungere
   * @throws StampaException
   *           nel caso esista già uno stile con lo stesso nome
   */
  public void addStile(StileCarattere stileCar) throws StampaException {
    if (c_mappaFont.containsKey(stileCar.getName())) {
      throw new StampaException("Uno stile carattere esiste gia' con questo nome: " + stileCar.getName()); //$NON-NLS-1$
    }
    c_mappaFont.put(stileCar.getName(), stileCar);
    if (stileCar.isDefault()) {
      c_defaultFont = stileCar;
      StileCarattere sysDefault = c_mappaFont.get(StileCarattere.SYSTEM_DEFAULT_NAME);
      if (sysDefault != null) {
        //copio nella font di sistema la font di default
        sysDefault.setBold(stileCar.isBold());
        sysDefault.setColore(stileCar.getColore());
        sysDefault.setDefault(stileCar.isDefault());
        sysDefault.setFamily(stileCar.getFamily());
        sysDefault.setItalic(stileCar.isItalic());
        sysDefault.setSize(stileCar.getSize());
        sysDefault.setSourceFont(stileCar.getSourceFont());
        sysDefault.setUnderline(stileCar.isUnderline());
      }
    }
  }

  /**
   * Aggiunge un colore all'elenco dei colori mantenuto da questo oggetto.
   * 
   * @param color
   *          colore da aggiungere
   * @throws StampaException
   *           se esiste già un colore con lo stesso nome
   */
  public void addColor(Colore color) throws StampaException {
    if (c_mappaColori.containsKey(color.getName()))
      throw new StampaException("Colore gia' esistente: " + color.getName()); //$NON-NLS-1$
    char c = color.getName().charAt(0);
    if ( !Character.isLetter(c)) {
      throw new StampaException("Il nome di colore deve iniziare con una lettera: " + color.getName()); //$NON-NLS-1$
    }
    if ( !isLegalName(color.getName())) {
      throw new StampaException("Il nome di colore non è ammesso: " + color.getName()); //$NON-NLS-1$
    }
    c_mappaColori.put(color.getName(), color);
  }

  /**
   * Torna la lista dei colori corrente. <br>
   * Ogni elemento della lista corrisponde ad un elemento <b>&lt;color&gt;</b>
   * del sorgente XML del report.
   */
  public Collection<Colore> getColorList() {
    return c_mappaColori.values();
  }

  /**
   * Torna la lista degli stili di carattere corrente. <br>
   * Ogni elemento della lista corrisponde ad un elemento <b>&lt;font&gt;</b>
   * del sorgente XML del report.
   * 
   * @return
   */
  public Collection<StileCarattere> getStileCarattList() {
    return c_mappaFont.values();
  }

  /**
   * Ritorna il tag radice del report. <br>
   * Il sorgente XML del report viene analizzato sintatticamente e viene
   * costruita internamente una struttura di oggetti (nel package
   * ciscoop.stampa.source) che corrisponde alla struttura DOM dei nodi XML. <br>
   * La sezione <b>&lt;head&gt;</b> viene gestita a parte; la sezione
   * <b>&lt;body&gt;</b> viene analizzata e questo metodo torna il nodo
   * principale di <b>&lt;body&gt;</b>.
   */
  //	public GroupElement getElemRadice() {
  //		return c_elemRadice;
  //	}

  public RootGroup getGruppoRadice() {
    return c_mainReport.getRootGroup();
  }

  private MainReportInfo getMainReport() {
    return c_mainReport;
  }

  public RootModel getRootModel() {
    if (c_rootModel == null) {
      c_rootModel = new RootModel();
      c_rootModel.setDebugMode(isDebugData());
    }
    return c_rootModel;
  }

  /**
   * Imposta il report principale. Settato da {@link XMLSchemaValidationHandler}
   * durante l'analisi XML del sorgente.
   * 
   * @throws ValidateException
   * @throws StampaException
   */
  private void setMainReport(GroupElement elemRadice, RootModel model) throws DataException, ValidateException {
    String defName = getDefaultFont().getName();
    elemRadice.setRefFontName(defName);
    if (getDataQuery() != null) {
      c_mainReport = new MainReportInfo(this, getDataQuery(), model, elemRadice);
    } else if (getDataSimpleList() != null) {
      c_mainReport = new MainReportInfo(this, getDataSimpleList(), model, elemRadice);
    } else if (getDataList() != null) {
      c_mainReport = new MainReportInfo(this, getDataList(), model, elemRadice);
    } else if (getDataInputFile() != null) {
      List<HashMap<String, Object>> dataMap = readInputFile();
      c_mainReport = new MainReportInfo(this, dataMap, model, elemRadice);
    } else {
      //report senza dati
      c_mainReport = new MainReportInfo(this, model, elemRadice);
    }

  }

  /**
   * Ritorna l'elemento <tt>pageHeader</tt> definito nel sorgente XML.
   */
  public PageHeader getPageHeader() {
    return c_pageHeader;
  }

  /**
   * Ritorna l'elemento <tt>pageFooter</tt> definito nel sorgente XML.
   */
  public PageFooter getPageFooter() {
    return c_pageFooter;
  }

  /*
   * public void setFontElemRadice() {
   * c_elemRadice.setRefFontName(getDefaultFont().getName() ); }
   */

  /**
   * Ritorna il database impostato con la {@link #setDatabase(JBB)}.
   */
  public JBB getDatabase() {
    return c_database;
  }

  /**
   * Imposta il database. E' necessario impostarlo nel caso si passi una query
   * SQL che questo oggetto deve eseguire.
   * 
   * @param database
   *          db da usare per leggere i dati
   */
  public void setDatabase(JBB database) {
    c_database = database;
  }

  /**
   * Nome del file .xml che è il sorgente del report.
   */
  public File getReportSource() {
    return c_fileXMLSource;
  }

  /**
   * Permette di specificare il file .xml che è il sorgente del report. Da tale
   * file verrà generato un pdf tramite il metodo {@link #creaReport()}.
   * 
   * @param source
   *          sorgente del report
   * @throws StampaException
   *           nel caso il file non esista
   */
  public void setReportSource(File source) throws StampaException {
    if ( !source.exists()) {
      throw new StampaException("Il file del report specificato non esiste: " + source.getAbsolutePath()); //$NON-NLS-1$
    }
    c_fileXMLSource = source;
    c_streamXMLSource = null;
    addInnerParameter(INNERPARAM_SOURCE_FULLPATH, source.getAbsolutePath());
    addInnerParameter(INNERPARAM_SOURCE_NAME, FileUtil.getLastPart(source.getAbsolutePath()));
  }

  /**
   * Permette di specificare il file .xml che è il sorgente del report con un
   * path relativo al class loader. Il path può essere in modalità
   * "file-system", cioè con "/" o "\" come separatore di cartelle, o in
   * modalità java col "." come separatore dei package. <br/>
   * Se ad esempio ho il report clienti.xml nel package
   * sm.myapp.reports.clienti, si può specificare nei seguenti 2 modi del tutto
   * equivalenti:
   * <ul>
   * <li>setReportSource("sm.myapp.reports.clienti.clienti.xml");</li>
   * <li>setReportSource("sm/myapp/reports/clienti/clienti.xml");</li>
   * </ul>
   * 
   * @param source
   *          path completo del sorgente relativo al class-path; si può anche
   *          omettere l'estensione .xml
   * @throws StampaException
   *           nel caso il file non esista o source sia null
   */
  public void setReportSource(String source) throws StampaException {
    if (source == null || source.length() == 0) {
      throw new StampaException("Il file del report non specificato!"); //$NON-NLS-1$
    }
    try {
      if ( !source.endsWith(".xml")) { //$NON-NLS-1$
        source += ".xml"; //$NON-NLS-1$
      }
      //assumo che sia il "." prima dell'estensione xml
      int i = source.lastIndexOf('.');
      //rimpiazzo tutti i punti con la "/"
      String replSource = source.substring(0, i).replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      //aggiungo l'estensione
      replSource += source.substring(i);
      //facilitazione per i distratti: levo eventuale slash iniziale
      if (replSource.startsWith("/")) { //$NON-NLS-1$
        replSource = replSource.substring(1);
      }
      URL u = getClass().getClassLoader().getResource(replSource);
      if (u == null) {
        throw new StampaException("Il path specificato per il report non esiste: " + source); //$NON-NLS-1$
      }
      File sourceFile = new File(u.toURI());
      setReportSource(sourceFile);
    } catch (URISyntaxException e) {
      throw new StampaException("Errore imprevisto in rilevazione file sorgente: " + e.toString()); //$NON-NLS-1$
    }
  }

  public void setReportSource(InputStream stream) throws StampaException {
    c_fileXMLSource = null;
    c_streamXMLSource = stream;
    c_fileXMLSource = new File(c_tempDir, "Report" + Text.getRandomString(5, Text.CHAR_DIGITS | Text.CHAR_LETTERS) + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
    addInnerParameter(INNERPARAM_SOURCE_FULLPATH, c_fileXMLSource.getAbsolutePath());
    addInnerParameter(INNERPARAM_SOURCE_NAME, FileUtil.getLastPart(c_fileXMLSource.getAbsolutePath()));
    try {
      FileUtil.copy(c_streamXMLSource, new FileOutputStream(c_fileXMLSource));
    } catch (Exception e) {
      throw new StampaException("Errore grave in copia su file temporaneo: " + e.getMessage(), e); //$NON-NLS-1$
    }
  }

  /**
   * Ritorna la directory sorgente, cioè la directory dove è il file XML
   * sorgente del report impostato con la {@link #setReportSource(File)}.
   * Ritorna null se non è stato specificato alcun file sorgente.
   */
  public File getSourceDir() {
    if (c_fileXMLSource != null) {
      return c_fileXMLSource.getParentFile();
    }

    return null;
  }

  /**
   * Modalità di debug. Se true, vengono emessi su standard output diversi
   * messaggi informativi durante la generazione del report.
   */
  public boolean isDebugMode() {
    return c_debugMode;
  }

  /**
   * Imposta modalità debug.
   * 
   * @param debugMode
   *          debugMode to set
   */
  public void setDebugMode(boolean debugMode) {
    c_debugMode = debugMode;
    setupLogger();
  }

  /**
   * Imposta modalità di debug dei dati. Se la modalità è true viene generato un
   * file xml contenente la valutazione espansa della struttura da generare
   * 
   * @param debugMode
   *          debugMode to set
   */
  public void setDebugData(boolean debugData) {
    c_debugData = debugData;
  }

  /**
   * Modalità di debug. Se true, vengono emessi su standard output diversi
   * messaggi informativi durante la generazione del report.
   */
  public boolean isDebugData() {
    return c_debugData;
  }

  /**
   * Ritorna la directory di output. Nella directory di output va il file
   * contenente il prodotto finale (il report) ed eventuali files di log. <br/>
   * Inizialmente è impostata in automatico alla directory temporanea definita
   * dal sistema. In seguito può essere modificata in 2 modi:
   * <ul>
   * <li>direttamente, chiamando {@link #setOutputDir(File)}</li>
   * <li>indirettamente, chiamando {@link #setOutputFileName(String)}: se (e
   * solo se) il file impostato è un path assoluto, la directory viene presa e
   * impostata con essa la directory di output.</li>
   * </ul>
   */
  public File getOutputDir() {
    return c_outputDir;
  }

  /**
   * Imposta la directory di output.
   * 
   * @see #getOutputDir()
   * @param dir
   *          la directory da settare
   */
  public void setOutputDir(File dir) {
    c_outputDir = dir;
  }

  /**
   * Ritorna il file di output (il prodotto del report9 in base a
   * {@link #getOutputDir()} e {@link #getOutputFileName()}. Se
   * {@link #getOutputFileName()} è null, il metodo torna null.
   * 
   * @return file di output destinazione del report
   */
  public File getOutputFile() {
    if (getOutputFileName() != null) {
      return new File(c_outputDir, getOutputFileName());
    }
    return null;
  }

  /**
   * Ritorna il nome del file di output. <br/>
   * Se il nome del file è assoluto, viene presa la sua directory come directory
   * di output. Se nessun file di output è stato specificato e
   * {@link #isAutoOutputFileName()} è true, il nome viene preso dal sorgente
   * xml del report (vedi {@link #getReportSource()}) e cambiata l'estensione in
   * base al tipo di output.
   */
  public String getOutputFileName() {
    if (m_outputFileName == null && isAutoOutputFileName()) {
      String nomeReport = c_fileXMLSource != null ? c_fileXMLSource.getName() : "report" //$NON-NLS-1$
          + Text.getRandomString(5, Text.CHAR_DIGITS | Text.CHAR_LETTERS);
      int i = nomeReport.lastIndexOf('.');
      String extension = ""; //$NON-NLS-1$
      try {
        extension = c_factoryElementi.getExtension();
      } catch (GenerateException e) {
        e.printStackTrace();
      }
      if (i > 0) {
        //levo estensione originale
        nomeReport = nomeReport.substring(0, i);
      }
      m_outputFileName = nomeReport + "." + extension; //$NON-NLS-1$
    }
    return m_outputFileName;
  }

  /**
   * Imposta il file di output. Se il file è un path assoluto, la sua directory
   * viene automaticamente impostata come directory di output, sovrascrivendo
   * eventuali settaggi fatti in precedenza.
   * 
   * @param outputFileName
   *          nome del file di output, semplice o con path
   */
  public void setOutputFileName(String outputFileName) {
    m_outputFileName = outputFileName;
    if (outputFileName != null) {
      File f = new File(outputFileName);
      if (f.isAbsolute()) {
        setOutputDir(f.getParentFile());
      }
    }
  }

  /**
   * @deprecated usare {@link #addParameter(String, Object)}
   */
  @Deprecated
  public void addConstant(String name, Object value) throws StampaException {
    addParameter(name, value);
  }

  /**
   * Aggiunge un parametro utilizzabile nel report. Ad esempio se aggiungo il
   * parametro 'azienda' con <tt>addParameter("azienda", "CIScoop")</tt> nel
   * report posso riferirmi alla parametro con
   * <tt>&lt;field value="$azienda" /&gt;</tt> e verrà stampato il suo valore "
   * <tt>CIScoop</tt>".
   * 
   * @param name
   *          nome parametro (case sensitive)
   * @param value
   *          valore del parametro, può essere null
   * @return il parametro aggiunto
   * @throws ValidateException
   *           nel caso il nome non sia valido, vedi
   *           {@link #isLegalParameterName(String)}, oppure esista già
   * @see #getParameterValue(String)
   * @see #existParameter(String)
   * @see #removeParameter(String)
   */
  public Parameter addParameter(String name, Object value) throws ValidateException {
    return addParameter(name, value, null);
  }

  /**
   * Aggiunge un parametro utilizzabile nel report. Ad esempio se aggiungo il
   * parametro 'azienda' con <tt>addParameter("azienda", "CIScoop")</tt> nel
   * report posso riferirmi alla parametro con
   * <tt>&lt;field value="$azienda" /&gt;</tt> e verrà stampato il suo valore "
   * <tt>CIScoop</tt>".
   * 
   * @param name
   *          nome parametro (case sensitive)
   * @param value
   *          valore del parametro, può essere null
   * @param className
   *          nome della classe java del tipo che deve avere il parametro, può
   *          essere null
   * @return il parametro aggiunto
   * @throws ValidateException
   *           nel caso il nome non sia valido, vedi
   *           {@link #isLegalParameterName(String)}, oppure esista già
   * @see #getParameterValue(String)
   * @see #existParameter(String)
   * @see #removeParameter(String)
   */
  public Parameter addParameter(String name, Object value, String className) throws ValidateException {
    if (isLegalParameterName(name)) {
      if (m_parameters.containsKey(name))
        throw new ValidateException("Il parametro '%s' esiste gia', non puoi aggiungerlo", name); //$NON-NLS-1$ //$NON-NLS-2$      
      Parameter p = new Parameter(this, name, value, className);
      m_parameters.put(name, p);
      return p;
    }
    throw new ValidateException("Il nome di parametro '%s' non e' valido", name); //$NON-NLS-1$ //$NON-NLS-2$    
  }

  /**
   * Se il parametro non esiste lo aggiunge, se invece esiste ne sostituisce il
   * valore con quello passato
   * 
   * @param name
   *          nome parametro (case sensitive)
   * @param value
   *          valore del parametro
   * @return il parametro aggiunto oppure di cui e' stato modificato il valore
   * @throws StampaException
   * @throws ValidateException
   *           nel caso il nome non sia valido, vedi
   *           {@link #isLegalParameterName(String)} oppure non sia compatibile
   *           con la classe del parametro
   */
  public Parameter setParameterValue(String name, Object value) throws ValidateException {
    Parameter p = null;
    if (m_parameters.containsKey(name)) {
      p = m_parameters.get(name);
      p.setValue(value);
    } else {
      p = addParameter(name, value);
    }
    return p;
  }

  private void addInnerParameter(String name, Object value) throws StampaException {
    m_parameters.put(name, new Parameter(this, name, value));
  }

  /**
   * @deprecated usare {@link #removeParameter(String)}
   */
  @Deprecated
  public Object removeConstant(String name) {
    return removeParameter(name);
  }

  /**
   * Rimuove un parametro precedentemente assegnato con la
   * {@link #addParameter(String, Object)}. <br/>
   * NOTA BENE: le costanti di sistema non possono essere rimosse
   * 
   * @param name
   *          nome parametro (case sensitive); se è un inner parameter non può
   *          essere rimossa
   * @return valore che aveva il parametro oppure null se il parametro non
   *         esiste
   */
  public Object removeParameter(String name) {
    if ( !isInnerParameter(name))
      return m_parameters.remove(name);
    return null;
  }

  /**
   * Indica se un nome è legale per una user class. Un nome legale può
   * contenere, solo cifre e lettere, e non può iniziare per una cifra.
   * 
   * @param name
   *          nome da controllare
   * @return true sse nome valido per una user class
   */
  public boolean isLegalClassName(String name) {
    boolean bLegal = isLegalName(name);
    if (bLegal) {
      bLegal = Text.isOnlyCharType(name, Text.CHAR_LETTERS | Text.CHAR_DIGITS);
    }
    if (bLegal) {
      bLegal = !Character.isDigit(name.charAt(0));
    }
    return bLegal;
  }

  /**
   * Controlla che una stringa sia un nome legale per un parametro, un campo, un
   * nome di colore, etc. Un nome legale può contenere, solo cifre e lettere,
   * spazi e punti. Non può iniziare per una cifra.
   * 
   * @param name
   *          nome da controllare
   * @return true se il nome è utilizzabile per un parametro, un campo, un
   *         colore, etc
   */
  public boolean isLegalName(String name) {
    boolean bLegal = Text.isValue(name);

    if (bLegal) {
      for (int i = 0; i < name.length(); i++) {
        char c = name.charAt(i);
        if ( ! (Character.isLetterOrDigit(c) || c == ' ' || c == '.')) {
          bLegal = false;
          break;
        }
      }
    }
    if (bLegal) {
      bLegal = !Character.isDigit(name.charAt(0));
    }
    if (bLegal) {
      bLegal = !isInnerParameter(name);
    }
    return bLegal;
  }

  /**
   * Indica se un nome è legale per un parametro: oltre al controllo di sintassi
   * del nome, controlla che il nome non sia uno dei nomi dei parametri interni
   * (vedi {@link #isInnerParameter(String)}).
   * 
   * @param name
   *          nome da controllare
   * @return true sse nome valido per un parametro definito dall'utente
   */
  public boolean isLegalParameterName(String name) {
    boolean bLegal = isLegalName(name);
    if (bLegal) {
      bLegal = !isInnerParameter(name);
    }
    return bLegal;
  }

  /**
   * Indica se il nome passato è di un inner parameter
   * 
   * @param name
   *          nome da testare
   * @return true sse è un parametro interno (quelle che iniziano con "stampa.")
   */
  private boolean isInnerParameter(String name) {
    if (name == null)
      return false;
    return name.toLowerCase().startsWith(INNERPARAM_PREFIX); //$NON-NLS-1$
  }

  /**
   * Aggiunge la specifica di una userclass
   * 
   * @param name
   *          nome logico classe
   * @param value
   *          nome completo della classe
   * @throws ValidateException
   *           in caso di nome classe non valido
   */
  public void addUserClass(String name, String value) throws ValidateException {
    checkUserClassName(name);
    m_userClasses.put(name, value);
  }

  private void checkUserClassName(String name) throws ValidateException {
    if ( !isLegalClassName(name))
      throw new ValidateException("Il nome di user class %s non e' valido", name);
  }

  /**
   * Aggiunge la specifica di una userclass tramite la sua class
   * 
   * @param name
   *          nome logico classe
   * @param theClass
   *          class da istanziare
   * @throws ValidateException
   *           in caso di nome classe non valido
   */
  public void addUserClass(String name, Class<?> theClass) throws ValidateException {
    checkUserClassName(name);
    m_userClasses.put(name, theClass.getName());
  }

  /**
   * Aggiunge una istanza di userclass: in questo modo si comunica che la
   * userclass è già istanziata e si deve usare l'istanza passata.
   * 
   * @param name
   *          nome logico classe, cioè quello da usare nel sorgente per
   *          riferirsi a questa istanza
   * @param classInstance
   *          istanza della user class da usare, mappata al nome <tt>name</tt>
   * @throws ValidateException
   *           in caso di nome classe non valido
   */
  public void addUserClassInstance(String name, Object classInstance) throws ValidateException {
    checkUserClassName(name);
    m_userClasses.put(name, classInstance.getClass().getName());
    m_userInstances.put(name, classInstance);
  }

  /**
   * Dato un nome simbolico di classe, ne ritorna la sua istanza. Se la classe è
   * già stata istanziata, ritorna quella istanza, altrimenta la istanzia e ne
   * ritorna l'oggetto creato.
   * 
   * @param classRefName
   *          nome classe simbolico
   * @return istanza classe
   * @throws StampaException
   *           nel caso non sia possobile istanziare la classe
   */
  public Object getUserClassInstance(String classRefName) throws EvaluateException {
    checkUserClassAndThrow(classRefName);

    Class<?> cl;
    Object instance = m_userInstances.get(classRefName);
    String classFullName = null;
    if (instance == null) {
      try {
        classFullName = m_userClasses.get(classRefName);
        cl = Class.forName(classFullName);
      } catch (ClassNotFoundException e) {
        throw new EvaluateException("Non è possibile trovare la user class " + classFullName + ": " + e.toString(), e); //$NON-NLS-1$ //$NON-NLS-2$
      }
      try {
        instance = cl.newInstance();
      } catch (Exception e) {
        throw new EvaluateException("Non è possibile istanziare la user class " + classFullName + ": " + e.toString(), e); //$NON-NLS-1$ //$NON-NLS-2$
      }
      m_userInstances.put(classRefName, instance);
    }
    return instance;
  }

  /**
   * @throws ValidateException
   * @throws EvaluateException 
   * @deprecated usare {@link #getParameterValue(String)}
   */
  @Deprecated
  public Object getConstantValue(String name) throws ValidateException, EvaluateException {
    return getParameterValue(name);
  }

  /**
   * Ottiene il valore del parametro di cui è passato il nome.
   * 
   * @param name
   *          nome parametro; NB: name è <b>case sensitive</b>
   * @return valore parametro oppure null se non è stata definito
   * @throws ValidateException
   * @throws EvaluateException 
   */
  public Object getParameterValue(String name) throws ValidateException, EvaluateException {
    if (name == null)
      return null;
    if (m_parameters.containsKey(name)) {
      Parameter p = m_parameters.get(name);
      p.check();
      return p.getValue();
    } else if (m_sysParameters.containsKey(name.toLowerCase())) {
      return m_sysParameters.get(name.toLowerCase());
    }
    return null;
  }

  public Parameter getParameter(String name) {
    return m_parameters.get(name);
  }

  /**
   * Controlla se un parametro è stata definita. Ciò comprende sia le costanti
   * utente (aggiunte con {@link #addParameter(String, Object)}) sia quelle di
   * sistema.
   * 
   * @param name
   *          nome del parametro; NB: name è <b>case sensitive</b>
   * @return true sse il parametro <tt>name</tt> esiste.
   */
  public boolean existParameter(String name) {
    if (name == null)
      return false;
    return m_parameters.containsKey(name) || m_sysParameters.containsKey(name.toLowerCase());
  }

  /**
   * Controlla se un parametro è un parametro di sistema.
   * 
   * @param name
   *          nome del parametro; NB: name è <b>case sensitive</b>
   * @return true sse parametro <tt>name</tt> esiste ed è di sistema.
   */
  public boolean isSystemParameter(String name) {
    return m_sysParameters.containsKey(name);
  }

  /**
   * Ritorna true se una user class con il nome passato è stata definita nel XML
   * sorgente del report. Il nome è l'attributo <tt>name</tt> dell'elemento
   * <tt>&lt;class&gt;</tt>.
   * 
   * @param name
   *          nome della user class
   * @return true sse è stata definita
   */
  public boolean existUserClass(String name) {
    return m_userClasses.containsKey(name);
  }

  /**
   * Testa esistenza di una user class tramite il suo nome e genera exception se
   * non la trova.
   * 
   * @param name
   * @throws StampaException
   */
  private void checkUserClassAndThrow(String name) throws EvaluateException {
    if ( !existUserClass(name)) {
      throw new EvaluateException("La user class '" + name + "' non è stata definita."); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * @return the pdfGenStatus
   */
  public GenerationStatus getGenerationStatus() {
    return c_pdfGenStatus;
  }

  /**
   * @param p_pdfGenStatus
   *          the pdfGenStatus to set
   */
  public void setGenerationStatus(GenerationStatus p_pdfGenStatus) {
    c_pdfGenStatus = p_pdfGenStatus;
  }

  /**
   * @return the c_resourcesDir
   */
  public File getResourcesDir() {
    return c_resourcesDir;
  }

  /**
   * Imposta la directory dove trovare le risorse per il report (immagini, files
   * di font,...)
   * 
   * @param dir
   *          directory da impostare
   */
  public void setResourcesDir(File dir) {
    c_resourcesDir = dir;
  }

  /**
   * Dato un nome di file, cerca di trovare il path assoluto della risorsa. La
   * ricerca del file è fatta in quest'ordine:
   * <ol>
   * <li>la directory corrente</li>
   * <li>la directory delle risorse, se specificata</li>
   * <li>la directory dove risiede il sorgente XML del report</li>
   * </ol>
   * La ricerca è fatta solo se il nome non è un path assoluto.
   * 
   * @param fileName
   *          nome file da cercare
   * @return path assoluto del file *solo se* effettivamente esistente; se il
   *         file non esiste torna <b>null</b>
   */
  public String findResource(String fileName) {
    File fSrc = new File(fileName);
    if (fSrc.exists()) {
      return fSrc.getAbsolutePath();
    }
    if ( !fSrc.isAbsolute() && getResourcesDir() != null) {
      File fSrcTemp = new File(getResourcesDir(), fileName);
      if (fSrcTemp.exists()) {
        return fSrcTemp.getAbsolutePath();
      }
    }
    if ( !fSrc.isAbsolute() && getSourceDir() != null) {
      fSrc = new File(getSourceDir(), fileName);
      if (fSrc.exists()) {
        return fSrc.getAbsolutePath();
      }
    }
    return null;
  }

  public void addDebugMessage(String format, Object... params) {
    addDebugMessage(String.format(format, params));
  }

  public void addDebugMessage(String msg) {
    String out = "[DEBUG] " + m_dtlogger.format(new Date()); //$NON-NLS-1$
    out += "  " + msg; //$NON-NLS-1$
    if (c_logger != null) {
      c_logger.debug(out);
    } else {
      if (isDebugMode()) {
        System.out.println(out);
      }
    }
  }

  public void addInfoMessage(String msg) {
    String out = "[INFO] " + m_dtlogger.format(new Date()); //$NON-NLS-1$
    out += "  " + msg; //$NON-NLS-1$
    if (c_logger != null) {
      c_logger.info(out);
    } else {
      System.out.println(out);
    }
  }

  public void addWarningMessage(String msg) {
    String out = "[WARNING] " + m_dtlogger.format(new Date()); //$NON-NLS-1$
    out += "  " + msg; //$NON-NLS-1$
    if (c_logger != null) {
      c_logger.warn(out);
    } else {
      System.out.println(out);
    }
  }

  public void addErrorMessage(String msg) {
    String out = "[ERROR] " + m_dtlogger.format(new Date()); //$NON-NLS-1$
    out += "  " + msg; //$NON-NLS-1$
    if (c_logger != null) {
      c_logger.error(out);
    } else {
      System.err.println(out);
    }
  }

  public void addErrorMessage(String msg, Throwable exception) {
    String out = msg;
    if (exception != null) {
      out += "\n" + getStackTrace(exception); //$NON-NLS-1$
    }
    addErrorMessage(out);
  }

  public void addErrorMessage(Throwable exception, String format, Object... params) {
    addErrorMessage(String.format(format, params), exception);
  }

  private String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  private int c_elementLevel = 0;

  public void debugElementOpen(AbstractElement elem) {
    debugElementOpen(elem, null);
  }

  public void debugElementOpen(AbstractElement elem, String msg) {
    String s = elem.getXMLOpenTag() + " " + elem.getNodeLocation(); //$NON-NLS-1$
    if (msg != null)
      s += "  [" + msg + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    String prefix = Text.getChars(' ', c_elementLevel * 2);
    if (elem instanceof FieldElement) {
      s += " value='" + ((FieldElement) elem).getEvaluatedExpression() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    addToDebugFile(prefix + s);
    if (elem.canChildren())
      c_elementLevel++;
  }

  public void debugTextnode(TextNode node) {
    String s = "TEXTNODE: \"" + node.getTesto() + "\"  " + node.getNodeLocation(); //$NON-NLS-1$ //$NON-NLS-2$
    String prefix = Text.getChars(' ', c_elementLevel * 2);
    addToDebugFile(prefix + s);
  }

  public void debugElementClose(AbstractElement elem) {
    if ( !elem.canChildren())
      return;
    String s = elem.getXMLCloseTag();
    c_elementLevel--;
    String prefix = Text.getChars(' ', c_elementLevel * 2);
    addToDebugFile(prefix + s);
  }

  public void addToDebugFile(String szVal) {
    try {
      if (c_outfile != null) {
        c_outfile.write("\n" + szVal); //$NON-NLS-1$
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @return the c_logger
   */
  public Logger getLogger() {
    return c_logger;
  }

  /**
   * @param logger
   *          the c_logger to set
   */
  public void setLogger(Logger logger) {
    c_logger = logger;
    setupLogger();
  }

  private void setupLogger() {
    if (c_logger != null) {
      if (isDebugMode()) {
        c_logger.setLevel(Level.DEBUG);
      } else {
        c_logger.setLevel(Level.INFO);
      }
    }
  }

  /**
   * Ritorna la quantità massima di record da caricare.
   * 
   * @see #setMaxNumRecords(int)
   * @return the c_maxNumRecords
   */
  public int getMaxNumRecords() {
    return c_maxNumRecords;
  }

  /**
   * Imposta la quantità massima di record da caricare.
   * 
   * @param numRecords
   *          the c_maxNumRecords to set
   */
  public void setMaxNumRecords(int numRecords) {
    c_maxNumRecords = numRecords;
  }

  /**
   * Ritorna lo stream su cui viene scritto il file di output. Tale stream può
   * venire utilizzato in ambiente servlet per essere copiato sull'output stream
   * della response.
   */
  public ByteArrayOutputStream getOutputStream() {
    return c_baOutputStream;
  }

  public boolean isAutoOutputFileName() {
    return m_autoOutputFileName;
  }

  /**
   * Imposta la generazione automatica del file di output. In questo modo, se
   * non si è impostato un nome di file di output (vedi
   * {@link #setOutputFileName(String)} ), viene preso il nome del sorgente e
   * cambiata l'estensione.
   * 
   * @param auto
   *          modalità di generazione automatica del nome del file di output
   */
  public void setAutoOutputFileName(boolean auto) {
    m_autoOutputFileName = auto;
  }

  /**************************** FLATTEN LISTA TO SIMPLELIST X STAMPA **************************************/

  /**
   * Riga corrente flatten da aggiungere alla lista flatten ( con flatten si
   * intende il fatto che la lista non sarà più annidata ma tutta su una sola
   * riga )
   */
  private Map<String, Object>       m_rigaCorrenteFlatten = null;
  private List<Map<String, Object>> m_listaFlatten        = new LinkedList<Map<String, Object>>();

  /**
   * Appiattimento di una Lista, metodo chiamato da
   * {@link #setDataFullList(Lista)}.
   * 
   * @param lista
   *          lista da appiattire
   * @return SimpleList appiattita; lo stato della lista ritornata è
   *         {@link PDCStatus#OPENED}
   * @throws CISException
   */
  private SimpleList flattenLista(Lista lista) throws CISException {
    for (int i = 0; i < lista.getPDCCount(); i++) {
      m_rigaCorrenteFlatten = null;
      PDC p = lista.get(i);
      flattenPDC(p, true);
    }
    SimpleList simpleList = new SimpleList(new MessHandler());
    for (Map<String, Object> rigaFlatten : m_listaFlatten) {
      simpleList.add(rigaFlatten);
    }
    simpleList.setStatus(PDCStatus.OPENED);
    return simpleList;
  }

  /**
   * Aggiunge alla riga corrente che sto appiattendo, i dati passati
   * nell'argomento. Se la riga corrente non esiste, la crea. I dati vengono
   * aggiunti alla riga corrente solo se non ci sono già, e il nome è sempre
   * aggiunto in versione minuscola.
   * 
   * @param riga
   *          mappa nome-valore dei dati da aggiungere alla riga corrente.
   */
  private void addToRigaCorrente(Map<String, Object> riga) {
    if (m_rigaCorrenteFlatten == null) {
      m_rigaCorrenteFlatten = new HashMap<String, Object>();
    }
    for (String name : riga.keySet()) {
      if ( !m_rigaCorrenteFlatten.containsKey(name.toLowerCase())) {
        m_rigaCorrenteFlatten.put(name.toLowerCase(), riga.get(name));
      }
    }
  }

  /**
   * Crea una nuova riga corrente e le aggiunge i dati passati nell'argomento.
   * Se precedentemente esisteva una riga corrente, prima di crearne una nuova,
   * questa viene aggiunta alla SimpleList appiattita (
   * {@linkplain #m_listaFlatten}).
   * 
   * @param riga
   *          dati da aggiungere
   */
  private void creaRigaCorrente(Map<String, Object> riga) {
    if (m_rigaCorrenteFlatten != null) {
      m_listaFlatten.add(m_rigaCorrenteFlatten);
    }
    m_rigaCorrenteFlatten = new HashMap<String, Object>();
    addToRigaCorrente(riga);
  }

  /**
   * Ritorna true sse esiste la riga corrente dei dati appiattiti
   */
  private boolean hoRigaCorrente() {
    return m_rigaCorrenteFlatten != null;
  }

  /**
   * Mappa di supporto durante la fase di appiattimento della lista.
   * <dl>
   * <dt>mappa esterna:</dt>
   * <dd>
   * chiave=hashcode del PDC <br>
   * valore=mappa delle posizioni delle sue relazioni</dd>
   * <dt>mappa interna:</dt>
   * <dd>
   * chiave=nome della relazione 1-N del PDC <br>
   * valore=oggetto PosizioneRelazione che descrive lo stato della relazione</dd>
   * </dl>
   * 
   */
  private Map<Integer, Map<String, PosizioneRelazione>> c_posizioniPDC = new HashMap<Integer, Map<String, PosizioneRelazione>>();

  /**
   * Classe di supporto per il processing delle relazioni 1-N durante la fase di
   * appiattimento della lista.
   */
  private class PosizioneRelazione {
    //String name = null;
    /**
     * posizione (0-based) del PDC che si sta processando di questa relazione
     * (che è 1-N)
     */
    int     posizione = -1;
    /**
     * flag che indica se sono stati processati tutti i PDC di questa relazione
     * 1-N
     */
    boolean hoFinito  = false;
  }

  /**
   * Siccome la flatten viene richiamata in modo ricorsivo anche dalle relazioni
   * 1-n
   * 
   * @param p
   * @param topLevel
   * @throws CISException
   */
  private void flattenPDC(PDC p, boolean topLevel) throws CISException {
    try {
      if (p == null) {
        return;
      }

      Map<String, PosizioneRelazione> posizioni = null;
      if ( (posizioni = c_posizioniPDC.get(new Integer(p.hashCode()))) == null) {
        posizioni = new HashMap<String, PosizioneRelazione>();
        c_posizioniPDC.put(new Integer(p.hashCode()), posizioni);
      }

      Map<String, Object> datiPDC = getMapFromAttr(p.getAttributi());
      if ( !hoRigaCorrente()) {
        creaRigaCorrente(datiPDC);
      } else {
        addToRigaCorrente(datiPDC);
      }
      // navigo in tutti i miei PDC discendenti nelle sole relazioni abilitate
      for (Relazione r : p.getRelazioni().values()) {
        if (r.isEnabled()) {
          if (r instanceof Relazione1_1) {
            Relazione1_1 r1 = (Relazione1_1) r;
            PDC istanza = r1.getPDC();
            flattenPDC(istanza, false);
          } else if (r instanceof Relazione1_N) {
            PosizioneRelazione pos = posizioni.get(r.getName());
            if (pos == null) {
              pos = new PosizioneRelazione();
              //pos.name = r.getName();
            }
            pos.posizione = pos.posizione + 1;
            Relazione1_N r1N = (Relazione1_N) r;
            // ci sono ancora figli???
            PDC figlioNesimo = r1N.getLista().get(pos.posizione);
            if (figlioNesimo != null) {
              flattenPDC(figlioNesimo, false);
            } else {
              pos.hoFinito = true;
            }
            posizioni.put(r.getName(), pos);
          }
        }
      }
      if (topLevel) {
        //Ciclo per creare correttamente le righe delle relazioni 1-n
        if ( !posizioni.isEmpty()) {
          for (PosizioneRelazione posrel : posizioni.values()) {
            if ( !posrel.hoFinito) {
              creaRigaCorrente(datiPDC);
              flattenPDC(p, true);
            }
          }
        } else {
          creaRigaCorrente(datiPDC);
        }
      }
    } catch (PDCException e) {
      e.printStackTrace();
    }
  }

  /**
   * Ritorna il valore di tutti gli attributi del PDC sotto forma di mappa
   * nome-valore.
   * 
   * @param a
   *          collection attributi da convertire in mappa
   * @return mappa nome-valore corrispondente alla collection di attributi
   * @throws CISException
   */
  private Map<String, Object> getMapFromAttr(Attributi a) throws CISException {
    Map<String, Object> mappaAttrs = new HashMap<String, Object>();
    for (Attributo attr : a.attribs()) {
      mappaAttrs.put(attr.getName(), attr.getObjValue());
    }
    return mappaAttrs;
  }

  /**
   * @return il messaggio da generare nel caso il report risulti senza dati.
   */
  public String getNoDataMessage() {
    return c_NoDataMessage;
  }

  /**
   * Imposta il messaggio da generare nel caso il report risulti senza dati. Da
   * usare in congiunzione con {@link #setShowMessageWithNoData(boolean)}.
   * 
   * @param message
   *          messaggio da mettere nel report nel caso di mancanza di dati.
   */
  public void setNoDataMessage(String message) {
    c_NoDataMessage = message;
  }

  /**
   * @return il comportamente da tenere nel caso il report risulti senza dati.
   */
  public boolean isShowMessageWithNoData() {
    return c_ShowMessageWithNoData;
  }

  /**
   * Imposta il comportamente da tenere nel caso il report risulti senza dati. <br>
   * 
   * @param showMessageWithNoData
   *          se true, indica che, in caso di mancanzi di dati, deve essere
   *          generato un report che ha come contenuto solo il messaggio dato da
   *          {@link #getNoDataMessage()}; se false, la mancanza di dati genera
   *          una exception e non viene geenrato nulla.
   * 
   */
  public void setShowMessageWithNoData(boolean showMessageWithNoData) {
    c_ShowMessageWithNoData = showMessageWithNoData;
  }

  /**
   * @return il documentTitle
   * @throws EvaluateException
   */
  public String getDocumentTitle() throws EvaluateException {
    if (c_documentTitle_symbol != null)
      return c_documentTitle_symbol.evaluate(this).toString();
    else
      return null;
  }

  /**
   * @param documentTitle
   *          the documentTitle to set
   * @throws ValidateException
   */
  public void setDocumentTitle(String documentTitle) throws ValidateException {
    c_documentTitle = documentTitle;
    c_documentTitle_symbol = parseTextExpression(c_documentTitle, XMLSchemaValidationHandler.A_DOCUMENTO_TITLE);
  }

  /**
   * @return il documentSubject
   * @throws EvaluateException
   */
  public String getDocumentSubject() throws EvaluateException {
    if (c_documentSubject_symbol != null)
      return c_documentSubject_symbol.evaluate(this).toString();
    else
      return null;
  }

  /**
   * @param documentSubject
   *          the documentSubject to set
   * @throws ValidateException
   */
  public void setDocumentSubject(String documentSubject) throws ValidateException {
    c_documentSubject = documentSubject;
    c_documentSubject_symbol = parseTextExpression(c_documentSubject, XMLSchemaValidationHandler.A_DOCUMENTO_SUBJECT);
  }

  /**
   * @return il documentAuthor
   * @throws EvaluateException
   */
  public String getDocumentAuthor() throws EvaluateException {
    if (c_documentAuthor_symbol != null)
      return c_documentAuthor_symbol.evaluate(this).toString();
    else
      return null;
  }

  /**
   * @param documentAuthor
   *          the documentAuthor to set
   * @throws ValidateException
   */
  public void setDocumentAuthor(String documentAuthor) throws ValidateException {
    c_documentAuthor = documentAuthor;
    c_documentAuthor_symbol = parseTextExpression(c_documentAuthor, XMLSchemaValidationHandler.A_DOCUMENTO_AUTHOR);
  }

  private Symbol parseTextExpression(String sourceExpr, String nome) throws ValidateException {
    if (sourceExpr != null) {
      GenericParser parser = GenericParser.getInstance(ParserType.TEXT_EXPRESSION, sourceExpr);
      try {
        return parser.parse();
      } catch (Exception e) {
        throw new ValidateException("L'espressione per '" + nome + "' non è valida: " + parser.getErrorDesc(), e); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    return null;
  }

  /**
   * Ritorna il character set che viene usato per la lettura dei file sorgenti
   * del report. Se questa proprietà non viene esplicitamente impostatat (vedi
   * {@link #setSourceEncoding(String)}), assumerà il valore di un'encoding
   * dedotta in automatico dalla lettura del file.
   * <p>
   * Tale deduzione non è sicura al 100%, per cui è preferibile settare a mano
   * l'encoding. Una codifica che funziona sempre, anche con deduzione
   * automatica, è {@link #CHARSET_UTF8}.
   * 
   * @return il sourceEncoding encoding (character set) usato per la lettura dei
   *         file sorgenti del report.
   */
  public String getSourceEncoding() {
    return c_sourceEncoding;
  }

  /**
   * Imposta l'encoding da usare per leggere il file del sorgente del report.
   * Anche eventuali file inclusi dovranno avere lo stesso encoding. Il nome
   * impostato deve essere un valido character set supportato dalla classe
   * java.nio.charset.Charset .
   * <p>
   * Sono state definite le seguenti costanti per comodità:
   * <ul>
   * <li>{@link #CHARSET_CP1252}</li>
   * <li>{@link #CHARSET_ISOLatin1}</li>
   * <li>{@link #CHARSET_UTF8}</li>
   * </ul>
   * 
   * @param sourceEncoding
   *          la codifica del file sorgente, se null resetta al platform default
   * @throws StampaException
   *           nel caso il character set passato non sia supportato
   */
  public void setSourceEncoding(String sourceEncoding) throws StampaException {
    
    if ( sourceEncoding != null && !Charset.isSupported(sourceEncoding)) {
      throw new StampaException("Il character set " + sourceEncoding + " non e' supportato"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    c_sourceEncoding = sourceEncoding;
  }

  /**
   * Ritorna il numero di versione del XML Schema in uso nel sorgente XML
   * attualmente caricato. <br/>
   * NOTA BENE: questo valore è indefinito finchè non si lancia il report con
   * {@link #creaReport()}.
   * 
   * @return numero di versione XML Schema del sorgente corrente oppure null se
   *         non c'è sorgente o non è stato lanciato il report.
   */
  public Integer getXMLSchemaVersion() {
    return c_XMLSchemaVersion;
  }

  /**
   * Indica se il sorgente corrente usa una versione "development" del XML
   * schema. Corrisponde all'attributo <tt>development</tt> del root tag
   * <tt>stampa</tt>
   * 
   * @return true sse si sta usando una versione development del XML Schema
   */
  public boolean isXMLSchemaDevelopment() {
    return c_XMLSchemaDevelopment;
  }

  /**
   * Nome del file che contiene i dati, impostato con la
   * {@link #setDataInputFile(String)}
   */
  public String getDataInputFile() {
    return c_dataInputFile;
  }

  /**
   * Permette di impostare un file che contiene tutti i dati del report. Il
   * formato è tab-delimited.
   * 
   * @param dataInputFile
   *          nome del file; se è un path assoluto usa quello, altrimenti usa
   *          l'algoritmo della ricerca risorse (vedi
   *          {@link #findResource(String)}).
   */
  public void setDataInputFile(String dataInputFile) {
    c_dataInputFile = dataInputFile;
  }

  private List<HashMap<String, Object>> readInputFile() throws DataException {
    String dataFile = findResource(getDataInputFile());
    if (dataFile == null) {
      throw new DataException("Non riesco a trovare il data file " + getDataInputFile()); //$NON-NLS-1$
    }
    TabbedFileReader tfr = new TabbedFileReader(this, dataFile);
    return tfr.load();
  }

  /*
   * (non-Javadoc)
   * @see
   * ciscoop.expressions.symbols.Evaluator#evaluate(ciscoop.expressions.symbols
   * .Symbol)
   */
  @Override
  public Object evaluate(Symbol symbol) throws ResolveException {
    if (symbol.isConstant()) {
      try {
        return resolveParameter(symbol);
      } catch (Exception e) {
        return "?" + symbol.getText() + "?"; //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    throw new ResolveException("Impossibile valutare il simbolo " + symbol); //$NON-NLS-1$
  }

  /**
   * Semplifica la vita al gc azzerando tutte le collection e i riferimenti a
   * oggetti.
   */
  public void destroy() {
    destroyObject(c_documentTitle_symbol);
    c_documentTitle_symbol = null;

    destroyObject(c_documentSubject_symbol);
    c_documentSubject_symbol = null;

    destroyObject(c_documentAuthor_symbol);
    c_documentAuthor_symbol = null;

    destroyMap(m_parameters);
    m_parameters = null;

    destroyMap(m_sysParameters);
    m_sysParameters = null;

    destroyMap(m_userClasses);
    m_userClasses = null;

    destroyMap(m_userInstances);
    m_userInstances = null;

    destroyMap(c_builtinColors);
    c_builtinColors = null;

    destroyMap(c_docPermissions);
    c_docPermissions = null;

    destroyMap(c_mappaColori);
    c_mappaColori = null;

    destroyMap(c_mappaFont);
    c_mappaFont = null;

    destroyMap(c_mappaIncludes);
    c_mappaIncludes = null;

    if (c_watermarks != null) {
      for (WatermarkElement we : c_watermarks) {
        we.destroy();
      }
      c_watermarks.clear();
      c_watermarks = null;
    }

    if (c_datalist != null) {
      for (HashMap<String, Object> r : c_datalist) {
        destroyMap(r);
      }
      c_datalist.clear();
      c_datalist = null;
    }

    getMainReport().destroy();
    
    c_database = null;

    destroyObject(c_defaultFont);
    c_defaultFont = null;

    c_docMargini = null;

    c_docPassword = null;

    c_docRettangolo = null;

    c_factoryElementi = null;

    destroyMap(m_rigaCorrenteFlatten);
    m_rigaCorrenteFlatten = null;
  }

  private void destroyObject(Destroyable o) {
    if (o != null)
      o.destroy();
  }

  private void destroyMap(Map<?, ?> m) {
    if (m != null)
      m.clear();
  }

  public boolean isDocumentBookmarksOpened() {
    return c_documentBookmarksOpened;
  }

}
