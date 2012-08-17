/**
 * 
 */
package org.xreports.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.xreports.datagroup.DataFieldModel.TipoCampo;
import org.xreports.engine.DataException;
import org.xreports.engine.XReport;
import org.xreports.util.Text;

/**
 * 
 * Classe utilizzata per leggere files a campi delimitati e tornare i dati
 * letti in forma compatibile con il sistema di report.
 * @author pier
 *
 */
public class TabbedFileReader {
  private String m_fileName;
  private XReport m_stampa;

  
  /**
   * Costruttore con nome file
   * @param stp oggetto stampa, utilizzato per vari servizi
   * @param fileName nome file, con path assoluto o relativo (vedi {@link XReport#setDataInputFile(String)}).
   */
  public TabbedFileReader(XReport stp, String fileName) {
    m_fileName = fileName;
    m_stampa = stp;
  }
  
  /**
   * @return file impostato nel costruttore
   */
  public String getFileName() {
    return m_fileName;
  }

  /**
   * Apre il file e ne ritorna il contenuto in forma compatibile col sistema di report.
   * @return lista di mappe nome->valore di tutte le righe del file. La prima riga deve essere obbligatoriamente
   * una riga con i nomi dei campi.
   * @throws DataException in caso di errori in lettura/apertura file o nel formato dei dati
   */
  public List<HashMap<String, Object>> load() throws DataException {
    BufferedReader br = null;
    try {
      List<HashMap<String, Object>> rows = new LinkedList<HashMap<String, Object>>();
      
      br = new BufferedReader(new FileReader(m_fileName));
      String header = br.readLine();      
      
      List<Field> fields = parseHeader(header);
      String line = null;
      int fieldsCount = fields.size();
      if (fieldsCount==0) {
        throw new DataException("Il file di dati è vuoto: " + m_fileName);         
      }

      while ( (line = br.readLine()) != null) {
        HashMap<String, Object> riga = parseDataLine(line, fields);
        if (riga != null)
          rows.add(riga);
      }      
      
      return rows;
    } catch (FileNotFoundException e) {
      throw new DataException("File di dati non trovato: " + m_fileName); 
    } catch (IOException e) {
      throw new DataException("Errore in lettura dati da file : " + m_fileName + ", " + e.toString()); 
    }
    finally {
      try { br.close(); }
      catch (Exception e) {}
    }
  }
  

  private HashMap<String, Object> parseDataLine(String line, List<Field> fields) {
    if (fields.size() > 1) {
      //se ci sono più di 1 campo, le righe vuote o composte solo di spazi le ignoro
      if (line.trim().length()==0)
        return null;
    }
    Scanner s = new Scanner(line);
    s.useDelimiter("\t");

    HashMap<String, Object> map = new HashMap<String, Object>();
    for (Field field : fields) {
      Object objVal = getValue(s.next(), field);
      map.put(field.getName(), objVal);
    }
    return map;
  }

  private Object getValue(String s, Field f) {
    Object value;
    if (f.getTipo() == TipoCampo.UNKNOWN) {
      value = parseValue(s, f);
    } else {
      value = convertValue(s, f);
    }
    return value;
  }

  private Object convertValue(String s, Field f) {
    if (s == null) {
      return null;
    }
    if (s.length() == 0) {
      if (f.isNumeric() || f.isDate())
        return null;
      else
        return "";
    }
    try {
      switch (f.getTipo()) {
        case BOOLEAN:
          if (s.length() > 0) {
            Integer i = Text.toInteger(s);
            if (i!=null) {
              //numero: false <--> 0
              return Boolean.valueOf(i.intValue() != 0);
            }
            else {
              if (s.equalsIgnoreCase("false"))
                return Boolean.FALSE;
              else if (s.equalsIgnoreCase("true"))
                return Boolean.TRUE;
              
              //se la stringa non è nè "false" nè "true", true <--> s!= ""
              return Boolean.TRUE;
            }              
          }
          //arrivo qui se s=""
          return Boolean.FALSE;
        case CHAR:
          return s;
        case LONG:
          return Long.valueOf(s);
        case DOUBLE:
          return Double.valueOf(s);
        case BIGDECIMAL:
          return new BigDecimal(s.toString());
        case DATE:
          SimpleDateFormat sdf = new SimpleDateFormat(f.getFormat());
          return sdf.parse(s);
        default:
          throw new IllegalArgumentException("Non riesco a trovare il tipo per il campo " + f.getName());
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Non riesco a convertire il campo " + f.getName());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Non riesco a convertire il campo " + f.getName());
    }
  }

