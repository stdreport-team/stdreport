package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Tabella;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.validation.ValidateException;
import org.xreports.engine.validation.XMLSchemaValidationHandler;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.NumberLiteral;
import org.xreports.expressions.symbols.Symbol;

public class TableElement extends AbstractElement {
  /** Nomi della controparte XML degli attributi dell'elemento "table" */
  private static final String ATTRIB_COLS = "cols";
  private static final String ATTRIB_WIDTH = "width";
  private static final String ATTRIB_WIDTHS = "widths";
  private static final String ATTRIB_HEIGHT = "heigth";
  private static final String ATTRIB_EXTEND = "extendToBottom";
  private static final String ATTRIB_CELLPADDING = "cellPadding";
  private static final String ATTRIB_CELLPADDING_LEFT = "cellPaddingLeft";
  private static final String ATTRIB_CELLPADDING_RIGHT = "cellPaddingRight";
  private static final String ATTRIB_CELLPADDING_TOP = "cellPaddingTop";
  private static final String ATTRIB_CELLPADDING_BOTTOM = "cellPaddingBottom";
  private static final String ATTRIB_HEADERS = "headers";
  private static final String ATTRIB_GRID = "grid";
  private static final String ATTRIB_BACKGROUNDCOLOR = "backgroundColor";
  private static final String ATTRIB_BACKGROUNDCOLOR_ODD = "backgroundColorOdd";
  private static final String ATTRIB_BACKGROUNDCOLOR_EVEN = "backgroundColorEven";
  private static final String ATTRIB_KEEP_TOGETHER = "keepTogether";
	
  public static final String DEFAULT_BORDER = "0";
  public static final String DEFAULT_MARGIN_BOTTOM = "2";
  public static final String DEFAULT_MARGIN_TOP = "2";

  private float attrib_widths[];
  
  private int    m_currentRowIndex; 
  private int    m_currColIndex; 
  

//  private int c_colIndexCounter = 0;
  
  
  public TableElement(Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(attrs, lineNum, colNum);
  }

	@Override
  protected void initAttrs() {
  	super.initAttrs();
  	addAttributo(ATTRIB_COLS, String.class, null, TAG_VALUE);
  	addAttributo(ATTRIB_HEADERS, Integer.class, "0");
  	addAttributo(ATTRIB_MARGIN_LEFT, Measure.class, "0");
  	addAttributo(ATTRIB_WIDTHS, String.class);
    
  	addAttributoMeasure(ATTRIB_WIDTH, "100%", true, false);
    
  	addAttributo(ATTRIB_HEIGHT, Float.class, "0");

		addAttributoBorder(ATTRIB_GRID, DEFAULT_BORDER, Border.BOX);
  	
		addAttributoBorder(ATTRIB_BORDER, DEFAULT_BORDER, Border.BOX);
		addAttributoBorder(ATTRIB_BORDERLEFT, DEFAULT_BORDER, Border.LEFT);
		addAttributoBorder(ATTRIB_BORDERRIGHT, DEFAULT_BORDER, Border.RIGHT);
		addAttributoBorder(ATTRIB_BORDERTOP, DEFAULT_BORDER, Border.TOP);
		addAttributoBorder(ATTRIB_BORDERBOTTOM, DEFAULT_BORDER, Border.BOTTOM);

    addAttributo(ATTRIB_BACKGROUNDCOLOR, String.class, null);
    addAttributo(ATTRIB_BACKGROUNDCOLOR_ODD, String.class, null);
    addAttributo(ATTRIB_BACKGROUNDCOLOR_EVEN, String.class, null);
		
  	addAttributo(ATTRIB_CELLPADDING, Measure.class, "0");
    addAttributo(ATTRIB_CELLPADDING_LEFT, Measure.class, "0");
    addAttributo(ATTRIB_CELLPADDING_RIGHT, Measure.class, "0");
    addAttributo(ATTRIB_CELLPADDING_BOTTOM, Measure.class, "0");
    addAttributo(ATTRIB_CELLPADDING_TOP, Measure.class, "0");
		
  	addAttributo(ATTRIB_MARGIN_TOP, Measure.class, DEFAULT_MARGIN_TOP);
  	addAttributo(ATTRIB_MARGIN_BOTTOM, Measure.class, DEFAULT_MARGIN_BOTTOM);

  	addAttributo(ATTRIB_EXTEND, Boolean.class, "false");  	
    addAttributo(ATTRIB_KEEP_TOGETHER, Boolean.class, "false");    
  }

