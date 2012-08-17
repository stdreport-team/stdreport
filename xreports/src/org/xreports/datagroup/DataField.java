/**
 * 
 */
package org.xreports.datagroup;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Date;

import org.xreports.datagroup.DataFieldModel.TipoCampo;
import org.xreports.util.FileUtil;

/**
 * Questa classe rappresenta un dato reale presente in un {@link Group}. Un
 * DataField ha un riferimento ad un suo {@link DataFieldModel}, il quale
 * mantiene le informazioni 'statiche' del campo, quali il nome, il tipo, la
 * formattazione desiderata,...
 * <p>
 * Un DataField mantiene invece il valore reale di un campo, quindi
 * sostanzialmente il suo valore (vedi {@link #getValue()}).
 * </p>
 * <p>
 * Un campo è empty ({@link #isEmpty()}) se nessun valore gli è mai stato
 * impostato, oppure null ({@link #isNull()}) se il suo valore è null.
 * 
 * @author pier
 * 
 */
public class DataField implements Serializable {
  private static final long         serialVersionUID = -7392028373077204874L;
  private Object                    m_value          = null;
  private boolean                   m_isEmpty        = true;
  private boolean                   m_inError        = false;

  public static transient int s_fieldCount     = 0;

  /** il modello di questo campo */
  private transient DataFieldModel  m_model;

  /** identificativo univoco del mio modello di gruppo */
  private Integer                   m_modelId;

  /** gruppo a cui appartiene questo campo */
  private Group                     m_group;

  public DataField(DataFieldModel myModel) {
    if (myModel == null) {
      throw new NullPointerException("DataFieldModel can't be null");
    }
    m_model = myModel;
  }

  public String getNome() {
    return m_model.getNome();
  }

  public String getNomeEsteso() {
    return m_model.getNomeEsteso();
  }

  public boolean isNome(String nome) {
    return getNome().equalsIgnoreCase(nome);
  }

  public boolean isInError() {
    return m_inError;
  }

  public void resetErrorState() {
    m_inError = false;
  }

  public void setErrorState(String errorMessage) {
    m_inError = true;
    if (errorMessage == null) {
      errorMessage = "";
    }
  }

  /**
   * Ritorna true se il valore del campo è null. <br>
   * NB: null non è la stessa cosa che empty, se si assegna ad un campo null con
   * setValue(null), isEmpty() risulta false.
   * 
   * @return true se il valore del campo è null, false altrimenti
   */
  public boolean isNull() {
    return m_value == null;
  }

  /**
   * Ritorna true se il valore del campo è null o empty.
   * 
   * @return true se il valore del campo è null o empty, false altrimenti
   * @see isEmpty()
   * @see isNull()
   */
  public boolean isEmptyOrNull() {
    return isEmpty() || isNull();
  }

  /**
   * Ritorna true se il valore del campo è null o empty oppure se è numerico e
   * vale 0 oppure se stringa ed è una stringa vuota o composta solo di spazi.
   * 
   * @return
   * @see isEmpty()
   * @see isNull()
   */
  public boolean isEmptyNullOrZero() {
    if (isEmpty() || isNull()) {
      return true;
    }
    if (m_model.isNumeric()) {
      if (getAsDouble() == 0.0) {
        return true;
      }
      return false;
    } else if (m_model.getTipo() == TipoCampo.CHAR) {
      return getValue().toString().trim().length() == 0;
    }
    return false;
  }

  /**
   * Confronta il valore corrente di questo campo con quello passato e ritorna
   * true se sono uguali. Per i campi numerici value deve essere un'istanza di
   * java.lang.Number . Per i campi stringa il confronto viene fatto in maniera
   * case-insensitive.
   * 
   * @param value
   *          valore da confrontare
   * @return true se sono uguali i valori
   */
  public boolean isEqualValue(Object value) {
    if (value != null && isNull()) {
      return false;
    }
    if (value == null && !isNull()) {
      return false;
    }
    if (value == null && isNull()) {
      return true;
    }
    if (m_model.isNumeric() && value instanceof Number) {
      if (m_model.getTipo().equals(TipoCampo.LONG)) {
        return ((Number) m_value).longValue() == ((Number) value).longValue();
      }
      if (m_model.getTipo().equals(TipoCampo.DOUBLE) || m_model.getTipo().equals(TipoCampo.INTEGER)) {
        return ((Number) m_value).doubleValue() == ((Number) value).doubleValue();
      }
      if (m_model.getTipo().equals(TipoCampo.BIGDECIMAL)) {
        return ((BigDecimal)m_value).compareTo(new BigDecimal(((Number) value).toString())) == 0;
      }
    } else if (m_model.isString()) {
      return m_value.toString().equalsIgnoreCase(value.toString());
    } else if (m_model.isDate()) {
      // FIXME usare compare
      return m_value.toString().equalsIgnoreCase(value.toString());
    } else if (m_model.isBoolean()) {
      if (value instanceof Boolean) {
        return ((Boolean) value).booleanValue() == getAsBoolean();
      }
      return false;
    }
    throw new IllegalStateException("Stato del campo " + m_model.getNome() + " incoerente!!");
  }

