/** 
 *  
 */
package org.xreports.engine.source;

import java.util.Scanner;

import org.xreports.stampa.output.Colore;
import org.xreports.stampa.validation.ValidateException;

/**
 * Riconosce una sintassi del seguente tipo: "misura stile colore".
 * 
 */
public class Border {
  public static final String DEF_COLOR = "nero";
  public static final String DEF_STYLE = "solid";
  public static final int    BOX       = 0;
  public static final int    TOP       = 1;
  public static final int    RIGHT     = 2;
  public static final int    BOTTOM    = 3;
  public static final int    LEFT      = 4;

  public enum BorderStyle {
    SOLID, DOTTED, DASHED, ROUNDED, DOUBLE;
  }

  private boolean     m_hasBorder = false;

  private Measure     m_measure;
  private BorderStyle m_style;
  private String      m_refColor;
  private int         m_position  = BOX;
  private Colore      m_color;

  public static Border getDefaultBorder() {
    try {
      return new Border("0");
    } catch (ValidateException e) {
      return null;
    }
  }

  /**
   * Riconosce una sintassi del seguente tipo: "misura [stile [colore]]". Nel caso colore sia assente
   * 
   * @param desc
   * @throws ValidateException
   */
  public Border(String desc) throws ValidateException {
    initBorder(desc, BOX);
  }

  /**
   * Riconosce una sintassi del seguente tipo: "misura [stile [colore]]". Nel caso colore sia assente
   * 
   * @param desc
   * @throws ValidateException
   */
  public Border(String desc, int position) throws ValidateException {
    initBorder(desc, position);
  }

  private void initBorder(String desc, int position) throws ValidateException {
    try {
      m_position = position;
      if (desc == null || desc.length() == 0 || desc.equals("0")) {
        return;
      }
      Scanner scan = new Scanner(desc);
      String szSpessore = scan.next();
      m_measure = new Measure(szSpessore);
      String style = DEF_STYLE;
      if (scan.hasNext()) {
        style = scan.next();
      }
      String tmp = DEF_COLOR;
      if (scan.hasNext()) {
        tmp = scan.next();
      }
      if (Character.isLetter(tmp.charAt(0))) {
        m_refColor = tmp;
      }
      else {
        m_color = Colore.getInstance("dummy", tmp, 0);
      }
      m_hasBorder = !m_measure.isZero();
      applyStyle(style);
    } catch (ValidateException e) {
      throw e;
    } catch (Exception e) {
      throw new ValidateException("Specifica bordo non valida", e);
    }
  }

  private void applyStyle(String s) {
    m_style = BorderStyle.valueOf(s.toUpperCase());
  }

  /**
   * Ritorna <tt>true</tt> se questo oggetto definisce un bordo; ritorna <tt>false</tt> se nessun bordo deve essere disegnato.
   */
  public boolean hasBorder() {
    return m_hasBorder;
  }

  /**
   * Ritorna il colore del bordo. E' il nome del colore come definito nella sezione colors
   * E' alternativo a {@link #getColor()}.
   */
  public String getColorName() {
    return m_refColor;
  }

  /**
   * Ritorna il colore del bordo. E' la specifica diretta del colore, senza passare per il nome del colore.
   * E' alternativo a {@link #getColorName()}.
   */
  public Colore getColor() {
    return m_color;
  }

  /**
   * Ritorna la dimensione del bordo in punti
   */
  public float getSize() {
    return m_measure.getValue();
  }

  /**
   * @return the position
   */
  public int getPosition() {
    return m_position;
  }

  /**
   * @return the style
   */
  public BorderStyle getStyle() {
    return m_style;
  }

  @Override
  public String toString() {
    return m_measure.toString() + m_style.toString() + m_refColor;
  }

}
