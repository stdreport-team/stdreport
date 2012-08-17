package org.xreports.engine.source;

import org.xml.sax.Attributes;

import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.Immagine;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.util.Text;

/**
 * @author pier
 * 
 */
public class BarcodeElement extends FieldElement {
  public static final String  BARCODE_EAN13  = "EAN13";
  public static final String  BARCODE_EAN8   = "EAN8";
  public static final String  BARCODE_UPCA   = "UPCA";
  public static final String  BARCODE_UPCE   = "UPCE";
  public static final String  BARCODE_128    = "128";
  public static final String  BARCODE_39     = "39";
  public static final String  BARCODE_39ext  = "39ext";
  public static final String  BARCODE_QRCODE = "qrcode";

  /** Nomi della controparte XML degli attributi del Tag "number" */
  private static final String ATTRIB_TYPE    = "type";
  /** Nomi della controparte XML degli attributi del Tag "number" */
  private static final String ATTRIB_HEIGHT  = "height";

  public BarcodeElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);
  }

  @Override
  protected void initAttrs() {
    super.initAttrs();
    addAttributo(ATTRIB_TYPE, String.class, null);
    addAttributoMeasure(ATTRIB_HEIGHT, null, false, false);
    
    //il valore di un barcode può essere sia una stringa qualsiasi che un valore che viene dal data model
    //nel caso ci sia un errore sintattico, lo ignoro: viene preso letteralmente il contenuto dell'attributo
    //value e usato per generare il barcode
    Attributo a = getAttributo(ATTRIB_VALUE);
    a.setParsingException(false);
  }

  @Override
  public String getTagName() {
    return "barcode";
  }

  public String getType() {
    Object ret = getAttrValue(ATTRIB_TYPE);
    if (ret == null) {
      return null;
    }
    return ret.toString();
  }

  @Override
  protected Elemento createOutputElement(Stampa stampa, Elemento padre) throws GenerateException {
    Immagine immagine = stampa.getFactoryElementi().creaImmagine(stampa, this, padre);
    immagine.fineGenerazione();
    return immagine;
  }

  /**
   * Controlla che il valore del barcode sia compatibile con il tipo specificato
   * 
   * @param type
   *          tipo barcode
   * @param value
   *          valore barcode
   * @throws GenerateException
   *           in caso di non validità del valore rispetto al tipo
   */
  private void checkValue(String type, String value) throws GenerateException {
    if (value == null) {
      throw new GenerateException(this, "valore del barcode non fornito");
    }
    if (type.equalsIgnoreCase(BARCODE_EAN13)) {
      checkNumberValue(value, type, 13);
    } else if (type.equalsIgnoreCase(BARCODE_UPCA)) {
      checkNumberValue(value, type, 12);
    } else if (type.equalsIgnoreCase(BARCODE_UPCE)) {
      checkNumberValue(value, type, 8);
    } else if (type.equalsIgnoreCase(BARCODE_EAN8)) {
      checkNumberValue(value, type, 8);
    } else if (type.equalsIgnoreCase(BARCODE_39)) {
      checkUpperCase(value, type);
    }
  }
  private void checkUpperCase(String value, String type) throws GenerateException {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c >= 'a' && c <= 'z') {
        throw new GenerateException(this, "Il valore del barcode '" + value + "' non è valido: il tipo " + type
            + " pretende solo lettere maiuscole");        
      }
    }
  }

  private void checkNumberValue(String value, String type, Integer requiredLen) throws GenerateException {
    if ( !Text.isNumeric(value)) {
      throw new GenerateException(this, "Il valore del barcode '" + value + "' non è valido: il tipo " + type
          + " pretende solo cifre");
    }
    if (requiredLen != null) {
      if (value.length() != requiredLen.intValue()) {
        throw new GenerateException(this, "Il valore del barcode '" + value + "' non è valido: il tipo " + type + " pretende "
            + requiredLen + " caratteri");
      }
    }
  }

  public Measure getHeight() {
    return getAttrValueAsMeasure(ATTRIB_HEIGHT);
  }
  
  /*
   * (non-Javadoc)
   * @see
   * ciscoop.stampa.source.FieldElement#calcola(ciscoop.datagroup.DataFieldAuto)
   */
  @Override
  protected String getFormattedValue(Object value) throws GenerateException {
    String vv = super.getFormattedValue(value);
    checkValue(getType(), vv);
    return vv;
  }
}