  /**
   * Azzera il valore corrente e riporta il campo allo stato di empty.
   * 
   */
  public void resetValue() {
    m_value = null;
    m_isEmpty = true;
  }

  /**
   * Ritorna true se il valore del campo è empty, cioè <b>nessun valore
   * assegnato</b>. <br>
   * NB: null non è la stessa cosa che empty, se si assegna ad un campo null con
   * setValue(null), isEmpty() risulta false.
   * <p>
   * Per riportare un campo allo stato di empty, si può usare reset().
   * 
   * @return true se il valore del campo è empty, false altrimenti
   */
  public boolean isEmpty() {
    return m_isEmpty;
  }

  /**
   * Ritorna il valore del campo come oggetto usato internamente per mantenere
   * il valore. Nel caso il campo sia null o empty, viene ritornato <b>null</b>.
   * 
   * @return valore come oggetto
   * @see #isEmpty()
   * @see #isNull()
   */
  public Object getValue() {
    return m_value;
  }

  /**
   * Genera NullPointerException se il campo è null.
   * 
   * @return valore del campo come long
   */

  /**
   * Restituisce il valore del campo come un long.
   * 
   * @return valore del campo come long
   * @throws IllegalStateException
   *           se il campo è empty
   * @throws NullPointerException
   *           se il campo è null
   */
  public long getAsLong() throws IllegalStateException, NullPointerException {
    checkState();
    return ((Number) m_value).longValue();
  }

  public boolean getAsBoolean() throws IllegalStateException, NullPointerException {
    checkState();
    return ((Boolean) m_value).booleanValue();
  }

