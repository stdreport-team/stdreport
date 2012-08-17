package org.xreports.expressions.lexer;

import org.xreports.expressions.symbols.Operator;
import org.xreports.util.Text;

/**
 * Analizzatore lessicale usato dalle classi del package <tt>ciscoop.parsers</tt>
 * per analizzare sintatticamente le espressioni del linguaggio di stdReport.
 * <p>
 * Spezza il testo passatogli in "tokens" (vedi {@link Token}) distinti in varie
 * categorie (costanti, identificatori, operatori aritmetici, separatori,
 * ecc...), e riconosce anche le principali keywords SQL. <br>
 * L'analisi lessicale si effettua passandogli il testo da analizzare con la
 * {@link #setText(String)} e "mangiando" tokens con la {@link #nextToken()}. <br>
 * Tramite questa classe si costruisce un {@link TokenStream}, che è quello realmente 
 * usato dalle classi di parsing.
 * <p>
 * Copyright: <b>CIS Coop<sup><font size=-2>TM</font></sup>&nbsp;2010-2012</b>
 * 
 * @author CISCoop - Pier
 * @version 2.0
 */
public class Lexer {

	public enum TokenType {
		/**
		 * Tipo token assente. E' il tipo ritornato dal token corrente dopo la
		 * {@link #restart()}.
		 */
		NONE("Nessuno"),
		/**
		 * Tipo di token stringa.
		 * <p>
		 * Riconosce come stringhe sequenze di caratteri delimitate sia da singolo
		 * apice che da doppio apice. <br>
		 * L'escape del delimitatore è dato dal raddoppio dello stesso.
		 */
		String("Costante Stringa"),
		/** Costante numerica (ad esempio 123, 45.8, 12e3, 45E-8, 123.4E5, ...) */
		Number("Costante numerica"),
		/** Costante booleana: "true" o "false" (case insensitive) */
		BoolLiteral("Costante booleana"),
		/** campo dati, cioè <tt>gruppo#campo</tt> oppure <tt>#campo</tt> */
		Field("Campo Dati"),
		/** costante figurativa, ad esempio <tt>$pigreco</tt> */
		Constant("Costante Figurativa"),
		/** chiamata di funzione, cioè <tt>classRef.method</tt> */
		MethodCall("Chiamata di funzione"),
		/** parentesi tonda, sia aperta che chiusa */
		ParT("Parentesi tonda"),
		/** parentesi quadrata, sia aperta che chiusa */
		ParQ("Parentesi quadrata"),
		/**
		 * Operatore aritmetico. Gli operatori attualmente supportati sono, oltre
		 * alle 4 operazioni matematiche di base <tt>+</tt>, <tt>-</tt>, <tt>*</tt>
		 * e <tt>/</tt>, sono:
		 * <ul>
		 * <li> <tt>%</tt>: resto della divisione fra due numeri</li>
		 * <li> <tt>div</tt>: quoziente intero della divisione fra due numeri</li>
		 * <li> <tt>^</tt>: operazione di potenza (2^3 = 2*2*2)</li>
		 * </ul>
		 */
		MathOp("Operatore aritmetico"),
		/**
		 * Operatore relazionale. Gli operatori relazionali supportati sono:
		 * <ul>
		 * <li> <tt>&gt;</tt> e <tt>&gt;=</tt></li>
		 * <li> <tt>&lt;</tt> e <tt>&lt;=</tt></li>
		 * <li> <tt>=</tt></li>
		 * <li> <tt>!=</tt> (diverso da)</li>
		 * </ul>
		 */
		RelationalOp("Operatore Relazionale"),
		/**
		 * Operatore logico. Gli operatori supportati sono:
		 * <ul>
		 * <li><tt>&&</tt> = AND logico</li>
		 * <li><tt>||</tt> = OR logico</li>
		 * <li><tt>! </tt> = NOT logico</li>
		 * </ul>
		 */
		LogicalOp("Operatore Logico"),
		/** identificatore (ad esempio: codiss, tab_mesi,...) */
		Identifier("Identificatore"),
		/** è la virgola (',') */
		Comma("Virgola"),
		/** è il punto ('.') */
		Period("Punto"),
		/** parola chiave */
		Keyword("Parola chiave"),
		/** token virtuale di inizio testo */
		StartOfText("Inizio Input"),
		/** token virtuale di raggiunta fine testo */
		EndOfText("Fine Input"),
		/** token non riconosciuto */
		Unknown("Non riconosciuto"),
    /** tipo speciale, usato per l'analisi sintattica */
    Any("Qualsiasi");

		String	typeDesc; // descrizione

		TokenType(String desc) {
			typeDesc = desc;
		}

