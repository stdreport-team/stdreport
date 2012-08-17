package org.xreports.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities per gestione stringhe.
 * 
 * Copyright: Copyright (c) 2001 Company: CISCOOP
 * 
 * @author Paride Zavoli
 * @version 1.0
 */
public class Text {

  private static java.util.Random m_rndGen      = new Random( (new Date().getTime()));

  public static int               CHAR_LETTERS  = 1;
  public static int               CHAR_DIGITS   = 2;
  public static int               CHAR_SPACE    = 4;
  public static int               CHAR_PUNCT    = 8;
  public static int               CHAR_ALL      = 127;

  // in alternativa: "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
  // Con questo pattern non valida email corrette del tipo: i.qwety@asdfg.wq
  //  private static final String     EMAIL_PATTERN = "^[A-Za-z0-9][A-Za-z0-9-_]+(\\.[A-Za-z][A-Za-z0-9-]*)*[A-Za-z0-9]*" + //
  //      "@[A-Za-z0-9][A-Za-z0-9-_]*(\\.[A-Za-z]+[A-Za-z0-9-]*)*" + //
  //      "(\\.[A-Za-z]{2,})$";
  private static final String     EMAIL_PATTERN = "^[A-Za-z0-9][A-Za-z0-9-_]*(\\.[A-Za-z][A-Za-z0-9-]*)*[A-Za-z0-9]*" + //
                                                    "@[A-Za-z0-9][A-Za-z0-9-_]*(\\.[A-Za-z]+[A-Za-z0-9-]*)*" + //
                                                    "(\\.[A-Za-z]{2,})$";
  private static Pattern          s_emailPatrn  = Pattern.compile(EMAIL_PATTERN);

  public Text() {
    //costruttore vuoto
  }

  /*
   * PIER 7-6-2010: funzione 'oscura' di cui non si conoscono utilizzi.
   * Commentata... public static int indexOf(String s, int fromIndex) { int
   * max=s.length(); int from = fromIndex; if (from < 0) { from = 0; } else if
   * (from >= max) { return -1; } for (int i=from; i < max; i++) { char
   * v=s.charAt(i); if (v == '&' || v == '<' || v == '>') { return i; } } return
   * -1; }
   */

  /**
   * Ritorna true se una stringa è numerica. Una stringa è considerata numerica
   * se è convertibile da Java in un numero intero o un double. Esempi:
   * <ul>
   * <li><b>123</b>: numerico</li>
   * <li><b>123.4</b>: numerico</li>
   * <li><b>123,4</b>: NON numerico</li>
   * <li><b>123e+3</b>: NON numerico</li>
   * </ul>
   */
  public static boolean isNumeric(String s) {
    boolean bRet = false;
    try {
      if (s != null) {
        @SuppressWarnings("unused")
        double d = Double.parseDouble(s);
        bRet = true;
      }
    } catch (Exception ex) {
      //
    }
    return bRet;
  }

  /**
   * Restituisce il valore booleano dell'oggetto passato.
   * <ul>
   * <li>Se l'oggetto è di classe Boolean, torna il suo valore</li>
   * 
   * <li>Se l'oggetto è di classe Number, si considera <tt>true</tt> qualsiasi
   * valore (convertito in Long) diverso da zero.</li>
   * 
   * <li>Se è di qualsiasi altra classe, viene convertito in stringa e si
   * considerà <tt>true</tt> la stringa "true" (case insensitive) e
   * <tt>false</tt> la stringa "false" (case insensitive); qualsiasi altra
   * stringa viene ignorata e ritornato il valore di default.</li>
   * 
   * <li>Se <var>obj</var> è <tt>null</tt>, viene ritornato <var>defValue</var></li>
   * 
   * </ul>
   * 
   * @param obj
   *          oggetto da convertire in boolean
   * @param defValue
   *          valore di default nel caso non si riesca a convertire <tt>obj</tt>
   * @return valore booleano dell'oggetto.
   */
  public static boolean toBoolean(Object obj, boolean defValue) {
    try {
      if (obj instanceof Boolean) {
        return ((Boolean) obj).booleanValue();
      } else if (obj instanceof Number) {
        return ((Number) obj).longValue() != 0;
      } else {
        if (obj.toString().toLowerCase().equals("true"))
          return true;
        else if (obj.toString().toLowerCase().equals("false"))
          return false;
        return defValue;
      }
    } catch (Exception ex) {
      return defValue;
    }
  }

  /**
   * Restituisce il valore int dell'oggetto passato oppure il valore di default
   * se l'oggetto non può essere interpretato come int. Se o è un'istanza di
   * java.lang.Number o una stringa di sole cifre la conversione avviene con
   * successo.
   * 
   * @param o
   *          oggetto da convertire in int
   * @param defValue
   *          valore di default
   * @return valore int dell'oggetto.
   */
  public static int toInt(Object o, int defValue) {
    int n;
    try {
      if (o instanceof Number)
        n = ((Number) o).intValue();
      else
        n = Integer.parseInt(o.toString());
    } catch (Exception ex) {
      n = defValue;
    }
    return n;
  }

  /**
   * Tenta di convertire la stringa passata come intero. Se è una stringa di
   * sole cifre la conversione avviene con successo, altrimenti ritorna il
   * valore di default.
   * 
   * @param s
   *          stringa da convertire
   * @param defValue
   *          valore di default
   * @return stringa convertita oppure defValue se stringa non convertibile
   */
  public static int toInt(String s, int defValue) {
    try {
      return Integer.parseInt(s);
    } catch (Exception ex) {
      return defValue;
    }
  }

  public static Integer toi(int vv) {
    return Integer.valueOf(vv);
  }

  /**
   * Se possibile, converte un object in un integer; ritorna null se
   * <code>o</code> non è convertibile.
   * 
   * @param o
   *          l'oggetto da convertire
   * @return conversione in <code>Integer</code> di <code>o</code> oppure
   *         <code>null</code> se non convertibile
   */
  public static Integer toInteger(Object o) {
    Integer retValue;
    if (o instanceof Number) {
      retValue = Integer.valueOf( ((Number) o).intValue());
    } else {
      try {
        retValue = new Integer(Integer.parseInt(o.toString()));
      } catch (Exception ex) {
        //errore di parsing: ritorno null
        retValue = null;
      }
    }
    return retValue;
  }

  /**
   * Se possibile, converte un object in un long; ritorna il valore di default
   * se non è convertibile.
   * 
   * @param obj
   *          l'oggetto da convertire
   * @return conversione in <code>long</code> di <code>obj</code> oppure
   *         <code>defValue</code> se <code>obj</code> non è convertibile
   */
  public static long toLong(Object obj, long defValue) {
    long l;
    try {
      if (obj instanceof Number)
        l = ((Number) obj).longValue();
      else
        l = Long.parseLong(obj.toString());
    } catch (Exception ex) {
      l = defValue;
    }
    return l;
  }

  /**
   * Se possibile, converte una stringa in un long; ritorna il valore di default
   * se non è convertibile.
   * 
   * @param s
   *          stringa da convertire
   * @return conversione in <code>long</code> di <code>s</code> oppure
   *         <code>defValue</code> se <code>s</code> non è convertibile
   */
  public static long toLong(String s, long defValue) {
    try {
      return Long.parseLong(s);
    } catch (Exception ex) {
      return defValue;
    }
  }

