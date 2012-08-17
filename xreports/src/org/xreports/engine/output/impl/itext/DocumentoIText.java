package org.xreports.engine.output.impl.itext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Rettangolo;
import org.xreports.engine.output.StileCarattere;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.IReportNode;
import org.xreports.engine.source.MarginboxElement;
import org.xreports.engine.source.Margini;
import org.xreports.engine.source.RulersElement;
import org.xreports.engine.source.WatermarkElement;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.util.Text;


import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfWriter;

public class DocumentoIText extends ElementoIText implements Documento {
  private Document               c_document       = null;

  private Map<String, BaseColor> c_mappaColori    = new HashMap<String, BaseColor>();
  private Map<String, Font>      c_mappaFont      = new HashMap<String, Font>();
  private Map<String, BaseFont>  c_baseFonts      = new HashMap<String, BaseFont>();

  private PdfWriter              c_writer;
  private XReport                 c_stampa;
  private PageListener           c_pageListener;
  
  //gestione bookmarks
  private PdfOutline             c_currentOutline;
  private int                    c_currentOutlineLevel = 0;

  // ----- proprietà del documento -----
  private String                 c_documentTitle;
  private String                 c_documentSubject;
  private String                 c_documentAuthor;
  private Map<String,Image>      c_imgCache;
  private boolean                c_bookmarksOpened;

  // ----- elementi interni del documento -----
  private MarginboxElement       c_marginBox;
  private RulersElement          c_rulers;
  private List<WatermarkElement>       c_watermarks;
  
  
  /** mantiene l'interlinea corrente */
  private float                  c_currentLeading = 0;

  public DocumentoIText(XReport objStampa, IReportNode reportElement) throws GenerateException {
    c_stampa = objStampa;

    Rettangolo r = objStampa.getDocSize();
    Margini m = objStampa.getDocMargini();
    Rectangle rect = new Rectangle(r.getLLx(), r.getLLy(), r.getURx(), r.getURy());
    c_document = new Document(rect, m.getLeft().scale(rect.getWidth()), m.getRight().scale(rect.getWidth()), m
        .getTop().scale(rect.getHeight()), m.getBottom().scale(rect.getHeight()));
    
    buildColors(objStampa);
    buildFonts(objStampa);

    try {
      c_writer = PdfWriter.getInstance(c_document, objStampa.getOutputStream());
      c_writer.setFullCompression();
      byte[] userPassword = objStampa.getDocumentUserPassword();
      int perm = buildDocPermission(objStampa);
      c_writer.setEncryption(userPassword, null, perm, PdfWriter.STANDARD_ENCRYPTION_128);

      c_pageListener = new PageListener(c_stampa);
      c_writer.setPageEvent(c_pageListener);
      
      
      setDocumentAuthor(objStampa.getDocumentAuthor());
      setDocumentSubject(objStampa.getDocumentSubject());
      setDocumentTitle(objStampa.getDocumentTitle());
      setBookmarksOpened(objStampa.isDocumentBookmarksOpened());
      
      setViewerPref();
      
      c_watermarks = new ArrayList<WatermarkElement>();
    } catch (DocumentException e) {
      throw new GenerateException("Non riesco a creare il writer iText: " + e.toString(), e);
    } catch (EvaluateException e) {
      throw new GenerateException(e, "Errore grave in creazione documento");
    }
  }

  private void setViewerPref() {
    //per visualizzare a 2 pagine. PdfWriter.PageLayoutTwoColumnLeft
    int viewerPref = PdfWriter.FitWindow;
    if (isBookmarksOpened()) {
      viewerPref |= PdfWriter.PageModeUseOutlines;
    }
    if (getDocumentTitle() != null) {
      //se ho un titolo, visualizzo quello nella title bar del reader invece del nome del file.
      viewerPref |= PdfWriter.DisplayDocTitle;
    }    
    
    c_writer.setViewerPreferences(viewerPref);    
  }
  
  /**
   * Costruisce l'intero che racchiude i flags dei permessi del documento per iText.
   * Se non sono stati specificati i permessi, il default è copia=stampa=si, modifica=no
   * @param s oggetto contenente i permessi
   * @return flags permessi riconosciuti da iText
   */
  private int buildDocPermission(XReport s) {
    int perm = PdfWriter.ALLOW_SCREENREADERS;
    Boolean pval = s.getPermission(XReport.DocumentPermission.COPY);
    if (Text.toBoolean(pval, true))
      perm += PdfWriter.ALLOW_COPY;
    pval = s.getPermission(XReport.DocumentPermission.PRINT);
    if (Text.toBoolean(pval, true)) {
      perm += PdfWriter.ALLOW_PRINTING;
    }
    pval = s.getPermission(XReport.DocumentPermission.MODIFY);
    if (Text.toBoolean(pval, false)) {
      perm += PdfWriter.ALLOW_MODIFY_CONTENTS;
      perm += PdfWriter.ALLOW_ASSEMBLY;
    }
    return perm;
  }
  
