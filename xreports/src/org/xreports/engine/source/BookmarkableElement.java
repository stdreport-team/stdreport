package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.validation.ValidateException;

/**
 * elemento astratto padre di tutti gli elementi bookmarkabili.
 * Definisce gli attributi standard per gli elementi che supportano i bookmark
 * @author pier
 *
 */
public abstract class BookmarkableElement extends AbstractElement {
  private static final String ATTRIB_BOOKMARK = "bookmark";
  private static final String ATTRIB_BMTEXT   = "bookmarkText";
  private static final String ATTRIB_BMLEVEL  = "bookmarkLevel";

  private BookmarkElement  c_bkmElement;
  
  public BookmarkableElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  public BookmarkableElement(XReport stp, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stp, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_BOOKMARK, String.class, null, TAG_BOOLEAN);
    addAttributo(ATTRIB_BMTEXT, String.class, null, TAG_VALUE);
    addAttributo(ATTRIB_BMLEVEL, String.class, "1", TAG_VALUE);
  }

  /**
   * Ritorna il simbolo in cima all'albero sintattico ottenuto dal parsing
   * dell'attributo <var>bookmark</var>. In fase di generazione verrà valutato
   * per verificare se questo elemento ha un bookmark associato.
   * 
   * @return simbolo radice dell'espressione parsata dell'attributo
   *         <var>bookmark</var>
   */
  public Symbol getBookmarkSymbol() {
    return getAttrSymbol(ATTRIB_BOOKMARK);
  }

  /**
   * Ritorna il simbolo in cima all'albero sintattico ottenuto dal parsing
   * dell'attributo <var>bookmarkText</var>. In fase di generazione verrà valutato
   * per verificare se questo elemento ha un bookmark associato.
   * 
   * @return simbolo radice dell'espressione parsata dell'attributo
   *         <var>bookmarkText</var>
   */
  public Symbol getBookmarkTextSymbol() {
    return getAttrSymbol(ATTRIB_BMTEXT);
  }

  /**
   * Ritorna il simbolo in cima all'albero sintattico ottenuto dal parsing
   * dell'attributo <var>bookmarkLevel</var>. In fase di generazione verrà valutato
   * per verificare se questo elemento ha un bookmark associato.
   * 
   * @return simbolo radice dell'espressione parsata dell'attributo
   *         <var>bookmarkLevel</var>
   */
  public Symbol getBookmarkLevelSymbol() {
    return getAttrSymbol(ATTRIB_BMLEVEL);
  }
  
  /**
   * Valuta l'espressione dell'attributo <var>bookmark</var> e ne ritorna il valore.
   * @return true sse questo elemento ha associato un bookmark
   * @throws EvaluateException nel caso l'espressione non sia booleana o sia sintatticamente errata
   */
  public boolean isBookmark() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.isBookmark();
    }
    boolean bBookmark = true;
    if (this instanceof BookmarkElement) {
      bBookmark = isVisible();
    }
    else if (isVisible()) {
      //solo se visible=true, controllo attributo bookmark
      if (getBookmarkSymbol() == null) {
        bBookmark = false;
      }
      else {        
        Object colValue = getAttributo(ATTRIB_BOOKMARK).getExpressionAsBoolean(this);
        if (colValue != null) {
          bBookmark = ((Boolean) colValue).booleanValue();
        }        
        else {
          bBookmark = false;          
        }
      }
    }
    return bBookmark;
  }

  /**
   * Valuta l'espressione dell'attributo <var>bookmarkText</var> e ne ritorna il valore.
   * @return testo risultato della valutazione; ritorna null se attributo non specificato
   * @throws EvaluateException nel caso l'espressione sia sintatticamente errata
   */
  public String getBookmarkText() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.getBookmarkText();
    }
    if (getBookmarkTextSymbol() == null) {
      return null;
    }
    Object colValue = getAttributo(ATTRIB_BMTEXT).getExpressionAsString(this);
    if (colValue != null) {
      return colValue.toString();
    }

    return null;
  }

  /**
   * Valuta l'espressione dell'attributo <var>bookmarkLevel</var> e ne ritorna il valore.
   * @return testo risultato della valutazione; ritorna 1 se attributo non specificato 
   * @throws EvaluateException nel caso l'espressione sia sintatticamente errata o non numerica
   */
  public int getBookmarkLevel() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.getBookmarkLevel();
    }
    
    //1=default level
    
    if (getBookmarkTextSymbol() == null) {
      return 1;
    }
    Object colValue = getAttributo(ATTRIB_BMLEVEL).getExpressionAsNumber(this);
    if (colValue != null) {
      return ((Number) colValue).intValue();
    }

    return 1;
  }

  /**
   * Ritorna elemento bookmark impostato con la {@link #setBookmarkElement(BookmarkElement)}.
   * @return bookmark di riferimento per questo chunk element
   */
  public BookmarkElement getBookmarkElement() {
    return c_bkmElement;
  }

  /**
   * Indica che questo {@link TextNode} ha un bookmark di riferimento. Quando questo nodo
   * verrà generato, se il bookmark è verificato verrà generato sul documento finale avendo questo nodo
   * come riferimento per la posizione verticale.
   * 
   * @param bkmElement bookmark di riferimento per questo nodo
   */
  public void setBookmarkElement(BookmarkElement bkmElement) {
    if (this instanceof BookmarkElement)
      throw new UnsupportedOperationException("setBookmarkElement su BookmarkElement");
    c_bkmElement = bkmElement;
  }

  @Override
  public void destroy() {
    super.destroy();
    c_bkmElement = null;
  }
}
