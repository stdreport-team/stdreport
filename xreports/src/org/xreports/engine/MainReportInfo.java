/**
 * 
 */
package org.xreports.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xreports.datagroup.RootModel;
import org.xreports.dmc.SimpleList;
import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.stampa.DataException;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Documento;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.StileCarattere;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.source.GroupElement;
import org.xreports.stampa.source.TextElement;
import org.xreports.stampa.source.TextNode;
import org.xreports.stampa.validation.ValidateException;

/**
 * @author pier
 * 
 */
public class MainReportInfo extends ReportInfo {
  private SimpleList m_list = null;

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
   * Costruttore report con dati derivanti da SimpleList.
   * 
   * @param stp oggetto stampa di riferimento
   * @param szQuery testo query
   * @param rootModel modello radice
   * @param groupElem elemento gruppo radice 
   * @throws ValidateException in caso di parametri incongruenti
   */
  public MainReportInfo(XReport stp, SimpleList list, RootModel rootModel, GroupElement groupElem) throws ValidateException {
    super(stp, rootModel, groupElem);
    m_list = list;
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

  /**
   * @return il m_list
   */
  public SimpleList getSimpleList() {
    return m_list;
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
      return super.loadList(m_list);
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
    if (m_list != null) {
      m_list.destroy();
      m_list = null;
    }
  }
}