  /**
   * Metodo chiamato quando per il campo indicato non è ancora stato dedotto il tipo di dato.
   * Guarda la il valore e cerca di convertirlo in un tipo opportuno: se tutto fallisce,
   * si desume che il tipo sia stringa.
   * <br/>
   * NB: se il campo è vuoto, ritorna null e non desume alcun tipo.
   * 
   * @param s valore del campo in formato stringa, come presente nel file
   * @param f oggetto Field associato al campo: qui memorizzo il tipo di dato dedotto, in modo
   *  che non debba più farlo per le successive righe.
   * @return valore convertito nel tipo corretto
   */
  private Object parseValue(String s, Field f) {
    if (s == null || s.length() == 0) {
      return null;
    }

    try {
      Integer value = Integer.valueOf(s);
      f.setTipo(TipoCampo.LONG);
      return value;
    } catch (NumberFormatException e) {
    }
    try {
      Long value = Long.valueOf(s);
      f.setTipo(TipoCampo.LONG);
      return value;
    } catch (NumberFormatException e) {
    }
    try {
      Double value = Double.valueOf(s);
      f.setTipo(TipoCampo.DOUBLE);
      return value;
    } catch (NumberFormatException e) {
    }

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      Date value = sdf.parse(s);
      f.setTipo(TipoCampo.DATE);
      return value;
    } catch (ParseException e) {
    }

