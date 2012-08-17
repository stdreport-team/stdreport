/**
 * 
 */
package org.xreports.expressions.symbols;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xreports.expressions.lexer.Lexer.TokenType;
import org.xreports.expressions.lexer.Token;
import org.xreports.expressions.parsers.ParseException;
import org.xreports.engine.ResolveException;
import org.xreports.util.DtUtil;
import org.xreports.util.Text;

/**
 * Definizione astratta di 'funzione' che verrà poi concretizzata nelle funzioni
 * con numero specifico di argomenti.
 * 
 * @author pier
 * 
 */
public class Function extends Symbol {

	// ***************** NULLARY FUNCTIONS ******************
	public static final String	FUN_NOW						= "now";
	public static final String	FUN_TODAY					= "today";
	public static final String	FUN_TIME					= "time";

	// ***************** UNARY FUNCTIONS ******************
	public static final String	FUN_MONTHNAME			= "monthname";
	public static final String	FUN_MONTH					= "month";
	public static final String	FUN_YEAR					= "year";
	public static final String	FUN_DAY						= "day";
	public static final String	FUN_DAYSINMONTH		= "daysInMonth";
	public static final String	FUN_ABS						= "abs";
	public static final String	FUN_UPPER					= "upper";
	public static final String	FUN_LOWER					= "lower";
	public static final String	FUN_TRIM					= "trim";
	public static final String	FUN_RTRIM					= "rtrim";
	public static final String	FUN_LTRIM					= "ltrim";
	public static final String	FUN_CAPITALIZE		= "capitalize";
	public static final String	FUN_ISNULL				= "isnull";
	public static final String	FUN_ISNULLORZERO	= "isNullOrZero";
	public static final String	FUN_LEN						= "length";
	public static final String	FUN_TOHOUR				= "toHour";
	public static final String	FUN_ISHOLIDAY			= "isHoliday";
	public static final String	FUN_STRIPHOUR			= "stripHour";
	public static final String	FUN_STRIPDATE			= "stripDate";
	public static final String	FUN_SQRT					= "sqrt";

	// ***************** BINARY FUNCTIONS ******************
	public static final String	FUN_MIN						= "min";
	public static final String	FUN_MAX						= "max";
	public static final String	FUN_ROUND					= "round";
	public static final String	FUN_TRUNC					= "trunc";
	public static final String	FUN_CUT						= "cut";
	public static final String	FUN_PADLEFT				= "padLeft";
	public static final String	FUN_PADRIGHT			= "padRight";
	public static final String	FUN_FORMAT				= "format";
	public static final String  FUN_TODATE        = "toDate";

	// ***************** TERTIARY FUNCTIONS ******************
	public static final String	FUN_CASE					= "case";
  public static final String  FUN_SUBSTR        = "substr";

	// ***************** NARY FUNCTIONS ******************
	public static final String	FUN_CONCAT				= "concat";

	// ***************** MIXED FUNCTIONS ******************
	public static final String	FUN_WEEKDAY				= "weekday";
	public static final String	FUN_TOINT					= "toInt";
	public static final String	FUN_TODOUBLE			= "toDouble";

	private String							m_functionName;

	/**
	 * Cardinalità argomenti di funzione.
	 * 
	 * @author pier
	 */
	private enum FunctionCard {
		/** funzione che non accetta argomenti */
		NULLARY,
		/** funzione che non accetta 1 argomento */
		UNARY,
		/** funzione che non accetta 2 argomenti */
		BINARY,
		/** funzione che non accetta 3 argomenti */
		TERTIARY,
		/** funzione che accetta qualsiasi numero di argomenti */
		NARY;
		public static FunctionCard valueOf(int nArgs) {
			switch (nArgs) {
			case 0:
				return NULLARY;
			case 1:
				return UNARY;
			case 2:
				return BINARY;
			case 3:
				return TERTIARY;
			default:
				return NARY;
			}
		}
	}

	/**
	 * Mappa statica di tutte le funzioni gestite. Sono suddivise per quantità di
	 * argomenti accettati. Le funzioni che hanno un numero illimitato di
	 * parametri, sono elencate solo in {@link FunctionCard#NARY}.
	 */
	private static Map<FunctionCard, Set<String>>	m_functions;

