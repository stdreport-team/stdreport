/**
 * 
 */
package org.xreports.datagroup;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * @author pier
 * 
 */
public class NumberComparator implements Comparator<Number> {

  /*
   * (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Number a, Number b) {
    return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
  }
}
