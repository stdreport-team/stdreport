package org.xreports.engine;

import java.math.BigDecimal;

import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.parsers.GenericParser;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.expressions.parsers.GenericParser.ParserType;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.source.AbstractElement;
import org.xreports.stampa.validation.ValidateException;

/**
 * Classe che implementa il generico parametro del report.
 * 
 * @author pier
 * 
 */
public class Parameter {

  private String  c_name;
  private String  c_className;
  private Object  c_value;
  private boolean c_assigned;
  private boolean c_required;
  private String  c_defaultValue;
  private XReport  c_stampa;

  public Parameter(XReport s, String name, Object value) {
    c_name = name;
    c_stampa = s;
    try {
      if (value != null)
        setValue(value);
    } catch (ValidateException e) {
      //qui non può mai succedere!!
    }
  }

  public Parameter(XReport s, String name) {
    c_name = name;
    c_stampa = s;
  }

  /**
   * Crea un parametro.
   * 
   * @param name
   *          nome parametro
   * @param value
   *          valore parametro; può essere anche null
   * @param className
   *          classe imposta al valore
   * 
   * @throws ValidateException
   *           nel caso value non sia della classe className
   */
  public Parameter(XReport s, String name, Object value, String className) throws ValidateException {
    c_name = name;
    c_className = className;
    c_stampa = s;
    if (value != null)
      setValue(value);
  }

  public String getName() {
    return c_name;
  }

  public String getClassName() {
    return c_className;
  }

  public Object getValue() throws ValidateException, EvaluateException {
    if ( !isAssigned() && getDefaultValue() != null)
      assignDefaultValue();
    return c_value;
  }

  public void setValue(Object value) throws ValidateException {
    check();
    c_value = value;
    c_assigned = true;
  }

  private void assignDefaultValue() throws ValidateException, EvaluateException {
    if (getDefaultValue() != null) {
      if (c_className == null || c_className.equals(String.class.getName())) {
        //se il tipo del parametro non è specificato oppure e' semplicemente una stringa
        //lo assegno così com'è
        c_value = getDefaultValue();
        return;
      }
      ParserType type = null;
      try {
        Class<?> expectedClass = Class.forName(c_className);
        if (Boolean.class.isAssignableFrom(expectedClass)) {
          type = ParserType.BOOLEAN_EXPRESSION;
        } else {
          type = ParserType.MATH_EXPRESSION;
        }
        GenericParser parser = GenericParser.getInstance(type, getDefaultValue());

        Symbol expressionValue = parser.parse();
        //expressionValue.setTag(c_expressionTag);
        Object actualValue = expressionValue.evaluate(c_stampa);
        if ( !expectedClass.isAssignableFrom(actualValue.getClass())) {
          Object convertedValue = convertValue(expectedClass, actualValue);
          if ( !expectedClass.isAssignableFrom(convertedValue.getClass())) {          
            throw new ValidateException("Valore di default '%s' del parametro %s non e' compatibile con la classe imposta '%s'",
                getDefaultValue(), getName(), getClassName());
          }
          actualValue = convertedValue;
        }
        c_value = actualValue;
      } catch (ClassNotFoundException e) {
        throw new ValidateException(e, "Non ho trovato la classe %s del parametro %s", c_className, getName());
      } catch (LexerException e) {
        throw new ValidateException(e, "Parametro %s: sintassi errata nel valore di default %s", getName(), getDefaultValue());
      } catch (ParseException e) {
        throw new ValidateException(e, "Parametro %s: sintassi errata nel valore di default %s", getName(), getDefaultValue());
      }
    }
  }

  private Object convertValue(Class<?> expectedClass, Object value) throws ValidateException {
    if (c_className == null || value == null)
      return value; //no conversion needed

    Object newValue = null;
    //boolean isAbstract = (expectedClass.getModifiers() & Modifier.ABSTRACT) > 0;
    if (Number.class.isAssignableFrom(expectedClass)) {
      //il parametro deve essere un numero
      if (expectedClass == Double.class)
        newValue = Double.valueOf(value.toString());
      else if (expectedClass == Float.class)
        newValue = Float.valueOf(value.toString());
      else if (expectedClass == Long.class)
        newValue = Long.valueOf(value.toString());
      else if (expectedClass == Integer.class)
        newValue = Integer.valueOf(value.toString());
      else if (expectedClass == Short.class)
        newValue = Short.valueOf(value.toString());
      else if (expectedClass == Byte.class)
        newValue = Byte.valueOf(value.toString());
      else if (expectedClass == BigDecimal.class)
        newValue = new BigDecimal(value.toString());
      else
        newValue = Double.valueOf(value.toString()); //maybe is Number
      return newValue;
    }
    
    throw new ValidateException("Parametro %s: non riesco a convertire %s (%s) in un %s", getName(), value, value.getClass()
        .getName(), expectedClass.getName());
    
    
  }

  public void check() throws ValidateException {
    if (c_value != null && c_className != null) {
      try {
        Class<?> expectedClass = Class.forName(c_className);
        Class<?> actualClass = c_value.getClass();
        //boolean isAbstract = (expectedClass.getModifiers() & Modifier.ABSTRACT) > 0;
        if ( !expectedClass.isAssignableFrom(actualClass)) {
          throw new ValidateException("Il parametro %s e' di classe %s ma gli e' stato assegnato '%s' che e' di classe %s",
              getName(), getClassName(), c_value, c_value.getClass().getName());
        }
      } catch (ClassNotFoundException e) {
        throw new ValidateException(e, "Non ho trovato la classe %s del parametro %s", c_className, getName());
      }
    }
    if (isRequired() && c_value == null)
      throw new ValidateException("Il parametro %s e' obbligatorio", getName());
  }

  public boolean isRequired() {
    return c_required;
  }

  public void setRequired(boolean required) {
    c_required = required;
  }

  public void setClassName(String className) throws ValidateException {
    c_className = className;
  }

  public String getDefaultValue() {
    return c_defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    c_defaultValue = defaultValue;
  }

  public boolean isAssigned() {
    return c_assigned;
  }
}
