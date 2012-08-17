/**
 * 
 */
package org.xreports.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xreports.datagroup.RootModel;

import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.engine.DataException;
import org.xreports.engine.output.Documento;
import org.xreports.engine.output.Elemento;
import org.xreports.engine.output.StileCarattere;
import org.xreports.engine.output.impl.GenerateException;
import org.xreports.engine.source.GroupElement;
import org.xreports.engine.source.TextElement;
import org.xreports.engine.source.TextNode;
import org.xreports.engine.validation.ValidateException;

/**
 * @author pier
 * 
 */
public class MainReportInfo extends ReportInfo {

  /**
   * Costruttore report con dati derivanti da query.
   * 
   * @param stp oggetto stampa di riferimento
   * @param szQuery testo query
   * @param rootModel modello radice
   * @param groupElem elemento gruppo radice 
   * @throws ValidateException in caso di parametri incongruenti
   */
  public MainReportInfo(XReport stp, String szQuery, RootModel rootModel, GroupElement groupElem) throws ValidateException {
    super(stp, rootModel, groupElem);
    setQuery(szQuery);
  }

  /**
   * Costruttore report con dati passati manualmente.
   * 
   * @param stp oggetto stampa di riferimento
   * @param dataList dati passati manualmente a oggetto {@link XReport}
   * @param rootModel modello radice
   * @param groupElem elemento gruppo radice 
   * @throws ValidateException in caso di parametri incongruenti
   */
  public MainReportInfo(XReport stp, List<HashMap<String, Object>> dataList, RootModel rootModel, GroupElement groupElem) throws ValidateException {
    super(stp, rootModel, groupElem);
    setDataList(dataList);
  }

  /**
   * Costruttore report senza dati esterni.
   * 
   * @param stp oggetto stampa di riferimento
   * @param szQuery testo query
   * @param rootModel modello radice
   * @param groupElem elemento gruppo radice 
   * @throws ValidateException in caso di parametri incongruenti
   */
  public MainReportInfo(XReport stp, RootModel rootModel, GroupElement groupElem) throws ValidateException {
    super(stp, rootModel, groupElem);
  }
  
  @Override
  public boolean isMainReport() {
    return true;
  }


  @Override
  public int caricaDati() throws DataException {
    try {
      loadDataStart();
      if (getQuery() != null) {
        return super.loadQuery(getQuery());
      }
      else if (getDataList() != null) {
        return super.loadDataList(getDataList(), false);
      }
      return -1;
    } catch (DataException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new DataException(m_groupElem, e, "Errore inaspettato in caricamento dati");
    } finally {
      loadDataEnd();
    }

  }

  public void generate() throws GenerateException, EvaluateException {
    Documento doc = m_stampa.getDocumento();
    m_groupElem.setGroup(m_rootModel.getRootGroup());
    List<Elemento> listaElementi = new LinkedList<Elemento>();
    while (m_stampa.getGenerationStatus() != XReport.GenerationStatus.END_PDFGEN) {
      if (m_stampa.getGenerationStatus() == XReport.GenerationStatus.GOTO_NEXTPAGE) {
        doc.saltaPagina();
        m_stampa.setGenerationStatus(XReport.GenerationStatus.CONTINUE_PDFGEN);
      }
      listaElementi = m_groupElem.generate(m_rootModel.getRootGroup(), m_stampa, doc);
      doc.addElements(listaElementi);
    }
  }

  public void generateEmptyDoc() throws GenerateException, ValidateException, EvaluateException {
    Documento doc = m_stampa.getDocumento();
    TextElement noDataMess = new TextElement(null, 0, 0);
    noDataMess.setRefFontName(StileCarattere.SYSTEM_DEFAULT_NAME);
    TextNode noDataText = new TextNode(m_stampa.getNoDataMessage(), 0, 0);
    noDataMess.addChild(noDataText);
    doc.addElements(noDataMess.generate(null, m_stampa, doc));
  }
  
  
  @Override
  public void destroy() {
    super.destroy();
  }
}
