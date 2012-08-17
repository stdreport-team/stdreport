package org.xreports.engine.output.impl.itext;

import java.util.List;

import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.BloccoTesto;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement;
import org.xreports.engine.source.AbstractNode;
import org.xreports.engine.source.BookmarkElement;
import org.xreports.engine.source.BookmarkableElement;
import org.xreports.engine.source.FieldElement;
import org.xreports.engine.source.Measure;
import org.xreports.engine.source.SpanElement;
import org.xreports.engine.source.TextNode;


import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;

/**
 * Un blocco di testo specifico di IText -> viene realizzato attraverso la
 * classe di IText Chunk
 * 
 * @author fabrizio
 */
public class BloccoTestoIText extends ElementoIText implements BloccoTesto {

  /** Questo è il blocco base per scrivere del testo all'interno del documento */
  private Chunk             c_chunk;

  private Phrase            c_phrase;

  private SpanElement       c_spanElem;
  private Elemento          c_parentElem;
  private XReport            c_stampa;
  private FieldElement      c_fieldElem;

//  private static final char ZERO_WIDTH_SPACE = '\u200B';

  private Bookmark          c_spanBookmark;

  /**
   * Costruisce un blocco di testo partendo da un elemento TextNode
   * 
   * @param stampa
   *          - oggetto principale stampa
   * @param testo
   *          - nodo testo da trasformare in output
   * @param padre
   *          - il padre dell'elemento testo
   * @throws GenerateException
   * @throws ResolveException 
   */
  public BloccoTestoIText(XReport stampa, TextNode testo, Elemento padre) throws GenerateException, ResolveException {
    c_chunk = new Chunk(testo.getTesto());
    init(stampa, testo, padre);
    handleBookmark(testo, c_chunk);
  }

  /**
   * Costruisce un blocco di testo partendo da un elemento FieldElement
   * 
   * @param stampa
   *          - oggetto principale stampa
   * @param fieldElem
   *          - elemento field da trasformare in output
   * @param padre
   *          - il padre dell'elemento fieldElem
   * @throws GenerateException
   * @throws ResolveException 
   */
  public BloccoTestoIText(XReport stampa, FieldElement fieldElem, Elemento padre) throws GenerateException, ResolveException {
    c_chunk = new Chunk(fieldElem.getEvaluatedExpression());
    init(stampa, fieldElem, padre);
    c_fieldElem = fieldElem;
    prepareChunk(c_chunk);
    DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
    if (fieldElem.getTrace() != null) {
      doc.getPageListener().addField(this, fieldElem.getTrace());
    }
    handleBookmark(fieldElem, c_chunk);
  }

  /**
   * Costruisce un blocco di testo partendo da un elemento BookmarkElement
   * 
   * @param stampa
   *          - oggetto principale stampa
   * @param bkmElem
   *          - elemento BookmarkElement da aggiungere agli outline iText
   * @param padre
   *          - il padre dell'elemento bkmElem
   * @throws GenerateException
   * @throws ResolveException 
   */
  public BloccoTestoIText(XReport stampa, BookmarkElement bkmElem, Elemento padre) throws GenerateException, ResolveException {
    //c_chunk = new Chunk(ZERO_WIDTH_SPACE);
    c_chunk = new Chunk("X");
    c_stampa = stampa;
    setParent(padre);
    handleBookmark(bkmElem, c_chunk);
  }

  /**
   * Costruisce un blocco di testo partendo da un elemento SpanElement
   * 
   * @param stampa
   *          - oggetto principale stampa
   * @param spanElem
   *          - elemento span da trasformare in output
   * @param padre
   *          - il padre dell'elemento spanElem
   * @throws GenerateException
   */
  public BloccoTestoIText(XReport stampa, SpanElement spanElem, Elemento padre) throws GenerateException {
    c_phrase = new Phrase();
    c_spanElem = spanElem;
    init(stampa, spanElem, padre);
    try {
      if (c_spanElem.isBookmark()) {
        c_spanBookmark = new Bookmark(spanElem);
      }
    } catch (ResolveException e) {
      throw new GenerateException(c_spanElem, e);
    }
  }

  private Element prepareChunk(Element e) throws GenerateException {
    if ( ! (e instanceof Chunk)) {
      return e;
    }
    Chunk c = (Chunk) e;

    setHorizontalScaling(c);
    setTextRise(c);
    setBackgroundColor(c);
    setCharSpacing(c);

    return c;
  }

  private void setHorizontalScaling(Chunk c) {
    //se ho già l'attributo hscale impostato, lo lascio com'è e non faccio nulla
    if (c.hasAttributes()) {
      if (c.getAttributes().get(Chunk.HSCALE) != null)
        return;
    }
    float horizScale = 1f;
    if (c_spanElem != null)
      horizScale = c_spanElem.getHorizScale();
    if (c_fieldElem != null)
      horizScale = c_fieldElem.getHorizScale();
    c.setHorizontalScaling(horizScale);
  }

  private void setCharSpacing(Chunk c) {
    //se ho già l'attributo hscale impostato, lo lascio com'è e non faccio nulla
    if (c.hasAttributes()) {
      if (c.getAttributes().get(Chunk.CHAR_SPACING) != null)
        return;
    }
    float charSpacing = 0f;
    if (c_spanElem != null)
      charSpacing = c_spanElem.getCharSpacing();
    if (c_fieldElem != null)
      charSpacing = c_fieldElem.getCharSpacing();
    c.setCharacterSpacing(charSpacing);
  }

