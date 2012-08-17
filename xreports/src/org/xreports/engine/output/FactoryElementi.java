package org.xreports.engine.output;

import org.xreports.engine.XReport;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.IReportNode;


public interface FactoryElementi {
  public String getExtension() throws GenerateException;

  public String getMimeType() throws GenerateException;

  public BloccoTesto creaBloccoTesto(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Paragrafo creaParagrafo(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Cella creaCella(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Tabella creaTabella(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Documento creaDocumento(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Immagine creaImmagine(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Linea creaLinea(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Chart creaChart(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Marginbox creaMarginbox(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Rulers creaRulers(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;

  public Watermark creaWatermark(XReport report, IReportNode reportElement, Elemento padre) throws GenerateException;
}
