/**
 * 
 */
package org.xreports.engine.source;

import org.xreports.stampa.validation.ValidateException;
import org.xreports.util.Text;

/**
 * Questa classe si occupa del parsing degli attributi di specifica dimensione di un oggetto (width, height, size,...). Accetta le
 * seguenti stringhe:
 * <ul>
 * <li><tt><i>nnn.mmm</i></tt><b>cm</b>: quantità decimale di centimetri</li>
 * <li><tt><i>nnn</i></tt><b>%</b>: quantità intera percentuale</li>
 * <li><tt><i>nnn.mmm</i></tt><b>pt</b>: quantità decimale di punti</li>
 * </ul>
 * Se dopo il numero manca la specifica dell'unità di misura, si presuppone <b>pt</b> (punti). Fra l'unità di misura e la cifra NON
 * ci possono essere spazi.
 * 
 */
public class Measure {
  private static final float NOVALUE   = Float.MIN_VALUE;

  /** valore della misura in punti */
  private float              m_value   = 0f;

  /** true se la misura è in percentuale e non assoluta */
  private boolean            m_percent = false;

  /** true se la misura è in linee e non assoluta */
  private boolean            m_lines   = false;

  /**
   * Costruisce un oggetto parsando immediatamente la stringa passata. NB: in questo costruttore si ammette l'unità di misura
   * <b>%</b> ma non si ammette l'unità di misura <b>li</b> (linee).
   * 
   * @param desc
   *          stringa che identifica la misura
   * @throws ValidateException
   *           in caso di sintassi errata in desc
   */
  public Measure(String desc) throws ValidateException {
    this(desc, false, true);
  }

  /**
   * Costruisce una misura passando direttamente il valore in punti
   * @param points quantita di punti di questa misura
   */
  public Measure(float points)  {
    m_value = points;
  }
  
  /**
   * Costruisce un oggetto parsando immediatamente la stringa passata. NB: in questo costruttore si ammette l'unità di misura
   * <b>%</b>
   * 
   * @param desc
   *          stringa che identifica la misura
   * @param lines
   *          impostare a <tt>true</tt> se è ammessa anche l'unità di misura <b>li</b> (linee)
   * @throws ValidateException
   *           in caso di sintassi errata in desc
   */
  public Measure(String desc, boolean lines) throws ValidateException {
    this(desc, lines, true);
  }

  /**
   * Costruisce un oggetto parsando immediatamente la stringa passata
   * 
   * @param desc
   *          stringa che identifica la misura
   * @param lines
   *          impostare a <tt>true</tt> se è ammessa anche l'unità di misura <b>li</b> (linee)
   * @param percent
   *          impostare a <tt>true</tt> se è ammessa anche l'unità di misura <b>%</b>
   * @throws ValidateException
   *           in caso di sintassi errata in desc
   */
  public Measure(String desc, boolean lines, boolean percent) throws ValidateException {
    //se la descrizione è vuota assumo la misura 0
    if (desc == null || desc.length() == 0) {
      return;
    }
    String d = desc.toLowerCase().trim();
    float numeric = Text.toFloat(d, NOVALUE);

    if (d.endsWith("%") && percent) {
      int n = Text.toInt(d.substring(0, d.length() - 1), -1);
      if (n <= 0) {
        throw new ValidateException("Devi specificare un intero positivo prima del segno '%'");
      }
      m_value = n;
      m_percent = true;
    } else if (d.endsWith("cm")) {
      float n = Text.toFloat(d.substring(0, d.length() - 2), NOVALUE);
      if (n == NOVALUE) {
        throw new ValidateException("Devi specificare un numero prima dell'unità 'cm'");
      }
      m_value = cmToPoints(n);
    } else if (d.endsWith("li") && lines) {
      float n = Text.toFloat(d.substring(0, d.length() - 2), NOVALUE);
      if (n == NOVALUE) {
        throw new ValidateException("Devi specificare un numero prima dell'unità 'li'");
      }
      m_value = n;
      m_lines = true;
    } else if (d.endsWith("pt") || numeric != NOVALUE) {
      if (numeric != NOVALUE) {
        m_value = numeric;
      } else {
        float n = Text.toFloat(d.substring(0, d.length() - 2), NOVALUE);
        if (n == NOVALUE) {
          throw new ValidateException("Devi specificare un numero prima dell'unità 'pt'");
        }
        m_value = n;
      }
    } else {
      throw new ValidateException("Misura non riconosciuta: " + desc);
    }
  }

  /**
   * Converte una misura espressa in centimetri in punti.
   * 
   * @param cm
   *          centimetri da convertire
   * @return misura convertita in punti
   */
  private float cmToPoints(float cm) {
    return cm * 72f / 2.54f;
  }

  /**
   * Converte una misura espressa in punti in centimetri.
   * 
   * @param pt
   *          punti da convertire
   * @return misura convertita in centimetri
   */
  public float pointsToCm(float pt) {
    return pt / 72f * 2.54f;
  }

  /**
   * Ritorna il valore numerico della misura. Se la misura è stata data in cm, viene ritornata la misura corrispondente in punti, se
   * è stata data in percentuale ritorna il valore passato. Esempi:<br>
   * <tt>36</tt> --> getValue() == 36 <br/>
   * <tt>2cm</tt> --> getValue() == 56.69 <br/>
   * <tt>75%</tt> --> getValue() == 75
   */
  public float getValue() {
    return m_value;
  }

  /**
   * Ritorna true se questa misura è nulla (cioè zero)
   * 
   * @return
   */
  public boolean isZero() {
    return m_value == 0f;
  }

  /**
   * Ritorna il valore 'scalato', cioè se è un valore percentuale, ritorna la misura assoluta calcolando la percentuale della misura
   * passata in <tt>size</tt>, se non è un valore percentuale ritorna {@link #getValue()}.
   * 
   * @param size
   *          dimensione rispetto a cui scalare nel caso di percentuale
   */
  public float scale(float size) {
    if (m_percent) {
      return size * m_value / 100f;
    }
    return m_value;
  }

  /**
   * True se la specifica è in termini percentuali e non assoluta.
   */
  public boolean isPercent() {
    return m_percent;
  }

  /**
   * True se la specifica è in termini di linee e non assoluta.
   */
  public boolean isLines() {
    return m_lines;
  }

  @Override
  public String toString() {
    if (m_percent) {
      return m_value + "%";
    } else if (m_lines) {
      return m_value + "lines";
    } else {
      return m_value + "pt (=" + pointsToCm(m_value) + "cm)";
    }
  }

}