	static {
		// inizializzazione mappa funzioni in base alla cardinalità

		m_functions = new HashMap<FunctionCard, Set<String>>();

		Set<String> nullary = new HashSet<String>();
		nullary.add(FUN_NOW);
		nullary.add(FUN_TODAY);
		nullary.add(FUN_TIME);
		m_functions.put(FunctionCard.NULLARY, nullary);

		Set<String> unary = new HashSet<String>();
		unary.add(FUN_MONTHNAME);
		unary.add(FUN_MONTH);
		unary.add(FUN_YEAR);
		unary.add(FUN_DAY);
		unary.add(FUN_DAYSINMONTH);
		unary.add(FUN_WEEKDAY); // NB: weekday c'è sia con 1 argomento che con 2
		unary.add(FUN_ABS);
		unary.add(FUN_UPPER);
		unary.add(FUN_LOWER);
		unary.add(FUN_TRIM);
		unary.add(FUN_RTRIM);
		unary.add(FUN_LTRIM);
		unary.add(FUN_CAPITALIZE);
		unary.add(FUN_ISNULL);
		unary.add(FUN_ISNULLORZERO);
		unary.add(FUN_LEN);
		unary.add(FUN_TOHOUR);
		unary.add(FUN_ISHOLIDAY);
		unary.add(FUN_STRIPHOUR);
		unary.add(FUN_STRIPDATE);
		unary.add(FUN_SQRT);
		unary.add(FUN_TOINT); // NB: toint c'è sia con 1 argomento che con 2
		unary.add(FUN_TODOUBLE); // NB: todouble c'è sia con 1 argomento che con 2
		m_functions.put(FunctionCard.UNARY, unary);

		Set<String> binary = new HashSet<String>();
		binary.add(FUN_WEEKDAY); // NB: weekday c'è sia con 1 argomento che con 2
		binary.add(FUN_MIN);
		binary.add(FUN_MAX);
		binary.add(FUN_ROUND);
		binary.add(FUN_TRUNC);
		binary.add(FUN_CUT);
		binary.add(FUN_PADLEFT);
		binary.add(FUN_PADRIGHT);
		binary.add(FUN_FORMAT);
    binary.add(FUN_TODATE);
		binary.add(FUN_TOINT); // NB: toint c'è sia con 1 argomento che con 2
		binary.add(FUN_TODOUBLE); // NB: todouble c'è sia con 1 argomento che con 2
		m_functions.put(FunctionCard.BINARY, binary);

		Set<String> tertiary = new HashSet<String>();
		tertiary.add(FUN_CASE);
		tertiary.add(FUN_SUBSTR);
		m_functions.put(FunctionCard.TERTIARY, tertiary);

		Set<String> nary = new HashSet<String>();
		nary.add(FUN_CONCAT);
		m_functions.put(FunctionCard.NARY, nary);
	}

	/**
	 * Costruttore standard con il token sorgente
	 * 
	 * @param t token che rappresenta questo simbolo
	 * 
	 * @throws ParseException in caso il token passato sia del tipo imprevisto o sia errato
	 */
	public Function(Token t) throws ParseException {
    super(t, TokenType.Identifier);	  
		m_functionName = t.getValue();
	}

	/**
	 * Valutazione della funzione.<br/>
	 * Qui viene valutata la funzione corrispondente a questo simbolo: se è
	 * sufficiente una valutazione interna (ad esempio funzione
	 * {@link #FUN_WEEKDAY}), viene valutato dentro questa classe, altrimentati è
	 * demandata la valutazione al Evaluator passato come parametro. <br/>
	 * La valutazione è ricorsiva sugli argomenti della funzione.
	 * 
	 * @param evaluator
	 *          valutatore esterno a cui demandare la valutazione di questa
	 *          funzione nel caso non sia sufficiente la valutazione interna.
	 */
	public Object evaluate(Evaluator evaluator) throws EvaluateException {
		Object result = null;
		// prima controllo se questa funzione è gestita qui
		if (checkFunction(getFunctionName())) {
			// prima controllo quantità argomenti
			FunctionCard cardinality = checkArguments(getFunctionName());
			// adesso guardo se riesco a valutare internamente questa funzione
			result = internalEvaluate(cardinality, evaluator);
			if (result != null) {
				setPartialValue(result);
				return result;
			}
		}

		// se la funzione non riesco a valutarla qui, uso l'evaluator esterno
		if (evaluator != null) {
      try {
        result = evaluator.evaluate(this);
      } catch (ResolveException e) {
        throw new EvaluateException(e);
      }
			setPartialValue(result);
			return result;
		}
		// valutazione impossibile-->exception
		throw new EvaluateException("Funzione %s: non riesco a valutarla.",
				getFunctionName());
	}

	private Object internalEvaluate(FunctionCard cardinality, Evaluator evaluator)
			throws EvaluateException {
		switch (cardinality) {
		case NULLARY:
			return internalEvaluateNullary(getFunctionName());
		case UNARY:
			return internalEvaluateUnary(getFunctionName(), evaluator);
		case BINARY:
			return internalEvaluateBinary(getFunctionName(), evaluator);
		case TERTIARY:
			return internalEvaluateTertiary(getFunctionName(), evaluator);
		default:
			return internalEvaluateNary(getFunctionName(), evaluator);
		}
	}

	/**
	 * Metodo principale per la valutazione delle funzioni nullarie (senza
	 * argomenti).
	 * 
	 * @param functName
	 *          nome funzione
	 * @return risultato calcolato oppure null se la valutazione non è possibile
	 *         (richiesto intervento del valutatore esterno)
	 * @throws EvaluateException
	 *           in caso di errori di valutazione
	 */
	private Object internalEvaluateNullary(String functName)
			throws EvaluateException {
		Date adesso = new Date();
		if (functName.equals(FUN_NOW)) {
			return adesso;
		} else if (functName.equals(FUN_TODAY)) {
			return DtUtil.azzeraHHMMSS(adesso);
		} else if (functName.equals(FUN_TIME)) {
			return DtUtil.azzeraData(adesso);
		}

		// tornando null indica che qui non si può valutare la funzione passata
		// la deve valutare qualcun altro
		return null;
	}