  public boolean getAsBooleanSafe() throws IllegalStateException, NullPointerException {
    try {
      return getAsBoolean();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Come {@link #getAsLong()}, ma ritorna 0 invece di generare Exception nel
   * caso di campo null o empty
   * 
   * @return valore del campo come long oppure 0 nel caso null o empty
   */
  public long getAsLongSafe() {
    try {
      return getAsInt();
    } catch (Exception e) {
      return 0;
    }
  }

  private void checkState() throws IllegalStateException, NullPointerException {
    if (isEmpty())
      throw new IllegalStateException("Il campo '" + m_model.getNome() + "' è empty");
    if (isNull())
      throw new IllegalStateException("Il campo '" + m_model.getNome() + "' è null");

  }

  /**
   * Restituisce il valore del campo come un int.
   * 
   * @return valore del campo come int
   * @throws IllegalStateException
   *           se il campo è empty
   * @throws NullPointerException
   *           se il campo è null
   */
  public int getAsInt() throws IllegalStateException, NullPointerException {
    checkState();
    return ((Number) m_value).intValue();
  }

  /**
   * Come {@link #getAsInt()}, ma ritorna 0 invece di generare Exception nel
   * caso di campo null o empty
   * 
   * @return valore del campo come int oppure 0 nel caso null o empty
   */
  public int getAsIntSafe() {
    try {
      return getAsInt();
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Restituisce il valore del campo come un double.
   * 
   * @return valore del campo come double
   * @throws IllegalStateException
   *           se il campo è empty
   * @throws NullPointerException
   *           se il campo è null
   */
  public double getAsDouble() throws IllegalStateException, NullPointerException {
    checkState();
    return ((Number) m_value).doubleValue();
  }

  /**
   * Come {@link #getAsDouble()}, ma ritorna 0.0 invece di generare Exception
   * nel caso di campo null o empty
   * 
   * @return valore del campo come double oppure 0.0 nel caso null o empty
   */
  public double getAsDoubleSafe() {
    try {
      return getAsDouble();
    } catch (Exception e) {
      return 0.0;
    }
  }

  /**
   * Restituisce il valore del campo come una stringa.
   * 
   * @return valore del campo come stringa
   * @throws IllegalStateException
   *           se il campo è empty
   * @throws NullPointerException
   *           se il campo è null
   */

  public String getAsString() throws IllegalStateException, NullPointerException {
    checkState();
    return String.valueOf(m_value);
  }

  /**
   * Come {@link #getAsString()}, ma ritorna stringa vuota invece di generare
   * Exception nel caso di campo null o empty
   * 
   * @return valore del campo come stringa oppure stringa vuota nel caso null o
   *         empty
   */
  public String getAsStringSafe() {
    try {
      return getAsString();
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    DataField clone = (DataField) super.clone();
    // esplicitamente imposto a null il valore: la clone non copia il valore
    // ma solo gli attributi del campo
    clone.resetValue();
    return clone;
  }

  /**
   * Distrugge questo oggetto eliminando tutti i riferimenti a oggetti esterni e
   * azzerando le strutture dati interne.
   */
  public void destroy() {
    m_value = null;
    m_model = null;
  }

  /**
   * Aggiunge il valore passato al valore corrente del campo. Sia questo campo
   * che il valore passato devono essere numerici, pena una Exception.
   * 
   * @param v
   *          valore da aggiungere, deve essere un tipo numerico, cioè una
   *          subclasse {@link java.lang.Number} .
   */
  public void addValue(Object v) {
    if ( !m_model.isNumeric())
      throw new IllegalArgumentException("Campo non numerico, addValue vietata.");
    if (m_model.getTipo() == TipoCampo.LONG) {
      if (v instanceof Number || v instanceof java.math.BigInteger || v instanceof java.math.BigDecimal) {
        setValue(new Long( ((Number) v).longValue() + getAsLong()));
      } else if (v == null) {
        //se null lo considero 0 quindi non lo sommo
      } else {
        String errDesc = " Campo: " + m_model.getNome() + ", valore=";
        errDesc += v.toString();
        throw new IllegalArgumentException("Valore non numerico, addValue vietata." + errDesc);
      }
    }
    if (m_model.getTipo() == TipoCampo.DOUBLE || m_model.getTipo() == TipoCampo.INTEGER) {
      if (v instanceof Number || v instanceof java.math.BigInteger || v instanceof java.math.BigDecimal) {
        setValue(new Double( ((Number) v).doubleValue() + getAsDouble()));
      } else if (v == null) {
        //se null lo considero 0 quindi non lo sommo
      } else {
        String errDesc = " Campo: " + m_model.getNome() + ", valore=";
        errDesc += v.toString();
        throw new IllegalArgumentException("Valore non numerico, addValue vietata." + errDesc);
      }
    }
  }

  /**
   * Assegna il valore v al campo. Se il campo non è stato definito con un tipo
   * preciso, il tipo viene dedotto dal valore passato e il campo diventa del
   * tipo dedotto. <br/>
   * Se il campo ha un tipo assegnato, il valore deve essere di tale tipo. Se il
   * tipo non viene riconosciuto, viene generata una IllegalArgumentException. <br/>
   * In seguito a un assegnamento con successo, isEmpty() ritorna false e
   * isCalcolato() ritorna true.
   * 
   * @param v
   *          valore da assegnare
   */
  public void setValue(Object v) {
    if (m_model.getTipo() == TipoCampo.UNKNOWN) {
      // se tipo non ancora assegnato, lo deduco dalla classe dell'oggetto passato
      if (v instanceof Number) {
        if (v instanceof Double || v instanceof Float) {
          m_model.setTipo(TipoCampo.DOUBLE);
          m_model.setCifreDecMin(DataFieldModel.CIFREDEC_MIN_DOUBLE_DEFAULT);
          m_model.setCifreDecMax(DataFieldModel.CIFREDEC_MAX_DOUBLE_DEFAULT);
        }
        else if (v instanceof BigDecimal) {
          m_model.setTipo(TipoCampo.BIGDECIMAL);
          m_model.setCifreDecMin(DataFieldModel.CIFREDEC_MIN_DOUBLE_DEFAULT);
          m_model.setCifreDecMax(DataFieldModel.CIFREDEC_MAX_DOUBLE_DEFAULT);
          //converto comunque il valore ad un bigdecimal
          if (v != null) {
            v = new BigDecimal(v.toString());            
          }
        }
        else if (v instanceof Integer || v instanceof Byte || v instanceof Short) {
          m_model.setTipo(TipoCampo.INTEGER);
          m_model.setCifreDecMin(0);
          m_model.setCifreDecMax(0);
        }
        else if (v instanceof Long) {
          m_model.setTipo(TipoCampo.LONG);
          m_model.setCifreDecMin(0);
          m_model.setCifreDecMax(0);
        }
      } else if (v instanceof String) {
        m_model.setTipo(TipoCampo.CHAR);
      } else if (v instanceof Clob) {
        m_model.setTipo(TipoCampo.CHAR);
        try {
          try {
            v = FileUtil.toString( ((Clob) v).getCharacterStream());
          } catch (SQLFeatureNotSupportedException e) {
            v = FileUtil.toString( ((Clob) v).getAsciiStream());
          }
        } catch (Exception e) {
          throw new IllegalArgumentException("non riesco a leggere il valore del campo al campo " + getNome());
        }
      } else if (v instanceof Date) {
        m_model.setTipo(TipoCampo.DATE);
      } else if (v instanceof Boolean) {
        m_model.setTipo(TipoCampo.BOOLEAN);
      }
    }
    if (m_model.getTipo() == TipoCampo.UNKNOWN) {
      if (v != null) {
        //il tipo passato non lo tratto...
        throw new IllegalArgumentException("setValue: il valore assegnato al campo " + m_model.getNome()
            + " e' di una classe non riconosciuta: " + v.getClass().getName());
      }
    }
    m_value = v;
    m_isEmpty = false;
    setCalcolato(true);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getModel().getNome() + "=");
    if (isAuto()) {
      sb.append(" *auto*");    	
    }
    sb.append("=");    	
    if (isCalcolato()) {
      if (isNull()) {
        sb.append("*NULL*");
      } else {
        sb.append(getValue().toString());
      }
    } else {
      sb.append("*non ancora calcolato*");
    }
    return sb.toString();
  }

  /**
   * Formatta il valore corrente di questo campo.
   * <ul>
   * <li><b>campi numerici</b>: La formattazione dei campi numerici viene fatta
   * in base alle informazioni passate in {@link #setCifreDecimali(int, int)}.
   * Nel caso non sia stata passata alcuna informazione si usa il default (
   * {@link #CIFREDEC_MIN_DOUBLE_DEFAULT} , {@link #CIFREDEC_MAX_DOUBLE_DEFAULT}, {@link #CIFREDEC_MIN_PERCENT_DEFAULT} ,
   * {@link #CIFREDEC_MAX_PERCENT_DEFAULT}).</li>
   * <li><b>campi stringa</b>: nessuna formattazione, il campo viene preso così
   * com'è</li>
   * <li><b>campi data/ora</b>: per adesso si usa una formattazione di default</li>
   * </ul>
   * 
   * @return valore formattato
   */
  public String format() {
    m_model.buildFormatter();
    if (m_value == null) {
      return "";
    }
    if (m_model.getTipo().equals(TipoCampo.CHAR)) {
      // FIXME    CDATA ???????????????????????????
      return "<![CDATA[" + m_value.toString() + "]]>";
    } else if (m_model.getTipo().equals(TipoCampo.LONG)) {
      long valLong;
      if (m_value instanceof Number) {
        valLong = ((Number) m_value).longValue();
      } else {
        valLong = Long.parseLong(m_value.toString());
      }
      return m_model.getNumberFormatter().format(valLong);
    } else if (m_model.getTipo().equals(TipoCampo.DOUBLE) || m_model.getTipo().equals(TipoCampo.INTEGER)) {
      double valDbl;
      if (m_value instanceof Number) {
        valDbl = ((Number) m_value).doubleValue();
      } else {
        valDbl = Double.parseDouble(m_value.toString());
      }
      return m_model.getNumberFormatter().format(valDbl);
    } else if (m_model.isDate()) {
      return m_model.getDateFormatter().format(m_value);
    } else {
      throw new IllegalStateException("Il campo '" + m_model.getNome() + "' non ha un tipo valido: formattazione non possibile.");
    }
  }

  //  public String toXML() {
  //    StringBuilder sb = new StringBuilder();
  //    if (m_model.getAttributi() != null) {
  //      for (Attributo a : m_model.getAttributi().values()) {
  //        sb.append(a.toXML() + " ");
  //      }
  //    }
  //    return RootGroup.getXMLElem(m_model.getNome(), sb.toString().trim(), format());
  //  }

  public DataFieldModel getModel() {
    if (m_model == null) {
      m_model = getGroup().getModel().getFieldModelFromCache(m_modelId);
    }
    return m_model;
  }

  public Group getGroup() {
    return m_group;
  }

  void setGroup(Group group) {
    m_group = group;
  }

  public boolean isAuto() {
    return false;
  }

  /**
   * Funzione no-op che viene estesa da CampoAuto. Calcola il valore del campo
   */
  public void calcola() {

  }

  public boolean isString() {
    return m_model.isString();
  }

  public boolean isNumeric() {
    return m_model.isNumeric();
  }

  public boolean isDate() {
    return m_model.isDate();
  }

  public boolean isBoolean() {
    return m_model.isBoolean();
  }

  public TipoCampo getTipo() {
    return m_model.getTipo();
  }

  public boolean isAddValue() {
    return m_model.isAddValue();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    s_fieldCount++;
    out.defaultWriteObject();
  }

  /**
   * Indica se il valore di questo campo è calcolato, cioè
   * è presente e valido. Per i normali campi (DataField) è sempre true, se invece il campo è un
   * istanza di {@link DataFieldAuto} ritorna true solo se il metodo {@link DataFieldAuto#calcola()}
   * è stato chiamato.
   * 
   * @return true sse il valore di questo campo è già stato calcolato
   */
  public boolean isCalcolato() {
    return true;
  }
  
  /**
   * Forza lo stato di calcolato al campo.
   * 
   */
  public void setCalcolato(boolean bCalcolato) {
    //ridefinito da campoauto
  }

  
}
