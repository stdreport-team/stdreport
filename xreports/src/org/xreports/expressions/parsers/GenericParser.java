package org.xreports.expressions.parsers;

import java.util.LinkedList;
import java.util.List;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.LexerException;
import org.xreports.expressions.lexer.TokenStream;
import org.xreports.expressions.symbols.Function;
import org.xreports.expressions.symbols.StringLiteral;
import org.xreports.expressions.symbols.Symbol;

public abstract class GenericParser {
  private String      m_sourceText    = null;
  private TokenStream m_tokenStream   = null;

  private boolean     m_debugMode     = false;

  protected Symbol    m_TreeRoot      = null;
  protected Symbol    m_currentSymbol = null;

  private boolean     parsedOK        = false;
  private boolean     m_parsed        = false;

  private String      errorDesc       = null;

  public enum ParserType {
    MATH_EXPRESSION, BOOLEAN_EXPRESSION, ANY_EXPRESSION, TEXT_EXPRESSION
  }

  /**
   * Metodo factory che restituisce il parser appropriato per il tipo di grammatica richiesto.
   * Il parser è solo inizializzato, il testo non viene analizzato: lo dovrà fare il chiamante.
   * 
   * @param type tipo di grammatica richiesto
   * @param text testo sorgente da analizzara da parte del parser 
   * @return oggetto parser richiesto (torna sempre un'istanza fresca)
   */
  public static GenericParser getInstance(ParserType type, String text) {
    GenericParser parser = null;
    switch (type) {
      case MATH_EXPRESSION:
        parser = new MathParser(text);
        break;
      case BOOLEAN_EXPRESSION:
        parser = new BoolParser(text);
        break;
      case TEXT_EXPRESSION:
        parser = new TextParser(text);
        break;
      default:
        break;
    }
    return parser;
  }

  /**
   * Costruttore con il solo testo. L'analizzatore lessicale viene costruito
   * internamente.
   * 
   * @param text
   *          testo da analizzare
   */
  public GenericParser(String text) {
    m_sourceText = text;
  }

  /**
   * Costruttore con il solo testo. L'analizzatore lessicale viene costruito
   * internamente.
   * 
   * @param stream
   *          token stream già costruito
   */
  public GenericParser(TokenStream stream) {
    m_tokenStream = stream;
  }

  /**
   * Ritorna il testo sorgente da analizzare.
   * Il testo è assegnato nel costruttore.
   * 
   * @return Ritorna il testo dell'espressione da analizzare.
   */
  public String getText() {
    return m_sourceText;
  }

  /**
   * Ritorna true solo se ci sono stati errori durante l'analisi sintattica.
   * 
   * @return Ritorna true solo se ci sono stati errori durante l'analisi
   *         sintattica.
   * @see #getErrorDesc()
   */
  public boolean isParsedOK() {
    return this.parsedOK;
  }

  /**
   * Ritorna la descrizione dell'errore/i incontrato durante l'analisi
   * sintattica.
   * <p>
   * 
   * @return Ritorna la descrizione dell'errore/i incontrato durante l'analisi
   *         sintattica oppure stringa vuota se non ci sono stati errori.
   * @see #isParsedOK()
   */
  public String getErrorDesc() {
    if ( !this.parsedOK)
      return this.errorDesc;
    return "";
  }

  /**
   * Imposta l'errore di questo oggetto
   * 
   * @param testoErrore
   *          testo dell'errore
   */
  public void setErrorDesc(String testoErrore) {
    if (testoErrore == null || testoErrore.length() == 0)
      testoErrore = "?? errore non specificato ??";
    this.errorDesc = testoErrore;
    this.parsedOK = false;
  }

  /**
   * Ritorna allo stato iniziale, prepara l'oggetto per un nuovo processo di
   * parsing.
   * 
   * @throws LexerException
   */
  public void restart() throws LexerException {
    errorDesc = "";
    parsedOK = false;
    m_parsed = false;
    m_TreeRoot = null;
    m_currentSymbol = null;
    if (m_tokenStream != null) {
      m_tokenStream.restart();
    }
  }