	/**
	 * Metodo principale per la valutazione delle funzioni unarie.
	 * 
	 * @param functName
	 *          nome funzione
	 * @param evaluator
	 *          valutatore esterno per la valutazione degli argomenti
	 * @return risultato calcolato oppure null se la valutazione non è possibile
	 *         (richiesto intervento del valutatore esterno)
	 * @throws EvaluateException
	 *           in caso di errori di valutazione
	 */
	private Object internalEvaluateUnary(String functName, Evaluator evaluator)
			throws EvaluateException {
		// prima di valutare la funzione prendo l'argomento e lo valuto
		Symbol arg = getChild(0);

		// IMPORTANTE! non valutare l'argomento prima di sapere se la funzioone è
		// gestita qui. Infatti a seconda della funzione, l'argomento deve essere
		// valutato in modi diversi.
		if (functName.equals(FUN_ABS)) {
			Object value = arg.evaluate(evaluator);
			return abs(value);
		} else if (functName.equals(FUN_UPPER) || functName.equals(FUN_LOWER)
				|| functName.equals(FUN_LTRIM) || functName.equals(FUN_RTRIM)
				|| functName.equals(FUN_CAPITALIZE) || functName.equals(FUN_TRIM)) {
			Object value = arg.evaluate(evaluator);
			return execStringFunction(functName, value);
		} else if (functName.equals(FUN_LEN)) {
			Object value = arg.evaluate(evaluator);
			if (value == null) {
				return Integer.valueOf(0);
			} else {
				if (value instanceof Boolean) {
					if (((Boolean) value).booleanValue()) {
						// true = 1
						return Integer.valueOf(1);
					} else {
						// false = 0
						return Integer.valueOf(0);
					}
				}
				return Integer.valueOf(value.toString().length());
			}
		} else if (functName.equals(FUN_MONTHNAME)) {
			Object value = arg.evaluate(evaluator);
			return execMonthName(value);
		} else if (functName.equals(FUN_MONTH)) {
			Object value = arg.evaluate(evaluator);
			return execMonth(value);
		} else if (functName.equals(FUN_YEAR)) {
			Object value = arg.evaluate(evaluator);
			return execYear(value);
		} else if (functName.equals(FUN_DAY)) {
			Object value = arg.evaluate(evaluator);
			return execDay(value);
		} else if (functName.equals(FUN_WEEKDAY)) {
			Object value = arg.evaluate(evaluator);
			return execWeekDay(value);
		} else if (functName.equals(FUN_DAYSINMONTH)) {
			Object value = arg.evaluate(evaluator);
			return execDaysInMonth(value);
		} else if (functName.equals(FUN_ISNULL)) {
			Object value = arg.evaluate(evaluator);
			return execIsNull(value);
		} else if (functName.equals(FUN_ISNULLORZERO)) {
			Object value = arg.evaluate(evaluator);
			return execIsNullOrZero(value);
		} else if (functName.equals(FUN_TOHOUR)) {
			Object value = arg.evaluate(evaluator);
			return execToHour(value);
		} else if (functName.equals(FUN_ISHOLIDAY)) {
			Object value = arg.evaluate(evaluator);
			return execIsHoliday(value);
		} else if (functName.equals(FUN_STRIPHOUR)) {
			Object value = arg.evaluate(evaluator);
			return execStripHour(value);
		} else if (functName.equals(FUN_STRIPDATE)) {
			Object value = arg.evaluate(evaluator);
			return execStripDate(value);
		} else if (functName.equals(FUN_SQRT)) {
			Object value = arg.evaluate(evaluator);
			if (value instanceof Number) {
				return new Double(Math.sqrt(((Number) value).doubleValue()));
			}
			throw new EvaluateException(
					"La funzione sqrt accetta solo argumenti numerici: " + getFunctionName()
							+ " non è numerico");
		} else if (functName.equals(FUN_TOINT)) {
			Object value = arg.evaluate(evaluator);
			return execToInt(value, null);
		} else if (functName.equals(FUN_TODOUBLE)) {
			Object value = arg.evaluate(evaluator);
			return execToDouble(value, null);
		}

		// tornando null indica che qui non si può valutare la funzione passata
		// la deve valutare qualcun altro
		return null;
	}

	/**
	 * Metodo principale per la valutazione delle funzioni unarie.
	 * 
	 * @param functName
	 *          nome funzione
	 * @param evaluator
	 *          valutatore esterno per la valutazione degli argomenti
	 * @return risultato calcolato oppure null se la valutazione non è possibile
	 *         (richiesto intervento del valutatore esterno)
	 * @throws EvaluateException
	 *           in caso di errori di valutazione
	 */
	private Object internalEvaluateBinary(String functName, Evaluator evaluator)
			throws EvaluateException {
		Symbol arg1 = getChild(0);
		Symbol arg2 = getChild(1);

		if (functName.equals(FUN_MAX) || functName.equals(FUN_MIN)
				|| functName.equals(FUN_ROUND) || functName.equals(FUN_TRUNC)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execNumericFunction(functName, value1, value2);
		} else if (functName.equals(FUN_CUT)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execCut(value1, value2);
		} else if (functName.equals(FUN_FORMAT)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execFormat(value1, value2);
		} else if (functName.equals(FUN_TODATE)) {
      Object value1 = arg1.evaluate(evaluator);
      Object value2 = arg2.evaluate(evaluator);
      return execToDate(value1, value2);
    } else if (functName.equals(FUN_PADLEFT) || functName.equals(FUN_PADRIGHT)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execPad(value1, value2, functName);
		} else if (functName.equals(FUN_WEEKDAY)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execWeekDay(value1, value2);
		} else if (functName.equals(FUN_TOINT)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execToInt(value1, value2);
		} else if (functName.equals(FUN_TODOUBLE)) {
			Object value1 = arg1.evaluate(evaluator);
			Object value2 = arg2.evaluate(evaluator);
			return execToDouble(value1, value2);
		}

		// tornando null indica che qui non si può valutare la funzione passata
		// la deve valutare qualcun altro
		return null;
	}

