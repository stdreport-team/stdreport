package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.XReport;
import org.xreports.engine.output.BloccoTesto;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;

public class SpanElement extends ChunkElement {

  private String c_testo = null;

  public SpanElement(XReport stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.source.IReportElement#fineParsingElemento()
   */
  @Override
  public void fineParsingElemento() {
  }

  public String getTesto() {
    if (c_testo != null) {
      return c_testo;
    }
    c_testo = "";
    for (IReportNode reportElem : c_elementiFigli) {
      if (reportElem instanceof TextNode) {
        c_testo += ((TextNode) reportElem).getTesto();
      }
    }

    return c_testo;
  }

  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    try {
      salvaStampaGruppo(stampa, gruppo);

      //NB: anche se l'elemento non è visibile, torno comunque una lista, vuota.
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isVisible()) {
        if (isDebugData()) {
          stampa.debugElementOpen(this);
        }
        BloccoTesto bloccoTesto = stampa.getFactoryElementi().creaBloccoTesto(stampa, this, padre);

        for (IReportNode reportElem : c_elementiFigli) {
          reportElem.setDebugData(isDebugData());
          List<Elemento> listaFigli = reportElem.generate(gruppo, stampa, bloccoTesto);
          for (Elemento elementoOutput : listaFigli) {
            elementoOutput.fineGenerazione();
            bloccoTesto.addElement(elementoOutput);
          }
        }
        bloccoTesto.fineGenerazione();
        listaElementi.add(bloccoTesto);
        if (isDebugData()) {
          stampa.debugElementClose(this);
        }
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_SPAN;
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
  public void destroy() {    
    super.destroy();
    c_testo = null;
  }

  @Override
  public boolean canChildren() {
    return true;
  }
  
}
