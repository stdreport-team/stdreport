/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

/**
 * @author pier
 * 
 */
public class ChooseElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi del Tag "choose" */

  public ChooseElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    parseAttributes(attrs);
  }

  private void parseAttributes(Attributes attrs) {

  }

  @Override
  public void fineParsingElemento() {

  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLOpenTag());
      }

      salvaStampaGruppo(stampa, gruppo);
      List<Elemento> listaElementi = new LinkedList<Elemento>();

      /**
       * Ciclo sulle condizioni When! Ricordiamoci che Otherwise è considerata una sottoclasse di When e può apparire solo in fondo
       */
      for (IReportNode reportElem : c_elementiFigli) {
        if ( ! (reportElem instanceof WhenElement)) {
          continue;
        }
        WhenElement whenElement = (WhenElement) reportElem;
        List<Elemento> listaFiglio = reportElem.generate(gruppo, stampa, padre);
        if (whenElement.isGenerated()) {
          for (Elemento elementoPDF : listaFiglio) {
            elementoPDF.fineGenerazione();
            listaElementi.add(elementoPDF);
          }
          /** Appena genero una condizione salto le altre! */
          break;
        }
      }
      //listaElementi.add(padre);

      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLCloseTag());
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  @Override
  public String getXMLAttrs() {
    String szXMLAttrs = super.getXMLAttrs();

    return szXMLAttrs;
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_CHOOSE;
  }

  /* (non-Javadoc)
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