  public void addPageListener(PageListener listener) {
    c_writer.setPageEvent(listener);      
  }

  public void writeBookmark(Bookmark elem, float vertPos) throws EvaluateException, GenerateException {
    int level = elem.getLevel();
    if (level <= 0)
      throw new GenerateException(elem.getElement(), "Livello del bookmark errato: " + level);
    String text = elem.getText();
    if (!Text.isValue(text))
      throw new GenerateException(elem.getElement(), "Testo del bookmark assente");    
    
    PdfOutline parent = findParentOutline(elem, level);
    
    
    //Paragraph p = new Paragraph(text);
    PdfOutline bookm = new PdfOutline(parent,
        new PdfDestination(PdfDestination.FITH, vertPos), text);
//    try {
//      c_document.add(p);
//    } catch (DocumentException e) {
//      e.printStackTrace();
//      throw new GenerateException(elem.getElement(), "Errore grave in creazione bookmark", e);
//    }
    c_currentOutline = bookm;
    c_currentOutlineLevel = level;    
  }
  
  public void writeBookmark(Bookmark elem) throws EvaluateException, GenerateException {
    writeBookmark(elem, c_writer.getVerticalPosition(true));
  }
  
  private PdfOutline findParentOutline(Bookmark elem, int level) throws GenerateException {
    if (level == 1)
      return c_writer.getRootOutline();
    
    if (level > c_currentOutlineLevel) {
      if (level > c_currentOutlineLevel + 1)
        throw new GenerateException(elem.getElement(), "Livello del bookmark errato: " + level);
      return c_currentOutline;
    }
    
    //qui level <= c_currentOutlineLevel    
    PdfOutline parent = c_currentOutline.parent();
    for (int i=c_currentOutlineLevel; i > level; i--)
      parent = parent.parent();
    
    return parent;    
  }
  
  public PageListener getPageListener() {
    return c_pageListener;
  }

  private void buildColors(XReport stampaPDF) {
    for (Colore col : stampaPDF.getColorList()) {
      c_mappaColori.put(col.getName(), createBaseColor(col));
    }
  }

  /**
   * Ritorna un colore iText (BaseColor) da un colore astratto (Colore)
   * @param c colore astratto
   * @return colore concreto per iText
   */
  public static BaseColor createBaseColor(Colore c) {
    return new BaseColor(c.getRed(), c.getGreen(), c.getBlue());
  }
  
  private String getBaseFontName(StileCarattere stile) {
    String name = stile.getFamily();
    if (name.equals(BaseFont.TIMES_ROMAN)) {
      if (stile.isBold() && stile.isItalic()) {
        name = BaseFont.TIMES_BOLDITALIC;
      } else if (stile.isBold()) {
        name = BaseFont.TIMES_BOLD;
      } else if (stile.isItalic()) {
        name = BaseFont.TIMES_ITALIC;
      }
    } else if (name.equals(BaseFont.HELVETICA)) {
      if (stile.isBold() && stile.isItalic()) {
        name = BaseFont.HELVETICA_BOLDOBLIQUE;
      } else if (stile.isBold()) {
        name = BaseFont.HELVETICA_BOLD;
      } else if (stile.isItalic()) {
        name = BaseFont.HELVETICA_OBLIQUE;
      }
    } else if (name.equals(BaseFont.COURIER)) {
      if (stile.isBold() && stile.isItalic()) {
        name = BaseFont.COURIER_BOLDOBLIQUE;
      } else if (stile.isBold()) {
        name = BaseFont.COURIER_BOLD;
      } else if (stile.isItalic()) {
        name = BaseFont.COURIER_OBLIQUE;
      }
    }
    return name;
  }

