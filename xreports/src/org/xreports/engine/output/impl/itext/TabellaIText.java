package ciscoop.stampa.output.impl.itext;

import java.util.List;

import ciscoop.expressions.symbols.EvaluateException;
import org.xreports.engine.Stampa;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Tabella;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement.HAlign;
import org.xreports.engine.source.Border;
import org.xreports.engine.source.Measure;
import org.xreports.engine.source.TableElement;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class TabellaIText extends ElementoIText implements Tabella {
  /**
   * paragrafo che contiene la vera tabella: si utilizza per avere qualche
   * caratteristica aggiuntiva che non hanno le tabelle in iText, come i margini
   * sinistro e destro
   */
  private Paragraph    paragOuter         = null;
  private PdfPTable    tabella            = null;

  private float        cellWidths[]       = null;
  private int          c_colCount         = 0;

  private float        c_height           = 0.0f;
  private float        c_currentRowHeight = 0.0f;

  private int          c_colIndexCounter  = 0;
  private int          c_rowIndexCounter  = -1;

  private TableElement c_tableElem        = null;

  public TabellaIText(XReport stampa, TableElement tableElem, Elemento padre) throws GenerateException {
    c_tableElem = tableElem;
    setParent(padre);
    creaPdfTable(stampa, tableElem);
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    Object obj = figlio.getContent(this);
    if (obj != null) {
      if (obj instanceof PdfPCell) {
        synchronized (this) {
          PdfPCell cell = (PdfPCell) obj;
          tabella.addCell(cell);
          if (cell.getHeight() > c_currentRowHeight) {
            c_currentRowHeight = cell.getHeight();
          }
          //per ogni cella aggiunta calcolo a quante colonne corrisponde
          //e aggiorno l'indice corrente c_colIndexCounter
          if (c_colIndexCounter == 0) {
            //sono arrivato sulla prima colonna della riga: incremento indice di riga
            c_rowIndexCounter++;
            c_height += c_currentRowHeight;
            c_currentRowHeight = 0;
          }
          c_colIndexCounter += cell.getColspan() % c_colCount;
        }
      } else {
        throw new GenerateException(c_tableElem, "Non posso aggiungere come figlio " + figlio);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    for (Elemento figlio : figli) {
      addElement(figlio);
    }
  }

  @Override
  public Object getContent(Elemento padre) throws GenerateException {
    //IMPORTANTE: sembra che mettere dentro una cella un paragrafo con una tabella,
    //  non sia supportato da iText: semplicemente la tabella sparisce!
    //  Quindi se la tabella è dentro una cella, ritorno il suo PdfPTable.

    if (padre instanceof CellaIText)
      return tabella;

    return paragOuter;
  }

  /**
   * Ritorna la quantità effettiva di colonne di questa tabella
   */
  public int getColCount() {
    return c_colCount;
  }

  public float getColumnWidth(int index) {
    if (cellWidths == null) {
      //nessuna specifica di larghezza: tutte le celle sono larghe uguali
      if (c_colCount > 0) {
        return calcAvailWidth() / c_colCount;
      }
      else {
        return 0;
      }
    }
    else {
      float totalRelative = 0;
      for (float col : cellWidths) {
        totalRelative += col;
      }
      return calcAvailWidth() * cellWidths[index] / totalRelative;      
    }
  }

  private void creaPdfTable(XReport stampa, TableElement tableElem) throws GenerateException {
    try {
      paragOuter = new Paragraph();
      paragOuter.setIndentationLeft(tableElem.getSpazioSinistra());

      float[] widths = tableElem.getWidths();
      if (widths != null) {
        tabella = new PdfPTable(widths);
        cellWidths = widths;
        c_colCount = widths.length;
      } else {
        int cols = evaluateColCount(tableElem);
        if (cols == 0) {
          cols = tableElem.getChildCells();
        }
        if (cols <= 0) {
          throw new GenerateException(tableElem, "Non trovo celle dentro la tabella.");
        }
        tabella = new PdfPTable(cols);
        c_colCount = cols;
      }
      paragOuter.add(tabella);
      //la cella di default è senza bordi
      tabella.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
      tabella.getDefaultCell().setUseBorderPadding(true);

      tabella.setSplitLate(true);
      if (tableElem.getExtendToBottom()) {
        tabella.setExtendLastRow(true);
      }

      //=============== ALLINEAMENTO ==============
      HAlign hAlignEnum = tableElem.getHalign();
      if (hAlignEnum != null) {
        tabella.setHorizontalAlignment(getHAlignForItext(hAlignEnum));
      }

      if (tableElem.getHeaders() > 0) {
        tabella.setHeaderRows(tableElem.getHeaders());
      }

      //keeptogether=true --> non spezza la tabella su più pagine, ma impone una nuova pagina prima della tabella
      tabella.setKeepTogether(tableElem.isKeepTogether());

      //=============== LARGHEZZE ==============
      DocumentoIText docIText = (DocumentoIText) stampa.getDocumento();
      Document doc = docIText.getDocument();
      float availWidth = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
      Measure width = tableElem.getWidth();
      if (width.isPercent()) {
        tabella.setWidthPercentage(width.getValue());
      } else {
        tabella.setWidthPercentage( (width.getValue() / availWidth) * 100);
      }

      //=============== COLORI DI SFONDO ==============
      if (tableElem.getBackgroundColorEven() != null || tableElem.getBackgroundColorOdd() != null) {
        _handleBackColor(tableElem, stampa);
      }
      
      
      //=============== BORDI E SPAZI ==============
      _handleBorder(tableElem.getBorder(), stampa);

      _handleBorder(tableElem.getBorderTop(), stampa);
      _handleBorder(tableElem.getBorderBottom(), stampa);
      _handleBorder(tableElem.getBorderLeft(), stampa);
      _handleBorder(tableElem.getBorderRight(), stampa);

      Measure spacingBefore = tableElem.getSpacingBefore();
      if (spacingBefore != null) {
        tabella.setSpacingBefore(spacingBefore.getValue());
      }
      Measure spacingAfter = tableElem.getSpacingAfter();
      if (spacingAfter != null) {
        tabella.setSpacingAfter(spacingAfter.getValue());
      }
    } catch (GenerateException gex) {
      throw gex;
    } catch (Exception e) {
      throw new GenerateException(tableElem, e);
    }
  }

  private void _handleBorder(Border border, Stampa stampa) throws GenerateException {
    if (border != null && border.hasBorder()) {
      tabella.setTableEvent(new TableEvent(border, (DocumentoIText) stampa.getDocumento()));
    }
  }

  private void _handleBackColor(TableElement elem, Stampa stampa) throws GenerateException {
    tabella.setTableEvent(new TableEvent(elem, (DocumentoIText) stampa.getDocumento()));
  }
  
  private int evaluateColCount(TableElement tableElem) throws EvaluateException {
    if (tableElem.getColsSymbol() == null) {
      return 0;
    }
    Object colValue = tableElem.getColsSymbol().evaluate(tableElem);
    if (colValue instanceof Number) {
      return ((Number) colValue).intValue();
    }

    throw new EvaluateException(this.toString() + ": non riesco a valutare il campo 'cols'.");

  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#flush(ciscoop.stampa.output.Documento)
   */
  @Override
  public void flush(Documento p_doc) throws GenerateException {

  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Table" + getElementID();
  }

  @Override
  public float calcAvailWidth() {
    float maxWidth = ((ElementoIText) getParent()).calcAvailWidth();
    float wPercent = tabella.getWidthPercentage();
    return maxWidth * wPercent / 100;
  }

  @Override
  public float getHeight() {
    return c_height;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#fineGenerazione()
   */
  @Override
  public void fineGenerazione() throws GenerateException {
    super.fineGenerazione();
    if (tabella != null) {
      tabella.completeRow();
    }
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
