package org.xreports.datagroup;

public class DataFieldAutoModel extends DataFieldModel {

  /**
   * 
   */
  private static final long serialVersionUID = 6603790252023041715L;

  public enum TipoOper {
    UNKNOWN, //
    CAMPOREF, //
    CONTEGGIODISCEN, //
    SOMMAFIGLI, //
    SOMMADISCEN, //
    PERC_PADRE, //
    DIFF_PERC_PADRE, //
    MEDIAFIGLI, //
    MATH_DIVIDE, //
    MATH_SUBTRACT, //
    MATH_MULTIPLY, //
    CALC_USER; //
  }

  private TipoOper         m_op             = TipoOper.UNKNOWN;
  private String           m_campoPadre     = "";
  private String           m_gruppoPadre    = "";
  private String           m_campoPadre2    = "";
  private String           m_gruppoPadre2   = "";
  private String           m_campo          = "";
  private Number           m_divisore       = null;
  private Number           m_sottrattore    = null;
  private Number           m_moltiplicatore = null;
  private UserCalcListener m_listener       = null;

  /**
   * Costruttore che crea un campo automatico e lo aggiunge al modello passato.
   * @param parent modello a cui aggiungere il campo
   * @param nome nome del campo
   * @param tipo tipo del campo
   * @throws GroupException se il campo esiste già in parent
   */
  public DataFieldAutoModel(GroupModel parent, String nome, TipoCampo tipo) throws GroupException {
    super(parent, nome, tipo);
    parent.addFieldAuto(this);
  }

  public void setUserCalc(UserCalcListener l) {
    m_op = TipoOper.CALC_USER;
    m_listener = l;
  }

  /**
   * Crea un'istanza di DataField che fa riferimento a questo Field model
   * @return istanza creata
   */
  DataFieldAuto newFieldInstance() {
    DataFieldAuto f = new DataFieldAuto(this);
    return f;
  }
  
  
  /**
   * Definisce questo campo come la media aritmetica del campo 'nomeCampo' di
   * tutti i gruppi figli di nome 'nomeGruppo'. <br>
   * Il gruppo 'nomeGruppo' deve essere figlio del gruppo a cui appartiene
   * questo campo.
   * 
   * @param nomeGruppo
   * @param nomeCampo
   */
  public void setMediaFigli(String nomeGruppo, String nomeCampo) {
    m_op = TipoOper.MEDIAFIGLI;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
  }

  public void setCampoRef(String nomeGruppo, String nomeCampo) {
    m_op = TipoOper.CAMPOREF;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
  }

