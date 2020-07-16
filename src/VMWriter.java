import java.io.*;
import java.util.*;

public class VMWriter {
    PrintWriter pw;
    Map<String, String> arithmeticMap;

    public VMWriter(String fileName) {
        try {
            pw = new PrintWriter(fileName);
            initArithmeticMap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void println() {
        pw.println();
    }

    void println(String s) {
        pw.println(s);
    }

    void print(String s) {
        pw.print(s);
    }

    void initArithmeticMap() {
        arithmeticMap = new HashMap<String, String>();
        arithmeticMap.put("+", "add");
        arithmeticMap.put("-", "sub");
        arithmeticMap.put("neg", "neg");
        arithmeticMap.put("=", "eq");
        arithmeticMap.put(">", "gt");
        arithmeticMap.put("<", "lt");
        arithmeticMap.put("&", "and");
        arithmeticMap.put("|", "or");
        arithmeticMap.put("~", "not");
        arithmeticMap.put("*", "call Math.multiply 2");
        arithmeticMap.put("/", "call Math.divide 2");
    }

    void writePush(String segment, int index) {
        if (segment.equals("field")) {
            pw.println("push this " + index);
        } else {
            pw.println("push " + segment + " " + index);
        }
    }

    void writePop(String segment, int index) {
        if (segment.equals("field")) {
            pw.println("pop this " + index);
        } else {
            pw.println("pop " + segment + " " + index);
        }
    }

    void writeArithmetic(String command) {
        pw.println(arithmeticMap.get(command));
    }

    void writeLabel(String label) {
        pw.println("label " + label);
    }

    void writeGoto(String label) {
        pw.println("goto " + label);
    }

    void writeIf(String label) {
        pw.println("if-goto " + label);
    }

    void writeCall(String name, int nArgs) {

        pw.println("call " + name + " " + nArgs);
    }

    void writeFunction(String name, int nLocals) {

        pw.println("function " + name + " " + nLocals);
    }

    void writeReturn() {
        pw.println("return");
    }

    void close() {
        pw.close();
    }
}
