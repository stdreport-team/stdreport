package org.xreports.engine.output.impl.itext;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.TextAnchor;

import org.xreports.datagroup.Group;
import org.xreports.engine.ResolveException;
import org.xreports.engine.XReport;
import org.xreports.engine.output.Chart;
import org.xreports.engine.output.Colore;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.ChartElement;
import org.xreports.engine.source.ChartElement.GraphType;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.expressions.symbols.Evaluator;
import org.xreports.expressions.symbols.Symbol;
import org.xreports.util.Text;


import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfPrinterGraphics2D;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class ChartIText extends ElementoIText implements Chart, Evaluator {

  public enum Style {
    SOLID, DASH, DOT, DASHDOT
  }

  /** posizione verticale della linea */

  private Chunk              outerChunk    = null;
  private Paragraph          outerPara     = null;

  private ChartElement       c_chartElem;
  private float              c_height;
  private float              c_marginTop;
  private float              c_marginBottom;

  private final static float c_topDistance = 5.0f;

  private Group              c_currentGroup;

  public ChartIText(XReport report, ChartElement chartElem, Elemento padre) throws GenerateException {
    super(report, padre);
    c_chartElem = chartElem;
    outerChunk = new Chunk(" ");
    outerChunk.setGenericTag(getUniqueID());
    outerPara = new Paragraph();
    c_marginTop = c_chartElem.getMarginTop();
    c_marginBottom = c_chartElem.getMarginBottom();
    c_height = c_chartElem.getHeight() + c_marginTop + c_marginBottom + c_topDistance;
    outerPara.setLeading(c_height);
    outerPara.add(outerChunk);
    getDocumentImpl().getPageListener().addChart(getUniqueID(), this);
  }

  @Override
  public float calcAvailWidth() {
    return c_chartElem.getWidth();
  }

  @Override
  public float getHeight() {
    return c_height;
  }

  private JFreeChart getChart() throws GenerateException {
    try {
      Group grup = c_chartElem.getGroup();
      List<Group> groups = grup.getDescendantInstances(c_chartElem.getChartGroup());
      //      //se non trovo i dati nel report principale, cerco in tutti i subreport:
      //      //mi fermo appena trovo il gruppo richiesto
      //      if (groups == null || groups.size() == 0) {
      //        for (ReportInfo ri : c_stampa.listSubreports()) {
      //          groups = ri.getRootGroup().getDescendantInstances(c_chartElem.getChartGroup());
      //          if (groups != null && groups.size() > 0) {
      //            break;
      //          }
      //        }
      //      }
      if (groups == null) {
        throw new GenerateException(c_chartElem, "Il grafico non ha dati");
      }
      JFreeChart chart = null;
      if (c_chartElem.getType() == ChartElement.GraphType.pie || c_chartElem.getType() == ChartElement.GraphType.pie3D) {
        chart = createPieChart(groups, c_chartElem.getType());
      } else {
        chart = createBarLineChart(groups, c_chartElem.getType());
      }

      //        if (c_chartElem.getType() == ChartElement.GraphType.bar || c_chartElem.getType() == ChartElement.GraphType.bar3D) {
      //        chart = getBarChart(groups, c_chartElem.getType() == ChartElement.GraphType.bar3D);
      //      } else if (c_chartElem.getType() == ChartElement.GraphType.line || c_chartElem.getType() == ChartElement.GraphType.line3D) {
      //        chart = getLineChart(groups, c_chartElem.getType() == ChartElement.GraphType.line3D);
      //      }

      if (chart != null) {
        setCommonAttrs(chart);
        return chart;
      }

      throw new GenerateException(c_chartElem, "Tipo di grafico non ancora supportato: " + c_chartElem.getType());
    } catch (GenerateException e) {
      throw e;
    } catch (Exception e) {
      throw new GenerateException(c_chartElem, e.getMessage());
    }
  }

  private void setCommonAttrs(JFreeChart chart) throws EvaluateException {
    Colore c = getReport().getColorByName(c_chartElem.getBackgroundColor());
    if (c != null) {
      chart.setBackgroundPaint(convertToAWTColor(c));
    }
    String title = c_chartElem.getTitle();
    if (title != null) {
      TextTitle source = new TextTitle(title);
      source.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
      // source.setPosition(RectangleEdge.TOP);
      // source.setHorizontalAlignment(HorizontalAlignment.RIGHT);			
      chart.setTitle(source);
    }

    String subtitle = c_chartElem.getSubtitle();
    if (subtitle != null) {
      TextTitle source = new TextTitle(subtitle);
      source.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
      // source.setPosition(RectangleEdge.BOTTOM);
      // source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
      chart.addSubtitle(source);
    }

    LegendTitle legend = chart.getLegend();
    if (legend != null) {
      legend.setItemFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
      legend.setPadding(0, 0, 0, 0);
      //legend.setHeight(50);
    }

  }

  private Map<String,Double> c_otherValues;
  //private String c_otherLabel = "altri";
  
  /**
   * Ha il compito di controllare che il valore passato sia da aggiungere al dataset
   * che poi il grafico andrà a rendere. Si basa sulle proprietà {@link ChartElement#getMaxCategories()},
   * {@link ChartElement#getMinCategoryValue()}, {@link ChartElement#getOtherLabel(), {@link ChartElement#isSumOther()}.
   * @param dataset dataset del grafico a cui aggiungere i valori
   * @param value valore da aggiungere
   * @param serie serie a cui appartiene il valore
   * @return true se valore è da aggiungere
   * @throws EvaluateException per errori di valutazione attributi collegati di ChartElement
   * @throws ResolveException 
   */
  private boolean isValueToAdd(AbstractDataset dataset, double value, String serie) throws EvaluateException, ResolveException {
    boolean bAdd = true;
    if (c_chartElem.getMaxCategories() > 0) {
      int xCount = 0;
      if (dataset instanceof DefaultCategoryDataset)
        xCount = ((DefaultCategoryDataset)dataset).getColumnCount();
      else if (dataset instanceof DefaultPieDataset)
        xCount = ((DefaultPieDataset)dataset).getItemCount();
      if (xCount >= c_chartElem.getMaxCategories()) {
        bAdd = false;
      }
    }
    if (c_chartElem.getMinCategoryValue() > Double.MIN_VALUE) {
      if (value < c_chartElem.getMinCategoryValue())
        bAdd = false;
    }

    if (!bAdd && c_otherValues != null) {
      double otherValue = 0.0;
      if (Text.isValue(serie)) {
        Double dv = c_otherValues.get(serie);
        if (dv != null)
          otherValue = dv.doubleValue();
      }
      otherValue += value;
      c_otherValues.put(serie, Double.valueOf(otherValue));
    }
    
    return bAdd;
  }
  
  /**
   * Crea il grafico con tutte le sue proprietà
   * @param groups todo
   * @param type todo
   * @return todo...
   */
  private JFreeChart createBarLineChart(List<Group> groups, ChartElement.GraphType type) {
    try {
      if (groups.size() > 0) {
        Symbol serie = c_chartElem.getSerieTree();
        Symbol category = c_chartElem.getCategoryTree();
        Symbol val = c_chartElem.getValueTree();
        Set<String> series = new HashSet<String>();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (c_chartElem.isSumOther()) {
          c_otherValues = new HashMap<String, Double>();
        }
        for (Group g : groups) {
          c_currentGroup = g;
          Object labelCategories = category.evaluate(this);
          if (labelCategories == null) {
            labelCategories = "???";
          }
          Object labelSeries = null;
          if (serie != null) {
            labelSeries = serie.evaluate(this);
            series.add(String.valueOf(labelSeries));
          }
          if (labelSeries == null) {
            labelSeries = "";
          }
          Object value = val.evaluate(this);
          double dblValue = Text.toDouble(value, 0.0);
          if (isValueToAdd(dataset, dblValue, labelSeries.toString())) {
            dataset.addValue(dblValue, labelSeries.toString(), labelCategories.toString().trim());            
          }
        }
        
        if (c_chartElem.isSumOther()) {
          String otherLabel = c_chartElem.getOtherLabel() != null ? c_chartElem.getOtherLabel() : "altri"; 
          for (Entry<String, Double> entry : c_otherValues.entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), otherLabel);                                  
          }
        }
          
        PlotOrientation plotOr = c_chartElem.isBarOrientationVertical() ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;

        JFreeChart chart = null;
        // domain axis label (etichetta sotto: le ascisse)
        String domainText = c_chartElem.getDomainText();
        // range axis label (etichetta a fianco dell'asse verticale dei valori: le ordinate)
        String valuesText = c_chartElem.getDomainText();
        switch (type) {
          case bar:
            chart = ChartFactory.createBarChart("", //title
                domainText, valuesText, dataset, plotOr, c_chartElem.isShowLegend(), false, // tooltips?
                false); // URLs?
            break;
          case bar3D:
            chart = ChartFactory.createBarChart3D("", //title
                domainText, valuesText, dataset, plotOr, c_chartElem.isShowLegend(), false, // tooltips?
                false); // URLs?
            break;
          case line:
            chart = ChartFactory.createLineChart("", //title
                domainText, valuesText, dataset, plotOr, c_chartElem.isShowLegend(), false, // tooltips?
                false); // URLs?
            break;
          case line3D:
            chart = ChartFactory.createLineChart3D("", //title
                domainText, valuesText, dataset, plotOr, c_chartElem.isShowLegend(), false, // tooltips?
                false); // URLs?
            break;
          default:
            throw new GenerateException("Tipo grafico non previsto: " + type);
        }
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        CategoryAxis domainAxis = plot.getDomainAxis();
        //        domainAxis.setCategoryLabelPositions(
        //                CategoryLabelPositions.createUpRotationLabelPositions(
        //                        Math.PI / 2.0));

        //font per la descrizione delle categorie, cioè la descrizione che sta sotto ogni barra
        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));

        //più il numero è alto, più le barre sono strette
        domainAxis.setCategoryMargin(0.1);

        //{0} = nome secondo parametro della dataset.addValue
        //{1} = nome terzo parametro della dataset.addValue
        //{2} = valore (primo parametro della dataset.addValue)

        if (c_chartElem.isShowLabel()) {
          CategoryItemRenderer renderer;
          //renderer = new MyBarRenderer(dataset);
          //plot.setRenderer(renderer);
          renderer = plot.getRenderer();
          CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(c_chartElem.getLabelText(),
              new DecimalFormat("0.##"));
          renderer.setBaseItemLabelGenerator(generator);
          renderer.setBaseItemLabelsVisible(true);

          ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER);
          if (series.size() > 0) {
            for (int i = 0; i < series.size(); i++) {
              renderer.setSeriesPositiveItemLabelPosition(i, p);
            }
          } else {
            renderer.setBasePositiveItemLabelPosition(p, false);
          }

          //distanza fra le serie
          if (renderer instanceof BarRenderer) {
            ((BarRenderer) renderer).setItemMargin(0.15);
          } else if (renderer instanceof LineAndShapeRenderer) {
            ((LineAndShapeRenderer) renderer).setItemMargin(0.15);
          }

          //font dell'etichetta in cima alle barre (dove di solito ci si mette il valore) 
          renderer.setBaseItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
          renderer.setSeriesItemLabelFont(0, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
          renderer.setSeriesItemLabelFont(1, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
        }

        return chart;
      }
      // nessun dato: non creo alcun grafico e torno semplicemente null
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public class MyBarRenderer extends BarRenderer {
    private DefaultCategoryDataset c_dataset;
    public MyBarRenderer(DefaultCategoryDataset dataset) {
      super();
    }
    public Paint getItemPaint(int x_row, int x_col) {
      Color c = new Color(255 - 5*x_col, 255 - 11*x_col, 17 * x_col);
//      if (x_row % 2 == 0) { return Color.red; }      
//      else { return Color.green; }
      return c;
    }    
  }  
  
