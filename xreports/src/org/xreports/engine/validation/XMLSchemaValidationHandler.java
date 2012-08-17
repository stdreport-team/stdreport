package org.xreports.engine.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.xreports.datagroup.GroupException;
import org.xreports.datagroup.GroupModel;
import org.xreports.engine.Parameter;
import org.xreports.engine.XReport;
import org.xreports.engine.StampaException;

import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Rettangolo;
import org.xreports.engine.output.StileCarattere;
import org.xreports.engine.source.AbstractElement;
import org.xreports.engine.source.AbstractNode;
import org.xreports.engine.source.BarcodeElement;
import org.xreports.engine.source.BookmarkElement;
import org.xreports.engine.source.CellElement;
import org.xreports.engine.source.ChartElement;
import org.xreports.engine.source.ChooseElement;
import org.xreports.engine.source.DateFieldElement;
import org.xreports.engine.source.FieldElement;
import org.xreports.engine.source.GroupElement;
import org.xreports.engine.source.IReportElement;
import org.xreports.engine.source.IfElement;
import org.xreports.engine.source.ImageElement;
import org.xreports.engine.source.LineElement;
import org.xreports.engine.source.MarginboxElement;
import org.xreports.engine.source.Margini;
import org.xreports.engine.source.NewpageElement;
import org.xreports.engine.source.NumberFieldElement;
import org.xreports.engine.source.OtherwiseElement;
import org.xreports.engine.source.PageElement;
import org.xreports.engine.source.PageFooter;
import org.xreports.engine.source.PageHeader;
import org.xreports.engine.source.RulersElement;
import org.xreports.engine.source.SpanElement;
import org.xreports.engine.source.TableElement;
import org.xreports.engine.source.TextElement;
import org.xreports.engine.source.TextNode;
import org.xreports.engine.source.WatermarkElement;
import org.xreports.engine.source.WhenElement;
import org.xreports.util.Text;

/**
 * Parser SAX SAX and DOM differences
 * 
 * Both SAX and DOM are used to parse the XML document. Both has advantages and
 * disadvantages and can be used in our programming depending on the situation.
 * SAX: 1. Parses node by node 2. Doesnt store the XML in memory 3. We cant
 * insert or delete a node 4. Top to bottom traversing DOM 1. Stores the entire
 * XML document into memory before processing 2. Occupies more memory 3. We can
 * insert or delete nodes 4. Traverse in any direction.
 * 
 * If we need to find a node and doesnt need to insert or delete we can go with
 * SAX itself otherwise DOM provided we have more memory.
 */
public final class XMLSchemaValidationHandler extends DefaultHandler {
  /**
   * Due maschere per riconoscere se l'elemento parsato fa parte dell'header o
   * del body della stampa
   */
  public static final int      HEADER_ELEMENTS_MASK     = 1024;
  public static final int      BODY_ELEMENTS_MASK       = 2048;

  /** !! ELEMENTI SFUSI !! **/
  /** Root Element */
  public static final String   ELEMENTO_STAMPA          = "stampa";
  public static final int      TP_STAMPA                = 1;

  /** Figli dell'elemento stampa */
  public static final int      TP_HEAD                  = 2;
  public static final int      TP_BODY                  = 3;
  public static final String   ELEMENTO_HEAD            = "head";
  public static final String   ELEMENTO_BODY            = "body";

  /** !! ELEMENTI HEADER !! **/
  /** Figli dell'elemento head */
  /** Utilizzano una numerazione partendo da HEADER_ELEMENTS_MASK + numero **/

  /** colori e fonts */
  public static final int      TP_COLORS                = HEADER_ELEMENTS_MASK + 3;
  public static final int      TP_FONTS                 = HEADER_ELEMENTS_MASK + 4;
  public static final String   ELEMENTO_COLORS          = "colors";
  public static final String   ELEMENTO_FONTS           = "fonts";

  /** Figli dell'elemento colors */
  public static final int      TP_COLOR                 = HEADER_ELEMENTS_MASK + 5;
  public static final String   ELEMENTO_COLOR           = "color";

  /** Figli dell'elemento fonts */
  public static final int      TP_FONT                  = HEADER_ELEMENTS_MASK + 6;
  public static final String   ELEMENTO_FONT            = "font";

  public static final int      TP_DOCUMENT              = HEADER_ELEMENTS_MASK + 7;
  public static final String   ELEMENTO_DOCUMENT        = "document";

  /** user classes */
  public static final String   ELEMENTO_USERCLASSES     = "userClasses";
  public static final String   ELEMENTO_USERCLASS       = "class";
  public static final int      TP_USERCLASSES           = HEADER_ELEMENTS_MASK + 8;
  public static final int      TP_USERCLASS             = HEADER_ELEMENTS_MASK + 9;

  /** Figli dell'elemento document */
  public static final int      TP_MARGINBOX             = HEADER_ELEMENTS_MASK + 10;
  public static final int      TP_RULERS                = HEADER_ELEMENTS_MASK + 11;
  public static final String   ELEMENTO_MARGINBOX       = "marginBox";
  public static final String   ELEMENTO_RULERS          = "rulers";

  public static final int      TP_QUERY                 = HEADER_ELEMENTS_MASK + 12;
  public static final String   ELEMENTO_QUERY           = "query";

  public static final int      TP_WATERMARK             = HEADER_ELEMENTS_MASK + 13;

  public static final int      TP_PARAMETERS            = HEADER_ELEMENTS_MASK + 14;
  public static final int      TP_PARAMETER             = HEADER_ELEMENTS_MASK + 15;
  public static final String   ELEMENTO_PARAMETERS      = "parameters";
  public static final String   ELEMENTO_PARAMETER       = "parameter";
  
  /** attributo textmode di body */
  public static final String   BODY_TEXTMODE            = "textmode";

  /** !! ELEMENTI BODY !! **/
  /** Figli dell'elemento body */
  /** Utilizzano una numerazione partendo da BODY_ELEMENTS_MASK + numero **/
  public static final int      TP_TABLE                 = BODY_ELEMENTS_MASK + 1;
  public static final int      TP_CELL                  = BODY_ELEMENTS_MASK + 2;
  public static final int      TP_GROUP                 = BODY_ELEMENTS_MASK + 3;
  public static final int      TP_LINE                  = BODY_ELEMENTS_MASK + 4;
  public static final int      TP_NEWPAGE               = BODY_ELEMENTS_MASK + 5;
  public static final int      TP_PAGEHEADER            = BODY_ELEMENTS_MASK + 6;
  public static final int      TP_PAGEFOOTER            = BODY_ELEMENTS_MASK + 7;
  public static final int      TP_CHART                 = BODY_ELEMENTS_MASK + 8;
  public static final int      TP_IF                    = BODY_ELEMENTS_MASK + 9;
  public static final int      TP_CHOOSE                = BODY_ELEMENTS_MASK + 10;
  public static final int      TP_WHEN                  = BODY_ELEMENTS_MASK + 11;
  public static final int      TP_OTHERWISE             = BODY_ELEMENTS_MASK + 12;
  public static final int      TP_BOOKMARK              = BODY_ELEMENTS_MASK + 13;

  public static final String   ELEMENTO_TABLE           = "table";
  public static final String   ELEMENTO_CELL            = "cell";
  public static final String   ELEMENTO_GROUP           = "group";
  public static final String   ELEMENTO_LINE            = "line";
  public static final String   ELEMENTO_NEWPAGE         = "newpage";
  public static final String   ELEMENTO_PAGEHEADER      = "pageHeader";
  public static final String   ELEMENTO_PAGEFOOTER      = "pageFooter";
  public static final String   ELEMENTO_CHART           = "chart";
  public static final String   ELEMENTO_IF              = "if";
  public static final String   ELEMENTO_CHOOSE          = "choose";
  public static final String   ELEMENTO_WHEN            = "when";
  public static final String   ELEMENTO_OTHERWISE       = "otherwise";
  public static final String   ELEMENTO_WATERMARK       = "watermark";
  public static final String   ELEMENTO_BOOKMARK        = "bookmark";

