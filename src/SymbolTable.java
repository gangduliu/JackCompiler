import java.util.*;

public class SymbolTable {

    Map<String, Table> classLevel, subroutineLevel;
    private int fieldCount, staticCount;
    private int argCount, nVar;

    public SymbolTable() {
        classLevel = new HashMap<String, Table>();
        fieldCount = staticCount = 0;
        startSubroutin();
    }

    void startSubroutin() {
        subroutineLevel = new HashMap<String, Table>();
        argCount = nVar = 0;
    }

    void define(String name, String type, String kind) {
        if (kind.equals("field") || kind.equals("static")) {
            if (!classLevel.containsKey(name))
                classLevel.put(name, new Table(name, type, kind, varCount(kind)));
        } else {
            if (!subroutineLevel.containsKey(name))
                subroutineLevel.put(name, new Table(name, type, kind, varCount(kind)));
        }
    }

    private int varCount(String kind) {
        if (kind.equals("field")) {
            fieldCount++;
            return fieldCount - 1;
        } else if (kind.equals("static")) {
            staticCount++;
            return staticCount - 1;
        } else if (kind.equals("argument")) {
            argCount++;
            return argCount - 1;
        } else {
            nVar++;
            return nVar - 1;
        }
    }

    String kindOf(String name) {
        if (subroutineLevel.containsKey(name))
            return subroutineLevel.get(name).kind;
        else if (classLevel.containsKey(name))
            return classLevel.get(name).kind;
        else
            return null;
    }

    String typeOf(String name) {
        if (subroutineLevel.containsKey(name))
            return subroutineLevel.get(name).type;
        else if (classLevel.containsKey(name))
            return classLevel.get(name).type;
        else
            return null;
    }

    int indexOf(String name) {
        if (subroutineLevel.containsKey(name))
            return subroutineLevel.get(name).index;
        else if (classLevel.containsKey(name))
            return classLevel.get(name).index;
        else
            return -1;
    }

    String getTable(String name) {
        if (classLevel.containsKey(name))
            return classLevel.get(name).toString();
        else
            return subroutineLevel.get(name).toString();
    }
}

class Table {
    String name, type, kind;
    int index;

    Table(String name, String type, String kind, int index) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String toString() {
        return String.format("name: %s, type: %s, kind: %s, index: %d", name, type, kind, index);
    }
}