	/**
	 * Metodo principale per la valutazione delle funzioni a 3 argomenti.
	 * 
	 * @param functName
	 *          nome funzione
	 * @param evaluator
	 *          valutatore esterno per la valutazione degli argomenti
	 * @return risultato calcolato oppure null se la valutazione non è possibile
	 *         (richiesto intervento del valutatore esterno)
	 * @throws EvaluateException
	 *           in caso di errori di valutazione
	 */
	private Object internalEvaluateTertiary(String functName, Evaluator evaluator)
			throws EvaluateException {
		if (functName.equals(FUN_CASE)) {
			return execCase(evaluator);
		}
    if (functName.equals(FUN_SUBSTR)) {
      return execSubstr(evaluator);
    }

		// tornando null indica che qui non si può valutare la funzione passata
		// la deve valutare qualcun altro
		return null;
	}

	/**
	 * Metodo principale per la valutazione delle funzioni a 2 o più argomenti.
	 * 
	 * @param functName
	 *          nome funzione
	 * @param evaluator
	 *          valutatore esterno per la valutazione degli argomenti
	 * @return risultato calcolato oppure null se la valutazione non è possibile
	 *         (richiesto intervento del valutatore esterno)
	 * @throws EvaluateException
	 *           in caso di errori di valutazione
	 */
	private Object internalEvaluateNary(String functName, Evaluator evaluator)
			throws EvaluateException {
		if (functName.equals(FUN_CONCAT)) {
			return execConcat(evaluator);
		}

		// tornando null indica che qui non si può valutare la funzione passata
		// la deve valutare qualcun altro
		return null;
	}

	/**
	 * Controlla che la funzione data abbia un numero di parametri corretto.
	 * Valuta solo la quantità di parametri, non il loro valore.
	 * 
	 * @param functName
	 *          nome funzione da controllare
	 * @return cardinalità funzione
	 * @throws EvaluateException
	 */
	private FunctionCard checkArguments(String functName)
			throws EvaluateException {
		FunctionCard fc = FunctionCard.valueOf(getChildNumber());
		boolean bOK = m_functions.get(fc).contains(functName);
		if (!bOK) {
			// guardo se è una funzione n-aria
			fc = FunctionCard.NARY;
			bOK = m_functions.get(fc).contains(functName);
		}
		if (!bOK) {
			throw new EvaluateException(
					"La funzione %s non ha il corretto numero di parametri(%d).",
					getFunctionName(), getChildNumber());
		}
		return fc;
	}

	/**
	 * Controlla se la funzione di cui è passato il nome è valutabile qui
	 * internamente.
	 * 
	 * @param functName
	 *          nome funzione
	 * @return true sse funziona valutabile internamente
	 */
	private boolean checkFunction(String functName) {
		boolean bOK = m_functions.get(FunctionCard.NULLARY).contains(functName);
		if (!bOK) {
			bOK = m_functions.get(FunctionCard.UNARY).contains(functName);
		}
		if (!bOK) {
			bOK = m_functions.get(FunctionCard.BINARY).contains(functName);
		}
		if (!bOK) {
			bOK = m_functions.get(FunctionCard.TERTIARY).contains(functName);
		}
		if (!bOK) {
			bOK = m_functions.get(FunctionCard.NARY).contains(functName);
		}
		return bOK;
	}

	public void debug(int index) {
		String tabs = index == 0 ? "" : Text.getChars('\t', index);
		System.out.println(tabs + getClassName() + ": " + getText() + ", position "
				+ getPosition());
		for (Symbol arg : getChildren()) {
			arg.debug(index + 1);
		}
	}