  /** Gruppo Campi field */
  public static final int      TP_DATE                  = BODY_ELEMENTS_MASK + 120;
  public static final int      TP_NUMBER                = BODY_ELEMENTS_MASK + 121;
  public static final int      TP_FIELD                 = BODY_ELEMENTS_MASK + 122;
  public static final int      TP_TEXT                  = BODY_ELEMENTS_MASK + 123;
  public static final int      TP_IMAGE                 = BODY_ELEMENTS_MASK + 124;
  public static final int      TP_SPAN                  = BODY_ELEMENTS_MASK + 125;
  public static final int      TP_BARCODE               = BODY_ELEMENTS_MASK + 126;
  public static final String   ELEMENTO_DATE            = "date";
  public static final String   ELEMENTO_NUMBER          = "number";
  public static final String   ELEMENTO_FIELD           = "field";
  public static final String   ELEMENTO_TEXT            = "text";
  public static final String   ELEMENTO_IMAGE           = "image";
  public static final String   ELEMENTO_SPAN            = "span";
  public static final String   ELEMENTO_BARCODE         = "barcode";

  /** !! ATTRIBUTI !! **/
  /** Attributi color */
  private static final String  A_COLOR_NAME             = "name";
  private static final String  A_COLOR_R                = "r";
  private static final String  A_COLOR_G                = "g";
  private static final String  A_COLOR_B                = "b";
  private static final String  A_COLOR_RGB              = "rgb";
  private static final String  A_COLOR_RGBHEX           = "rgbh";

  /** Attributi font */
  public static final String   A_FONT_NAME              = "name";
  public static final String   A_FONT_FAMILY            = "family";
  public static final String   A_FONT_SRC               = "src";
  public static final String   A_FONT_SIZE              = "size";
  public static final String   A_FONT_BOLD              = "bold";
  public static final String   A_FONT_ITALIC            = "italic";
  public static final String   A_FONT_UNDERLINE         = "underline";
  public static final String   A_FONT_DEFAULT           = "default";
  public static final String   A_FONT_REFCOLOR          = "refColor";

  /** Attributi userclass */
  public static final String   A_USERCLASS_NAME         = "name";
  public static final String   A_USERCLASS_VALUE        = "value";

  /** Attributi parameter */
  public static final String   A_PARAM_NAME             = "name";
  public static final String   A_PARAM_CLASS            = "class";
  public static final String   A_PARAM_DEFAULT          = "defaultValue";
  public static final String   A_PARAM_REQUIRED         = "required";
  
  /** Attributi documento */
  public static final String   A_DOCUMENTO_FOGLIO       = "foglio";
  public static final String   A_DOCUMENTO_ORIENTAMENTO = "orientamento";
  public static final String   A_DOCUMENTO_MARGINI      = "margini";
  public static final String   A_DOCUMENTO_TITLE        = "title";
  public static final String   A_DOCUMENTO_SUBJECT      = "subject";
  public static final String   A_DOCUMENTO_AUTHOR       = "author";
  public static final String   A_DOCUMENTO_ALLOWPRINT   = "allowPrint";
  public static final String   A_DOCUMENTO_ALLOWCOPY    = "allowCopy";
  public static final String   A_DOCUMENTO_ALLOWMODIFY  = "allowModify";
  public static final String   A_DOCUMENTO_BKMOPEN      = "bookmarksOpened";

  public static final String   PERM_PRINTING            = "PRINT";

  private DocumentAttributes   c_document               = null;

  private XReport              c_stampa                 = null;
  /** true se sto processando gli elementi dentro body */
  private boolean              c_isBody;
  /** true se sto processando il testo in query */
  private boolean              c_isQuery;
  private String               c_queryText;
  private String               c_watermarkText;

  /** associazione nome elemento --> codice */
  private Map<String, Integer> c_mappaElementi          = null;
  /** associazione codice --> nome elemento */
  private Map<Integer, String> c_mappaCodici            = null;

  /**
   * mappa degli elementi del sorgente: un elemento è qui se ha almeno
   * un'occorrenza nel sorgente
   */
  private Set<Integer>         c_foundElems             = null;

  /** indica che textmode="text" nell'elemento body */
  private boolean              c_body_textmode          = true;

  /**
   * utilizzato per creare il nome del gruppo di default per ogni RootModel.
   * 
   * */
  public static final String   DEFAULTGROUP_POSTFIX     = "_radice";

  /**
   * E' il gruppo di default corrente. <br/>
   * In pratica ce n'è uno per la query principale del report, più uno per ogni
   * subreport.
   * */
  private GroupElement         c_currentDefaultGroup    = null;
  private Deque<GroupElement>  c_stackDefaultGroups     = null;

  /**
   * E' l'elemento corrente, corrispondente all'ultimo tag aperto incontrato
   * durante il parsing.
   */
  private IReportElement       c_currentElem            = null;

  /**
   * GroupModel corrente: usato per aggiungere i field che si incontrano durante
   * il parsing
   */
  private GroupModel           c_currentModel           = null;

  private GroupElement         c_rootGroupElement;

  /**
   * stack con i GroupModel modificato quando si incontrano aperture o chiusure
   * del tag <tt>group</tt>.
   */
  private Deque<GroupModel>    c_stackGroupModels       = null;

  private Locator              m_locator;

  private String               c_currentIncludeFile     = null;
  private String               c_currentTagName         = "";
  private int                  c_currentLineNum         = 0;

  public XMLSchemaValidationHandler(XReport report) {
    c_stampa = report;
    try {
      c_document = new DocumentAttributes();
    } catch (ValidateException e) {
      //ignoro exception del costruttore
      e.printStackTrace();
    }
  }

  public GroupElement getRootGroupElement() {
    return c_rootGroupElement;
  }

  /** Inizio parsing del Documento XML */
  @Override
  public void startDocument() throws SAXException {
    super.startDocument();

    try {
      //Creiamo gli stack che mi servono durante il parsing per mantenere la struttura degli elementi da generare
      // e la struttura dei gruppi
      c_stackGroupModels = new ArrayDeque<GroupModel>();
      c_stackDefaultGroups = new ArrayDeque<GroupElement>();

      try {
        c_rootGroupElement = new GroupElement(DEFAULTGROUP_POSTFIX, c_stampa.getRootModel());
        c_rootGroupElement.setDebugData(c_stampa.isDebugData());
        pushDefaultGroup(c_rootGroupElement);
        pushContext(c_rootGroupElement, c_stampa.getRootModel());
      } catch (Exception e) {
        throw new SAXException(e);
      }

      initElementsMap();

      c_foundElems = new HashSet<Integer>();
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      SAXException saxe = new SAXException("Errore grave in inizializzazione parsing: " + e.toString());
      throw saxe;
    }
  }

