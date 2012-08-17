/**
 * 
 */
package org.xreports.datagroup;

import org.xreports.datagroup.DataFieldAutoModel.TipoOper;

/**
 * @author pier
 *
 */
public class DataFieldAuto extends DataField {

  /**
   * 
   */
  private static final long serialVersionUID = -8485820620537423079L;
  private boolean                    m_isCalcolato    = false;

  
  public DataFieldAuto(DataFieldModel myModel) {
    super(myModel);
  }

  private DataFieldAutoModel getAutoModel()  {
    return (DataFieldAutoModel)getModel();
  }
  
  
  /**
   * Calcola il valore di questo campo chiamando le opportune funzioni. <br>
   * Dopo questa chiamata, isCalcolato() ritorna true.
   * 
   * @see isCalcolato()
   */
  @Override
  public void calcola() {
    if (isCalcolato())
      return;

    try {
      Group gruppo = getGroup();
      resetErrorState();
      TipoOper op = getAutoModel().getOperation();
      String gruppoPadre = getAutoModel().getGruppoPadre();
      String campoPadre = getAutoModel().getCampoPadre();
      
      if (getModel().equals(TipoOper.SOMMAFIGLI)) {
        setValue(gruppo.sommaFigli(gruppoPadre, campoPadre));
      } else if (op.equals(TipoOper.SOMMADISCEN)) {
        setValue(gruppo.sommaDiscen(gruppoPadre, campoPadre));
      } else if (op.equals(TipoOper.CONTEGGIODISCEN)) {
        setValue(new Integer(gruppo.getDescendantGroupCount(gruppoPadre)));
      } else if (op.equals(TipoOper.PERC_PADRE)) {
        setValue(gruppo.percPadre(gruppoPadre, campoPadre, getAutoModel().getNomeCampoRif()));
      } else if (op.equals(TipoOper.DIFF_PERC_PADRE)) {
        setValue(gruppo.diffPercPadre(gruppoPadre, campoPadre, getAutoModel().getNomeCampoRif()));
      } else if (op.equals(TipoOper.MEDIAFIGLI)) {
        setValue(gruppo.mediaFigli(gruppoPadre, campoPadre));
      } else if (op.equals(TipoOper.CAMPOREF)) {
        Group g = gruppo;
        if (gruppoPadre != null) {
          g = g.getAncestorGroup(gruppoPadre);
        }
        if (g != null) {
          setValue(g.getField(campoPadre).getValue());
        } else {
          setValue(null);
        }
      } else if (op.equals(TipoOper.MATH_DIVIDE) 
           || op.equals(TipoOper.MATH_MULTIPLY) 
           || op.equals(TipoOper.MATH_SUBTRACT)) {

        String gruppoPadre2 = getAutoModel().getGruppoPadre2();
        String campoPadre2 = getAutoModel().getCampoPadre2();
        
        DataField campo1 = getCampoByName(gruppoPadre, campoPadre);
        DataField campo2 = getCampoByName(gruppoPadre2, campoPadre2);
        Number divisore = getAutoModel().getDivisore();
        Number moltiplicatore = getAutoModel().getMoltiplicatore();
        Number sottrattore = getAutoModel().getSotrattore();
        if (op.equals(TipoOper.MATH_DIVIDE)) {
          if (campo1 != null && campo2 != null) {
            if (campo1.isNull() || campo2.isNull()) {
              setValue(null);
            } else {
              setValue(new Double(campo1.getAsDouble() / campo2.getAsDouble()));
            }
          } else if (divisore != null) {
            if (campo1.isNull()) {
              setValue(null);
            } else {
              setValue(new Double(campo1.getAsDouble() / divisore.doubleValue()));
            }
          }
        }
        if (op.equals(TipoOper.MATH_MULTIPLY)) {
          if (campo1 != null && campo2 != null) {
            if (campo1.isNull() || campo2.isNull()) {
              setValue(null);
            } else {
              setValue(new Double(campo1.getAsDouble() * campo2.getAsDouble()));
            }
          } else if (moltiplicatore != null) {
            if (campo1.isNull()) {
              setValue(null);
            } else {
              setValue(new Double(campo1.getAsDouble() * moltiplicatore.doubleValue()));
            }
          }
        }
        if (op.equals(TipoOper.MATH_SUBTRACT)) {
          if (campo1 != null && campo2 != null) {
            setValue(new Double(campo1.getAsDouble() - campo2.getAsDouble()));
          } else if (sottrattore != null) {
            setValue(new Double(campo1.getAsDouble() - sottrattore.doubleValue()));
          }
        }
      } else if (op.equals(TipoOper.CALC_USER)) {
        if (getAutoModel().getListener() != null) {
          getAutoModel().getListener().evaluateField(this);
        }
      }
      m_isCalcolato = true;
    } catch (Exception e) {
      e.printStackTrace();
      setErrorState("Errore nel calcolo del campo: " + e.toString());
    }
  }

  
  /**
   * Restituisce il campo appartenente al gruppo passato e con il nome passato.
   * Se il gruppo è null, il campo appartiene a questo gruppo, altrimenti al
   * gruppo antenato di questo col nome passato.
   * 
   * @param gruppo
   *          del campo
   * @param nome
   *          del campo
   * @return Campo o null se non trovato
   */
  private DataField getCampoByName(String gruppo, String nome) {
    try {
      if (nome == null) {
        return null;
      }

      Group g = null;

      if (gruppo != null && gruppo.length() > 0) {
        g = getGroup().getAncestorGroup(gruppo);
      } else {
        g = getGroup();
      }
      return g.getField(nome);
    } catch (RuntimeException e) {
      return null;
    }
  }

  
  
  @Override
  public boolean isNull() {
    calcola();
    return super.isNull();
  }


  /**
   * Resetta il valore del campo e anche il flag calcolato, cioè dopo questa
   * chiamata isNull() è true e isCalcolato() è false.
   * 
   */
  @Override
  public void resetValue() {
    super.resetValue();
    m_isCalcolato = false;
  }

  /**
   * Ritorna true sse il metodo calcola è già stato chiamato e il campo non è
   * stato resettato (vedi resetValue).
   * 
   * @return se il campo è già stato calcolato
   */
  @Override
  public boolean isCalcolato() {
    return m_isCalcolato;
  }

  /**
   * Forza lo stato di calcolato al campo.
   * 
   */
  public void setCalcolato(boolean bCalcolato) {
    m_isCalcolato = bCalcolato;
  }


  @Override
  public Object getValue() {
    calcola();
    return super.getValue();
  }

  @Override
  public long getAsLong() {
    calcola();
    return super.getAsLong();
  }

  @Override
  public int getAsInt() {
    calcola();
    return super.getAsInt();
  }

  @Override
  public double getAsDouble() {
    calcola();
    return super.getAsDouble();
  }  

  public boolean isAuto() {
    return true;
  }
  
}