		/**
		 * Descrizione comprensibile della tipologia di token
		 * 
		 * @return
		 */
		public String getDesc() {
			return typeDesc;
		}

	}

	private static String[] s_relOperators;	
	
	// -------------- costanti che identificano la classe della keyword
	// -----------------
	// public enum LiteralType {
	// CONSTANT, FUNCTION_UNARY, FUNCTION_BINARY
	// }

	/** Indica che le costanti stringa sono delimitate dal singolo apice */
	public static final int					STRING_DELIM_QT				= 1;
	/** Indica che le costanti stringa sono delimitate dal doppio apice */
	public static final int					STRING_DELIM_DOUBLEQT	= 2;
	/** Indica che gli identificatori non hanno alcun delimitatore speciale */
	public static final int					ID_DELIM_NONE					= 0;
	/** Indica che gli identificatori sono delimitate dal doppio apice */
	public static final int					ID_DELIM_DOUBLEQT			= 1;
	/** Indica che gli identificatori sono delimitate dalla parentesi quadra */
	public static final int					ID_DELIM_SQBRK				= 2;

	/** literal booleano "true" */
	public static final String			BOOL_TRUE							= "true";
	/** literal booleano "false" */
	public static final String			BOOL_FALSE							= "false";

	/** testo da parsare */
	private String									c_sourceText					= null;
	/** lunghezza testo da parsare */
	private int											c_sourceLen;
	/**
	 * posizione nel testo da cui partire ad analizzare il prossimo token
	 * (1-based)
	 */
	private int											c_currentPos;																			//
	/** token corrente */
	private Token										c_currToken						= null;

	private int											mnStringMode					= STRING_DELIM_QT
																														+ STRING_DELIM_DOUBLEQT;
	private int											mnIDMode							= ID_DELIM_NONE;
	
	private boolean                 c_textMode;

	/**
	 * carattere che identifica l'uso di una costante; dopo tale carattere deve
	 * comparire il nome della costante
	 */
	public static final char				CONSTANT_MODIFIER			= '$';
	/** carattere che in un Field separa il nome del gruppo dal nome del campo */
	public static final char				FIELD_MODIFIER				= '#';
	/**
	 * carattere che in un Field segna l'inizio della eventuale qualified
	 * expression
	 */
	public static final char				FIELD_QUALIFIED_START	= '[';
	/**
	 * carattere che in un Field segna la fine della eventuale qualified
	 * expression
	 */
	public static final char				FIELD_QUALIFIED_END		= ']';

	/** il codice ascii del carattere '0' */
	private static final int				ASCII_ZERO						= 48;
	/** il codice ascii del carattere '9' */
	private static final int				ASCII_NOVE						= 57;
	
	static {
		s_relOperators = new String[] { "<", "<=", "=", ">", ">=", "!="};		
	}
	/**
	 * Costruttore di default.
	 * 
	 */
	public Lexer() {
		this(null);
	}

	/**
	 * Costruttore con assegnamento di testo.
	 * 
	 * @param sourceExpression
	 *          testo da analizzare
	 * @see #nextToken()
	 */
	public Lexer(String sourceExpression) {
		setText(sourceExpression);
	}

	/**
	 * Ritorna l'intero testo sorgente che l'analizzatore lessicale sta
	 * analizzando.
	 * 
	 * @see #setText(String)
	 */
	public String getText() {
		return c_sourceText;
	}

	/**
	 * Imposta il testo da analizzare. Se c'era già un'analisi in corso su un
	 * testo, viene resetttao tutto e l'analisi ripartirà dall'inizio.
	 * 
	 * @param text
	 *          testo che l'analizzatore lessicale dovrà analizzare
	 */
	public void setText(String text) {
		c_sourceText = text;
		restart();
		if (c_sourceText != null) {
			c_sourceText = c_sourceText.replace('\n', ' ');
			c_sourceText = c_sourceText.replace('\r', ' ');
			c_sourceText = c_sourceText.replace('\t', ' ');
			c_sourceText += ' '; // spazio finale per facilitare analisi lessicale
			c_sourceLen = c_sourceText.length();
		}
	}

	/**
	 * Riporta l'analisi allo stato iniziale, azzerando la posizione corrente e il
	 * token corrente.
	 */
	public void restart() {
		c_currentPos = 0;
		c_currToken = new Token("", TokenType.StartOfText);
		this.c_currToken.setTokenPos(0);
	}