  /**
   * Definisce questo campo come la quantità di nodi del gruppo 'nomeGruppo'
   * presenti come discendenti del gruppo a cui appartiene questo campo. <br>
   * Il gruppo 'nomeGruppo' deve essere figlio del gruppo a cui appartiene
   * questo campo.
   * 
   * @param nomeGruppo
   *          gruppo figlio di cui contare le occorrenze
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setConteggioDiscen(String nomeGruppo) {
    m_op = TipoOper.CONTEGGIODISCEN;
    m_gruppoPadre = nomeGruppo;
    return this;
  }

  /**
   * Definisce questo campo come la somma del campo 'nomeCampo' appartenente al
   * gruppo 'nomeGruppo' che è figlio del gruppo a cui appartiene questo campo. <br>
   * Il gruppo 'nomeGruppo' deve essere figlio del gruppo a cui appartiene
   * questo campo.
   * 
   * @param nomeGruppo
   *          gruppo a cui appartiene nomeCampo
   * @param nomeCampo
   *          nome (case-insensitive) del campo da sommare
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setSommaFigli(String nomeGruppo, String nomeCampo) {
    m_op = TipoOper.SOMMAFIGLI;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    return this;
  }

  /**
   * Definisce questo campo come la somma del campo 'nomeCampo' appartenente al
   * gruppo 'gruppo' che è figlio del gruppo a cui appartiene questo campo. <br>
   * Il gruppo 'gruppo' deve essere figlio del gruppo a cui appartiene questo
   * campo.
   * 
   * @param gruppo
   *          gruppo a cui appartiene nomeCampo
   * @param nomeCampo
   *          nome (case-insensitive) del campo da sommare
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setSommaFigli(Group gruppo, String nomeCampo) {
    return setSommaFigli(gruppo.getName(), nomeCampo);
  }

  /**
   * Definisce questo campo come la somma del campo 'nomeCampo' di tutti i
   * gruppi figli o discendenti di nome 'nomeGruppo'. <br>
   * Il gruppo 'nomeGruppo' deve essere discendente (figlio, nipote,...) del
   * gruppo a cui appartiene questo campo.
   * 
   * @param nomeGruppo
   * @param nomeCampo
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setSommaDiscen(String nomeGruppo, String nomeCampo) {
    m_op = TipoOper.SOMMADISCEN;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;

    return this;
  }

  /**
   * Definisce questo campo come la percentuale del campo 'nomeCampo' del gruppo
   * a cui appartiene questo campo, rispetto al campo 'nomeCampoPadre' del
   * gruppo antenato di nome 'nomeGruppoPadre', cioè definisce che il valore di
   * questo campo è calcolato così:
   * 
   * <pre>
   * this.value = (nomeCampo / nomeGruppoPadre.nomeCampoPadre)
   * </pre>
   * 
   * <br>
   * Il gruppo 'nomeGruppo' deve essere antenato del gruppo a cui appartiene
   * questo campo.
   * 
   * @param nomeGruppoPadre
   *          nome del gruppo antenato da cui prendere il campo nomeCampoPadre;
   *          se null, prende semplicemente il campo padre del gruppo a cui
   *          appartiene questo campo
   * @param nomeCampoPadre
   *          nome campo a cui riferirsi (denominatore) per calcolare la
   *          percentuale
   * @param nomeCampo
   *          nome campo del gruppo a cui appartiene questo campo a cui
   *          riferirsi (numeratore) per calcolare la percentuale
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setPercAntenato(String nomeGruppoPadre, String nomeCampoPadre, String nomeCampo) {
    m_op = TipoOper.PERC_PADRE;
    m_campoPadre = nomeCampoPadre;
    m_gruppoPadre = nomeGruppoPadre;
    m_campo = nomeCampo;

    return this;
  }

  /**
   * Definisce questo campo come la differenza, in percentuale, del campo
   * 'nomeCampo' del gruppo a cui appartiene questo campo, rispetto al campo
   * 'nomeCampoPadre' del gruppo antenato di nome 'nomeGruppoPadre', cioè
   * definisce che il valore di questo campo è calcolato così:
   * 
   * <pre>
   * this.value = ( (nomeCampo - nomeGruppoPadre.nomeCampoPadre) / nomeCampo)
   * </pre>
   * 
   * <br>
   * Il gruppo 'nomeGruppo' deve essere antenato del gruppo a cui appartiene
   * questo campo.
   * 
   * @param nomeGruppoPadre
   *          nome del gruppo antenato da cui prendere il campo nomeCampoPadre;
   *          se null, prende semplicemente il campo padre del gruppo a cui
   *          appartiene questo campo
   * @param nomeCampoPadre
   *          nome campo a cui riferirsi (denominatore) per calcolare la
   *          percentuale
   * @param nomeCampo
   *          nome campo del gruppo a cui appartiene questo campo a cui
   *          riferirsi (numeratore) per calcolare la percentuale
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setDiffPercAntenato(String nomeGruppoPadre, String nomeCampoPadre, String nomeCampo) {
    m_op = TipoOper.DIFF_PERC_PADRE;
    m_campoPadre = nomeCampoPadre;
    m_gruppoPadre = nomeGruppoPadre;
    m_campo = nomeCampo;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della divisione fra il campo di nome
   * 'nomeCampo' appartenente al gruppo 'nomeGruppo' e la costante 'divisore'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da dividendo
   * @param divisore
   *          costante che fa da divisore
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathDivide(String nomeGruppo, String nomeCampo, Number divisore) {
    m_op = TipoOper.MATH_DIVIDE;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_divisore = divisore;
    m_campoPadre2 = null;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della divisione fra il campo di nome
   * 'nomeCampo' appartenente al gruppo 'nomeGruppo' con il campo di nome
   * 'nomeCampo2' appartenente al gruppo 'nomeGruppo2'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da dividendo
   * @param nomeGruppo2
   *          gruppo di appartenenza del campo2. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo2
   *          nome del campo che fa da divisore
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathDivide(String nomeGruppo, String nomeCampo, String nomeGruppo2, String nomeCampo2) {
    m_op = TipoOper.MATH_DIVIDE;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_campoPadre2 = nomeCampo2;
    m_gruppoPadre2 = nomeGruppo2;
    m_divisore = null;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della sottrazione fra il campo di
   * nome 'nomeCampo' appartenente al gruppo 'nomeGruppo' con il campo di nome
   * 'nomeCampo2' appartenente al gruppo 'nomeGruppo2'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da primo termine della sottrazione
   * @param nomeGruppo2
   *          gruppo di appartenenza del campo2. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo2
   *          nome del campo che fa da secondo termine della sottrazione
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathSubtract(String nomeGruppo, String nomeCampo, String nomeGruppo2, String nomeCampo2) {
    m_op = TipoOper.MATH_SUBTRACT;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_campoPadre2 = nomeCampo2;
    m_gruppoPadre2 = nomeGruppo2;
    m_sottrattore = null;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della sottrazione fra il campo di
   * nome 'nomeCampo' appartenente al gruppo 'nomeGruppo' e la costante
   * 'sottrattore'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da dividendo
   * @param sottrattore
   *          costante che fa da secondo termine della sottrazione
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathSubtract(String nomeGruppo, String nomeCampo, Number sottrattore) {
    m_op = TipoOper.MATH_DIVIDE;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_sottrattore = sottrattore;
    m_campoPadre2 = null;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della moltiplicazione fra il campo
   * di nome 'nomeCampo' appartenente al gruppo 'nomeGruppo' e la costante
   * 'moltiplicatore'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da fattore
   * @param moltiplicatore
   *          costante che fa da fattore
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathMultiply(String nomeGruppo, String nomeCampo, Number moltiplicatore) {
    m_op = TipoOper.MATH_MULTIPLY;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_moltiplicatore = moltiplicatore;
    m_campoPadre2 = null;

    return this;
  }

  /**
   * Imposta questo campo come il risultato della moltiplicazione fra il campo
   * di nome 'nomeCampo' appartenente al gruppo 'nomeGruppo' con il campo di
   * nome 'nomeCampo2' appartenente al gruppo 'nomeGruppo2'.
   * 
   * @param nomeGruppo
   *          gruppo di appartenenza del campo. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo
   *          nome del campo che fa da fattore
   * @param nomeGruppo2
   *          gruppo di appartenenza del campo2. Se null è lo stesso gruppo di
   *          questo campo
   * @param nomeCampo2
   *          nome del campo che fa da secondo fattore
   * 
   * @return ritorna questo campo (utile per le chiamate di metodi con chaining)
   */
  public DataFieldAutoModel setMathMultiply(String nomeGruppo, String nomeCampo, String nomeGruppo2, String nomeCampo2) {
    m_op = TipoOper.MATH_MULTIPLY;
    m_campoPadre = nomeCampo;
    m_gruppoPadre = nomeGruppo;
    m_campoPadre2 = nomeCampo2;
    m_gruppoPadre2 = nomeGruppo2;
    m_moltiplicatore = null;

    return this;
  }

  public TipoOper getOperation() {
    return m_op;
  }

  @Override
  public boolean isAuto() {
    return true;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    DataFieldAutoModel clone = (DataFieldAutoModel) super.clone();
    return clone;
  }

  public String getGruppoPadre() {
    return m_gruppoPadre;
  }

  public String getGruppoPadre2() {
    return m_gruppoPadre2;
  }

  public String getCampoPadre() {
    return m_campoPadre;
  }

  public String getCampoPadre2() {
    return m_campoPadre2;
  }

  public String getNomeCampoRif() {
    return m_campo;
  }

  public Number getDivisore() {
    return m_divisore;
  }

  public Number getMoltiplicatore() {
    return m_moltiplicatore;
  }

  public Number getSotrattore() {
    return m_sottrattore;
  }

  public UserCalcListener getListener() {
    return m_listener;
  }

}
