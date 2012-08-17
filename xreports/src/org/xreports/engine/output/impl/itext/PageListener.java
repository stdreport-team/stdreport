package ciscoop.stampa.output.impl.itext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.PageElement;
import org.xreports.engine.source.PageFooter;
import org.xreports.engine.source.PageHeader;
import org.xreports.expressions.symbols.EvaluateException;


import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * 
 * @author pier
 */
public class PageListener extends PdfPageEventHelper {
  private Map<String, LineaIText>             m_linee         = new HashMap<String, LineaIText>();
  private Map<String, ChartIText>             m_charts        = new HashMap<String, ChartIText>();
  private MarginboxIText                      m_marginbox;
  private RulersIText                         m_rulers;
  private List<WatermarkIText>                m_watermarks;
  private XReport                              m_stampa;
  private Document                            c_document      = null;

  private int                                 c_pageNumber    = 0;

  /**
   * Contiene i valori correnti di tutti i field del report marcati con 'trace'.
   * <ul>
   * <li><b>key</b>: valore attributo 'trace' del FieldElement che ha generato
   * il BloccoTestoIText</li>
   * <li><b>value</b>: valore corrente del campo, cioè l'ultimo stampato</li>
   * </ul>
   */
  private Map<String, String>                 c_currentValues = new HashMap<String, String>();

  /**
   * Contiene i valori correnti di inizio pagina di tutti i field del report
   * marcati con 'trace'.
   * <ul>
   * <li><b>key</b>: valore attributo 'trace' del FieldElement che ha generato
   * il BloccoTestoIText</li>
   * <li><b>value</b>: valore corrente del campo, cioè l'ultimo stampato</li>
   * </ul>
   */
  private Map<String, String>                 c_firstValues   = new HashMap<String, String>();

  /**
   * Contiene l'associazione attributo 'trace' -> BloccoTestoIText
   * <ul>
   * <li><b>key</b>: valore attributo 'trace' del FieldElement che ha generato
   * il BloccoTestoIText</li>
   * <li><b>value</b>: BloccoTestoIText utilizzato per emettere il field</li>
   * </ul>
   */
  private Map<String, List<BloccoTestoIText>> c_tags          = new HashMap<String, List<BloccoTestoIText>>();

  private Map<String, Bookmark>               c_bookmarks     = new HashMap<String, Bookmark>();

  private PageHeader                          c_pageHeader;
  private PageFooter                          c_pageFooter;

  public PageListener(XReport stampa) {
    m_stampa = stampa;
    c_pageHeader = stampa.getPageHeader();
    c_pageFooter = stampa.getPageFooter();
  }

  public void addLine(String name, LineaIText linea) {
    m_linee.put(name, linea);
  }

  public void setMarginbox(MarginboxIText marginbox) {
    m_marginbox = marginbox;
  }

  public void setRulers(RulersIText rulers) {
    m_rulers = rulers;
  }

  public void addWatermark(WatermarkIText w) {
    if (m_watermarks == null)
      m_watermarks = new ArrayList<WatermarkIText>();
    m_watermarks.add(w);
  }

  public void addField(BloccoTestoIText blocco, String fieldName) {
    List<BloccoTestoIText> blocchi = c_tags.get(fieldName);
    if (blocchi == null) {
      blocchi = new ArrayList<BloccoTestoIText>();
    }
    blocchi.add(blocco);
    c_tags.put(fieldName, blocchi);
    ((Chunk) blocco.getContent(null)).setGenericTag(fieldName);
  }

  public void addBookmark(BloccoTestoIText blocco, Bookmark b) {
    c_bookmarks.put(blocco.getUniqueID(), b);
  }

  /**
   * Consuma il blocco di testo (chunk) di nome <i>field</i>. Di chunk con tag
   * <i>field</i> ce ne possono essere più di uno: consuma il primo chunk con
   * tale tag della lista.
   * 
   * @param field
   *          nome del generic tag del chunk da "consumare"
   */
  private void consumaBlocco(String field) {
    List<BloccoTestoIText> blocchi = c_tags.get(field);
    if (blocchi != null && blocchi.size() > 0) {
      BloccoTestoIText del = blocchi.remove(0);
      c_currentValues.put(field, ((Chunk) del.getContent(null)).getContent());
      if ( !c_firstValues.containsKey(field)) {
        c_firstValues.put(field, ((Chunk) del.getContent(null)).getContent());
      }
    }
  }

  @Override
  public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {
    //System.out.println("onParagraph " + paragraphPosition);
  }

  public void addChart(String name, ChartIText linea) {
    m_charts.put(name, linea);
  }

