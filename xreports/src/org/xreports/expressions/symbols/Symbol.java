/**
 * 
 */
package org.xreports.expressions.symbols;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.xreports.Destroyable;
import org.xreports.expressions.lexer.Lexer;
import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.util.Text;

/**
 * Generico simbolo risultato dell'analisi sintattica fatta con il package
 * ciscoop.util.expression. Tale analisi utilizza una produzione che riconosce
 * le usuali espressioni aritmetiche, booleane e le espressioni di tipo testo.
 * Per maggiori dettagli vedi <tt>ciscooop.util.expression.grammatica.doc</tt>
 * 
 * L'analisi sintattica produce un albero sintattico in cui tutti i nodi sono
 * istanze di Symbol.
 * 
 * 
 * @author pier
 * 
 */
public abstract class Symbol implements Destroyable {

  private String       m_fullText;
  private String       m_text;
  private int          m_position;

  /** indica il simbolo padre di questo nel parse tree */
  private Symbol       m_parent;

  private List<Symbol> m_childs = new LinkedList<Symbol>();

  /**
   * tag user-defined per distinguere questo simbolo da altri nel namespace
   * corrente o per altri scopi comunque esterni all'analisi sintattica
   */
  private String       m_tag;

  private Object       c_partialValue;

  /**
   * Costruttore per simboli non terminali
   */
  public Symbol() {
    
  }
  
  /**
   * Costruttore per simboli terminali
   * @param t token che rappresenta questo simbolo
   * @param type tipo previsto del token: se = null o TokenType.Any, viene ignorato
   * 
   * @throws ParseException in caso di token errato/imprevisto
   */
  public Symbol(Token t, TokenType type) throws ParseException {
    if (type!=null && type!=TokenType.Any) {
      if (!t.is(type)) {
        throw new ParseException(
            "Errore interno, token di tipo imprevisto", t);
      }      
    }
    setPosition(t.getStartPosition());
    setText(t.getValue());
    setFullText(t.getTokenSourceVal());
  }
  
  /**
   * Ritorna il testo del simbolo. In alcuni casi il testo non corrisponde
   * esattamente al testo originale nel sorgente (per il quale si deve usare
   * {@link #getFullText()}); è piuttosto il testo che rappresenta il simbolo,
   * senza alcuni caratteri modificatori che a questo livello non servono. Ad
   * esempio una {@link Function} nel testo ha solo il nome della funzione e non
   * anche le parentesi "()" dopo il nome o gli argomenti; oppure se ho un
   * {@link Constant}, {@link #getText()} non ritorna il '$' che precede il nome
   * della costante.
   * 
   * @return il testo associato a questo simbolo
   */
  public String getText() {
    if (m_text != null) {
      return m_text;
    }
    StringBuilder sb = new StringBuilder();
    for (Symbol child : m_childs) {
      sb.append(child.getText());
    }
    return sb.toString();
  }

  /**
   * Imposta il testo del simbolo
   * 
   * @param text
   *          text to set
   */
  public void setText(String text) {
    m_text = text;
    m_fullText = text;
  }

  /**
   * Ritorna la posizione del token, cioè l'indice (0-based) del primo carattere
   * del token nel testo di input.
   * 
   * @return posizione del token nell'input
   */
  public int getPosition() {
    return m_position;
  }

  /**
   * Imposta la posizione
   * 
   * @param p_position
   *          the position to set
   */
  public void setPosition(int p_position) {
    m_position = p_position;
  }

  public void debug() {
    debug(0);
  }

  public void debug(int index) {
    String tabs = index == 0 ? "" : Text.getChars('\t', index);
    System.out.println(tabs + getClassName() + ": '" + getText() + "', position " + getPosition());
    if (m_childs != null) {
      for (Symbol child : m_childs) {
        child.debug(index + 1);
      }
    }
  }

  /**
   * Ritorna il nome di questa classe senza la specifica del package
   */
  public String getClassName() {
    String name = getClass().getName();
    int i = name.lastIndexOf('.');
    if (i > 0)
      name = name.substring(i + 1);
    return name;
  }

