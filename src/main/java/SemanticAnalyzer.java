

public class SemanticAnalyzer extends GramaticaBaseVisitor<String>{

    private final SymbolTable tablaSimbolos = new SymbolTable();

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
        // Si la sentencia es una declaracion de variable, la visitamos
        if (ctx.varDecl() != null) {
            return visit(ctx.varDecl());
        }
        // Si es una asignación de variable, la auditamos
        if (ctx.varAssign() != null) {
            return visit(ctx.varAssign());
        }
        return null;
    }

    @Override
    public String visitPrintln(GramaticaParser.PrintlnContext ctx) {
        visit(ctx.expression()); // Validamos la expresión matemática de adentro
        return null;
    }

    @Override
    public String visitVarDecl(GramaticaParser.VarDeclContext ctx) {
        String nombreVar = ctx.ID().getText();

        // Tu tablaSimbolos se encarga de tirar la excepción si ya existe.
        // es UNDEF porque aun no tiene tipo
        tablaSimbolos.declare(nombreVar, "UNDEF");

        return null;
    }

    @Override
    public String visitVarAssign(GramaticaParser.VarAssignContext ctx) {
        String nombreVar = ctx.ID().getText();

        // Verificar si la variable fue declarada
        if (!tablaSimbolos.exists(nombreVar)) {
            throw new RuntimeException("Error Semántico: La variable '" + nombreVar + "' no ha sido declarada.");
        }

        // Obtener el tipo actual de la variable en la tabla y el tipo de la expresión derecha
        String tipoActual = (String) tablaSimbolos.get(nombreVar);
        String tipoExpresion = visit(ctx.expression()); // Analiza la jerarquia de expresiones

        // Aplicar la regla de tipado dinámico-fijo
        if ("UNDEF".equals(tipoActual)) {
            // Es la primera asignación: definimos el tipo de la variable para siempre
            tablaSimbolos.assign(nombreVar, tipoExpresion);
        } else {
            // Ya tenía tipo: validamos que coincidan estrictamente
            if (!tipoActual.equals(tipoExpresion)) {
                throw new RuntimeException("Error Semántico: No se puede asignar un valor de tipo " + tipoExpresion +
                        " a la variable '" + nombreVar + "' que es de tipo " + tipoActual + ".");
            }
        }

        return null; // Es una acción, no devuelve tipo hacia arriba
    }

    @Override
    public String visitExpression(GramaticaParser.ExpressionContext ctx) {
        // Metodo puente: simplemente delega en orExpr
        return visit(ctx.orExpr());
    }

    @Override
    public String visitOrExpr(GramaticaParser.OrExprContext ctx) {
        // Acá se muda la lógica real del OR (||)
        String tipoIzquierdo = visit(ctx.andExpr(0));

        if (ctx.andExpr().size() > 1) {
            for (int i = 1; i < ctx.andExpr().size(); i++) {
                String tipoDerecho = visit(ctx.andExpr(i));

                if (!"BOOLEAN".equals(tipoIzquierdo) || !"BOOLEAN".equals(tipoDerecho)) {
                    throw new RuntimeException("Error Semántico: El operador lógico '||' solo se puede aplicar a expresiones booleanas.");
                }
            }
            return "BOOLEAN";
        }
        return tipoIzquierdo;
    }

    @Override
    public String visitAndExpr(GramaticaParser.AndExprContext ctx) {
        // 1. Evaluamos el tipo del primer hijo (izquierdo)
        String tipoIzquierdo = visit(ctx.comparisonExpr(0));

        // 2. Si hay más de un hijo comparisonExpr, significa que hay operadores "&&"
        if (ctx.comparisonExpr().size() > 1) {
            for (int i = 1; i < ctx.comparisonExpr().size(); i++) {
                String tipoDerecho = visit(ctx.comparisonExpr(i));

                // Validación estricta: ambos lados deben ser BOOLEAN
                if (!"BOOLEAN".equals(tipoIzquierdo) || !"BOOLEAN".equals(tipoDerecho)) {
                    throw new RuntimeException("Error Semántico: El operador lógico '&&' solo se puede aplicar a expresiones booleanas.");
                }
            }
            // Si pasa la validación, el resultado de la cadena de ANDs es BOOLEAN
            return "BOOLEAN";
        }

        // Si no había operador &&, sube el tipo original (que podría ser INT, FLOAT, etc.)
        return tipoIzquierdo;
    }
    @Override
    public String visitComparisonExpr(GramaticaParser.ComparisonExprContext ctx) {
        // 1. Evaluamos el tipo del lado izquierdo
        String tipoIzquierdo = visit(ctx.arithmeticExpr(0));

        // 2. Si hay más de un hijo, significa que hay un operador de comparación
        if (ctx.arithmeticExpr().size() > 1) {
            for (int i = 1; i < ctx.arithmeticExpr().size(); i++) {
                String tipoDerecho = visit(ctx.arithmeticExpr(i));

                // Obtenemos el texto del operador (<, >, ==, etc.)
                String operador = ctx.getChild(i * 2 - 1).getText();

                if ("==".equals(operador) || "!=".equals(operador)) {
                    // Para igualdad/desigualdad, permitimos que sean iguales, o que ambos sean numéricos
                    boolean ambosNumericos = ("INT".equals(tipoIzquierdo) || "FLOAT".equals(tipoIzquierdo)) &&
                            ("INT".equals(tipoDerecho) || "FLOAT".equals(tipoDerecho));

                    if (!tipoIzquierdo.equals(tipoDerecho) && !ambosNumericos) {
                        throw new RuntimeException("Error Semántico: No se puede comparar la igualdad entre los tipos " + tipoIzquierdo + " y " + tipoDerecho);
                    }
                } else {
                    // Para <, <=, >, >= obligatoriamente deben ser numéricos
                    if (!("INT".equals(tipoIzquierdo) || "FLOAT".equals(tipoIzquierdo)) ||
                            !("INT".equals(tipoDerecho) || "FLOAT".equals(tipoDerecho))) {
                        throw new RuntimeException("Error Semántico: El operador '" + operador + "' solo se puede aplicar a tipos numéricos.");
                    }
                }
            }
            // Toda comparación válida produce un Booleano hacia arriba
            return "BOOLEAN";
        }

        // Si no había operador, sube el tipo original sin alterarse
        return tipoIzquierdo;
    }

    @Override
    public String visitArithmeticExpr(GramaticaParser.ArithmeticExprContext ctx) {
        // 1. Empezamos evaluando el tipo del primer hijo (el de la izquierda)
        String tipoIzquierdo = visit(ctx.multiplicativeExpr(0));

        // 2. Si hay más de un hijo multiplicativeExpr, hay operaciones (+ o -)
        if (ctx.multiplicativeExpr().size() > 1) {
            for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
                String tipoDerecho = visit(ctx.multiplicativeExpr(i));

                // Validación: Ambos lados deben ser obligatoriamente numéricos
                if (!("INT".equals(tipoIzquierdo) || "FLOAT".equals(tipoIzquierdo)) ||
                        !("INT".equals(tipoDerecho) || "FLOAT".equals(tipoDerecho))) {
                    throw new RuntimeException("Error Semántico: No se pueden realizar operaciones aritméticas con tipos no numéricos.");
                }

                // Regla de promoción: Si cualquiera es FLOAT, el resultado es FLOAT
                if ("FLOAT".equals(tipoIzquierdo) || "FLOAT".equals(tipoDerecho)) {
                    tipoIzquierdo = "FLOAT";
                } else {
                    tipoIzquierdo = "INT";
                }

            }
        }
        return tipoIzquierdo;
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
            // Si no existe, la misma tablaSimbolos lanzará la excepción
            return (String) tablaSimbolos.get(varName);
        }

        // Si es una expresión entre paréntesis (PAR_OPEN expression PAR_CLOSE)
        if (ctx.expression() != null) {
            // Evaluamos recursivamente el tipo de la expresión interna
            return visit(ctx.expression());
        }
        return null;
    }
}