	/**
	 * Riconosce il prossimo token dalla posizione corrente e ritorna il tipo di
	 * token riconosciuto. Se si è già arrivati a fine input, la nextToken ritorna
	 * indefinitamente un token speciale: {@link TokenType#EndOfText}.
	 * 
	 * Dopo la chiamata a questo metodo, il token riconosciuto è accessibile,
	 * oltre che come valore di ritorno del metodo stesso, tramite il metodo
	 * {@link #getCurrentToken()}. Il token che era corrente prima della chiamata
	 * a questo metodo, dopo la chiamata è accessibile tramite la
	 * {@link #getPreviousToken()}.
	 * 
	 * @return token riconosciuto
	 * @throws LexerException in caso di errore in riconoscimento token
	 */
	public Token nextToken() throws LexerException {
		if (c_currToken.getType() == TokenType.EndOfText)
			return c_currToken;
		if (isTextMode()) {
      analyzeTextModeToken();     		  
		}
		else {
	    analyzeToken();		  
		}
		return c_currToken;
	}

	/**
	 * Ritorna true sse ci sono ancora token da analizzare nel testo, cioè se non
	 * si è raggiunta ancora la fine del testo nell'analisi
	 * 
	 * @return
	 */
	public boolean hasMoreTokens() {
		return (c_currToken.getType() != TokenType.EndOfText);
	}

	/**
	 * Ritorna il token corrente, cioè l'ultimo parsato. Se il parsing non è
	 * iniziato, ritorna un token di tipo {@link TokenType#NONE}, se il parsing è
	 * finito, cioè è stata raggiunta la fine dell'input, ritorna un token di tipo
	 * {@link TokenType#EndOfText}.
	 * 
	 * @return token corrente
	 */
	public Token getCurrentToken() {
		return c_currToken;
	}

	/**
	 * Ritorna la posizione del primo carattere successivo al token corrente.
	 * <P>
	 * La posizione è sempre 0-based. Ad esempio, se la stringa analizzata finora
	 * è
	 * 
	 * <pre>
	 * SOTTO LA
	 * </pre>
	 * 
	 * <code>getCurrentPos()</code> ritorna 8, cioè lo spazio dopo la
	 * <code>'A'</code>.
	 * <p>
	 * 
	 * @return Ritorna la posizione del primo carattere successivo al token
	 *         corrente.
	 * @see #setCurrentPos(int)
	 */
	public int getCurrentPos() {
		return this.c_currentPos;
	}