    f.setTipo(TipoCampo.CHAR);
    return s;
  }  
  
  
  /**
   * Analizza la prima riga del file per trovare i nomi dei campi
   * @param header prima riga del file
   * @return lista di campi (solo i nomi, il tipo verrà valutato in lettura delle righe)
   * @throws DataException  in caso di formato riga errato
   */
  private List<Field> parseHeader(String header) throws DataException {
    Scanner s = new Scanner(header);
    s.useDelimiter("\t");
    List<Field> fl = new LinkedList<Field>();
    while (s.hasNext()) {
      String name = s.next().trim();
      Field f = null;
      if (name.endsWith("]")) {
        int i = name.indexOf('[');
        if (i > 0) {
          f = parseTypeSpec(name.substring(0, i).trim(),  name.substring(i+1, name.length() - 1).trim());
          name = name.substring(0, i).trim();
        }
        else {
          throw new DataException("Il campo " + name + " ha il nome con formato errato: manca '['");
        }
      }
      if (f==null)
         f = new Field(name);
      if (m_stampa.isLegalName(f.getName()))
        fl.add(f);
      else {
        throw new DataException("Il campo " + name + " ha caratteri non validi nel nome");
      }
    }

    return fl;
  }
  
  
  private static final String TYPE_CHAR1 = "c";
  private static final String TYPE_CHAR2 = "s";
  private static final String TYPE_CHAR3 = "char";
  private static final String TYPE_CHAR4 = "string";

  private static final String TYPE_INT1 = "i";
  private static final String TYPE_INT2 = "int";
  private static final String TYPE_INT3 = "integer";

  private static final String TYPE_LONG1 = "l";
  private static final String TYPE_LONG2 = "long";

  private static final String TYPE_DOUBLE1 = "d";
  private static final String TYPE_DOUBLE2 = "double";

  private static final String TYPE_BIGDEC1 = "bd";
  private static final String TYPE_BIGDEC2 = "bigdecimal";
  
  private static final String TYPE_DATE1 = "dt";
  private static final String TYPE_DATE2 = "date";

  private static final String TYPE_BOOL1 = "b";
  private static final String TYPE_BOOL2 = "bool";
  private static final String TYPE_BOOL3 = "boolean";
  
  private Field parseTypeSpec(String name, String typeSpec) throws DataException {
    TipoCampo tipo = null;
    Field f = null;
    String format = null;
    typeSpec = typeSpec.toLowerCase(); 
    if (Text.isOneOf(typeSpec, TYPE_CHAR1, TYPE_CHAR2, TYPE_CHAR3, TYPE_CHAR4)) {
      tipo = TipoCampo.CHAR;
    }
    else if (Text.isOneOf(typeSpec, TYPE_INT1, TYPE_INT2, TYPE_INT3)) {
      tipo = TipoCampo.INTEGER;
    }
    else if (Text.isOneOf(typeSpec, TYPE_LONG1, TYPE_LONG2)) {
      tipo = TipoCampo.LONG;
    }
    else if (Text.isOneOf(typeSpec, TYPE_DOUBLE1, TYPE_DOUBLE2)) {
      tipo = TipoCampo.DOUBLE;
    }
    else if (Text.isOneOf(typeSpec, TYPE_BIGDEC1, TYPE_BIGDEC2)) {
      tipo = TipoCampo.BIGDECIMAL;
    }
    else if (Text.isOneOf(typeSpec, TYPE_BOOL1, TYPE_BOOL2, TYPE_BOOL3 )) {
      tipo = TipoCampo.BOOLEAN;
    }
    else if (Text.isOneOf(typeSpec, TYPE_DATE1, TYPE_DATE2)) {
      tipo = TipoCampo.DATE;
    }
    else if (typeSpec.startsWith(TYPE_DATE1)) {
      format = parseDateFormat(name, TYPE_DATE1, typeSpec);
      tipo = TipoCampo.DATE;
    }
    else if (typeSpec.startsWith(TYPE_DATE2)) {
      format = parseDateFormat(name, TYPE_DATE2, typeSpec);
      tipo = TipoCampo.DATE;
    }

    if (tipo != null) {
      f = new Field(name);
      f.setTipo(tipo);
      if (format != null) {
        f.setFormat(format);
      }
    }
    return f;
  }  
  
  /**
   * Analizza la parte tipo di un header di tipo data.
   * Se ho ad esempio un campo data definito così: <tt>dtnasc [dt, dd/MM/yyyy]</tt>,
   * a questo metodo viene passata la stringa <tt>dt, dd/MM/yyyy</tt>.
   *  
   * @param name nome del campo (nell'esempio sopra <var>dtnasc</var>)
   * @param dateType tipo specificato (nell'esempio sopra <var>dt</var>) 
   * @param typeSpec intera stringa del formato (nell'esempio sopra <var>dt, dd/MM/yyyy</var>)
   * @return la stringa corretta del formato (nell'esempio sopra <var>dd/MM/yyyy</var>)
   * @throws DataException nel caso il formato non sia della sintassi prevista o la stringa di formattazione sia non valida
   */
  private String parseDateFormat(String name, String dateType, String typeSpec) throws DataException {
    String format = typeSpec.substring(dateType.length()).trim();
    if (!format.startsWith(",")) {
      throw new DataException("Il campo " + name + " ha la specifica del formato errata");
    }    
    String fmt = format.substring(1).trim();
    try {
      //istanzio SimpleDateFormat solo per verificare che la stringa di formattazione sia corretta
      @SuppressWarnings("unused")
      SimpleDateFormat sdf = new SimpleDateFormat(fmt);
    } catch (Exception e) {
      throw new DataException("Il campo " + name + " ha un formato data non valido: " + fmt);
    }
    return fmt;
  }
  
  public class Field {
    String    m_name;
    TipoCampo m_tipo = TipoCampo.UNKNOWN;
    String    m_format;

    public Field(String name) {
      m_name = name;
    }

    public String getName() {
      return m_name;
    }

    public TipoCampo getTipo() {
      return m_tipo;
    }

    public void setTipo(TipoCampo tipo) {
      m_tipo = tipo;
    }

    public boolean isNumeric() {
      return m_tipo == TipoCampo.LONG || m_tipo == TipoCampo.DOUBLE || m_tipo == TipoCampo.INTEGER;
    }

    public boolean isDate() {
      return m_tipo == TipoCampo.DATE;
    }
    
    public String getFormat() {
      return m_format;
    }

    public void setFormat(String format) {
      m_format = format;
    }
    
  }  
}
