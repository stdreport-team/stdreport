/**
 * 
 */
package org.xreports.engine.output;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.xreports.Destroyable;
import org.xreports.engine.validation.ValidateException;


/**
 * @author pier
 * 
 */
public class Colore implements Destroyable {
  public static final Colore NERO           = new Colore("nero", 0, 0, 0);
  public static final Colore GRIGIO         = new Colore("grigio", 151, 151, 151);
  public static final Colore GRIGIO_CHIARO  = new Colore("grigio_c", 215, 215, 215);
  public static final Colore GRIGIO_SCURO   = new Colore("grigio_s", 90, 90, 90);
  public static final Colore ROSSO          = new Colore("rosso", 255, 0, 0);
  public static final Colore ROSSOSCURO     = new Colore("rosso_s", 139, 0, 0);
  public static final Colore ROSSOCHIARO    = new Colore("rosso_c", 255, 140, 140);
  public static final Colore VERDE          = new Colore("verde", 0, 255, 0);
  public static final Colore VERDESCURO     = new Colore("verde_s", 0, 100, 0);
  public static final Colore VERDECHIARO    = new Colore("verde_c", 124, 218, 125);
  public static final Colore BLU            = new Colore("blu", 0, 0, 255);
  public static final Colore BLUSCURO       = new Colore("blu_s", 0, 0, 139);
  public static final Colore BLUCHIARO      = new Colore("blu_c", 122, 151, 233);
  public static final Colore GIALLO         = new Colore("giallo", 255, 255, 0);
  public static final Colore GIALLOCHIARO   = new Colore("giallo_c", 255, 255, 186);
  public static final Colore GIALLOSCURO    = new Colore("giallo_s", 255, 230, 87);
  public static final Colore BIANCO         = new Colore("bianco", 255, 255, 255);
  public static final Colore ARANCIO        = new Colore("arancio", 255, 165, 0);

  private static Set<Colore> builtin_colors = new HashSet<Colore>();

  static {
    builtin_colors.add(NERO);
    builtin_colors.add(GRIGIO);
    builtin_colors.add(GRIGIO_CHIARO);
    builtin_colors.add(GRIGIO_SCURO);
    builtin_colors.add(ROSSO);
    builtin_colors.add(ROSSOSCURO);
    builtin_colors.add(ROSSOCHIARO);
    builtin_colors.add(VERDE);
    builtin_colors.add(VERDESCURO);
    builtin_colors.add(VERDECHIARO);
    builtin_colors.add(BLU);
    builtin_colors.add(BLUSCURO);
    builtin_colors.add(BLUCHIARO);
    builtin_colors.add(GIALLO);
    builtin_colors.add(GIALLOCHIARO);
    builtin_colors.add(GIALLOSCURO);
    builtin_colors.add(ARANCIO);
    builtin_colors.add(BIANCO);
  }
  private String             m_name;

  private int                m_red          = 0;
  private int                m_green        = 0;
  private int                m_blue         = 0;
  private int                m_alpha        = 0;

  public Colore(String name, int red, int green, int blue) {
    this(name, red, green, blue, 255);
  }

  /**
   * Costruttore con i 3 colori base + il canale alpha per l'opacità
   * 
   * @param name
   *          nome del colore
   * @param red
   *          0-255: componente rossa
   * @param green
   *          0-255: componente verde
   * @param blue
   *          0-255: componente blue
   * @param alpha
   *          0-255: opacità (0=trasparente, 255=opaco)
   */
  public Colore(String name, int red, int green, int blue, int alpha) {
    m_red = red;
    m_blue = blue;
    m_green = green;
    m_alpha = alpha;
    m_name = name;
  }

  public static Set<Colore> getBuiltinColors() {
    return builtin_colors;
  }

  /**
   * @return the name
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the red
   */
  public int getRed() {
    return m_red;
  }

  /**
   * @param p_red
   *          the red to set
   */
  public void setRed(int p_red) {
    m_red = p_red;
  }

