/**
 * 
 */
package org.xreports.engine.source;

import java.util.Scanner;

import org.xreports.stampa.validation.ValidateException;

/**
 * Rappresenta i 4 margini di vari elementi grafici. E' in pratica un array di 4 {@link Measure}, una per lato.
 * 
 */
public class Margini {
  //In senso orario come l'orologio
  private static final int TOP    = 0;
  private static final int RIGHT  = 1;
  private static final int BOTTOM = 2;
  private static final int LEFT   = 3;

  private Measure[]        m_measures;

  public Margini(String margini) throws ValidateException {
    m_measures = new Measure[4];
    identificaMargini(margini);
  }

  private void identificaMargini(String szVal) throws ValidateException {
    Scanner scanner = new Scanner(szVal);
    scanner.useDelimiter(" ");
    int mCount = 0;
    while (scanner.hasNext()) {
      String szToken = scanner.next();
      m_measures[mCount] = new Measure(szToken);
      if (mCount++ > 3) {
        throw new ValidateException("non si possono specificare più di 4 margini");
      }
    }
    if (mCount == 2) {
      //solo due margini, quindi il primo è top+bottom, il secondo left+right
      m_measures[BOTTOM] = m_measures[TOP];
      m_measures[LEFT] = m_measures[RIGHT];
    } else if (mCount == 1) {
      //solo 1 margine --> tutti uguali
      m_measures[BOTTOM] = m_measures[TOP];
      m_measures[LEFT] = m_measures[TOP];
      m_measures[RIGHT] = m_measures[TOP];
    }
  }

  public Measure getTop() {
    return m_measures[TOP];
  }

  public void setTop(Measure top) {
    m_measures[TOP] = top;
  }
  
  public Measure getRight() {
    return m_measures[RIGHT];
  }
  public void setRight(Measure right) {
    m_measures[RIGHT] = right;
  }

  public Measure getBottom() {
    return m_measures[BOTTOM];
  }
  public void setBottom(Measure bottom) {
    m_measures[BOTTOM] = bottom;
  }

  public Measure getLeft() {
    return m_measures[LEFT];
  }
  public void setLeft(Measure left) {
    m_measures[LEFT] = left;
  }

}
