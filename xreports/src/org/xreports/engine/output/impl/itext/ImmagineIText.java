package ciscoop.stampa.output.impl.itext;

import java.util.List;

import org.xreports.engine.Stampa;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.Immagine;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.Border;
import org.xreports.engine.source.ImageElement;
import org.xreports.engine.source.Measure;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

public class ImmagineIText extends ElementoIText implements Immagine {
  private Chunk chunk    = null;

  private float c_width  = 0;
  private float c_height = 0;

  public ImmagineIText(XReport stampa, ImageElement tagImage, Elemento padre) throws GenerateException {
    setParent(padre);
    chunk = creaImage(stampa, tagImage);
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
    return chunk;
  }

  private Chunk creaImage(XReport stampa, ImageElement imageElem) throws GenerateException {
    Chunk chunkForImage = null;
    try {
      
      //========== ricerca sorgente immagine ===========      
      String szSrc = imageElem.getSrc();
      String foundFile = stampa.findResource(szSrc);
      if (foundFile == null) {
        throw new GenerateException(imageElem, "Non trovo l'immagine " + szSrc);
      }
      
      DocumentoIText doc = (DocumentoIText)stampa.getDocumento();
      Image img = doc.getImageFromCache(foundFile);
      
      //========== gestione dimensioni ===========      
      Measure width = imageElem.getWidth();
      Measure heigth = imageElem.getHeight();
      if (width != null && heigth != null) {
        //specificato altezza e larghezza: li scalo separatamente
        img.scaleAbsolute(width.scale(img.getWidth()), heigth.scale(img.getHeight()));
      } else if (width != null) {
        float ratio = 1f;
        ratio = width.isPercent() ? width.getValue() / 100f : width.getValue() / img.getWidth();

        img.scaleAbsolute(ratio * img.getWidth(), ratio * img.getHeight());
      } else if (heigth != null) {
        float ratio = 1f;
        ratio = heigth.isPercent() ? heigth.getValue() / 100f : heigth.getValue() / img.getHeight();
        img.scaleAbsolute(ratio * img.getWidth(), ratio * img.getHeight());
      }
      c_width = img.getWidth();
      c_height = img.getHeight();

      //========== gestione bordo ===========      
      if (imageElem.getBorder() != null) {
        Border b = imageElem.getBorder();
        if (b.hasBorder()) {
          if (b.getColorName() != null) {
            img.setBorderColor(doc.getColorByName(b.getColorName()));
          }
          else if (b.getColor() != null) {
            Colore c = b.getColor();
            img.setBorderColor(new BaseColor(c.getRed(), c.getGreen(), c.getBlue()));            
          }
          img.setBorderWidth(b.getSize());
          img.setBorder(Rectangle.BOX);
        }        
      }
      
      //========== gestione posizione ===========      
      float posX = imageElem.getPosX() == null ? 0 : imageElem.getPosX().getValue();
      float posY = imageElem.getPosY() == null ? 0 : imageElem.getPosY().getValue();
      chunkForImage = new Chunk(img, posX, posY, true);
    } catch (GenerateException gex) {
      throw gex;
    } catch (Exception e) {
      throw new GenerateException(imageElem, e);
    }
    return chunkForImage;
  }

  @Override
  public void flush(Documento documento) throws GenerateException {

  }

  @Override
  public String getUniqueID() {
    return "Img" + getElementID();
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
