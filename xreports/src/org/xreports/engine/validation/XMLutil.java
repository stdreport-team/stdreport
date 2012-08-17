package org.xreports.engine.validation;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

public class XMLutil {
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  private static final String JAXP_SCHEMA_SOURCE   = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  private static final String W3C_XML_SCHEMA       = "http://www.w3.org/2001/XMLSchema";


  //  private static void which(Class<?> aClass)  {
  //    try {
  //      String clsName = aClass.getName();
  //      if (aClass.getProtectionDomain().getCodeSource()!=null) {
  //        System.out.println(clsName + " caricata da " + aClass.getProtectionDomain().getCodeSource().getLocation());
  //      } 
  //      else {
  //        String rn = clsName.replace('.', '/') + ".class";
  //        URL resource = ClassLoader.getSystemResource(rn);
  //        if (resource!=null) {
  //        	 String res= resource.toString();
  //           int i = res.indexOf('!');
  //           if (i>=0)
  //             System.out.println("Classe " + res.substring(i+2) + " caricata dal jar " + res.substring(0,i));
  //           else
  //             System.out.println(clsName  + " caricata da " + res);
  //        }
  //      }
  //    } 
  //    catch (Exception e) {
  //    }
  //  }

  public static void saxParserValidation(InputStream streamDaValidare, InputStream inputSchema, DefaultHandler handler)
      throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    if (inputSchema != null) {
      factory.setValidating(true);
    } else {
      factory.setValidating(false);
    }
    SAXParser parser = factory.newSAXParser();
    if (inputSchema != null) {
      parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      parser.setProperty(JAXP_SCHEMA_SOURCE, inputSchema);
    }
    parser.parse(streamDaValidare, handler);
  }

  //  /** Validate */
  //  public static void validate(File xml, File schema) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException {
  //    SAXParserFactory factory = SAXParserFactory.newInstance();
  //    factory.setNamespaceAware(true);
  //    factory.setValidating(true);
  //    SAXParser parser = factory.newSAXParser();
  //
  //    parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
  //    parser.setProperty(JAXP_SCHEMA_SOURCE, schema);
  //    DefaultHandler handler = new ValidationHandler();
  //    parser.parse(new FileInputStream(xml), handler);
  //  }
  //  
  //  /** Carica il DOM a partire dal nome del file XML
  //   *  
  //   *  @throws Exception 
  //   */
  //  public static Document caricaDOMDocument(String nomeFileXML) throws Exception {
  //    return caricaDOMDocument(new File(nomeFileXML));
  //  }
  //  
  /**
   * Carica il DOM a partire da un file contenente un documento XML
   * 
   * @throws Exception
   */
  public static Document caricaDOMDocument(File fileXML) throws Exception {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document domDocument = builder.parse(fileXML);
    if (domDocument == null) {
      throw new Exception("Errore in lettura del Document del file XML di configurazione");
    }
    return domDocument;
  }
}