  /**
   * Se possibile, converte un object in un double; ritorna il valore di default
   * se non è convertibile.
   * 
   * @param obj
   *          l'oggetto da convertire
   * @return conversione in <code>double</code> di <code>obj</code> oppure
   *         <code>defValue</code> se <code>obj</code> non è convertibile
   */
  public static double toDouble(Object obj, double defValue) {
    double d;
    try {
      if (obj instanceof Number)
        d = ((Number) obj).doubleValue();
      else
        d = Double.parseDouble(obj.toString());
    } catch (Exception ex) {
      d = defValue;
    }
    return d;
  }

  /**
   * Se possibile, converte una stringa in un double; ritorna il valore di
   * default se non è convertibile.
   * 
   * @param s
   *          stringa da convertire
   * @return conversione in <code>double</code> di <code>s</code> oppure
   *         <code>defValue</code> se <code>s</code> non è convertibile
   */
  public static double toDouble(String s, double defValue) {
    try {
      return Double.parseDouble(s);
    } catch (Exception ex) {
      return defValue;
    }
  }

  /**
   * Se possibile, converte un object in un float; ritorna il valore di default
   * se non è convertibile.
   * 
   * @param obj
   *          l'oggetto da convertire
   * @return conversione in <code>float</code> di <code>obj</code> oppure
   *         <code>defValue</code> se <code>obj</code> non è convertibile
   */
  public static float toFloat(Object obj, float defValue) {
    float fVal;
    try {
      if (obj instanceof Number)
        fVal = ((Number) obj).floatValue();
      else
        fVal = Float.parseFloat(obj.toString());
    } catch (Exception ex) {
      fVal = defValue;
    }
    return fVal;
  }

  /**
   * Se possibile, converte una stringa in un float; ritorna il valore di
   * default se non è convertibile.
   * 
   * @param s
   *          stringa da convertire
   * @return conversione in <code>float</code> di <code>s</code> oppure
   *         <code>defValue</code> se <code>s</code> non è convertibile
   */
  public static float toFloat(String s, float defValue) {
    try {
      return Float.parseFloat(s);
    } catch (Exception ex) {
      return defValue;
    }
  }

  /**
   * Ritorna un double, se la stringa passata è in qualche modo convertibile.
   * 
   * <br>
   * Se la stringa non è un double, da' una exception. Risolve il problema di
   * conversione dato dal formato locale dei double: + ITALIA: 1.250,33 + USA :
   * 1,250.33 Entrambe le stringhe vengono interpretate correttamente.
   * 
   * @param val
   *          String da convertire
   * @return Double stringa convertita; la stringa vuota o di soli spazi viene
   *         convertita in 0.0
   */
  public static Double stringToDouble(String val) {
    // Ragi - 15 nov 2003
    // Per uniformare il comportamento a quello della setValue(String);
    // se mi arriva "" la interpreto come 0.0
    String newVal = val;
    if (newVal.trim().length() == 0)
      newVal = "0.0";
    if (newVal.indexOf(',', 0) >= 0) {
      newVal = Text.replace(newVal, '.', "");
      newVal = Text.replace(newVal, ',', ".");
      return new Double(String.valueOf(Double.parseDouble(newVal)));
    }
    return new Double(newVal);
  }

  /**
   * claudio 14/11/2005 - è una vatte in tranmask nella transformXML ebbe dei
   * problemi<br>
   * Siccome in DOM le lettere accentate non sono <b>gradite</b>, questa
   * funzione converte le eventuali lettere accentate nei loro simili ASCII con
   * <b>apice singolo</b>
   * 
   * @param sz
   *          String con lettere accentate
   * @return String con succedanei
   */
  public static String convertiLettereAccentate(String sz) {
    String szRet = sz.replaceAll("è", "e'");
    szRet = szRet.replaceAll("é", "e'");
    szRet = szRet.replaceAll("à", "a'");
    szRet = szRet.replaceAll("ì", "i");
    szRet = szRet.replaceAll("ù", "u");
    szRet = szRet.replaceAll("ò", "o'");
    return szRet;
  }

  /**
   * Sostituisce le vocali accentate con le vocali naturali. Esempio:
   * 
   * <pre>
   *   convertiLettereAccentate("àbcèx", true) --> "a'bce'x"
   *   convertiLettereAccentate("àbcèx", false) --> "abcex"
   * </pre>
   * 
   * @param sz
   *          stringa con lettere accentate
   * @param aggiungiAccento
   *          se true, dopo ogni vocale sostituita aggiunge accento
   * 
   * @return stringa con vocali sostituite
   */
  public static String convertiLettereAccentate(String sz, boolean aggiungiAccento) {
    String accento = aggiungiAccento ? "'" : "";
    String szRet = sz.replaceAll("è", "e" + accento);
    szRet = szRet.replaceAll("é", "e" + accento);
    szRet = szRet.replaceAll("à", "a" + accento);
    szRet = szRet.replaceAll("ì", "i" + accento);
    szRet = szRet.replaceAll("ù", "u" + accento);
    szRet = szRet.replaceAll("ò", "o" + accento);
    return szRet;
  }

  /**
   * Converte ogni lettera accentata presente nella stringa in entità HTML
   * 
   * @param s1
   *          stringa da aggiustare
   * @return stringa con le lettere accentate sostituite
   */
  public static String replaceAccenti(String s1) {
    String s = "";
    s = replace(s1, 'à', "&#224;");
    s = replace(s, 'è', "&#232;");
    s = replace(s, 'é', "&#233;");
    s = replace(s, 'ì', "&#236;");
    s = replace(s, 'ò', "&#242;");
    s = replace(s, 'ù', "&#249;");
    return s;
  }

  /**
   * Sostituisce tutte le lettere accentate con relative entità HTML
   * 
   * @param s1
   *          stringa da smanettare
   * @return stringa smanettata
   */
  public static String replaceAccentiHTML(String s1) {
    String s = "";
    s = replace(s1, 'à', "&agrave;");
    s = replace(s1, 'á', "&aacute;");
    s = replace(s, 'è', "&egrave;");
    s = replace(s, 'é', "&eacute;");
    s = replace(s, 'ì', "&igrave;");
    s = replace(s, 'ò', "&ograve;");
    s = replace(s, 'ù', "&ugrave;");
    return s;
  }

