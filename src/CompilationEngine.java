import java.io.*;
import java.util.*;

class CompilationEngine {
    private VMWriter vmw;
    private JackTokenizer jt;
    private Set<String> operators;
    private SymbolTable st;
    private String className;
    private int fieldNum, labelNum;
    private String subroutineName, subroutineKind, subroutineType;

    CompilationEngine(JackTokenizer jt, VMWriter pw) {
        this.jt = jt;
        vmw = pw;
        jt.advance();
        initOperators();
        st = new SymbolTable();
        fieldNum = 0;
        labelNum = -1;
        compileClass();
    }

    private void initOperators() {
        operators = new HashSet<String>();
        String[] s = new String[] {"+", "-", "*", "/", "&", "|", "<", ">", "=", "~"};
        Collections.addAll(operators, s);
    }

    private void compileClass() {
        jt.advance();
        className = jt.token;
        jt.advance();
        jt.advance();
        while (jt.keyWord().equals("static") || jt.keyWord().equals("field")) {
            compileClassVarDec();
        }
        while (jt.keyWord().equals("constructor") || jt.keyWord().equals("function") || jt.keyWord().equals("method")) {
            compileSubroutineDec();
            st.startSubroutin();
        }
        jt.advance();
    }

    private void compileClassVarDec() {
        String kind = jt.token;
        jt.advance();
        String type = jt.token;
        jt.advance();
        String name = jt.token;
        jt.advance();
        st.define(name, type, kind);
        if (kind.equals("field")) fieldNum++;
        while (jt.symbol().equals(",")) {
            jt.advance();
            name = jt.token;
            jt.advance();
            st.define(name, type, kind);
            if (kind.equals("field")) fieldNum++;
        }
        jt.advance();
    }

    private void compileSubroutineDec() {
        subroutineKind = jt.token;
        jt.advance();
        subroutineType = jt.token;
        jt.advance();
        subroutineName = className + "." + jt.token;
        jt.advance();
        jt.advance();
        if (subroutineKind.equals("method")) {
            st.define("this", className, "argument");
        }
        compileParameterList();
        jt.advance();
        compileSubroutineBody();
    }

    private void compileParameterList() {
        if (jt.tokenType().equals("symbol")) {
            return;
        }
        String kind = "argument";
        String type = jt.token;
        jt.advance();
        String name = jt.token;
        jt.advance();
        st.define(name, type, kind);
        while (jt.symbol().equals(",")) {
            jt.advance();
            type = jt.token;
            jt.advance();
            name = jt.token;
            jt.advance();
            st.define(name, type, kind);
        }
    }

    private void compileSubroutineBody() {
        jt.advance();
        int n = 0;
        while (jt.keyWord().equals("var")) {
            n = n + compileVarDec();
        }
        vmw.writeFunction(subroutineName, n);
        if (subroutineKind.endsWith("constructor")) {
            vmw.writePush("constant", fieldNum);
            vmw.writeCall("Memory.alloc", 1);
            vmw.writePop("pointer", 0);
        } else if (subroutineKind.equals("method")) {
            vmw.writePush("argument", 0);
            vmw.writePop("pointer", 0);
        }
        compileStatements();
        jt.advance();
    }

    private int compileVarDec() {
        int n = 0;
        jt.advance();
        String kind = "local";
        String type = jt.token;
        jt.advance();
        String name = jt.token;
        jt.advance();
        st.define(name, type, kind);
        n++;
        while (jt.symbol().equals(",")) {
            jt.advance();
            name = jt.token;
            jt.advance();
            st.define(name, type, kind);
            n++;
        }
        jt.advance();
        return n;
    }

    private void compileStatements() {
        while (true) {
            if (jt.keyWord().equals("let")) {
                compileLet();
                continue;
            }
            if (jt.keyWord().equals("if")) {
                labelNum++;
                compileIf(labelNum);
                continue;
            }
            if (jt.keyWord().equals("while")) {
                labelNum++;
                compileWhile(labelNum);
                continue;
            }
            if (jt.keyWord().equals("do")) {
                compileDo();
                continue;
            }
            if (jt.keyWord().equals("return")) {
                compileReturn();
                continue;
            }
            break;
        }
    }

    private void compileLet() {
        jt.advance();
        String name = jt.token;
        jt.advance();
        if (jt.symbol().equals("[")) {
            vmw.writePush(st.kindOf(name), st.indexOf(name));
            jt.advance();
            compileExpression();
            jt.advance();
            vmw.writeArithmetic("+");
            jt.advance();
            compileExpression();
            vmw.writePop("temp", 0);
            vmw.writePop("pointer", 1);
            vmw.writePush("temp", 0);
            vmw.writePop("that", 0);
        } else {
            jt.advance();
            compileExpression();
            vmw.writePop(st.kindOf(name), st.indexOf(name));
        }
        jt.advance();
    }