  /**
   * Effettua l'analisi sintattica dello stream di token corrente.
   * 
   * @param expectedEnd
   *          token che ci si aspetta esserci dopo il riconoscimento della
   *          produzione; se null o {@link TokenType#Any}, non viene
   *          controllato, altrimenti una discrepanza genera exception
   * 
   * @return simbolo radice del parse tree
   * @throws LexerException
   *           in caso di errori del lexer
   * @throws ParseException
   *           in caso di errori del parser
   */
  public Symbol parse(TokenType expectedEnd) throws LexerException, ParseException {
    // ricomincio a parsare dall'inizio, così non faccio assunzioni sul
    // punto corrente di parsing
    if (m_tokenStream == null) {
      m_tokenStream = new TokenStream(m_sourceText);
      m_tokenStream.open();
    } else {
      m_tokenStream.restart();
    }

    m_TreeRoot = parseGrammar(expectedEnd);

    postParsing();

    pruneTree();

    return m_TreeRoot;
  }

  /**
   * Effettua l'analisi sintattica dello stream di token corrente. <br/>
   * Si aspetta che dopo il riconoscimento della produzione ci sia il token di
   * fine input; in caso contrario genera exception
   * 
   * @return simbolo radice del parse tree
   * @throws LexerException
   *           in caso di errori del lexer
   * @throws ParseException
   *           in caso di errori del parser
   */
  public Symbol parse() throws LexerException, ParseException {
    return parse(TokenType.EndOfText);
  }

  protected void pruneTree() {
    if (m_TreeRoot == null) {
      return;
    }
    for (Symbol child : m_TreeRoot.getChildren()) {
      pruneNode(child);
    }
  }

  private void pruneNode(Symbol node) {
    if (node.getChildNumber() == 0) {
      return;
    }
    if (node.getChildNumber() > 1) {
      for (Symbol child : node.getChildren()) {
        pruneNode(child);
      }
    } else {
      //node ha solo un figlio:
      //devo rimpiazzare il simbolo corrente, 'node',
      //con il suo unico figlio 'child'
      Symbol child = node.getChild(0);
      if (!node.isConcrete()) {
        node.getParent().replaceChild(node, child);
        node.destroy();
        pruneNode(child);        
      }
      else {
        pruneNode(child);  
      }
    }
  }

  /**
   * @return the treeRoot
   */
  public Symbol getTreeRoot() {
    return m_TreeRoot;
  }

  public boolean isDebugMode() {
    return m_debugMode;
  }

  public void setDebugMode(boolean mDebugMode) {
    m_debugMode = mDebugMode;
  }

  protected abstract Symbol parseGrammar(TokenType expectedEnd) throws ParseException, LexerException;

  protected void postParsing() {

  }

  /**
   * Dato un simbolo del parse tree, ritorna un elenco ordinato
   * di tutti i simboli foglia del parse tree.
   * @param startSymbol nodo di partenza
   * @return elenco foglie 
   */
  public List<Symbol> getLeaves(Symbol startSymbol) {
    List<Symbol> leaves = new LinkedList<Symbol>();
    findLeaves(startSymbol, leaves);   
    return leaves;
  }
  
  /**
   * Ricorsione per {@link #getLeaves(Symbol)}
   * @param startSymbol simbolo di partenza
   * @param leafList lista foglie
   */
  private void findLeaves(Symbol startSymbol, List<Symbol> leafList) {
    for (Symbol child : startSymbol.getChildren()) {
      if (child.getChildNumber()==0) {
        leafList.add(child);
      }
      else {
        findLeaves(child, leafList);
      }
    }
  }
  
  
  
  /**
   * Ritorna una stringa con la descrizione dettagliata dell'oggetto.
   * 
   * Viene chiamata <code>toString()</code> per ogni classe sottostante della
   * gerarchia.
   * <p>
   * Utile per le fasi di debugging.
   * 
   * @return Una stringa con la descrizione dell'oggetto.
   */
  public String toString() {
    StringBuffer out = new StringBuffer(300);
    out.append("text: '" + m_sourceText + "'\n");
    if (parsedOK)
      out.append("Parsing OK!\n");
    else
      out.append("Errori durante il parsing:" + this.errorDesc);
    return out.toString();
  }

  public TokenStream getTokenStream() {
    return m_tokenStream;
  }