	/**
	 * Dato l'indice del giorno nella settimana, ne ritorna la descrizione
	 * 
	 * @param day
	 *          indice giorno, vedi costanti {@link Calendar#SUNDAY},
	 *          {@link Calendar#MONDAY},...
	 * @param abbreviato
	 *          se true torna il nome abbreviato (ad esempio "<b>ven</b>"),
	 *          altrimenti intero (ad esempio "<b>venerdì</b>")
	 * @return descrizione del giorno della settimana
	 */
	private String getWeekday(int day, boolean abbreviato) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] days = abbreviato ? dfs.getShortWeekdays() : dfs.getWeekdays();
		return days[day];
	}

	/**
	 * Implementa funzione {@link #FUN_ABS}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return valore assoluto dell'argomento (numerico)
	 * @throws EvaluateException
	 *           nel caso argomento non sia numerico
	 */
	private Number abs(Object value) throws EvaluateException {
		if (value instanceof Number) {
			if (value instanceof Short) {
				return Math.abs(((Number) value).shortValue());
			}
			if (value instanceof Integer) {
				return Math.abs(((Number) value).intValue());
			}
			if (value instanceof Long) {
				return Math.abs(((Number) value).longValue());
			}
			if (value instanceof Float) {
				return Math.abs(((Number) value).floatValue());
			}
			if (value instanceof Double) {
				return Math.abs(((Number) value).doubleValue());
			}
			if (value instanceof BigDecimal) {
				return Math.abs(((BigDecimal) value).doubleValue());
			}
			return Math.abs(((Number) value).intValue());
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione abs su argomenti non numerici");
		}
	}

	/**
	 * Esegue funzioni varie sulle stringhe.
	 * 
	 * @param funName
	 *          nome funzione da eseguire
	 * @param value
	 *          argomento della funzione
	 * @return risultato funzione; nel caso value sia null, ritorna sempre stringa
	 *         vuota
	 * @throws EvaluateException
	 *           in caso di funzione non prevista
	 */
	private String execStringFunction(String funName, Object value)
			throws EvaluateException {
		if (value == null)
			return "";
		else {
			String arg = value.toString();
			if (funName.equals(FUN_LOWER)) {
				return arg.toLowerCase();
			} else if (funName.equals(FUN_UPPER)) {
				return arg.toUpperCase();
			} else if (funName.equals(FUN_TRIM)) {
				return arg.trim();
			} else if (funName.equals(FUN_LTRIM)) {
				return Text.trimLeft(arg);
			} else if (funName.equals(FUN_RTRIM)) {
				return Text.trimRight(arg);
			} else if (funName.equals(FUN_CAPITALIZE)) {
				return Text.capitalizeWords(arg);
			}
		}
		throw new EvaluateException("Errore interno, funzione non riconosciuta: "
				+ funName);
	}

	/**
	 * Implementa funzione {@link #FUN_MONTH}.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto una data)
	 * @return numero del mese corrispondente all'argomento
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Number execMonth(Object value) throws EvaluateException {
		if (value instanceof Date) {
			return DtUtil.getMonth((Date) value);
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione 'month' su argomenti non data");
		}
	}

	/**
	 * Implementa funzione {@link #FUN_YEAR}.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto una data)
	 * @return anno della data passata
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Number execYear(Object value) throws EvaluateException {
		if (value instanceof Date) {
			return DtUtil.getYear((Date) value);
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione 'year' su argomenti non data");
		}
	}

	/**
	 * Implementa funzione {@link #FUN_DAY}.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto una data)
	 * @return giorno della data passata
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Number execDay(Object value) throws EvaluateException {
		if (value instanceof Date) {
			return DtUtil.getDay((Date) value);
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione 'day' su argomenti non data");
		}
	}

	/**
	 * Implementa funzione {@link #FUN_MONTHNAME}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return nome del mese corrispondente all'argomento (1-based)
	 * @throws EvaluateException
	 *           nel caso di argomento non numerico
	 */
	private String execMonthName(Object value) throws EvaluateException {
		if (!(value instanceof Number)) {
			throw new EvaluateException(
					"Non posso usare la funzione monthname su argomenti non numerici");
		}
		int n = ((Number) value).intValue();

		String[] months = new DateFormatSymbols().getMonths();

		// sottraggo 1 perchè mi aspetto un numero mese 1-based, ma Java è 0-based
		int monthIndex = ((Number) value).intValue() - 1;
		if (monthIndex >= 0 && monthIndex <= 11) {
			return months[monthIndex];
		}
		return "?? mese " + n + "??";
	}

	/**
	 * Implementa funzione {@link #FUN_WEEKDAY}.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto una data)
	 * @return descrizione del giorno della settimana corrispondente alla data
	 *         passata
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private String execWeekDay(Object value) throws EvaluateException {
		if (value instanceof Date) {
			Calendar cal = DtUtil.calendarFromDate((Date) value);
			return getWeekday(cal.get(Calendar.DAY_OF_WEEK), false);
		} else {
			throw new EvaluateException("Non posso usare la funzione '" + FUN_WEEKDAY
					+ "' su argomenti non data");
		}
	}

	/**
	 * Implementa funzione {@link #FUN_DAYSINMONTH}.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto un numero di mese 1-based o
	 *          una data)
	 * @return qta giorni nel mese passato
	 * @throws EvaluateException
	 *           nel caso di argomento di tipo errato
	 */
	private Number execDaysInMonth(Object value) throws EvaluateException {
		if (value instanceof Date) {
			return DtUtil.getGiorniDelMese((Date) value);
		} else if (value instanceof Number) {
			Date d = DtUtil.createDate(DtUtil.getCurrentYear(),
					((Number) value).intValue(), 1);
			return DtUtil.getGiorniDelMese(d);
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione 'daysInMonth' su argomenti non numerici e non data");
		}
	}

	/**
	 * Implementa funzione {@link #FUN_ISHOLIDAY}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return <b>true</b> se il giorno è festivo, <b>false</b> altrimenti
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Object execIsHoliday(Object value) throws EvaluateException {
		if (!(value instanceof Date)) {
			throw new EvaluateException(
					"Non posso usare la funzione isHoliday su argomenti non data");
		}
		return new Boolean(DtUtil.isHoliday((Date) value, DtUtil.HOL_RSM, true));
	}

	/**
	 * Valuta la funzione {@link #FUN_ISNULL}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return true sse l'argomento passato è null
	 */
	private Boolean execIsNull(Object value) {
		if (value == null) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Valuta la funzione {@link #FUN_ISNULLORZERO}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return true sse l'argomento passato è null, o un numero zero o una stringa
	 *         vuota
	 */
	private Boolean execIsNullOrZero(Object value) {
		if (value == null) {
			return Boolean.TRUE;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue() == 0.0;
		} else {
			return value.toString().length() == 0;
		}
	}

	/**
	 * Implementa funzione {@link #FUN_TOHOUR}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return minuti formattati in ora
	 * @throws EvaluateException
	 *           nel caso di argomento non numerico
	 */
	private String execToHour(Object value) throws EvaluateException {
		if (!(value instanceof Number)) {
			throw new EvaluateException(
					"Non posso usare la funzione toHour su argomenti non numerici");
		}
		int n = ((Number) value).intValue();

		int minuti = n % 60;
		int ore = n / 60;

		String sMin = String.valueOf(minuti);
		if (minuti < 10)
			sMin = "0" + sMin;
		String sOre = String.valueOf(ore);

		return sOre + ":" + sMin;
	}

	/**
	 * Implementa funzione {@link #FUN_STRIPHOUR}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return una data copia di quella passata senza la parte ore-minuti-secondi
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Object execStripHour(Object value) throws EvaluateException {
		if (!(value instanceof Date)) {
			throw new EvaluateException(
					"Non posso usare la funzione stripHour su argomenti non data");
		}
		return DtUtil.azzeraHHMMSS((Date) value);
	}

	/**
	 * Implementa funzione {@link #FUN_STRIPDATE}.
	 * 
	 * @param value
	 *          argomento della funzione
	 * @return una data copia di quella passata senza la parte giorno-mese-anno
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private Object execStripDate(Object value) throws EvaluateException {
		if (!(value instanceof Date)) {
			throw new EvaluateException(
					"Non posso usare la funzione stripDate su argomenti non data");
		}
		return DtUtil.azzeraData((Date) value);
	}

	/**
	 * Esegue tutte le funzioni che si aspettano due argomenti numerici
	 * 
	 * @param funName
	 *          nome funzione
	 * @param arg1
	 *          primo argomento
	 * @param arg2
	 *          secondo argomento
	 * @return risultato della valutazione
	 * @throws EvaluateException
	 *           nel caso uno dei due argomenti non sia numerico o la funzione non
	 *           nsia prevista
	 */
	private Object execNumericFunction(String funName, Object arg1, Object arg2)
			throws EvaluateException {
		boolean areNumbers = (arg1 instanceof Number && arg1 instanceof Number);
		if (!areNumbers) {
			throw new EvaluateException("Funzione " + funName
					+ ", almeno uno dei due argomenti non è numerico: " + arg1 + " e "
					+ arg2);
		}
		double d1 = ((Number) arg1).doubleValue();
		double d2 = ((Number) arg2).doubleValue();

		if (funName.equals(FUN_MAX)) {
			if (d1 < d2) {
				return arg2;
			} else {
				return arg1;
			}
		} else if (funName.equals(FUN_MIN)) {
			if (d1 < d2) {
				return arg1;
			} else {
				return arg2;
			}
		} else if (funName.equals(FUN_ROUND) || funName.equals(FUN_TRUNC)) {
			BigDecimal bd = new BigDecimal(d1);
			RoundingMode rm = funName.equals(FUN_ROUND) ? RoundingMode.HALF_UP
					: RoundingMode.DOWN;
			return bd.setScale(((Number) arg2).intValue(), rm);
		}
		throw new EvaluateException("Errore interno, funzione non riconosciuta: "
				+ funName);
	}

	/**
	 * Esegue la funzione {@link #FUN_CUT}.
	 * 
	 * @param arg1
	 *          stringa da tagliare
	 * @param arg2
	 *          qta caratteri da tenere
	 * @return stringa di input con al massimo <var>arg2</var> caratteri
	 * @throws EvaluateException
	 *           nel caso arg2 non sia numerico
	 */
	private String execCut(Object arg1, Object arg2) throws EvaluateException {
		if (!(arg2 instanceof Number))
			throw new EvaluateException(
					"La funzione cut si aspetta un numero come secondo parametro invece di '"
							+ arg2 + "'");

		int n = ((Number) arg2).intValue();
		String s = (arg1 == null) ? "" : arg1.toString();

		if (s.length() > n)
			return s.substring(0, n);
		else
			return s;
	}

	/**
	 * Esegue la funzione {@link #FUN_CONCAT}, cioè la concatenazione di n
	 * oggetti. <br/>
	 * Viene concatenato il valore del metodo toString() di ogni argomento; se un
	 * argomento è null, viene preso come valore stringa vuota ("").
	 * 
	 * @param arg1
	 *          primo valore da concatenare;
	 * @param arg2
	 *          secondo valore da concatenare
	 * @return stringa risultato della concatenazione dei due argomenti
	 * @throws EvaluateException
	 */
	private String execConcat(Evaluator evaluator) throws EvaluateException {
		StringBuilder sb = new StringBuilder();
		for (Symbol arg : getChildren()) {
			Object val = arg.evaluate(evaluator);
			String s = val != null ? val.toString() : "";
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Esegue la funzione {@link #FUN_FORMAT}, cioè la formattazione di una data.
	 * 
	 * @param arg1
	 *          data da formattare
	 * @param arg2
	 *          stringa di formato
	 * @return stringa risultato della formattazione della data
	 * @throws EvaluateException
	 *           nel caso di tipi degli argomenti errati o di stringa di
	 *           formattazione errata
	 */
	private String execFormat(Object arg1, Object arg2) throws EvaluateException {
		if (!(arg2 instanceof String)) {
			throw new EvaluateException("La funzione "+FUN_FORMAT+" si aspetta una stringa come secondo parametro invece di " + arg2);
		}
		if (arg1 == null) {
			return "";
		}
		if (!(arg1 instanceof Date)) {
			throw new EvaluateException("La funzione "+FUN_FORMAT+" si aspetta una data come primo parametro");
		}
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat(arg2.toString());
		} 
		catch (Exception e) {
			throw new EvaluateException("Il formato passato non è valido: " + arg2);
		}
		return sdf.format((Date) arg1);
	}
	
	 /**
   * Esegue la funzione {@link #FUN_TODATE}, cioè la formattazione di una data.
   * 
   * @param arg1
   *          intervallo di tempo
   * @param arg2
   *          unità di tempo dell'intervallo
   * @return stringa risultato della formattazione della data
   * @throws EvaluateException
   *           nel caso di tipi degli argomenti errati o di stringa di
   *           formattazione errata
   */
  private Date execToDate(Object arg1, Object arg2) throws EvaluateException {
    if (!(arg2 instanceof Number)) {
      throw new EvaluateException("La funzione "+FUN_TODATE+" si aspetta un intero come secondo parametro invece di " + arg2);
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(DtUtil.azzeraHHMMSS(new Date()));
    int timeUnit = 0;
    switch ( ((Number)arg2).intValue() ) {
      //MILLI_SECONDI
      case 1: {
        timeUnit = Calendar.MILLISECOND;
      } break;
      // SECONDI
      case 2: {
        timeUnit = Calendar.SECOND;
      } break;
      // MINUTI
      case 3: {
        timeUnit = Calendar.MINUTE;
      } break;
      case 4: {
        timeUnit = Calendar.HOUR;
      } break;
      default: {
        throw new EvaluateException("La funzione "+FUN_TODATE+" si aspetta come secondo parametro un valore tra " //
        		+ "1 (millisecondi), 2 (secondi), 3 (minuti), 4 (ore)");
      }
    }
    if (arg1 == null) {
      return null;
    }
    if (!(arg1 instanceof Number)) {
      throw new EvaluateException("La funzione "+FUN_TODATE+" si aspetta una numero come primo parametro");
    }
    
    calendar.add(timeUnit, ((Number)arg1).intValue());
//    SimpleDateFormat sdf = new SimpleDateFormat();
    return calendar.getTime();
  }

	/**
	 * Esegue le funzioni padLeft e padRight.
	 * 
	 * @param arg1
	 *          stringa da "paddare"
	 * @param arg2
	 *          qta caratteri
	 * @param function
	 *          "padLeft" o "padRight"
	 * @return stringa "paddata"
	 * @throws EvaluateException
	 *           nel caso arg2 non sia numerico
	 */
	private String execPad(Object arg1, Object arg2, String function)
			throws EvaluateException {
		if (!(arg2 instanceof Number))
			throw new EvaluateException(
					"La funzione padLeft/padRight si aspetta un numero come secondo parametro invece di '%s'",
					arg2);
		int n = ((Number) arg2).intValue();
		String s = (arg1 == null) ? "" : arg1.toString();

		if (function.equals(FUN_PADLEFT))
			return Text.padLeft(s, n);
		else
			return Text.padRight(s, n);
	}

	/**
	 * Implementa funzione {@link #FUN_WEEKDAY}, versione con due parametri.
	 * 
	 * @param value
	 *          argomento della funzione (mi aspetto una data)
	 * @param abbrev
	 *          se si vuole la versione abbreviata del nome del giorno (mi aspetto
	 *          un booleano)
	 * @return descrizione del giorno della settimana corrispondente alla data
	 *         passata
	 * @throws EvaluateException
	 *           nel caso di argomento non data
	 */
	private String execWeekDay(Object value, Object abbrev)
			throws EvaluateException {
		if (value instanceof Date) {
			boolean abbr = false;
			if (abbrev instanceof Boolean) {
				abbr = ((Boolean) abbrev).booleanValue();
			} else if (abbrev != null) {
				throw new EvaluateException(
						"Errore in chiamata a '%s': il secondo parametro deve essere booleano.",
						FUN_WEEKDAY);
			}
			Calendar cal = DtUtil.calendarFromDate((Date) value);
			return getWeekday(cal.get(Calendar.DAY_OF_WEEK), abbr);
		} else {
			throw new EvaluateException(
					"Non posso usare la funzione '%s' su argomenti non data", FUN_WEEKDAY);
		}
	}

	private Object execCase(Evaluator evaluator) throws EvaluateException {
		if (getChildNumber() != 3) {
			throw new EvaluateException("la funzione '" + FUN_CASE
					+ "' richiede esattamente 3 argomenti");
		}
		Symbol arg0 = getChild(0);
		Symbol arg1 = getChild(1);
		Symbol arg2 = getChild(2);
		/* valuto il primo argomento (espressione booleana) */
		Object bVal = arg0.evaluate(evaluator);
		if (!(bVal instanceof Boolean)) {
			throw new EvaluateException("il primo argomento della funzione '"
					+ FUN_CASE + "' deve essere un'espressione booleana");
		}
		Object result = null;
		if ((Boolean) bVal) {
			/* valuto il secondo argomento */
			if (arg1 != null) {
				result = arg1.evaluate(evaluator);
			}
		} else {
			/* valuto il terzo argomento */
			if (arg2 != null) {
				result = arg2.evaluate(evaluator);
			}
		}
		return result;
	}

	/**
	 * Esegue funzione <var>substr</var>. <br/>
	 * <code>substr(s, start, len)</code> simile alla funzione substring java
	 * @param evaluator valutatore per gli argomenti
	 * @return risultato funzione
	 * @throws EvaluateException
	 */
  private Object execSubstr(Evaluator evaluator) throws EvaluateException {
    if (getChildNumber() < 2 || getChildNumber() > 3) {
      throw new EvaluateException("la funzione '" + FUN_SUBSTR
          + "' richiede 2 o 3 argomenti");
    }
    Symbol arg0 = getChild(0);
    Symbol arg1 = getChild(1);
    Symbol arg2 = getChild(2);
    /* valuto il primo argomento (espressione qualsiasi) */
    Object val = arg0.evaluate(evaluator);
    if (val == null) {
      return null;
    }
    Object oStart = arg1.evaluate(evaluator);
    if (!(oStart instanceof Number)) {
      throw new EvaluateException("il secondo argomento della funzione '"
          + FUN_SUBSTR + "' deve essere un'espressione numerica");
    }
    Object oLen = arg2.evaluate(evaluator);
    if (oLen != null && !(oLen instanceof Number)) {
      throw new EvaluateException("il terzo argomento della funzione '"
          + FUN_SUBSTR + "' deve essere un'espressione numerica");
    }
    String result;
    String targetString = val.toString();
    int nStart = ((Number)oStart).intValue();
    if (nStart < 0) {
      nStart = targetString.length() + nStart;
    }
    if (oLen != null) {
      result = targetString.substring(nStart, nStart + ((Number)oLen).intValue()); 
    }
    else {
      result = targetString.substring(nStart);
    }
    return result;
  }	
	/**
	 * Converte un valore in Int.<br/>
	 * Quando la conversione è impossibile ritorna la conversione a Int di
	 * <code>defaultValue</code> (se diverso da NULL).<br/>
	 * Se però la conversione è impossibile e <code>defaultValue=null</code>,
	 * allora viene lanciata eccezione.
	 * 
	 * @param value
	 *          valore da convertire in Int
	 * @param defaultValue
	 *          valore di ritorno quando non è possibile convertire
	 *          <code>value</code>, oppure <code>null</code>
	 * @return <code>value</code> convertito in Int
	 * @throws EvaluateException
	 *           se è impossibile convertire <code>value</code> e
	 *           <code>defaultValue=null</code>
	 */
	private Integer execToInt(Object value, Object defaultValue)
			throws EvaluateException {
		Integer intVal = null;
		if (value == null) {
			throw new EvaluateException(
					"funzione '%s', il primo argomento deve essere diverso da null",
					FUN_TOINT);
		}
		if (value instanceof Integer) {
			intVal = (Integer) value;
		} else {
			try {
				intVal = Integer.valueOf(value.toString().trim());
			} catch (NumberFormatException e) {
				if (defaultValue == null) {
					throw new EvaluateException(
							"funzione '%s', impossibile convertire in Int l'argomento '%s'",
							FUN_TOINT, value.toString());
				}
				intVal = execToInt(defaultValue, null);
			}
		}
		return intVal;
	}

	/**
	 * Converte un valore in Double.<br/>
	 * Quando la conversione è impossibile ritorna la conversione a Double di
	 * <code>defaultValue</code> (se diverso da NULL).<br/>
	 * Se però la conversione è impossibile e <code>defaultValue=null</code>,
	 * allora viene lanciata eccezione.
	 * 
	 * @param value
	 *          valore da convertire in Int
	 * @param defaultValue
	 *          valore di ritorno quando non è possibile convertire
	 *          <code>value</code>, oppure <code>null</code>
	 * @return <code>value</code> convertito in Int
	 * @throws EvaluateException
	 *           se è impossibile convertire <code>value</code> e
	 *           <code>defaultValue=null</code>
	 */
	private Double execToDouble(Object value, Object defaultValue)
			throws EvaluateException {
		Double dblVal = null;
		if (value == null) {
			throw new EvaluateException(
					"funzione '%s', il primo argomento deve essere diverso da null",
					FUN_TODOUBLE);
		}
		if (value instanceof Double) {
			dblVal = (Double) value;
		} else {
			try {
				dblVal = Double.valueOf(value.toString());
			} catch (NumberFormatException e) {
				if (defaultValue == null) {
					throw new EvaluateException(
							"funzione '%s', impossibile convertire in Double l'argomento '%s'",
							FUN_TODOUBLE, value.toString());
				}
				dblVal = execToDouble(defaultValue, null);
			}
		}
		return dblVal;
	}

	@Override
	public boolean isFunction() {
		return true;
	}

	@Override
	public boolean isNullaryFunction() {
		return getChildNumber() == 0;
	}

	@Override
	public boolean isUnaryFunction() {
		return getChildNumber() == 1;
	}

	@Override
	public boolean isBinaryFunction() {
		return getChildNumber() == 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ciscoop.expressions.symbols.Symbol#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return false;
	}

	/**
	 * Ritorna il testo di questa funzione assieme ai suoi parametri  
	 */
	@Override
	public String getText() {
		StringBuilder sb = new StringBuilder(getFunctionName() + "(");
		int nPar=0;
		for (Symbol child : getChildren()) {
			if (nPar > 0)
				sb.append(',');
			sb.append(child.getText());
			nPar++;
		}
		sb.append(')');
				
		return sb.toString();
	}

	public String getFunctionName() {
		return m_functionName;
	}
	
  /* (non-Javadoc)
   * @see ciscoop.expressions.symbols.Symbol#isConcrete()
   */
  @Override
  public boolean isConcrete() {
    return true;
  }
	
}