	/* (non-Javadoc)
	 * @see ciscoop.stampa.source.AbstractElement#loadAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void loadAttributes(Attributes attrs) throws ValidateException {
		super.loadAttributes(attrs);
    attrib_widths = identificaWidths(getAttributeText(ATTRIB_WIDTHS));		
    Measure mw = getAttrValueAsMeasure(ATTRIB_WIDTH);
    if (mw.isPercent() && mw.getValue() > 100 ) {
    	throw new ValidateException(this, "Non puoi specificare una larghezza superiore a 100%.");
    }
	}

	/**
	 * Parsa e gestiche il contenuto dell'attributo <tt>widths</tt>.
	 * @param szValoreAttributo valore attributo letto dal XML parser
	 * @return array di float con la specifica delle dimensioni definite in widths
	 * @throws ValidateException 
	 */
	private float[] identificaWidths(String szValoreAttributo) throws ValidateException {
    if( szValoreAttributo == null ) {
      return null;
    }
    try {
      Scanner scanner = new Scanner(szValoreAttributo);
      scanner.useDelimiter(" ");
      List<Float> fl = new LinkedList<Float>();
      while (scanner.hasNext()) {
        fl.add(Float.valueOf(scanner.next().toString()));
      }
      float[] arrayWidths = new float[fl.size()];
      for( int i=0; i < fl.size(); i++ ) {      
        arrayWidths[i] = fl.get(i);
      }
      return arrayWidths;
    } catch (Exception e) {
      throw new ValidateException(this, "errore in analisi attributo widths", e);
    }
  }

	public int getChildCells() {
		return _childCells(this);
	}
	
	/**
	 * Ritorna la quantità di elementi CellElement figli o discendenti dell'elemento dato.
	 * <br>
	 * Naviga la struttura in basso ricorsivamente e si ferma quando trova un elemento TableElement.
	 * @param elem elemento di partenza
	 * @return quantità di celle discendenti, ma non appartenenti ad un'altro elemento table.
	 */
	public int _childCells(AbstractElement elem) {
		int cells = 0;
		for (IReportNode node : elem.getChildren()) {
			if (node instanceof CellElement) {
				cells++;
			}
			else if (node.isElement() && !(node instanceof TableElement)) {
				cells += _childCells((AbstractElement)node);
			}
		}
		return cells;		
	}
	
	
  /**
   * Ritorna true solo se la quantità di colonne di questa tabella è fissa.
   * Se torna false, la quantità di colonne è determinata a run-time e può essere diversa
   * per ogni istanza generata di questa tabella.
   * @return true sse qta colonne è fissa a design-time
   */
  public boolean isColsFixed() {
  	 if( attrib_widths != null ) {
     	return true;
     }  	
  	 return getColsSymbol() instanceof NumberLiteral;
  }

  /**
   * Ritorna il simbolo in cima all'albero sintattico ottenuto dal parsing dell'attributo <var>cols</var>.
   * Serve per la successiva valutazione dell'espressione in fase di generazione.
   * @return simbolo radice dell'espressione parsata dell'attributo <var>cols</var>
   */
  public Symbol getColsSymbol() {
  	return getAttrSymbol(ATTRIB_COLS);
  }
  
  /**
   * Ritorna la quantità di colonne di questa tabella.
   * La quantità è dedotta o dagli attributi <b>cols</b> o <b>widths</b>.
   * Se nè <b>cols</b> nè <b>widths</b> sono definiti, ritorna 0.
   * @throws EvaluateException per errori di valutazione del campo (nel caso di espressioni)
   */
  public int getColCount() throws EvaluateException {
//  	if (!isColsFixed())
//  		throw new EvaluateException(this.toString() + ", getColCount(): la quantità di colonne è variabile");  	
    if( attrib_widths != null ) {
    	return attrib_widths.length;
    }
    if (getColsSymbol() == null) {
      return 0;
    }
   	Object colValue = getColsSymbol().evaluate(this);
   	if (colValue instanceof Number)
   		return ((Number)colValue).intValue();
   	
   	throw new EvaluateException(this.toString() + ": non riesco a valutare il campo 'cols'.");
  }

  public Border getGridBorder() {
		return getAttrValueAsBorder(ATTRIB_GRID);
  }
  
  public Border getBorder() {
		return getAttrValueAsBorder(ATTRIB_BORDER);
  }

  public Border getBorderLeft() {
		Border b = getAttrValueAsBorder(ATTRIB_BORDERLEFT);
    if( b != null ) {
      return b;
    }
    return getAttrValueAsBorder(ATTRIB_BORDER);
  }

  public Border getBorderRight() {
		Border b = getAttrValueAsBorder(ATTRIB_BORDERRIGHT);
    if( b != null ) {
      return b;
    }
    return getAttrValueAsBorder(ATTRIB_BORDER);
  }

  public Border getBorderTop() {
		Border b = getAttrValueAsBorder(ATTRIB_BORDERTOP);
    if( b != null ) {
      return b;
    }
    return getAttrValueAsBorder(ATTRIB_BORDER);
  }

  public Border getBorderBottom() {
		Border b = getAttrValueAsBorder(ATTRIB_BORDERBOTTOM);
    if( b != null ) {
      return b;
    }
    return getAttrValueAsBorder(ATTRIB_BORDER);
  }

  public boolean getExtendToBottom() {
    return getAttrValueAsBoolean(ATTRIB_EXTEND);
  }

  public boolean isKeepTogether() {
    return getAttrValueAsBoolean(ATTRIB_KEEP_TOGETHER);
  }
  
  
  /**
   * @return il nome del colore di sfondo della tabella
   */
  public String getBackgroundColor() {
    return getAttributeText(ATTRIB_BACKGROUNDCOLOR);
  }
  
