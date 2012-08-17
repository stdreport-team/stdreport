/**
 * 
 */
package org.xreports.engine.output;

/**
 * @author pier
 * 
 */
public class Rettangolo {
  /** This is the letter format */
  public static final Rettangolo LETTER          = new Rettangolo(612, 792);

  /** This is the a2 format */
  public static final Rettangolo A2              = new Rettangolo(1191, 1684);

  /** This is the a3 format */
  public static final Rettangolo A3              = new Rettangolo(842, 1191);

  /** This is the a4 format */
  public static final Rettangolo A4              = new Rettangolo(595, 842);

  /** This is the a5 format */
  public static final Rettangolo A5              = new Rettangolo(420, 595);

  /** This is the a6 format */
  public static final Rettangolo A6              = new Rettangolo(297, 420);

  /** This is the legal format */
  public static final Rettangolo LEGAL           = new Rettangolo(612, 1008);

  /** The rotation of the Rectangle */
  protected int                  rotation        = 0;

  /** the lower left x-coordinate. */
  protected float                llx;

  /** the lower left y-coordinate. */
  protected float                lly;

  /** the upper right x-coordinate. */
  protected float                urx;

  /** the upper right y-coordinate. */
  protected float                ury;

  /** This is the color of the background of this rectangle. */
  protected Colore               backgroundColor = null;

  /**
   * Constructs a <CODE>Rectangle</CODE> -object.
   * 
   * @param llx
   *          lower left x
   * @param lly
   *          lower left y
   * @param urx
   *          upper right x
   * @param ury
   *          upper right y
   */
  public Rettangolo(float llx, float lly, float urx, float ury) {
    this.llx = llx;
    this.lly = lly;
    this.urx = urx;
    this.ury = ury;
  }

  /**
   * Constructs a <CODE>Rectangle</CODE> -object starting from the origin (0, 0).
   * 
   * @param urx
   *          upper right x
   * @param ury
   *          upper right y
   */
  public Rettangolo(float urx, float ury) {
    this(0, 0, urx, ury);
  }

  /**
   * Rotates the rectangle. Swaps the values of llx and lly and of urx and ury.
   * 
   * @return the rotated <CODE>Rectangle</CODE>
   */
  public Rettangolo rotate() {
    Rettangolo rect = new Rettangolo(lly, llx, ury, urx);
    rect.rotation = rotation + 90;
    rect.rotation %= 360;
    return rect;
  }

  /**
   * Coordinata x del vertice sinistro inferiore.
   */
  public float getLLx() {
    return llx;
  }

  /**
   * Coordinata y del vertice sinistro inferiore.
   */
  public float getLLy() {
    return lly;
  }

  /**
   * Coordinata x del vertice destro superiore.
   */
  public float getURx() {
    return urx;
  }

  /**
   * Coordinata y del vertice destro superiore.
   */
  public float getURy() {
    return ury;
  }

  public float getHeight() {
    return ury - lly;
  }

  public float getWidth() {
    return urx - llx;
  }
  
}
