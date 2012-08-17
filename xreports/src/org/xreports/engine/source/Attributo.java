package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.expressions.parsers.GenericParser;
import org.xreports.expressions.parsers.GenericParser.ParserType;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.ResolveException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;

public class Attributo {
  private String   c_nome             = null;
  private String   c_defaultValue     = null;
  /**
   * valore dell'attributo convertito nella classe corretta: è
   * {@link #c_stringValue} convertito secondo la classe {@link #c_classeDato}
   * e/o secondo {@link #c_expressionTag}
   */
  private Object   c_objValue         = null;
  /**
   * valore dell'attributo in forma stringa: è il valore specificato nel
   * sorgente XML
   */
  private String   c_stringValue      = null;
  private Class<?> c_classeDato       = null;
  
  private Stampa   c_stampa      = null;
  
  /**
   * risultato dell'analisi del parser: è il simbolo top level dell'albero
   * sintattico
   */
  private Symbol   c_expressionValue  = null;
  /**
   * indica il tipo di espressione da sottoporre al parser: vedi
   * {@link AbstractElement#TAG_BOOLEAN} , {@link AbstractElement#TAG_VALUE},
   * {@link AbstractElement#TAG_TEXT}
   */
  private String   c_expressionTag    = null;
  /** indica che questo attributo è una espressione da sottoporre al parser */
  private boolean  c_parseValue       = false;
  private boolean  c_measureLines     = false;
  private boolean  c_measurePercent   = false;
  /** indica se l'attributo esiste nel sorgente XML */
  private boolean  c_existInSource    = false;
  private int      c_borderPosition   = Border.BOX;

  private boolean  c_parsingException = true;

  /**
   * Istanzia attributo con il nome dato. Il secondo parametro indica la classe
   * richiesta per il valore. Se l'attributo non esiste e il valore di default è
   * impostato, gli viene assegnato quello.
   * 
   * @param nome
   *          nome attributo
   * @param classeDato
   *          classe richiesta per il valore
   * @param valoreDefault
   *          valore di default in formato stringa, come se fopsse assegnato nel
   *          XML
   */
  public Attributo(String nome, Class<?> classeDato, String valoreDefault) {
    c_nome = nome;
    c_classeDato = classeDato;
    c_defaultValue = valoreDefault;
  }

  /**
   * Costruttore con in più l'elemento Stampa corrente. Serve per alcuni tipi di
   * attributo, per determinare il valore.
   * 
   * @param nome
   *          nome attributo
   * @param classeDato
   *          classe richiesta per il valore
   * @param valoreDefault
   *          valore di default in formato stringa, come se fopsse assegnato nel
   *          XML
   * @param stampa
   *          oggetto stampa corrente
   * 
   */
  public Attributo(String nome, Class<?> classeDato, String valoreDefault, Stampa stampa) {
    c_nome = nome;
    c_classeDato = classeDato;
    c_defaultValue = valoreDefault;
    c_stampa = stampa;
  }

  /**
   * Costruttore per attributi di tipo bordo. Oltre ai soliti parametri, si deve
   * specificare la posizione del bordo, una delle costanti presenti nella
   * classe {@link Border}.
   * 
   * @param nome
   *          nome attributo
   * @param valoreDefault
   *          valore di default in formato stringa, come se fopsse assegnato nel
   *          XML
   * @param borderPosition
   *          posizione bordo: {@link Border#BOX}, {@link Border#BOTTOM}, ...
   */
  public Attributo(String nome, String valoreDefault, int borderPosition) {
    this(nome, Border.class, valoreDefault);
    c_borderPosition = borderPosition;
  }

  /**
   * Valorizza l'attributo con ciò che è presente nel sorgente XML. Se
   * richiesto, parsa già il contenuto come espressione.
   * 
   * @param attrs
   *          collection attributi del tag corrente; se null, viene caricato
   *          l'attributo con il valore di default
   * @throws ValidateException
   */
  public void load(Attributes attrs) throws ValidateException {
    String val = null;
    if (attrs != null) {
      val = attrs.getValue(c_nome);
    }
    c_objValue = null;
    if (val == null) {
      if (c_defaultValue == null) {
        return;
      }
      val = c_defaultValue;
    } else {
      c_existInSource = true;
    }
    setValue(val);
  }