  private void initElementsMap() {
    c_mappaElementi = new HashMap<String, Integer>();
    c_mappaElementi.put(ELEMENTO_BODY, new Integer(TP_BODY));
    c_mappaElementi.put(ELEMENTO_COLOR, new Integer(TP_COLOR));
    c_mappaElementi.put(ELEMENTO_COLORS, new Integer(TP_COLORS));
    c_mappaElementi.put(ELEMENTO_DOCUMENT, new Integer(TP_DOCUMENT));
    c_mappaElementi.put(ELEMENTO_QUERY, new Integer(TP_QUERY));
    c_mappaElementi.put(ELEMENTO_FONT, new Integer(TP_FONT));
    c_mappaElementi.put(ELEMENTO_FONTS, new Integer(TP_FONTS));
    c_mappaElementi.put(ELEMENTO_GROUP, new Integer(TP_GROUP));
    c_mappaElementi.put(ELEMENTO_TABLE, new Integer(TP_TABLE));
    c_mappaElementi.put(ELEMENTO_CELL, new Integer(TP_CELL));
    c_mappaElementi.put(ELEMENTO_HEAD, new Integer(TP_HEAD));
    c_mappaElementi.put(ELEMENTO_STAMPA, new Integer(TP_STAMPA));
    c_mappaElementi.put(ELEMENTO_FIELD, new Integer(TP_FIELD));
    c_mappaElementi.put(ELEMENTO_SPAN, new Integer(TP_SPAN));
    c_mappaElementi.put(ELEMENTO_NUMBER, new Integer(TP_NUMBER));
    c_mappaElementi.put(ELEMENTO_DATE, new Integer(TP_DATE));
    c_mappaElementi.put(ELEMENTO_BARCODE, new Integer(TP_BARCODE));
    c_mappaElementi.put(ELEMENTO_LINE, new Integer(TP_LINE));
    c_mappaElementi.put(ELEMENTO_TEXT, new Integer(TP_TEXT));
    c_mappaElementi.put(ELEMENTO_IMAGE, new Integer(TP_IMAGE));
    c_mappaElementi.put(ELEMENTO_NEWPAGE, new Integer(TP_NEWPAGE));
    c_mappaElementi.put(ELEMENTO_USERCLASSES, new Integer(TP_USERCLASSES));
    c_mappaElementi.put(ELEMENTO_USERCLASS, new Integer(TP_USERCLASS));
    c_mappaElementi.put(ELEMENTO_PARAMETERS, new Integer(TP_PARAMETERS));
    c_mappaElementi.put(ELEMENTO_PARAMETER, new Integer(TP_PARAMETER));
    c_mappaElementi.put(ELEMENTO_PAGEFOOTER, new Integer(TP_PAGEFOOTER));
    c_mappaElementi.put(ELEMENTO_PAGEHEADER, new Integer(TP_PAGEHEADER));
    c_mappaElementi.put(ELEMENTO_CHART, new Integer(TP_CHART));
    c_mappaElementi.put(ELEMENTO_IF, new Integer(TP_IF));
    c_mappaElementi.put(ELEMENTO_CHOOSE, new Integer(TP_CHOOSE));
    c_mappaElementi.put(ELEMENTO_WHEN, new Integer(TP_WHEN));
    c_mappaElementi.put(ELEMENTO_OTHERWISE, new Integer(TP_OTHERWISE));
    c_mappaElementi.put(ELEMENTO_MARGINBOX, new Integer(TP_MARGINBOX));
    c_mappaElementi.put(ELEMENTO_RULERS, new Integer(TP_RULERS));
    c_mappaElementi.put(ELEMENTO_WATERMARK, new Integer(TP_WATERMARK));
    c_mappaElementi.put(ELEMENTO_BOOKMARK, new Integer(TP_BOOKMARK));

    c_mappaCodici = new HashMap<Integer, String>();
    for (Entry<String, Integer> entry : c_mappaElementi.entrySet()) {
      c_mappaCodici.put(entry.getValue(), entry.getKey());
    }

  }

  private void pushDefaultGroup(GroupElement groupElem) {
    c_currentDefaultGroup = groupElem;
    c_stackDefaultGroups.push(groupElem);
  }

  //  private void popDefaultGroup() {
  //    c_stackDefaultGroups.pop();
  //    c_currentDefaultGroup = c_stackDefaultGroups.getFirst();
  //  }

  @Override
  public void endDocument() throws SAXException {
    super.endDocument();
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    m_locator = locator;
  }

  /**
   * Se l'elemento corrente è un TextElement fittizio, lo chiude levandolo dallo
   * stack
   * 
   * @param valElemento
   */
  private void chiudiTextFittizio(int valElemento) {
    if (c_currentElem instanceof TextElement) {
      TextElement text = (TextElement) c_currentElem;
      if (text.isFittizio()) {
        if (isContentElement(valElemento) && isBlockElement(valElemento)) {
          fineElemento("text");
        }
      }
    }
  }

  /**
   * Ritorna true se è un elemento block level
   * 
   * @param valElemento
   *          codice elemento
   */
  private boolean isBlockElement(int valElemento) {
    if (valElemento == TP_IMAGE || valElemento == TP_FIELD || valElemento == TP_DATE || valElemento == TP_NUMBER
        || valElemento == TP_BARCODE || valElemento == TP_SPAN  || valElemento == TP_BOOKMARK) {
      return false;
    }
    return true;
  }

  /**
   * Ritorna true se è un elemento cocnreto che incide sul flusso della pagina
   * 
   * @param valElemento
   *          codice elemento
   */
  private boolean isContentElement(int valElemento) {
    if (valElemento == TP_CHOOSE || valElemento == TP_IF || valElemento == TP_MARGINBOX || valElemento == TP_OTHERWISE
        || valElemento == TP_RULERS || valElemento == TP_WHEN || valElemento == TP_WATERMARK) {
      return false;
    }
    return true;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);