  /**
   * @return the green
   */
  public int getGreen() {
    return m_green;
  }

  /**
   * @param p_green
   *          the green to set
   */
  public void setGreen(int p_green) {
    m_green = p_green;
  }

  /**
   * @return the blue
   */
  public int getBlue() {
    return m_blue;
  }

  /**
   * @param p_blue
   *          the blue to set
   */
  public void setBlue(int p_blue) {
    m_blue = p_blue;
  }

  public int getAlpha() {
    return m_alpha;
  }

  public void setAlpha(int alpha) {
    m_alpha = alpha;
  }

  /**
   * Crea un'istanza di Colore, dato nome e valore
   * 
   * @param colorName
   *          nome del colore
   * @param value
   *          valore stringa del colore, come appare nel sorgente XML
   * @param lineNum
   *          numero linea sorgente XML. Se 0, è ignorato
   * @return colore creato
   * @throws ValidateException
   *           nel caso value non sia della sintassi corretta
   */
  public static Colore getInstance(String colorName, String value, int lineNum) throws ValidateException {
    int[] rgb = Colore.identificaColorComponents(value, lineNum);
    return new Colore(colorName, rgb[0], rgb[1], rgb[2], rgb[3]);
  }

  /**
   * Fa il parsing della specifica di colore e ritorna le 3 componenti
   * red,gree,blue in un array di interi. Riconosce sia il formato
   * <tt>rrr ggg bbb</tt> con i 3 colori in decimale, che il formato stile CSS
   * <tt>#rrggbb</tt> con i 3 colori in esadecimale.
   * 
   * @param szValoreAttributo
   *          valore dell'attributo <b>rgb</b> del tag <b>color</b>
   * @throws ValidateException
   */
  private static int[] identificaColorComponents(String szValoreAttributo, int lineNum) throws ValidateException {
    String lineDesc = "";
    if (lineNum > 0)
      lineDesc = " (linea " + lineNum + ")";
    int[] rgb = new int[4];
    rgb[3] = 255;  //alpha default=255=opaco al 100%
    szValoreAttributo = szValoreAttributo.trim();
    if (szValoreAttributo.startsWith("#")) {
      //assumo un valore stile css, cioè #rrggbb in esadecimale
      if (szValoreAttributo.equalsIgnoreCase("#000")) {
        //shortcut per #000000
        rgb[0] = rgb[1] = rgb[2] = 0;
      } else if (szValoreAttributo.equalsIgnoreCase("#fff")) {
        //shortcut per #FFFFFF
        rgb[0] = rgb[1] = rgb[2] = 255;
      } else {
        if (szValoreAttributo.length() != 7) {
          throw new ValidateException("La specifica di colore '%s' non e' corretta %s", szValoreAttributo, lineDesc);
        }
        String red = szValoreAttributo.substring(1, 3);
        String green = szValoreAttributo.substring(3, 5);
        String blue = szValoreAttributo.substring(5, 7);
        rgb[0] = Integer.parseInt(red, 16);
        rgb[1] = Integer.parseInt(green, 16);
        rgb[2] = Integer.parseInt(blue, 16);
      }
    } else {
      //valore con 3/4 interi separati da spazio
      Scanner scanner = new Scanner(szValoreAttributo);
      String delimiter = " ";
      if (szValoreAttributo.contains(","))
        delimiter = ",";
      scanner.useDelimiter(delimiter);
      int count = 0;
      for (int i = 0; scanner.hasNext(); i++) {
        String szToken = scanner.next();
        try {
          rgb[i] = Integer.parseInt(szToken);
          count++;
        } catch (NumberFormatException e) {
          throw new ValidateException("Componente colore non numerico: '%s'", szValoreAttributo, lineDesc);
        }
      }
      if (count < 3 || count > 4) {
        throw new ValidateException("Specifica colore '%s' errata, manca un componente rgb", szValoreAttributo, lineDesc);
      }
    }
    return rgb;
  }

  @Override
  public void destroy() {
    m_name = null;    
  }

}
