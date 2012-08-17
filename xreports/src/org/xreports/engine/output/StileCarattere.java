/**
 * 
 */
package org.xreports.engine.output;

import org.xreports.Destroyable;
import org.xreports.engine.StampaException;


/**
 * @author pier
 * 
 */
public class StileCarattere implements Destroyable {
  public static final Colore    COLORE_DEFAULT      = Colore.NERO;
  public static final int       SIZE_DEFAULT        = 8;
  public static final String    FAMILY_DEFAULT      = "Helvetica";
  public static final String    SYSTEM_DEFAULT_NAME = "_defaultStyle";
  private static StileCarattere stileDefault        = null;

  private String                m_name;
  private String                m_source;
  private String                m_family;
  private float                 m_size;
  private boolean               m_italic            = false;
  private boolean               m_bold              = false;
  private boolean               m_underline         = false;
  private boolean               m_default           = false;
  private Colore                m_colore              = null;

  public StileCarattere(String name, String fontFamily, float size) throws StampaException {
    if (name == null || name.trim().length() == 0) {
      throw new StampaException("Una font non può avere nome nullo/vuoto.");
    }
    m_name = name;
    m_family = fontFamily;
    m_size = size;
  }

  public StileCarattere(String name, StileCarattere copia) throws StampaException {
    this(name, copia.getFamily(), copia.getSize());
    m_bold = copia.isBold();
    m_underline = copia.isUnderline();
    m_italic = copia.isItalic();
    m_source = copia.getSourceFont();
    m_colore = copia.getColore();
  }

  /**
   * Ritorna uno stile di default che sarà usato nel caso non ci sia un default definito dall'utente.
   */
  public static StileCarattere getStileDefault() {
    try {
      if (stileDefault == null) {
        stileDefault = new StileCarattere(SYSTEM_DEFAULT_NAME, FAMILY_DEFAULT, SIZE_DEFAULT);
        stileDefault.setColore(COLORE_DEFAULT);
      }
    } catch (Exception e) {
      //volutamente ignorato
    }
    return stileDefault;
  }

  /**
   * @return the name
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the size
   */
  public float getSize() {
    return m_size;
  }

  /**
   * @param p_size
   *          the size to set
   */
  public void setSize(float p_size) {
    m_size = p_size;
  }

  /**
   * @return the italic
   */
  public boolean isItalic() {
    return m_italic;
  }

  /**
   * @param p_italic
   *          the italic to set
   */
  public void setItalic(boolean p_italic) {
    m_italic = p_italic;
  }

  /**
   * @return the bold
   */
  public boolean isBold() {
    return m_bold;
  }

  /**
   * @param p_bold
   *          the bold to set
   */
  public void setBold(boolean p_bold) {
    m_bold = p_bold;
  }

  /**
   * @return the underline
   */
  public boolean isUnderline() {
    return m_underline;
  }

  /**
   * @param p_underline
   *          the underline to set
   */
  public void setUnderline(boolean p_underline) {
    m_underline = p_underline;
  }

  /**
   * @return the colore
   */
  public Colore getColore() {
    return m_colore;
  }

  /**
   * @param p_colore
   *          the colore to set
   */
  public void setColore(Colore p_colore) {
    m_colore = p_colore;
  }

  /**
   * @return the source
   */
  public String getSourceFont() {
    return m_source;
  }

  /**
   * @param p_source
   *          the source to set
   */
  public void setSourceFont(String p_source) {
    m_source = p_source;
  }

  /**
   * @return the default
   */
  public boolean isDefault() {
    return m_default;
  }

  /**
   * @param p_default
   *          the default to set
   */
  public void setDefault(boolean p_default) {
    m_default = p_default;
  }

  /**
   * @return the family
   */
  public String getFamily() {
    return m_family;
  }

  /**
   * @param p_family
   *          the family to set
   */
  public void setFamily(String p_family) {
    m_family = p_family;
  }

  @Override
  public void destroy() {
    m_name = null;
    m_source = null;
    m_family = null;
    if (m_colore != null) {
      m_colore.destroy();
      m_colore = null;
    }
  }

}
