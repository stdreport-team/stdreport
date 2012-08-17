/**
 * 
 */
package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Colore;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.Watermark;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;
import org.xreports.util.Text;

/**
 * @author pier
 * 
 */
public class WatermarkElement extends AbstractElement {
	/** Nomi degli attributi di questo tag */
  public static final String	ATTRIB_TEXT							= "text";
	private static final String	ATTRIB_IMAGE						= "image";
	private static final String	ATTRIB_ROTATION					= "rotation";
  private static final String ATTRIB_FITTEXT          = "fitText";
	private static final String	ATTRIB_HEIGHT						= "height";
	private static final String	ATTRIB_WIDTH						= "width";
  private static final String ATTRIB_USEMARG          = "useMargins";
  private static final String ATTRIB_LEADING          = "leading";
  private static final String ATTRIB_COLOR            = "color";
  private static final String ATTRIB_REFCOLOR         = "refColor";
	
	public WatermarkElement(Stampa stampa, Attributes attrs, int lineNum, int colNum)
			throws ValidateException {
		super(stampa, attrs, lineNum, colNum);
	}

	@Override
	protected void initAttrs() {
		super.initAttrs();
		addAttributo(ATTRIB_TEXT, String.class, "", TAG_TEXT);
		addAttributo(ATTRIB_IMAGE, String.class, "");
		addAttributo(ATTRIB_ROTATION, Float.class, "0");
		getAttributo(ATTRIB_HALIGN).setValoreDefault("center");
    addAttributo(ATTRIB_FITTEXT, Boolean.class, "false");
    addAttributo(ATTRIB_USEMARG, Boolean.class, "true");
    addAttributoColore(ATTRIB_COLOR, Colore.NERO.getName());
    addAttributoColore(ATTRIB_REFCOLOR, Colore.NERO.getName());
		
		addAttributoMeasure(ATTRIB_WIDTH, null, true, false);
		addAttributoMeasure(ATTRIB_HEIGHT, null, true, false);
    addAttributoMeasure(ATTRIB_LEADING, null, false, true);
	}

	@Override
	protected void loadAttributes(Attributes attrs) throws ValidateException {
	  super.loadAttributes(attrs);
	  if (existAttr(ATTRIB_IMAGE) && existAttr(ATTRIB_TEXT)) {
	    throw new ValidateException(this, "Non puoi specificare sia 'text' che 'image'");
	  }
    if (existAttr(ATTRIB_COLOR) && existAttr(ATTRIB_REFCOLOR)) {
      throw new ValidateException(this, "Non puoi specificare sia 'color' che 'refColor'");
    }
	}
	
	@Override
	public void fineParsingElemento() {
	}

	@Override
	public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre)
			throws GenerateException {
		try {
			if (isDebugData()) {
				System.out.println("[STRUTTURA] " + this.toString());
			}
			salvaStampaGruppo(stampa, gruppo);
			List<Elemento> listaElementi = new LinkedList<Elemento>();
			if (isVisible()) {
				Watermark wmark = stampa.getFactoryElementi().creaWatermark(stampa,
						this, padre);
				wmark.fineGenerazione();
				listaElementi.add(wmark);
			}
			if (isDebugData()) {
				System.out.println("[STRUTTURA] >> FINE " + this.toString());
			}
			return listaElementi;
		} catch (Exception e) {
			throw new GenerateException("Errore grave in generazione "
					+ this.toString() + ":  " + e.getMessage(), e);
		}
	}

	/**
	 * Restituisce valore attributo {@value #ATTRIB_FITTEXT}.
	 * @return true sse la dimensione della font del watermark testo deve essere calcolata
	 * in base alla dimensione del foglio
	 */
	public boolean isFitText() {
	  return getAttrValueAsBoolean(ATTRIB_FITTEXT);
	}

  /**
   * Restituisce valore attributo {@value #ATTRIB_USEMARG}.
   * @return true sse il watermark deve essere dentro i margini
   */
  public boolean isUseMargins() {
    return getAttrValueAsBoolean(ATTRIB_USEMARG);
  }
	
	/**
	 * Restituisce altezza immagine ({@link #ATTRIB_HEIGHT}).
	 */
	public Measure getHeight() {
		Measure m = getAttrValueAsMeasure(ATTRIB_HEIGHT);
		return m;
	}

  /**
   * Restituisce interlinea testo ({@link #ATTRIB_LEADING}).
   */
  public Measure getLeading() {
    Measure m = getAttrValueAsMeasure(ATTRIB_LEADING);
    return m;
  }
	
	/**
	 * Restituisce larghezza immagine ({@link #ATTRIB_WIDTH}).
	 * 
	 */
	public Measure getWidth() {
		Measure m = getAttrValueAsMeasure(ATTRIB_WIDTH);
		return m;
	}

	public String getText(Stampa stp) throws EvaluateException {	 
		if (Text.isValue(getAttributeText(ATTRIB_TEXT))) {
	    Symbol s = getAttrSymbol(ATTRIB_TEXT);
	    if (s != null) {
	      setStampa(stp);
	      return String.valueOf(s.evaluate(this));
	    }
	    return null;		  
		}
		return null;
	}

	public String getImage() {
		if (existAttr(ATTRIB_IMAGE))
			return getAttrValue(ATTRIB_IMAGE).toString();
		return null;
	}

	public float getRotation() {
  	return getAttrValueAsFloat(ATTRIB_ROTATION);
	}
	
	
	public Colore getColor() {
    if (existAttr(ATTRIB_COLOR)) {
      return getAttrValueAsColore(ATTRIB_COLOR);
    }
    else if (existAttr(ATTRIB_REFCOLOR)) {
      return getAttrValueAsColore(ATTRIB_REFCOLOR);
    } 
    return null;
	}
	
	@Override
	public String getTagName() {
		return XMLSchemaValidationHandler.ELEMENTO_WATERMARK;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
	 */
	@Override
	public boolean isConcreteElement() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.stampa.source.AbstractElement#isContentElement()
	 */
	@Override
	public boolean isContentElement() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
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
