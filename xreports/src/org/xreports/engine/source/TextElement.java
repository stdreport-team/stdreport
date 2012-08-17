package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Paragrafo;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;
import org.xreports.util.Text;

public class TextElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi del Tag "text" */
  private static final String ATTRIB_FIRSTLINE     = "firstLineIndent";
  private static final String ATTRIB_TRIMLINES     = "trimLines";
  private static final String ATTRIB_JOINLINES     = "joinLines";
  private static final String ATTRIB_BREAKLINES    = "breakLines";
  private static final String ATTRIB_INTERLINEA    = "interlinea";
  private static final String ATTRIB_MARGINI       = "margini";

  private boolean             m_fittizio           = false;

  /** flag per evitare di trimmare più volte lo stesso text element */
  private boolean             bTrimmed             = false;
  /** flag per evitare di unire più volte le linee dello stesso text element */
  private boolean             bJoined              = false;

  public TextElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    m_fittizio = (attrs == null);
  }

  /**
   * Indica se questo elemento è fittizio. <br>
   * Un elemento &lt;text&gt: è fittizio se non è presente nel testo XML del sorgente, ma viene automaticamente messo dal sistema
   * quando si incontrano caratteri fuori da qualsiasi tag.
   * 
   * @return true sse questo elemento è fittizio
   */
  public boolean isFittizio() {
    return m_fittizio;
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_BREAKLINES, Boolean.class, "true");
    addAttributo(ATTRIB_TRIMLINES, Boolean.class, "false");
    addAttributo(ATTRIB_JOINLINES, Boolean.class, "false");
    addAttributoMeasure(ATTRIB_INTERLINEA, null, false, true);
    addAttributoMeasure(ATTRIB_FIRSTLINE, "0", false, false);

    addAttributo(ATTRIB_MARGINI, Margini.class);
    addAttributoMeasure(ATTRIB_MARGIN_LEFT, null, true, false);
    addAttributoMeasure(ATTRIB_MARGIN_RIGHT, null, true, false);
    addAttributoMeasure(ATTRIB_MARGIN_BOTTOM, null, false, false);
    addAttributoMeasure(ATTRIB_MARGIN_TOP, null, false, false);
  }

  /**
   * Ritorna true se questo text ha come figli solo TextNode
   * e tutti sono composti solo da spazi.
   */
  public boolean isOnlySpace() {
    for (IReportNode child: getChildren()) {
      if (!(child instanceof TextNode)) {
        return false;
      }
      if (!((TextNode)child).isSpace()) {
        return false;
      }
    }
    
    return true;    
  }
  
  /**
   * Se l'attributo trimLines del tag text è true, trimma tutte le righe di solo testo contenute nel tag.
   */
  private void trimLines() {
    if (bTrimmed) {
      return;
    }

    bTrimmed = true;
    if (getTrimLines()) {
      int q = c_elementiFigli.size();
      for (int i = 0; i < q; i++) {
        IReportNode current = c_elementiFigli.get(i);
        IReportNode prev = i > 0 ? c_elementiFigli.get(i - 1) : null;
        IReportNode next = i < q - 1 ? c_elementiFigli.get(i + 1) : null;
        boolean left = false, right = false;
        //devo stare attento a non trimmare troppo, rischio di levare spazi significativi che separano il testo
        //da un tag field successivo
        if (prev == null) {
          left = true;
        }
        if (next == null) {
          right = true;
        }
        if (current instanceof TextNode) {
          trimNodeLines((TextNode) current, left, right);
        }
      }
    } else {
      //se non trimmo le righe devo però levare le tabulazioni,
      //che causano problemi
      for (IReportNode node : c_elementiFigli) {
        if (node instanceof TextNode) {
          eraseTabs((TextNode) node);
        }
      }
    }
  }

  /**
   * Se l'attributo breakLines del tag text è true, leva il carattere 'a-capo' in tutte le righe del tag
   */
  private void joinLines() {
    if (bJoined) {
      return;
    }

    bJoined = true;
    if ( isJoinLines()) {
      int q = c_elementiFigli.size();
      for (int i = 0; i < q; i++) {
        IReportNode current = c_elementiFigli.get(i);
        if (current instanceof TextNode) {
          joinNodeLines((TextNode) current, i==0);
        }
      }
    }
  }

  private void joinNodeLines(TextNode txt, boolean isFirstChild) {
    if (txt.getTesto() == null) {
      return;
    }

    //  	System.out.println("\n>>>>>>>>>>>> START trim node, left=" + trimLeft + ", right=" + trimRight +
    //  			":\n\"" + txt.getTesto() + '"');
    if (txt.isSpace()) {
      txt.setTesto("");
    }
    else {
      String sText = txt.getTesto();
      sText = Text.replace(sText, '\n', " ");
      if (isFirstChild)
        sText = Text.trimLeft(sText);
      txt.setTesto(sText);      
    }
    //  	System.out.println("\n<<<<<<<<<< END trim node:\n\"" + txt.getTesto() + '"');
  }

  /**
   * Elimina tutti i caratteri tab da un nodo testo. In iText causano problemi e comunque il significato di un tab non è chiaro, nel
   * senso che non si sa quanto spazio debba coprire; meglio usare una sequenza di spazi.
   * 
   * @param txt
   */
  private void eraseTabs(TextNode txt) {
    txt.setTesto(txt.getTesto().replaceAll("\t", " "));
  }

  /**
   * Trimma un singolo nodo di testo. Tutte le linee di testo che compongono il nodo vengono trimmate a destra e sinistra (vanno via
   * sia gli spazi che le tabulazioni); le linee vuote vengono preservate.
   * 
   * @param txt
   *          nodo testo da trimmare
   */
  private void trimNodeLines(TextNode txt, boolean trimLeft, boolean trimRight) {
    if (txt.getTesto() == null) {
      return;
    }

    //  	System.out.println("\n>>>>>>>>>>>> START trim node, left=" + trimLeft + ", right=" + trimRight +
    //  			":\n\"" + txt.getTesto() + '"');
    String[] lines = txt.getTesto().split("\n");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.length; i++) {
      String testo = "";
      String line = lines[i];
      if (line.length() > 0) {
        if (trimLeft || i > 0) {
          //trimmo a sinistra tutte le righe eccetto la prima
          testo = Text.trimLeft(line);
        } else {
          testo = line;
        }
        if (trimRight || i < lines.length - 1) {
          //trimmo a destra tutte le righe eccetto l'ultima
          testo = Text.trimRight(testo);
        }
        sb.append(testo);
      }
      if (i < lines.length - 1) {
        //se non sono sull'ultima riga, aggiungo un newline
        sb.append("\n");
      }
    }

    txt.setTesto(sb.toString());
    //  	System.out.println("\n<<<<<<<<<< END trim node:\n\"" + txt.getTesto() + '"');
  }

  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    salvaStampaGruppo(stampa, gruppo);
    try {
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        if (isDebugData()) {
          stampa.debugElementOpen(this);
        }
        trimLines();
        joinLines();
        Paragrafo paragrafo = stampa.getFactoryElementi().creaParagrafo(stampa, this, padre);

        for (IReportNode reportElem : c_elementiFigli) {
          reportElem.setDebugData(isDebugData());
          List<Elemento> listaFigli = reportElem.generate(gruppo, stampa, paragrafo);
          for (Elemento elementoOutput : listaFigli) {
            elementoOutput.fineGenerazione();
            paragrafo.addElement(elementoOutput);
          }
        }
        paragrafo.fineGenerazione();
        listaElementi.add(paragrafo);
      }
      if (isDebugData()) {
        stampa.debugElementClose(this);
      }
      return listaElementi;
    } catch (GenerateException e) {
      throw e;
    } catch (Exception e) {
      throw new GenerateException(this, e);
    }
  }

  /**
   * Indica se le righe di questo elemento <tt>text</tt> devono essere "trimmate"
   * o devono rimanere con gli spazi  come nel sorgente.
   * @return true sse devono essere eliminati gli spazi a inizio e fine riga
   */
  public boolean getTrimLines() {
    return getAttrValueAsBoolean(ATTRIB_TRIMLINES);
  }

  /**
   * Indica se le righe di questo elemento <tt>text</tt> devono essere unite
   * o devono rimanere spezzate come nel sorgente.
   * 
   * <br/>
   * NB: per compatibilità con la versione 1, se "joinLines" non viene specificato, 
   * ritorna il valore corrispondente di "breakLines".
   * 
   * @return true sse devono essere eliminati i newlines fra le righe
   */
  public boolean isJoinLines() {
    if (isAttrNull(ATTRIB_JOINLINES))
      return !getAttrValueAsBoolean(ATTRIB_BREAKLINES);
    
    return getAttrValueAsBoolean(ATTRIB_JOINLINES);
  }
  
  /**
   * @return the attrib_interlinea
   */
  public Measure getInterlinea() {
    return getAttrValueAsMeasure(ATTRIB_INTERLINEA);
  }

  public float getFirstLineIndent() {
    Float f = getAttrValueAsFloat(ATTRIB_FIRSTLINE);
    if (f == null) {
      return 0f;
    }

    return f.floatValue();
  }

  /**
   * @return the attrib_spazioPrima
   */
  public float getSpazioPrima() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_TOP);
    if (f != null) {
      return f.floatValue();
    }
    Margini m = getAttrValueAsMargini(ATTRIB_MARGINI);
    if (m != null) {
      return m.getTop().getValue();
    }

    return 0f;
  }

  /**
   * Ritorna il margine sinistro in punti
   */
  public float getSpazioSinistra() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_LEFT);
    if (f != null) {
      return f.floatValue();
    }

    Margini m = getAttrValueAsMargini(ATTRIB_MARGINI);
    if (m != null) {
      return m.getLeft().getValue();
    }
    return 0f;
  }

  /**
   * Ritorna il margine destro in punti.
   */
  public float getSpazioDestra() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_RIGHT);
    if (f != null) {
      return f.floatValue();
    }
    Margini m = getAttrValueAsMargini(ATTRIB_MARGINI);
    if (m != null) {
      return m.getRight().getValue();
    }

    return 0f;
  }

  /**
   * @return the attrib_spazioDopo
   */
  public float getSpazioDopo() {
    Float f = getAttrValueAsFloat(ATTRIB_MARGIN_BOTTOM);
    if (f != null) {
      return f.floatValue();
    }

    Margini m = getAttrValueAsMargini(ATTRIB_MARGINI);
    if (m != null) {
      return m.getBottom().getValue();
    }

    return 0f;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_TEXT;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#fineParsingElemento()
   */
  @Override
  public void fineParsingElemento() {
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#toString()
   */
  @Override
  public String toString() {
    String s = super.toString();
    if (isFittizio()) {
      s += " (fittizio)";
    }
    return s;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return true;
  }

  @Override
  public String getXMLOpenTag() {
    String s = super.getXMLOpenTag();
    if (isFittizio())
      s += " (fittizio)";
    return s;
  }
  
  @Override
  public boolean canChildren() {
    return true;
  }
}