  /**
   * @see com.itextpdf.text.pdf.PdfPageEvent#onEndPage(com.itextpdf.text.pdf.PdfWriter,
   *      com.itextpdf.text.Document)
   */
  @Override
  public void onEndPage(PdfWriter writer, Document document) {
    try {
      //System.out.println("onEndPage, " + getCurrentPageNumber() + "," + writer.getCurrentPageNumber());  		
      DocumentoIText doc = (DocumentoIText) m_stampa.getDocumento();
      c_document = doc.getDocument();
      writeHeaderFooter(writer.getDirectContent(), c_pageHeader);
      writeHeaderFooter(writer.getDirectContent(), c_pageFooter);
      if (m_marginbox != null) {
        m_marginbox.draw(writer);
      }
      if (m_rulers != null) {
        m_rulers.draw(writer);
      }
      if (m_watermarks != null) {
        for (WatermarkIText wi : m_watermarks)
          wi.draw(writer);
      }
    } catch (GenerateException e) {
      e.printStackTrace();
      m_stampa.addErrorMessage("Errore grave in stampa page footer/header", e);
    }
  }

  public int getCurrentPageNumber() {
    return c_pageNumber;
  }

  public String getCurrentValue(String nomeCampo) {
    return c_currentValues.get(nomeCampo);
  }

  public String getCurrentStartValue(String nomeCampo) {
    if (c_firstValues.containsKey(nomeCampo)) {
      return c_firstValues.get(nomeCampo);
    }

    return c_currentValues.get(nomeCampo);
  }