  /**
   * Ritorna true se questo simbolo è una funzione
   */
  public boolean isFunction() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una funzione unaria (a un solo parametro)
   */
  public boolean isUnaryFunction() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una funzione binaria (a due parametri)
   */
  public boolean isBinaryFunction() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una chiamata a funzione user-defined
   */
  public boolean isMethodCall() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una funzione nullaria (senza parametri)
   */
  public boolean isNullaryFunction() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è un Field
   */
  public boolean isField() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è un Identifier
   */
  public boolean isIdentifier() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una Costante
   */
  public boolean isConstant() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è una operazione (matematica,...)
   */
  public boolean isOperation() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è un literal (boolean, numerico, etc)
   */
  public boolean isLiteral() {
    return false;
  }

  /**
   * Ritorna true se questo simbolo è un literal numerico
   */
  public boolean isNumberLiteral() {
    return false;
  }

  /**
   * Confronta (ignorando maiuscole/minuscole) il testo passato con il testo di
   * questo simbolo.
   * 
   * @param text
   *          testo da confrontare
   * @return true sse il testo passato è uguale (case-insensitive) al testo di
   *         quetso simbolo
   * @see #getText()
   */
  public boolean is(String text) {
    if (m_text != null && text != null)
      return m_text.equalsIgnoreCase(text);
    else
      return false;
  }

  /**
   * Ritorna true se una delle stringhe passate è il testo di questo simbolo. Il
   * confronto è <b>case-insensitive</b>.
   * 
   * @param text
   *          elenco stringhe da confrontare
   */
  public boolean isOneOf(String... text) {
    if (m_text != null && text != null) {
      for (String t : text) {
        if (m_text.equalsIgnoreCase(t))
          return true;
      }
      return false;
    } else
      return false;
  }

  /**
   * @return il tag impostato con {@link #setTag(String)}
   */
  public String getTag() {
    return m_tag;
  }

  /**
   * Permette di impostare una qualsiasi stringa user-defined a questo simbolo.
   * Il significato e l'uso sono totalmente a carico dell'ambiente esterno.
   * 
   * @param tag
   *          tag da impostare
   */
  public void setTag(String p_tag) {
    m_tag = p_tag;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String txt = getClassName() + "@" + hashCode() + ": '";
    txt += getText() + "'";
    return txt;
  }

  public abstract Object evaluate(Evaluator evaluator) throws EvaluateException;

  /**
   * @return il testo completo di questo simbolo come compare nel testo sorgente
   */
  public String getFullText() {
    return m_fullText;
  }

  /**
   * @param mFullText
   *          testo da impostare
   */
  public void setFullText(String mFullText) {
    m_fullText = mFullText;
  }

  public abstract boolean isTerminal();

  public abstract boolean isConcrete();
  
  public static Symbol getInstance(Token t) throws LexerException, ParseException {
    switch (t.getType()) {
      case MathOp:
        if (t.isOneOf("+", "-")) {
          return new SumOp(t);
        } else if (t.is("^")) {
          return new ExpOp(t);
        } else {
          return new MultOp(t);
        }
      case LogicalOp:
        if (t.is("&&")) {
          return new AndOperator(t);
        } else if (t.is("||")) {
          return new OrOperator(t);
        } else if (t.is("!")) {
          return new NotOperator(t);
        } else {
          return null;
        }
      case Number:
        return new NumberLiteral(t);
      case Identifier:
        return new Identifier(t);
      case Constant:
        return new Constant(t);
      case String:
        return new StringLiteral(t);
      case MethodCall:
        return new MethodCall(t);
      case Field:
        int i = t.getValue().indexOf(Lexer.FIELD_MODIFIER);
        String groupName,
        fieldName;
        if (i > 0) {
          groupName = t.getValue().substring(0, i);
        } else {
          groupName = null;
        }
        fieldName = t.getValue().substring(i + 1);
        return new Field(groupName, fieldName, t.getStartPosition());
//      case ParT:
//        if (t.is("(")) {
//          return new MathParenthesizedExpr(t.getStartPosition());
//        } else {
//          throw new LexerException("Token non previsto per la creazione del simbolo: " + t);
//        }
      default:
        throw new LexerException("Token non previsto per la creazione del simbolo: " + t);
    }
  }

