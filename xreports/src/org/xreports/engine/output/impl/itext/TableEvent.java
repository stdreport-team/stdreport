/**
 * 
 */
package ciscoop.stampa.output.impl.itext;

import org.xreports.engine.source.Border;
import org.xreports.engine.source.TableElement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;

/**
 * @author pier
 * 
 */
public class TableEvent implements PdfPTableEvent {
  private Border         m_border     = null;
  private BorderIText    m_borderDraw = null;
  private TableElement   m_table      = null;
  private DocumentoIText m_doc        = null;

  /**
   * Costruttore da usare per disegnare i bordi della tabella
   * @param border bordo da disegnare
   * @param doc documento da utilizzare, serve ad esempio per reperire i colori
   */
  public TableEvent(Border border, DocumentoIText doc) {
    m_border = border;
    m_borderDraw = new BorderIText(doc);
  }

  /**
   * Costruttore da usare per colorare lo sfondo delle righe pari-dispari.
   * @param table elemento table da usare per avere le proprietà necessarie per il disegno
   * @param doc documento da utilizzare, serve ad esempio per reperire i colori
   */
  public TableEvent(TableElement table, DocumentoIText doc) {
    m_table = table;
    m_doc = doc;
  }

  /**
   * Questo evento viene innescato da iText ogni volta che una tabella deve
   * essere disegnata su una pagina. Quindi se una tabella si espande su più
   * pagine, viene innescata una volta per pagina.
   * 
   * @see com.itextpdf.text.pdf.PdfPTableEvent#tableLayout(com.itextpdf.text.pdf.PdfPTable,
   *      float[][], float[], int, int, com.itextpdf.text.pdf.PdfContentByte[])
   */
  @Override
  public void tableLayout(PdfPTable p_table, float[][] widths, float[] heights, int headerRows, int rowStart,
      PdfContentByte[] canvas) {

    if (m_borderDraw != null) {
      drawBorder(canvas[PdfPTable.LINECANVAS], widths, heights);
//      System.out.println("TABLE heights:");
//      for (int i=0; i < heights.length; i++)
//        System.out.println("\t" + heights[i]);
    }

    if (m_table != null) {
      if (m_table.getBackgroundColorEven() != null) {
        drawBackgroundEven(true, p_table, canvas[PdfPTable.BASECANVAS], widths, heights, m_table.getBackgroundColorEven());
      }    
      if (m_table.getBackgroundColorOdd() != null) {
        drawBackgroundEven(false, p_table, canvas[PdfPTable.BASECANVAS], widths, heights, m_table.getBackgroundColorOdd());
      }          
    }    
  }

  private void drawBackgroundEven(boolean even, PdfPTable p_table, PdfContentByte cb, float[][] widths, float[] heights, String color) {
    int columns;
    Rectangle rectAlt;
    BaseColor backColor = m_doc.getColorByName(color);
    int lastRow = widths.length - p_table.getFooterRows() - 1;
    int firstRow = p_table.getHeaderRows() - p_table.getFooterRows();
    if (even) {
      //per le righe pari parto dalla seconda riga
      firstRow++;
    }
    for (int row = firstRow; row <= lastRow; row += 2) {
      columns = widths[row].length - 1;
      rectAlt = new Rectangle(widths[row][0], heights[row], widths[row][columns], heights[row + 1]);
      rectAlt.setBackgroundColor(backColor);
      rectAlt.setBorder(Rectangle.NO_BORDER);
      cb.rectangle(rectAlt);
    }
  }

  private void drawBorder(PdfContentByte cb, float[][] widths, float[] heights) {
    //    //NB: disegno il bordo 1 punto fuori dalla tabella + la dimensione del bordo    
    //    float bsize = m_border != null ? m_border.getSize() : 0;

    //    dist += 1;
    float dist = 0;

    //differenza fra altezza prima riga e ultima riga
    float tblHeight = heights[0] - heights[heights.length - 1];
    //differenza fra coordinata prima cella (della prima riga) e ultima cella (della prima riga)
    float tblWidth = widths[0][widths[0].length - 1] - widths[0][0];

    float llx = widths[0][0] - dist;
    float lly = heights[heights.length - 1] - dist;
    Rectangle rectBorder = new Rectangle(llx, lly, llx + tblWidth + dist * 2, lly + tblHeight + dist * 2);
    m_borderDraw.draw(m_border, rectBorder, cb);
  }

}
