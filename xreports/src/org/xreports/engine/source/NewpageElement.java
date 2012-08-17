package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.XReport;
import org.xreports.engine.XReport.GenerationStatus;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;
import org.xreports.expressions.symbols.EvaluateException;

public class NewpageElement extends AbstractElement {
  private static final String ATTRIB_IF   = "if";

  public NewpageElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_IF, String.class, null, TAG_BOOLEAN);
  }  
  
  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
    salvaStampaGruppo(stampa, gruppo);
    if (isDebugData()) {
      stampa.addToDebugFile("\n" + getXMLOpenTag());
    }
    try {
      if (isIf()) {
        stampa.setGenerationStatus(GenerationStatus.MET_NEWPAGE);      
      }
    } catch (EvaluateException e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ": " + e.toString(), e);    }

    if (isDebugData()) {
      stampa.addToDebugFile(getXMLCloseTag());
    }
    return new LinkedList<Elemento>();
  }

  @Override
  public void fineParsingElemento() {
  }

  @Override
  public String getXMLOpenTag() {
    String szXML = "<" + XMLSchemaValidationHandler.ELEMENTO_NEWPAGE;
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
    return "</" + XMLSchemaValidationHandler.ELEMENTO_NEWPAGE + ">";
  }

  @Override
  public String getTagName() {
    return "newpage";
  }

  /**
   * Ritorna il valore dell'attributo <b>if</b>.
   * @throws GenerateException 
   * @throws EvaluateException 
   */
  public boolean isIf() throws GenerateException, EvaluateException {
    if (isAttrNull(ATTRIB_IF)) {
      // caso in cui l'attributo if non è specificato: è sempre true
      return true;
    }
    if (getAttrSymbol(ATTRIB_IF) != null) {
      Object ret = getAttrSymbol(ATTRIB_IF).evaluate(this);
      if (ret instanceof Boolean) {
        return ((Boolean) ret).booleanValue();
      }
    }
    throw new GenerateException(this, "L'espressione specificata per attributo 'if' non è booleana!");
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
    return false;
  }
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isBlockElement()
   */
  @Override
  public boolean isBlockElement() {
    return true;
  }
  
  @Override
  public boolean canChildren() {
    return false;
  }  
}
