package org.xreports.engine.output.impl.itext;

import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.BloccoTesto;
import org.xreports.engine.output.Cella;
import org.xreports.engine.output.Chart;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.FactoryElementi;
import org.xreports.engine.output.Immagine;
import org.xreports.engine.output.Linea;
import org.xreports.engine.output.Marginbox;
import org.xreports.engine.output.Paragrafo;
import org.xreports.engine.output.Rulers;
import org.xreports.engine.output.Tabella;
import org.xreports.engine.output.Watermark;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.AbstractElement;
import org.xreports.engine.source.BarcodeElement;
import org.xreports.engine.source.BookmarkElement;
import org.xreports.engine.source.CellElement;
import org.xreports.engine.source.ChartElement;
import org.xreports.engine.source.FieldElement;
import org.xreports.engine.source.IReportNode;
import org.xreports.engine.source.ImageElement;
import org.xreports.engine.source.LineElement;
import org.xreports.engine.source.MarginboxElement;
import org.xreports.engine.source.RulersElement;
import org.xreports.engine.source.SpanElement;
import org.xreports.engine.source.TableElement;
import org.xreports.engine.source.TextElement;
import org.xreports.engine.source.TextNode;
import org.xreports.engine.source.WatermarkElement;

public class FactoryElementiIText implements FactoryElementi {
  private static final String ERR_NOT_SUPP = "Tag non supportato!";
  
  
  @Override
  public BloccoTesto creaBloccoTesto(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    try {
      if (reportElement instanceof TextNode) {
        return new BloccoTestoIText(stampa, (TextNode) reportElement, padre);
      } else if (reportElement instanceof FieldElement) {
        return new BloccoTestoIText(stampa, (FieldElement) reportElement, padre);
      } else if (reportElement instanceof SpanElement) {
        return new BloccoTestoIText(stampa, (SpanElement) reportElement, padre);
      } else if (reportElement instanceof BookmarkElement) {
        return new BloccoTestoIText(stampa, (BookmarkElement) reportElement, padre);
      }
    } catch (ResolveException e) {
      throw new GenerateException(e); 
    } 
    
    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public Cella creaCella(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof CellElement) {
      return new CellaIText(stampa, (CellElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public Documento creaDocumento(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    return new DocumentoIText(stampa, reportElement);
  }

  @Override
  public Paragrafo creaParagrafo(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof TextElement) {
      return new ParagrafoIText(stampa, (TextElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
  }

  private void throwExceptionNotSupp(IReportNode reportElement) throws GenerateException {
    if (reportElement instanceof AbstractElement) {
      throw new GenerateException((AbstractElement)reportElement, ERR_NOT_SUPP);          
    }
    throw new GenerateException(ERR_NOT_SUPP);              
  }
  
  
  @Override
  public Tabella creaTabella(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof TableElement) {
      return new TabellaIText(stampa, (TableElement) reportElement, padre);
    }

    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public Immagine creaImmagine(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof ImageElement) {
      return new ImmagineIText(stampa, (ImageElement) reportElement, padre);
    }
    else if (reportElement instanceof BarcodeElement) {
      return new BarcodeIText(stampa, (BarcodeElement) reportElement, padre);
    } 
    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public Linea creaLinea(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof LineElement) {
      return new LineaIText(stampa, (LineElement) reportElement, padre);
    }
    throw new GenerateException("Tag non supportato!");
  }

  @Override
  public Chart creaChart(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof ChartElement) {
      return new ChartIText(stampa, (ChartElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public String getExtension() throws GenerateException {
    return "pdf";
  }

  @Override
  public String getMimeType() throws GenerateException {
    return "application/pdf";
  }
  
  @Override
  public Marginbox creaMarginbox(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof MarginboxElement) {
      return new MarginboxIText(stampa, (MarginboxElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
  }

  @Override
  public Rulers creaRulers(XReport stampa, IReportNode reportElement, Elemento padre) throws GenerateException {
    if (reportElement instanceof RulersElement) {
      return new RulersIText(stampa, (RulersElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
  }

	/* (non-Javadoc)
	 * @see ciscoop.stampa.output.FactoryElementi#creaWatermark(ciscoop.stampa.Stampa, ciscoop.stampa.source.IReportNode, ciscoop.stampa.output.Elemento)
	 */
	@Override
	public Watermark creaWatermark(XReport stampaPDF, IReportNode reportElement,
			Elemento padre) throws GenerateException {

    if (reportElement instanceof WatermarkElement) {
      return new WatermarkIText(stampaPDF, (WatermarkElement) reportElement, padre);
    }
    throwExceptionNotSupp(reportElement);
    return null;
	}
  
}
