import java.io.*;
import java.util.*;

public class JackTokenizer {
	String token, code;
	private StringTokenizer st;
	private Scanner sc;
	private Set<String> keywords, symbols;

	public JackTokenizer(String fileName) {
		try {
			sc = new Scanner(new File(fileName));
			token = code = null;
			st = null;
			initialKeywords();
			initialSymbols();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initialKeywords() {
		keywords = new HashSet<String>();
		String[] s = new String[] {"class", "constructor", "function", "method", "field", "static", "var", "int",
				"char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
				"return"};
		Collections.addAll(keywords, s);
	}

	private void initialSymbols() {
		symbols = new HashSet<String>();
		String[] s = new String[] {"{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|",
				"<", ">", "=", "~"};
		Collections.addAll(symbols, s);
	}

	public boolean hasMoreTokens() {
		return sc.hasNextLine() || (st != null && st.hasMoreTokens());
	}
	
	public void advance() {
		if (st != null && st.hasMoreTokens()) {
			token = st.nextToken();
			if (token.startsWith("\"")) advanceStringConstant();
			return;
		}
		skipComments();
		if (code == null) return;
		addSpaceDelimiterToCode();
		st = new StringTokenizer(code);
		if (st.hasMoreTokens()) {
			token = st.nextToken();
			if (token.startsWith("\"")) advanceStringConstant();
		}
	}

	private void advanceStringConstant() {
		do {
			token = token + " " + st.nextToken();
		} while (!token.endsWith("\""));
	}

	private void skipComments() {
		do  {
			if (!sc.hasNextLine()) {
				token = code = null;
				return;
			}
			code = sc.nextLine().trim();
		} while (code.length() == 0 || code.startsWith("/")  || code.startsWith("*"));
		int commentIdx = code.indexOf("//");
		if (commentIdx != -1){
			code = code.substring(0, commentIdx).trim();
		}
	}

	private void addSpaceDelimiterToCode() {
		for (String symbol: symbols) {
			code = code.replaceAll(String.format("\\%s", symbol), " " + symbol + " ");
		}
	}

	public String tokenType() {
		if (keywords.contains(token)) {
			return "keyword";
		} else if (symbols.contains(token)) {
			return "symbol";
		} else if (token.matches("[0-9]+")) {
			return "integerConstant";
		} else if (token.matches("\".*\"")) {
			return "stringConstant";
		} else {
			return "identifier";
		}
	}
	
	public String keyWord() {
		return token;
	}
	
	public String symbol() {
		return token;
	}

	public String identifier() {
		return token;
	}

	public int intVal() {
		return Integer.parseInt(token);
	}

	public String stringVal() {
		return token.replaceAll("\"", "");
	}

	public static void main(String[] args) throws FileNotFoundException {
		File dir = new File(args[0]);
		File[] inputFiles;
		if (dir.isDirectory()) {
			inputFiles = dir.listFiles();
		} else {
			inputFiles = new File[] {dir};
		}
        for (File f: inputFiles) {
            if (!f.getName().endsWith(".jack")) continue;
            JackTokenizer jt = new JackTokenizer(f.getPath());
            PrintWriter pw = new PrintWriter(f.getPath().replace(".jack", "T.xml"));
            pw.println("<tokens>");
            while (jt.hasMoreTokens()) {
                jt.advance();
                if (jt.token == null) continue;
                String tokenType = jt.tokenType();
                pw.print("<" + tokenType + ">");
                if (tokenType.equals("symbol")) {
                    pw.print(jt.symbol());
                } else if (tokenType.equals("stringConstant")){
                    pw.print(jt.stringVal());
                } else {
                	pw.print(jt.token);
				}
                pw.print("</" + tokenType + ">");
                pw.println();
            }
            pw.println("</tokens>");
            pw.close();
            System.out.println(f.getPath() + ", Done!");
        }
	}
}