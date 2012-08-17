/**
 * 
 */
package org.xreports.engine.output.impl.itext;


import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement.HAlign;
import org.xreports.engine.source.AbstractElement.VAlign;

import com.itextpdf.text.Element;

/**
 * Implementazione di elemento per motore iText
 * @author pier
 * 
 */
public abstract class ElementoIText implements Elemento {
  private static int elem_count = 0;
  private int        m_elemID   = 0;

  /** elemento padre nella gerarchia */
  private Elemento   c_parent   = null;

  public ElementoIText() {
    synchronized (ElementoIText.class) {
      elem_count++;
      m_elemID = elem_count;
    }
  }

  /**
   * Ritorna id univoco di questo elemento.
   * E' garantita univocità fra tutti gli elementi di output
   * di questo report.
   * @return id univoco
   */
  public int getElementID() {
    return m_elemID;
  }

  @Override
  public Elemento getParent() {
    return c_parent;
  }

  @Override
  public void setParent(Elemento elem) {
    c_parent = elem;
  }

  /**
   * Ritorna il primo elemento della classe passata antenato di questo elemento. Ritorna null se non viene trovato.
   * 
   * @param cls
   *          classe dell'elemento cercato
   * @return elemento antenato di classe cls
   */
  public ElementoIText closest(Class<? extends ElementoIText> cls) {
    ElementoIText padre = (ElementoIText) getParent();
    if (padre == null) {
      return null;
    }
    //vediamo se fabri mette le parentesi graffe anche qui...
    if (padre.getClass().equals(cls)) {
      return padre;
    }
    return padre.closest(cls);
  }

  /**
   * Converte gli elementi dell'enumerato VAlign nelle corrispondenti costanti
   * di iText
   * @param align valore da convertire
   * @return costante iText corrispondente
   */
  public int getVAlignForItext(VAlign align) {
    int valign = Element.ALIGN_MIDDLE;
    if (align == VAlign.BOTTOM) {
      valign = Element.ALIGN_BOTTOM;
    } else if (align == VAlign.MIDDLE) {
      valign = Element.ALIGN_MIDDLE;
    } else if (align == VAlign.TOP) {
      valign = Element.ALIGN_TOP;
    }
    return valign;
  }

  /**
   * Converte gli elementi dell'enumerato HAlign nelle corrispondenti costanti
   * di iText
   * @param align valore da convertire
   * @return costante iText corrispondente
   */
  public int getHAlignForItext(HAlign align) {
    int halign = Element.ALIGN_MIDDLE;
    if (align == HAlign.CENTER) {
      halign = Element.ALIGN_CENTER;
    } else if (align == HAlign.JUSTIFIED) {
      halign = Element.ALIGN_JUSTIFIED;
    } else if (align == HAlign.LEFT) {
      halign = Element.ALIGN_LEFT;
    } else if (align == HAlign.RIGHT) {
      halign = Element.ALIGN_RIGHT;
    }
    return halign;
  }

  /**
   * Calcolo della larghezza massima dell'elemento  (in punti). Ogni elemento deve auto-calcolarsi la sua larghezza. Alcuni elementi non hanno
   * una larghezza ben definita, come ad esempio i blocchi di testo libero.
   */
  public abstract float calcAvailWidth();

  /**
   * Ritorna l'altezza dell'elemento (in punti). Ogni elemento deve auto-calcolarsi la sua altezza. 
   * Alcuni elementi non hanno una altezza ben definita, in tal caso ritornano 0.
   * Altri elementi non riescono a calcolare un'altezza precisa ma ritornano un'altezza minima
   */
  public abstract float getHeight();
  
  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#fineGenerazione()
   */
  @Override
  public void fineGenerazione()  throws GenerateException {
    //no-op: lascio l'implementazione opzionale alle classi specifiche	  
  }

  /**
   * Indica se questo elemento è un block level element.
   * <ul>
   * <li><b>block element</b>: questo elemento va scritto a capo: gli elementi prima e dopo sono
   * su righe diverse (esempi: <tt>text</tt>, <tt>table</tt>,...).
   * 
   * <li><b>inline element</b>: questo elemento è inline: gli elementi prima e dopo sono
   * contigui a questo (esempi: <tt>field</tt>, <tt>image</tt>, blocco testo,...).
   * </ul>
   * @return true se questo elemento è un block level element
   */
  public abstract boolean isBlock();
  
}
