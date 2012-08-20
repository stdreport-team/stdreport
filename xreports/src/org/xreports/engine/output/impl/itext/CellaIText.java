package org.xreports.engine.output.impl.itext;

import java.util.List;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Cella;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement.HAlign;
import org.xreports.engine.source.AbstractElement.VAlign;
import org.xreports.engine.source.Border;
import org.xreports.engine.source.Border.BorderStyle;
import org.xreports.engine.source.CellElement;
import org.xreports.engine.source.TableElement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;

public class CellaIText extends ElementoIText implements Cella {
  /**
   * interlinea di default in una cella: utilizzato per impostare interlinea di
   * eventuale paragrafo fittizio: l'interlinea viene calcolata moltiplicando
   * misura della sua font per questo valore.
   */
  public static final float DEFAULT_CELL_LEADING = 1.3f;

  /** Elemento itext per rendere la cella */
  private PdfPCell          c_cella              = null;
  protected CellElement     c_elemCella;
  /** flag usato per sapere se sto aggiungendo il primo figlio a questa cella */
  private boolean           bFirstChild          = true;

  /** indice 0-based della colonna rispetto all'interno della sua riga */
  private int               c_colIndex           = -1;
  /** indice 0-based della riga a cui appartiene la colonna */
  private int               c_rowIndex           = -1;

  private float             c_minHeight          = 0;
  private float             c_fixedHeight        = 0;
  private float             c_height             = 0;
  /**
   * paragrafo fittizio nel caso la cella contenga solo un chunk: è necessario
   * per fare ereditare al paragrafo l'allineamento orizzontale dalla cella
   */
  Paragraph                 c_autoParag          = null;

  public CellaIText(XReport report, CellElement cellElem, Elemento parent) throws GenerateException {
    super(report, parent);
    c_elemCella = cellElem;
  }

//  private Paragraph createDefaultParagraph() throws GenerateException {
//    Paragraph p = new Paragraph();
//    p.setAlignment(c_cella.getHorizontalAlignment());
//    //le linee successive sono necessarie: senza di esse il paragrafo c_autoParag
//    //avrebbe una interlinea diversa (troppo alta) dagli altri paragrafi non fittizi, ciò
//    //causerebbe delle celle inutilmente alte e il non funzionamento dell'attributo valign nelle celle
//    DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
//    p.setLeading(doc.getDefaultFont().getCalculatedLeading(DEFAULT_CELL_LEADING));
//    p.setSpacingAfter(0);
//    p.setSpacingBefore(0);
//    return p;
//  }

