package org.xreports.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * <p>Title: Gestione date</p>
 * <p>Description: Metodi di utilitÃ  generale per la gestione delle date</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: CIS Coop</p>
 * @author $Author: gianni $
 * @version $Revision: 1.50 $ $Date: 2012/08/08 08:48:22 $
 */
public class DtUtil {
  /**
   * Costante per la funzione isHoliday: indica di calcolare le festività sammarinesi.
   */
  public static final int HOL_RSM = 0;
  /**
   * Costante per la funzione isHoliday: indica di calcolare le festività italiane.
   */
  public static final int HOL_ITA = 1;
  /**
   * Costante per la funzione isHoliday: indica di calcolare le festività sammarinesi
   * compresi i giorni di chiusura degli uffici pubblici.
   * Questo comprende sempre i giorni 24 e 31 dicembre oltre ai giorni calcolati con
   * HOL_RSM.
   * <br>Daniele 28/11/2008: vengono considerati anche eventuali altri giorni festivi per 
   * <br>       recupero festività, ovvero se il 24 Dicembre (e quindi anche il 31) 
   * <br>       vengono presi di Sabato o Domenica allora sono festivi il 14 e il 16 Agosto
  */
  public static final int HOL_RSM_STATO = 2;

  /** Secondi in un giorno */
  public static final int SEC_IN_UN_GIORNO = 86400;
  /** MILLISecondi in un giorno */
  public static final int MILLISEC_IN_UN_GIORNO = SEC_IN_UN_GIORNO * 1000;

  /** Abilitazione controllo stretto sulle date.
   *  Il Calendar di java è abbastanza liberale nell'accettare valori 
   *  per giorno e mese (ex: valori < 0, valori > 12 per i mesi ...).
   *  Voglio un controllo + stretto ?
   *  + valori 1-12 per i mesi
   *  + valori 1-31 per i giorni */
  private static boolean mbStrict = true;
  
  private static ThreadLocal<SimpleDateFormat> m_sdf = new ThreadLocal<SimpleDateFormat>() {
    @Override
    public SimpleDateFormat initialValue() {
      return new SimpleDateFormat();
    }
  };

  public DtUtil() {
    //vuoto
  }