  private void analyzeTextModeToken() throws LexerException {
    // azzero il token corrente
    c_currToken.setValue("");
    c_currToken.setTokenType(TokenType.Unknown);
    c_currentPos = 0;
    if (c_sourceLen == 0) {
      c_currToken.setTokenType(TokenType.EndOfText);
      c_currToken.setTokenPos(0);
      return;
    }
    
    // TODO proprio tutto da fare...
  }
	
	
	/**
	 * Metodo principale di riconoscimento dei token.
	 * Se riconosce il token, avanza il puntatore nel testo di input e assegna il token
	 * corrente in modo da essere letto tramite la {@link #getCurrentToken()}.
	 * Se non lo riconosce, emette una exception.
	 * 
	 * @throws LexerException  in caso di token non riconosciuto
	 */
	private void analyzeToken() throws LexerException {
		int i, n;
		int incrPos;

		int n_ch; // carattere corrente di parsing
		int n_nextch; // carattere successivo a n_ch

		// nPos viene posizionato al carattere successivo al token identificato
		try {

			// azzero il token corrente
			c_currToken.setValue("");
			c_currToken.setTokenType(TokenType.Unknown);

			if (c_sourceLen == 0) {
				c_currToken.setTokenType(TokenType.EndOfText);
				c_currToken.setTokenPos(0);
				return;
			}

			// salto caratteri di separazione iniziali
			for (n = c_currentPos; n <= c_sourceLen - 1; n++)
				if ((c_sourceText.charAt(n) != ' ') && (c_sourceText.charAt(n) != '\t')
						&& (c_sourceText.charAt(n) != '\n')
						&& (c_sourceText.charAt(n) != '\r'))
					break;

			// se non ho un carattere di continuazione linea devo lasciare
			// almeno un separatore prima
			if (n >= c_sourceLen) {
				c_currToken.setTokenType(TokenType.EndOfText);
				c_currToken.setTokenPos(c_currentPos);
				return;
			}

			// mi posiziono su primo carattere diverso da separatore
			incrPos = n - c_currentPos;

			c_currentPos = c_currentPos + incrPos;
			c_currToken.setTokenPos(c_currentPos);

			if (c_currentPos >= c_sourceLen) {
				c_currToken.setTokenType(TokenType.EndOfText);
				return;
			}

			// ------ analisi token ---------
			n_ch = Character.toUpperCase(c_sourceText.charAt(c_currentPos));
			if (c_currentPos < c_sourceLen)
				n_nextch = Character.toUpperCase(c_sourceText.charAt(c_currentPos + 1));
			else
				// non c'è carattere successivo
				n_nextch = 0;

			switch (n_ch) {
			case '"':
			case '\'':
				boolean bOK = false;
				// prima testo la modalità di riconoscimento stringhe se
				// compatibile
				if ((n_ch == '"')
						&& ((mnStringMode & STRING_DELIM_DOUBLEQT) == STRING_DELIM_DOUBLEQT))
					bOK = true;
				else if ((n_ch == '\'')
						&& ((mnStringMode & STRING_DELIM_QT) == STRING_DELIM_QT))
					bOK = true;
				// OK, sono su un inizio stringa
				if (bOK) {
					incrPos = analyzeStringLiteral(c_currToken);
					if (incrPos > 0) {
						c_currToken.setTokenType(TokenType.String);
						c_currToken.setTokenPos(c_currentPos);
            c_currToken.setEndPosition(c_currentPos + incrPos - 1);
            c_currToken.setTokenSourceVal(c_sourceText.substring(c_currentPos, c_currentPos + incrPos));
					}
					else {
						incrPos = 1;
						c_currToken.setValue("" + (char) n_ch);
						c_currToken.setTokenType(TokenType.Unknown);
					}
					c_currentPos = c_currentPos + incrPos;
					return;
				} else if ((n_ch == '"')
						&& ((mnIDMode & ID_DELIM_DOUBLEQT) == ID_DELIM_DOUBLEQT)) {
					// se non sono su un inizio stringa, il doppio apice
					// potrebbe essere un inizio
					// identificatore
					incrPos = analyzeIdentifier(c_currToken, '"', c_currentPos);
					if (incrPos > 0) {
						c_currentPos = c_currentPos + incrPos;
						return;
					}

					// token sconosciuto
					c_currentPos++;
					c_currToken.setValue("" + (char) n_ch);
					c_currToken.setTokenType(TokenType.Unknown);
					return;

				} else {
					// token sconosciuto
					c_currentPos++;
					c_currToken.setValue("" + (char) n_ch);
					c_currToken.setTokenType(TokenType.Unknown);
					return;
				}
			case '.':
        assignOneCharToken(n_ch, TokenType.Period);       
				return;
			case Operator.PLUS_C:
			case Operator.MINUS_C:
			case Operator.DIV_C:
			case Operator.MULT_C:
			case Operator.MODULO_C:
			case Operator.EXP_C:
        assignOneCharToken(n_ch, TokenType.MathOp);			  
				return;
			case '>':
			case '<':
			case '=':
			case '!':
			  analyzeRelop(n_ch, n_nextch);
				return;
			case '&':
			case '|':
				c_currToken.setTokenType(TokenType.LogicalOp);
				c_currToken.setValue("" + (char) n_ch);
				incrPos = 1;
				if (n_nextch == n_ch) {
					incrPos = 2;
					c_currToken.setValue("" + (char) n_ch + (char) n_ch);
					c_currToken.setTokenSourceVal(c_currToken.getValue());
					c_currToken.setTokenPos(c_currentPos);
          c_currToken.setEndPosition(c_currentPos + incrPos - 1);
				} else {
					c_currToken.setTokenType(TokenType.Unknown);
				}
				c_currentPos = c_currentPos + incrPos;
				return;
			case '(':
			case ')':
			  assignOneCharToken(n_ch, TokenType.ParT);
				return;
			case '[':
			case ']':
        assignOneCharToken(n_ch, TokenType.ParQ);
				return;
			case ',':
        assignOneCharToken(n_ch, TokenType.Comma);
				return;
			case CONSTANT_MODIFIER:
			case FIELD_MODIFIER:
			  analyzeFieldOrConstant(n_ch, n_nextch);
				return;
			default:
				if ((n_ch >= ASCII_ZERO) && (n_ch <= ASCII_NOVE))
					// qui può essere solo un literal numero
					incrPos = analyzeNumber(c_currToken);
				else {
					// non è una cifra: guardo se è un ID valido
					incrPos = analyzeIdentifier(c_currToken, c_currentPos);
					if (incrPos > 0) {
						String id1 = c_currToken.getValue();
						if (Text.isOneOf(id1, BOOL_FALSE, BOOL_TRUE)) {
							c_currToken.setTokenType(TokenType.BoolLiteral);					
							//il valore lo metto lowercase
              c_currToken.setValue(c_currToken.getValue().toLowerCase());              
						}
						if (c_sourceText.charAt(c_currentPos + incrPos) == FIELD_MODIFIER) {
							// guardo se è un campo composto, cioè del tipo 'gruppo#campo'
						  Token tmpToken = new Token();
							int incrPos2 = analyzeIdentifier(tmpToken, c_currentPos + incrPos + 1);
							if (incrPos2 > 0) {
								c_currToken.setValue(id1 + FIELD_MODIFIER
										+ tmpToken.getValue());
								c_currToken.setTokenType(TokenType.Field);
                c_currToken.setEndPosition(tmpToken.getEndPosition());
                c_currentPos = tmpToken.getEndPosition() + 1;
								return;
							}
						} else if (c_sourceText.charAt(c_currentPos + incrPos) == '.') {
							// guardo se è un method call, cioè del tipo 'classRef.methodRef'
						  Token tmpToken = new Token();
							int incrPos2 = analyzeIdentifier(tmpToken, c_currentPos + incrPos + 1);
							if (incrPos2 > 0) {
								c_currToken.setValue(id1 + "." + tmpToken.getValue());                
								c_currToken.setTokenType(TokenType.MethodCall);
								c_currToken.setEndPosition(tmpToken.getEndPosition());
                c_currToken.setTokenSourceVal(c_currToken.getValue());                
								c_currentPos = tmpToken.getEndPosition() + 1;
								return;
							}
						}
					}
				}

				if (incrPos > 0) {
					c_currentPos = c_currentPos + incrPos;
					c_currToken.setEndPosition(c_currentPos - 1);
					c_currToken.setTokenSourceVal(c_sourceText.substring(c_currToken.getStartPosition(), c_currToken.getEndPosition() + 1));
				} else {
					// token non riconosciuto: prendo tutti i caratteri
					// fino al primo separatore e ritorno token BOH
					for (i = c_currentPos; i <= c_sourceLen; i++)
						if (isSeparator(c_sourceText.charAt(i)))
							break;
					if (i == c_currentPos)
						i = c_currentPos + 1;
					c_currToken.setTokenType(TokenType.Unknown);
					// salto fino al prossimo separatore: il token BOH non lo
					// comprende
					c_currToken.setValue(c_sourceText.substring(c_currentPos, i));
					c_currentPos = i;
				}
				return;
			}// end switch

		}// end try
		catch(LexerException lex) {
		  throw lex;
		}
		catch (Exception ex) {
      throw new LexerException("Unexpected run-time error in analyzeToken", ex);
		}
	}// end zGetToken