//  private JFreeChart getBarChart(List<Group> groups, boolean treD) {
//    try {
//      if (groups.size() > 0) {
//        Symbol serie = c_chartElem.getSerieTree();
//        Symbol category = c_chartElem.getCategoryTree();
//        Symbol val = c_chartElem.getValueTree();
//        Set<String> series = new HashSet<String>();
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        for (Group g : groups) {
//          c_currentGroup = g;
//          Object labelCategories = category.evaluate(this);
//          if (labelCategories == null) {
//            labelCategories = "???";
//          }
//          Object labelSeries = null;
//          if (serie != null) {
//            labelSeries = serie.evaluate(this);
//            series.add(String.valueOf(labelSeries));
//          }
//          if (labelSeries == null) {
//            labelSeries = "";
//          }
//          Object value = val.evaluate(this);
//          double dblValue = Text.toDouble(value, 0.0);
//          dataset.addValue(dblValue, labelSeries.toString(), labelCategories.toString().trim());
//        }
//
//        PlotOrientation plotOr = c_chartElem.isBarOrientationVertical() ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
//
//        JFreeChart chart = null;
//        // versione 3D
//        if (treD) {
//          chart = ChartFactory.createBarChart3D("", //title
//              c_chartElem.getDomainText(), // domain axis label (etichetta sotto le barre)
//              c_chartElem.getValuesText(), // range axis label (etichetta a fianco dell'asse verticale dei valori)
//              dataset, plotOr, // orientation
//              c_chartElem.isShowLegend(), // legend?
//              false, // tooltips?
//              false // URLs?
//              );
//        } else {
//          chart = ChartFactory.createBarChart("", //title
//              c_chartElem.getDomainText(), // domain axis label (etichetta sotto le barre)
//              c_chartElem.getValuesText(), // range axis label (etichetta a fianco dell'asse verticale dei valori)
//              dataset, plotOr, // orientation
//              c_chartElem.isShowLegend(), // legend?
//              false, // tooltips?
//              false // URLs?
//              );
//        }
//
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
//
//        CategoryAxis domainAxis = plot.getDomainAxis();
//        //        domainAxis.setCategoryLabelPositions(
//        //                CategoryLabelPositions.createUpRotationLabelPositions(
//        //                        Math.PI / 2.0));
//
//        //font per la descrizione delle categorie, cioè la descrizione che sta sotto ogni barra
//        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//
//        //più il numero è alto, più le barre sono strette
//        domainAxis.setCategoryMargin(0.1);
//
//        //{0} = nome secondo parametro della dataset.addValue
//        //{1} = nome terzo parametro della dataset.addValue
//        //{2} = valore (primo parametro della dataset.addValue)
//
//        if (c_chartElem.isShowLabel()) {
//          BarRenderer renderer = (BarRenderer) plot.getRenderer();
//          CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(c_chartElem.getLabelText(),
//              new DecimalFormat("0.##"));
//          renderer.setBaseItemLabelGenerator(generator);
//          renderer.setBaseItemLabelsVisible(true);
//
//          ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER);
//          if (series.size() > 0) {
//            for (int i = 0; i < series.size(); i++) {
//              renderer.setSeriesPositiveItemLabelPosition(i, p);
//            }
//          } else {
//            renderer.setBasePositiveItemLabelPosition(p, false);
//          }
//
//          //distanza fra le serie
//          renderer.setItemMargin(0.15);
//
//          //font dell'etichetta in cima alle barre (dove di solito ci si mette il valore) 
//          renderer.setBaseItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//          renderer.setSeriesItemLabelFont(0, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//          renderer.setSeriesItemLabelFont(1, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//        }
//
//        return chart;
//      }
//      // nessun dato: non creo alcun grafico e torno semplicemente null
//      return null;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//
//  private JFreeChart getLineChart(List<Group> groups, boolean treD) {
//    try {
//      if (groups.size() > 0) {
//        Symbol serie = c_chartElem.getSerieTree();
//        Symbol category = c_chartElem.getCategoryTree();
//        Symbol val = c_chartElem.getValueTree();
//        Set<String> series = new HashSet<String>();
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        for (Group g : groups) {
//          c_currentGroup = g;
//          Object labelCategories = category.evaluate(this);
//          if (labelCategories == null) {
//            labelCategories = "???";
//          }
//          Object labelSeries = null;
//          if (serie != null) {
//            labelSeries = serie.evaluate(this);
//            series.add(String.valueOf(labelSeries));
//          }
//          if (labelSeries == null) {
//            labelSeries = "";
//          }
//          Object value = val.evaluate(this);
//          double dblValue = Text.toDouble(value, 0.0);
//          dataset.addValue(dblValue, labelSeries.toString(), labelCategories.toString().trim());
//        }
//
//        PlotOrientation plotOr = c_chartElem.isBarOrientationVertical() ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
//
//        JFreeChart chart = null;
//        // versione 3D
//        if (treD) {
//          chart = ChartFactory.createLineChart3D("", //title
//              c_chartElem.getDomainText(), // domain axis label (etichetta sotto le barre)
//              c_chartElem.getValuesText(), // range axis label (etichetta a fianco dell'asse verticale dei valori)
//              dataset, plotOr, // orientation
//              c_chartElem.isShowLegend(), // legend?
//              false, // tooltips?
//              false // URLs?
//              );
//        } else {
//          chart = ChartFactory.createLineChart("", //title
//              c_chartElem.getDomainText(), // domain axis label (etichetta sotto le barre)
//              c_chartElem.getValuesText(), // range axis label (etichetta a fianco dell'asse verticale dei valori)
//              dataset, plotOr, // orientation
//              c_chartElem.isShowLegend(), // legend?
//              false, // tooltips?
//              false // URLs?
//              );
//        }
//
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
//
//        CategoryAxis domainAxis = plot.getDomainAxis();
//        //        domainAxis.setCategoryLabelPositions(
//        //                CategoryLabelPositions.createUpRotationLabelPositions(
//        //                        Math.PI / 2.0));
//
//        //font per la descrizione delle categorie, cioè la descrizione che sta sotto ogni barra
//        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//
//        //più il numero è alto, più le barre sono strette
//        domainAxis.setCategoryMargin(0.1);
//
//        //{0} = nome secondo parametro della dataset.addValue
//        //{1} = nome terzo parametro della dataset.addValue
//        //{2} = valore (primo parametro della dataset.addValue)
//
//        if (c_chartElem.isShowLabel()) {
//          LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
//          CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(c_chartElem.getLabelText(),
//              new DecimalFormat("0.##"));
//          renderer.setBaseItemLabelGenerator(generator);
//          renderer.setBaseItemLabelsVisible(true);
//
//          ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER);
//          if (series.size() > 0) {
//            for (int i = 0; i < series.size(); i++) {
//              renderer.setSeriesPositiveItemLabelPosition(i, p);
//            }
//          } else {
//            renderer.setBasePositiveItemLabelPosition(p, false);
//          }
//
//          //distanza fra le serie
//          renderer.setItemMargin(0.15);
//
//          //font dell'etichetta in cima alle barre (dove di solito ci si mette il valore) 
//          renderer.setBaseItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//          renderer.setSeriesItemLabelFont(0, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//          renderer.setSeriesItemLabelFont(1, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
//        }
//
//        return chart;
//      }
//      // nessun dato: non creo alcun grafico e torno semplicemente null
//      return null;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }

  private Color convertToAWTColor(Colore colore) {
    Color outColor = new Color(colore.getRed(), colore.getGreen(), colore.getBlue());
    return outColor;
  }

  private JFreeChart createPieChart(List<Group> groups, GraphType type) {
    try {
      if (groups.size() > 0) {
        Symbol cat = c_chartElem.getCategoryTree();
        Symbol val = c_chartElem.getValueTree();
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (c_chartElem.isSumOther()) {
          c_otherValues = new HashMap<String, Double>();
        }
        for (Group g : groups) {
          c_currentGroup = g;
          Object label = cat.evaluate(this);
          if (label == null) {
            label = "???";
          }
          Object value = val.evaluate(this);
          double dblValue = Text.toDouble(value, 0.0);
          String strLabel = label.toString();
          if (isValueToAdd(dataset, dblValue, "*")) {
            dataset.setValue(strLabel, dblValue);            
          }          
        }
        if (c_chartElem.isSumOther()) {
          String otherLabel = c_chartElem.getOtherLabel() != null ? c_chartElem.getOtherLabel() : "altri"; 
          for (Entry<String, Double> entry : c_otherValues.entrySet()) {
            //qui avrò una sola iterazione...
            dataset.setValue(otherLabel, entry.getValue());                                  
          }
        }        
        
        // create a chart...
        JFreeChart chart;
        if (type==GraphType.pie) {
          chart = ChartFactory.createPieChart("", //title
              dataset, c_chartElem.isShowLegend(), // legend?
              false, // tooltips?
              false // URLs?
              );          
        } else {
          chart = ChartFactory.createPieChart3D("", //title
              dataset, c_chartElem.isShowLegend(), // legend?
              false, // tooltips?
              false // URLs?
              );                    
        }

        if (c_chartElem.isShowLabel()) {
          //{0} = descrizione categoria
          //{1} = valore categoria
          //{2} = valore percentuale categoria rispetto al totale
          PiePlot plot = (PiePlot) chart.getPlot();
          plot.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
          plot.setLabelGenerator(new StandardPieSectionLabelGenerator(c_chartElem.getLabelText()));
          plot.setLabelBackgroundPaint(new Color(220, 220, 220));
          plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(c_chartElem.getLegendText()));
        }

        return chart;
      }

      // nessun dato: non creo alcun grafico e torno semplicemente null
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Effettua fisicamente il disegno del grafico secondo i parametri impostati.
   * Questo metodo è chiamato da
   * {@link PageListener#onGenericTag(PdfWriter, Document, Rectangle, String)}
   * il quale è stato istruito in ciò dal costruttore di questa classe. <br/>
   * Il grafico viene messo in un paragrafo (vedi {@link #outerPara}) al quale
   * viene impostata una interlinea alta quanto il grafico. In questo modo gli
   * elementi successivo vanno a capo dopo il grafico.
   * 
   * @param rect
   *          è il rettangolo del generic chunk
   * 
   * @throws GenerateException
   */
  public void draw(Rectangle rect) throws GenerateException {
    PdfWriter writer = getWriter();
    Document c_document = getDocumentImpl().getDocument();

    JFreeChart chart = getChart();
    if (chart == null) {
      getReport().addWarningMessage(c_chartElem.toString() + ", il grafico risulta null, non lo disegno");
      return;
    }

    PdfContentByte cb = writer.getDirectContent();
    float chartWidth = c_chartElem.getWidth();
    float chartHeight = c_chartElem.getHeight();
    // PdfTemplate tp = cb.createTemplate(width, height);    
    //Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
    //Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
    //chart.draw(g2, r2D);
    //g2.dispose();
    // cerco di centrare il grafico all'interno del suo parent
    float parentWidth = ((ElementoIText) getParent()).calcAvailWidth();
    float ulx = rect.getLeft();
    if (parentWidth > chartWidth) {
      ulx += (parentWidth - chartWidth) / 2;
    }

    //rect: è il rettangolo del chunk (outerChunk) associato al grafico
    //      ed è nella parte bassa del paragrafo outerPara che è alto quanto il grafico

    //float top = rect.getTop() - rect.getHeight() + c_marginBottom;
    float uly = c_document.getPageSize().getHeight() - (rect.getBottom() + chartHeight - c_topDistance); //c_document.getPageSize().getHeight() - rect.getTop() - chartHeight;
    Rectangle2D r2D = new Rectangle2D.Double(ulx, uly, chartWidth, chartHeight);
    PdfPrinterGraphics2D g2 = new PdfPrinterGraphics2D(cb, c_document.getPageSize().getWidth(), c_document.getPageSize()
        .getHeight(), new DefaultFontMapper(), PrinterJob.getPrinterJob());
    chart.draw(g2, r2D);
    g2.dispose();
    //    
    //    cb.saveState();
    //    cb.setLineWidth(1);
    //    cb.setRGBColorFill(0x8B, 0x20, 0x00);
    //    cb.setRGBColorStroke(0x00, 0x20, 0x40);
    //    cb.rectangle(ulx, uly, chartWidth, chartHeight);
    //    cb.fillStroke();
    //    cb.restoreState();

    //il rettangolo passato come parametro è solo il rettangolino del chunk.
    //Il paragrafo che contiene il chunk (outerPara) è alto quanto il grafico, il chunk
    //è allineato in basso.
    //Devo disegnare il grafico a partire dalla baseline del chunk, quindi devo sottrarre l'altezza del
    //chunk altrimenti mi disegna il grafico sopra il chunk e lascia un piccolo spazio sotto
    //cb.addTemplate(tp, left, rect.getTop() - rect.getHeight() + c_marginBottom);
  }

  @Override
  public void addElement(Elemento figlio) throws GenerateException {
    // E' un elemento finale non è possibile aggiungere altri elementi
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.stampa.output.Elemento#aggiungiElementi(java.util.List)
   */
  @Override
  public void addElements(List<Elemento> figli) throws GenerateException {
    // E' un elemento finale non è possibile aggiungere altri elementi
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
    return "Chart" + getElementID();
  }

  @Override
  public Object evaluate(Symbol symbol) throws ResolveException {
    return c_chartElem.evaluate(symbol, c_currentGroup);
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