  //  public void aggiungiElementoOLD(Elemento figlio) throws GenerateException {
  //    Object obj = figlio.contenuto(this);
  //    // paragrafo fittizio nel caso la cella contenga solo un chunk: è necessario per fare ereditare al paragrafo l'allineamento
  //    // orizzontale dalla cella     
  //    Paragraph c_autoParag = null;
  //    if (obj != null) {
  //      if (bFirstChild) {
  //        bFirstChild = false;
  //        if (figlio instanceof BloccoTestoIText) {
  //          c_autoParag = createDefaultParagraph();
  //          c_cella.addElement(c_autoParag);
  //        } else if (figlio instanceof ParagrafoIText) {
  //          ParagrafoIText para = (ParagrafoIText) figlio;
  //          if (para.getMarginTop() > 0 && para.getMarginTop() > c_cella.getPaddingTop()) {
  //            //nel caso il paragrafo iniziale della cella abbia il marginTop, non funziona:
  //            //quindi copio il marginTop nel paddingTop della cella e sono a posto!
  //            //NB: il marginBottom funziona
  //            c_cella.setPaddingTop(para.getMarginTop());
  //          }
  //        }
  //      }
  //      if (c_autoParag != null) {
  //        c_autoParag.add((Element) obj);
  //      } else {
  //        c_cella.addElement((Element) obj);
  //      }
  //      ElementoIText elItext = (ElementoIText) figlio;
  //      if (elItext.getHeight() > c_height) {
  //        if (c_fixedHeight == 0 && c_minHeight < c_height) {
  //          c_height = elItext.getHeight();
  //          c_minHeight = c_height;
  //          c_cella.setMinimumHeight(c_height);
  //        }
  //      }
  //    }
  //  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    creaPdfCell(getReport(), c_elemCella);
    Object obj = figlio.getContent(this);
    if (obj != null) {
      if (bFirstChild) {
        bFirstChild = false;
        //tratto in modo speciale il primo figlio se è un paragrafo
        if (figlio instanceof ParagrafoIText) {
          ParagrafoIText para = (ParagrafoIText) figlio;
          if (para.getMarginTop() > 0 && para.getMarginTop() > c_cella.getPaddingTop()) {
            //nel caso il paragrafo iniziale della cella abbia il marginTop, non funziona:
            //quindi copio il marginTop nel paddingTop della cella e sono a posto!
            //NB: il marginBottom funziona
            c_cella.setPaddingTop(para.getMarginTop());
          }
        }
      }
      ElementoIText elItext = (ElementoIText) figlio;
/*      
      if (elItext.isBlock()) {
        c_cella.addElement((Element) obj);
        //se l'elemento è block-level, annullo il paragrafo fittizio:
        //se in seguito arriveranno altri elementi inline, creerò un altro paragrafo fittizio
        c_autoParag = null;
      } else {
        if (c_autoParag == null) {
          c_autoParag = createDefaultParagraph();
          c_cella.addElement(c_autoParag);
        }
        c_autoParag.add((Element) obj);
      }
*/      
      c_cella.addElement((Element) obj);
      if (elItext.getHeight() > c_height) {
        if (c_fixedHeight == 0 && c_minHeight < c_height) {
          c_height = elItext.getHeight();
          c_minHeight = c_height;
          c_cella.setMinimumHeight(c_height);
        }
      }
    }
  }

  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    for (Elemento figlio : figli) {
      addElement(figlio);
    }
  }

  @Override
  public Object getContent(Elemento padre) throws GenerateException {
    creaPdfCell(getReport(), c_elemCella);
    return c_cella;
  }

  /**
   * Crea una cella itext con tutte le necessarie impostazioni per altezza, padding, etc.
   * 
   * @param stampa motore del report
   * @param elemCella corrispondente elemento &lt;cell&gt; nel sorgente
   * @throws GenerateException per errori gravi nelle impostazioni degli oggetti iText
   */
  private void creaPdfCell(XReport stampa, CellElement elemCella) throws GenerateException {
    if (c_cella != null) {
      return;
    }
    c_cella = new PdfPCell();    
    
    //=============== SPAN ==============
    int colspan = elemCella.getColspan();
    if (colspan != 0) {
      c_cella.setColspan(colspan);
    }
    int rowspan = elemCella.getRowspan();
    if (rowspan != 0) {
      c_cella.setRowspan(rowspan);
    }

    TableElement elemTable = elemCella.getTableElement();
    
    //=========== set di rowindex  =======
    c_rowIndex = elemTable.getCurrentRowIndex();
    c_colIndex = elemTable.getCurrentColIndex();
    elemTable.addCellToRowCount(elemCella);
    
    //=============== ALTEZZA ==============
    if (elemCella.getMinHeight() != null) {
      c_minHeight = elemCella.getMinHeight().getValue();
      c_cella.setMinimumHeight(c_minHeight);
    } else if (elemCella.getFixedHeight() != null) {
      c_fixedHeight = elemCella.getFixedHeight().getValue();
      c_cella.setFixedHeight(c_fixedHeight);
    }

    //=============== COLORE ==============
    String backColor = elemCella.getBackgroundColor();
    if (backColor == null) {
      //sulla cella non ho specificato il colore: guardo nella tabella padre
      backColor = elemTable.getBackgroundColor();
      if (backColor != null) {
        //guardo anche se ho specificato colori per le righe pari/dispari
        if (isInOddRow() && elemTable.getBackgroundColorOdd()!=null) {
          //sono su una riga dispari e ho specificato backgroundColorOdd sull'elemento table:
          //lascio gestire a lui il background di questa riga
          backColor = null;
        }
        if (isInEvenRow() && elemTable.getBackgroundColorEven()!=null) {
          //sono su una riga pari e ho specificato backgroundColorEven sull'elemento table:
          //lascio gestire a lui il background di questa riga
          backColor = null;
        }
      }
    }
    if (backColor != null) {
      DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
      if (doc.getColorByName(backColor) != null) {
        c_cella.setBackgroundColor(doc.getColorByName(backColor));
      }
    }

    //=============== ALLINEAMENTI ==============
    VAlign valignEnum = elemCella.getValign();
    if (valignEnum != null) {
      c_cella.setVerticalAlignment(getVAlignForItext(valignEnum));
    }
    c_cella.setHorizontalAlignment(Element.ALIGN_LEFT);
    HAlign hAlignEnum = elemCella.getHalign();
    if (hAlignEnum != null) {
      c_cella.setHorizontalAlignment(getHAlignForItext(hAlignEnum));
    }

    if (elemCella.getRotation() > 0) {
      c_cella.setRotation(elemCella.getRotation());
    }

    //=============== BORDO ==============
    c_cella.setUseBorderPadding(true);

    int borderMask = 0;
    borderMask = _handleBorder(elemCella.getBorderTop(), stampa, elemCella, borderMask);
    borderMask = _handleBorder(elemCella.getBorderRight(), stampa, elemCella, borderMask);
    borderMask = _handleBorder(elemCella.getBorderBottom(), stampa, elemCella, borderMask);
    borderMask = _handleBorder(elemCella.getBorderLeft(), stampa, elemCella, borderMask);

    if (borderMask == 0) {
      //se arrivo qui non è stato definito alcun bordo singolo
      borderMask = _handleBorder(elemCella.getBorder(), stampa, elemCella, borderMask);
      if (borderMask == 0) {
        c_cella.setBorder(Rectangle.NO_BORDER);
      }
    }

    //=============== PADDING ==============    
    //NOTA: lascio comunque 1 punto di padding per evitare che le scritte siano
    //non attaccate ai bordi

    c_cella.setPaddingLeft(Math.max(elemCella.getPaddingLeft().getValue(), 0));
    c_cella.setPaddingRight(Math.max(elemCella.getPaddingRight().getValue(), 0));
    c_cella.setPaddingTop(Math.max(elemCella.getPaddingTop().getValue(), 0));
    c_cella.setPaddingBottom(Math.max(elemCella.getPaddingBottom().getValue(), 0));

    //le successive due righe permettono di distanziare meglio il testo dai bordi della cella 
    c_cella.setUseDescender(true);
    c_cella.setUseBorderPadding(true);
  }

