import java.io.*;
import java.util.Arrays;

public class JackCompiler {
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
            VMWriter vmw = new VMWriter(f.getPath().replace(".jack", ".vm"));
            CompilationEngine ce = new CompilationEngine(jt, vmw);
            vmw.close();
        }
    }
}