  public Symbol getParent() {
    return m_parent;
  }

  public void setParent(Symbol parent) {
    m_parent = parent;
  }

  /**
   * Aggiunge i figli a questo simbolo. <br/>
   * Il metodo fa tutto quanto necessario per mantenere una corretta gerarchia
   * padre-figlio, cioè se il figlio da aggiungere è già figlio di un altro
   * simbolo, questo simbolo diventa medio fra il precedente padre e il child
   * aggiunto, in particolare:
   * <ol>
   * <li>imposta child come figlio di questo simbolo</li>
   * <li>se child ha un padre, imposta il padre di questo simbolo al padre che
   * aveva child</li>
   * <li>se child ha un padre, rimpiazza il figlio child con questo simbolo</li>
   * <li>imposta il padre di child con questo simbolo</li>
   * </ol>
   * 
   * @param child
   *          simbolo che deve diventare figlio di questo
   * @return questo simbolo, utile per chaining di chiamate
   * @throws ParseException
   */
  public Symbol addChild(Symbol child) throws ParseException {
    if (child == null) {
      //nella fase di parsing potrei avere dei simboli non terminali
      //che non generano nulla e quindi il rispettivo simbolo è null
      //in tal caso non si deve aggiungere alcun figlio
      return this;
    }
    if (isTerminal()) {
      throw new ParseException("Impossibile aggiungere un figlio ad un simbolo terminale");
    }
    m_childs.add(child);
    Symbol childParent = child.getParent();
    if (childParent != null) {
      childParent.replaceChild(child, this);
    }
    child.setParent(this);
    return this;
  }

  /**
   * Sostituisce il simbolo oldChild, figlio di questo, 
   * con il simbolo newChild.
   * Se oldChild non viene trovato, il metodo non fa nulla.
   * Questo metodo setta anche il parent di newChild nel caso la sostituzione venga fatta.
   * 
   * @param oldChild figlio da rimpiazzare
   * @param newChild figlio da sostituire al posto di oldChild
   * 
   * @return oldChild se è effettivamente figlio di questo nodo, altrimenti null
   */
  public Symbol replaceChild(Symbol oldChild, Symbol newChild) {
    for (int i = 0; i < m_childs.size(); i++) {
      if (m_childs.get(i) == oldChild) {
        m_childs.set(i, newChild);
        newChild.setParent(this);
        return oldChild;
      }
    }
    return null;
  }

  public List<Symbol> getChildren() {
    return m_childs;
  }

  public Symbol getChild(int index) {
    if (m_childs != null) {
      return m_childs.get(index);
    }
    return null;
  }

  /**
   * Ritorna la quantità di figli di questo simbolo.
   */
  public int getChildNumber() {
    return m_childs.size();
  }

  public Symbol getPreviousSibling() {
    Symbol prevSibling = null;
    if (getParent() != null) {
      int thisIndex = -1;
      for (int i = 0; i < getParent().getChildNumber(); i++) {
        if (getParent().getChild(i) == this) {
          thisIndex = i;
          break;
        }
      }
      if (thisIndex > 0) {
        prevSibling = getParent().getChild(thisIndex - 1);
      }
    }
    return prevSibling;
  }

  public Object getPartialValue() {
    return c_partialValue;
  }

  protected void setPartialValue(Object val) {
    //System.out.println(this + ", partialValue=" + val + ", parent=" + getParent());
    c_partialValue = val;
  }

  /**
   * Se non ha figli torna null, altrimenti torna la valutazione del primo
   * figlio
   * 
   * @param evaluator
   *          valutatore esterno
   * @return valutazione del solo primo figlio
   * @throws EvaluateException
   *           per errori in valutazione
   */
  protected Object evaluateChild(Evaluator evaluator) throws EvaluateException {
    if (getChildNumber() == 0) {
      return null;
    }
    return getChild(0).evaluate(evaluator);
  }