    int line = 0, col = 0;
    try {
      Integer valore = c_mappaElementi.get(localName);
      if (m_locator != null) {
        line = m_locator.getLineNumber();
        col = m_locator.getColumnNumber();
      }

      c_currentIncludeFile = c_stampa.getIncludeFile(line);
      line = c_stampa.getOriginalLineNumber(line);
      c_currentTagName = localName;
      c_currentLineNum = line;
      if (valore == null) {
        throw new SAXException("Elemento non previsto : " + localName);
      }
      chiudiTextFittizio(valore.intValue());
      AbstractElement element = null;
      switch (valore.intValue()) {
        case TP_HEAD: {
          c_isBody = false;
        }
          break;
        case TP_BODY: {
          elaboraBody(attributes, line, col);
        }
          break;
        case TP_USERCLASSES:
        case TP_PARAMETERS:
        case TP_FONTS:
        case TP_COLORS: {
          checkUnicity(valore);
        }
          break;
        case TP_COLOR: {
          parseColorAttributes(attributes);
        }
          break;
        case TP_FONT: {
          parseFontAttributes(attributes);
        }
          break;
        case TP_DOCUMENT: {
          parseDocumentAttributes(attributes);
        }
          break;
        case TP_QUERY: {
          elaboraQuery(attributes);
        }
          break;
        case TP_TABLE: {
          element = elaboraTable(attributes, line, col);
        }
          break;
        case TP_CELL: {
          element = elaboraCella(attributes, line, col);
        }
          break;
        case TP_GROUP: {
          element = elaboraGruppo(attributes, line, col);
        }
          break;
        case TP_LINE: {
          element = elaboraLine(attributes, line, col);
        }
          break;
        case TP_CHART: {
          element = elaboraChart(attributes, line, col);
        }
          break;
        case TP_MARGINBOX: {
          element = elaboraMarginbox(attributes, line, col);
        }
          break;
        case TP_RULERS: {
          element = elaboraRulers(attributes, line, col);
        }
          break;
        case TP_WATERMARK: {
          element = elaboraWatermark(attributes, line, col);
        }
          break;
        case TP_IF: {
          element = elaboraIf(attributes, line, col);
        }
          break;
        case TP_CHOOSE: {
          element = elaboraChoose(attributes, line, col);
        }
          break;
        case TP_WHEN: {
          element = elaboraWhen(attributes, line, col);
        }
          break;
        case TP_OTHERWISE: {
          element = elaboraOtherwise(attributes, line, col);
        }
          break;
        case TP_SPAN: {
          element = elaboraSpan(attributes, line, col);
        }
          break;
        case TP_FIELD: {
          element = elaboraField(attributes, TP_FIELD, line, col);
        }
          break;
        case TP_BARCODE: {
          element = elaboraField(attributes, TP_BARCODE, line, col);
        }
          break;
        case TP_NUMBER: {
          element = elaboraField(attributes, TP_NUMBER, line, col);
        }
          break;
        case TP_DATE: {
          element = elaboraField(attributes, TP_DATE, line, col);
        }
          break;
        case TP_USERCLASS: {
          elaboraUserClass(attributes, line, col);
        }
          break;
        case TP_PARAMETER: {
          elaboraParameter(attributes, line, col);
        }
          break;
        case TP_NEWPAGE: {
          element = elaboraNewpage(attributes, line, col);
        }
          break;
        case TP_IMAGE: {
          element = elaboraImage(attributes, line, col);
        }
          break;
        case TP_PAGEHEADER: {
          element = elaboraPageHeader(attributes, line, col);
        }
          break;
        case TP_PAGEFOOTER: {
          element = elaboraPageFooter(attributes, line, col);
        }
          break;
        case TP_TEXT: {
          element = elaboraText(attributes, line, col);
        }
          break;
        case TP_BOOKMARK: {
          element = elaboraBookmark(attributes, line, col);
        }
          break;
      }
      if (element != null)
        element.setIncludeFile(c_currentIncludeFile);
      c_foundElems.add(valore);
    } catch (SAXException saxe) {
      throw saxe;
    } catch (Exception e) {
      SAXException saxe = new SAXException("Errore grave in elaborazione elemento " + c_currentTagName + ", linea " + line, e);
      throw saxe;
    }
  }

  /**
   * @param attributes
   * @param line
   * @param col
   * @return
   */
  private AbstractElement elaboraIf(Attributes attributes, int line, int col) throws ValidateException {
    IfElement elemIf = new IfElement(attributes, line, col);
    elemIf.setDebugData(c_stampa.isDebugData());
    pushContext(elemIf);
    return elemIf;
  }

  /**
   * @param attributes
   * @param line
   * @param col
   * @return
   */
  private AbstractElement elaboraChoose(Attributes attributes, int line, int col) throws ValidateException {
    ChooseElement elemChoose = new ChooseElement(attributes, line, col);
    elemChoose.setDebugData(c_stampa.isDebugData());
    pushContext(elemChoose);
    return elemChoose;
  }

  /**
   * @param attributes
   * @param line
   * @param col
   * @return
   */
  private AbstractElement elaboraWhen(Attributes attributes, int line, int col) throws ValidateException {
    WhenElement elemWhen = new WhenElement(attributes, line, col);
    elemWhen.setDebugData(c_stampa.isDebugData());
    pushContext(elemWhen);
    return elemWhen;
  }

  /**
   * @param attributes
   * @param line
   * @param col
   * @return
   */
  private AbstractElement elaboraOtherwise(Attributes attributes, int line, int col) throws ValidateException {
    OtherwiseElement elemOtherwise = new OtherwiseElement(attributes, line, col);
    elemOtherwise.setDebugData(c_stampa.isDebugData());
    pushContext(elemOtherwise);
    return elemOtherwise;
  }

  /***/
  private PageFooter elaboraPageFooter(Attributes attributes, int line, int col) throws ValidateException {
    PageFooter elemFooter = new PageFooter(attributes, line, col);
    //elemFooter.setRefFontName(c_stampa.getDefaultFont().getName());    
    c_currentElem = elemFooter;

    c_document.setPageFooter(elemFooter);
    return elemFooter;
  }

  /**
   * @throws ValidateException
   */
  private PageHeader elaboraPageHeader(Attributes attributes, int line, int col) throws ValidateException {
    PageHeader elemHeader = new PageHeader(attributes, line, col);
    //    elemHeader.setRefFontName(c_stampa.getDefaultFont().getName());    
    c_currentElem = elemHeader;

    c_document.setPageHeader(elemHeader);
    return elemHeader;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
    Integer valore = c_mappaElementi.get(localName);
    chiudiTextFittizio(valore.intValue());

    fineElemento(localName);

    if (localName.equalsIgnoreCase(ELEMENTO_WATERMARK) && Text.isValue(c_watermarkText)) {
      c_watermarkText = purgeSpaces(c_watermarkText, true);
      try {
        c_document.getLastWatermark().setAttributeValue(WatermarkElement.ATTRIB_TEXT, c_watermarkText);
      } catch (ValidateException e) {
      }
    }

    c_currentTagName = "";
    c_isQuery = false;
  }

  /**
   * Gestione della chiusura di un tag
   * 
   * @param localName
   *          nome del tag
   */
  private void fineElemento(String localName) {
    Integer valoreElemento = c_mappaElementi.get(localName);

    if (valoreElemento.intValue() == TP_PAGEHEADER) {
      c_currentElem = c_currentDefaultGroup;
      c_stackGroupModels.clear();
      c_stackGroupModels.push(c_currentModel);
      return;
    }

    int valMask = (valoreElemento.intValue() & BODY_ELEMENTS_MASK);
    //Se on è uguale a 0 significa che è un elemento che fa parte del body
    if (valMask != 0) {
      //      switch (valoreElemento.intValue()) {
      //        case TP_TEXT: {
      //        }
      //          break;
      //        case TP_GROUP: {
      //          //popGruppo();
      //        }
      //          break;
      //      }
      popContext();
    }
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    super.characters(ch, start, length);

    if ( !c_isBody && !c_isQuery && !c_currentTagName.equalsIgnoreCase(ELEMENTO_WATERMARK)) {
      //il testo fuori dai tag body/query/watermark lo ignoro
      return;
    }

    if ( c_currentElem instanceof ChooseElement || c_currentElem instanceof TableElement) {
      //il testo figlio dei tag choose/table lo ignoro
      return;
    }
    
    int line = 0, col = 0;
    if (m_locator != null) {
      line = m_locator.getLineNumber();
      col = m_locator.getColumnNumber();
      line = c_stampa.getOriginalLineNumber(line);
    }

    try {
      char[] caratteri = Arrays.copyOfRange(ch, start, start + length);
      String stringa = String.valueOf(caratteri);
      if (c_isQuery) {
        addQueryText(stringa);
        return;
      }
      if (c_currentTagName.equalsIgnoreCase(ELEMENTO_WATERMARK)) {
        c_watermarkText += stringa;
        return;
      }
      if (c_currentElem instanceof PageElement) {
        if (Text.isOnlySpace(stringa)) {
          return;
        }
      }
      if (c_currentElem instanceof GroupElement || c_currentElem instanceof CellElement || c_currentElem instanceof TableElement
          || c_currentElem instanceof PageElement || c_currentElem instanceof WhenElement || c_currentElem instanceof OtherwiseElement
          || c_currentElem instanceof IfElement
          || (c_currentElem instanceof TextElement && ((TextElement) c_currentElem).isFittizio())) {
        //gestisco in modo speciale i blocchi dentro GROUP/CELL/TABLE/PAGEHEADER/PAGEFOOTER/TEXT fittizio:
        //se è il primo figlio lo ignoro se ha solo spazi;
        //se non è il primo figlio, trimmo la spaziatura iniziale rispettando però la qta di righe
        if (c_body_textmode) {
          //text mode: lascio così com'è
        } else {
          //html mode
          boolean startSpace = startWithSpace(stringa);
          boolean endSpace = endWithSpace(stringa);
          stringa = purgeSpaces(stringa, false);
          if (stringa.length() > 0) {
          if (startSpace) {
            stringa = ' ' + stringa;
          }
          if (endSpace) {
            stringa = stringa + ' ';
          }

        }
      }
      }

      c_currentLineNum = line;
      c_currentTagName = "charblock";

      if (stringa.length() > 0) {
        //elaboro la stringa risultante solo se ha qualche carattere
        elaboraCharacters(stringa, line, col);
      }
    } catch (ValidateException e) {
      throw new SAXException(e.toString());
    }
  }

  //  private boolean isOnlySpace(String s) {
  //    return searchFirstNonspaceChar(s) == -1;
  //  }

  //  /**
  //   * Cerca l'indice del primo carattere considerato non spazio
  //   * 
  //   * @param s
  //   *          stringa su cui cercare
  //   * @return indice del primo carattere non spazio (quindi >= 0) oppure -1 se
  //   *         non trovato
  //   */
  //  private int searchFirstNonspaceChar(String s) {
  //    int i = 0;
  //    for (i = 0; i < s.length(); i++) {
  //      if ( !Character.isWhitespace(s.charAt(i)))
  //        break;
  //    }
  //    if (i >= s.length())
  //      return -1; //non trovato
  //
  //    return i;
  //  }

  //  /**
  //   * Restituisce una stringa <tt>s</tt> in cui tutti i caratteri di spaziatura
  //   * iniziali (spazio, tab, newline,...) vengono levati.
  //   * 
  //   * @param s
  //   *          stringa da modificare
  //   * @return stringa modificata
  //   */
  //  private String trimLeft(String s) {
  //    int i = searchFirstNonspaceChar(s);
  //    for (i = 0; i < s.length(); i++) {
  //      if ( !Character.isWhitespace(s.charAt(i)))
  //        break;
  //    }
  //    if (i >= 0)
  //      s = s.substring(i);
  //
  //    return s;
  //  }

  /**
   * Restituisce una stringa da <tt>s</tt> in cui tutti i caratteri di
   * spaziatura sono collassati ad uno solo. Se inoltre ci sono spazi iniziali e
   * finali, vengono completamente rimossi.
   * 
   * @param s
   *          stringa da modificare
   * @return stringa modificata
   */
  private String purgeSpaces(String s, boolean preserveNewlines) {
    StringBuilder sb = new StringBuilder();
    boolean bSpaced = false;
    for (int i = 0; i < s.length(); i++) {
      if (Character.isWhitespace(s.charAt(i)) && ( !preserveNewlines || s.charAt(i) != '\n')) {
        if (i > 0) {
          if ( !bSpaced) {
            bSpaced = true;
            sb.append(' ');
          }
        }
      } else {
        bSpaced = false;
        sb.append(s.charAt(i));
      }
    }
    return sb.toString().trim();
  }

  /**
   * Ritorna true sse la stringa passata inizia con un carattere spaziatore
   * 
   * @param s
   *          stringa da controllare, può essere anche null o vuota
   */
  private boolean startWithSpace(String s) {
    if (s == null || s.length() == 0)
      return false;

    return Character.isWhitespace(s.charAt(0));
  }

  /**
   * Ritorna true sse la stringa passata finisce con un carattere spaziatore
   * 
   * @param s
   *          stringa da controllare, può essere anche null o vuota
   */
  private boolean endWithSpace(String s) {
    if (s == null || s.length() == 0)
      return false;

    return Character.isWhitespace(s.charAt(s.length() - 1));
  }

  //  /**
  //   * Restituisce una stringa <tt>s</tt> in cui la prima riga viene eliminata
  //   * se composta solo di spazi (spazio, tab,...).
  //   * 
  //   * Ad esempio <tt>"   \n \nciao"</tt>  diventa <tt>" \nciao"</tt> . 
  //   * 
  //   * @param s
  //   *          stringa da modificare
  //   * @return stringa modificata
  //   */
  //  private String trimFirstLine(String s) {
  //    int ns = searchFirstNonspaceChar(s);
  //    if (ns < 0)
  //      ns = 999999;
  //    int i = s.indexOf('\n');
  //    if (i >= 0 && i < ns)
  //      return s.substring(i + 1);
  //    return s;
  //  }

  //  /**
  //   * Restituisce una stringa <tt>s</tt> modificata così:
  //   * <ul>
  //   * <li>leva tutti i caratteri di spaziatura iniziali da <tt>s</tt> (spazio,
  //   * tab, newline,...)</li>
  //   * <li>se fra l'ultimo newline e il primo carattere non spazio c'era almeno un
  //   * carattere spaziatore, lo sostituisce con un singolo spazio</li>
  //   * </ul>
  //   * 
  //   * @param s
  //   *          stringa da modificare
  //   * @return stringa modificata
  //   */
  //  private String trimLeftWithSpace(String s) {
  //    int i = searchFirstNonspaceChar(s);
  //    if (i == -1) {
  //      i = s.length();
  //    }
  //    boolean bSpace = false;
  //    for (int k = i - 1; k >= 0; k--) {
  //      char c = s.charAt(k);
  //      if (Character.isSpaceChar((int) c) || c == '\t') {
  //        bSpace = true;
  //        break;
  //      } else {
  //        if (c == '\n' || c == '\r') {
  //          break;
  //        }
  //      }
  //    }
  //    if (i >= 0)
  //      s = s.substring(i);
  //    if (bSpace)
  //      s = " " + s;
  //
  //    return s;
  //  }

  @Override
  public void error(SAXParseException e) throws SAXParseException {
    final String prefix = "cvc-complex-type";
    String msg = e.getMessage();
    if (msg.startsWith(prefix)) {
      //cerco il primo ':' dopo il prefisso e scarto fino a lì
      int i = msg.indexOf(':', prefix.length());
      if (i < 0) {
        i = prefix.length();
      }
      msg = msg.substring(i + 1).trim();
    }
    int origLine = c_stampa.getOriginalLineNumber(e.getLineNumber());
    String includeFile = c_stampa.getIncludeFile(e.getLineNumber());
    if (includeFile == null) {
      includeFile = "";
    }
    else {
      includeFile = " (" + includeFile + ")";
    }
    String finaleMessage = "Errore in linea " + origLine + ", colonna " + e.getColumnNumber() + includeFile + ": " + msg;
    throw new SAXParseException(finaleMessage, null);
  }

  @Override
  public void warning(SAXParseException e) throws SAXParseException {
    c_stampa.addInfoMessage("Warning linea " + e.getLineNumber() + ": " + e.toString());
    throw e;
  }

  /*********************/
  /** METODI AUSILIARI */
  /*********************/

  private void elaboraBody(Attributes attrs, int line, int col) throws StampaException {
    c_isBody = true;
    //gestione attributo textmode: il default è "text"
    String textMode = readAttr(BODY_TEXTMODE, attrs);
    if (Text.isValueEq(textMode, "html"))
      c_body_textmode = false;
  }

  //DEFINE: figli
  /**
   * Nodo Color
   * 
   * @throws StampaException
   */
  private void parseColorAttributes(Attributes colorAttrs) throws StampaException {
    String szName = null;
    int red = 0, green = 0, blue = 0;
    boolean separateComponents = false;
    String szValoreAttributo = null;
    if ( (szValoreAttributo = readAttr(A_COLOR_NAME, colorAttrs)) != null) {
      szName = szValoreAttributo;
    }
    if ( (szValoreAttributo = readAttr(A_COLOR_R, colorAttrs)) != null) {
      red = Integer.parseInt(szValoreAttributo);
      separateComponents = true;
    }
    if ( (szValoreAttributo = readAttr(A_COLOR_G, colorAttrs)) != null) {
      green = Integer.parseInt(szValoreAttributo);
      separateComponents = true;
    }
    if ( (szValoreAttributo = readAttr(A_COLOR_B, colorAttrs)) != null) {
      blue = Integer.parseInt(szValoreAttributo);
      separateComponents = true;
    }
    String szRGB = readAttr(A_COLOR_RGB, colorAttrs);
    String szRGBH = readAttr(A_COLOR_RGBHEX, colorAttrs);
    if ( (szRGB != null || szRGBH != null) && separateComponents) {
      throw new ValidateException("Attributi colore incompatibili, controlla sintassi");
    }
    if (szRGB != null && szRGBH != null) {
      throw new ValidateException("Attributi colore incompatibili, non puoi usare si rgb che rgbh");
    }
    if (szRGB == null && szRGBH == null && !separateComponents) {
      throw new ValidateException("Manca la specifica del colore, linea " + m_locator.getLineNumber());
    }

    if (separateComponents) {
      c_stampa.addColor(new Colore(szName, red, green, blue));
    } else {
      if (szRGB != null)
        c_stampa.addColor(Colore.getInstance(szName, szRGB, m_locator.getLineNumber()));
      else
        c_stampa.addColor(Colore.getInstance(szName, szRGBH, m_locator.getLineNumber()));
    }
  }

  /**
   * Nodo "query"
   * 
   * @param documentAttrs
   * @throws ValidateException
   */
  private void elaboraQuery(Attributes documentAttrs) throws ValidateException {
    checkUnicity(TP_QUERY);
    c_isQuery = true;
    c_queryText = "";
  }

  /**
   * Emette exception se l'elemento passato è già stato incontrato nel sorgente.
   * 
   *  Questo metodo è necessario in quanto non è possibile con XML Schema definire che un elemento ha come figli
   *  vari altrie elementi di cui alcuni possono camparire più di una volta e altri solo 1 volta. In questi casi nel XML Schema
   *  ho messo che tutti gli elementi figli possono comparire più volte, ma poi il test di unicità degli elementi che possono comparire
   *  al max 1 volta, lo faccio qui.
   *  
   * @param elemType tipo dell'elemento
   * @throws ValidateException in caso l'elemento sia già presente nel sorgente in una riga precedente
   */
  private void checkUnicity(Integer elemType) throws ValidateException {
    if (c_foundElems.contains(elemType))
      throw new ValidateException("L'elemento '" + c_mappaCodici.get(elemType) + "' può comparire solo una volta (linea "
          + c_currentLineNum + ")");
  }

  /**
   * Aggiunge un pezzo al testo corrente della query
   * @param s
   */
  private void addQueryText(String s) {
    c_queryText += s;
  }

  /**
   * Ritorna  il testo compreso nell'elemento {@link #ELEMENTO_QUERY}.
   */
  public String getQuery() {
    return c_queryText;
  }

  /**
   * Nodo Document
   * 
   * @throws ValidateException
   */
  private void parseDocumentAttributes(Attributes documentAttrs) throws ValidateException {
    Rettangolo foglio = null;

    checkUnicity(TP_DOCUMENT);
    
    String szValoreAttributo = null;
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_FOGLIO, documentAttrs)) != null) {
      foglio = identificaFoglio(szValoreAttributo);
      c_document.setDocSize(foglio);
    }
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_ORIENTAMENTO, documentAttrs)) != null) {
      if (szValoreAttributo.equalsIgnoreCase("orizzontale")) {
        c_document.setDocSize(c_document.getDocSize().rotate());
      }
    }
    Margini mm;
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_MARGINI, documentAttrs)) != null) {
      mm = new Margini(szValoreAttributo);
      c_document.setMargini(mm);
    }
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_TITLE, documentAttrs)) != null) {
      c_document.setTitle(szValoreAttributo);
    }
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_SUBJECT, documentAttrs)) != null) {
      c_document.setSubject(szValoreAttributo);
    }
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_AUTHOR, documentAttrs)) != null) {
      c_document.setAuthor(szValoreAttributo);
    }
    if ( (szValoreAttributo = readAttr(A_DOCUMENTO_BKMOPEN, documentAttrs)) != null) {
      c_document.setBookmarksOpened(szValoreAttributo);
    }

    c_document.addPermissions(XReport.DocumentPermission.PRINT, readAttrAsBoolean(A_DOCUMENTO_ALLOWPRINT, documentAttrs, true));
    c_document.addPermissions(XReport.DocumentPermission.COPY, readAttrAsBoolean(A_DOCUMENTO_ALLOWCOPY, documentAttrs, true));
    c_document.addPermissions(XReport.DocumentPermission.MODIFY, readAttrAsBoolean(A_DOCUMENTO_ALLOWMODIFY, documentAttrs, true));

  }

  public DocumentAttributes getDocumentAttributes() {
    return c_document;
  }

  public class DocumentAttributes {
    private Rettangolo                              c_docSize;
    private Margini                                 c_margini;
    private PageHeader                              c_pageHeader;
    private PageFooter                              c_pageFooter;
    private RulersElement                           c_rulers;
    private List<WatermarkElement>                  c_watermarks;
    private MarginboxElement                        c_marginBox;
    private String                                  c_title;
    private String                                  c_subject;
    private String                                  c_author;
    private String                                  c_bookmarksOpened;
    private Map<XReport.DocumentPermission, Boolean> c_permissions;
    private final String                            DOC_MarginiDefault = "2cm 2cm 2cm 2cm";

    public DocumentAttributes() throws ValidateException {
      //assegno i default
      c_margini = new Margini(DOC_MarginiDefault);
      c_docSize = Rettangolo.A4;
      c_watermarks = new ArrayList<WatermarkElement>();
    }

    public Rettangolo getDocSize() {
      return c_docSize;
    }

    private void setDocSize(Rettangolo docSize) {
      c_docSize = docSize;
    }

    public Margini getMargini() {
      return c_margini;
    }

    private void setMargini(Margini margini) {
      c_margini = margini;
    }

    public String getTitle() {
      return c_title;
    }

    private void setTitle(String title) {
      c_title = title;
    }

    public String getSubject() {
      return c_subject;
    }

    private void setSubject(String subject) {
      c_subject = subject;
    }

    public String getAuthor() {
      return c_author;
    }

    private void setAuthor(String author) {
      c_author = author;
    }

    private void addPermissions(XReport.DocumentPermission perm, Boolean allow) {
      if (allow == null)
        return;
      if (c_permissions == null)
        c_permissions = new HashMap<XReport.DocumentPermission, Boolean>();
      c_permissions.put(perm, Boolean.valueOf(allow));
    }

    public Map<XReport.DocumentPermission, Boolean> getPermissions() {
      return c_permissions;
    }

    public PageHeader getPageHeader() {
      return c_pageHeader;
    }

    private void setPageHeader(PageHeader pageHeader) {
      c_pageHeader = pageHeader;
    }

    public PageFooter getPageFooter() {
      return c_pageFooter;
    }

    private void setPageFooter(PageFooter pageFooter) {
      c_pageFooter = pageFooter;
    }

    public RulersElement getRulers() {
      return c_rulers;
    }

    public void setRulers(RulersElement rulers) {
      c_rulers = rulers;
    }

    public List<WatermarkElement> getWatermarks() {
      return c_watermarks;
    }

    public WatermarkElement getLastWatermark() {
      return c_watermarks.get(c_watermarks.size() - 1);
    }

    public void addWatermark(WatermarkElement watermark) {
      c_watermarks.add(watermark);
    }

    public MarginboxElement getMarginBox() {
      return c_marginBox;
    }

    public void setMarginBox(MarginboxElement marginBox) {
      c_marginBox = marginBox;
    }

    public String getBookmarksOpened() {
      return c_bookmarksOpened;
    }

    public void setBookmarksOpened(String bookmarksOpened) {
      c_bookmarksOpened = bookmarksOpened;
    }
  }

  /**
   * Nodo Font
   * 
   * @throws StampaException
   */
  private void parseFontAttributes(Attributes fontAttrs) throws StampaException {
    String szName = null;
    String szFontFamily = StileCarattere.FAMILY_DEFAULT;
    String szFontSrc;
    float fontSize = 8;
    boolean bold;
    boolean italic;
    boolean underline;
    boolean defaultFont;
    Colore color = Colore.NERO;

    String szValoreAttributo = null;
    szName = readAttr(A_FONT_NAME, fontAttrs);
    if ( (szValoreAttributo = readAttr(A_FONT_FAMILY, fontAttrs)) != null) {
      szFontFamily = szValoreAttributo;
    }
    szFontSrc = readAttr(A_FONT_SRC, fontAttrs);
    fontSize = Text.toFloat(readAttr(A_FONT_SIZE, fontAttrs), StileCarattere.SIZE_DEFAULT);
    bold = Text.toBoolean(readAttr(A_FONT_BOLD, fontAttrs), false);
    italic = Text.toBoolean(readAttr(A_FONT_ITALIC, fontAttrs), false);
    underline = Text.toBoolean(readAttr(A_FONT_UNDERLINE, fontAttrs), false);
    defaultFont = Text.toBoolean(readAttr(A_FONT_DEFAULT, fontAttrs), false);
    if ( (szValoreAttributo = readAttr(A_FONT_REFCOLOR, fontAttrs)) != null) {
      Colore tmpColor = c_stampa.getColorByName(szValoreAttributo);
      if (tmpColor != null) {
        color = tmpColor;
      }
    }
    StileCarattere stile = new StileCarattere(szName, szFontFamily, fontSize);
    stile.setBold(bold);
    stile.setItalic(italic);
    stile.setUnderline(underline);
    stile.setColore(color);
    stile.setDefault(defaultFont);
    stile.setSourceFont(szFontSrc);
    c_stampa.addStile(stile);
  }

  /**
   * Legge un attributo dalla collection passata.
   * @param nomeAttributo nome dell'attributo
   * @param attrs collection attributi da cui prendere il valore
   * 
   * @return valore dell'attributo oppure null se non esiste nella collection
   */
  private String readAttr(String nomeAttributo, Attributes attrs) {
    return attrs.getValue(nomeAttributo);
  }

  /**
   * Legge un attributo e lo converte a boolean.
   * Se non riesce a convertirlo torna il defaultValue.
   * @param nomeAttributo nome dell'attributo
   * @param attrs collection attributi da cui prendere il valore
   * @param defaultValue valore di default nel caso sia assente l'attributo oppure non sia convertibile in booleano
   * 
   * @return valore booleano dell'attributo
   * @see Text#toBoolean(Object, boolean)
   * @see #readAttr(String, Attributes)
   */
  private boolean readAttrAsBoolean(String nomeAttributo, Attributes attrs, boolean defaultValue) {
    return Text.toBoolean(attrs.getValue(nomeAttributo), defaultValue);
  }

  /******************************/
  /** VARI METODI IDENTIFICA!! **/
  /******************************/

  private Rettangolo identificaFoglio(String szValoreAttributo) {
    Rettangolo foglio = null;
    //Lo metto prima perchè è il caso più comune
    if (szValoreAttributo.equalsIgnoreCase("A4")) {
      foglio = Rettangolo.A4;
    } else if (szValoreAttributo.equalsIgnoreCase("A3")) {
      foglio = Rettangolo.A3;
    } else if (szValoreAttributo.equalsIgnoreCase("A2")) {
      foglio = Rettangolo.A2;
    } else if (szValoreAttributo.equalsIgnoreCase("A5")) {
      foglio = Rettangolo.A5;
    } else if (szValoreAttributo.equalsIgnoreCase("A6")) {
      foglio = Rettangolo.A6;
    } else if (szValoreAttributo.equalsIgnoreCase("LETTER")) {
      foglio = Rettangolo.LETTER;
    } else if (szValoreAttributo.equalsIgnoreCase("LEGAL")) {
      foglio = Rettangolo.LEGAL;
    }
    return foglio;
  }

  /**
   * Nodo class dentro userClasses
   * 
   * @throws ValidateException
   * @throws Exception
   */
  private void elaboraUserClass(Attributes attrs, int line, int col) throws ValidateException {
    String name = readAttr(A_USERCLASS_NAME, attrs);
    String value = readAttr(A_USERCLASS_VALUE, attrs);
    if ( !c_stampa.isLegalClassName(name)) {
      throw new ValidateException("Il nome di una user class può contenere solo lettere e cifre: linea " + line);
    }

    c_stampa.addUserClass(name, value);
  }

  /**
   * Nodo class dentro userClasses
   * 
   * @throws ValidateException
   * @throws Exception
   */
  private void elaboraParameter(Attributes attrs, int line, int col) throws ValidateException {
    String name = readAttr(A_PARAM_NAME, attrs);
    
    if ( !c_stampa.isLegalParameterName(name)) {
      throw new ValidateException("Il nome di parametro %s non e' valido (linea %d)", name, line);
    }
    String clazz = readAttr(A_PARAM_CLASS, attrs);
    Parameter p;
    if (c_stampa.existParameter(name)) {
      p = c_stampa.getParameter(name);
      p.setClassName(clazz);
    }
    else {
      //lo creo
      p = c_stampa.addParameter(name, null, clazz);
    }
    boolean required = readAttrAsBoolean(A_PARAM_REQUIRED, attrs, false);
    p.setRequired(required);
    String defvalue = readAttr(A_PARAM_DEFAULT, attrs);
    p.setDefaultValue(defvalue);
  }
  
  /**
   * Nodo Group
   * 
   * @throws ValidateException
   * @throws GroupException
   */
  private GroupElement elaboraGruppo(Attributes attrs, int line, int col) throws ValidateException, GroupException {
    // Per ogni tag <group>
    GroupElement questoGruppo = new GroupElement(attrs, line, col);
    questoGruppo.setDebugData(c_stampa.isDebugData());

    //*SUBR*    if (questoGruppo.getQueryAttribute() != null) {
    //*SUBR*      // è un subreport!
    //*SUBR*      RootModel questoRootModel = new RootModel();
    //*SUBR*      questoRootModel.setDebugMode(c_stampa.isDebugData());

    //*SUBR*      questoGruppo.setGroupModel(questoRootModel);    //*SUBR*  

    //*SUBR*      GroupElement grDefault = new GroupElement(questoGruppo.getName() + DEFAULTGROUP_POSTFIX, questoRootModel);
    //*SUBR*      grDefault.setDebugData(c_stampa.isDebugData());
    //*SUBR*      grDefault.setSubreportName(questoGruppo.getName());
    //*SUBR*
    //*SUBR*      c_stampa.addSubreport(grDefault, questoRootModel);
    //*SUBR*      pushContext(grDefault, questoRootModel);
    //*SUBR*    }
    GroupModel questoModello = null;
    if (questoGruppo.getQueryAttribute() != null) {
      questoModello = c_currentModel.addChildModel(questoGruppo.getName(), questoGruppo.getQuerySymbol(), questoGruppo, c_stampa);
    } else {
      questoModello = c_currentModel.addChildModel(questoGruppo.getName());
    }
    questoGruppo.setGroupModel(questoModello);
    questoModello.setNullKeyAllowed(questoGruppo.isNullKeyAllowed());

    if (questoGruppo.isAllKey()) {
      // specificato 'keys="*"' nel XML
      questoModello.setAllKey(true);
    } else {
      // Per ogni item nell'attributo keys
      for (String key : questoGruppo.getKeys()) {
        questoModello.addKeyField(key);
      }
    }
    pushContext(questoGruppo, questoModello);
    return questoGruppo;
  }

  /**
   * Nodo Newpage
   * 
   * @throws ValidateException
   */
  private NewpageElement elaboraNewpage(Attributes attributes, int line, int col) throws ValidateException {
    NewpageElement elemNewpage = new NewpageElement(attributes, line, col);
    elemNewpage.setDebugData(c_stampa.isDebugData());

    pushContext(elemNewpage);
    return elemNewpage;
  }

  /**
   * Crea un elemento {@link TextNode} con la stringa passata e lo aggiunge
   * all'elemento corrente. <br>
   * Se necessario, aggiunge un elemento TEXT fittizio.
   * 
   * @param szTesto
   *          testo del TextNode creato
   * @param line
   *          linea in cui compare l'inizio del testo
   * @param col
   *          colonna in cui compare l'inizio del testo
   * @throws ValidateException
   *           errori in aggiunta dell'elemento al suo parent
   */
  private void elaboraCharacters(String szTesto, int line, int col) throws ValidateException {
    if (c_currentElem != null) {
      boolean insideText = c_currentElem instanceof TextElement;
      if ( !insideText) {
        insideText = ((AbstractNode) c_currentElem).getConcreteParent() instanceof TextElement;
      }
      boolean insideTable = c_currentElem instanceof TableElement;
      if ( !insideTable) {
        insideTable = ((AbstractNode) c_currentElem).getConcreteParent() instanceof TableElement;
      }
      //non aggiungo spazi come figli di table
      if (Text.isOnlySpace(szTesto) && insideTable)
        return;
      if ( !insideText) {
        //non aggiungo text fittizi composti di soli spazi in html mode
        if (Text.isOnlySpace(szTesto) && !c_body_textmode)
          return;
        elaboraText(null, line, col);
      }
      TextNode elemTesto = new TextNode(szTesto, line, col);
      elemTesto.setDebugData(c_stampa.isDebugData());
      c_currentElem.addChild(elemTesto);
    }
  }

  /**
   * Nodo Cell
   * 
   * @throws ValidateException
   */
  private CellElement elaboraCella(Attributes attrs, int line, int col) throws ValidateException {
    CellElement elemCell = new CellElement(attrs, line, col);
    elemCell.setDebugData(c_stampa.isDebugData());

    pushContext(elemCell);
    return elemCell;
  }

  /**
   * Nodo Table
   * 
   * @throws ValidateException
   */
  private TableElement elaboraTable(Attributes attrs, int line, int col) throws ValidateException {
    TableElement elemTable = new TableElement(attrs, line, col);
    elemTable.setDebugData(c_stampa.isDebugData());
    //Current è ancora il padre

    pushContext(elemTable);
    return elemTable;
  }

  /**
   * Creazione di un nodo Text.
   * 
   * @throws ValidateException
   *           in caso di errori in creazione
   */
  private TextElement elaboraText(Attributes attrs, int line, int col) throws ValidateException {
    TextElement elem = null;
    elem = new TextElement(attrs, line, col);
    elem.setDebugData(c_stampa.isDebugData());
    pushContext(elem);
    return elem;
  }

  /**
   * Creazione di un nodo Text.
   * 
   * @throws ValidateException
   *           in caso di errori in creazione
   */
  private BookmarkElement elaboraBookmark(Attributes attrs, int line, int col) throws ValidateException {
    BookmarkElement elem = null;
    elem = new BookmarkElement(c_stampa, attrs, line, col);
    elem.setDebugData(c_stampa.isDebugData());
    pushContext(elem);
    return elem;
  }
  
  //  /**
  //   * Ritorna true sse il primo elemento <b>concreto</b> padre/antenato di elem è
  //   * un TextElement.
  //   * <p>
  //   * NB: gli elementi concreti sono quelli che generano direttamente qualcosa
  //   * (vedi {@link AbstractElement#isConcreteElement()}: ad esempio l'elemento
  //   * <tt>if</tt> non è concreto.
  //   * 
  //   * @param elem
  //   *          elemento di cui cercare gli antenati
  //   */
  //  private boolean isFirstParentIsText(IReportElement elem) {
  //    if (elem == null) {
  //      return false;
  //    }
  //    IReportElement parent = elem.getParent();
  //    while (parent != null) {
  //      if (parent instanceof TextElement) {
  //        return true;
  //      }
  //      if ( ((AbstractElement) parent).isConcreteElement()) {
  //        return false;
  //      }
  //      parent = parent.getParent();
  //      if (parent instanceof GroupElement) {
  //        GroupElement group = ((GroupElement) parent);
  //        if (group.getName().endsWith(DEFAULTGROUP_POSTFIX)) {
  //          //sono sul gruppo radice: mi fermo
  //          break;
  //        }
  //      }
  //    }
  //    return false;
  //  }

  /**
   * Nodo Image
   * 
   * @throws ValidateException
   */
  private ImageElement elaboraImage(Attributes attrs, int line, int col) throws ValidateException {
    ImageElement elemImage = new ImageElement(attrs, line, col);
    elemImage.setDebugData(c_stampa.isDebugData());

    pushContext(elemImage);
    return elemImage;
  }

  /**
   * Nodo Line
   * 
   * @throws ValidateException
   */
  private LineElement elaboraLine(Attributes attrs, int line, int col) throws ValidateException {
    LineElement linea = new LineElement(c_stampa, attrs, line, col);
    pushContext(linea);
    return linea;
  }

  private ChartElement elaboraChart(Attributes attrs, int line, int col) throws ValidateException {
    ChartElement chart = new ChartElement(attrs, line, col);
    pushContext(chart);
    return chart;
  }

  private WatermarkElement elaboraWatermark(Attributes attrs, int line, int col) throws ValidateException {
    WatermarkElement wm = new WatermarkElement(c_stampa, attrs, line, col);
    c_document.addWatermark(wm);
    c_watermarkText = "";
    return wm;
  }

  private MarginboxElement elaboraMarginbox(Attributes attrs, int line, int col) throws ValidateException {
    checkUnicity(TP_MARGINBOX);
    MarginboxElement mbox = new MarginboxElement(c_stampa, attrs, line, col);
    c_document.setMarginBox(mbox);
    return mbox;
  }

  private RulersElement elaboraRulers(Attributes attrs, int line, int col) throws ValidateException {
    checkUnicity(TP_RULERS);
    RulersElement r = new RulersElement(c_stampa, attrs, line, col);
    c_document.setRulers(r);
    return r;
  }

  /**
   * Nodo span
   * 
   * @throws ValidateException
   */
  private SpanElement elaboraSpan(Attributes attrs, int line, int col) throws Exception {

    SpanElement elemSpan = new SpanElement(c_stampa, attrs, line, col);
    elemSpan.setDebugData(c_stampa.isDebugData());

    pushContext(elemSpan);
    return elemSpan;
  }

  /**
   * Nodo Field
   * 
   * @throws ValidateException
   */
  private FieldElement elaboraField(Attributes attrs, int tipoField, int line, int col) throws Exception {
    boolean insideText = c_currentElem instanceof TextElement;
    if ( !insideText) {
      insideText = ((AbstractNode) c_currentElem).getConcreteParent() instanceof TextElement;
    }
    if ( !insideText) {
      elaboraText(null, line, col);
    }

    //    boolean isTextOrSpan = c_currentElem instanceof TextElement || c_currentElem instanceof SpanElement
    //        || isFirstParentIsText(c_currentElem) || c_currentElem instanceof CellElement;
    //    if ( !isTextOrSpan) {
    //      //creo un tag text fittizio e lo aggiungo all'elemento corrente
    //      elaboraText(null, line, col);
    //    }

    FieldElement elemField = null;
    switch (tipoField) {
      case TP_FIELD: {
        elemField = new FieldElement(c_stampa, attrs, line, col);
      }
        break;
      case TP_NUMBER: {
        elemField = new NumberFieldElement(c_stampa, attrs, line, col);
      }
        break;
      case TP_BARCODE: {
        elemField = new BarcodeElement(c_stampa, attrs, line, col);
      }
        break;
      case TP_DATE: {
        elemField = new DateFieldElement(c_stampa, attrs, line, col);
      }
        break;
      default: {
        throw new ValidateException("Tipo di campo non gestito: linea " + line);
      }
    }
    elemField.setDebugData(c_stampa.isDebugData());

    elemField.addCampo(c_currentModel);
    //
    pushContext(elemField);
    return elemField;
  }

  /**
   * Versione di pushContext per tutti gli elementi eccetto <tt>group</tt>
   * 
   * @param tag
   *          tag corrente che sto iniziando ad analizzare
   * @throws ValidateException
   *           nel caso che la struttura degli elementi che vado a creare non
   *           sia valida
   */
  private void pushContext(IReportElement tag) throws ValidateException {
    pushContext(tag, null);
  }

  /**
   * La pushContext aggiunge al tag corrente (variabile {@link #c_currentElem})
   * un tag figlio che è il tag passato nel primo parametro. Dopo questa
   * impostazione il tag figlio diventa il tag corrente. <br/>
   * 
   * Se il gruppo passato come secondo parametro è diverso da null, lo aggiunge
   * allo stack che mantiene i contesti necessari per la valutazione di tutti i
   * campi. Inoltre imposta come modello corrente tale parametro. In questo modo
   * uso il model per la valutazione di tutti i campi successivi.
   * 
   * @param tag
   *          tag group che sto analizzando
   * @param gruppo
   *          modello associato al gruppo che sto analizzando
   * @throws ValidateException
   *           nel caso che la struttura degli elementi che vado a creare non
   *           sia valida
   */
  private void pushContext(IReportElement tag, GroupModel gruppo) throws ValidateException {
    // c_currentElem è null all'inizio del parsing
    if ((c_currentElem instanceof GroupElement || c_currentElem instanceof CellElement)  
          && ! ((AbstractNode) tag).isBlockElement()) {
      if (tag.isElement() && ((AbstractElement) tag).isContentElement() && ((AbstractElement) tag).isConcreteElement()) {
        //arrivo qui se si sta cercando di aggiungere un elemento non-block (quindi non del semplice testo) ad un gruppo/cella
        //in tal caso aggiungo un elemento "text" fittizio
        elaboraText(null, ((AbstractElement) c_currentElem).getLineNum(), ((AbstractElement) c_currentElem).getColumnNum());          
      }
    }
    if (c_currentElem != null) {
      c_currentElem.addChild(tag);
    }
    c_currentElem = tag;
    if (gruppo != null) {
      c_stackGroupModels.push(gruppo);
      c_currentModel = gruppo;
    }
    if (tag instanceof GroupElement) {
      GroupElement gr = (GroupElement) tag;
      if (gr.getSubreportName() != null)
        pushDefaultGroup(gr);
    }
  }

  /**
   * Complementare di pushContext, richiamata alla fine del parsing di un
   * elemento per rimettere come contesto corrente l'elemento padre del
   * corrente. <br/>
   * Fa tutti gli aggiustamenti di context necessari nel caso che l'elemento che
   * si stia chiudendo sia un gruppo e/o un subreport.
   */
  private void popContext() {
    // prima di levare il tag dallo stack, faccio gli ultimi aggiustamenti
    if (c_currentElem != null) {
      c_currentElem.fineParsingElemento();
    }
    if (c_currentElem instanceof GroupElement) {
      //GroupElement gr = (GroupElement) c_currentElem;
      c_stackGroupModels.pop();
      //      if (gr.getQueryAttribute() != null) {
      //        // sto facendo il pop di un tag group che è un subreport: devo fare un
      //        // doppio pop in quanto sullo stack dei GroupModel ho anche il gruppo
      //        // di default (ci sono 2 pushContext nella elaboraGruppo)
      //        c_stackGroupModels.pop();
      //        c_currentElem = c_currentElem.getParent();
      //        popDefaultGroup();
      //      }
      c_currentModel = c_stackGroupModels.getFirst();
    }
    c_currentElem = c_currentElem.getParent();
  }

  public String getCurrentTagDescription() {
    return c_currentTagName + ", riga " + c_currentLineNum;
  }

  public void destroy() {
    c_stampa = null;
    c_currentDefaultGroup = null;
    c_currentElem = null;
    c_currentModel = null;
    if (c_mappaElementi != null)
      c_mappaElementi.clear();
    if (c_mappaCodici != null)
      c_mappaCodici.clear();
    c_rootGroupElement = null;
    if (c_stackDefaultGroups != null)
      c_stackDefaultGroups.clear();
    if (c_stackGroupModels != null)
      c_stackGroupModels.clear();
  }
}
