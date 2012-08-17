/**
 * 
 */
package org.xreports.datagroup;

import java.util.HashMap;
import java.util.Map;


/**
 * @author pier
 * 
 */
public class RootModel extends GroupModel {
  /**
   * 
   */
  private static final long serialVersionUID = 4355571134760628360L;

  public static final String ROOT_NAME = "ROOTGROUP";

  /** mappa id->GroupModel di tutti i modelli presenti nella corrente gerarchia:
   *  la chiave di questa mappa è il modelId del GroupModel ( vedi {@link GroupModel#getModelId()} ).
   */
  private Map<Integer, GroupModel>  m_modelsMap;
  
  /** utilizzato per assegnare modelId univoci */
  private int  m_lastModelId;
  
  private RootGroup          m_root    = null;

  public RootModel() {
    super(ROOT_NAME, null);
    m_modelsMap = new HashMap<Integer, GroupModel>();
    m_lastModelId = 0;
  }

  @Override
  protected synchronized Integer addModelToCache(GroupModel model) {
    if (!m_modelsMap.containsValue(model)) {
      m_lastModelId++;
      Integer key = Integer.valueOf(m_lastModelId);
      m_modelsMap.put(key, model);
      return key;
    }    
    else {
      return null;
    }
  }

  public GroupModel getModelFromCache(Integer id) {
    return m_modelsMap.get(id);
  }

  public Integer getModelIdFromCache(GroupModel model) {
    for (Integer key : m_modelsMap.keySet())
      if (m_modelsMap.get(key)==model)
        return key;
    
    //not found
    return null;
  }
  
  private RootGroup createRootGroup() {
    RootGroup g = new RootGroup(this);
    g.addKeyFields(getKeyFields());
    g.addFields(getNotKeyFields());
    return g;
  }

  /*
   * (non-Javadoc)
   * @see ciscoop.datagroup.GroupModel#isRoot()
   */
  @Override
  public boolean isRoot() {
    return true;
  }

  /**
   * Assegna i dati alla gerarchia di gruppi. Crea, se necessario, il RootGroup padre di tutta la gerarchia.
   * 
   * @param values
   *          singola riga di dati
   * @throws GroupException
   */
  public void assignData(Map<String, Object> values) throws GroupException {
    if (m_root == null) {
      m_root = createRootGroup();
    }
    m_root.assignData(values);
  }

  /**
   * Azzera tutti i dati presenti nei gruppi capeggiati da questo RootModel.
   */
  public void clearData() {
    if (m_root != null) {
      m_root.destroy();
      m_root = null;
    }
  }


  /**
   * Ritorna il gruppo radice della gerarchia di tutti i gruppi. Tutti i dati sono contenuti in una struttura ad albero i cui nodi
   * sono i gruppi. La radice dell'albero è un'unica istanza di un gruppo speciale, il RootGroup, accessibile con questo metodo.
   * @return gruppo radice dei dati oppure <b>null</b> se nessun dato è stato assegnato
   */
  public RootGroup getRootGroup() {
    return m_root;
  }

  @Override
  public RootModel getRootModel() {
    return this;
  }

}