  public static void main(String[] args) {
    //vuoto
    Date dt = null;
    try {
      dt = createDate(2011,0,0);
      System.out.println(dt);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      dt = createDate(2011,2,29);
      System.out.println(dt);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      dt = createDate(2011,2,28);
      System.out.println(dt);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Fabbrica un'oggetto di tipo Date dato giorno, mese e anno.
   *
   * @param anno anno della data
   * @param mese mese della data (1-12)
   * @param giorno giorno della data (1-31)
   * @return Date
   */
  public static Date createDate(int anno, int mese, int giorno) {
    return createDate(anno, mese, giorno, 0, 0, 0);
  }

  /**
   * Fabbrica un'oggetto di tipo Date dato giorno, mese, anno, ore e minuti.
   *
   * <br>Giorno: 1-31
   * <br>Mese:   0-11
   *
   * @param anno anno della data
   * @param mese mese della data (1-12)
   * @param giorno giorno della data (1-31)
   * @param ora ora della data
   * @param minuti minuti della data
   * @return Date
   */
  public static Date createDate(int anno, int mese, int giorno, int ora,
      int minuti) {
    return createDate(anno, mese, giorno, ora, minuti, 0);
  }

  /**
   * Fabbrica un'oggetto di tipo Date dato giorno, mese, anno, ore, minuti e secondi.
   *
   * <br>Giorno: 1-31
   * <br>Mese:   1-12
   *
   * @param anno anno della data
   * @param mese mese della data (1-12)
   * @param giorno giorno della data (1-31)
   * @param ora ora della data
   * @param minuti minuti della data
   * @param secondi secondi della data
   * @return Date
   */
  public static Date createDate(int anno, int mese, int giorno, int ora,
      int minuti, int secondi) {
    // RaGi - 21 nov 2011
    // ma che roba é ?????
    //if (mese == 14)
    //  mese = 13;
    
    // Ragi - 21 nov 2011
    // test input
    testStrictCreateDate(anno,mese,giorno,ora,minuti,secondi);
    
    Calendar cl = Calendar.getInstance();
    cl.set(anno, mese - 1, giorno, ora, minuti, secondi);
    cl.set(Calendar.MILLISECOND, 0);
    return cl.getTime();
  }

  /**
   * Testa se gli input di createDate sono validi secondo le regole 'strict'
   * 
   * @param anno
   * @param mese
   * @param giorno
   * @param ora
   * @param minuti
   * @param secondi
   * @throws RuntimeException
   */
  public static void testStrictCreateDate(int anno, int mese, int giorno, int ora,
      int minuti, int secondi) throws RuntimeException {
    if( hasStrictControl() ) {
      // mese 1-12
      if( (mese < 1) || (mese > 12) ) {
        String s = "Valore non permesso per mese: " + mese + " (deve essere 1-12)";
        throw new RuntimeException(s);
      }
      // giorno 1-ultimo giorno del mese
      if( !giornoPermessoPerMese(anno,mese,giorno) ) {
        String s = "Valore " + giorno + " non permesso per giorno nel mese " + getNomeMese(mese);
        throw new RuntimeException(s);
      }
    }
  }

  /**
   * Testa se il giorno passato è valido per il mese passato
   * (tenendo conto degli anni bisestili) 
   * @param mese
   * @param giorno
   * @return
   */
  public static boolean giornoPermessoPerMese(int anno, int mese, int giorno) {
    boolean bRet = true;
    if( giorno < 1 )
      bRet = false;
    // ultimo ?
    if( bRet ) {
      switch (mese) {
        case 2:
          if( isAnnoBisestile(anno) )
            bRet = (giorno <= 29);
          else
            bRet = (giorno <= 28);
          break;
        case 4: case 6: case 9: case 11:
          bRet = (giorno <= 30);
          break;
        default:
          bRet = (giorno <= 31);
          break;
      }
    }
    return bRet;
  }
  /**
   * Formatta una data con SimpleDateFormat.
   *
   * @param d Date data da formattare
   * @param format String stringa di formato:<ul>
   *    <li>yy = anno con ultime 2 cifre; yyyy anno di 4 cifre
   *    <li>M = mese senza '0' davanti; MM = mese con '0' davanti; MMM = mese testuale abbreviato; MMMM = mese testuale intero;
   *    <li>D = giorno nell'anno (da 1 a 366);
   *    <li>d = giorno nel mese senza '0' davanti; dd = giorno nel mese con '0' davanti;
   *    <li>E/EE/EEE = giorno sett. testuale abbreviato; EEEE = giorno sett. testuale intero;
   *    <li>H = ora (0-23) senza '0' davanti; HH = ora (0-23) con '0' davanti;
   *    <li>m = minuti senza '0' davanti; mm = minuti con '0' davanti;
   *    <li>s = secondi senza '0' davanti; ss = secondi con '0' davanti;
   * </ul>
   *
   * @return String "" se d e' null, data formattata altrimenti
   */
  public static String format(Date d, String format) {
    if (d == null) {
      return "";
    }
    SimpleDateFormat sdf = m_sdf.get();
    sdf.applyPattern(format);
    return sdf.format(d);
  }

  /**
   * Formatta una data con SimpleDateFormat nel formato standard "dd/MM/yyyy".
   *
   * @param d Date data da formattare
   *
   * @return String "" se d e' null, data formattata altrimenti
   */
  public static String format(Date d) {
    return format(d, "dd/MM/yyyy");
  }

  /** Restituisce il numero del giorno della data passata
   * @param d data di cui prendere il giorno
   * @return giorno presente in <var>d</var>
   */
  public static int getDay(Date d) {
    Calendar c = calendarFromDate(d);
    return c.get(Calendar.DATE);
  }

  /**
   * Restituisce il numero di giorni che del mese della data passata (28,29,30 o 31)
   * @param d Date
   * @return int
   */
  public static int getGiorniDelMese(Date d) {
    Date d1 = getPrimoDelMese(d);
    Date d2 = getUltimoDelMese(d);
    return (int) dateDiff(d1, d2, Calendar.DATE) + 1;
  }

  /**
   * Restituisce una data col primo giorno del mese successivo alla data passata
   *
   * @param dt Date
   * @return Date
   */
  public static Date getPrimoDelMeseSuccessivo(Date dt) {
    Calendar c = Calendar.getInstance();
    // azzero tutto
    c.clear();
    c.setTime(dt);
    azzeraHHMMSS(c);
    int nMese = c.get(Calendar.MONTH);
    if (nMese == Calendar.DECEMBER) {
      c.set(Calendar.MONTH, Calendar.JANUARY);
      c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
    } else {
      c.set(Calendar.MONTH, nMese + 1);
    }
    c.set(Calendar.DATE, 1);
    return c.getTime();
  }

  /**
   * Restituisce una data con l'ultimo giorno del mese della data passata
   *
   * @param dt Date
   * @return Date
   */
  public static Date getUltimoDelMese(Date dt) {
    // primo giorno del mese successivo
    Date dtNextM = getPrimoDelMeseSuccessivo(dt);
    // Tolgo un giorno
    Calendar c = Calendar.getInstance();
    c.setTime(dtNextM);
    azzeraHHMMSS(c);
    c.add(Calendar.DATE, -1);
    return c.getTime();
  }

  /**
   * Restituisce una data con il primo giorno del mese della data passata
   *
   * @param dt Date
   * @return Date
   */
  public static Date getPrimoDelMese(Date dt) {
    Calendar c = Calendar.getInstance();
    c.setTime(dt);
    c.set(Calendar.DATE, 1);
    
    azzeraHHMMSS(c);
    return c.getTime();
  }

  /**
   * Controlla se la data passata è l'ultimo giorno del mese.
   *
   * <br>Non necessariamente è del mese CORRENTE !!!
   *
   * @param dt la data da controllare
   * @return true/false
   */
  public static boolean isUltimoDelMese(Date dt) {
    return isUltimoDelMese(dt, false);
  }

  /**
   * Controlla se la data passata è l'ultimo giorno del mese.
   *
   * @param dt la data da controllare
   * @param bQuestoMese se true deve essere di questo mese !!!
   * @return true/false
   */
  public static boolean isUltimoDelMese(Date dt, boolean bQuestoMese) {
    // agiungo un giorno e vedo se il mese cambia ...
    Calendar c1 = Calendar.getInstance();
    c1.setTime(dt);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(dt);
    c2.add(Calendar.DATE, 1);
    if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
      // non è l'ultimo di nessun mese ...
      return false;
    }
    if (bQuestoMese) {
      Calendar cNow = Calendar.getInstance();
      int nMonth = cNow.get(Calendar.MONTH);
      if (c1.get(Calendar.MONTH) != nMonth) {
        // E' l'ultimo giorno ma di un altro mese ...
        return false;
      }
    }
    return true;
  }

  /**
   * Controlla se la data passata è il primo giorno del mese.
   *
   * @param dt la data da controllare
   * @return true/false
   */
  public static boolean isPrimoDelMese(Date dt) {
    return isPrimoDelMese(dt, false);
  }

  /**
   * Controlla se la data passata è il primo giorno del mese.
   *
   * @param dt la data da controllare
   * @param bQuestoMese se true deve essere di questo mese !!!
   * @return true/false
   */
  public static boolean isPrimoDelMese(Date dt, boolean bQuestoMese) {
    // agiungo un giorno e vedo se il mese cambia ...
    Calendar c1 = Calendar.getInstance();
    c1.setTime(dt);
    if (1 != c1.get(Calendar.DATE)) {
      // non è il primo di nessun mese ...
      return false;
    }
    if (bQuestoMese) {
      Calendar cNow = Calendar.getInstance();
      int nMonth = cNow.get(Calendar.MONTH);
      if (c1.get(Calendar.MONTH) != nMonth) {
        // E' l'ultimo giorno ma di un altro mese ...
        return false;
      }
    }
    return true;
  }

  /**
   * Controlla se la data passata è il primo giorno dell'anno.
   *
   * @param dt la data da controllare
   * @return true/false
   */
  public static boolean isPrimoDellAnno(Date dt) {
    if (getDay(dt) == 1 && getMonth(dt) == 1) {
      return true;
    }
    return false;
  }

  /**
   * Controlla se la data passata è l'ultimo giorno dell'anno.
   *
   * @param dt la data da controllare
   * @return true/false
   */
  public static boolean isUltimoDellAnno(Date dt) {
    if (getDay(dt) == 31 && getMonth(dt) == 12) {
      return true;
    }
    return false;
  }

  /**
   * Restitusce una data uguale a quella passata meno 1 mese
   *
   * @param dt la data in input
   * @return la data trovata
   */
  public static Date dataMenoUnMese(Date dt) {
    return dataMenoMesi(dt, 1);
  }
  
  /**
   * Restitusce una data uguale a quella passata meno 'numeroMesi' mesi
   *
   * @param dt la data in input
   * @param numeroMesi il numero di mesi da sottrarre alla data
   * @return la data trovata
   */
  public static Date dataMenoMesi(Date dt, int numeroMesi) {
    int numMesi = Math.abs( numeroMesi );
    Calendar c = Calendar.getInstance();
    c.clear();
    c.setTime(dt);
    c.add(Calendar.MONTH, -numMesi);
    // azzero i campi che non servono
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }
  
  /**
   * Restitusce una data uguale a quella passata più 'numeroMesi' mesi
   *
   * @param dt la data in input
   * @param numeroMesi il numero di mesi da aggiungere alla data; se negativo
   * i mesi vengono sottratti
   * @return una nuova data con i mesi aggiunti o sottratti
   */
  public static Date addMesi(Date dt, int numeroMesi) {
    Calendar c = calendarFromDate(dt);
    c.add(Calendar.MONTH, numeroMesi);
    return c.getTime();
  }
  
  /**
   * Ritorna 0 se le due date sono uguali, -1 se data1 < data2 1 se data1 > data2.
   *
   * <br>Convertita da RaGi (10 mar 2003) con calendar per evitare i warning
   * dei metodi deprecati sulle date.
   *
   * @param data1 data 1
   * @param data2 data 2
   * @return -1, 0 o 1
   */
  public static int compare(Date data1, Date data2) {
    Calendar c1 = calendarFromDate(data1);
    Calendar c2 = calendarFromDate(data2);

    if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
      if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
        if (c1.get(Calendar.DATE) < c2.get(Calendar.DATE)) {
          return -1;
        }
        if (c1.get(Calendar.DATE) == c2.get(Calendar.DATE)) {
          return 0;
        }
        if (c1.get(Calendar.DATE) > c2.get(Calendar.DATE)) {
          return 1;
        }
      } else {
        if (c1.get(Calendar.MONTH) < c2.get(Calendar.MONTH)) {
          return -1;
        }
        if (c1.get(Calendar.MONTH) > c2.get(Calendar.MONTH)) {
          return 1;
        }
      }
    } else {
      if (c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR)) {
        return -1;
      }
      if (c1.get(Calendar.YEAR) > c2.get(Calendar.YEAR)) {
        return 1;
      }
    }
    return 0;
  }

  /**
   * Ritorna true se i due periodi passati sono consecutivi (possono anche essere contenuti uno nell'altro).
   * I due periodi devono essere ordinati in ordine crescente per data di inizio.
   *
   * @param dtin1 Date
   * @param dtfi1 Date
   * @param dtin2 Date
   * @param dtfi2 Date
   * @return boolean
   */
  public static boolean sonoPeriodiConsecutivi(Date dtin1, Date dtfi1,
      Date dtin2, Date dtfi2) {
    Calendar c2 = calendarFromDate(dtin2);
    c2.add(Calendar.DATE, -1);

    if (compare(dtin1, dtfi2) <= 0 && compare(dtfi1, c2.getTime()) >= 0) {
      return true;
    }
    return false;
  }

  /**
   * Crea un Calendar che corrisponde alla data passata.
   *
   * <br>Il calendar è utilizzato per tutte le operazioni interne di manipolazione
   * del valore (estrazione delle varie parti, confronto fra date, etc).
   *
   * @param dt data da trasformare
   * @return Calendar
   */
  public static Calendar calendarFromDate(Date dt) {
    Calendar c = null;
    if (dt != null) {
      c = Calendar.getInstance();
      c.setTime(dt);
    }
    return c;
  }

  /**
   * Calcola la data della Pasqua dato l'anno.
   *
   * @param anno anno per cui calcolare la data di Pasqua; deve essere compreso
   * fra 1900 e 2100
   * @return la data corrispondente alla domenica di Pasqua per l'anno passato
   * @throws IllegalArgumentException se l'anno non è compreso fra 1900 e 2100
   */
  public static Date getEasterDate(int anno) {
    final int MAXYEAR = 2100;
    final int MINYEAR = 1900;

    if ((anno < MINYEAR) || (anno > MAXYEAR)) {
      throw new IllegalArgumentException(
          "L'anno per il calcolo della pasqua deve essere compreso fra 1900 e 2100.");
    }

    Calendar calEaster = Calendar.getInstance();
    int prime, dominical;
    int i, j;
    int eMonth = 0, eDay = 0;
    int sundayLetter[] = new int[35];
    int goldenNumber[] = { 14, 3, 0, 11, 0, 19, 8, 0, 16, 5, 0, 13, 2, 0, 10,
        0, 18, 7, 0, 15, 4, 0, 12, 1, 0, 9, 17, 6, 0, 0, 0, 0, 0, 0, 0 };

    prime = (anno + 1) % 19;

    if (prime == 0) {
      prime = 19;
    }

    dominical = (anno + (anno / 4) + 6) % 7;

    for (i = 0; i < 35; i++) {
      sundayLetter[i] = 6 - ((i + 6 - 4) % 7);
    }

    lookup: for (i = 0; i < 35; i++) {
      if (prime == goldenNumber[i]) {
        for (j = i + 1; j < 35; j++) {
          if (sundayLetter[j] == dominical) {
            if (j > 9) {
              eMonth = 4;
              eDay = j - 9;
            } else {
              eMonth = 3;
              eDay = j + 22;
            }
            break lookup;
          }
        }
      }
    }

    calEaster.set(Calendar.DAY_OF_MONTH, eDay);
    calEaster.set(Calendar.MONTH, eMonth - 1);
    calEaster.set(Calendar.YEAR, anno);
    return calEaster.getTime();
  }

  /**
   * Guarda se il giorno passato corrisponde a un giorno festivo del calendario
   * sammarinese.
   * <p>
   * Calcola anche il giorno di Pasquetta e, per le festività sammarinesi, il giorno
   * del Corpus Domini.
   * <p>
   * @param d data da controllare
   * @param type tipo di festività da calcolare. Valori possibili
   * <ul>
   * <li>{@link #HOL_RSM}  festività sammarinesi</li>
   * <li>{@link #HOL_ITA}  festività italiane</li>
   * <li>{@link #HOL_RSM_STATO} festività sammarinesi contratto stato (24 dic e 31 dic)
   *                             Daniele 28/11/2008: + recupero festività
   * </li>
   * </ul>
   * @param saturday impostare a <code>true</code> se si vuole considerare il sabato festivo.
   * @return <code>true</code> se la data passata è un goirno festivo.
   */
  public static boolean isHoliday(Date d, int type, boolean saturday) {
	 boolean bFestivo = isHolidaySatSun(d, type, saturday, true) ;	  
    return bFestivo;
  }

  /**
   * Daniele 19/07/2011: creo un'altro metodo isHoliday che ha come parametro
   *                     anche la domenica
   * Guarda se il giorno passato corrisponde a un giorno festivo del calendario
   * sammarinese.
   * <p>
   * Calcola anche il giorno di Pasquetta e, per le festività sammarinesi, il giorno
   * del Corpus Domini.
   * <p>
   * @param d data da controllare
   * @param type tipo di festività da calcolare. Valori possibili
   * <ul>
   * <li>{@link #HOL_RSM}  festività sammarinesi</li>
   * <li>{@link #HOL_ITA}  festività italiane</li>
   * <li>{@link #HOL_RSM_STATO} festività sammarinesi contratto stato (24 dic e 31 dic)
   *                             Daniele 28/11/2008: + recupero festività
   * </li>
   * </ul>
   * @param saturday impostare a <code>true</code> se si vuole considerare il sabato festivo.
   * @param sunday   impostare a <code>true</code> se si vuole considerare la domenica festiva.
   * @return <code>true</code> se la data passata è un goirno festivo.
   */
  public static boolean isHoliday(Date d, int type, boolean saturday, boolean sunday) {
	 boolean bFestivo = isHolidaySatSun(d, type, saturday, sunday) ;	  
     return bFestivo;
  }
  
  /**
   * Daniele 19/07/2011: aggiunto parametro sunday 
   * Guarda se il giorno passato corrisponde a un giorno festivo del calendario
   * sammarinese.
   * <p>
   * Calcola anche il giorno di Pasquetta e, per le festività sammarinesi, il giorno
   * del Corpus Domini.
   * <p>
   * @param d data da controllare
   * @param type tipo di festività da calcolare. Valori possibili
   * <ul>
   * <li>{@link #HOL_RSM}  festività sammarinesi</li>
   * <li>{@link #HOL_ITA}  festività italiane</li>
   * <li>{@link #HOL_RSM_STATO} festività sammarinesi contratto stato (24 dic e 31 dic)
   *                             Daniele 28/11/2008: + recupero festività
   * </li>
   * </ul>
   * @param saturday impostare a <code>true</code> se si vuole considerare il sabato festivo.
   * @param sunday   impostare a <code>true</code> se si vuole considerare la domenica festiva.
   * @return <code>true</code> se la data passata è un giorno festivo.
   */
  private static boolean isHolidaySatSun(Date d, int type, boolean saturday, boolean sunday) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    int day = cal.get(Calendar.DAY_OF_WEEK);
    //se è sabato o domenica è festivo
    if (sunday) {
	    if (day == Calendar.SUNDAY) {
	      return true;
	    }
    }
    if (saturday) {
      if (day == Calendar.SATURDAY) {
        return true;
      }
    }

    day = cal.get(Calendar.DAY_OF_MONTH);
    int month = cal.get(Calendar.MONTH) + 1;

    //giorni SEMPRE FESTIVI!
    if ((month == 1) && ((day == 1) || (day == 6))) {
      return true; //primo dell'anno, epifania
    }
    if ((month == 5) && (day == 1)) {
      return true; //Festa lavoratori
    }
    if ((month == 8) && (day == 15)) {
      return true; //Ferragosto
    }