  /**
   * Aggiusta la stringa passata come segue:
   * <ul>
   * <li>rimpiazza le lettere accentate con entità HTML</li>
   * <li>leva i caratteri che non sono lettere, numeri, '_', '-'</li>
   * </ul>
   * 
   * @param sz
   *          stringa da aggiustare
   * @return stringa aggiustata
   */
  public static String convertiAId(String sz) {
    String s = replaceAccenti(sz);
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      if ( !Character.isLetter(c) && !Character.isDigit(c) && (c != '-') && (c != '_')) {
        //        System.out.println("Caratteraccio!="+c);
      } else
        sb.append(c);
    }
    return sb.toString();
  }

  public static String replaceAscii(String s1, char c) {
    String s = "";
    int as = c;
    s = replace(s1, c, "&#" + as + ";");
    return s;
  }

  /**
   * Rimpiazza il carattere c1 con la STRINGA s2 e ritorna la stringa nuova
   * 
   * @param s1
   *          String
   * @param c1
   *          char
   * @param s2
   *          String
   * @return String
   */
  public static String replace(String s1, char c1, String s2) {
    StringBuffer st = new StringBuffer(2000);
    int i = s1.indexOf(c1);
    //int i=indexOf(s1,0);
    int k = 0;
    while (i >= 0) {
      st.append(s1.substring(k, i));
      k = i + 1;
      st.append(s2);
      i = s1.indexOf(c1, i + 1);
      //i=indexOf(s1,i+1);
    }
    st.append(s1.substring(k));
    return st.toString();
  }

  /**
   * Rimpiazza i caratteri a posizione index1-index2 con la STRINGA s2 e ritorna
   * la stringa nuova
   * 
   * @param s1
   *          String
   * @param index1
   *          int
   * @param index2
   *          int
   * @param s2
   *          String
   * @return String
   */
  public static String replace(String s1, int index1, int index2, String s2) {
    StringBuffer st = new StringBuffer(2000);
    try {
      if (index1 > 0)
        st.append(s1.substring(0, index1));
      st.append(s2);
      st.append(s1.substring(index2 + 1));
    } catch (Exception e) {
      System.err.println("ERRORE:" + e.getMessage());
    }
    return st.toString();
  }

  /**
   * Nella stringa s1 rimpiazza la stringa s2 con la string s3 e ritorna la
   * stringa nuova. NB: rimpiazza tutte le occorrenze
   * 
   * @param s1
   *          String stringa su cui si effettua la sostituzione
   * @param s2
   *          String stringa cercata
   * @param s3
   *          String stringa sostituita al posto di s2
   * @return String stringa s1 modificata con la sostituzione di tutte le
   *         occorrenze di s2 con s3
   */
  public static String replace(String s1, String s2, String s3) {
    StringBuffer st = new StringBuffer(2000);
    int i = s1.indexOf(s2);
    int k = 0;
    while (i >= 0) {
      st.append(s1.substring(k, i));
      k = i + s2.length();
      st.append(s3);
      i = s1.indexOf(s2, i + 1);
    }
    st.append(s1.substring(k));
    return st.toString();
  }

  /**
   * Restituisce una string che rappresenta una data rovesciata aaaa-mm-dd come
   * voluto da JDBC
   * 
   * @param data
   *          String
   * @return String
   */
  public static String getDateFormatted(String dt) {
    char d = '/';
    String anno = "", mese = "", giorno = "";
    String data = dt;

    if (data.indexOf("-") > 0)
      d = '-';
    if (data.indexOf(d) <= 0)
      d = ' ';
    @SuppressWarnings("unused")
    int l = data.trim().length();
    giorno = data.substring(0, 2);
    if (giorno.charAt(1) == d || Integer.parseInt(giorno) > 31) {
      giorno = data.substring(0, 1);
      data = data.substring(1);
    } else {
      data = data.substring(2);
    }
    if (data.charAt(0) == '/' || data.charAt(0) == '-')
      data = data.substring(1);
    mese = data.substring(0, 2);
    if (mese.charAt(1) == d || Integer.parseInt(mese) > 12) {
      mese = data.substring(0, 1);
      data = data.substring(1);
    } else {
      data = data.substring(2);
    }
    if (data.charAt(0) == '/' || data.charAt(0) == '-')
      data = data.substring(1);
    anno = data.trim();
    if (anno.length() == 2) {
      if (Integer.parseInt(anno) >= 50)
        anno = "19" + anno;
      else
        anno = "20" + anno;
    } else if (anno.length() == 3) {
      if (Integer.parseInt(anno) >= 900)
        anno = "1" + anno;
      else
        anno = "2" + anno;
    }
    return anno + "-" + mese + "-" + giorno;
  }

  /**
   * Da una data rovesciata restituisce una data dd-mm-yyyy
   * 
   * @param data
   *          String
   * @return String
   */
  public static String getDateItaly(String data) {
    String anno = "", mese = "", giorno = "";
    giorno = data.substring(8, 10);
    mese = data.substring(5, 7);
    anno = data.substring(0, 4);
    return giorno + "/" + mese + "/" + anno;
  }

  /**
   * Controlla se il carattere passato è di punteggiatura, cioè uno fra:
   * <ul>
   * <li><tt>.</tt> (punto)</li>
   * <li><tt>,</tt> (virgola)</li>
   * <li><tt>:</tt> (due punti)</li>
   * <li><tt>;</tt> (punto e virgola)</li>
   * <li><tt>!</tt> (punto esclamativo)</li>
   * <li><tt>?</tt> (punto interrogativo)</li>
   * </ul>
   * 
   * @param c
   *          carattere da controllare
   * @return true sse c è un carattere di punteggiatura
   */
  public static boolean isPunctuation(char c) {
    if (c == '.' || c == ',' || c == ':' || c == ';' || c == '!' || c == '?')
      return true;
    return false;
  }

  /**
   * Controlla che tutti i caratteri della stringa passata siano della categoria
   * richiesta.
   * 
   * @param s
   *          stringa da controllare
   * @param charType
   *          maschera di bit con le categorie di carattere richieste. I
   *          possibili valori sono:
   *          <ul>
   *          <li><tt>CHAR_LETTERS</tt>: lettere</li>
   *          <li><tt>CHAR_DIGITS</tt>: cifre</li>
   *          <li><tt>CHAR_SPACE</tt>: caratteri di spaziatura (spazio, newline,
   *          formfeed, tab,...)</li>
   *          <li><tt>CHAR_PUNCT</tt>: caratteri di punteggiatura (vedi
   *          {@link #isPunctuation(char)})</li>
   *          </ul>
   * @return true sse s è composto solo da caratteri delle categorie richieste.
   *         Se la stringa è null o vuota, torna sempre false.
   */
  public static boolean isOnlyCharType(String s, int charType) {
    if (s == null || s.length() == 0)
      return false;

    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      boolean bOKChar = false;
      if ( (charType & CHAR_LETTERS) > 0) {
        if (Character.isLetter(chars[i]))
          bOKChar = true;
      }
      if ( (charType & CHAR_DIGITS) > 0) {
        if (Character.isDigit(chars[i]))
          bOKChar = true;
      }
      if ( (charType & CHAR_SPACE) > 0) {
        if (Character.isWhitespace(chars[i]))
          bOKChar = true;
      }
      if ( (charType & CHAR_PUNCT) > 0) {
        if (isPunctuation(chars[i]))
          bOKChar = true;
      }
      if ( !bOKChar)
        return false;
    }
    return true;
  }

  /**
   * Generatore di stringhe casuale. E' un wrapper di
   * {@link #getRandomString(int, int, Random)} a cui si rimanda per la
   * documentazione. Usa un random generator standard interno.
   * 
   * @param len
   *          int qta caratteri della stringa; se <=0, torna la stringa vuota
   * @param includeChars
   *          int maschera di bit per identificare le categorie di caratteri
   *          ammessi.
   */
  public static String getRandomString(int len, int includeChars) {
    return getRandomString(len, includeChars, m_rndGen);
  }

  /**
   * Ritorna una stringa di caratteri casuali con la lunghezza specificata
   * 
   * @param len
   *          int qta caratteri della stringa; se <=0, torna la stringa vuota
   * @param includeChars
   *          int maschera di bit per identificare le categorie di caratteri
   *          ammessi. I possibili valori sono:
   *          <ul>
   *          <li><tt>CHAR_LETTERS</tt>: lettere</li>
   *          <li><tt>CHAR_DIGITS</tt>: cifre</li>
   *          <li><tt>CHAR_SPACE</tt>: caratteri di spaziatura (spazio, newline,
   *          formfeed, tab,...)</li>
   *          <li><tt>CHAR_PUNCT</tt>: caratteri di punteggiatura (vedi
   *          {@link #isPunctuation(char)})</li>
   *          </ul>
   * @return String
   */
  public static String getRandomString(int len, int includeChars, Random randomGenerator) {
    Random rnd = randomGenerator != null ? randomGenerator : m_rndGen;
    int n;
    char c;
    if (len <= 0)
      return "";
    if ( ( (includeChars & CHAR_DIGITS) == 0) && ( (includeChars & CHAR_LETTERS) == 0) && ( (includeChars & CHAR_PUNCT) == 0)
        && ( (includeChars & CHAR_SPACE) == 0))
      return "";
    StringBuffer out = new StringBuffer(len);
    while (out.length() < len) {
      n = rnd.nextInt(94);
      if (n == 0)
        continue;
      c = (char) (31 + n);
      if (Character.isDigit(c) && ( (includeChars & CHAR_DIGITS) == CHAR_DIGITS))
        out.append(c);
      else if (Character.isLetter(c) && ( (includeChars & CHAR_LETTERS) == CHAR_LETTERS))
        out.append(c);
      else if (c == ' ' && ( (includeChars & CHAR_SPACE) == CHAR_SPACE))
        out.append(c);
      else if ( !Character.isLetter(c) && !Character.isDigit(c) && c != ' ' && ( (includeChars & CHAR_PUNCT) == CHAR_PUNCT))
        out.append(c);
    }
    return out.toString();
  }

  /**
   * Data una stringa la restituisce col primo carattere di ogni parola in
   * maiuscolo comodo per nomi, indirizzi, ecc...
   * 
   * @param str
   *          String
   * @return String
   */
  public static String primoMaiu(String str) {
    String strtot = "";
    String str2;
    String str1;
    String str3;
    String newString = str.toLowerCase();
    StringTokenizer st = new StringTokenizer(newString, " ", false);
    while (st.hasMoreTokens()) {
      str3 = st.nextToken();
      str1 = str3.substring(0, 1);
      str1 = str1.toUpperCase();
      str2 = str1 + str3.substring(1, str3.length()) + " ";
      strtot = strtot + str2;
    }
    return strtot;
  }

  /**
   * RTRIM di stringa (non esiste in java).
   * 
   * @param s
   *          String Stringa da rtrimmare
   * @return String stringa rtrimmata
   */
  public static final String rTrim(String s) {
    String szTmp = '.' + s;
    szTmp = szTmp.trim();
    return szTmp.substring(1);
  }

  /**
   * Ritorna una stringa formata da <tt>occ</tt> occorrenze del carattere
   * <tt>c</tt>.
   * <p>
   * 
   * @param c
   *          carattere da replicare
   * @param occ
   *          quantità occorrenze
   * @return ritorna una stringa vuota se <tt>occ</tt> è minore o uguale a zero,
   *         altrimenti una stringa composta da <tt>occ</tt> caratteri
   *         <tt>c</tt>.
   */
  public static String getChars(char c, int occ) {
    StringBuffer sOut = new StringBuffer(occ);
    for (int i = 0; i < occ; i++) {
      sOut.append(c);
    }
    return sOut.toString();
  }

  /**
   * Ritorna la quantità di occorrenze del carattere <em>c</em> nella stringa
   * <em>s</em>.
   * 
   * @param s
   *          stringa in cui cercare il carattere
   * @param c
   *          carattere da conteggiare
   * @return quantità di occorrenze di <b>c</b> in <b>s</b>; se <b>s</b> è null,
   *         torna 0 senza emettere exception.
   */
  public static int getCharCount(String s, char c) {
    if (s == null || s.length() == 0) {
      return 0;
    }
    int occ = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) {
        occ++;
      }
    }
    return occ;
  }

  /**
   * Converte una stringa in intero e ne ritorna il valore di default se stringa
   * non convertibile. <br>
   * In nessun caso genera exception, anche se <code>value</code> è null.
   * 
   * @param value
   *          stringa da convertire
   * @param defaultValue
   *          valore di default da restituire in caso di errori di conversione
   * 
   * @return un intero convertito dalla stringa
   */
  public static int getIntFromString(String value, int defaultValue) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  /**
   * Interpreta una stringa come booleano: riconosce 'true' e 'false', inoltre
   * tratta i numeri diversi da zero come true e '0' come false. <br>
   * In nessun caso genera exception, anche se <code>value</code> è null.
   * 
   * @param value
   *          valore da interpretare
   * @param defValue
   *          valore di default
   * 
   * @return il booleano convertito dalla stringa oppure <code>defValue</code>
   *         se impossibile convertirla
   */
  public static boolean getBoolFromString(String value, boolean defValue) {
    try {
      if (value.trim().equalsIgnoreCase("true")) {
        return true;
      } else if (value.trim().equalsIgnoreCase("false")) {
        return false;
      }

      int b = getIntFromString(value, Integer.MIN_VALUE);
      if (b != Integer.MIN_VALUE) {
        return b > 0;
      }
      return defValue;
    } catch (Exception ex) {
      return defValue;
    }
  }

  /**
   * Riduce la stringa passata alla lunghezza richiesta, troncandola se troppo
   * lunga o aggiungendo ' ' in coda se troppo corta
   * 
   * @param s
   *          String
   * @param nMaxLen
   *          int
   * @return String
   */
  public static String padOrCut(String s, int nMaxLen) {
    if (null == s)
      return getChars(' ', nMaxLen);
    String szTmp = s;
    if (szTmp.length() > nMaxLen) {
      szTmp.substring(0, nMaxLen);
    } else {
      szTmp += getChars(' ', nMaxLen - szTmp.length());
    }
    return szTmp;
  }

  /**
   * Restituisce la versione hex della stringa passata
   * 
   * @param szIn
   *          String
   * @return String
   */
  public static String toHex(String szIn) {
    String szTmp = "";

    for (int i = 0; i < szIn.length(); ++i) {
      char c = szIn.charAt(i);

      byte bHi = (byte) ( (c & 0xf0) / 16);
      byte bLo = (byte) (c & 0x0f);

      szTmp += nibbleToHex(bHi);
      szTmp += nibbleToHex(bLo);
      szTmp += ' ';
    }
    return szTmp.trim();
  }

  public static String toHex(byte c) {
    String szTmp = "";
    byte bHi = (byte) ( (c & 0xf0) / 16);
    byte bLo = (byte) (c & 0x0f);

    szTmp += nibbleToHex(bHi);
    szTmp += nibbleToHex(bLo);
    return szTmp;
  }

  public static String nibbleToHex(byte b) {
    String szTmp = "";
    if (b > 9) {
      szTmp = "" + (char) ('A' + b - 0x0A);
    } else {
      szTmp = "" + b;
    }
    return szTmp;
  }

  /**
   * Ritorna la stringa passata con il primo carattere maiuscolo. Attenzione il
   * resto in <b>minuscolo</b> <br>
   * Ad esempio: <tt>
   *    capitalize("ciao mOnDo")
   * </tt> <br>
   * ritorna <tt>"Ciao mondo"</tt>
   * 
   * @param p_szName
   *          stringa da capitalizzare
   * @return stringa capitalizzata
   */
  public static String capitalize(String p_szName) {
    String sz = p_szName.substring(0, 1).toUpperCase() + p_szName.substring(1).toLowerCase();
    return sz;
  }

  /**
   * Capitalizza tutte le parole di una frase. Capitalizzare significa rendere
   * il primo carattere maiuscolo e tutti gli altri minuscoli. <br/>
   * Ad esempio la frase <tt>"  QUESTA frase è da CAPitaliZZare   "</tt> <br/>
   * viene trasformata in <tt>"  Questa Frase È Da Capitalizzare   "</tt>
   * 
   * @param s
   *          stringa da capitalizzare
   * @return stringa con tutte le parole componenti capitalizzate
   */
  public static String capitalizeWords(String s) {
    if (s == null || s.length() == 0)
      return s;

    char[] chars = s.toCharArray();
    boolean inWord = false;
    //int wordStart = 0;
    StringBuffer sbOut = new StringBuffer();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if ( !Character.isWhitespace(c)) {
        if ( !inWord) {
          inWord = true;
          c = Character.toUpperCase(c);
        } else {
          c = Character.toLowerCase(c);
        }
      } else {
        inWord = false;
      }
      sbOut.append(c);
    }
    return sbOut.toString();
  }

  /**
   * Capitalizza una stringa, cioè rende il primo carattere maiuscolo e tutti
   * gli altri minuscoli. <br>
   * NOTA BENE: assume che la stringa sia una parola intera, se si vuogliono
   * capitalizzare tutte le parole di una frase usare
   * {@link #capitalizeWords(String)}. <br/>
   * Ad esempio:
   * <table border="1">
   * <tr>
   * <th>stringa</th>
   * <th>capitalizeWord(s)</th>
   * <th>capitalizeWords(s)</th>
   * <th>capitalize(s)</th>
   * </tr>
   * <tr>
   * <td><tt>abc</tt></td>
   * <td><tt>Abc</tt></td>
   * <td><tt>Abc</tt></td>
   * <td><tt>Abc</tt></td>
   * </tr>
   * <tr>
   * <td><tt>abc DEF</tt></td>
   * <td><tt>Abc def</tt></td>
   * <td><tt>Abc Def</tt></td>
   * <td><tt>Abc DEF</tt></td>
   * </tr>
   * </table>
   * NB: a differenza di {@link #capitalize(String)},
   * {@link #capitalizeWord(String)} rende minuscoli i caratteri dopo il primo
   * 
   * @param s
   *          stringa da capitalizzare
   * @return stringa capitalizzata
   */
  public static String capitalizeWord(String s) {
    if (s == null || s.length() == 0)
      return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

  /**
   * Effettua il parsing di una stringaspezzandola in token ogni qualvolta
   * incontra il delimitatore passato
   * 
   * @param toParse
   *          Stringa da parsare
   * @param delim
   *          Delimitatore con cui dividere la stringa in token
   * 
   * @return Una lista di token
   */
  public static List<String> tokenize(String toParse, String delim) {
    ArrayList<String> elementi = new ArrayList<String>();
    if (toParse == null) {
      return null;
    }
    StringTokenizer tokenizer = new StringTokenizer(toParse, delim);

    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      elementi.add(token.trim());
    }

    return elementi;
  }

  /**
   * Allunga la stringa fornita <code>sz</code> con spazi a
   * <strong>destra</strong> fino a raggiungere la dimensione <code>len</code> <br>
   * Se la stringa è più lunga di len, viene tornata così com'è. <br>
   * Esempio: <code>Text.padRight("abcd", 7)</code> ritorna la stringa
   * <code>"abcd&nbsp;&nbsp;&nbsp;"</code>
   * 
   * @param sz
   *          la stringa da allungare
   * @param len
   *          la lunghezza da raggiungere
   * @return stringa con spazi a destra fino a len caratteri
   */
  public static String padRight(String sz, int len) {
    return padRight(sz, len, ' ');
  }
  
  /**
   * Allunga la stringa fornita <code>sz</code> con il carattere <i>c</i> a
   * <strong>destra</strong> fino a raggiungere la dimensione <code>len</code>.<br>
   * Se la stringa è più lunga di len, viene tornata così com'è. <br>
   * Esempio: <code>Text.padRight("abcd", 7, '0')</code> ritorna la stringa
   * <code>"abcd000"</code>
   * 
   * @param sz
   *          la stringa da allungare
   * @param len
   *          la lunghezza da raggiungere
   * @param c
   *          carattere con cui riempire a destra
   * @return stringa con <i>c</i> a destra fino a len caratteri
   */
  public static String padRight(String sz, int len, char c) {
    int qtaCharDaMettere = len - sz.length();
    if (qtaCharDaMettere <= 0)
      return sz;
    return sz + getChars(c, qtaCharDaMettere);
  }

  /**
   * Allunga la stringa fornita <code>sz</code> con spazi a
   * <strong>sinistra</strong> fino a raggiungere la dimensione <code>len</code>
   * . <br>
   * Se la stringa è più lunga di len, viene tornata così com'è. <br>
   * Esempio: <code>Text.padLeft("abcd", 7)</code> ritorna la stringa
   * <code>"&nbsp;&nbsp;&nbsp;abcd"</code>
   * 
   * @param sz
   *          la stringa da allungare
   * @param len
   *          la lunghezza da raggiungere * @param c carattere con cui riempire
   *          a destra
   * @return stringa con spazi a sinistra fino a len caratteri
   */
  public static String padLeft(String sz, int len) {
    return padLeft(sz, len, ' ');
  }

  /**
   * Allunga la stringa fornita <code>sz</code> con il carattere <code>c</code>
   * a <strong>sinistra</strong> fino a raggiungere la dimensione
   * <code>len</code>. <br>
   * Se la stringa è più lunga di len, viene tornata così com'è. <br>
   * Esempio: <code>Text.padLeft("abcd", 7,'0')</code> ritorna la stringa
   * <code>"000abcd"</code>
   * 
   * @param sz
   *          la stringa da allungare
   * @param len
   *          la lunghezza da raggiungere
   * @param c
   *          carattere con cui riempire a sinistra
   * @return stringa con <i>c</i> a sinistra fino a len caratteri
   */
  public static String padLeft(String sz, int len, char c) {
    int qtaCharDaMettere = len - sz.length();
    if (qtaCharDaMettere <= 0)
      return sz;
    return getChars(c, qtaCharDaMettere) + sz;
  }

  /**
   * Converte in maiuscolo una stringa eccetto le lettere accentate. Esempio:
   * 
   * <pre>
   *   Text.upperCaseAscii("aàb") --> "AàB"
   * </pre>
   * 
   * @param sz
   *          stringa da convertire
   * @return stringa convertita
   */
  public static String upperCaseAscii(String sz) {
    StringBuilder ls = new StringBuilder();
    for (int i = 0; i < sz.length(); i++) {
      char c = sz.charAt(i);
      if (Character.isLowerCase(c)) {
        int ccode = c;
        if (ccode < 224 || ccode > 255)
          ls.append(String.valueOf(c).toUpperCase());
        else
          ls.append(c); // carattere accentato minuscolo: lo lascio così
      } else
        ls.append(c);
    }
    return ls.toString();
  }

  /**
   * Restituisce un elemento XML col nome e valore passati. <br/>
   * In pratica restituisce la stringa seguente:
   * 
   * <pre>
   *    &lt;<i>nome</i>&gt;&lt;![CDATA[<i>valore</i>]]&gt;&lt;/<i>nome</i>&gt;
   * 
   * @param nome nome elemento XML
   * @param valore valore elemento XML; se valore è null, uso defValue
   * @param defValue valore di da usare se valore è null; se anche questo è null il metodo emette stringa vuota
   * @return elemento XML col nome e valore passati
   */
  public static String getXMLElem(String nome, String valore, String defValue) {
    if (valore == null)
      valore = defValue;
    if (valore == null)
      return "";
    String valsStripped = trimLeft(valore);
    if (valsStripped.startsWith("<"))
      return "<" + nome + ">" + valore + "</" + nome + ">";
    else
      return "<" + nome + "><![CDATA[" + valore + "]]></" + nome + ">";
  }

  /**
   * Leva eventuale sequenza iniziale di caratteri di spaziatura dalla stringa
   * passata e la ritorna senza tale sequenza iniziale. <br/>
   * Vedi anche {@link #trimRight(String)}.
   * 
   * Per carattere di spaziatura si intende qualsiasi carattere per cui
   * {@link Character#isWhitespace(char)} ritorna true. Anche il tab e il
   * newline sono considerati caratteri di spaziatura.
   * 
   * @param str
   *          stringa da analizzare
   * @return stringa senza sequenza iniziale di spaziatura
   */
  public static String trimLeft(String str) {
    if (str == null)
      return null;
    int iStart = 0;
    for (char c : str.toCharArray()) {
      if ( !Character.isWhitespace(c))
        break;
      iStart++;
    }
    return str.substring(iStart);
  }

  /**
   * Leva eventuale sequenza finale di caratteri di spaziatura dalla stringa
   * passata e la ritorna senza tale sequenza iniziale. Per caratteri di
   * spaziatura si intendono anche tabulazioni, line feed, etc. Per maggiori
   * dettagli vedi {@link #isOnlySpace(String)}. <br/>
   * Vedi anche {@link #trimLeft(String)}. <br/>
   * <b>NOTA</b>: esiste anche il metodo {@link #rTrim(String)} che fa la stessa
   * cosa; da test effettuati è però più lento di oltre il doppio.
   * 
   * @param str
   *          stringa da analizzare
   * @return stringa senza sequenza finale di spaziatura
   */
  public static String trimRight(String str) {
    if (str == null)
      return null;
    char[] chars = str.toCharArray();
    //cerco il primo char non spazio all'indietro
    int i = 0;
    for (i = chars.length - 1; i >= 0; i--) {
      if ( !Character.isWhitespace(chars[i]))
        break;
    }
    if (i < 0)
      return "";
    else
      return str.substring(0, i + 1);
  }

  /**
   * Ritorna true se la stringa passata è composta solo da caratteri spaziatori,
   * quali spazio, tabulazione, line feed etc. L'elenco preciso dei caratteri
   * spaziatori è il seguente:
   * <ul>
   * <li>It is a Unicode space character (<code>SPACE_SEPARATOR</code>,
   * <code>LINE_SEPARATOR</code>, or <code>PARAGRAPH_SEPARATOR</code>) but is
   * not also a non-breaking space (<code>'&#92;u00A0'</code>,
   * <code>'&#92;u2007'</code>, <code>'&#92;u202F'</code>).
   * <li>It is <code>'&#92;u0009'</code>, HORIZONTAL TABULATION.
   * <li>It is <code>'&#92;u000A'</code>, LINE FEED.
   * <li>It is <code>'&#92;u000B'</code>, VERTICAL TABULATION.
   * <li>It is <code>'&#92;u000C'</code>, FORM FEED.
   * <li>It is <code>'&#92;u000D'</code>, CARRIAGE RETURN.
   * <li>It is <code>'&#92;u001C'</code>, FILE SEPARATOR.
   * <li>It is <code>'&#92;u001D'</code>, GROUP SEPARATOR.
   * <li>It is <code>'&#92;u001E'</code>, RECORD SEPARATOR.
   * <li>It is <code>'&#92;u001F'</code>, UNIT SEPARATOR.
   * </ul>
   * 
   * @param s
   *          stringa da controllare
   * @return true se <var>s</var> è composto solo da caratteri di spaziatura
   * @see #isOnlySpace(char[])
   */
  public static boolean isOnlySpace(String s) {
    return isOnlySpace(s.toCharArray());
  }

  /**
   * Ritorna true se l'array di caratteri passato è composto solo da caratteri
   * spaziatori. <br>
   * Per maggiori dettagli vedi {@link #isOnlySpace(String)}.
   * 
   * @param chars
   *          array di caratteri da controllare
   * @return true se <var>chars</var> è composto solo da caratteri di spaziatura
   */
  public static boolean isOnlySpace(char[] chars) {
    final int ZERO_WIDTH_NOBREAK_SPACE = 0xFEFF;
    int i = 0;
    for (i = 0; i < chars.length; i++) {
      if ( !Character.isWhitespace(chars[i]) && chars[i]!=ZERO_WIDTH_NOBREAK_SPACE)
        return false;
    }
    return true;
  }

  /**
   * Restituisce un elemento XML col nome e valore passati. <br>
   * Wrapper di <code>getXMLElem(nome, valore, "")</code>.
   * 
   * @param nome
   *          nome elemento XML
   * @param valore
   *          valore elemento XML; se valore è null, uso ""
   * @return elemento XML col nome e valore passati
   */
  public static String getXMLElem(String nome, String valore) {
    return getXMLElem(nome, valore, "");
  }

  /**
   * Restituisce un elemento XML col nome e valore passati. In base al tipo di
   * classe del valore, chiama il metodo specifico opportuno.
   * 
   * @param nome
   *          nome elemento XML
   * @param valore
   *          valore elemento XML; se valore è null, emetto stringa vuota
   * @return elemento XML col nome e valore passati
   */
  public static String getXMLElem(String nome, Object valore) {
    if (valore == null)
      return getXMLElem(nome, "");
    if (valore instanceof Number)
      return getXMLElem(nome, (Number) valore);
    else
      return getXMLElem(nome, valore.toString());
  }

  /**
   * Restituisce un elemento XML col nome e valore passati. <br/>
   * In pratica restituisce la stringa seguente:
   * 
   * <pre>
   *    &lt;<i>nome</i>&gt;<i>valore</i>&lt;/<i>nome</i>&gt;
   * 
   * @param nome nome elemento XML
   * @param valore valore elemento XML; se valore è null, emetto il valore zero
   * @return elemento XML col nome e valore passati
   */
  public static String getXMLElem(String nome, Number valore) {
    if (valore == null)
      valore = 0;
    return "<" + nome + ">" + valore + "</" + nome + ">";
  }

  /**
   * @deprecated vedi {@link #isValueEq(String, Object, boolean)}
   */
  @Deprecated
  public static boolean isValue(String s, String s2, boolean ignoreCase) {
    boolean bEquals = false;
    if (s != null && s2 != null) {
      bEquals = ignoreCase ? s.equalsIgnoreCase(s2) : s.equals(s2);
    } else if (s == null && s2 == null) {
      bEquals = true;
    }
    return bEquals;
  }

  /**
   * Confronta la stringa del primo parametro con l'oggetto passato come secondo
   * parametro e torna true se sono uguali. 
   * <br>IMPORTANTE: supporta anche le stringhe null. 
   * 
   * <pre style="margin:0;">
   *   Text.isValue("abc", "ABC", true) --> true
   *   Text.isValue("abc", "ABC", false) --> false
   *   Text.isValue("abc", null, true) --> false
   *   Text.isValue(null, "", true) --> false
   * </pre>
   * 
   * @param s
   *          prima stringa del confronto
   * @param o
   *          oggetto con cui confrontare 's': viene fatta la toString di 'o'
   * @param ignoreCase
   *          true se il confronto deve ignorare maiuscole/minuscole
   * @return true sse le due stringhe sono uguali; due oggetti <tt>null</tt>
   *         sono considerati uguali.
   */
  public static boolean isValueEq(String s, Object o, boolean ignoreCase) {
    boolean bEquals = false;
    if (s != null && o != null) {
      bEquals = ignoreCase ? s.equalsIgnoreCase(o.toString()) : s.equals(o.toString());
    } else if (s == null && o == null) {
      bEquals = true;
    }
    return bEquals;
  }

  /**
   * Wrapper di {@link #isValueEq(String, Object, boolean)}
   * 
   * @param s
   *          prima stringa del confronto
   * @param o
   *          oggetto con cui confrontare 's': viene fatta la toString di 'o'
   * @return true sse le due stringhe sono uguali in modo <b>case sensitive</b>; due oggetti <tt>null</tt>
   *         sono considerati uguali.
   */
  public static boolean isValueEq(String s, Object o) {
    return isValueEq(s, o, false);
  }
  
  /**
   * @deprecated vedi {@link #isValueEq(String, Object)}
   */
  @Deprecated
  public static boolean isValue(String s, String s2) {
    return isValue(s, s2, false);
  }

  /**
   * Testa se una stringa è composta da almeno n caratteri
   * 
   * @param s
   *          stringa da testare
   * @param n
   *          qta caratteri minimi che deve avere; gli spazi sono significativi
   * @return <tt>true</tt> se la stringa passata ha almeno n caratteri; se
   *         <i>s</i> è <tt>null</tt> ritorna <tt>false</tt>
   * @see #isValue(String, int, boolean)
   * @see #isValue(String)
   */
  public static boolean isValue(String s, int n) {
    return isValue(s, n, true);
  }

  /**
   * Testa se una stringa è composta da almeno 1 carattere diverso da spazio.
   * 
   * @param s
   *          stringa da testare
   * @return <tt>true</tt> se la stringa passata ha almeno 1 carattere non
   *         spazio; se <i>s</i> è <tt>null</tt> ritorna <tt>false</tt>
   * @see #isValue(String, int, boolean)
   * @see #isValue(String, int)
   */
  public static boolean isValue(String s) {
    return isValue(s, 1, false);
  }

  /**
   * Testa se una stringa è composta da almeno n caratteri
   * 
   * @param s
   *          stringa da testare
   * @param n
   *          qta caratteri minimi che deve avere
   * @param useSpace
   *          se <tt>false</tt>, non considera gli spazi iniziali e finali come
   *          caratteri significativi (la stringa viene trimmata)
   * 
   * @return <tt>true</tt> se la stringa passata ha almeno n caratteri; se
   *         <i>s</i> è <tt>null</tt> ritorna <tt>false</tt>
   * @see #isValue(String, int)
   * @see #isValue(String)
   */
  public static boolean isValue(String s, int n, boolean useSpace) {
    if ( !useSpace && s != null)
      s = s.trim();
    return s != null && s.length() >= n;
  }

  /**
   * Testa se un oggetto Integer è un intero diverso da zero.
   * 
   * @param i
   *          Integer da testare
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore diverso da 0
   * @see #isValue(Integer, int)
   */
  public static boolean isValue(Integer i) {
    return i != null && i.intValue() != 0;
  }

  /**
   * Testa se un oggetto Integer è un intero uguale al valore passato.
   * 
   * @param i
   *          Integer da testare
   * @param value
   *          intero con cui confrontare il valore di i
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore uguale a <em>value</em>.
   * @see #isValue(Integer)
   */
  public static boolean isValueEq(Integer i, int value) {
    return i != null && i.intValue() == value;
  }

  /**
   * Testa se un oggetto Short è un intero uguale al valore passato.
   * 
   * @param i
   *          Integer da testare
   * @param value
   *          intero con cui confrontare il valore di i
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore uguale a <em>value</em>.
   * @see #isValue(Integer)
   */
  public static boolean isValueEq(Short i, int value) {
    return i != null && i.intValue() == value;
  }

  /**
   * @deprecated nome ambiguo, usare {@link #isValueEq(Integer, int)}
   */
  @Deprecated
  public static boolean isValue(Integer i, int value) {
    return i != null && i.intValue() == value;
  }

  /**
   * Testa se un oggetto Integer ha un valore uguale ad un altro Integer
   * passato. Se i due Integer sono ambedue <tt>null</tt>, sono considerati
   * uguali.
   * 
   * @param i
   *          Integer da testare
   * @param value
   *          Integer con cui confrontare il valore di i
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore uguale a quello di <em>value</em>, oppure se ambedue gli
   *         oggetti sono null.
   * @see #isValue(Integer)
   * @see #isValue(Integer, int)
   */
  public static boolean isValueEq(Integer i, Integer value) {
    //    boolean bRet = i==null ^ value==null;
    //    if ( bRet )
    //      return !bRet;
    //    if ( i==null)
    //      return bRet;
    //    return i.equals(value);
    return (i != null && value != null && i.intValue() == value.intValue()) // 
        || (i == null && value == null);
  }

  /**
   * 
   * @deprecated nome ambiguo, usare {@link #isValueEq(Integer, Integer)}
   */
  @Deprecated
  public static boolean isValue(Integer i, Integer value) {
    return (i != null && value != null && i.intValue() == value.intValue()) || (i == null && value == null);
  }

  public static boolean isValue(Date dt) {
    return dt != null;
  }

  /**
   * Testa se un oggetto Integer è un intero positivo.
   * 
   * @param i
   *          Integer da testare
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore maggiore di 0
   * @see #isValue(Integer, int)
   * @see #isValue(Integer)
   */
  public static boolean isValuePos(Integer i) {
    return i != null && i.intValue() > 0;
  }

  /**
   * Testa se un oggetto Integer è un intero negativo.
   * 
   * @param i
   *          Integer da testare
   * @return <tt>true</tt> se l'Integer passato è diverso da null ed ha un
   *         valore minore di 0
   * @see #isValue(Integer, int)
   * @see #isValue(Integer)
   */
  public static boolean isValueNeg(Integer i) {
    return i != null && i.intValue() < 0;
  }

  /**
   * Testa se un oggetto BigDecimal è un intero diverso da zero considerato il
   * proprio scale (con scale=2 isValue(0.001)=false, con scale=3
   * isValue(0.001)=true).
   * 
   * @param i
   *          BigDecimal da testare
   * @return <tt>true</tt> se il BigDecimal passato è diverso da null ed ha un
   *         valore diverso da 0
   * 
   */
  public static boolean isValue(BigDecimal i) {
    return i != null && i.signum() != 0;
  }

  /**
   * Testa se un oggetto BigDecimal è positivo considerato il proprio scale (con
   * scale=2 isValuePos(0.001)=false, con scale=3 isValuePos(0.001)=true).
   * 
   * @param i
   *          BigDecimal da testare
   * @return <tt>true</tt> se il BigDecimal passato è diverso da null ed ha un
   *         valore maggiore di 0
   */
  public static boolean isValuePos(BigDecimal i) {
    return i != null && i.signum() > 0;
  }

  /**
   * Testa se un oggetto BigDecimal è negativo considerato il proprio scale.
   * 
   * @param i
   *          BigDecimal da testare
   * @return <tt>true</tt> se il BigDecimal passato è diverso da null ed ha un
   *         valore minore di 0
   */
  public static boolean isValueNeg(BigDecimal i) {
    return i != null && i.signum() < 0;
  }

  /**
   * Verifica che la stringa passata passa il parsing della espressione regolare
   * {@link #s_emailPatrn}
   * 
   * @param sz
   * @return
   */
  public static final boolean isEmail(String sz) {
    boolean bRet = false;
    if (sz == null)
      return bRet;
    Matcher pt = s_emailPatrn.matcher(sz);
    bRet = pt.matches();
    return bRet;
  }

  /**
   * Confronta la stringa passata con i valori passati, determinando se è uguale
   * almeno ad una.
   * 
   * @param s
   *          stringa da confrontare; se null, ritorna false senza causare exceptions
   * @param values
   *          valori con cui confrontare s
   * @return true sse s è uguale almeno ad uno dei valori passati;nota bene: il
   *         confronto è <b>case insensitive</b>
   */
  public static boolean isOneOf(String s, String... values) {
    if (s == null || values == null) {
      return false;
    }
    for (String c : values) {
      if (s.equalsIgnoreCase(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Confronta un intero passato con i valori passati, determinando se è uguale
   * almeno ad uno.
   * 
   * @param i
   *          intero da confrontare, se null torna sempre false
   * @param values
   *          valori con cui confrontare i
   * @return true sse i è uguale almeno ad uno dei valori passati;nota bene: il
   *         confronto è <b>case insensitive</b>
   */
  public static boolean isOneOf(Integer i, Integer... values) {
    if (i == null || values == null) {
      return false;
    }
    for (Integer c : values) {
      if (Text.isValueEq(i, c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Confronta due BigDecimal e torna true se sono uguali. IMPORTANTE: supporta
   * anche null.
   * 
   * @param bd
   *          primo numero del confronto
   * @param bd2
   *          secondo numero del confronto
   * @return true sse i due BigDecimal sono uguali; due BigDecimal <tt>null</tt>
   *         sono considerati uguali.
   */
  public static boolean isValue(BigDecimal bd, BigDecimal bd2) {
    boolean bEquals = false;
    if (bd != null && bd2 != null) {
      bEquals = bd.compareTo(bd2) == 0;
    } else if (bd == null && bd2 == null) {
      bEquals = true;
    }
    return bEquals;
  }

  public static boolean isValue(Boolean p_bv) {
    boolean bRet = false;
    bRet = p_bv != null;
    return bRet;
  }

  /**
   * Accorcia una stringa (troncando alla fine o all'inizio) alla lunghezza
   * massima indicata, mettendo opzionalmente il simbolo di ellispis (cioe' i
   * tre puntini "..."). <br/>
   * Esempio:
   * 
   * <pre style="margin: 0;">
   *   shortenString("abcdef", 5, true, false)  --> ab...
   *   shortenString("abcdef", 5, false, false)  --> abcde
   *   shortenString("abcdef", 5, true, true)  --> ...ef
   *   shortenString("abcdef", 5, false, true)  --> bcdef
   *   shortenString("abcdef", 6, true, any)  --> abcdef
   * </pre>
   * 
   * @param s
   *          stringa da controllare
   * @param maxLen
   *          lunghezza max che deve avere la stringa di output, compresi i
   *          caratteri dell'ellipsis se previsto
   * @param showEllipsis
   *          true se si deve avere l'ellipsis al posto della parte troncata,
   *          nel caso venga effettivamente troncata
   * @param truncateStart
   *          true se si deve troncare la stringa all'inizio, false se alla fine
   * @return stringa troncata se più lunga di maxlen, altrimenti la stringa di
   *         input
   * @see #shortenString(String, int, boolean)
   */
  public static String shortenString(String s, int maxLen, boolean showEllipsis, boolean truncateStart) {
    if (s == null || s.length() == 0) {
      return s;
    }
    String out = "";
    if (s.length() > maxLen) {
      if (showEllipsis) {
        maxLen -= 3;
      }
      if (truncateStart) {
        if (showEllipsis) {
          out = "...";
        }
        out += s.substring(s.length() - maxLen, s.length());
      } else {
        out = s.substring(0, maxLen);
        if (showEllipsis) {
          out += "...";
        }
      }
    } else {
      out = s;
    }
    return out;
  }

  /**
   * Accorcia una stringa, troncando alla fine, alla lunghezza massima indicata,
   * mettendo opzionalmente il simbolo di ellispis (cioe' i tre puntini "...").
   * 
   * @param s
   *          stringa da controllare
   * @param maxLen
   *          lunghezza max che deve avere la stringa di output, compresi i
   *          caratteri dell'ellipsis se previsto
   * @param showEllipsis
   *          true se si deve avere l'ellipsis al posto della parte troncata,
   *          nel caso venga effettivamente troncata
   * @return stringa troncata se più lunga di maxlen, altrimenti la stringa di
   *         input
   * @see #shortenString(String, int, boolean, boolean)
   */
  public static String shortenString(String s, int maxLen, boolean showEllipsis) {
    return shortenString(s, maxLen, showEllipsis, false);
  }

}
