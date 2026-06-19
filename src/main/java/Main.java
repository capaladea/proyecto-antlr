import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Cargar el archivo
        String input = "src/test/test01.prg";
        GramaticaLexer lexer = new GramaticaLexer(CharStreams.fromFileName(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GramaticaParser parser = new GramaticaParser(tokens);

        // 2. Parsear (Generar el árbol)
        ParseTree tree = parser.program();

        // 3. Fase Semántica (Auditoría)
        System.out.println("--- Iniciando Análisis Semántico ---");
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.visit(tree);
        System.out.println("¡Análisis semántico exitoso!");

        // 4. Fase de Ejecución (Acción)
        System.out.println("--- Iniciando Ejecución ---");
        Interpreter interpreter = new Interpreter();
        interpreter.visit(tree);

        System.out.println("\n--- Fin de ejecución ---");
    }
}