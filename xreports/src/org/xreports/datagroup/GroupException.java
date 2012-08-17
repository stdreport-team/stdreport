/**
 * 
 */
package org.xreports.datagroup;


/**
 * @author pier
 * 
 */
public class GroupException extends Exception {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public GroupException(String message, Object... params) {
    super(String.format(message, params));
  }

  
  public GroupException(Throwable cause, String message, Object... params) {
    super(String.format(message, params), cause);
  }

  public GroupException(Throwable cause) {
    super(cause);
  }

}
