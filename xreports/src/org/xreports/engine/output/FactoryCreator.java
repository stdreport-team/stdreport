package org.xreports.engine.output;

import java.util.Properties;

import org.xreports.engine.output.impl.GenerateException;


public class FactoryCreator {
  public static final String    DEFAULT_FACTORY_NAME    = "default";
  public static final String    FACTORY_PROPERTIES_FILE = "factory.properties";

  /** properties file */
  private Properties            m_props;
  /** istanza singleton di questa classe */
  private static FactoryCreator m_instance;

  private FactoryCreator() throws GenerateException {
    try {
      m_props = new Properties();
      m_props.load(this.getClass().getResourceAsStream(FACTORY_PROPERTIES_FILE));
    } catch (Exception e) {
      throw new GenerateException("Non riesco a caricare il file " + FACTORY_PROPERTIES_FILE + ": " + e.toString(), e);
    }
  }

  /**
   * Restituisce l'istanza singleton di questa classe.
   * 
   * @throws GenerateException
   *           nel caso ci siano stati problemi nell'istanziazione della classe.
   */
  public static FactoryCreator getInstance() throws GenerateException {
    synchronized (FactoryCreator.class) {
      if (m_instance == null) {
        m_instance = new FactoryCreator();
      }
    }
    return m_instance;
  }

  /**
   * Restituisce l'implementazione di default per la trasformazione del report.
   * 
   * @throws GenerateException
   *           vedi {@link #getFactoryElementi(String)}
   */
  public static FactoryElementi getDefaultFactory() throws GenerateException {
    FactoryCreator fc = getInstance();
    return fc.getFactoryElementi(DEFAULT_FACTORY_NAME);
  }

  /**
   * Carica la libreria col nome richiesto. Il mapping nome->libreria è nel file {@link #FACTORY_PROPERTIES_FILE}, in cui ad ogni
   * nome è associato il nome completo della classe.
   * 
   * @param szLibreria
   *          nome della libreria
   * @return istanza della classe principale della libreria richiesta
   * @throws GenerateException
   *           nel caso una delle seguenti condizioni si verifichi:
   *           <ul>
   *           <li>non esista o non sia leggibile il file {@link #FACTORY_PROPERTIES_FILE}</li>
   *           <li>non esista la libreria chiesta in {@link #FACTORY_PROPERTIES_FILE}</li>
   *           <li>non esista o non si riesca a caricare la classe associata alla libreria richiesta</li>
   *           <li>la classe associata alla libreria richiesta non implementa l'interfaccia {@link FactoryElementi}</li>
   *           </ul>
   * 
   */
  public FactoryElementi getFactoryElementi(String szLibreria) throws GenerateException {
    String szClass = m_props.getProperty(szLibreria);
    if (szClass != null) {
      Class<?> cl;
      try {
        cl = Class.forName(szClass);
      } catch (Exception e) {
        throw new GenerateException("Non riesco a caricare la classe " + szClass + ": " + e.toString(), e);
      }
      try {
        Object obj = cl.newInstance();
        if ( ! (obj instanceof FactoryElementi)) {
          throw new GenerateException("La classe " + szClass + " non è un'istanza di " + FactoryElementi.class.getName());
        }
        return (FactoryElementi) obj;
      } catch (Exception e) {
        throw new GenerateException("Non riesco a istanziare la classe " + szClass + " per la generazione con la libreria "
            + szLibreria + ": " + e.toString());
      }
    }

    throw new GenerateException("La libreria " + szLibreria + " non è disponibile (vedi " + FACTORY_PROPERTIES_FILE + ")");
  }

  /**
   * Restituisce l'implementazione della libreria richiesta. <br>
   * Per maggiori info vedi {@link #getFactoryElementi(String)}
   * 
   * @param szLibreria
   *          nome della libreria
   * @throws GenerateException
   */
  public static FactoryElementi getFactory(String szLibreria) throws GenerateException {
    FactoryCreator fc = getInstance();
    return fc.getFactoryElementi(szLibreria);
  }
}
