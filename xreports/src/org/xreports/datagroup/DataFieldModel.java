package org.xreports.datagroup;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataFieldModel implements Cloneable, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 4243634260324805016L;
  
  private NumberFormat           m_fmt;
  private SimpleDateFormat       m_sdf;
  private String                 m_name                       = null;
  private TipoCampo              m_tipo                       = TipoCampo.UNKNOWN;
  private int                    m_cifreDecMin                = -1;
  private int                    m_cifreDecMax                = -1;
  private boolean                m_segnoPiu                   = false;
  /**
   * formattazione campo: quantità predefinita delle cifre decimali minime per
   * un valore double
   */
  public static final int        CIFREDEC_MIN_DOUBLE_DEFAULT  = 2;
  /**
   * formattazione campo: quantità predefinita delle cifre decimali massime per
   * un valore double
   */
  public static final int        CIFREDEC_MAX_DOUBLE_DEFAULT  = 2;
  /**
   * formattazione campo: quantità predefinita delle cifre decimali minime per
   * un valore percentuale
   */
  public static final int        CIFREDEC_MIN_PERCENT_DEFAULT = 1;
  /**
   * formattazione campo: quantità predefinita delle cifre decimali massime per
   * un valore percentuale
   */
  public static final int        CIFREDEC_MAX_PERCENT_DEFAULT = 1;
  private boolean                m_sepMigliaia                = false;
  private boolean                m_addValue                   = false;
