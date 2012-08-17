/**
 * 
 */
package org.xreports.expressions.symbols;


/**
 * @author pier
 *
 */
public class EvaluateException  extends Exception  {

	/**
   * 
   */
  private static final long serialVersionUID = 6637688450010707412L;

  public EvaluateException() {
		super();
	}

  public EvaluateException(String message, Object... args) {
    super(String.format(message, args));
  }
	
  
  public EvaluateException(Throwable cause) {
    super(cause);
  }
  

  public EvaluateException(Throwable cause, String message, Object... args) {
    super(String.format(message, args), cause);
  }

}