  /**
   * Se non ha figli torna null, altrimenti torna la valutazione del primo
   * figlio. Se ha almeno due figli valuta anche il secondo. Memorizza il
   * risultato finale nel partialValue di questo oggetto.
   * 
   * @param evaluator
   *          valutatore esterno
   * @return valutazione del primo figlio e se c'è del secondo
   * @throws EvaluateException
   *           per errori in valutazione
   */
  protected Object evaluateChildren(Evaluator evaluator) throws EvaluateException {
    if (getChildNumber() == 0) {
      return null;
    }
    //valuto il primo figlio che sicuramente è un MathFactor
    setPartialValue(getChild(0).evaluate(evaluator));
    if (getChildNumber() > 1) {
      //devo valutare il secondo figlio che è un MathTermOpt
      setPartialValue(getChild(1).evaluate(evaluator));
    }
    return getPartialValue();
  }

  protected Boolean evaluateBinaryBoolOp(Evaluator evaluator, Object val1, Operator op, Symbol o2) throws EvaluateException {
    // ***************** valutazione primo operando ***************    
//    if (o1 == null) {
//      throw new EvaluateException("Il primo operando dell'espressione " + getText() + " è null");
//    }
    Boolean boolean1 = null;
    // *** operatore logico e valutazione short-circuit
    Boolean shortCircuitResult = null;
    if (op.isLogical()) {
      if ( ! (val1 instanceof Boolean)) {
        throw new EvaluateException("Il primo operando dell'espressione " + getText() + " non è booleano: '" + val1 + "'");
      }
      boolean1 = (Boolean) val1;
      if (op.isAND() && !boolean1.booleanValue()) {
        // ho un AND col primo operando false --> risultato false
        shortCircuitResult = Boolean.FALSE;
      } else if (op.isOR() && boolean1.booleanValue()) {
        // ho un OR col primo operando true --> risultato true
        shortCircuitResult = Boolean.TRUE;
      }
    }
    if (shortCircuitResult != null) {
      return shortCircuitResult;
    }

    // *** valutazione secondo operando 
    if (o2 == null) {
      throw new EvaluateException("Il secondo operando dell'espressione " + getText() + " è null");
    }
    Boolean boolean2 = null;
    Object val2 = o2.evaluate(evaluator);
    if (op.isLogical()) {
      if ( ! (val2 instanceof Boolean)) {
        throw new EvaluateException("Il secondo operando " + o2.getText() + " dell'espressione " + getText() + " non è booleano: '" + val2 + "'");
      }
      boolean2 = (Boolean)val2;
      if (op.isAND())
        return boolean1 && boolean2;
      else if (op.isOR())
        return boolean1 || boolean2;
    } else if (op.isRelational()) {
      return compareObj(val1, val2, op);
    }
    throw new EvaluateException("Operatore non riconosciuto: '" + op + "'");
  }

