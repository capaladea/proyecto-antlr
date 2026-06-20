import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        try {   // Cargar el archivo
                String input = "src/test/test01.prg";

                GramaticaLexer lexer = new GramaticaLexer(CharStreams.fromFileName(input));

                // Forzar al LEXER a lanzar excepción ante errores de tokens: falta un ';' o un '(' etc.
                lexer.removeErrorListeners();
                lexer.addErrorListener(new BaseErrorListener() {
                    @Override
                     public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                            throw new ParseCancellationException("Error Léxico en línea " + line + ":" + charPositionInLine + " -> " + msg);
                    }
                });

                CommonTokenStream tokens = new CommonTokenStream(lexer);
                GramaticaParser parser = new GramaticaParser(tokens);

                // Forzar al PARSER a lanzar excepción ante errores de sintaxis
                parser.removeErrorListeners(); // Limpiamos los mensajes feos por defecto
                parser.setErrorHandler(new BailErrorStrategy());

                // Parsear (Genera el árbol)
                ParseTree tree = parser.program();

                // Fase Semántica (Auditoría)
                System.out.println("--- Iniciando Análisis Semántico ---");
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
                semanticAnalyzer.visit(tree);
                System.out.println("--- ¡Análisis semántico exitoso! ---");

                // Fase de Ejecución (Acción)
                System.out.println("--- Iniciando Ejecución ---");
                Interpreter interpreter = new Interpreter();
                interpreter.visit(tree);

                System.out.println("\n--- Fin de ejecución ---");

            } catch (ParseCancellationException e) {
                System.err.println("\n[ERROR SINTÁCTICO / LÉXICO]: El código fuente está mal escrito.");
                System.err.println("Detalle del error de ANTLR: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Causa: " + e.getCause().getMessage());
            }
            } catch (RuntimeException e) {
                System.err.println("\n[ERROR EN TIEMPO DE EJECUCIÓN / SEMÁNTICO]:");
                System.err.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("\n[ERROR CRÍTICO DEL SISTEMA]: " + e.getMessage());
        }
    }
}