  private void buildFonts(XReport stampa) throws GenerateException {
    //Prima metto nella hashmap c_baseFonts tutti i basefont degli stili utilizzati
    for (StileCarattere stile : stampa.getStileCarattList()) {
      try {
        BaseFont bf = null;
        String src = null;
        if (stile.getSourceFont() != null) {
          src = stile.getSourceFont();
          String foundFile = stampa.findResource(src);
          if (foundFile == null) {
            throw new GenerateException("Non trovo il file di font " + src);
          }
          try {
            bf = BaseFont.createFont(foundFile, "", BaseFont.EMBEDDED);
          } catch (Exception e) {
            // non trovo src della font: forse manca il file di font.
            // in uscita da questo blocco tenta il caricamento solo con la family
            e.printStackTrace();
          }
        }
        if (bf == null) {
          src = getBaseFontName(stile);
          if ( !c_baseFonts.containsKey(src)) {
            // creo la BaseFont solo se non l'ho già fatto: posso avere infatti più
            // stili con la stessa baseFont
            bf = BaseFont.createFont(src, "", BaseFont.NOT_EMBEDDED);
          }
        }
        if (bf != null) {
          c_baseFonts.put(src, bf);
        }
      } catch (Exception e) {
        throw new GenerateException("Errore in creazione font base per stile '" + stile.getName() + "': " + e.toString());
      }
    }

    // qui creo gli oggetti Font in base ai BaseFont memorizzati in precedenza
    for (StileCarattere stile : stampa.getStileCarattList()) {
      String src = null;
      BaseFont bf = null;
      if (stile.getSourceFont() != null) {
        src = stile.getSourceFont();
        bf = c_baseFonts.get(src);
      }
      if (bf == null) {
        src = getBaseFontName(stile);
        if (src == null) {
          throw new GenerateException("Errore interno: manca family e source nella font!");
        }
        bf = c_baseFonts.get(src);
      }
      if (bf == null) {
        throw new GenerateException("Errore interno: BaseFont " + src + " risulta null");
      }
      Font f = new Font(bf, stile.getSize());
      Colore colore = stile.getColore();
      if (colore != null) {
        f.setColor(c_mappaColori.get(colore.getName()));
      }
      int style = 0;
      if (stile.isBold()) {
        style |= Font.BOLD;
      }
      if (stile.isUnderline()) {
        style |= Font.UNDERLINE;
      }
      if (stile.isItalic()) {
        style |= Font.ITALIC;
      }
      f.setStyle(style);
      c_mappaFont.put(stile.getName(), f);
    }    
  }
  
  /**
   * Ritorna la font di default di questa implementazione specifica di documento.
   * Corrisponde alla font di default definita nel report, la cui rappresentazione
   * astratta è data da {@link Stampa#getDefaultFont()}.
   * 
   * @return font di default del documento, come definita nel report; non torna mai null,
   * se il report non definisce una font di default, ritorna una font predefinita di sistema
   */
  public Font getDefaultFont() {
    return getFontByName(c_stampa.getDefaultFont().getName());
  }
  

  /**
   * Ritorna il colore dato il nome. Il nome qui è il nome del colore definito dall'utente nel sorgente XML del report; precisamente
   * è il valore dell'attributo <b>name</b> del tag <tt>&lt;color&gt;</tt>.
   * 
   * @param colorName
   *          nome del colore richiesto
   * @return BaseColor corrispondente oppure null se nessun colore è stato definito con il nome passato.
   */
  public BaseColor getColorByName(String colorName) {
    return c_mappaColori.get(colorName);
  }

  /**
   * Ritorna l'oggetto Font dato il nome. Il nome qui è il nome della font definita dall'utente nel sorgente XML del report;
   * precisamente è il valore dell'attributo <b>name</b> del tag <tt>&lt;font&gt;</tt>.
   * 
   * @param fontName
   *          nome della font richiesta
   * @return oggetto Font corrispondente oppure null se nessuna font è stato definita con il nome passato.
   */
  public Font getFontByName(String fontName) {
    return c_mappaFont.get(fontName);
  }

  @Override
  public void inizioDocumento() throws GenerateException {
    if (c_marginBox != null) {
      c_marginBox.generate(null, c_stampa, null);      
    }
    if (c_rulers != null) {
      c_rulers.generate(null, c_stampa, null);      
    }
    if (c_watermarks != null) {
      for (WatermarkElement we : c_watermarks)
        we.generate(null, c_stampa, null);      
    }
    c_document.open();
  }

  @Override
  public void saltaPagina() {
    c_document.newPage();
  }

  @Override
  public void fineDocumento() {
    c_document.close();
    destroy();
  }

  public int getCurrentPageNumber() {
    return c_pageListener.getCurrentPageNumber();
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    Object obj = null;
    try {
      obj = figlio.getContent(null);
      figlio.fineGenerazione();
      if (obj != null) {
        if (obj instanceof Element) {
          c_document.add((Element) obj);
          if (obj instanceof Phrase) {
            c_currentLeading = ((Phrase) obj).getLeading();
          }
        }
      } else {
      }
      figlio.flush(this);
    } catch (Exception e) {
      e.printStackTrace();
      throw new GenerateException("Errore grave in aggiungiElemento " + obj + ": " + e.toString(), e);
    }
  }

  /**
   * Interlinea corrente. E' il valore dell'interlinea dell'ultima Phrase aggiunta al documento.
   */
  public float getCurrentLeading() {
    return c_currentLeading;
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
    return c_document;
  }

