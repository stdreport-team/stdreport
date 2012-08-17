package ciscoop.stampa.output.impl.itext;

import java.util.List;

import org.xreports.engine.Stampa;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Linea;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.LineElement;
import org.xreports.engine.source.Measure;
import org.xreports.engine.source.AbstractElement.HAlign;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class LineaIText extends ElementoIText implements Linea {

  public enum Style {
    SOLID, DASH, DOT, DASHDOT
  }

  // posizione verticale della linea

  private float           c_marginBottom   = 0;
  private float           c_deltaY;
  private float           c_deltaX;
  private boolean         c_absolutePos  = false;

  /** altezza della linea: comprende margini e spessore linea */
  private float           c_height;

  private Chunk           outerChunk     = null;
  private Paragraph       outerPara      = null;

  /** lunghezza della linea */
  private Measure         c_length;

  /** spessore della linea */
  private float           c_spessore;
  /** qta di linee da stampare */
  private int             c_count;
  /** qta max di linee stampabili */
  public static final int MAX_LINE_COUNT = 4;
  /** colore della linea */
  private BaseColor       c_color;
  /** stile del tratto della linea */
  private Style           c_style        = Style.SOLID;

  /** allineamento orizzontale della linea rispetto al contenitore */
  private int             c_halign       = Element.ALIGN_CENTER;

  private PdfWriter       c_writer;
  private Document        c_document;
  private Stampa          c_stampa;
  private LineElement     c_lineElem;

  public LineaIText(XReport stampa, LineElement lineElem, Elemento padre) throws GenerateException {
    setParent(padre);
    c_stampa = stampa;
    c_lineElem = lineElem;

    //prendo gli attributi dal tag
    c_length = lineElem.getLength();
    c_spessore = lineElem.getThickness();
    c_count = lineElem.getCount();
    String style = lineElem.getStyle();
    if (style.equalsIgnoreCase(LineElement.STYLE_SOLID)) {
      c_style = Style.SOLID;
    } else if (style.equalsIgnoreCase(LineElement.STYLE_DASH)) {
      c_style = Style.DASH;
    } else if (style.equalsIgnoreCase(LineElement.STYLE_DOT)) {
      c_style = Style.DOT;
    } else if (style.equalsIgnoreCase(LineElement.STYLE_DASHDOT)) {
      c_style = Style.DASHDOT;
    } else {
      throw new GenerateException(lineElem, "Stile linea non previsto: " + style);
    }

    HAlign halign = lineElem.getHalign();
    if (halign == HAlign.LEFT) {
      c_halign = Element.ALIGN_LEFT;
    } else if (halign == HAlign.CENTER) {
      c_halign = Element.ALIGN_CENTER;
    } else if (halign == HAlign.RIGHT) {
      c_halign = Element.ALIGN_RIGHT;
    } else {
      throw new GenerateException(lineElem, "Allineamento orizzontale linea non previsto: " + halign);
    }

    //colore: se non specificato uso il nero, altrimenti deve
    //essere uno dei colori predefiniti nel documento
    String colore = lineElem.getColor();
    if (colore == null || colore.length() == 0) {
      c_color = BaseColor.BLACK;
    } else {
      DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
      c_color = doc.getColorByName(colore);
      if (c_color == null) {
        throw new GenerateException(lineElem, "Colore linea non definito: " + colore);
      }
    }

    //qui salvo i valori che servono poi al momento del disegno della linea
    c_marginBottom = lineElem.getMarginBottom();
    c_deltaY = lineElem.getY();
    c_deltaX = lineElem.getX();
    c_absolutePos = lineElem.isAbsolutePosition();

    //NOTA BENE: genero un chunk con generic tag per poter avere l'evento opportuno nel PageListener:
    //in questo modo ho le cordinate x,y per poter disegnare la linea nella posizione giusta.
    //racchiudo il chunk in un paragrafo perchè:
    //  1) sono sicuro di partire a disegnare la linea a capo
    //  2) posso controllare la distanza della linea dal testo sopra e sotto impostando
    //     l'interlinea del paragrafo (setLeading).
    // La linea verrà disegnata sulla baseline del chunk creato, quindi spostando l'interlinea del paragrafo posto
    // anche la linea

    outerChunk = new Chunk(" ");
    outerChunk.setGenericTag(getUniqueID());
    outerPara = new Paragraph();
    float leading = lineElem.getThickness() + lineElem.getMarginTop() + lineElem.getMarginBottom();
    if (leading < 1) {
      leading = 1; //mi assicuro almeno 1 punto di interlinea
    }
    outerPara.setLeading(leading);
    c_height = leading;
    outerPara.add(outerChunk);
    DocumentoIText doc = (DocumentoIText) stampa.getDocumento();
    doc.getPageListener().addLine(getUniqueID(), this);
  }

  @Override
  public float calcAvailWidth() {
    return 0;
  }

  @Override
  public float getHeight() {
    return c_height;
  }

  /**
   * Effettua fisicamente il disegno della linea secondo i parametri impostati
   * 
   * @throws GenerateException
   */
  public void draw(Rectangle rect) throws GenerateException {
    DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
    c_writer = doc.getWriter();
    c_document = doc.getDocument();
    if (c_writer == null) {
      throw new GenerateException(c_lineElem, "Il PDF writer è null: impossibile disegnare la linea.");
    }
    if (c_document == null) {
      throw new GenerateException(c_lineElem, "Il documento è null: impossibile disegnare la linea.");
    }
    // determino la larghezza massima disponibile nel foglio
    Rectangle drawRect = rect;

    // coordinate x minima e massima per il disegno della linea
    float minimumX = 0;

    //larghezza disponibile per il disegno della linea
    float availWidth = 0;

    ElementoIText parent = (ElementoIText) getParent();
    availWidth = parent.calcAvailWidth();
    if (parent instanceof DocumentoIText) {
      minimumX = c_document.leftMargin();
    } else {
      minimumX = rect.getLeft();
    }

    // determino la lunghezza della linea.
    // se necessario, viene accorciata
    float width = c_length.scale(availWidth);
    if (width > availWidth || width <= 0) {
      width = availWidth;
    }

    //=================================================
    // determino startX, la coordinata orizzontale di partenza della linea
    float spaceHorizBefore = availWidth - width;
    if (spaceHorizBefore > 0) {
      if (c_halign == Element.ALIGN_CENTER) {
        spaceHorizBefore = spaceHorizBefore / 2;
      } else if (c_halign == Element.ALIGN_LEFT) {
        spaceHorizBefore = 0;
      }
    } else {
      spaceHorizBefore = 0;
    }
    float startX = 0;
    float startY = 0;
    if (c_absolutePos) {
      //la posizione assoluta si intende rispetto al documento			
      startY = c_document.getPageSize().getHeight() - c_deltaY;
      startX = c_deltaX;
    } else {
      startX = spaceHorizBefore + minimumX + c_deltaX;
      //=================================================
      // determino startY, la coordinata verticale di partenza della linea a partire dal rect che mi hanno passato
      startY = drawRect.getBottom();

      //NB: in iText le coordinate verticali scendono andando in basso, al contrario
      //    del piano cartesiano e del HTML
      startY -= c_deltaY;

      //se ho un margine inferiore, sposto la linea in su di tale valore
      startY += c_marginBottom;

    }

    //=================================================
    //finalmente scrivo la linea
    PdfContentByte cb = c_writer.getDirectContentUnder();
    cb.saveState();
    applyStyle(cb);
    for (int i = 0; i < c_count; i++) {
      //System.out.println("disegno linea da " + startX + "," + (startY - i * c_spessore * 2));
      cb.moveTo(startX, startY - i * c_spessore * 2);
      cb.lineTo(startX + width, startY - i * c_spessore * 2);
    }

    //cb.rectangle(drawRect.getLeft(), drawRect.getTop() - drawRect.getHeight(), drawRect.getWidth(), drawRect.getHeight());
    //System.out.println("top=" + drawRect.getTop() + ", bottom=" + drawRect.getBottom() + ", h=" + drawRect.getHeight());
    cb.stroke();
    cb.restoreState();
  }

  /**
   * Applica al PdfContentByte passato, gli stili necessari per tracciare una linea.
   * 
   * @param cb PdfContentByte su cui disegnare la linea
   * @param thickness spessore linea
   * @param style stile linea
   * @param color colore linea
   */
  public static void applyLineStyle(PdfContentByte cb, float thickness, Style style, BaseColor color) {
    cb.setLineWidth(thickness);
    if (color != null) {
      cb.setColorStroke(color);
    }    
    if (style == Style.SOLID) {
      float[] array = new float[0];
      cb.setLineDash(array, 0);
    } else if (style == Style.DASH) {
      float[] array = new float[1];
      array[0] = thickness * 5;
      cb.setLineDash(array, 0);
    } else if (style == Style.DOT) {
      float[] array = new float[1];
      if (thickness > 1) {
        array[0] = thickness;
      } else {
        array[0] = 1;
      }
      //      array[1] = 2;
      cb.setLineDash(array, 0);
    } else if (style == Style.DASHDOT) {
      float[] array = new float[4];
      array[0] = 5;
      array[1] = 2;
      array[2] = 1;
      array[3] = 2;
      cb.setLineDash(array, 0);
    }    
  }
  
  
  private void applyStyle(PdfContentByte cb) {
    applyLineStyle(cb, c_spessore, c_style, c_color);
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi
  }

  @Override
  public Object getContent(Elemento padre) throws GenerateException {
    return outerPara;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#flush(ciscoop.stampa.output.Documento)
   */
  @Override
  public void flush(Documento doc) throws GenerateException {
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Line" + getElementID();
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