  /**
   * @return il nome del colore di sfondo delle righe dispari
   */
  public String getBackgroundColorOdd() {
    return getAttributeText(ATTRIB_BACKGROUNDCOLOR_ODD);
  }

  /**
   * @return il nome del colore di sfondo delle righe pari
   */
  public String getBackgroundColorEven() {
    return getAttributeText(ATTRIB_BACKGROUNDCOLOR_EVEN);
  }
  
  /**
   * Ritorna la larghezza della tabella.
   * L'oggetto ritornato non è mai null, nel caso la larghezza non sia specificata
   * nell'attributo width, ritorna il default che è "100%".
   */
  public Measure getWidth() {
    return getAttrValueAsMeasure(ATTRIB_WIDTH);
  }

  public float getTableHeigth() {
    return getAttrValueAsFloat(ATTRIB_HEIGHT);
  }

  public Measure getCellPadding() {
    return getAttrValueAsMeasure(ATTRIB_CELLPADDING);
  }

  public Measure getCellPaddingTop() {
    return getAttrValueAsMeasure(ATTRIB_CELLPADDING_TOP);
  }
  public Measure getCellPaddingBottom() {
    return getAttrValueAsMeasure(ATTRIB_CELLPADDING_BOTTOM);
  }
  public Measure getCellPaddingLeft() {
    return getAttrValueAsMeasure(ATTRIB_CELLPADDING_LEFT);
  }
  public Measure getCellPaddingRight() {
    return getAttrValueAsMeasure(ATTRIB_CELLPADDING_RIGHT);
  }
  
  public Measure getSpacingBefore() {
    return getAttrValueAsMeasure(ATTRIB_MARGIN_TOP);
  }

  public Measure getSpacingAfter() {
    return getAttrValueAsMeasure(ATTRIB_MARGIN_BOTTOM);
  }
  
  public float[] getWidths() {
    return attrib_widths;
  }
  public void setWidths(float[] widths) {
    attrib_widths = widths;
  }
  
  public void setRefFont(String refFont) throws ValidateException {
    setAttributeValue(ATTRIB_REFFONT, refFont);
  }

  @Override
  public List<Elemento> generate(Group gruppo, XReport stampa, Elemento padre) throws GenerateException {
	  try {
    	salvaStampaGruppo(stampa, gruppo);
			List<Elemento> listaElementi = new LinkedList<Elemento>();
			if (isVisible()) {
	      if (isDebugData()) {
	        stampa.debugElementOpen(this);
	      }
			  Tabella tabella = stampa.getFactoryElementi().creaTabella(stampa, this, padre);
			  for (IReportNode reportElem : c_elementiFigli ) {
			    List<Elemento> listaFigli = reportElem.generate(gruppo, stampa, tabella); 
			    tabella.addElements(listaFigli);
			  }    	
			  tabella.fineGenerazione();
			  listaElementi.add(tabella);
	      if (isDebugData()) {
	        stampa.debugElementClose(this);
	      }
			}
			
			return listaElementi;
		} catch (ResolveException e) {
			throw new GenerateException(this, e, "Errore grave in generazione table");
		}
  }
  
	@Override
	public void fineParsingElemento() {
	  
	}
	
	/**
	 * @return la quantità di righe da considerare headers di tabella
	 */
	public int getHeaders() {
		return getAttrValueAsInteger(ATTRIB_HEADERS);
	}


	/**
	 * Ritorna il margine sinistro in punti.
	 * Ritorna 0 se non è stato definito alcun margine sinistro.
	 */
	public float getSpazioSinistra() {
		return getAttrValueAsMeasure(ATTRIB_MARGIN_LEFT).getValue();
	}

	@Override
	public String getTagName() {
		return XMLSchemaValidationHandler.ELEMENTO_TABLE;
	}  
  
  /* (non-Javadoc)
   * @see ciscoop.stampa.source.AbstractElement#isConcreteElement()
   */
  @Override
  public boolean isConcreteElement() {
    return true;
  }

  /**
   * Utilizzato durante la scrittura finale delle celle,
   * mantiene la riga corrente (0-based) durante la fase di generazione. 
   * @return riga a cui la generazione è arrivata fino a questo momento
   */
  public int getCurrentRowIndex() {
    return m_currentRowIndex;
  }


  /**
   * Utilizzato durante la scrittura finale delle celle,
   * mantiene l'indice di colonna corrente (0-based) durante la fase di generazione. 
   * @return colonna a cui la generazione è arrivata fino a questo momento
   */
  public int getCurrentColIndex() {
    return m_currColIndex;
  }

  /**
   * Incrementa {@link #getCurrentColIndex()} e {@link #getCurrentRowIndex()}
   * in base all'elemento cella passato
   * @param cell cella in questione
   */
  public void addCellToRowCount(CellElement cell) {
    int span = cell.getColspan();
    m_currColIndex += span;
    try {
      int cols = getColCount();
      if (m_currColIndex >= cols) {
        m_currColIndex -= cols;
        m_currentRowIndex++;
      }
    } catch (EvaluateException e) {
      e.printStackTrace();
    }
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
    return true;
  }

  @Override
  public boolean canChildren() {
    return true;
  }
}