  protected Number evaluateBinaryMathOp(Evaluator evaluator, Object o1, Operator op, Object o2) throws EvaluateException {
    if ( ! (o1 instanceof Number)) {
      throw new EvaluateException("Il primo operando dell'espressione " + getText() + " non è numerico: '" + o1 + "'");
    }
    if ( ! (o2 instanceof Number)) {
      throw new EvaluateException("Il secondo operando dell'operatore '" + op + "' nell'espressione " + getText()
          + " non è numerico: '" + o2 + "'");
    }
    boolean bDecimal = false;
    bDecimal = (o1 instanceof Double) || (o2 instanceof Double) || (o1 instanceof BigDecimal) || (o2 instanceof BigDecimal);

    Double d1 = null, d2 = null;
    Long l1 = null, l2 = null;
    if (bDecimal) {
      d1 = Double.valueOf( ((Number) o1).doubleValue());
      d2 = Double.valueOf( ((Number) o2).doubleValue());
    } else {
      l1 = Long.valueOf( ((Number) o1).longValue());
      l2 = Long.valueOf( ((Number) o2).longValue());
    }

    if (op.is(Operator.PLUS)) {
      if (bDecimal)
        return new Double(d1.doubleValue() + d2.doubleValue());
      else
        return new Long(l1.longValue() + l2.longValue());
    } else if (op.is(Operator.MINUS)) {
      if (bDecimal)
        return new Double(d1.doubleValue() - d2.doubleValue());
      else
        return new Long(l1.longValue() - l2.longValue());
    } else if (op.is(Operator.MULT)) {
      if (bDecimal)
        return new Double(d1.doubleValue() * d2.doubleValue());
      else
        return Long.valueOf(l1.longValue() * l2.longValue());
    } else if (op.is(Operator.DIV)) {
      if (bDecimal)
        return new Double(d1.doubleValue() / d2.doubleValue());
      else
        return new Double(l1.doubleValue() / l2.doubleValue());
    } else if (op.is(Operator.MODULO)) {
      if (bDecimal)
        return new Double(d1.doubleValue() % d2.doubleValue());
      else
        return new Long(l1.longValue() % l2.longValue());
    } else if (op.is(Operator.EXP)) {
      if (bDecimal)
        return new Double(Math.pow(d1.doubleValue(), d2.doubleValue()));
      else
        return new Double(Math.pow(l1.doubleValue(), l2.doubleValue()));
    } else {
      throw new EvaluateException("Operatore non riconosciuto");
    }
  }

  /**
   * Valuta le produzioni del tipo: <tt>S = Op T S | &#923;</tt>. <br/>
   * 
   * La valutazione avviene così: assegna a partialValue il risultato di
   * <tt>P Op T</tt>, dove P è il partialValue del padre di S. <br/>
   * 
   * Se S ha come terzo figlio un altro S, allora valuta l'S figlio e ne assegna
   * il partialValue al S padre.
   * 
   * @param evaluator
   *          valutatore esterno per valutare simboli quali Field, MethodCall,
   *          Constant,...
   * @return il valore calcolato e assegnato al partialValue di questo simbolo
   * @throws EvaluateException
   *           in caso di errori di valutazione
   */
  protected Object evaluatePartialOp(Evaluator evaluator) throws EvaluateException {
    Object superPartialValue = getParent().getPartialValue();
    Operator op = (Operator)getChild(0);
    Object val2 = getChild(1).evaluate(evaluator);
    c_partialValue = evaluateBinaryMathOp(evaluator, superPartialValue, op, val2);
    if (getChildNumber() > 2) {
      //produzione   SumOp MathTerm MathExprOpt   
      c_partialValue = getChild(2).evaluate(evaluator);
    }
    return c_partialValue;
  }

  protected Object evaluateBoolPartialOp(Evaluator evaluator) throws EvaluateException {
    Object superPartialValue = getParent().getPartialValue();
    Operator op = (Operator)getChild(0);
    //Object val2 = getChild(1).evaluate(evaluator);
    c_partialValue = evaluateBinaryBoolOp(evaluator, superPartialValue, op, getChild(1));
    if (getChildNumber() > 2) {
      //produzione   SumOp MathTerm MathExprOpt   
      c_partialValue = getChild(2).evaluate(evaluator);
    }
    return c_partialValue;
  }
  