  public boolean isParsed() {
    return m_parsed;
  }

  /**
   * Produzione
   * <p style="margin: 0 0 2 20;">
   * <tt> FunctionExpr   ::=  "("ArgList")" </tt>
   * </p>
   * 
   * @throws LexerException
   *           in caso di errore nella lettura dello stream di token
   * @throws ParseException
   *           in caso di errore in riconoscimento della produzione
   */
  protected Symbol function(TokenStream ts) throws LexerException, ParseException {
    Function fun = new Function(ts.getCurrentToken());
    ts.moveNext();
    //adesso dovrei essere sul token "("
    ts.moveNext();
    //ora o sono sul primo argomento o sulla ")" di chiusura per una nullary function
    argList(ts, fun);
    //ora sono sicuramente sulla ")" di chiusura degli argomenti
    if ( !ts.getCurrentToken().is(")")) {
      throw new ParseException("Mi aspettavo una ')'", getTokenStream().getCurrentToken());
    }
    ts.moveNext();
    return fun;
  }

  private void argList(TokenStream ts, Function f) throws LexerException, ParseException {
    if (ts.getCurrentToken().is(")")) {
      //sono arrivato a fine argomenti
      return;
    }
    argExpr(ts, f);
    argOpt(ts, f);
  }

  private void argExpr(TokenStream ts, Function f) throws LexerException, ParseException {
    Symbol e = nonTextExpr(ts, ",", ")");
    if (e == null) {
      try {
        if (ts.getCurrentToken().is(TokenType.String) && ts.getNextToken().isOneOf(",", ")")) {
          e = new StringLiteral(ts.getCurrentToken());
          ts.moveTo(ts.getCurrentPosition() + 1);
        }
      } catch (Exception ex) {
        //ignoro volutamente
      }
    }
    if (e == null) {
      throw new ParseException(ts.getCurrentToken());
    }
    f.addChild(e);
    argOpt(ts, f);
  }

  private void argOpt(TokenStream ts, Function f) throws LexerException, ParseException {
    if (ts.getCurrentToken().is(TokenType.Comma)) {
      ts.moveNext();
      argExpr(ts, f);
      argOpt(ts, f);
    } else {
      //se arrivo qui non ho la ",", ho finito gli argomenti
    }
  }

  /**
   * Produzione:
   * <p style="margin: 0 0 2 20;">
   * <tt> NonTextExpr ::= BoolExpr  |  MathExpr</tt>
   * </p>
   * Riconosce a partire dal token corrente una espressione aritmetica o
   * booleana. L'espressione deve obbligatoriamente avere come token successivo
   * uno dei token indicati in afterTokens
   * 
   * @param ts
   *          token stream corrente
   * @param afterTokens
   *          elenco opzionale di possbili token successivi all'espressione
   *          riconosciuta; passare null per non avere vincoli sui token
   *          successivi
   * @return espressione riconosciuta oppure null se non riconosciuta; se viene
   *         riconosciuta, il token stream viene avanzato al primo token
   *         successivo all'espressione riconosciuta.
   * 
   * @throws LexerException
   *           per errori sulo stream di tokens
   */
  protected Symbol nonTextExpr(TokenStream ts, String... afterTokens) throws LexerException {
    Symbol e = null;
    int pointerIncrement = 0;
    try {
      TokenStream subStream = ts.getSubStream();
      MathParser mp = new MathParser(subStream);
      e = mp.parse(TokenType.Any);
      if (afterTokens == null || subStream.getCurrentToken().isOneOf(afterTokens)) {
        pointerIncrement = subStream.getCurrentPosition();
      } else {
        //test necessario per evitare di riconoscere espressioni parziali: l'espressione
        //riconosciuta può avere dopo solo uno dei token indicati in afterTokens
        e = null;
      }
    } catch (Exception ex) {
      //ignoro volutamente: cerco altri tipi di espressione
    }
    if (e == null) {
      try {
        TokenStream subStream = ts.getSubStream();
        BoolParser bp = new BoolParser(subStream);
        e = bp.parse(TokenType.Any);
        if (afterTokens == null || subStream.getCurrentToken().isOneOf(afterTokens)) {
          pointerIncrement = subStream.getCurrentPosition();
        } else {
          //test necessario per evitare di riconoscere espressioni parziali: l'espressione
          //riconosciuta può avere dopo solo uno dei token indicati in afterTokens
          e = null;
        }
      } catch (Exception ex) {
        //ignoro volutamente: cerco altri tipi di espressione
      }
    }
    if (e != null) {
      ts.moveTo(ts.getCurrentPosition() + pointerIncrement);
    }
    return e;
  }