  private void assignOneCharToken(int currentChar, TokenType type) throws LexerException {
    c_currToken.setValue(String.valueOf((char) currentChar));
    c_currToken.setTokenSourceVal(c_currToken.getValue());
    c_currToken.setTokenPos(c_currentPos);
    c_currToken.setEndPosition(c_currentPos);
    c_currentPos = c_currentPos + 1;
    c_currToken.setTokenType(type);    
  }
	
	/**
   * Analizza il prossimo token come operatore relazione o not logico.
   * 
   * Imposta correttamente il token corrente (c_currToken) e incremente di conseguenza c_currentPos.
   * 
   * @param currentChar carattere corrente
   * @param nextChar carattere successivo
	 * @throws LexerException
	 */
	private void analyzeRelop(int currentChar, int nextChar) throws LexerException {
    c_currToken.setTokenType(TokenType.RelationalOp);
    c_currToken.setValue(String.valueOf((char) currentChar));
    int incrPos = 1;
    if (currentChar == '>') {
      if (nextChar == '=') {
        incrPos = 2;
        c_currToken.setValue(">=");
      }
    } else if (currentChar == '<') {
      if (nextChar == '=') {
        incrPos = 2;
        c_currToken.setValue("<=");
      } else if (nextChar == '>') {
        incrPos = 2;
        c_currToken.setValue("<>");
      }
    } else if (currentChar == '!') {
      if (nextChar == '=') {
        incrPos = 2;
        c_currToken.setValue("!=");
      } else {
        // è '!' = not logico
        c_currToken.setTokenType(TokenType.LogicalOp);
      }
    }
    c_currToken.setTokenSourceVal(c_currToken.getValue());
    c_currToken.setTokenPos(c_currentPos);
    c_currToken.setEndPosition(c_currentPos + incrPos - 1);
    c_currentPos = c_currentPos + incrPos;	  
	}
	
