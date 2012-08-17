package org.xreports.datagroup;

import java.util.Map;

import org.xreports.expressions.symbols.EvaluateException;
import org.xreports.engine.DataException;

public class RootGroup extends Group {
  /**
   *    */
  private static final long serialVersionUID = -8692331675319647012L;
  
  int m_lastGroupID = -1; //ultimo identificativo univoco di gruppo assegnato

  /**
   * Costruttore standard.
   * 
   * @param root
   *          oggetto a capo della intera gerarchia dei gruppi
   * @param model
   *          modello di riferimento del gruppo
   * @param list
   *          lista di cui fa parte il gruppo
   */
  public RootGroup(RootModel model) {
    super();
    if (model==null) {
      throw new NullPointerException("GroupModel can't be null");
    }
    _initID(this);
    setModel(model);
    //inizializzo le liste dei possibili gruppi figli di questo
    for (GroupModel childModel : model.getChildModels()) {
      addChildList(childModel, this);
    }
  }

  public GroupModel getModelFromCache(Integer id) {
    if (m_model != null) {
      return ((RootModel)m_model).getModelFromCache(id);      
    }
    
    return null;
  }
  
  
  @Override
  public boolean isRoot() {
    return true;
  }

  /**
   * Restituisce l'ultimo ID di gruppo univoco assegnato. Equivale alla quantita' di gruppi generata finora.
   * 
   * @return ultimo ID di gruppo
   */
  public int getLastGroupID() {
    return m_lastGroupID;
  }

  /**
   * Ritorna il prossimo ID di gruppo incrementando il contatore interno.
   * 
   * @return prossimo ID di gruppo
   */
  int nextID() {
    if (m_lastGroupID < 0)
      m_lastGroupID = 0;
    return (++m_lastGroupID);
  }

  /**
   * Testa se la stringa passata è un nome corretto, cioè se è composto solo da lettere o numeri o "_" e se il primo carattere è una
   * lettera.
   * 
   * @param nome
   *          String stringa da testare
   * @return boolean true sse nome è un nome di campo valido
   */
  public static boolean checkNome(String nome) {
    char[] ca = nome.toCharArray();
    for (int i = 0; i < ca.length; i++) {
      if ( !Character.isLetterOrDigit(ca[i])) {
        if (ca[i] != '_') {
          return false;
        }
      }
      if (i == 0) {
        if ( !Character.isLetter(ca[i])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Utility method per avere semplicemente l'XML di un campo.
   * 
   * @param elemName
   *          nome tag XML
   * @param attributi
   *          attributi già formattati
   * @param value
   *          valore campo
   * @return XML del campo
   */
  public static String getXMLElem(String elemName, String attributi, String value) {
    String attrs = attributi.trim();
    return "<" + elemName + " " + attrs + ">" + value + "</" + elemName + ">";
  }

  @Override
  public Group getParent() {
    return null;
  }

  @Override
  public void assignData(Map<String, Object> values) throws GroupException {
    if ( !hasData()) {
      copyValues(values);
    }
    else {
      addValues(values);
    }

    m_hasData = true;

    for (GroupList list : getChildLists().values()) {
      try {
        list.assignData(values);
      } catch (GroupException e) {        
        throw e;
      } catch (EvaluateException e) {
        throw new GroupException(e);
      }
    }
  }

}