  private void setBackgroundColor(Chunk c) throws GenerateException {
    //se ho già l'attributo background impostato, lo lascio com'è e non faccio nulla
    if (c.hasAttributes()) {
      if (c.getAttributes().get(Chunk.BACKGROUND) != null)
        return;
    }
    Colore backColor = null;
    if (c_spanElem != null)
      backColor = c_spanElem.getBackgroundColor();
    if (c_fieldElem != null)
      backColor = c_fieldElem.getBackgroundColor();
    if (backColor != null) {
      DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
      if (doc.getColorByName(backColor.getName()) != null) {
        c.setBackground(doc.getColorByName(backColor.getName()));
      }
    }
  }

  private void setTextRise(Chunk c) {
    //se ho già l'attributo textrise impostato, lo lascio com'è e non faccio nulla
    if (c.hasAttributes()) {
      if (c.getAttributes().get(Chunk.SUBSUPSCRIPT) != null)
        return;
    }

    Measure y = null;
    if (c_spanElem != null)
      y = c_spanElem.getY();
    if (c_fieldElem != null)
      y = c_fieldElem.getY();
    if (y != null) {
      float rise = y.getValue();
      if (y.isLines()) {
        if (c_parentElem instanceof ParagrafoIText) {
          rise = ((ParagrafoIText) c_parentElem).getLeading() * rise;
        } else if (c_parentElem instanceof CellaIText) {
          rise = ((CellaIText) c_parentElem).getLeading() * rise;
        } else {
          rise = 0;
        }
      }
      c.setTextRise(rise);
    }
  }

  /**
   * Controlla se l'elemento che si deve stampare ha un bookmark collegato, e in
   * tal caso predisponde il documento per generare il bookmark nel momento in
   * cui l'elemento sarà effettivamente scritto nel pdf.
   * 
   * @param elem
   *          elemento che potrebbe avere un bookmark associato
   * @param c
   *          chunk che "renderizza" l'elemento, il quale avrà un PdfOutline
   *          collegato
   * 
   * @throws GenerateException
   *           nel caso la valutazione delle bookmark property di elem dia
   *           errore
   */
  private void handleBookmark(BookmarkableElement elem, Chunk c) throws ResolveException, GenerateException {
    if (elem.isBookmark()) {
      c.setGenericTag(getUniqueID());
      DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
      doc.getPageListener().addBookmark(this, new Bookmark(elem));
    }
  }

  private void handleBookmark(TextNode node, Chunk c) throws GenerateException, ResolveException {
    if (node.isBookmark()) {
      c.setGenericTag(getUniqueID());
      DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
      doc.getPageListener().addBookmark(this, new Bookmark(node.getBookmarkText(), node.getBookmarkLevel()));
    }
  }
  
  /**
   * 1) Tramanda a cascata a partire dal padre il font impostato con refFont 2)
   * Lega all'elemento BloccoTestoIText il suo elemento padre
   * 
   * @param stampa
   * @param element
   *          elemento che sto generando
   * @param padre
   * @throws GenerateException
   * @throws EvaluateException
   */
  private void init(XReport stampa, AbstractNode element, Elemento padre) throws GenerateException {
    c_stampa = stampa;
    if (element != null) {
      String refFont = null;

      if (element instanceof AbstractElement) {
        refFont = ((AbstractElement) element).getRefFontName();
      }
      if (refFont == null) {
        AbstractElement absPadre = (AbstractElement) element.getParent();
        refFont = absPadre.getRefFontName();
      }
      if (refFont != null) {
        DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
        if (c_phrase != null)
          c_phrase.setFont(doc.getFontByName(refFont));
        else
          c_chunk.setFont(doc.getFontByName(refFont));
      }

    }
    setParent(padre);
  }

  @Override
  public Object getContent(Elemento padre) {
    if (c_phrase != null)
      return c_phrase;
    return c_chunk;
  }

  public void addElement(Elemento figlio) throws GenerateException {
    if (c_phrase != null) {
      //se arrivo qui, sono su un elemento span (c_spanElem) che ha elementi figli. 
      Object obj = figlio.getContent(this);
      if (obj != null && obj instanceof Element) {
        Element el = (Element) obj;
        prepareChunk(el);
        if (c_spanBookmark != null && el instanceof Chunk) {
          //qui gestisco il fatto che l'elemento span abbia il settaggio di un bookmark: appena il primo
          //chunk figlio viene aggiunto, uso lui come destinatario del bookmark          
          ((Chunk) el).setGenericTag(getUniqueID());
          DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
          doc.getPageListener().addBookmark(this, c_spanBookmark);
          c_spanBookmark = null;
        }
        c_phrase.add(el);
        return;
      }
    }
    //E' un elemento finale non è possibile aggiungere altri elementi!!!
    return;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi!!!
    for (Elemento el : figli)
      addElement(el);
  }

  @Override
  public void flush(Documento documento) throws GenerateException {

  }

  @Override
  public String getUniqueID() {
    return "Testo" + getElementID();
  }

  @Override
  public float calcAvailWidth() {
    return c_chunk.getWidthPoint();
  }

  @Override
  public float getHeight() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return false;
  }

}