	/**
	 * Analizza il prossimo token come Field o Constant. Arrivo qui se il carattere
	 * corrente è il carattere di inizio di un Field/Constant.
	 * 
	 * Imposta correttamente il token corrente (c_currToken) e incremente di conseguenza c_currentPos.
	 * 
	 * @param currentChar carattere corrente
	 * @param nextChar carattere successivo
	 * @throws LexerException
	 */
	private void analyzeFieldOrConstant(int currentChar, int nextChar) throws LexerException {
	  int incrPos;
    c_currToken.setValue("");
    int startPos = c_currentPos;
    c_currentPos = c_currentPos + 1;
    boolean bWithPar = nextChar == '{'; 
    if (bWithPar) {
      incrPos = analyzeIdentifier(c_currToken, '{', c_currentPos);                  
    }
    else {
      incrPos = analyzeIdentifier(c_currToken, c_currentPos);         
    }
    if (incrPos > 0) {
      c_currentPos = c_currentPos + incrPos;
      c_currToken.setEndPosition(c_currentPos - 1);
      c_currToken.setTokenPos(startPos);
      c_currToken.setTokenSourceVal(c_sourceText.substring(c_currToken.getStartPosition(), c_currToken.getEndPosition()+1));
      TokenType tt = currentChar == CONSTANT_MODIFIER ? TokenType.Constant : TokenType.Field;
      c_currToken.setTokenType(tt);
      if (bWithPar) {
        //nel valore del token non metto le parentesi graffe
        c_currToken.setValue(c_sourceText.substring(c_currToken.getStartPosition()+2, c_currToken.getEndPosition()));
      }
    } else {
      // token non riconosciuto: prendo tutti i caratteri
      // fino al primo separatore e ritorno token BOH
      int i = 0;
      for (i = c_currentPos; i <= c_sourceLen; i++)
        if (isSeparator(c_sourceText.charAt(i)))
          break;
      if (i == c_currentPos)
        i = c_currentPos + 1;
      c_currToken.setTokenType(TokenType.Unknown);
      // salto fino al prossimo separatore: il token BOH non lo
      // comprende
      c_currToken.setValue(c_sourceText.substring(c_currentPos, i));
      c_currentPos = i;
    }
    return;	  
	}
	
	/**
	 * Analizza dalla posizione corrente una costante literal stringa. Assumo che
	 * m_CurrPos punti al double quote di inizio stringa
	 * 
	 * @param token
	 *          Riceve la stringa analizzata (compresi i quote/double-quote
	 *          iniziale e finale)
	 * @return qta da sommare alla posizione corrente in modo che punti al
	 *         carattere successiva alla stringa identificata. E' 0 se non
	 *         riconosce una stringa valida.
	 * @throws LexerException in caso di formato stringa errato
	 */
	private int analyzeStringLiteral(Token token) throws LexerException {
		int n = 0;
		int nStato;
		int nch;
		int nStartString;
		int incr; // valore ritorno

		try {
			incr = 0;
			nStato = 0; // stato iniziale

			token.setValue(""); // azzero subito il token
			token.setTokenType(TokenType.String);

			// il primo carattere è il carattere di inizio stringa
			nStartString = c_sourceText.charAt(c_currentPos);
			// ------ analisi token ---------
			for (n = c_currentPos; n < c_sourceLen; n++) {
				nch = c_sourceText.charAt(n);
				switch (nStato) {
				case 0: // stato iniziale: quote/dblquote di apertura
					if ((nch != '"') && (nch != '\''))
						return incr;
					nStato = 1;
					break;
				case 1: // corpo stringa
					if (nch == '\\') {
						nStato = 2; // '\' può essere un escape di quote/dblquote
					} else if (nch == nStartString) {
						incr = n - c_currentPos + 1;
						token.setValue(c_sourceText.substring(c_currentPos, n + 1));
						return incr;
					}
					break;
				case 2: // ho incontrato '\':
					// può essere un escape del carattere delimitatore o di se stesso
					nStato = 1;
					break;
				}// end switch
			}// end-for

			if (nStato == 2) {
				// literal a fine stringa di parse
				incr = n - c_currentPos + 1;
				token.setValue(c_sourceText.substring(c_currentPos, n));
			}

			return incr;
		}// end-try
		catch (LexerException lex) {
		  throw lex;
		}
		catch (Exception ex) {
		  throw new LexerException("Unexpected run-time error in analyzeStringLiteral", ex);
		}

	}// end zAnalString

	/**
	 * Controlla se un carattere è un separatore per un ID o un numero.
	 * 
	 * @param nch
	 *          char da controllare
	 * @return true se <em>nch</em> è un separatore, false altrimenti
	 */
	private boolean isSeparator(char nch) {
		// sono considerati separatori tutti i caratteri non stampabili
		// quindi anche il tab e Cr/Lf/FormFeed
		// spazio e parentesi tonde sono separatori
		if ((nch <= ' ') || (nch == '(') || (nch == ')'))
			return true;
		return false;
	}

