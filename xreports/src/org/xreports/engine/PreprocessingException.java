/**
 * 
 */
package org.xreports.stampa;

/**
 * @author pier
 * 
 */
public class PreprocessingException extends StampaException {
  private static final long serialVersionUID = 1L;

  public PreprocessingException() {
    super();
  }

  public PreprocessingException(String s) {
    super(s);
  }

  public PreprocessingException(Throwable cause, String s) {
    super(cause, s);
  }

  public PreprocessingException(Throwable cause) {
    super(cause);
  }

  @Override
  protected String getExceptionType() {
    return "pre-processing";
  }  
  
}