//  private boolean isHeaderCell() {
//    int qtaRowsHeader = c_elemCella.getTableElement().getHeaders();
//    return getRowIndex() < qtaRowsHeader;
//  }

  /**
   * Indica se la riga è una riga di dati dispari. La prima riga è considerata dispari.
   * @return true sse non è una riga dell'header ed è di indice dispari.
   */
  private boolean isInOddRow() {
    int qtaRowsHeader = c_elemCella.getTableElement().getHeaders();
    if (getRowIndex() < qtaRowsHeader) {
      return false;
    }
    int rowIndex = getRowIndex() - qtaRowsHeader + 1;
    return rowIndex % 2 == 1;
  }

  /**
   * Indica se la riga è una riga di dati pari. La prima riga è considerata dispari.
   * @return true sse non è una riga dell'header ed è di indice pari.
   */
  private boolean isInEvenRow() {
    int qtaRowsHeader = c_elemCella.getTableElement().getHeaders();
    if (getRowIndex() < qtaRowsHeader) {
      return false;
    }
    int rowIndex = getRowIndex() - qtaRowsHeader + 1;
    return rowIndex % 2 == 0;
  }
  
  /**
   * Disegna e abilita i bordi di una cella itext
   * 
   * @param border
   * @param stampa
   * @param elemCella
   * @return
   * @throws GenerateException
   */
  private int _handleBorder(Border border, XReport stampa, CellElement elemCella, int borderMask) throws GenerateException {
    if (border != null && border.hasBorder()) {
      if (border.getPosition() == Border.BOTTOM) {
        borderMask |= Rectangle.BOTTOM;
      } else if (border.getPosition() == Border.TOP) {
        borderMask |= Rectangle.TOP;
      } else if (border.getPosition() == Border.LEFT) {
        borderMask |= Rectangle.LEFT;
      } else if (border.getPosition() == Border.RIGHT) {
        borderMask |= Rectangle.RIGHT;
      } else if (border.getPosition() == Border.BOX) {
        borderMask = Rectangle.BOX;
      }

      disegnaBordo(border, stampa, elemCella, borderMask);
    }
    return borderMask;
  }

  private void disegnaBordo(Border border, XReport stampa, CellElement elemCella, int borderMask) throws GenerateException {
    int position = border.getPosition();
    float size = border.getSize();
    String szColorName = border.getColorName();

    DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
    if (border.getStyle() == BorderStyle.SOLID) {
      switch (position) {
        case Border.TOP: {
          c_cella.setBorderWidthTop(size);
          BaseColor color = doc.getColorByName(szColorName);
          if (color != null) {
            c_cella.setBorderColorTop(color);
          }
          c_cella.setBorder(borderMask);
        }
          break;
        case Border.RIGHT: {
          c_cella.setBorderWidthRight(size);
          BaseColor color = doc.getColorByName(szColorName);
          if (color != null) {
            c_cella.setBorderColorRight(color);
          }
          c_cella.setBorder(borderMask);
        }
          break;
        case Border.BOTTOM: {
          c_cella.setBorderWidthBottom(size);
          BaseColor color = doc.getColorByName(szColorName);
          if (color != null) {
            c_cella.setBorderColorBottom(color);
          }
          c_cella.setBorder(borderMask);
        }
          break;
        case Border.LEFT: {
          c_cella.setBorderWidthLeft(size);
          BaseColor color = doc.getColorByName(szColorName);
          if (color != null) {
            c_cella.setBorderColorLeft(color);
          }
          c_cella.setBorder(borderMask);
        }
          break;
        case Border.BOX: {
          c_cella.setBorderWidth(size);
          BaseColor color = doc.getColorByName(szColorName);
          if (color != null) {
            c_cella.setBorderColor(color);
          }
          c_cella.setBorder(Rectangle.BOX);
        }
        break;
      }
    } else {
      //se il bordo non è solid, disegno a manina il bordo con la classe CellBorderEvent
      c_cella.setBorder(Rectangle.NO_BORDER);
      c_cella.setCellEvent(new CellBorderEvent(elemCella, doc));
    }
  }

  /**
   * 
   * @return Interlinea della cella
   */
  public float getLeading() {
    if (c_cella != null) {
      return c_cella.getLeading();
    }
    return 0;
  }

  @Override
  public void flush(Documento documento) throws GenerateException {
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Cella" + getElementID();
  }

  @Override
  public float calcAvailWidth() {
    TabellaIText tabella = (TabellaIText) getParent();
    int span = c_cella.getColspan();
    float w = 0;
    for (int i = c_colIndex; i < c_colIndex + span; i++) {
      w += tabella.getColumnWidth(i);
    }
    return w - c_cella.getBorderWidthRight() - c_cella.getBorderWidthLeft() - c_cella.getPaddingRight() - c_cella.getPaddingLeft();
  }

  /**
   * Ritorna l'indice (0-based) di questa colonna all'interno della generica
   * riga. Cioè se questa è la prima colonna delle righe ritorna 0, se è la
   * seconda ritorna 1, etc.
   */
  @Override
  public int getColIndex() {
    return c_colIndex;
  }

  /**
   * Ritorna l'indice (0-based) della riga a cui appartiene questa cella. Cioè
   * se questa cella è nella prima riga ritorna 0, se è nella seconda ritorna
   * 1, etc.
   */
  @Override
  public int getRowIndex() {
    return c_rowIndex;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#getHeight()
   */
  @Override
  public float getHeight() {
    if (c_fixedHeight > 0) {
      return c_fixedHeight;
    } else if (c_height == 0 && c_minHeight > 0) {
      return c_minHeight;
    }
    return c_height;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return true;
  }

}
