package org.xreports.engine.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.xreports.engine.XReport;
import org.xreports.util.FileUtil;
import org.xreports.util.Text;

/**
 * Parser SAX SAX and DOM differences
 * 
 * Both SAX and DOM are used to parse the XML document. Both has advantages and disadvantages and can be used in our programming
 * depending on the situation. SAX: 1. Parses node by node 2. Doesnt store the XML in memory 3. We cant insert or delete a node 4.
 * Top to bottom traversing DOM 1. Stores the entire XML document into memory before processing 2. Occupies more memory 3. We can
 * insert or delete nodes 4. Traverse in any direction.
 * 
 * If we need to find a node and doesnt need to insert or delete we can go with SAX itself otherwise DOM provided we have more
 * memory.
 */
public final class XMLIncludeValidationHandler extends DefaultHandler {

  private XReport         c_stampa                 = null;
  private File           c_outputFile             = null;
  private BufferedWriter c_fileWriter             = null;
  private List<String>   c_lines                  = null;
  private String         c_encoding;

  private Locator        m_locator;

  private int            c_previousIncludeLineNum = 0;

  public XMLIncludeValidationHandler(XReport stampa, File outputFile, List<String> lines, String encoding) {
    c_stampa = stampa;
    c_outputFile = outputFile;
    c_lines = lines;
    c_encoding = encoding;
    c_stampa.addDebugMessage("Scrivo xml con includes in " + outputFile.getAbsolutePath());
  }

  /** Inizio parsing del Documento XML */
  @Override
  public void startDocument() throws SAXException {
    try {
      super.startDocument();
      c_fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(c_outputFile), c_encoding));
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      SAXException saxe = new SAXException(e);
      throw saxe;
    }
  }

  @Override
  public void endDocument() throws SAXException {
    try {
      super.endDocument();
      writeLines(c_previousIncludeLineNum + 1, c_lines.size());
      c_fileWriter.close();
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      SAXException saxe = new SAXException(e);
      throw saxe;
    }
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    m_locator = locator;
  }

  private void writeContent(String text) throws ValidateException {
    try {
      c_fileWriter.write(text);
    } catch (IOException e) {
      throw new ValidateException("Non riesco a scrivere su " + c_outputFile.getAbsolutePath() + ", " + e.getMessage());
    }
  }

  /**
   * Scrive le linee da start a end, dal buffer di linee c_lines al file di output.
   * 
   * @param start
   *          linea di partenza (1-based)
   * @param end
   *          linea finale (1-based)
   * @throws ValidateException
   *           in caso di errori di IO o altro
   */
  private void writeLines(int start, int end) throws ValidateException {
    try {
      start--;
      end--;
      end = Math.min(end, c_lines.size() - 1);
      for (int i = start; i <= end; i++) {
        c_fileWriter.write(c_lines.get(i) + '\n');
      }
    } catch (IOException e) {
      throw new ValidateException("Non riesco a scrivere su " + c_outputFile.getAbsolutePath() + ", " + e.getMessage());
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);

    try {
      int line = 0, col = 0;
      if (m_locator != null) {
        line = m_locator.getLineNumber();
        col = m_locator.getColumnNumber();
      }
      if (localName.equals("include")) {
        elaboraInclude(attributes, line, col);
      }
    } catch (Exception e) {
      SAXException saxe = new SAXException(e);
      throw saxe;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
  }

  /**
   * @throws ValidateException
   */
  private void elaboraInclude(Attributes attributes, int line, int col) throws ValidateException {
    String pathAbsOk = null;
    try {
      String path = attributes.getValue("src");
      if (!Text.isValue(path)) {
        throw new ValidateException("Manca l'attributo 'src' nell'elemento include, linea " + line);
      }
      String encoding = attributes.getValue("encoding");
      if (!Text.isValue(encoding)) {
        encoding = null;
      }
      
      String content = null;
      String elementIncluded = null;
      if (path.trim().startsWith("$")) {
        //content = variable
        String varname = path.trim().substring(1);
        if (varname.startsWith("{") && varname.endsWith("}"))
          varname = varname.substring(1, varname.length() - 1);
        
        if (!c_stampa.existParameter(varname)) {
          throw new ValidateException("Variabile " + varname + " non definita, elemento include, linea " + line);          
        }
        content = (String)c_stampa.getParameterValue(varname);
        c_stampa.addInfoMessage("Elemento include, linea " + line + ": inclusa costante " + varname);
        elementIncluded = "constant:" + varname;
      }
      else {
        //content=file
        pathAbsOk = c_stampa.findResource(path);
        if (pathAbsOk == null) {
          throw new ValidateException("Non riesco a trovare il file da includere: '" + path + "',  linea " + line);
        }
        File fileInclude = new File(pathAbsOk);
        content = FileUtil.readFileToString(fileInclude, encoding);
        c_stampa.addInfoMessage("Elemento include, linea " + line + ": letto il file " + pathAbsOk);
        elementIncluded = "file:" + pathAbsOk;
      }
      
      
      String[] includeLines = content.split("\n");
      c_stampa.addIncludeFile(elementIncluded, line, includeLines.length);
      writeLines(c_previousIncludeLineNum + 1, line - 1);
      String currentLine = c_lines.get(line - 1);      
      int startInclude = currentLine.indexOf("<include ");
      if (startInclude > 0) {
        String preInclude = currentLine.substring(0, startInclude);
        content = preInclude + content;
      }
      String postInclude = currentLine.substring(col - 1);
      content += postInclude;
      /*
       * //determino colonna iniziale e finale del tag include String includeLine = c_lines.get(line - 1); //line è 1-based int
       * iStart = includeLine.indexOf("<include "); if (iStart < 0) { throw new
       * ValidateException("Errore grave in analisi elemento include, linea " + line + ",  colonna " + col +
       * ": non trovo inizio tag!"); } int iEnd = includeLine.indexOf("</include>", iStart + 8); if (iEnd < 0) iEnd =
       * includeLine.indexOf("/>", iStart + 8);
       */
      writeContent(content);
      c_previousIncludeLineNum = line;
    } catch (IOException e) {
      throw new ValidateException("Non riesco a leggere il file da includere '" + pathAbsOk + "'", e);
    } catch (ValidateException ve) {
      throw ve;
    } catch (Exception e) {
      throw new ValidateException("Errore grave in analisi elemento include, linea " + line + ",  colonna " + col, e);
    }
  }

  //Incontriammo un nodo testo
  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    super.characters(ch, start, length);
  }

  @Override
  public void error(SAXParseException e) throws SAXParseException {
    final String prefix = "cvc-complex-type";
    String msg = e.getMessage();
    if (msg.startsWith(prefix)) {
      //cerco il primo ':' dopo il prefisso e scarto fino a lì
      int i = msg.indexOf(':', prefix.length());
      if (i < 0) {
        i = prefix.length();
      }
      msg = msg.substring(i + 1).trim();
    }
    c_stampa.addErrorMessage("Errore di validazione: linea " + e.getLineNumber() + ", colonna " + e.getColumnNumber() + ": " + msg);
    throw e;
  }

  @Override
  public void warning(SAXParseException e) throws SAXParseException {
    c_stampa.addInfoMessage("Warning linea " + e.getLineNumber() + ": " + e.toString());
    throw e;
  }

}
