/**
 * 
 */
package org.xreports.datagroup;

import java.util.Comparator;
import java.util.Set;

/**
 * @author pier
 * 
 */
public class GroupComparator implements Comparator<Group> {
  private Set<String> m_nomiCampo     = null;
  private boolean      m_caseSensitive = true;
  private boolean      m_ascending     = true;

  /**
   * Crea un oggetto che compara due gruppi rispetto all'elenco ordinato di campi passati.
   * 
   * @param nomi
   *          elenco campi
   * @param caseSensitive
   *          true se il confronto fra stringhe è case-sensitive
   * @param ascending
   *          true se ordinamento crescente, false se decrescente
   * 
   * @throws CISException
   *           nel caso la lista di campi sia vuota
   */
  public GroupComparator(Set<String> nomi, boolean caseSensitive, boolean ascending) throws GroupException {
    if (nomi == null || nomi.size() == 0) {
      throw new GroupException("Non si può passare una lista vuota di nomi");
    }
    m_nomiCampo = nomi;
    m_caseSensitive = caseSensitive;
    m_ascending = ascending;
  }

  /**
   * Confronta i due gruppi passati in base all'elenco di campi passati nel costruttore. Due gruppi sono uguali (ritorna 0) se tutti
   * i campi (nella rappresentazione stringa) sono uguali. <br/>
   * Ritorna un intero positivo se g1 è maggiore di g2, nel senso che il primo campo diverso fra i due gruppi è maggiore in g1
   * rispetto a g2. <br/>
   * Ritorna un intero negativo se g1 è minore di g2, nel senso che il primo campo diverso fra i due gruppi è minore in g1 rispetto
   * a g2.
   * 
   */
  @Override
  public int compare(Group g1, Group g2) {
    int fatt = m_ascending ? 1 : -1;
    for (String nomeCampo : m_nomiCampo) {
      Object v1;
      Object v2;
      try {
        v1 = getCampoValue(g1, nomeCampo);
        v2 = getCampoValue(g2, nomeCampo);
      } catch (Exception e) {
        throw new IllegalStateException("Errore grave in lettura dei campi: " + e.toString());
      }
      int compare = 0;
      //gestione di uno o 2 valori null
      if (v1 == null || v2 == null) {
        if (v1 == null && v2 == null) {
          compare = 0;
        } else if (v1 == null) {
          compare = -1;
        } else {
          compare = 1;
        }
      } else {
        //nessuno dei due è null: uso Comparable
        if (v1 instanceof Number && v2 instanceof Number) {
          NumberComparator nc = new NumberComparator();
          compare = nc.compare((Number)v1, (Number)v2);
        }
        else {
          if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>) {
            compare = ((Comparable) v1).compareTo((Comparable) v2);
          }          
        }
      }
      //se i due gruppi su questo campo sono diversi, mi fermo qui
      //altrimenti passo al prossimo
      if (compare != 0) {
        return compare * fatt;
      }
    }

    //se arrivo qui tutti i campi sono uguali, quindi torno 0
    return 0;
  }

  private Object getCampoValue(Group g, String nomeCampo) throws GroupException {
    DataField campo = g.getField(nomeCampo);
    if (campo == null) {
      throw new GroupException("Il campo '" + nomeCampo + "' non esiste nel gruppo " + g.getName());
    }
    //per evitare NullPointerException, se il campo non esiste torno stringa vuota
    if (campo.isEmptyNullOrZero()) {
      return null;
    }
    if ( !campo.isString()) {
      return campo.getValue();
    }
    return (m_caseSensitive ? campo.getAsStringSafe() : campo.getAsStringSafe().toLowerCase());
  }
}