    private void compileIf(int n) {
        jt.advance();
        jt.advance();
        compileExpression();
        vmw.writeArithmetic("~");
        vmw.writeIf("IF_TRUE" + n);
        jt.advance();
        jt.advance();
        compileStatements();
        jt.advance();
        vmw.writeGoto("IF_FALSE" + n);
        vmw.writeLabel("IF_TRUE" + n);
        if (jt.token.equals("else")) {
            jt.advance();
            jt.advance();
            compileStatements();
            jt.advance();
        }
        vmw.writeLabel("IF_FALSE" + n);
    }

    private void compileWhile(int n) {
        vmw.writeLabel("WHILE_EXP" + n);
        jt.advance();
        jt.advance();
        compileExpression();
        vmw.writeArithmetic("~");
        vmw.writeIf("WHILE_END" + n);
        jt.advance();
        jt.advance();
        compileStatements();
        jt.advance();
        vmw.writeGoto("WHILE_EXP" + n);
        vmw.writeLabel("WHILE_END" + n);
    }

    private void compileDo() {
        jt.advance();
        compileExpression();
        vmw.writePop("temp", 0);
        jt.advance();
    }

    private void compileReturn() {
        jt.advance();
        if (!jt.token.equals(";")) {
            compileExpression();
        }
        if (subroutineType.equals("void")) {
            vmw.writePush("constant", 0);
        }
        vmw.writeReturn();
        jt.advance();
    }

    private void compileExpression() {
        compileTerm();
        while (operators.contains(jt.token)) {
            String symbol = jt.symbol();
            jt.advance();
            compileTerm();
            vmw.writeArithmetic(symbol);
        }
    }

    private void compileTerm() {
        if (jt.tokenType().equals("integerConstant")) {
            vmw.writePush("constant", jt.intVal());
            jt.advance();
        } else if (jt.tokenType().equals("stringConstant")) {
            String s = jt.token;
            vmw.writePush("constant", s.length()-2);
            vmw.writeCall("String.new", 1);
            for (int i = 1; i < s.length()-1; i++) {
                vmw.writePush("constant", (int) s.charAt(i));
                vmw.writeCall("String.appendChar", 2);
            }
            jt.advance();
        } else if (jt.tokenType().equals("keyword")){
            if (jt.token.equals("true")) {
                vmw.writePush("constant", 0);
                vmw.writeArithmetic("~");
            } else if (jt.token.equals("false") || jt.token.equals("null")) {
                vmw.writePush("constant", 0);
            } else {
                vmw.writePush("pointer", 0);
            }
            jt.advance();
        } else if (jt.tokenType().equals("symbol")) {
            if (jt.token.equals("-") || jt.token.equals("~")) {
                String symbol = jt.symbol().equals("-") ? "neg" : jt.symbol();
                jt.advance();
                compileTerm();
                vmw.writeArithmetic(symbol);
            } else if (jt.token.equals("(")) {
                jt.advance();
                compileExpression();
                jt.advance();
            }
        } else if (jt.tokenType().equals("identifier")){
            String name = jt.identifier();
            jt.advance();
            int n = 0;
            if (jt.token.equals("[")) {
                vmw.writePush(st.kindOf(name), st.indexOf(name));
                jt.advance();
                compileExpression();
                jt.advance();
                vmw.writeArithmetic("+");
                vmw.writePop("pointer", 1);
                vmw.writePush("that", 0);
            } else if (jt.token.equals("(")) {
                vmw.writePush("pointer", 0);
                name = className + "." + name;
                jt.advance();
                n = 1 + compileExpressionList();
                jt.advance();
                vmw.writeCall(name, n);
            } else if (jt.token.equals(".")) {
                jt.advance();
                if (st.kindOf(name) != null) {
                    vmw.writePush(st.kindOf(name), st.indexOf(name));
                    name = st.typeOf(name) + "." + jt.identifier();
                    n = 1;
                } else {
                    name = name + "." + jt.identifier();
                }
                jt.advance();
                jt.advance();
                n = n + compileExpressionList();
                jt.advance();
                vmw.writeCall(name, n);
            } else {
                vmw.writePush(st.kindOf(name), st.indexOf(name));
            }
        }
    }

    private int compileExpressionList() {
        int n = 0;
        if (!jt.token.equals(")")) {
            compileExpression();
            n++;
            while (jt.token.equals(",")) {
                jt.advance();
                compileExpression();
                n++;
            }
        }
        return n;
    }

    private void eat(String tokenType) {
        if (tokenType.equals(jt.tokenType())) {
            vmw.print("<" + tokenType + ">");
            if (tokenType.equals("stringConstant")) {
               vmw.print(jt.stringVal());
            } else if (tokenType.equals("symbol")) {
                vmw.print(jt.symbol());
            } else {
                vmw.print(jt.token);
            }
            vmw.print("</" + tokenType + ">");
            vmw.println();
        } else {
            vmw.close();
            throw new NullPointerException();
        }
        if (jt.hasMoreTokens()) jt.advance();
    }
}
