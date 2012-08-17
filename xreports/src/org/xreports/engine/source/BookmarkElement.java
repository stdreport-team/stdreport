package org.xreports.engine.source;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import org.xreports.datagroup.Group;
import org.xreports.stampa.Stampa;
import org.xreports.stampa.output.Elemento;
import org.xreports.stampa.output.impl.GenerateException;
import org.xreports.stampa.validation.ValidateException;
import org.xreports.stampa.validation.XMLSchemaValidationHandler;

public class BookmarkElement extends BookmarkableElement {

  public BookmarkElement(Stampa stampa, Attributes attrs, int lineNum, int colNum) throws ValidateException {
    super(stampa, attrs, lineNum, colNum);    
  }
  
  @Override
  protected void initAttrs() {
    super.initAttrs();
  }
  
  @Override
  public void fineParsingElemento() {
  }

  @Override
  public List<Elemento> generate(Group gruppo, Stampa stampa, Elemento padre) throws GenerateException {
    try {
      salvaStampaGruppo(stampa, gruppo);

      //NB: anche se l'elemento non è visibile, torno comunuqe una lista, vuota.
      List<Elemento> listaElementi = new LinkedList<Elemento>();
      if (isBookmark()) {
        AbstractNode chunk = ((AbstractElement)getParent()).getNextChunknode(this);
        if (chunk != null && chunk instanceof ChunkElement) {
          ChunkElement chunkEl = (ChunkElement)chunk;
          chunkEl.setBookmarkElement(this);
          if (isDebugData()) {
            stampa.debugElementOpen(this, "su " + chunkEl);
          }
        }
        else if (chunk != null && chunk instanceof TextNode) {
          TextNode tn = (TextNode)chunk;
          tn.setBookmarkElement(this);
          if (isDebugData()) {
            stampa.debugElementOpen(this, "su " + tn);
          }
        }
        else {
          stampa.addWarningMessage(this + ": bookmark non generato!");
        }
      }
      return listaElementi;
    } catch (Exception e) {
      throw new GenerateException("Errore grave in generazione " + this.toString() + ":  " + e.getMessage(), e);
    }
  }

  @Override
  public String getTagName() {
    return XMLSchemaValidationHandler.ELEMENTO_BOOKMARK;
  }

  @Override
  public boolean isConcreteElement() {
    return false;
  }

  @Override
  public boolean isContentElement() {
    return false;
  }

  @Override
  public boolean isBlockElement() {
    return false;
  }

  @Override
  public boolean canChildren() {
    return false;
  }

}