  private boolean compareObj(Object o1, Object o2, Operator op) throws EvaluateException {
    if (o1==null || o2==null) {
      if (op.is(Operator.EQ))
        return o1==o2;
      else if (op.is(Operator.NEQ)) 
        return o1 != o2;
      
      return false;
    }
    
    if (o1.getClass() != o2.getClass()) {
      // gli oggetti sono di classe diversa: se uno dei due è string,
      // provo a convertire
      // l'altro in stringa e poi le confronto
      if (o1.getClass() == String.class) {
        o2 = String.valueOf(o2);
      } else if (o2.getClass() == String.class) {
        o1 = String.valueOf(o1);
      }
    }
    if (o1 instanceof Number && o2 instanceof Number) {
      Number n1 = (Number) o1;
      Number n2 = (Number) o2;
      if (areInt(n1, n2)) {
        if (op.is(Operator.EQ)) {
          return n1.longValue() == n2.longValue();
        }
        if (op.is(Operator.NEQ)) {
          return n1.longValue() != n2.longValue();
        }
        if (op.is(Operator.GT)) {
          return n1.longValue() > n2.longValue();
        } else if (op.is(Operator.GTE)) {
          return n1.longValue() >= n2.longValue();
        } else if (op.is(Operator.LT)) {
          return n1.longValue() < n2.longValue();
        } else if (op.is(Operator.LTE)) {
          return n1.longValue() <= n2.longValue();
        }
      } else {
        if (op.is(Operator.EQ)) {
          return n1.doubleValue() == n2.doubleValue();
        }
        if (op.is(Operator.NEQ)) {
          return n1.doubleValue() != n2.doubleValue();
        }
        if (op.is(Operator.GT)) {
          return n1.doubleValue() > n2.doubleValue();
        } else if (op.is(Operator.GTE)) {
          return n1.doubleValue() >= n2.doubleValue();
        } else if (op.is(Operator.LT)) {
          return n1.doubleValue() < n2.doubleValue();
        } else if (op.is(Operator.LTE)) {
          return n1.doubleValue() <= n2.doubleValue();
        }
      }
    }
    if (o1 instanceof String && o2 instanceof String) {
      String s1 = (String) o1;
      String s2 = (String) o2;
      if (op.is(Operator.EQ)) {
        return s1.compareTo(s2) == 0;
      }
      if (op.is(Operator.NEQ)) {
        return s1.compareTo(s2) != 0;
      }
      if (op.is(Operator.GT)) {
        return s1.compareTo(s2) > 0;
      } else if (op.is(Operator.GTE)) {
        return s1.compareTo(s2) >= 0;
      } else if (op.is(Operator.LT)) {
        return s1.compareTo(s2) < 0;
      } else if (op.is(Operator.LTE)) {
        return s1.compareTo(s2) <= 0;
      }
    } else if (o1 instanceof Date && o2 instanceof Date) {
      Date dt1 = (Date) o1;
      Date dt2 = (Date) o2;
      if (op.is(Operator.EQ)) {
        return dt1.compareTo(dt2) == 0;
      }
      if (op.is(Operator.NEQ)) {
        return dt1.compareTo(dt2) != 0;
      }
      if (op.is(Operator.GT)) {
        return dt1.compareTo(dt2) > 0;
      } else if (op.is(Operator.GTE)) {
        return dt1.compareTo(dt2) >= 0;
      } else if (op.is(Operator.LT)) {
        return dt1.compareTo(dt2) < 0;
      } else if (op.is(Operator.LTE)) {
        return dt1.compareTo(dt2) <= 0;
      }
    }
    throw new EvaluateException("Almeno un tipo di dato non previsto nella espressione " + getText() + ". Primo operando: "
        + o1.getClass().getName() + ", secondo operando: " + o2.getClass().getName());
  }

  /**
   * Ritorna true sse ambedue i Number passati sono interi
   * 
   * @param n1
   *          primo numero
   * @param n2
   *          secondo numero
   */
  private boolean areInt(Number n1, Number n2) {
    if (n1 instanceof Byte || n1 instanceof Short || n1 instanceof Integer || n1 instanceof Long || n1 instanceof BigInteger) {
      if (n2 instanceof Byte || n2 instanceof Short || n2 instanceof Integer || n2 instanceof Long || n2 instanceof BigInteger) {
        return true;
      }
    }
    return false;
  }

  
  /**
   * Distrugge tutti i riferimenti ad altri oggetti 
   * e azzera tutte le var. interne.
   */
  @Override
  public void destroy() {
    c_partialValue = null;
    m_parent = null;
    if (m_childs != null) {
      m_childs.clear();
      m_childs = null;
    }
    m_text = null;
    m_fullText = null;
    m_tag = null;
  }
}