  public void setValue(String value) throws ValidateException {
    try {
      c_stringValue = value;
      if (c_parseValue) {
        parseSymbol();
        return;
      }
      if (c_classeDato == String.class) {
        c_objValue = c_stringValue;
      } else if (c_classeDato == Integer.class) {
        c_objValue = Integer.valueOf(c_stringValue);
      } else if (c_classeDato == Long.class) {
        c_objValue = Long.valueOf(c_stringValue);
      } else if (c_classeDato == Float.class) {
        c_objValue = Float.valueOf(c_stringValue);
      } else if (c_classeDato == Measure.class) {
        if (c_stringValue != null) {
          c_objValue = new Measure(c_stringValue, c_measureLines, c_measurePercent);
        } else {
          if (c_defaultValue != null) {
            c_objValue = new Measure(c_defaultValue.toString(), c_measureLines, c_measurePercent);
          }
        }
      } else if (c_classeDato == Border.class) {
        if (c_stringValue != null) {
          c_objValue = new Border(c_stringValue, c_borderPosition);
        } else {
          if (c_defaultValue != null) {
            c_objValue = new Border(c_defaultValue.toString(), c_borderPosition);
          }
        }
      } else if (c_classeDato == Margini.class) {
        if (c_stringValue != null) {
          c_objValue = new Margini(c_stringValue);
        } else {
          if (c_defaultValue != null) {
            c_objValue = new Margini(c_defaultValue.toString());
          }
        }
      } else if (c_classeDato == Boolean.class) {
        //NB: non uso la conversione nativa di Boolean, perchè qualsiasi valore diverso da "true" lo considera false,
        //    e io volgio "true"/"false" precisi.
        if (c_stringValue.equalsIgnoreCase("true")) {
          c_objValue = Boolean.TRUE;
        } else if (c_stringValue.equalsIgnoreCase("false")) {
          c_objValue = Boolean.FALSE;
        } else {
          throw new ValidateException("Attributo '" + c_nome + "': il valore " + c_stringValue + " non è di tipo booleano");
        }
      } else if (c_classeDato == Colore.class) {
        if (c_stringValue != null) {
          char first = c_stringValue.charAt(0);
          if (Character.isLetter(first))
            c_objValue = c_stampa.getColorByName(c_stringValue);
          else
            c_objValue = Colore.getInstance("dummyfor" + this.toString(), c_stringValue, 0);
        } else {
          if (c_defaultValue != null) {
            c_objValue = c_stampa.getColorByName(c_defaultValue);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ValidateException("Attributo '" + c_nome 
          + "': il valore " + c_stringValue + " non è un tipo di dato corretto", e);
    }

    if (c_objValue == null && c_defaultValue != null) {
      c_objValue = c_defaultValue;
    }
  }

  private void parseSymbol() throws ValidateException {
    if (c_stringValue != null) {
      c_expressionValue = null;
      ParserType type = null;
      if (c_expressionTag.equalsIgnoreCase(AbstractElement.TAG_VALUE)) {
        type = ParserType.MATH_EXPRESSION;
      } else if (c_expressionTag.equalsIgnoreCase(AbstractElement.TAG_BOOLEAN)) {
        type = ParserType.BOOLEAN_EXPRESSION;
      } else if (c_expressionTag.equalsIgnoreCase(AbstractElement.TAG_TEXT)) {
        type = ParserType.TEXT_EXPRESSION;
      } else {
        type = ParserType.ANY_EXPRESSION;
      }
      GenericParser parser = GenericParser.getInstance(type, c_stringValue);

      try {
        c_expressionValue = parser.parse();
        c_expressionValue.setTag(c_expressionTag);
      } catch (Exception e) {
        if (isParsingException()) {
          throw new ValidateException(e, "attributo '%s': '%s' non è un'espressione valida", c_nome, c_stringValue );
        }
      }
    }
  }

  /**
   * Se l'attributo è una espressione da valutare, viene valutata e ritornato il
   * risultato
   * 
   * @param elem
   *          elemento a cui appartiene l'attributo da valutare
   * @return risultato booleano della valutazione dell'espressione
   * @throws GenerateException
   *           in caso di attributo che non sia espressione da valutare, oppure
   *           di errore in valutazione oppure risultato della valutazione non
   *           booleano
   */
  public Boolean getExpressionAsBoolean(AbstractElement elem) throws ResolveException {
    Object ret = evaluateExpression(elem);
    if (ret == null) {
      return null;
    }
    if (ret instanceof Boolean) {
      return (Boolean) ret;
    }
    throw new ResolveException(elem, "L'espressione specificata per attributo '%s' non e' booleana!", getNome());
  }

  /**
   * Se l'attributo è una espressione da valutare, viene valutata e ritornato il
   * risultato
   * 
   * @param elem
   *          elemento a cui appartiene l'attributo da valutare
   * @return risultato numerico della valutazione dell'espressione
   * @throws GenerateException
   *           in caso di attributo che non sia espressione da valutare, oppure
   *           di errore in valutazione oppure risultato della valutazione non
   *           numerico
   */
  public Number getExpressionAsNumber(AbstractElement elem) throws ResolveException {
    Object ret = evaluateExpression(elem);
    if (ret == null) {
      return null;
    }
    if (ret instanceof Number) {
      return (Number) ret;
    }
    throw new ResolveException(elem, "L'espressione specificata per attributo '%s' non e' numerica!", getNome());
  }
  
  /**
   * Se l'attributo è una espressione da valutare, viene valutata e ritornato il
   * risultato
   * 
   * @param elem
   *          elemento a cui appartiene l'attributo da valutare
   * @return risultato stringa della valutazione dell'espressione
   * 
   * @throws GenerateException
   *           in caso di attributo che non sia espressione da valutare, oppure
   *           di errore in valutazione oppure espressione non analizzata
   *           correttamente
   */
  public String getExpressionAsString(AbstractElement elem) throws ResolveException {
    Object ret = evaluateExpression(elem);
    if (ret == null) {
      return null;
    }
    return ret.toString();
  }

  /**
   * Se l'attributo è una espressione da valutare, viene valutata e ritornato il
   * risultato.
   * 
   * @param elem
   *          elemento a cui appartiene l'attributo da valutare
   * @return risultato della valutazione dell'espressione; se l'attributo non
   *         esiste e non è stato specificato alcun valore di default, ritorna
   *         <b>null</b>
   * 
   * @throws GenerateException
   *           in caso di attributo che non sia espressione da valutare, oppure
   *           di errore in valutazione oppure espressione non analizzata
   *           correttamente
   */
  private Object evaluateExpression(AbstractElement elem) throws ResolveException {
    if ( !c_parseValue) {
      throw new ResolveException(elem, "L'attributo '%s' non supporta la valutazione di espressioni", getNome());
    }
    try {
      if (isNull()) {
        //attributo non specificato: lascio gestire al chiamante
        return null;
      }

      if (getExpressionValue() != null) {
        return getExpressionValue().evaluate(elem);
      }
      throw new ResolveException(elem, "L'espressione specificata per attributo '%s' non e' stata correttamente analizzata!", getNome());
    } catch (ResolveException e) {
      throw e;
    } catch (Exception e) {
      throw new ResolveException(elem, e, "Errore in valutazione dell'espressione %s specificata per attributo '%s'", c_stringValue, getNome());
    }
  }

  /**
   * Ritorna true sse l'attributo esiste nel sorgente.
   */
  public boolean exists() {
    return c_existInSource;
  }

  /**
   * Ritorna true sse l'attributo non esiste nel sorgente e non è stato passato
   * alcun valore di default.
   */
  public boolean isNull() {
    return c_stringValue == null;
  }

  /**
   * @return nome attributo
   */
  public String getNome() {
    return c_nome;
  }

  /**
   * Ritorna il valore dell'attributo nella classe specificata all'atto della
   * creazione di questo oggetto. Se l'attributo non esiste nel sorgente ed è
   * stato fornito un valore di default, torna quello.
   */
  public Object getObjValue() {
    return c_objValue;
  }

  /**
   * @return valore di default impostato nel costruttore.
   */
  public String getValoreDefault() {
    return c_defaultValue;
  }

  /**
   * @param valore
   *          di default da impostare
   */
  public void setValoreDefault(String valDefault) {
    c_defaultValue = valDefault;
  }

  /**
   * @return il valore dell'attributo in formato stringa, esattamente come è nel
   *         sorgente XML
   */
  public String getText() {
    return c_stringValue;
  }

  /**
   * @return classe a cui bisogna convertire il valore
   */
  public Class<?> getClasseDato() {
    return c_classeDato;
  }

  /**
   * @param classe
   *          a cui bisogna convertire il valore
   */
  public void setClasseDato(Class<?> cClasseDato) {
    c_classeDato = cClasseDato;
  }

  /**
   * @return true se questo attributo è una espressione da parsare
   * @see #setTaggedExpression(boolean, String)
   */
  public boolean isExpression() {
    return c_parseValue;
  }

  /**
   * Ritorna il tag assegnato con la
   * {@link #setTaggedExpression(boolean, String)}
   */
  public String getExpressionTag() {
    return c_expressionTag;
  }

  /**
   * Imposta questa attributo come espressione da parsare. Una volta caricato
   * l'attributo dal parser XML, il contenuto stringa viene parsato e se il
   * parsing è OK, viene assegnato al Symbol top level il tag qui passato
   * 
   * @param parseValue
   *          true se questo attributo contiene il testo di una espressione da
   *          parsare
   * @param tagToAssign
   *          tag da assegnare al Symbol top level dopo il parsing; può essere
   *          anche null.
   */
  public void setTaggedExpression(boolean parseValue, String tagToAssign) {
    c_parseValue = parseValue;
    c_expressionTag = tagToAssign;
  }

  /**
   * Ritorna il simbolo top level dell'espressione parsata. E' null se il
   * parsing effettuato in precedenza è andato male o se non è mai stato
   * effettuato
   */
  public Symbol getExpressionValue() {
    return c_expressionValue;
  }

  @Override
  public String toString() {
    return c_nome + " (" + c_classeDato.toString() + ") = " + c_stringValue;
  }

  /**
   * @return posizione border del valore
   */
  public int getBorderPosition() {
    return c_borderPosition;
  }

  /**
   * @return il c_measureLines
   */
  public boolean isMeasureLines() {
    return c_measureLines;
  }

  /**
   * @param cMeasureLines
   *          the c_measureLines to set
   */
  public void setMeasureLines(boolean cMeasureLines) {
    c_measureLines = cMeasureLines;
  }

  /**
   * @return il c_measurePercent
   */
  public boolean isMeasurePercent() {
    return c_measurePercent;
  }

  /**
   * @param cMeasurePercent
   *          the c_measurePercent to set
   */
  public void setMeasurePercent(boolean cMeasurePercent) {
    c_measurePercent = cMeasurePercent;
  }

  /**
   * Indica se, nel caso l'attributo sia un'espressione da analizzare
   * sintatticamente, l'errore di parsing deve dare eccezione o meno. Il default
   * è che manda un'eccezione.
   * 
   * @return true se l'errore sintattico dell'espressione deve causare una
   *         eccezione
   */
  public boolean isParsingException() {
    return c_parsingException;
  }

  /**
   * Indica se, nel caso l'attributo sia un'espressione da analizzare
   * sintatticamente, l'errore di parsing deve dare eccezione o meno. Il default
   * è che manda un'eccezione: chiamando <tt>setParsingException(false)</tt> il
   * parsing non da eccezione.
   * 
   * @param parsingException
   *          true (default) sse l'errore di parsing di questo attributo, che
   *          deve essere un'espressione da analizzare, deve dare eccezione o
   *          meno
   */
  public void setParsingException(boolean parsingException) {
    c_parsingException = parsingException;
  }
}
