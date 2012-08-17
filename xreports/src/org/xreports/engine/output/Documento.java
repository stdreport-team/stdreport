package org.xreports.engine.output;

import java.util.List;

import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.MarginboxElement;
import org.xreports.engine.source.RulersElement;
import org.xreports.engine.source.WatermarkElement;
import org.xreports.engine.ResolveException;


public interface Documento extends Elemento {
  public void inizioDocumento() throws GenerateException;

  public void fineDocumento() throws GenerateException;

  public void saltaPagina() throws GenerateException;

  public int getCurrentPageNumber() throws ResolveException;

  /**
   * @return il titolo del documento
   */
  public String getDocumentTitle();

  /**
   * @param documentTitle
   *          titolo da impostare
   */
  public void setDocumentTitle(String documentTitle);

  /**
   * @return il subject del documento
   */
  public String getDocumentSubject();

  /**
   * @param documentSubject
   *          il subject da impostare
   */
  public void setDocumentSubject(String documentSubject);

  /**
   * @return l'autore del documento
   */
  public String getDocumentAuthor();

  /**
   * @param documentAuthor
   *          l'autore da impostare
   */
  public void setDocumentAuthor(String documentAuthor);
  
  public void setMarginboxElement(MarginboxElement e);
  public MarginboxElement getMarginboxElement();

  public void setRulersElement(RulersElement e);
  public RulersElement getRulersElement();

  public void addWatermarkElement(WatermarkElement e);
  public List<WatermarkElement> getWatermarkElements();

  /**
   * Indica che il pannello dei bookmarks deve essere visibile all'apertura del documento
   * @param opened true=visibile, false=invisibile
   */ 
  public void setBookmarksOpened(boolean opened);
}