// daniele 11/10/2011: il 2 novembre è festa solo a san marino non in italia
//    if ((month == 11) && ((day == 1) || (day == 2))) {
//      return true; //Ognissanti e Defunti
//    }
    if ((month == 11) && (day == 1)) {
      return true; //Ognissanti 
    }
    if ((month == 12) && ((day == 8) || (day == 25) || (day == 26))) {
      return true; //Immacolata, Vigilia, Natale, S.Stefano, ultimo dell'anno
    }

    //------- calcolo Pasqua e Pasquetta ------
    int year = cal.get(Calendar.YEAR);
    Calendar pasqua = calendarFromDate(getEasterDate(year));
// Daniele 19/07/2011: verifico anche pasqua    
    if ((month == pasqua.get(Calendar.MONTH) + 1)
            && (day == pasqua.get(Calendar.DAY_OF_MONTH))) {
    	return true; //Pasqua
    }
    
    //aggiungo un giorno per avere la pasquetta
    pasqua.add(Calendar.DATE, 1);
    if ((month == pasqua.get(Calendar.MONTH) + 1)
        && (day == pasqua.get(Calendar.DAY_OF_MONTH))) {
      return true; //Pasquetta
    }

    if (type == HOL_RSM || type == HOL_RSM_STATO) {
      //controllo festività nazionali sammarinesi
      if ((month == 2) && (day == 5)) {
        return true; //S.Agata
      }
      if ((month == 3) && (day == 25)) {
        return true; //Arengo
      }
      if ((month == 4) && (day == 1)) {
        return true; //Reggenti Aprile
      }
      if ((month == 7) && (day == 28)) {
        return true; //Liberazione
      }
      if ((month == 9) && (day == 3)) {
        return true; //S.Marino
      }
      if ((month == 10) && (day == 1)) {
        return true; //Reggenti Ottobre
      }
// Daniele 11/10/2011
      if ((month == 11) && (day == 2)) {
        return true; //Defunti
      }
      //aggiungo 59 giorni alla pasquetta (Corpus Domini = Pasqua + 60 gg)
      //per avere il giorno del Corpus Domini
      pasqua.add(Calendar.DATE, 59);
      if ((month == pasqua.get(Calendar.MONTH) + 1)
          && (day == pasqua.get(Calendar.DAY_OF_MONTH))) {
        return true; //Corpus Domini
      }
    }

    if (type == HOL_RSM_STATO) {
      //controllo festività uffici Stato
      if ((month == 12) && ((day == 24) || (day == 31))) {
        return true; //Vigilia, ultimo dell'anno
      }

  // Daniele 28/11/2008: recupero festività uffici stato: se il 24 Dicembre (e quindi anche il 31) 
  // vengono presi di Sabato o Domenica allora sono festivi il 14 e il 16 Agosto
// Daniele 03/08/2011: modifica per differenziare se il 24 è sabato o domenica
//   se il 24 Dicembre (e quindi anche il 31) è sabato allora sono festivi il 23 e il 30 dicembre
//   se il 24 Dicembre (e quindi anche il 31) è domenica allora sono festivi il 14 e il 16 Agosto
      if ((month == 12) && ((day == 23) || (day == 30))) {
          Calendar calDic = Calendar.getInstance();
          calDic.set(year, 11, 24);
          boolean vigNataleIsFest = calDic.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
          if (vigNataleIsFest) {
            return true; // recupero festività 
          }
      }
      
      if ((month == 8) && ((day == 14) || (day == 16))) {
        Calendar calDic = Calendar.getInstance();
        calDic.set(year, 11, 24);
        boolean vigNataleIsFest = calDic.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        if (vigNataleIsFest) {
          return true; // recupero festività 
        }
      }
    }

    if (type == HOL_ITA) {
      //controllo festività nazionali italiane
      if ((month == 4) && (day == 25)) {
        return true; //Liberazione
      }
      if ((month == 6) && (day == 2)) {
        return true; //Festa della Repubblica
      }
    }

    return false;
  }
  
  
  /**
   * Restituisce una string a nella forma hh:mm che è l'ora
   * della data passata.
   * Vedi {@link #getHHMM(Calendar)}.
   *
   * @param d Date
   * @return String
   */
  public static String getHHMM(Date d) {
    if (d == null) {
      return "";
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    return getHHMM(cal);
  }

  /**
   * Restituisce una string a nella forma hh:mm che è l'ora
   * del calendar passato.
   * Se ad esempio cal = '23/05/2005 08:29:17', questo metodo
   * ritorna "08:29".
   * @param cal Calendar calendar che contiene la data
   * @return String ora nella forma hh:mm, oppure stringa vuota se cal è null.
   * @see #getHHMM(Date)
   */
  public static String getHHMM(Calendar cal) {
    if (cal == null) {
      return "";
    }
    int h = cal.get(Calendar.HOUR_OF_DAY);
    int m = cal.get(Calendar.MINUTE);
    StringBuffer st = new StringBuffer();
    if (h < 10) {
      st.append("0");
    }
    st.append(h + ":");
    if (m < 10) {
      st.append("0");
    }
    st.append(m);
    return st.toString();
  }

  /**
   * Passata una data, ne converte la parte <b>ore:minuti</b> in minuti totali.
   * La parte mese/giorno/anno è ignorata. Anche i secondi sono ignorati.
   *
   * Ad esempio chiamata con <code>'04/11/2003 03:21:48'</code>, ritorna i minuti
   * di 3:21 cioè 201.
   *
   * @param date Date data da convertire
   * @return int i minuti dell'ora della data passata, 0 se date è null.
   */
  public static int convOraInMinuti(java.util.Date date) {
    if (date == null) {
      return 0;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * Passato un int con la quantità di minuti, ritorna la sua rappresentazione
   * nel formato hh:mm.
   * Ad esempio <tt>convMinutiInOra(125)</tt> ritorna "02:05".
   *
   * @param minuti int rappresentazione dei minuti nel formato hh:mm
   * @return String conversione dei minuti passati nel formato "hh:mm"; le ore e i minuti sono sempre
   * in 2 cifre.
   * @throws CISException nel caso i minuti siano minori di 0 o maggiori di 1439 (=23:59)
   */
  public static String convMinutiInOra(int minuti) throws IllegalArgumentException {
    if (minuti < 0 || minuti >= 1440) { //1440 = 24 ore
      throw new IllegalArgumentException(String.format(
          "convMinutiInOra: i minuti %d non corrispondono ad una valida ora.", minuti));
    }

    int ore = minuti / 60;
    int min = minuti - ore * 60;
    String s_ore = (ore < 10) ? "0" + ore : String.valueOf(ore);
    String s_min = (min < 10) ? "0" + min : String.valueOf(min);
    return s_ore + ":" + s_min;
  }

  /**
   * Passato un int con la quantità di minuti, ritorna la sua rappresentazione
   * nel formato hh:mm.
   * Ad esempio convMinutiInQtaOre(5246) ritorna '87:26'.
   * L'unica differenza con la convMinutiInOra è che non c'è il vincolo
   * che i minuti corrispondano a meno di 24 ore.
   *
   * @param minuti int qta minuti da convertire; se minore di zero, da exception.
   * @return String
   */
  public static String convMinutiInQtaOre(int minuti) {
    if (minuti < 0)
      throw new IllegalArgumentException(
          "I minuti sono un numero minore di zero.");

    int ore = minuti / 60;
    int min = minuti - ore * 60;
    String s_ore = (ore < 10) ? "0" + ore : String.valueOf(ore);
    String s_min = (min < 10) ? "0" + min : String.valueOf(min);
    return s_ore + ":" + s_min;
  }

  /**
   * Restituisce ore e minuti numerici da una stringa nel formato hh:mm.
   * Se l'ora non è valida, genera exception.
   * Cerca di fare alcune assunzioni nel caso la stringa non abbia il separatore
   * ':' e/o non abbia le ore o i minuti di 2 cifre.
   * <br/>
   * Esempi:<br/>
   * <br/> <tt>getOreMinuti("1234")</tt> --&gt; <tt>[12,34]</tt>
   * <br/> <tt>getOreMinuti("18")</tt> --&gt; <tt>[18,0]</tt>
   * <br/> <tt>getOreMinuti("123")</tt> --&gt; <tt>[1,23]</tt>
   * <br/> <tt>getOreMinuti("12:3")</tt> --&gt; <tt>[12,30]</tt>
   *    
   * @param sOra stringa nel formato hh:mm
   * @return array con ore (indice 0) e minuti (indice 1)
   * 
   * @see #convHHMMInMinuti(String)
   */
  public static int[] convStringaInMinuti(String sOra) {
    if (sOra == null || sOra.length() == 0)
      throw new IllegalArgumentException("L'ora passata è vuota");

    sOra = sOra.trim();
    int i = sOra.indexOf(':');
    String min = "", ora = "";
    if (i > 0 && i < sOra.length() - 1) {
      min = sOra.substring(i + 1).trim();
      ora = sOra.substring(0, i).trim();
    }
    if (i < 0) {
      if (sOra.length() == 4) {
        ora = sOra.substring(0, 2);
        min = sOra.substring(2);
      } else if (sOra.length() == 3) {
        ora = "0" + sOra.charAt(0);
        min = sOra.substring(1);
      } else if (sOra.length() == 2) {
        ora = sOra;
        min = "0";
      } else if (sOra.length() == 1) {
        ora = "0" + sOra;
        min = "0";
      }
    }

    if (ora.length() == 0 || ora.length() > 2 || min.length() != 2)
      throw new IllegalArgumentException("L'ora passata non è valida: " + sOra);

    int nMin = Integer.parseInt(min);
    int nOra = Integer.parseInt(ora);
    if (nMin < 0 || nMin >= 60)
      throw new IllegalArgumentException(
          "L'ora passata non è valida, minuti errati: " + sOra);
    if (nOra < 0 || nOra >= 24)
      throw new IllegalArgumentException(
          "L'ora passata non è valida, ore errate: " + sOra);

    return new int[] { nOra, nMin };
  }

  /**
   * Restituisce l'anno corrente.
   *
   * @return l'anno corrente.
   */
  public static int getCurrentYear() {
    Calendar cNow = Calendar.getInstance();
    return cNow.get(Calendar.YEAR);
  }

  /**
   * Restituisce il mese corrente.
   * Il mese qui è 0-based, cioè gennaio=0, febbraio=1,...,dicembre=11
   *
   * @return il mese corrente.
   */
  public static int getCurrentMonth() {
    Calendar cNow = Calendar.getInstance();
    return cNow.get(Calendar.MONTH);
  }

  public static String getNomeMese(int mese) {
    switch (mese) {
    case 1:
      return "Gennaio";
    case 2:
      return "Febbraio";
    case 3:
      return "Marzo";
    case 4:
      return "Aprile";
    case 5:
      return "Maggio";
    case 6:
      return "Giugno";
    case 7:
      return "Luglio";
    case 8:
      return "Agosto";
    case 9:
      return "Settembre";
    case 10:
      return "Ottobre";
    case 11:
      return "Novembre";
    case 12:
      return "Dicembre";
    default:
      return "???";
    }
  }

  /**
   * Restituisce il giorno del mese corrente.
   *
   * @return il giorno del mese corrente.
   */
  public static int getCurrentDay() {
    Calendar cNow = Calendar.getInstance();
    return cNow.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Restituisce l'ora dell'istante corrente in formato 24h.
   *
   * @return ora dell'istante corrente (0-23).
   */
  public static int getCurrentHour() {
    Calendar cNow = Calendar.getInstance();
    return cNow.get(Calendar.HOUR_OF_DAY);
  }

  /**
   * Restituisce i minuti dell'istante corrente.
   *
   * @return minuti dell'istante corrente (0-59).
   */
  public static int getCurrentMinute() {
    Calendar cNow = Calendar.getInstance();
    return cNow.get(Calendar.MINUTE);
  }

  /**
   * Ritorna il nome del giorno della data passata ("domenica", "Lunedì", etc)
   *
   * @param cal la data da cui estrarre il giorno
   * @return Il nome del giorno
   */
  public static String getDayName(Calendar cal) {
    if (null == cal)
      return "";
    String aszNames[] = { "Domenica", "Lunedì", "Martedì", "Mercoledì",
        "Giovedì", "Venerdì", "Sabato" };
    return aszNames[cal.get(Calendar.DAY_OF_WEEK) - 1];
  }
  
  /**
   * Ritorna il numero del giorno della settimana secondo il costume italiano
   * 1=Lunedi', 2=Martedi', 3=Mercoledi',4=Giovedi',5=Venerdi',6=Sabato,7=Domenica 
   * 
   * @param giorno - giorno di cui si vuole ottenere l'informazione
   * @return il numero che rappresenta il giorno all'interno della settimana (come listato sopra)
   */
  public static int getWeekNumber(Date giorno) {
    int giornoSettimana = 0;
    Calendar c = Calendar.getInstance();
    c.setTime(giorno);
    int calendarDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
    if( calendarDayOfWeek == 1 ) {
      giornoSettimana = 7;
    }
    else {
      giornoSettimana = calendarDayOfWeek - 1;
    }
    return giornoSettimana;
  }


  /**
   * Confronta la parte anno-mese-giorno di due date.
   *
   * @param dt1 Date data
   * @param dt2 Date altra data
   *
   * @return boolean true se le due date si riferiscono allo stesso giorno, false altrimenti;
   * se almeno una delle due è null, ritorna false.
   */
  public static boolean isSameDay(Date dt1, Date dt2) {
    if (dt1 == null || dt2 == null) {
      return false;
    }
    Calendar cal1 = calendarFromDate(dt1);
    Calendar cal2 = calendarFromDate(dt2);
    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
        && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
        && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
      return true;
    }
    return false;
  }

  /**
   * Ritorna il nome del giorno della data passata ("domenica", "Lunedì", etc)
   *
   * @param dt la data da cui estrarre il giorno
   * @return Il nome del giorno
   */
  public static String getDayName(Date dt) {
    if (null == dt) {
      return "";
    }
    Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return getDayName(c);
  }

  /**
   * Ritorna l'anno della data passata.
   *
   * @param dt Date data di cui tornare l'anno
   * @return int ritorna anno della data passata oppure -1 se la data è null o errori.
   */
  public static int getYear(Date dt) {
    try {
      if (dt != null) {
        Calendar cal = calendarFromDate(dt);
        return cal.get(Calendar.YEAR);
      }
      return -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  /**
   * Ritorna il mese della data passata.
   * <br/>
   * <em>NB: il mese ritornato è 1-based</em>
   *
   * @param dt Date data di cui tornare il mese
   * @return int ritorna mese della data passata oppure -1 se la data è null o errori.
   */
  public static int getMonth(Date dt) {
    try {
      if (dt != null) {
        Calendar cal = calendarFromDate(dt);
        return cal.get(Calendar.MONTH) + 1;
      }
      return -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  /**
   * Ritorna l'ora della data passata nella scala 0-23.
   *
   * @param dt Date data di cui tornare l'ora
   * @return int ritorna ora della data passata oppure -1 se la data è null o errori.
   */
  public static int getHour(Date dt) {
    try {
      if (dt != null) {
        Calendar cal = calendarFromDate(dt);
        return cal.get(Calendar.HOUR_OF_DAY);
      }
      return -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  /**
   * Ritorna i minuti della data passata.
   *
   * @param dt Date data di cui tornare i minuti
   * @return int ritorna minuti della data passata oppure -1 se la data è null o errori.
   */
  public static int getMinute(Date dt) {
    try {
      if (dt != null) {
        Calendar cal = calendarFromDate(dt);
        return cal.get(Calendar.MINUTE);
      }
      return -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  /**
   * Restituisce una data in cui la parte data (giorno,mese,anno) è presa dalla
   * prima data, la parte ora (ore,minuti) è presa dalla seconda.
   * @param d data da cui prendere giorno,mese,anno
   * @param dateHour data da cui prendere ore,minuti
   * @return d(giorno,mese,anno) + dateHour(ore,minuti)
   */
  public static Date copyHourToDate(Date d, Date dateHour) {
    Calendar c = calendarFromDate(d);
    Calendar c2 = calendarFromDate(dateHour);
    c.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
    c.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
    return c.getTime();
  }

  /**
   * Restituisce una data in cui la parte data (giorno,mese,anno) e' presa dalla
   * data passata e la parte ora e' presa da ore e minuti passati.
   * @param d data da cui prendere giorno,mese,anno
   * @param ore ore da impostare alla data
   * @param minuti minuti da impostare alla data
   * @return d(giorno,mese,anno) + (ore,minuti)
   */
  public static Date setHourToDate(Date d, int ore, int minuti) {
    Calendar c = calendarFromDate(d);
    c.set(Calendar.HOUR_OF_DAY, ore);
    c.set(Calendar.MINUTE, minuti);
    return c.getTime();
  }

  /**
   * Restituisce la differenza fra due date nell'unità richiesta.
   * 
   * <br>La differenza in mesi e in anni si intende in 'mesi interi' o 'anni interi', cioè
   * il giorno (e il mese nel caso dell'anno) è significativo. Ad esempio  
   * fra il 29 gennaio 2011 e il 28 marzo 2011 la differenza in mesi è 1 !
   * Le ore, invece, sono ignorate nelle differenze in anni e mesi. 
   *
   * @param dInizio data inizio
   * @param dFine data fine
   * @param unit unita' di misura del valore ritornato. E' espressa secondo i valori
   *              definiti in Calendar: {@link Calendar#SECOND}, {@link Calendar#HOUR}, etc
   * @return la differenza tra le due date nell'unita' indicata
   */
  public static long dateDiff(Date dInizio, Date dFine, int unit) {
    if (dFine.before(dInizio)) {
      throw new IllegalArgumentException("dateDiff: la seconda data deve essere successiva o identica alla prima");
    }
    long delta = dFine.getTime() - dInizio.getTime();
    Calendar calInizio = calendarFromDate(dInizio);
    Calendar calFine = calendarFromDate(dFine);
    delta = calFine.getTimeInMillis() + calFine.get(Calendar.DST_OFFSET) + calFine.get(Calendar.ZONE_OFFSET);
    delta -= calInizio.getTimeInMillis() + calInizio.get(Calendar.DST_OFFSET) + calInizio.get(Calendar.ZONE_OFFSET);
    switch (unit) {
      case Calendar.MILLISECOND:
        break;
      case Calendar.SECOND:
        delta /= 1000;
        break;
      case Calendar.MINUTE:
        delta /= (1000 * 60);
        break;
      case Calendar.HOUR:
        delta /= (1000 * 60 * 60);
        break;
      case Calendar.DATE:
        delta /= (1000 * 60 * 60 * 24);
        break;
      case Calendar.MONTH:
        int diffAnni = calFine.get(Calendar.YEAR) - calInizio.get(Calendar.YEAR); 
        int dmesi = -1;
        if( 0 == diffAnni ) {
          // stesso anno
          dmesi = calFine.get(Calendar.MONTH) - calInizio.get(Calendar.MONTH);        
        } else {
          // anni diversi
          dmesi = calFine.get(Calendar.MONTH) + (12 - calInizio.get(Calendar.MONTH)) + 12 * (diffAnni - 1);
        }      
        if (calFine.get(Calendar.DATE) < calInizio.get(Calendar.DATE)) {
          //giorno finale minore dell'iniziale: un mese di meno
          dmesi--;
        }
        return dmesi;
      case Calendar.YEAR:
        int anni = calFine.get(Calendar.YEAR) - calInizio.get(Calendar.YEAR);
        int mesi = calFine.get(Calendar.MONTH) - calInizio.get(Calendar.MONTH);
        if (mesi < 0) {
          //mese finale minore di mese iniziale: decremento anni
          anni--;
        } else if (mesi == 0) {
          //stesso mese: guardo se è passato il giorno
          int giorni = calFine.get(Calendar.DAY_OF_MONTH) - calInizio.get(Calendar.DAY_OF_MONTH);
          if (giorni < 0) {
            //mese finale = mese iniziale ma giorno finale minore di giorno iniziale: decremento anni
            anni--;
          }
        }
        return anni;
      default:
        // Unità sconosciuta !!!
        throw new UnsupportedOperationException("Unità " + unit + " sconosciuta per differenza di date");
    }
    return delta;
  }

//  /**
//   * Azzera la parte da hh fino a ms di un calendar.
//   *
//   * @param c il Calendar da azzerare
//   * @return Calendar
//   */
//  private static Calendar _azzeraHHMMSSCalendar(Calendar c) {
//    c.set(Calendar.HOUR_OF_DAY, 0);
//    return _azzeraMMSSCalendar(c);
//  }

//  /**
//   * Azzera la parte da mm fino a ms di un calendar.
//   *
//   * @param c il Calendar da azzerare
//   * @return Calendar
//   */
//  private static Calendar _azzeraMMSSCalendar(Calendar c) {
//    c.set(Calendar.MINUTE, 0);
//    return azzeraSS(c);
//  }  
  
//  /**
//   * Azzera la parte da ss fino a ms di un calendar.
//   *
//   * @param c il Calendar da azzerare
//   * @return Calendar
//   */
//  private static Calendar _azzeraSSCalendar(Calendar c) {
//    c.set(Calendar.SECOND, 0);
//    c.set(Calendar.MILLISECOND, 0);
//    return c;
//  }  
  
  /**
   * Azzera la parte da hh fino a ms di un calendar.
   *
   * @param c il Calendar da azzerare
   * @return Calendar
   */
  public static Calendar azzeraHHMMSS(Calendar c) {
    c.set(Calendar.HOUR_OF_DAY, 0);
    return azzeraMMSS(c);
  }

  /**
   * Azzera la parte data (da giorno ad anno) di un calendar.
   * Azzera inoltre anche la parte millisecondi, in modo da lasciare
   * inalterati i soli campi ora-minuti-secondi. 
   *
   * @param c il Calendar da azzerare
   * @return Calendar modificato
   */
  public static Calendar azzeraData(Calendar c) {
    c.set(Calendar.DAY_OF_MONTH, 0);
    c.set(Calendar.MONTH, 0);
    c.set(Calendar.YEAR, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }

  /**
   * Ritorna una data uguale a quella data
   * in cui la parte giorno/mese/anno è uguale a zero.
   * Azzera inoltre anche la parte millisecondi, in modo da lasciare
   * inalterati i soli campi ora-minuti-secondi. 
   *
   * @param dt data da azzerara
   * @return data copia di quella passata con la data azzerata
   */
  public static Date azzeraData(Date dt) {	  
	  Calendar c = calendarFromDate(dt);
	  azzeraData(c);
	  Date d = c.getTime();
	  return d;
  }
  
  /**
   * Azzera la parte da mm fino a ms di un calendar.
   *
   * @param c il Calendar da azzerare
   * @return Calendar
   */
  public static Calendar azzeraMMSS(Calendar c) {
    c.set(Calendar.MINUTE, 0);
    return azzeraSS(c);
  }

  /**
   * Azzera la parte da ss fino a ms di un calendar.
   *
   * @param c il Calendar da azzerare
   * @return Calendar
   */
  public static Calendar azzeraSS(Calendar c) {
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }

  /**
   * Ritorna una data in cui la parte ora (da hh fino a ms)
   * è zero e la parte data (gg/mm/aa) è uguale alla data passata.
   *
   * @param dt la data da copiare
   * @return Date una nuova data con la parte gg/mm/aa uguale a dt e l'ora a zero
   */
  public static Date azzeraHHMMSS(Date dt) {
    Calendar c = calendarFromDate(dt);
    azzeraHHMMSS(c);
    Date d = c.getTime();
    return d;
  }

  /**
   * Ritorna una data in cui la parte da mm fino a ms
   * è zero e la parte data (gg/mm/aa HH) è uguale alla data passata.
   *
   * @param dt la data da copiare
   * @return Date una nuova data con la parte gg/mm/aa HH uguale a dt e dai minuti in giù a zero
   */
  public static Date azzeraMMSS(Date dt) {
    Calendar c = calendarFromDate(dt);
    azzeraMMSS(c);
    Date d = c.getTime();
    return d;
  }

  /**
   * Ritorna una data in cui la parte da ss fino a ms
   * è zero e la parte data (gg/mm/aa HH:mm) è uguale alla data passata.
   *
   * @param dt la data da copiare
   * @return Date una nuova data con la parte gg/mm/aa HH :mmuguale a dt e dai secondi in giù a zero
   */
  public static Date azzeraSS(Date dt) {
    Calendar c = calendarFromDate(dt);
    azzeraSS(c);
    Date d = c.getTime();
    return d;
  }

  /**
   * Ritorna un Timestamp in cui la parte ora (hh:MM:ss.SSSS)
   * è zero e la parte data (gg/mm/aa) è uguale al Timestamp passato.
   *
   * @param ts Timestamp da copiare
   * @return Timestamp un nuovo Timestamp con la parte gg/mm/aa uguale a ts e l'ora a zero
   */
  public static Timestamp azzeraHHMMSS(Timestamp ts) {
    Date dt = new Date(ts.getTime());
    Calendar c = calendarFromDate(dt);
    azzeraHHMMSS(c);
    dt = c.getTime();
    Timestamp ts2 = new Timestamp(dt.getTime());
    return ts2;
  }

  /**
   * Restituisce il primo lunedi del mese passato.
   * Se impostato bAncheNoLunedi, se il primo giorno capita non di lunedì lo restituisco.
   * @param dt
   * @return
   */
  public static Date getPrimoLunediDelMese(Date dt, boolean bAncheNoLunedi) {
    Date dtprimo = getPrimoDelMese(dt);
    Calendar c = Calendar.getInstance();
    c.setFirstDayOfWeek(Calendar.MONDAY);
    c.setTime(dtprimo);
    azzeraHHMMSS(c);
    int giorno = c.get(Calendar.DAY_OF_WEEK);
    giorno--;
    if (giorno == 0) {
      giorno = 7;
    }
    int diff = 7 - giorno + 1;
    if (!bAncheNoLunedi && diff < 7) {
      c.add(Calendar.DATE, diff);
    }
    return azzeraHHMMSS(c.getTime());
  }

  /**
   * Restituisce una data ottenuta sommando i giorni passati alla data passata. 
   * NOTA BENE: azzera HH MM SS
   *
   * @param dt data a cui sommare i giorni
   * @param numgiorni giorni da sommare; se negativo li sottrae
   * @param bNoOltreFineMese se true, se viene superata la fine del mese viene restituita la fine del mese.
   * 
   * @return una nuova data ottenuta sommando a quella passata i giorni indicati; la data originale non viene modificata
   * 
   * @see #addGiorni(Date, int)
   */
  public static Date addGiorni(Date dt, int numgiorni, boolean bNoOltreFineMese) {
    Calendar c = calendarFromDate(dt);
    azzeraHHMMSS(c);
    c.add(Calendar.DATE, numgiorni);
    Date dtfine = getUltimoDelMese(dt);
    if (bNoOltreFineMese && compare(c.getTime(), dtfine) > 0) {
      c.setTime(dtfine);
    }
    return azzeraHHMMSS(c.getTime());
  }


  /**
   * Restituisce una data ottenuta sommando i giorni passati alla data passata. 
   * NOTA BENE: azzera HH MM SS
   *
   * @param dt data a cui sommare i giorni
   * @param numgiorni giorni da sommare; se negativo li sottrae
   * @return una nuova data ottenuta sommando a quella passata i giorni indicati; la data originale non viene modificata
   * 
   * @see #addGiorni(Date, int, boolean)
   */
  public static Date addGiorni(Date dt, int numgiorni) {
    return addGiorni(dt, numgiorni, false);
  }

  /**
   * Restituisce il primo giorno non festivo maggiore o uguale alla data passata.
   * Se la data passata è non festiva ritorna la data stessa.
   * @param dt
   * @return
   */
  public static Date getPrimoNonFestivo(Date dt) {
    try {
      Date d = dt;
      while (isHoliday(d, HOL_RSM, true)) {
        d = addGiorni(d, 1, false);
      }
      return d;
    } catch (Exception ex) {
      return null;
    }
  }

  /** 
   * Fabri:
   * Restituisce il primo giorno non festivo.
   * Se la data passata è non festiva ritorna la data stessa.
   * 
   * @param dt - data da cui incominciare la ricerca
   * @param versoFuturo - true se devo cercare, a partire da dt, verso il futuro, false se devo cercare nel passato
   * @return La prima data non festiva
   */
  public static Date getPrimoNonFestivo(Date dt, boolean versoFuturo) {
    Date dataNonFestiva = null;
    //Se voglio cercare verso il futuro...
    if (versoFuturo) {
      dataNonFestiva = getPrimoNonFestivo(dt);
    }
    //...altrimenti cerco nel passato
    else {
      try {
        dataNonFestiva = dt;
        while (isHoliday(dataNonFestiva, HOL_RSM, true)) {
          dataNonFestiva = addGiorni(dataNonFestiva, -1, false);
        }
      } catch (Exception ex) {
        return null;
      }
    }

    return dataNonFestiva;
  }

  /**
   * Wrapper per Timestamp.valueOf(String).
   *
   * <br> Per rappezzare un comportamento cretino di Timestamp.valueof() ...
   * Per forza devo avere il formato con hh_mm_ss anche se l'attributo
   * è LOGICAMENTE una data.
   *
   * @return Timestamp
   */
  public static Timestamp valueOfTimestamp(String szVal) {
    String szTmp = szVal;

    if ((szTmp.length() > 0)
        && (szTmp.length() < "yyyy-MM-dd hh:mm:ss".length())) {
      if (szTmp.length() == "yyyy-MM-dd".length()) {
        szTmp += " 00:00:00";
      } else if (szTmp.length() == "yyyy-MM-dd hh".length()) {
        szTmp += ":00:00";
      } else if (szTmp.length() == "yyyy-MM-dd hh:mm".length()) {
        szTmp += ":00";
      }
    }
    return Timestamp.valueOf(szTmp);
  }

  /** Calcola l'eta (anni,mesi,giorni) alla data di riferimento in base alla data di nascita passata.
   *  Ad esempio: <pre>
   *   Date d1 = DtUtil.createDate(2001, 1, 23);
   *   Date d2 = DtUtil.createDate(2009, 4, 29);
   *   int eta[] = DtUtil.calcolaEta(d2, d1);
   * </pre>
   * DtUtil.calcolaEta ritorna l'array (eta[]) di interi [8,3,6] .
   * 
   * @param dtRif data di riferimento, solitamente è il giorno attuale
   * @param dtNascita data di nascita, da cui calcolare la differenza con dtRif
   * @return vettore che contiene rispettivamente l'eta in anni, mesi e giorni
   * */
  public static int[] calcolaEta(Date dtRif, Date dtNascita) {
    int[] eta = { 0, 0, 0 };

    // se la data di riferimento è precedente alla data di nascita, si ritorna eta={0,0,0}
    if (dtRif.compareTo(dtNascita) <= 0) {
      return eta;
    }

    eta[0] = getYear(dtRif) - getYear(dtNascita);
    if (getMonth(dtRif) < getMonth(dtNascita)) {
      eta[0]--;
    }

    if (getMonth(dtRif) == getMonth(dtNascita)) {
      if (getDay(dtRif) < getDay(dtNascita)) {
        eta[0]--;
        eta[1] = 11;
        eta[2] = getGiorniDelMese(dtNascita) - getDay(dtNascita)
            + getDay(dtRif);
      } else {
        eta[2] = getDay(dtRif) - getDay(dtNascita);
      }
    } else {

      if (getMonth(dtRif) < getMonth(dtNascita)) {
        eta[1] = 12 - getMonth(dtNascita) + getMonth(dtRif);
      }
      if (getMonth(dtRif) > getMonth(dtNascita)) {
        eta[1] = getMonth(dtRif) - getMonth(dtNascita);
      }
      if (getDay(dtRif) < getDay(dtNascita)) {
        eta[1]--;
        eta[2] = getGiorniDelMese(dtNascita) - getDay(dtNascita)
            + getDay(dtRif);
      } else {
        eta[2] = getDay(dtRif) - getDay(dtNascita);
      }
    }
    return eta;
  }

  /**
   * Testa se l'anno passato è bisestile
   *
   * @param nAnno int
   * @return boolean
   */
  public static boolean isAnnoBisestile(int nAnno) {
    // bisestile se divisibile per 4 ma non per 100
    // a meno che non sia divisibile per 400
    return (0 == nAnno % 400) || ((0 == nAnno % 4) & (0 != nAnno % 100));
  }

  /** Ritorna il numero di secondi passati dalla data 'startingTime' ad ora
   */
  public static long elapsedSeconds(Date startingTime) {
    Date now = new Date();
    long elapsedSeconds = (now.getTime() - startingTime.getTime()) / 1000;

    return elapsedSeconds;
  }

  /**
   * Aggiunge la quantità di secondi 'secondi' alla data passata
   * 
   * @param data - la data a cui aggiungere i secondi
   * @param secondi - i secondi da aggiungere
   * @return la nuova data ottenuta sommando i secondi passati alla data passata
   */
  public static Date addSecondi(Date data, int secondi) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(data);
    cal.add(Calendar.SECOND, secondi);
    
    return cal.getTime();
  }
  
  /**
   * Converte un valore intero rappresentato in secondi, in una stringa HH:MM
   * Daniele 03/10/2011: modifiche per gestire valori negativi
   * @param oreMinutiInSecondi - secondi passati da calcolare
   * @return la stringa "HH:MM"
   */
  public static String convHHMM(int oreMinutiInSecondi){
    String szSegno = "";
    if (oreMinutiInSecondi < 0){
      szSegno = "-";
      oreMinutiInSecondi = -1 * oreMinutiInSecondi;
    }
    int ore = oreMinutiInSecondi / 3600;
    int minuti = (oreMinutiInSecondi % 3600) / 60;
    String strOre = "" + ore;
    String strMin = "" + minuti;
    if(ore <= 9) {
      strOre = "0" + ore;
    }
    if(minuti <= 9) {
      strMin = "0" + minuti;
    }
    return szSegno + "" + strOre + ":" + strMin ; 
  }

  /**
   * Converte un valore intero rappresentato in secondi, in una stringa H ore M minuti
   * Se le ore sono = 0 non vengono inserite nella stringa
   * Se i minuti sono = 0 non vengono inserite nella stringa
   * @param oreMinutiInSecondi - secondi passati da calcolare
   * @return la stringa "H ore M minuti"
   */
  public static String convHHMMEsteso(int oreMinutiInSecondi){
    String szTesto = "";
    int ore = oreMinutiInSecondi / 3600;
    int minuti = (oreMinutiInSecondi % 3600) / 60;
    String strOre = "" + ore;
    String strMin = "" + minuti;
    if(ore > 0) {
      szTesto = strOre;
      if (ore == 1){
        szTesto += " ora";
      }
      else {
        szTesto += " ore";
      }
    }
    
    if(minuti > 0) {
      if(szTesto.length() > 0){
        szTesto += " ";
      }
      szTesto += strMin;
      if (minuti == 1){
        szTesto += " minuto";
      }
      else{
        szTesto += " minuti";
      }
    }
    return szTesto;
  }
  
  /**
   * Ritorna il totale dei secondi dato dalla parte HH:MM:SS della data passata non curandosi
   * della parte giorni, mesi, anno
   * 
   * @param data
   * @return
   */
  public static int convHHMMSSInSecondi(Date data) {
    if (data == null) {
      return 0;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(data);
    return cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
  }

  /**
   * Restituisce lo stato del flag 'strict' del modulo
   * @return
   */
  public static boolean hasStrictControl() {
    return mbStrict;
  }
  /**
   * Setta lo stato del flag 'strict' del modulo
   * @param bStrict
   */
  public static void setStrictControl(boolean bStrict) {
    mbStrict = bStrict;
  }
  
	/**
	 * Data un'ora in formato hh:mm oppure hh:mm:ss, torna i minuti corrispondenti.
	 * Il separatore deve essere il carattere ":". I secondi, se presenti, sono ignorati.
	 * <br/>
	 * Se l'ora è null o stringa vuota, torna 0.
	 * Esempi:<br/>
	 *  <tt>parseHHMM("9:40")</tt> ritorna <b>580</b>
	 * <br/>
	 *  <tt>parseHHMM("15:30:27")</tt> ritorna <b>930</b>
	 * <br/>
	 * A differenza di convStringaInMinuti, non ammette l'assenza del separatore ":".
	 * 
	 * @param s ora in formato testuale
	 * @return minuti corrispondenti all'ora passata
	 * @throws CISException nel caso l'ora sia in un formato non riconosciuto
	 * @see #convHHMM(int)
	 * @see #convHHMMEsteso(int)
	 * @see #convHHMMSSInSecondi(Date)
	 * @see #convMinutiInOra(int)
	 * @see #convMinutiInQtaOre(int)
	 * @see #convOraInMinuti(Date)
	 * @see #convStringaInMinuti(String)
	 * 
	 */
	public static int convHHMMInMinuti(String s) throws IllegalArgumentException {
		if (s == null || s.length()==0) {
			return 0;
		}
		int i = s.indexOf(':');
		if (i <= 0) {
			throw new IllegalArgumentException(s + ": formato ora non risconosciuto");
		}
		int i2 = s.indexOf(':', i+1);
		if (i2 <= 0) {
			i2 = s.length();
		}
		int ore = Text.toInt(s.substring(0, i), -1);
		int min = Text.toInt(s.substring(i + 1, i2), -1);
		if (ore < 0 || min < 0) {
			throw new IllegalArgumentException(s + ": formato ora non risconosciuto");
		}
		if (ore > 23 || min > 59) {
			throw new IllegalArgumentException(s + ": ore o minuti errati");
		}
		
		return ore*60 + min;		
	}

}
