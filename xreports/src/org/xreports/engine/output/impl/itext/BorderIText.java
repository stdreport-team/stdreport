package ciscoop.stampa.output.impl.itext;

import org.xreports.engine.source.Border;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;

/**
 * Gestisce i bordi
 * 
 * @author pier
 */
public class BorderIText {
  private DocumentoIText    m_doc         = null;
  public static final float CORNER_RADIUS = 4;

  public BorderIText(DocumentoIText doc) {
    m_doc = doc;
  }

  /**
   * Disegna il bordo passato attorno al rettangolo passato.
   * 
   * @param border
   *          bordo da disegnare; se null o con dimensione a zero, esce senza far nulla
   * @param rect
   *          rettangolo su cui disegnare il bordo
   * @param cb
   *          canvas su cui disegnare il bordo
   */
  public void draw(Border border, Rectangle rect, PdfContentByte cb) {
    if (border == null || !border.hasBorder()) {
      //se non ho alcun bordo da disegnare (non dovrebbe succedere ma non si sa mai...)
      //esco senza fare nulla
      return;
    }

    cb.saveState();

    try {
      BaseColor color = m_doc.getColorByName(border.getColorName());
      if (color != null) {
        cb.setColorStroke(color);
      }
      cb.setLineWidth(border.getSize());
      //coordinate rettangolo principale
      float x = rect.getLeft();
      float y = rect.getBottom();
      float w = rect.getWidth();
      float h = rect.getHeight();
      //distanza rettangolo secondario (solo per alcuni stili)
      float dist = 0;

      if (border.getStyle() == Border.BorderStyle.ROUNDED) {
        cb.roundRectangle(x, y, w, h, CORNER_RADIUS);
      } else {
        if (border.getStyle() == Border.BorderStyle.DOUBLE) {
          //rettangolo interno
          dist = border.getSize() * 2;
        } else if (border.getStyle() == Border.BorderStyle.DOTTED) {
          cb.setLineDash(border.getSize() / 2, border.getSize() * 3, 0);
          x = x + border.getSize();
          y = y - border.getSize();
          w = w - border.getSize() * 2;
          h = h - border.getSize() * 2;
        } else if (border.getStyle() == Border.BorderStyle.DASHED) {
          cb.setLineDash(border.getSize() * 5, border.getSize() * 6, 0);
          x = x + border.getSize();
          y = y + border.getSize();
          w = w - border.getSize() * 2;
          h = h - border.getSize() * 2;
        } else if (border.getStyle() == Border.BorderStyle.SOLID) {
          //resetto lo stile della linea a solid
          float[] array = new float[0];
          cb.setLineDash(array, 0);
        }
        int pos = border.getPosition();
        if (pos != Border.BOX) {
          if (pos == Border.TOP) {
            cb.moveTo(x, y + h);
            cb.lineTo(x + w, y + h);
            if (border.getStyle() == Border.BorderStyle.DOUBLE) {
              cb.moveTo(x, y + h - dist);
              cb.lineTo(x + w, y + h - dist);
            }
          } else if (pos == Border.BOTTOM) {
            cb.moveTo(x, y);
            cb.lineTo(x + w, y);
            if (border.getStyle() == Border.BorderStyle.DOUBLE) {
              cb.moveTo(x, y + dist);
              cb.lineTo(x + w, y + dist);
            }
          } else if (pos == Border.LEFT) {
            cb.moveTo(x, y + h);
            cb.lineTo(x, y);
            if (border.getStyle() == Border.BorderStyle.DOUBLE) {
              cb.moveTo(x + dist, y + h);
              cb.lineTo(x + dist, y);
            }
          } else if (pos == Border.RIGHT) {
            cb.moveTo(x + w, y + h);
            cb.lineTo(x + w, y);
            if (border.getStyle() == Border.BorderStyle.DOUBLE) {
              cb.moveTo(x + w - dist, y + h);
              cb.lineTo(x + w - dist, y);
            }
          }
        } else {
          cb.rectangle(x, y, w, h);
          if (border.getStyle() == Border.BorderStyle.DOUBLE) {
            cb.rectangle(x + dist, y + dist, w - dist * 2, h - dist * 2);
          }
        }
      }

      cb.stroke();

      //resetto lo stile della linea a solid
      float[] array = new float[0];
      cb.setLineDash(array, 0);
    } catch (Exception e) {
      // TODO che faccio qui???
      e.printStackTrace();
    }

    cb.restoreState();
  }
}
