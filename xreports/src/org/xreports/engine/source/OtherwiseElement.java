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
public class OtherwiseElement extends WhenElement {

  public OtherwiseElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
    parseAttributes(attrs);
    /** otherwise è sempre a true se viene valutato */
    setGenerated(true);
  }

  private void parseAttributes(Attributes attrs) {

  }

  @Override
  public void fineParsingElemento() {

  }

  @Override
  public String getXMLOpenTag() {
    String szXML = "<" + XMLSchemaValidationHandler.ELEMENTO_OTHERWISE;
    szXML += getXMLAttrs();
    szXML += ">";
    return szXML;
  }

  @Override
  public String getXMLAttrs() {
    String szXMLAttrs = super.getXMLAttrs();

    return szXMLAttrs;
  }

  @Override
  public String getXMLCloseTag() {
    return "</" + XMLSchemaValidationHandler.ELEMENTO_OTHERWISE + ">";
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_OTHERWISE;
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLOpenTag());
      }

      salvaStampaGruppo(stampa, gruppo);
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      for (IReportNode reportElem : c_elementiFigli) {
        List<Elemento> listaFiglio = reportElem.generate(gruppo, stampa, padre);
        for (Elemento elementoPDF : listaFiglio) {
          elementoPDF.fineGenerazione();
          listaElementi.add(elementoPDF);
        }
      }
      if (isDebugData()) {
        stampa.addToDebugFile("\n" + getXMLCloseTag());
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

}