  @Override
  public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
    try {
      consumaBlocco(text);

      LineaIText linea = m_linee.get(text);
      if (linea != null) {
        linea.draw(rect);
      }
      ChartIText ch = m_charts.get(text);
      if (ch != null) {
        ch.draw(rect);
      }
      Bookmark elem = c_bookmarks.get(text);
      if (elem != null) {
        c_bookmarks.remove(text);
        DocumentoIText doc = (DocumentoIText) m_stampa.getDocumento();
        doc.writeBookmark(elem, rect.getTop());
      }
    } catch (GenerateException e) {
      e.printStackTrace();
    } catch (EvaluateException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onStartPage(PdfWriter writer, Document document) {
    c_pageNumber++;
    //System.out.println("onStartPage " + c_pageNumber);
    c_firstValues.clear();
  }

  /*
   * private void writeFooter(List<Elemento> elementi, PdfContentByte cb) throws
   * GenerateException { PdfPTable footer = buildTable(elementi); //float hei =
   * footer.getTotalHeight(); //altezza dell'intestazione float x =
   * c_document.leftMargin(); float hei = footer.getTotalHeight(); //altezza del
   * footer VAlign vAlign = c_pageFooter.getValign(); if (vAlign == null) {
   * vAlign = VAlign.BOTTOM; } //calcolo scostamento per centrare in altezza il
   * page footer float dist = 0; //distanza verticale da margine float botmar =
   * c_document.bottomMargin(); if (hei < botmar) { dist = (botmar - hei) / 2; }
   * else { m_stampa.addWarningMessage("Pagina " + getCurrentPageNumber() +
   * ": il page footer (h=" + hei +
   * ") è più alto del margine inferiore della pagina (h=" + botmar + ")"); dist
   * = botmar; } float y = 0; switch (vAlign) { case BOTTOM: { y += hei; }
   * break; case MIDDLE: { y += hei + dist; } break; case TOP: { y +=
   * c_document.bottomMargin(); } break; } footer.writeSelectedRows(0, -1, x, y,
   * cb); } private void writeHeader(List<Elemento> elementi, PdfContentByte cb)
   * throws GenerateException { //left = leftmargin //right = PageSize.width() -
   * rightMargin //top = PageSize.height() - topMargin PdfPTable header =
   * buildTable(elementi); float hei = header.getTotalHeight(); //altezza
   * dell'intestazione float x = c_document.leftMargin(); //document.top() è la
   * coordinata y sotto il margine, cioè se l'altezza della pagina è //800 punti
   * e il margine è 30 punti, document.top() = 800 - 30 = 770 float topmar =
   * c_document.topMargin(); VAlign vAlign = c_pageHeader.getValign(); if
   * (vAlign == null) { vAlign = VAlign.BOTTOM; } //calcolo scostamento per
   * centrare in altezza il page header float dist = 0; //distanza verticale da
   * margine if (hei < topmar) { dist = (topmar - hei) / 2; } else {
   * m_stampa.addWarningMessage("Pagina " + getCurrentPageNumber() +
   * ": il page header (h=" + hei +
   * ") è più alto del margine superiore della pagina (h=" + topmar + ")"); dist
   * = topmar; } float y = c_document.top(); switch (vAlign) { case BOTTOM: { y
   * += hei; } break; case MIDDLE: { y += hei + dist; } break; case TOP: { y +=
   * c_document.topMargin(); } break; } header.writeSelectedRows(0, -1, x, y,
   * cb); }
   */
  private void writeHeaderFooter(PdfContentByte cb, PageElement pageElem) throws GenerateException {
    try {
      if (pageElem == null)
        return;
      List<Elemento> elementi = pageElem.generate(m_stampa.getGruppoRadice(), m_stampa, m_stampa.getDocumento());
      PdfPTable element = buildTable(elementi);

      //llx = leftmargin
      float llx;
      float lly;

      if (pageElem.isHeader()) {
        llx = c_document.leftMargin();
        lly = c_document.getPageSize().getHeight() - c_document.topMargin() + pageElem.getHeight().getValue();
      } else {
        llx = c_document.leftMargin();
        lly = c_document.bottomMargin();
      }
      element.writeSelectedRows(0, -1, llx, lly, cb);
    } catch (Exception e) {
      throw new GenerateException(pageElem, e, "Errore imprevisto in generazione header/footer");
    }
  }

  /**
   * Costruisce una PdfPTable con tante colonne quanti sono gli elementi passati
   * in input.
   * 
   * @param figli
   *          elementi da mettere nella tabella che viene creata
   * @return PdfPTable costruita
   * @throws GenerateException
   */
  private PdfPTable buildTable(List<Elemento> figli) throws GenerateException {
    PdfPTable header = new PdfPTable(figli.size());
    header.setTotalWidth(c_document.right() - c_document.left());
    header.setLockedWidth(true);
    boolean bFirst = true;
    for (Elemento figlio : figli) {
      Object obj = figlio.getContent(null);
      if (obj instanceof PdfPTable && bFirst) {
        ((PdfPTable) obj).setSpacingBefore(0);
      }
      bFirst = false;
      PdfPCell cell = new PdfPCell();
      cell.setBorder(Rectangle.NO_BORDER);
      if (obj instanceof Element) {
        if (obj instanceof Paragraph) {
          //Pier: per qualche misteriosa ragione non vengono visualizzate le tabelle
          //da quando hanno come surrounding element il paragrafo: quindi in tal caso metto la tabella
          //escludendo il paragrafo in cui sono state create 
          Paragraph para = (Paragraph) obj;
          if (para.size() == 1) {
            if (para.getContent().length() == 0 && para.get(0) instanceof PdfPTable) {
              cell.addElement(para.get(0));
            } else {
              cell.addElement(para);
            }
          } else {
            cell.addElement(para);
          }
        } else {
          cell.addElement((Element) obj);
        }
      } else if (obj instanceof PdfPTable) {
        //header.addCell((PdfPTable) obj );
      }
      header.addCell(cell);
    }
    return header;
  }

  /**
   * Costruisce una PdfPTable con tante colonne quanti sono gli elementi passati
   * in input.
   * 
   * @param figli
   *          elementi da mettere nella tabella che viene creata
   * @return PdfPTable costruita
   * @throws GenerateException
   */
  /*
   * private Element buildPageElement(List<Elemento> figli) throws
   * GenerateException { PdfPTable table; if (figli.size() == 1) { return
   * (Element) figli.get(0).getContent(null); } else { table = new
   * PdfPTable(figli.size()); table.setTotalWidth(c_document.right() -
   * c_document.left()); for (Elemento figlio : figli) { Object obj =
   * figlio.getContent(null); PdfPCell cell = new PdfPCell();
   * cell.setBorder(Rectangle.NO_BORDER); if (obj instanceof Element) { if (obj
   * instanceof Paragraph) { //Pier: per qualche misteriosa ragione non vengono
   * visualizzate le tabelle //da quando hanno come surrounding element il
   * paragrafo: quindi in tal caso metto la tabella //escludendo il paragrafo in
   * cui sono state create Paragraph para = (Paragraph) obj; if (para.size() ==
   * 1) { if (para.getContent().length() == 0 && para.get(0) instanceof
   * PdfPTable) { cell.addElement(para.get(0)); } else { cell.addElement(para);
   * } } else { cell.addElement(para); } } else { cell.addElement((Element)
   * obj); } } else if (obj instanceof PdfPTable) { //header.addCell((PdfPTable)
   * obj ); } table.addCell(cell); } } return table; }
   */
  public void destroy() {
    if (m_linee != null) {
      m_linee.clear();
      m_linee = null;
    }
    if (m_charts != null) {
      m_charts.clear();
      m_charts = null;
    }
    if (c_currentValues != null) {
      c_currentValues.clear();
      c_currentValues = null;
    }
    if (c_firstValues != null) {
      c_firstValues.clear();
      c_firstValues = null;
    }
    if (c_tags != null) {
      c_tags.clear();
      c_tags = null;
    }

    m_marginbox = null;
    m_rulers = null;
    m_stampa = null;
    c_document = null;
  }
}
