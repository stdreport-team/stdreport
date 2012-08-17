package org.xreports.engine.output.impl.itext;

import org.xreports.engine.ResolveException;
import org.xreports.engine.source.BookmarkableElement;


public class Bookmark {

  private String              c_text;
  private int                 c_level;
  private BookmarkableElement c_elem;

  public Bookmark(String text, int level) {
    setText(text);
    setLevel(level);
  }

  public Bookmark(BookmarkableElement elem) throws ResolveException {
    setText(elem.getBookmarkText());
    setLevel(elem.getBookmarkLevel());
    c_elem = elem;
  }

  public String getText() {
    return c_text;
  }

  public void setText(String text) {
    c_text = text;
  }

  public int getLevel() {
    return c_level;
  }

  public void setLevel(int level) {
    c_level = level;
  }

  public BookmarkableElement getElement() {
    return c_elem;
  }
}