  /**
   * @return the c_writer
   */
  public PdfWriter getWriter() {
    return c_writer;
  }

  /**
   * @return the document
   */
  public Document getDocument() {
    return c_document;
  }

  /**
   * @return the stampa
   */
  public XReport getStampa() {
    return c_stampa;
  }

  @Override
  public void flush(Documento doc) throws GenerateException {
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return "Doc";
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#getParent()
   */
  @Override
  public Elemento getParent() {
    //il documento non ha parent
    return null;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#setParent(ciscoop.stampa.output.Elemento)
   */
  @Override
  public void setParent(Elemento elem) {
    //no-op: il documento non ha parent		
  }

  @Override
  public float calcAvailWidth() {
    return c_document.getPageSize().getWidth() - c_document.leftMargin() - c_document.rightMargin();
  }

  @Override
  public float getHeight() {
    return c_document.getPageSize().getHeight() - c_document.topMargin() - c_document.bottomMargin();
  }
  
  
  /**
   * @return il documentTitle
   */
  public String getDocumentTitle() {
    return c_documentTitle;
  }

  /**
   * @param documentTitle
   *          the documentTitle to set
   */
  public void setDocumentTitle(String documentTitle) {
    c_documentTitle = documentTitle;
  }

  /**
   * @return il documentSubject
   */
  public String getDocumentSubject() {
    return c_documentSubject;
  }

  /**
   * @param documentSubject
   *          the documentSubject to set
   */
  public void setDocumentSubject(String documentSubject) {
    c_documentSubject = documentSubject;
    if (c_documentSubject != null) {
      c_document.addSubject(c_documentSubject);
    }
  }

  /**
   * @return il documentAuthor
   */
  public String getDocumentAuthor() {
    return c_documentAuthor;
  }

  /**
   * @param documentAuthor
   *          the documentAuthor to set
   */
  public void setDocumentAuthor(String documentAuthor) {
    c_documentAuthor = documentAuthor;
    if (c_documentAuthor != null) {
      c_document.addAuthor(c_documentAuthor);
    }
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return true;
  }

  public Image getImageFromCache(String imageFile) throws BadElementException, MalformedURLException, IOException {
    Image img = null;
    if (c_imgCache==null) {
      c_imgCache = new HashMap<String,Image>();      
    }
    else {
      img = c_imgCache.get(imageFile);      
    }
    if (img == null) {
      img = Image.getInstance(imageFile);
      /* compressione per risparmiare spazio */
      img.setCompressionLevel(9);
      c_imgCache.put(imageFile, img);
    }
    return img;
  }

  public void destroy() {
    if (c_imgCache != null) {
      c_imgCache.clear();
      c_imgCache = null;
    }
    if (c_mappaColori != null) {
      c_mappaColori.clear();
      c_mappaColori = null;
    }
    if (c_mappaFont != null) {
      c_mappaFont.clear();
      c_mappaFont = null;
    }
    if (c_baseFonts != null) {
      c_baseFonts.clear();
      c_baseFonts = null;
    }
    if (c_pageListener != null) {
      c_pageListener.destroy();
      c_pageListener = null;
    }
    c_writer = null;
    c_stampa = null;
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.output.Documento#setMarginbox(ciscoop.stampa.source.MarginboxElement)
   */
  @Override
  public void setMarginboxElement(MarginboxElement e) {
    c_marginBox = e;    
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.output.Documento#getMarginbox()
   */
  @Override
  public MarginboxElement getMarginboxElement() {
    return c_marginBox;
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.output.Documento#setRulersElement(ciscoop.stampa.source.RulersElement)
   */
  @Override
  public void setRulersElement(RulersElement e) {
    c_rulers = e;
  }

  /* (non-Javadoc)
   * @see ciscoop.stampa.output.Documento#getRulersElement()
   */
  @Override
  public RulersElement getRulersElement() {
    return c_rulers;
  }

	/* (non-Javadoc)
	 * @see ciscoop.stampa.output.Documento#setWatermarkElement(ciscoop.stampa.source.WatermarkElement)
	 */
	@Override
	public void addWatermarkElement(WatermarkElement e) {
		c_watermarks.add(e);		
	}

	/* (non-Javadoc)
	 * @see ciscoop.stampa.output.Documento#getWatermarkElement()
	 */
	@Override
	public List<WatermarkElement> getWatermarkElements() {
		return c_watermarks;
	}

  public boolean isBookmarksOpened() {
    return c_bookmarksOpened;
  }

  public void setBookmarksOpened(boolean bookmarksOpened) {
    c_bookmarksOpened = bookmarksOpened;
  }
  
  
}
