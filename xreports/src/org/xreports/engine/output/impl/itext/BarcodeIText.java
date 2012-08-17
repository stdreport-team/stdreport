package org.xreports.engine.output.impl.itext;

import java.util.List;

import org.xreports.engine.XReport;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Immagine;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.BarcodeElement;


import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class BarcodeIText extends ElementoIText implements Immagine {
  /**
   * interlinea di default per i barcode
   */
  public static final float DEFAULT_LEADING = 1.1f;

  private Paragraph         c_barcode       = null;
  private BarcodeElement    c_sourceElem    = null;
  private XReport            c_stampa;

  private float             c_width         = 0;
  private float             c_height        = 0;

  public BarcodeIText(XReport stampa, BarcodeElement tagBarcode, Elemento padre) throws GenerateException {
    setParent(padre);
    c_stampa = stampa;
    c_sourceElem = tagBarcode;
    c_barcode = creaImage(stampa, tagBarcode);
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    //E' un elemento finale non è possibile aggiungere altri elementi
    return;
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
    return c_barcode;
  }

  private Image getBarcodeImage(BarcodeElement elem) throws GenerateException {
    String type = elem.getType();
    if (type == null) {
      throw new GenerateException(c_sourceElem, "tipo barcode non specificato");
    }
    String value = elem.getEvaluatedExpression();    
    try {
      Barcode barcode = null;
      if (type.equalsIgnoreCase(BarcodeElement.BARCODE_EAN13)) {
        barcode = new BarcodeEAN();
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_UPCA)) {
        BarcodeEAN bEAN = new BarcodeEAN();
        bEAN.setCodeType(Barcode.UPCA);
        barcode = bEAN;
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_UPCE)) {
        BarcodeEAN bEAN = new BarcodeEAN();
        bEAN.setCodeType(Barcode.UPCE);
        barcode = bEAN;
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_EAN8)) {
        BarcodeEAN bEAN = new BarcodeEAN();
        bEAN.setCodeType(Barcode.EAN8);
        barcode = bEAN;
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_128)) {
        Barcode128 b128 = new Barcode128();
        barcode = b128;
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_39)) {
        Barcode39 b39 = new Barcode39();
        barcode = b39;
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_39ext)) {
        Barcode39 b39 = new Barcode39();
        b39.setExtended(true);
        barcode = b39;
      }
      Image imageBar = null;
      DocumentoIText doc = (DocumentoIText) c_stampa.getDocumento();
      PdfWriter writer = doc.getWriter();
      PdfContentByte cb = writer.getDirectContent();
      if (barcode != null) {
        barcode.setCode(value);       
        if (elem.getHeight() != null) {
          barcode.setBarHeight(elem.getHeight().getValue());          
        }
        imageBar = barcode.createImageWithBarcode(cb, null, null);
      } else if (type.equalsIgnoreCase(BarcodeElement.BARCODE_QRCODE)) {
        BarcodeQRCode qrcode = new BarcodeQRCode(value, 1, 1, null);
        imageBar = qrcode.getImage();
      } else {
        throw new GenerateException(c_sourceElem, "tipo barcode non supportato: " + type);
      }
      return imageBar;
    } catch (Exception e) {
      throw new GenerateException(c_sourceElem, e, "Errore imprevisto in creazione barcode");
    }
  }

  private Paragraph creaImage(XReport stampa, BarcodeElement elem) throws GenerateException {
    try {
      Image imBar = getBarcodeImage(elem);
      float hei = imBar.getHeight();
      Chunk cBar = new Chunk(imBar, 0, 0);
      Paragraph pBar = new Paragraph();
      c_height = hei * DEFAULT_LEADING;
      c_width = imBar.getWidth();
      pBar.setLeading(c_height);
      pBar.add(cBar);
      return pBar;
    } catch (GenerateException gex) {
      throw gex;
    } catch (Exception e) {
      throw new GenerateException(elem, e);
    }
  }

  @Override
  public void flush(Documento documento) throws GenerateException {

  }

  @Override
  public String getUniqueID() {
    return "Barcode" + getElementID();
  }

  @Override
  public float calcAvailWidth() {
    return c_width;
  }

  @Override
  public float getHeight() {
    return c_height;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.impl.itext.ElementoIText#isBlock()
   */
  @Override
  public boolean isBlock() {
    return false;
  }

}