//  private Map<String, Attributo> m_attrs;

  /** id univoco di questo modello nella cache mantenuta dal proprio {@link GroupModel} */
  private Integer                      m_modelId;
  
  
  public enum TipoCampo {
    UNKNOWN, LONG, DOUBLE, BIGDECIMAL, INTEGER, CHAR, DATE, BOOLEAN;
  }

  private GroupModel m_parent = null; //modello a cui appartiene questo campo

  public DataFieldModel(GroupModel parent, String nome) {
    if (parent == null) {
      throw new NullPointerException("parent GroupModel can't be null.");
    }
    m_parent = parent;
    setNome(nome);
  }

  public DataFieldModel(GroupModel parent, String nome, TipoCampo tipo) {
    if (parent == null) {
      throw new NullPointerException("parent GroupModel can't be null.");
    }
    m_parent = parent;
    setNome(nome);
    setTipo(tipo);
  }

  /**
   * Crea un'istanza di DataField che fa riferimento a questo Field model
   * @return istanza creata
   */
  DataField newFieldInstance() {
    DataField f = new DataField(this);
    return f;
  }
  
  protected void setNome(String nome) {
    if (nome == null || nome.length() == 0) {
      throw new IllegalArgumentException("Devi specificare un nome di campo.");
    }
    if ( !RootGroup.checkNome(nome)) {
      throw new IllegalArgumentException("Il nome '" + nome + "' specificato per il campo non e' valido.");
    }
    m_name = nome;
  }

  /**
   * Imposta il tipo di dato del campo.
   * 
   * @param tipo
   *          campo
   */
  public void setTipo(TipoCampo tipo) {
    m_tipo = tipo;
  }

  /**
   * Nome di questo campo. Il nome è sempre <b>case insensitive</b>
   * 
   * @return nome del campo
   */
  public String getNome() {
    return m_name;
  }

  /**
   * Nome esteso di questo campo, cioè il nome nella forma
   * 
   * <pre>
   * nomeGruppo.nomeCampo
   * </pre>
   * 
   * @return nome del campo
   */
  public String getNomeEsteso() {
    return m_parent.getName() + "." + m_name;
  }

  /**
   * Ritorna il path assoluto di questa istanza di questo campo. Il formato è
   * così:
   * 
   * <pre>
   *    gruppo1[id1].gruppo2[id2].gruppo3[id3]....gruppo<i>n</i>[id<i>n</i>]
   * </pre>
   * 
   * dove gruppo1, gruppo2,... sono i nomi dei gruppi della gerarchia che arriva
   * fino a questo gruppo e fra le parentesi [] c'è l'ID univoco dell'istanza
   * 
   * @return
   */
  public String getPath() {
    String path = getNome();
    if (getGroupModel() != null) {
      path = getGroupModel().getPath() + "." + path;
    }
    return path;
  }

  public TipoCampo getTipo() {
    return m_tipo;
  }

  public int getCifreDecimaliMin() {
    return m_cifreDecMin;
  }

  public int getCifreDecimaliMax() {
    return m_cifreDecMax;
  }

  /**
   * Imposta numero di cifre decimali minimo e massimo.
   * 
   * @param min
   *          minimo numero di cifre dec.
   * @param max
   *          massimo numero di cifre dec. (=0 --> nessuna cifra decimale)
   */
  public void setCifreDecimali(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException("Il numero di cifre decimali minimo non può superare il numero di cifre decimali massimo.");
    }
    m_cifreDecMin = min;
    m_cifreDecMax = max;
  }

  public void setSeparatoreMigliaia(boolean sep) {
    m_sepMigliaia = sep;
  }

  /**
   * Indica un campo stringa.
   * @return true se questo campo è di tipo stringa
   */
  public boolean isString() {
    return (m_tipo.equals(TipoCampo.CHAR));
  }

  public boolean isNumeric() {
    return (m_tipo.equals(TipoCampo.LONG)) 
        || (m_tipo.equals(TipoCampo.DOUBLE)) 
        || (m_tipo.equals(TipoCampo.BIGDECIMAL)) 
        || (m_tipo.equals(TipoCampo.INTEGER));
  }

  public boolean isDate() {
    return m_tipo.equals(TipoCampo.DATE);
  }

  public boolean isBoolean() {
    return m_tipo == TipoCampo.BOOLEAN;
  }


  /**
   * Indica se il segno più deve essere mostrato nel valore formattato.
   * Normalmente non viene mostrato.
   * 
   */
  public void setMostraSegnoPiu(boolean mostra) {
    m_segnoPiu = mostra;
  }

  /**
   * Ritorna il flag che indica se il segno più deve essere mostrato nel valore
   * formattato.
   * 
   * @return vedi sopra
   */
  public boolean getMostraSegnoPiu() {
    return m_segnoPiu;
  }

  
  //FIXME eliminare definitivamente il formatter? La formattazione è fatta dentro AbstractElement, qui serve a qualcuno ??? 
  protected void buildFormatter() {
    if (m_fmt != null)
      return;
    if (isNumeric()) {
      if (m_tipo.equals(TipoCampo.INTEGER)) {
        // FIXME che c'entra qui getPercentInstance ???
        
        //FIXME Locale.ITALY?????
        m_fmt = NumberFormat.getPercentInstance(Locale.ITALY);
        if (m_cifreDecMax == -1)
          m_cifreDecMax = CIFREDEC_MAX_PERCENT_DEFAULT;
        if (m_cifreDecMin == -1)
          m_cifreDecMin = CIFREDEC_MIN_PERCENT_DEFAULT;
      } else {
        m_fmt = NumberFormat.getNumberInstance(Locale.ITALY);
        if (m_tipo.equals(TipoCampo.DOUBLE) || m_tipo.equals(TipoCampo.BIGDECIMAL)) {
          if (m_cifreDecMax == -1)
            m_cifreDecMax = CIFREDEC_MAX_DOUBLE_DEFAULT;
          if (m_cifreDecMin == -1)
            m_cifreDecMin = CIFREDEC_MIN_DOUBLE_DEFAULT;
        } else {
          //è un numerico senza virgola
          m_cifreDecMax = 0;
          m_cifreDecMin = 0;
        }
      }
      m_fmt.setGroupingUsed(m_sepMigliaia);
      m_fmt.setMinimumFractionDigits(m_cifreDecMin);
      m_fmt.setMaximumFractionDigits(m_cifreDecMax);
      if (m_fmt instanceof DecimalFormat && getMostraSegnoPiu())
        ((DecimalFormat) m_fmt).setPositivePrefix("+");
    } else if (isDate()) {
      // FIXME serve a qualcosa SimpleDateFormat ????
      m_sdf = new SimpleDateFormat();
      m_sdf.applyPattern("dd/MM/yyyy");
    }
  }


  /**
   * Ritorna true se questo campo è automatico. E' assicurato che 
   * <pre>isAuto() == true</pre>
   * 
   * se e solo se
   * 
   * <pre>this.getClass() == DataFieldModelAuto.class</pre>
   * 
   * @return true se il campo è automatico
   */
  public boolean isAuto() {
    return false;
  }


  @Override
  public String toString() {
    String s = m_name + " (" + m_tipo + ")";
    if (isKey())
    	s += " key";
    if (isAuto())
    	s += " *auto*";
    return s;
  }

  /**
   * Indica che questo campo deve essere addizionato se fa parte di un gruppo in
   * cui la chiave appare + volte nei dati di input. Il valore di default è
   * false.
   * 
   * @return
   */
  public boolean isAddValue() {
    return m_addValue;
  }

  /**
   * Impostare a true se si vuole che questo campo sia addizionato se fa parte
   * di un gruppo in cui la chiave appare + volte nei dati di input. Può essere
   * addizionato solo se 1) il campo non è chiave 2) il campo è numerico
   * 
   * @param copy
   *          true se il campo deve essere addizionato, false (default) se
   *          sovrascritto
   */
  public void setAddValue(boolean copy) {
    if (m_tipo != TipoCampo.UNKNOWN) {
      if ( !isNumeric()) {
        throw new IllegalArgumentException("Può essere addizionato solo un campo numerico.");
      }
    }
    if (isKey()) {
      throw new IllegalArgumentException("Non può essere addizionato un campo chiave.");
    }
    m_addValue = copy;
  }

  /**
   * Restituisce true se questo campo è chiave nel gruppo di appartenenza.
   * 
   * @return true se chiave
   */
  public boolean isKey() {
    return m_parent.isKey(this);
  }

  /**
   * Usata internamente per assegnare un campo ad un gruppo.
   * 
   * @param g
   */
  void setGroupModel(GroupModel g) {
    m_parent = g;
  }

  /**
   * Restituisce il gruppo di appartenenza di questo campo.
   * 
   * @return
   */
  public GroupModel getGroupModel() {
    return m_parent;
  }

  /**
   * Restituisce il campo di nome 'nomeCampo' appartenente allo stesso gruppo di
   * questo campo.
   * 
   * @param nomeCampo
   * @return
   */
  public DataFieldModel getFratello(String nomeCampo) {
    try {
      return m_parent.getCampo(nomeCampo);
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Restituisce il campo di nome 'nomeCampo' appartenente al primo gruppo
   * antenato che ha questo campo.
   * 
   * @param nomeCampo
   * @return campo trovato oppure null se non trovato
   */
  public DataFieldModel getAntenato(String nomeCampo) {
    try {
      GroupModel gr = m_parent;
      DataFieldModel cc = gr.getCampo(nomeCampo);
      while (cc == null) {
        gr = gr.getParent();
        cc = gr.getCampo(nomeCampo);
      }
      return cc;
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Restituisce true se questo campo appartiene ad un gruppo con il nome
   * passato. Il nome <b>non</b> è case-sensitive.
   * 
   * @param gruppo
   *          nome del gruppo
   * @return true se questo campo appartiene ad un gruppo con il nome passato.
   */
  public boolean isGruppo(String gruppo) {
    return m_parent.getName().equalsIgnoreCase(gruppo);
  }


  /**
   * Restituisce true se questo campo appartiene ad un gruppo con il nome
   * 'gruppo' ed ha il nome 'nome'. I nomi passati <b>non</b> sono
   * case-sensitive.
   * 
   * @param nome
   *          nome del campo
   * @param gruppo
   *          nome del gruppo di appartenenza
   * @return true se questo campo appartiene ad un gruppo con il nome 'gruppo'
   *         ed ha il nome 'nome'.
   */
  public boolean isNomeEGruppo(String gruppo, String nome) {
    return m_parent.getName().equalsIgnoreCase(gruppo) && getNome().equalsIgnoreCase(nome);
  }

  /**
   * Restituisce true se questo campo ha il nome 'nome'. 'nome' <b>non</b> è
   * case-sensitive.
   * 
   * @param nome
   *          nome del campo
   * @return true se questo campo ha il nome 'nome'.
   */
  public boolean isNome(String nome) {
    return getNome().equalsIgnoreCase(nome);
  }

  public class Attributo {
    String    nome;
    String    valore;
    DataFieldModel campo;

    public Attributo(String pNome, String pValore) {
      if (pNome == null || pNome.trim().length() == 0) {
        throw new IllegalArgumentException("Il nome di un attributo non può essere null");
      }
      nome = pNome;
      valore = pValore;
    }

    public Attributo(String pNome) {
      if (pNome == null || pNome.trim().length() == 0) {
        throw new IllegalArgumentException("Il nome di un attributo non può essere null");
      }
      nome = pNome;
    }

    public DataFieldModel getCampo() {
      return campo;
    }

    public String toXML() {
      String szTmp = "";
      // Ragi - 08 jul 2008
      // eliminato warning e risistemato il codice
      //      if (valore!=null && valore.trim().length()>0)
      //         return nome + "=\"" + valore.trim().replace('"', '\'') + "\"";
      //      else
      //        return "";
      if (valore != null && valore.trim().length() > 0) {
        szTmp = nome + "=\"" + valore.trim().replace('"', '\'') + "\"";
      }
      return szTmp;
    }

  }

//  /**
//   * Restituisce l'attributo dato il nome, oppure null se non esiste.
//   * 
//   * @param nome
//   * @return
//   */
//  public Attributo getAttributo(String nome) {
//    if (m_attrs == null) {
//      return null;
//    }
//    if (m_attrs.containsKey(nome.toLowerCase())) {
//      return m_attrs.get(nome.toLowerCase());
//    }
//    return null;
//  }
//
//  private Attributo _removeAttributo(String nomeAttr) {
//    // Ragi - 08 jul 2008
//    // eliminato warning e risistemato il codice
//    //    if (m_attrs!=null)
//    //      return m_attrs.remove(nomeAttr.toLowerCase());
//    //    else
//    //      return null;
//    Attributo a = null;
//    if (m_attrs != null) {
//      a = m_attrs.remove(nomeAttr.toLowerCase());
//    }
//    return a;
//  }
//
//  private Attributo _addAttributo(Attributo a) {
//    if (getAttributo(a.nome) != null) {
//      throw new IllegalArgumentException("L'attributo '" + a.nome + "' esiste già nel campo " + getNome()
//          + ": non puoi aggiungerlo.");
//    }
//    a.campo = this;
//    if (m_attrs == null) {
//      m_attrs = new HashMap<String, Attributo>();
//    }
//    m_attrs.put(a.nome.toLowerCase(), a);
//    return a;
//  }
//
//  public Attributo addAttributo(String nomeAttr) {
//    Attributo a = new Attributo(nomeAttr);
//    return _addAttributo(a);
//  }
//
//  public Attributo addAttributo(String nomeAttr, String value) {
//    Attributo a = new Attributo(nomeAttr);
//    a.valore = value;
//    return _addAttributo(a);
//  }
//
//  /**
//   * Rimuove l'attributo specificato dal nome.
//   * 
//   * @param nomeAttr
//   *          nome attributo da rimuovere
//   * @return attributo rimosso se c'è, null se non c'è
//   */
//  public Attributo removeAttributo(String nomeAttr) {
//    return _removeAttributo(nomeAttr);
//  }
//
//  /**
//   * Se l'attributo nomeAttr c'è, gli assegna il valore, altrimenti lo crea e
//   * gli assegna il valore.
//   * 
//   * @param nomeAttr
//   * @param value
//   * @return
//   */
//  public Attributo setAttributo(String nomeAttr, String value) {
//    if (getAttributo(nomeAttr) != null) {
//      getAttributo(nomeAttr).valore = value;
//      return getAttributo(nomeAttr);
//    }
//    // Ragi - 08 jul 2008
//    // eliminato warning 
//    //    else
//    return addAttributo(nomeAttr, value);
//  }

  /**
   * Distrugge questo oggetto eliminando tutti i riferimenti a oggetti esterni e
   * azzerando le strutture dati interne.
   */
  public void destroy() {
    m_fmt = null;
    m_sdf = null;
//    if (m_attrs != null) {
//      m_attrs.clear();
//      m_attrs = null;
//    }
    m_parent = null;
  }

  public NumberFormat getNumberFormatter() {
    return m_fmt;
  }

  public SimpleDateFormat getDateFormatter() {
    return m_sdf;
  }
  
//  protected Map<String, Attributo> getAttributi() {
//    return m_attrs;
//  }
//  private void writeObject(ObjectOutputStream out) throws IOException {
//    //System.out.println(">>write " + this);
//    out.defaultWriteObject();             
//  }

  public int getCifreDecMin() {
    return m_cifreDecMin;
  }

  public int getCifreDecMax() {
    return m_cifreDecMax;
  }

  protected void setCifreDecMin(int cifreDecMin) {
    m_cifreDecMin = cifreDecMin;
  }

  protected void setCifreDecMax(int cifreDecMax) {
    m_cifreDecMax = cifreDecMax;
  }

  /**
   * Ritorna l'id univoco di questo modello nella cache mantenuta dal proprio {@link GroupModel}.
   * 
   * @return id del modello (non è mai null)
   */
  public Integer getModelId() {
    return m_modelId;
  }

  protected void setModelId(Integer modelId) {
    m_modelId = modelId;
  }
  
}