	/**
	 * Wrapper di {@link #analyzeIdentifier(Token, char, int)} con il secondo parametro =
	 * '\u0000'.
	 * 
	 * @param token
	 *          Riceve il valore del token riconosciuto
	 * @param fromPos
	 *          posizione del carattere di partenza del token nella stringa di
	 *          input
	 * @return qta da sommare a CurrentPos in modo che punti al carattere
	 *         successivo alla stringa identificata. E' 0 se non riconosce un ID
	 *         valido.
	 */
	private int analyzeIdentifier(Token token, int fromPos) {
		return analyzeIdentifier(token, '\u0000', fromPos);
	}

	/**
	 * Analizza dalla posizione corrente un ID.
	 * 
	 * @param token
	 *          Riceve il valore del token riconosciuto
	 * @param delim
	 *          carattere delimitatore iniziale dell'ID; se ='\u0000', è ignorato
	 * @param fromPos
	 *          posizione del carattere di partenza del token nella stringa di
	 *          input
	 * @return qta da sommare a CurrentPos in modo che punti al carattere
	 *         successivo alla stringa identificata. E' 0 se non riconosce un ID
	 *         valido.
	 */
	private int analyzeIdentifier(Token token, char delim, int fromPos) {

		int n = 0;
		int nTokEnd; // posizione finale del token corrente
		int nch;
		int incr; // valore ritorno
		@SuppressWarnings("unused")
		boolean bDelimDblQuote = false;
		@SuppressWarnings("unused")
		boolean bDelimSqrBracket = false;
		char cEndID = (char) 0;
		int startPos = 0; // posizione iniziale da cui parte l'analisi, con i
		// delimitatori parte un carattere dopo

		try {
			incr = 0;
			nTokEnd = 0;
			token.setValue(""); // azzero subito il token

			if (delim == '"') {
				bDelimDblQuote = true;
				cEndID = delim;
				startPos = 1;
			} else if (delim == '{') {
				bDelimSqrBracket = true;
				cEndID = '}';
				startPos = 1;
			}

			// ------ analisi token ---------
			for (n = fromPos + startPos; n < c_sourceLen; n++) {
				nch = Character.toUpperCase(c_sourceText.charAt(n));
				if (cEndID == (char) 0) {
					// se non ho un delimitatore, accetto cifre, lettere e underscore
					// e mi fermo al primo carattere diverso, che segnala la fine
					// dell'identificatore
					if (Character.isLetter(nch) || Character.isDigit(nch) || nch == '_') {
						// carattere ammesso per identificatore --> continuo
						continue;
					}

					nTokEnd = n;
					break;
				}
				// se ho un delimitatore, mi fermo solo quando lo trovo
				// (in questo caso l'identificatore può avere qualsiasi
				// carattere, anche spazio):
				// il delimitatore stesso fa parte dell'ID, quindi avanzo di un
				// carattere
				if (nch == cEndID) {
					// OK, trovato delimitatore finale
					nTokEnd = n + 1;
					break;
				}
			}
			// nTokEnd è l'indice del primo carattere successivo al token
			if (nTokEnd > 0) {
				token.setValue(c_sourceText.substring(fromPos, nTokEnd));
				incr = nTokEnd - fromPos;
        token.setEndPosition(nTokEnd - 1);
			} else {
				incr = n - fromPos;
				token.setValue(c_sourceText.substring(fromPos, fromPos + 1));
				token.setEndPosition(fromPos);
			}

			// if (isKeyword(token.getValue()))
			// token.setTokenType(TokenType.Keyword);
			// else
			token.setTokenType(TokenType.Identifier);
			token.setTokenPos(fromPos);
      token.setTokenSourceVal(c_sourceText.substring(token.getStartPosition(), token.getEndPosition() + 1));
			return incr;
		}// end-try
		catch (Exception ex) {
			String msg = "Procedura analyzeIdentifier \n Errore Run-Time " + ex.getMessage();
			System.err.println(msg);
			ex.printStackTrace(System.err);
			return n - fromPos;
		}
	}

