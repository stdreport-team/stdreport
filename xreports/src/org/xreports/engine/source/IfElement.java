/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;

/**
 * @author pier
 * 
 */
public class IfElement extends AbstractElement {
  /** nome dell'attributo <b>test</b> del tag "if" */
  private static final String ATTRIB_TEST  = "test";

  /**
   * Indica se il segmento condizionale è stato generato oppure no a seconda che
   * sia 'true' o 'false' il risultato della valutazione
   */
  private boolean             m_generated  = false;

  public IfElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_TEST, String.class, null, TAG_BOOLEAN);
  }

  protected void setGenerated(boolean generated) {
    m_generated = generated;
  }

  /**
   * Indica se il contenuto del tag if è stato generato oppure no, a seconda che
   * sia <tt>true</tt> o <tt>false</tt> il risultato della valutazione.
   * @return <tt>true</tt> se contenuto generato
   * @see #isTrue()
   */
  public boolean isGenerated() {
    return m_generated;
  }

  /**
   * Il simbolo radice dell'albero sintattico dell'espressione booleana definita dentro l'attributo
   * <b>test</b>.
   */
  public Symbol getTestSymbol() {
    return getAttrSymbol(ATTRIB_TEST);
  }

  @Override
  public void fineParsingElemento() {

  }

  /**
   * Indica se questo elemento deve essere generato in output. Se l'attributo
   * <var>test</var> è presente nel tag del sorgente XML, questo metodo torna la
   * valutazione dell'attributo; se non è presente, viene ritornato <b>true</b>
   * 
   * @return true sse questo tag deve venire visualizzato in output
   * 
   * @throws GenerateException
   *           nel caso di valore di ritorno errato della user call
   * @throws EvaluateException
   *           nel caso di errori gravi in chiamata user call
   */
  public boolean isTrue() throws ResolveException {
    Boolean result = getExpressionAsBoolean(ATTRIB_TEST);
    if (result==null) {
      //caso in cui l'attributo test non è specificato: la condizione è sempre false
      return false;
    }
    return result.booleanValue();
  }
  
  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    try {
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLOpenTag());
      }

      salvaStampaGruppo(stampa, gruppo);
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isTrue()) {
        //Elemento mioPadre = (Elemento) padre.contenuto(padre);
        for (IReportNode reportElem : c_elementiFigli) {
          List<Elemento> listaFiglio = reportElem.generate(gruppo, stampa, padre);
          for (Elemento elementoPDF : listaFiglio) {
            elementoPDF.fineGenerazione();
            listaElementi.add(elementoPDF);
          }
        }
        m_generated = true;
      } else {
        m_generated = false;
      }
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLCloseTag());
      }
      return listaElementi;
    } catch (GenerateException e) {
      throw e;
    } catch (Exception e) {
      throw new GenerateException(this, e);
    }
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_IF;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isContentElement()
   */
  @Override
  public boolean isContentElement() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }

  @Override
  public boolean canChildren() {
    return true;
  }
}
