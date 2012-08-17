package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xreports.datagroup.Group;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.BloccoTesto;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.util.Text;

public class TextNode extends AbstractNode {
  private String c_testo = null;
  private BookmarkElement  c_bkmElement;

  public TextNode(String contenuto, int lineNum, int colNum) {
    super(contenuto, lineNum, colNum);
    c_testo = contenuto;
  }

  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    boolean generate = true;  //!isSpace();
    
    List<Elemento> listaElementi = new LinkedList<Elemento>();
    if (generate) {
      if (isDebugData()) {
        stampa.debugTextnode(this);
      }
      BloccoTesto bloccoTesto = stampa.getFactoryElementi().creaBloccoTesto(stampa, this, padre);
      bloccoTesto.fineGenerazione();
      listaElementi.add(bloccoTesto);      
    }
    return listaElementi;
  }

  public String getTesto() {
    return c_testo;
  }

  /**
   * Ritorna true se questo nodo testo è composto solo da caratteri spaziatori,
   * quali spazio, tab, newline, form-feed,...
   */
  public boolean isSpace() {
    if (c_testo != null)
      return Text.isOnlySpace(c_testo);
    
    return true;
  }
  
  public void setTesto(String testo) {
    c_testo = testo;
  }

  @Override
  public String toString() {
    return "TextNode: \"" + c_testo + "\"";
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractNode#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractNode#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }
  
  @Override
  public void destroy() {    
    super.destroy();
    c_testo = null;
    c_bkmElement = null;
  }

  public String getBookmarkText() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.getBookmarkText();
    }
    return null;
  }

  public boolean isBookmark() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.isBookmark();
    }
    return false;
  }

  public int getBookmarkLevel() throws ResolveException {
    if (c_bkmElement != null) {
      return c_bkmElement.getBookmarkLevel();
    }
    return 0;
  }

  /**
   * Indica che questo {@link TextNode} ha un bookmark di riferimento. Quando questo nodo
   * verrà generato, se il bookmark è verificato verrà generato sul documento finale avendo questo nodo
   * come riferimento per la posizione verticale.
   * 
   * @param bkmElement bookmark di riferimento per questo nodo
   */
  public void setBookmarkElement(BookmarkElement bkmElement) {
    c_bkmElement = bkmElement;
  }
  
}
