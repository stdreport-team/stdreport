package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.DataFieldAuto;
import org.xreports.datagroup.DataFieldAutoModel;
import org.xreports.datagroup.DataFieldModel;
import org.xreports.datagroup.DataFieldModel.TipoCampo;
import org.xreports.datagroup.Group;
import org.xreports.datagroup.GroupModel;
import org.xreports.datagroup.UserCalcListener;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Field;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.BloccoTesto;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

public class FieldElement extends ChunkElement implements UserCalcListener {
  /** Nomi della controparte XML degli attributi dell'elemento "field" */
  protected static final String ATTRIB_NAME           = "name";
  protected static final String ATTRIB_FORMAT         = "format";
  protected static final String ATTRIB_VALUE          = "value";
  protected static final String ATTRIB_DEFAULTIFNULL  = "defaultIfNull";
  protected static final String ATTRIB_TRACE          = "trace";

  public static final String    DEFAULTIFNULL_DEFAULT = "#null#";

  /**
   * cache del valore formattato
   */
  private String                c_evaluatedExpression = null;

  public FieldElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_NAME, String.class);
    addAttributo(ATTRIB_FORMAT, String.class);
    addAttributo(ATTRIB_DEFAULTIFNULL, String.class, DEFAULTIFNULL_DEFAULT);
    addAttributo(ATTRIB_VALUE, String.class, null, TAG_VALUE);
    addAttributo(ATTRIB_TRACE, String.class, null);
  }

  @Override
  public String toString() {
    String out = "";
    if (getName() != null) {
      out += " name=" + getName();
    }
    if (getValue() != null) {
      if (out.length() > 0) {
        out += ",";
      }
      out += " value=" + getValue();
    }
    return getTagName() + out + super.getNodeLocation();
  }

  public String getFormat() {
    return getAttributeText(ATTRIB_FORMAT);
  }

  /**
   * Stringa da ritornare nel caso il valore sia null
   * 
   * @return the attrib_defaultIfNull
   */
  public String getDefaultValueIfNull() {
    return getAttributeText(ATTRIB_DEFAULTIFNULL);
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#fineParsingElemento()
   */
  @Override
  public void fineParsingElemento() {
  }

  /**
   * Nome del campo: corrisponde all'attributo <b>name</b> del corrispettivo
   * elemento XML.
   */
  @Override
  public String getName() {
    return getAttributeText(ATTRIB_NAME);
  }

  /**
   * Ritorna il valore <b>formattato</b> di questo campo. Il calcolo avviene
   * durante il metodo {@link #generate(Group, Stampa, Elemento)} e cachizzato
   * in questa proprietà.
   */
  public String getEvaluatedExpression() {
    return c_evaluatedExpression;
  }

  protected String getFormattedValue(Object value) throws GenerateException {
    if (value == null) {
      return getAttributeText(ATTRIB_DEFAULTIFNULL);
    }
    return value.toString();
  }

  /**
   * Nel caso il tag abbia l'attributo <var>value</var>, ritorna il simbolo
   * top-level dell'albero sintattico. <br>
   * Se non ha l'attributo <var>value</var>, torna null.
   */
  public Symbol getExpressionTree() {
    return getAttrSymbol(ATTRIB_VALUE);
  }

  /**
   * Valore dell'attributo value. Se questo field non ha l'attributo value,
   * ritorna null.
   * 
   * @return attributo value oppure null se attributo non esistente
   */
  public String getValue() {
    return getAttributeText(ATTRIB_VALUE);
  }

  /**
   * Ritorna valore dell'attributo <tt>trace</tt>
   */
  public String getTrace() {
    return getAttributeText(ATTRIB_TRACE);
  }

  /**
   * Routine chiamata durante il parsing del XML per aggiungere l'oggetto Campo
   * al suo Gruppo. Se questo field è un campo da aggiungere ad un gruppo, lo
   * crea e lo aggiunge al gruppo passato.
   * 
   * @param model
   *          modello di gruppo a cui aggiungere il campo
   * 
   * @return il campo creato e aggiunto a parent, oppure null se non ha aggiunto
   *         nulla
   * @throws GenerateException
   */
  public void addCampo(GroupModel model) throws GenerateException {
    DataFieldModel campo = null;
    Symbol expressionTree = getExpressionTree();

    if (expressionTree != null) {
      //"value" e "name" entrambi valorizzati
      addFieldsToModel(expressionTree, model);
      if (expressionTree.isField()) {
        if (getName() != null && !getName().equalsIgnoreCase( ((Field) expressionTree).getField())) {
          //sono in un caso di questo tipo: <field name="x" value="#y"/> dove x!=y. 
          //In pratica "x" è un alias di "y" 
          campo = model.addFieldAutoSafe(getName());
          ((DataFieldAutoModel) campo).setUserCalc(this);
        }
      } else if (getName() != null) {
        //se arrivo qui ho un campo di questo tipo: <field name="x" value="expression..." />
        //Cioè un'espression che non è un semplice campo
        campo = model.addFieldAutoSafe(getName());
        ((DataFieldAutoModel) campo).setUserCalc(this);
      }
    } else if (getName() != null) {
      //se sono qui ho l'attributo name ma non value: è un semplice campo dei dati 
      campo = model.addFieldSafe(getName());
    }

    //    if (expressionTree != null && getName() != null) {
    //      //se sono qui ho gli attributi name e value:
    //      //è un candidato per un campo auto
    //      if (expressionTree.isUnaryFunction()) {
    //        Function uf = (Function) expressionTree;
    //        if (uf.is(FUNZ_SUM)) {
    //          //campo somma: mi aspetto un campo come argomento
    //          Symbol arg = uf.getChild(0);
    //          if (arg.isField()) {
    //            Field f = (Field) arg;
    //            if (f.getGroup() == null) {
    //              throw new GenerateException(this, "Un campo somma deve avere la specifica del gruppo.");
    //            }
    //            campo = new DataFieldAutoModel(model, getName(), TipoCampo.UNKNOWN);
    //            ((DataFieldAutoModel) campo).setUserCalc(this);
    //            //((DataFieldAuto)campo).setSommaDiscen(f.getGroup(), f.getField());
    //          } else
    //            throw new GenerateException(this, "Un campo somma deve avere un campo come unico argomento.");
    //        } else if (uf.is(FUNZ_COUNT)) {
    //          //campo conteggio: mi aspetto un nome gruppo come argomento
    //          Symbol arg = uf.getChild(0);
    //          if (arg.isIdentifier()) {
    //            campo = new DataFieldAutoModel(model, getName(), TipoCampo.UNKNOWN);
    //            ((DataFieldAutoModel) campo).setUserCalc(this);
    //            //((DataFieldAuto)campo).setConteggioDiscen(arg.getText());
    //          } else
    //            throw new GenerateException(this, "Un campo conteggio deve avere un nome gruppo come unico argomento.");
    //        }
    //      } else if (expressionTree.isNullaryFunction()) {
    //        Function uf = (Function) expressionTree;
    //        if (uf.is(FUNZ_SUM)) {
    //          campo = model.addField(getName());
    //          campo.setAddValue(true);
    //        }
    //      }
    //      if (campo == null && getName() != null) {
    //        campo = new DataFieldAutoModel(model, getName(), TipoCampo.UNKNOWN);
    //        ((DataFieldAutoModel) campo).setUserCalc(this);
    //      }
    //    } else if (getName() != null) {
    //      //se sono qui ho l'attributo name ma non value: è un campo output
    //      if ( !model.existCampo(getName())) {
    //        campo = model.addField(getName());
    //      } 
    //    }
    if (campo != null) {
      if (isNumberField()) {
        //FIXME mettere double come default non è un granche.....
        if (isIntero())
          campo.setTipo(TipoCampo.LONG);
        else
          campo.setTipo(TipoCampo.DOUBLE);
      } else if (isDateField()) {
        campo.setTipo(TipoCampo.DATE);
      }
    }
  }

  private void addFieldsToModel(Symbol s, GroupModel model) {
    if (s.isField()) {
      Field f = (Field) s;
      if (f.getGroup() == null) {
        //aggiungo il campo solo se non ha il gruppo: infatti la mancanza di gruppo
        //indica che il campo appartiene al gruppo indicato, che è "model"
        model.addFieldSafe(f.getField());
      }
    }
    for (Symbol child : s.getChildren()) {
      if (child.isField()) {
        Field f = (Field) child;
        if (f.getField() != null && (f.getGroup() == null || f.getGroup().equalsIgnoreCase(model.getName()))) {
          model.addFieldSafe(f.getField());
        }
        if (f.getQualifiedExpression() != null) {
          addFieldsToModel(f.getQualifiedExpression(), model);
        }
      } else {
        addFieldsToModel(child, model);
      }
    }
  }

  protected boolean isDateField() {
    return false;
  }

  protected boolean isNumberField() {
    return false;
  }

  protected boolean isIntero() {
    return false;
  }

  /**
   * Questo metodo viene chiamato direttamente da
   * {@link DataFieldAuto#getValue()} quando si richiede il valore del campo. <br>
   * Quando c'è un campo con attributo <b>name</b> impostato e come valore
   * un'espressione, viene aggiunto il campo al gruppo come CampoAuto e
   * impostato lo UserCalcListener a questo oggetto. In questo modo si può
   * valutare l'espressione.
   */
  @Override
  public void evaluateField(DataFieldAuto campo) {
    //switcho temporanemente il gruppo corrente sul gruppo del campo
    Group gruppoSave = getGroup();
    try {
      setGroup((Group) campo.getGroup());
      Object value = getExpressionTree().evaluate(this);
      campo.setValue(value);
    } catch (EvaluateException e) {
      e.printStackTrace();
    } finally {
      //mi riporto al gruppo che avevo in partenza
      setGroup(gruppoSave);
    }
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      salvaStampaGruppo(stampa, gruppo);
      Object value = null;
      if (getName() == null) {
        if (isVisible()) {
          if (getExpressionTree() != null) {
            //questo campo non ha l'attributo 'name' --> non è un campo di Gruppo
            //quindi lo faccio valutare alle classi di ciscoop.util.expression
            value = getExpressionTree().evaluate(this);
          } else {
            //non c'è attributo 'name' e il parsing di value è andato male--> come valore
            //prendo letteralmente l'attributo value
            value = getValue();
          }
        }
      } else {
        //questo campo ha l'attributo 'name' --> è un campo di Gruppo
        //quindi ne prendo semplicemente il valore
        value = calcValue(getName(), gruppo);
      }

      //cache della valutazione effettuata
      c_evaluatedExpression = getFormattedValue(value);

      //NB: anche se l'elemento non è visibile, torno comunuqe una lista, vuota.
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        if (isDebugData()) {
          stampa.debugElementOpen(this);
        }
        listaElementi.add(createOutputElement(stampa, padre));
      }

      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException(this, e, "Errore imprevisto in generazione field");
    }
  }

  protected Elemento createOutputElement(Stampa stampa, Elemento padre) throws GenerateException {
    BloccoTesto bloccoTesto = stampa.getFactoryElementi().creaBloccoTesto(stampa, this, padre);
    bloccoTesto.fineGenerazione();
    return bloccoTesto;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_FIELD;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }

  @Override
  public boolean canChildren() {
    return false;
  }

}
