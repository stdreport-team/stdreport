/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;

/**
 * Classe intermedia da cui vengono ereditate le classi PageHeader/PageFooter, che implementano i corrispondenti tag. Qui c'è la
 * gestione comune alle due classi.
 * 
 * @author pier
 * 
 */
public abstract class PageElement extends AbstractElement {
  public static final String ATTR_HEIGHT = "height";

  public PageElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  public void fineParsingElemento() {

  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributoMeasure(ATTR_HEIGHT, null, true, false);
  }
  
  public Measure getHeight() {
    return getAttrValueAsMeasure(ATTR_HEIGHT);
  }
  
  
  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    if (isDebugData()) {
      System.out.println("[STRUTTURA] " + this.toString());
    }
    List<Elemento> listaFigli = new LinkedList<Elemento>();

    for (IReportNode reportElem : c_elementiFigli) {
      reportElem.setDebugData(isDebugData());
      //    	if (reportElem instanceof IReportElement) {
      //    		AbstractElement elem = (AbstractElement)reportElem;
      //    		if (elem.getHalign()==null && getHalign()!=null)
      //    			elem.setHalign(getHalign());
      //    		if (elem.getRefFontName()==null)
      //    			elem.setRefFontName(StileCarattere.SYSTEM_DEFAULT_NAME);
      //    	}
      for (Elemento elem : reportElem.generate(gruppo, stampa, padre)) {
        elem.fineGenerazione();
        listaFigli.add(elem);
      }
    }

    if (isDebugData()) {
      System.out.println("[STRUTTURA] >> FINE " + this.toString());
    }
    return listaFigli;
  }

  /* (non-Javadoc)
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
    return false;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return false;
  }
  
  public abstract boolean isHeader();
  public abstract boolean isFooter();
  
}
