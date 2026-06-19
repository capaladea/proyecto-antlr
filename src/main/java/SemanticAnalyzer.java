

public class SemanticAnalyzer extends GramaticaBaseVisitor<String>{

    private final SymbolTable symbolTable = new SymbolTable();

    @Override
    public String visitProgram(GramaticaParser.ProgramContext ctx) {
        // Le decimos al analizador que recorra todas las sentencias dentro de las llaves
        for (GramaticaParser.SentenceContext sentenceCtx : ctx.sentence()) {
            visit(sentenceCtx);
        }
        return null;
    }

    @Override
    public String visitSentence(GramaticaParser.SentenceContext ctx) {
        // Si la sentencia es un println, la visitamos
        if (ctx.println() != null) {
            return visit(ctx.println());
        }
        return null;
    }

    @Override
    public String visitPrintln(GramaticaParser.PrintlnContext ctx) {
        visit(ctx.expression()); // Validamos la expresión matemática de adentro
        return null;
    }

    @Override
    public String visitExpression(GramaticaParser.ExpressionContext ctx) {
        return visit(ctx.andExpr(0));
    }

    @Override
    public String visitAndExpr(GramaticaParser.AndExprContext ctx) {
        return visit(ctx.comparisonExpr(0));
    }

    @Override
    public String visitComparisonExpr(GramaticaParser.ComparisonExprContext ctx) {
        return visit(ctx.arithmeticExpr(0));
    }

    @Override
    public String visitArithmeticExpr(GramaticaParser.ArithmeticExprContext ctx) {
        return visit(ctx.multiplicativeExpr(0));
    }

    @Override
    public String visitMultiplicativeExpr(GramaticaParser.MultiplicativeExprContext ctx) {
        // 1. Empezamos evaluando el tipo del primer hijo (el de la izquierda)
        String tipoIzquierdo = visit(ctx.unaryExpr(0));

        // 2. Si existen más hijos, significa que hay operaciones (* o /)
        if (ctx.unaryExpr().size() > 1) {
            // Recorremos los demás elementos de la operación
            for (int i = 1; i < ctx.unaryExpr().size(); i++) {
                String tipoDerecho = visit(ctx.unaryExpr(i));

                // Validación: Ambos lados deben ser numéricos
                if (!("INT".equals(tipoIzquierdo) || "FLOAT".equals(tipoIzquierdo)) ||
                        !("INT".equals(tipoDerecho) || "FLOAT".equals(tipoDerecho))) {
                    throw new RuntimeException("Error Semántico: No se puede operar matemáticamente con tipos no numéricos.");
                }

                // Regla de promoción de tipos: Si uno es FLOAT, el resultado es FLOAT
                if ("FLOAT".equals(tipoIzquierdo) || "FLOAT".equals(tipoDerecho)) {
                    tipoIzquierdo = "FLOAT";
                } else {
                    tipoIzquierdo = "INT";
                }
            }
        }

        // Retornamos el tipo final calculado
        return tipoIzquierdo;
    }

    @Override
    public String visitUnaryExpr(GramaticaParser.UnaryExprContext ctx) {
        // Caso 1: Negación lógica con '!'
        if (ctx.NOT() != null) {
            String tipoHijo = visit(ctx.unaryExpr());
            if (!"BOOLEAN".equals(tipoHijo)) {
                throw new RuntimeException("Error Semántico: El operador '!' no se puede aplicar a un tipo " + tipoHijo);
            }
            return "BOOLEAN";
        }

        // Caso 2: Menos unario con '-'
        if (ctx.MINUS() != null) {
            String tipoHijo = visit(ctx.unaryExpr());

            // Validación: Solo permitimos INT o FLOAT
            if (!"INT".equals(tipoHijo) && !"FLOAT".equals(tipoHijo)) {
                throw new RuntimeException("Error Semántico: El operador '-' unario no se puede aplicar a un tipo " + tipoHijo);
            }

            return tipoHijo; // Si era INT, sigue siendo INT. Si era FLOAT, sigue siendo FLOAT.
        }

        // Caso base: Si no hay operador unario, pasa al 'term' [cite: 12]
        return visit(ctx.term());
    }

    @Override
    public String visitTerm(GramaticaParser.TermContext ctx) {
        // Si es un número entero
        if (ctx.INT() != null) {
            return "INT";
        }

        // Si es un número flotante
        if (ctx.FLOAT() != null) {
            return "FLOAT";
        }

        // Si es un booleano (true/false)
        if (ctx.BOOLEAN() != null) {
            return "BOOLEAN";
        }

        // Si es una cadena de texto
        if (ctx.STRING() != null) {
            return "STRING";
        }

        // Si es un identificador (variable)
        if (ctx.ID() != null) {
            String varName = ctx.ID().getText();
            // Retorna el tipo guardado en la tabla (ej: "INT")
            // Si no existe, la misma SymbolTable lanzará la excepción
            return (String) symbolTable.get(varName);
        }

        // Si es una expresión entre paréntesis (PAR_OPEN expression PAR_CLOSE)
        if (ctx.expression() != null) {
            // Evaluamos recursivamente el tipo de la expresión interna
            return visit(ctx.expression());
        }
        return null;
    }
}