	/**
	 * Analizza dalla posizione corrente un literal numerico. <br/>
	 * Assume che la posizione corrente (mlCurrentPos) punti al primo carattere
	 * del literal.
	 * 
	 * @param token
	 *          Riceve il token riconosciuto
	 * @return qta da sommare alla posizione corrente in modo che punti al
	 *         carattere successiva alla stringa identificata; è 0 se non
	 *         riconosce una litaral numerico valido.
	 */
	private int analyzeNumber(Token token) {
		int n = 0;
		int nStato;
		int nch; // codice ascii di ch
		int incr; // valore ritorno

		try {

			incr = 0;
			token.setValue(""); // azzero subito il token

			token.setTokenType(TokenType.Number);
			token.setTokenPos(c_currentPos);

			nStato = 0; // stato iniziale
			// ------ analisi token ---------
			for (n = c_currentPos; n <= c_sourceLen - 1; n++) {
				nch = this.c_sourceText.charAt(n);
				switch (nStato) {
				case 0: // stato iniziale: primo carattere ID
					if (!Character.isDigit(nch))
						nStato = 10;
					else
						nStato = 1;
					break;
				case 1: // avute finora solo cifre
					if (nch == '.')
						nStato = 2;
					else if (Character.isDigit(nch))
						nStato = 1;
					else if (Character.toUpperCase((char) nch) == 'E')
						nStato = 3;
					else {
						incr = n - c_currentPos;
						token.setValue(c_sourceText.substring(c_currentPos, n));
						return incr;
					}
					break;
				case 2: // incontrato il primo punto dopo cifre
					if (Character.isDigit(nch))
						nStato = 2;
					else if (Character.toUpperCase((char) nch) == 'E')
						nStato = 3;
					else {
						incr = n - c_currentPos;
						token.setValue(c_sourceText.substring(c_currentPos, n));
						return incr;
					}
					break;
				case 3: // incontrata la 'E' che identifica l'esponente
					if ((nch == '+') || (nch == '-'))
						nStato = 4;
					else if (Character.isDigit(nch))
						nStato = 5;
					else
						nStato = 10;
					break;
				case 4: // incontrata la 'E' che identifica l'esponente con il
					// segno dopo
					if (Character.isDigit(nch))
						nStato = 5;
					else
						nStato = 10;
					break;
				case 5: // cifre dell'esponente
					if (Character.isDigit(nch))
						nStato = 5;
					else {
						incr = n - c_currentPos;
						token.setValue(c_sourceText.substring(c_currentPos, n));
						return incr;
					}
					break;
				case 10: // errore!
					incr = n - c_currentPos + 1;
					token.setValue(c_sourceText.substring(c_currentPos, n));
					return incr;
				}// end switch
			}// end-for

			return incr;
		}// end-try
		catch (Exception ex) {
      ex.printStackTrace();
			String msg = "analyzeNumber, unexpected error " + ex.getMessage();
			System.err.println(msg);
			return n - c_currentPos;
		}
	}

	/**
	 * Restituisce la modalità di riconoscimento delle stringhe.
	 * 
	 * @return una combinazione in or delle costanti {@link #STRING_DELIM_QT} e {@link #STRING_DELIM_DOUBLEQT}
	 * @see #setStringMode(int)
	 */
	public int getStringMode() {
		return mnStringMode;
	}

	/**
	 * Imposta la modalità di riconoscimento stringhe.
	 * 
	 * @param mode
	 *          una combinazione delle seguenti costanti
	 *          <ul>
	 *          <li>{@link #STRING_DELIM_QT} : delimitatore = apice singolo </li>
	 *          <li>{@link #STRING_DELIM_DOUBLEQT} : delimitatore = apice doppio
	 *          </li>
	 *          </ul>
	 * @throws ParseException
	 *           se parametro passato non valido
	 * @see #getStringMode()
	 */
	public void setStringMode(int mode) throws LexerException {
		if (((mode & STRING_DELIM_DOUBLEQT) == 0)
				&& ((mode & STRING_DELIM_QT) == 0))
			throw new LexerException("setStringMode: impostare una valida modalità.");
		mnStringMode = mode;
	}

	/**
	 * Restituisce la modalità di riconoscimento delle stringhe.
	 * 
	 * @return vedi sopra
	 * @see #setIDMode(int)
	 */
	public int getIDMode() {
		return mnIDMode;
	}

	/**
	 * Imposta la modalità di riconoscimento identificatori.
	 * <p>
	 * Questo metodo serve in quanto diverse versioni di SQL hanno l'impostazione
	 * dei delimitatori degli identificatori diversa.
	 * <p>
	 * 
	 * @param mode
	 *          una combinazione delle seguenti costanti
	 *          <ul>
	 *          <li>{@link #ID_DELIM_NONE} : nessun delimitatore</li> <li>
	 *          {@link #ID_DELIM_DOUBLEQT} : delimitatore = apice doppio</li> <li>
	 *          {@link #ID_DELIM_SQBRK} : delimitatore = parentesi quadra </li>
	 *          </ul>
	 * @throws ParseException
	 *           se parametro passato non valido
	 * @see #getIDMode()
	 */
	public void setIDMode(int mode) throws LexerException {
		if (((mode & STRING_DELIM_DOUBLEQT) == 0)
				&& ((mode & STRING_DELIM_QT) == 0))
			throw new LexerException("setIDMode: impostare una valida modalità.");
		mnIDMode = mode;
	}

	
	public static String[] getRelOperators() {
		return s_relOperators;
	}

  public boolean isTextMode() {
    return c_textMode;
  }

  public void setTextMode(boolean textMode) {
    c_textMode = textMode;
  }
}
