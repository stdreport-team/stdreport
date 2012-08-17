/**
 * 
 */
package org.xreports.expressions.symbols;

import org.xreports.engine.ResolveException;
import org.xreports.util.Text;
import org.xreports.expressions.lexer.Lexer;

/**
 * @author pier
 * 
 */
public class Field extends UnsignedMathValue {

  /** nome del campo */
  private String  m_field;
  /** nome del gruppo */
  private String  m_group;
  /**
   * QualifiedField: � la radice dell'albero sintattico dell'espressione dentro
   * le parentesi quadre
   */
  private Symbol  m_qualifiedExpression;
  /** indica se la qualified expressione � booleana (true) o aritmetica (false) */
  private boolean m_booleanQualifiedExpression;

  /**
   * Costruttore di SimpleField, cio� un campo senza qualifiedExpression. In
   * pratica un campo nella forma <tt>gruppo#campo</tt> oppure <tt>#campo</tt>.
   * 
   * @param groupName
   *          nome del gruppo; se null o stringa vuota � un Field senza
   *          specifica del gruppo, cio� del tipo <tt>#campo</tt>, altrimenti �
   *          del tipo <tt>gruppo#campo</tt>
   * @param fieldName
   *          nome del campo
   * @param position
   *          indice del carattere iniziale del simbolo nella stringa che
   *          contiene l'intera espressione
   */
  public Field(String groupName, String fieldName, int position) {
    this(groupName, fieldName, null, position);
  }

  /**
   * Shortcut per {@link #Field(String, String, Symbol, boolean, int)}
   * con <tt>booleanQualExpr</tt> = <b>true</b>.
   */
  public Field(String groupName, String fieldName, Symbol qualifiedExpression, int position) {
    this(groupName, fieldName, qualifiedExpression, true, position);
  }

  /**
   * Costruttore di QualifiedField, cio� un campo con qualifiedExpression. In
   * pratica un campo nella forma <tt>gruppo[expression]#campo</tt> oppure
   * <tt>gruppo[expression]</tt>. Se qualifiedExpression � null � invece un
   * campo normale.
   * 
   * @param groupName
   *          nome del gruppo; se null o stringa vuota � un Field senza
   *          specifica del gruppo (e quindi non pu� essere un qualified field)
   * @param fieldName
   *          nome del campo; pu� essere null solo se � un qualifiedField
   * @param qualifiedExpression
   *          se diverso da null, � il simbolo radice dell'albero sintattico
   *          dell'espressione contenuta dentro le parentesi quadre della
   *          qualified expression
   * @param booleanQualExpr
   *          se true, indica che la qualified expression � booleana, altrimenti
   *          � aritmetica
   * @param position
   *          indice del carattere iniziale del simbolo nella stringa che
   *          contiene l'intera espressione
   * 
   */
  public Field(String groupName, String fieldName, Symbol qualifiedExpression, boolean booleanQualExpr, int position) {
    setPosition(position);

    m_group = Text.isValue(groupName) ? groupName : null;
    m_field = fieldName;

    if (m_group == null && m_field == null) {
      throw new IllegalArgumentException("Impossibile avere un Field senza gruppo e campo");
    }

    m_qualifiedExpression = qualifiedExpression;
    m_booleanQualifiedExpression = booleanQualExpr;

    if (m_group != null) {
      if (m_qualifiedExpression != null) {
        setText(m_group + Lexer.FIELD_QUALIFIED_START + "..." + Lexer.FIELD_QUALIFIED_END);
        //testo la presenza del nome campo: potrebbe essere assente in un qualified field 
        if (m_field != null) {
          setText(getText() + Lexer.FIELD_MODIFIER + m_field);
        }
      } else {
        setText(m_group + Lexer.FIELD_MODIFIER + m_field);
      }
    } else {
      setText(Lexer.FIELD_MODIFIER + m_field);
    }
  }

  /**
   * @return il nome del campo, senza il simbolo '#' e senza il nome di gruppo
   */
  public String getField() {
    return m_field;
  }

  /**
   * @return il nome del gruppo; se non c'� la specifica del gruppo, ritorna
   *         null
   */
  public String getGroup() {
    return m_group;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isField()
   */
  @Override
  public boolean isField() {
    return true;
  }

  public Object evaluate(Evaluator evaluator) throws EvaluateException {
    if (evaluator == null) {
      throw new EvaluateException("Field " + getText() + ": non riesco a valutare il simbolo.");      
    }
    try {
      setPartialValue(evaluator.evaluate(this));
    } catch (ResolveException e) {
      throw new EvaluateException(e);
    }
    return getPartialValue();
  }

  public String toString() {
    return "Field " + getText();
  }

  public Symbol getQualifiedExpression() {
    return m_qualifiedExpression;
  }

  public boolean isQualifiedBooleanExpression() {
    return m_booleanQualifiedExpression;
  }

  //  public void setQualifiedExpression(Symbol qualifiedExpression) {
  //    m_qualifiedExpression = qualifiedExpression;
  //  }
}
