
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    // Usamos <String, Object> para ser genéricos.
    // En el SemanticAnalyzer, el Object será el "Tipo" (ej: "INT")
    // En el Interpreter, el Object será el "Valor" (ej: 10, true, "hola")
    private Map<String, Object> table = new HashMap<>();

    // Declarar una variable (registrar su existencia)
    public void declare(String name, Object valueOrType) {
        if (table.containsKey(name)) {
            throw new RuntimeException("Error: La variable '" + name + "' ya ha sido declarada.");
        }
        table.put(name, valueOrType);
    }

    // Actualizar una variable existente
    public void assign(String name, Object value) {
        if (!table.containsKey(name)) {
            throw new RuntimeException("Error: La variable '" + name + "' no ha sido declarada.");
        }
        table.put(name, value);
    }

    // Obtener el valor o tipo
    public Object get(String name) {
        if (!table.containsKey(name)) {
            throw new RuntimeException("Error: La variable '" + name + "' no existe en este ámbito.");
        }
        return table.get(name);
    }

    public boolean exists(String name) {
        return table.containsKey(name);
    }
}