  /**
   * Produzione:
   * <p style="margin: 0 0 2 20;">
   * <tt> NonBoolExpr ::= StringLiteral  |  MathExpr</tt>
   * </p>
   * Riconosce a partire dal token corrente una espressione aritmetica o
   * booleana. L'espressione deve obbligatoriamente avere come token successivo
   * uno dei token indicati in afterTokens
   * 
   * @param ts
   *          token stream corrente
   * @param afterTokens
   *          elenco opzionale di possbili token successivi all'espressione
   *          riconosciuta; passare null per non avere vincoli sui token
   *          successivi
   * @return espressione riconosciuta oppure null se non riconosciuta; se viene
   *         riconosciuta, il token stream viene avanzato al primo token
   *         successivo all'espressione riconosciuta.
   * 
   * @throws LexerException
   *           per errori sulo stream di tokens
   */
  protected Symbol nonBoolExpr(TokenStream ts, String... afterTokens) throws LexerException {
    Symbol e = null;
    int pointerIncrement = 0;
    try {
      TokenStream subStream = ts.getSubStream();
      MathParser mp = new MathParser(subStream);
      e = mp.parse(TokenType.Any);
      if (afterTokens == null || subStream.getCurrentToken().isOneOf(afterTokens)) {
        pointerIncrement = subStream.getCurrentPosition();
      } else {
        //test necessario per evitare di riconoscere espressioni parziali: l'espressione
        //riconosciuta può avere dopo solo uno dei token indicati in afterTokens
        e = null;
      }
    } catch (Exception ex) {
      //ignoro volutamente: cerco altri tipi di espressione
    }
    if (e == null) {
      try {
        if (ts.getCurrentToken().is(TokenType.String)) {
          if (afterTokens == null || ts.getNextToken().isOneOf(afterTokens)) {
            pointerIncrement = 1;
            e = Symbol.getInstance(ts.getCurrentToken());
          }
        }
      } catch (Exception ex) {
        //ignoro volutamente: cerco altri tipi di espressione
      }
    }
    if (e != null) {
      ts.moveTo(ts.getCurrentPosition() + pointerIncrement);
    }
    return e;
  }

  /**
   * Riconosce a partire dal token corrente una espressione booleana.
   * L'espressione deve obbligatoriamente avere come token successivo uno dei
   * token indicati in afterTokens
   * 
   * @param ts
   *          token stream corrente
   * @param afterTokens
   *          elenco opzionale di possbili token successivi all'espressione
   *          riconosciuta; passare null per non avere vincoli sui token
   *          successivi
   * @return espressione riconosciuta oppure null se non riconosciuta; se viene
   *         riconosciuta, il token stream viene avanzato al primo token
   *         successivo all'espressione riconosciuta.
   * 
   * @throws LexerException
   *           per errori sulo stream di tokens
   */
  protected Symbol boolExpr(TokenStream ts, String... afterTokens) throws LexerException {
    Symbol e = null;
    int pointerIncrement = 0;
    try {
      TokenStream subStream = ts.getSubStream();
      BoolParser bp = new BoolParser(subStream);
      e = bp.parse(TokenType.Any);
      if (afterTokens == null || subStream.getCurrentToken().isOneOf(afterTokens)) {
        pointerIncrement = subStream.getCurrentPosition();
      } else {
        //test necessario per evitare di riconoscere espressioni parziali: l'espressione
        //riconosciuta può avere dopo solo uno dei token indicati in afterTokens
        e = null;
      }
    } catch (Exception ex) {
      //ignoro volutamente: cerco altri tipi di espressione
    }
    if (e != null) {
      ts.moveTo(ts.getCurrentPosition() + pointerIncrement);
    }
    return e;
  }

}
