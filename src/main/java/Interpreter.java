
public class Interpreter extends GramaticaBaseVisitor<Object>{

    // tabla para guardar las variables y su valor
    private final SymbolTable memoria = new SymbolTable();

    @Override
    public Object visitProgram(GramaticaParser.ProgramContext ctx) {
        // Le decimos al intérprete que ejecute todas las sentencias dentro de las llaves
        for (GramaticaParser.SentenceContext sentenceCtx : ctx.sentence()) {
            visit(sentenceCtx);
        }
        return null;
    }

    @Override
    public Object visitSentence(GramaticaParser.SentenceContext ctx) {
        if (ctx.println() != null) {
            return visit(ctx.println());
        }
        // Si es una declaración de variable, reservamos memoria
        if (ctx.varDecl() != null) {
            return visit(ctx.varDecl());
        }
        // Si es una asignación de variable, la ejecutamos
        if (ctx.varAssign() != null) {
            return visit(ctx.varAssign());
        }
        if (ctx.conditional() != null) {
            return visit(ctx.conditional());
        }

        if (ctx.doWhile() != null) {
            return visit(ctx.doWhile());
        }
        return null;
    }

    @Override
    public Object visitPrintln(GramaticaParser.PrintlnContext ctx) {
        Object valor = visit(ctx.expression()); // Resolvemos la matemática
        System.out.println(valor); // ¡Lo mostramos!
        return null;
    }

    @Override
    public Object visitVarDecl(GramaticaParser.VarDeclContext ctx) {
        String nombreVar = ctx.ID().getText();

        // Inicializamos en null dentro de la memoria real
        memoria.declare(nombreVar, null);

        return null;
    }

    @Override
    public Object visitVarAssign(GramaticaParser.VarAssignContext ctx) {
        String nombreVar = ctx.ID().getText();

        // Evaluamos la expresión de la derecha para obtener el valor real (Integer, Boolean, etc.)
        Object valorReal = visit(ctx.expression());

        // Guardamos el valor real en tu memoria reemplazando el null inicial
        // El metodo assign ya tira error si la variable no existe en tiempo de ejecución
        memoria.assign(nombreVar, valorReal);

        return null;
    }

    @Override
    public Object visitExpression(GramaticaParser.ExpressionContext ctx) {
        // Metodo puente: simplemente delega en orExpr
        return visit(ctx.orExpr());
    }

    @Override
    public Object visitOrExpr(GramaticaParser.OrExprContext ctx) {
        // Acá se muda la ejecución real del OR (||)
        Object resultado = visit(ctx.andExpr(0));

        if (ctx.andExpr().size() > 1) {
            for (int i = 1; i < ctx.andExpr().size(); i++) {
                Object operandoDerecho = visit(ctx.andExpr(i));
                resultado = (Boolean) resultado || (Boolean) operandoDerecho;
            }
        }
        return resultado;
    }

    @Override
    public Object visitAndExpr(GramaticaParser.AndExprContext ctx) {
        // 1. Obtenemos el valor real del primer componente
        Object resultado = visit(ctx.comparisonExpr(0));

        // 2. Si hay operadores "&&", evaluamos iterativamente
        if (ctx.comparisonExpr().size() > 1) {
            for (int i = 1; i < ctx.comparisonExpr().size(); i++) {
                Object operandoDerecho = visit(ctx.comparisonExpr(i));

                // Operación lógica AND real de Java
                resultado = (Boolean) resultado && (Boolean) operandoDerecho;
            }
        }

        return resultado;
    }

    @Override
    public Object visitComparisonExpr(GramaticaParser.ComparisonExprContext ctx) {
        Object resultado = visit(ctx.arithmeticExpr(0));

        if (ctx.arithmeticExpr().size() > 1) {
            for (int i = 1; i < ctx.arithmeticExpr().size(); i++) {
                Object operandoDerecho = visit(ctx.arithmeticExpr(i));
                String operador = ctx.getChild(i * 2 - 1).getText();

                // Detectamos si estamos comparando números y si alguno involucra un Double/Float
                boolean esFloat = (resultado instanceof Double || operandoDerecho instanceof Double);
                boolean sonNumeros = (resultado instanceof Number && operandoDerecho instanceof Number);

                switch (operador) {
                    case "<":
                        if (esFloat) {
                            resultado = ((Number) resultado).doubleValue() < ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = (Integer) resultado < (Integer) operandoDerecho;
                        }
                        break;
                    case "<=":
                        if (esFloat) {
                            resultado = ((Number) resultado).doubleValue() <= ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = (Integer) resultado <= (Integer) operandoDerecho;
                        }
                        break;
                    case ">":
                        if (esFloat) {
                            resultado = ((Number) resultado).doubleValue() > ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = (Integer) resultado > (Integer) operandoDerecho;
                        }
                        break;
                    case ">=":
                        if (esFloat) {
                            resultado = ((Number) resultado).doubleValue() >= ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = (Integer) resultado >= (Integer) operandoDerecho;
                        }
                        break;
                    case "==":
                        if (sonNumeros) {
                            resultado = ((Number) resultado).doubleValue() == ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = resultado.equals(operandoDerecho);
                        }
                        break;
                    case "!=":
                        if (sonNumeros) {
                            resultado = ((Number) resultado).doubleValue() != ((Number) operandoDerecho).doubleValue();
                        } else {
                            resultado = !resultado.equals(operandoDerecho);
                        }
                        break;
                }
            }
        }

        return resultado;
    }

    @Override
    public Object visitArithmeticExpr(GramaticaParser.ArithmeticExprContext ctx) {
        // 1. Obtenemos el valor real del primer término
        Object resultado = visit(ctx.multiplicativeExpr(0));

        // 2. Si hay más términos, iteramos sobre ellos
        if (ctx.multiplicativeExpr().size() > 1) {
            for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
                Object operandoDerecho = visit(ctx.multiplicativeExpr(i));

                // Obtenemos el token del operador (+ o -)
                String operador = ctx.getChild(i * 2 - 1).getText();

                // Verificamos si alguno de los dos lados es de tipo Double (Float de nuestro lenguaje)
                boolean esFloat = (resultado instanceof Double || operandoDerecho instanceof Double);

                if ("+".equals(operador)) {
                    if (esFloat) {
                        double a = (resultado instanceof Integer) ? (Integer) resultado : (Double) resultado;
                        double b = (operandoDerecho instanceof Integer) ? (Integer) operandoDerecho : (Double) operandoDerecho;
                        resultado = a + b;
                    } else {
                        resultado = (Integer) resultado + (Integer) operandoDerecho;
                    }
                } else if ("-".equals(operador)) {
                    if (esFloat) {
                        double a = (resultado instanceof Integer) ? (Integer) resultado : (Double) resultado;
                        double b = (operandoDerecho instanceof Integer) ? (Integer) operandoDerecho : (Double) operandoDerecho;
                        resultado = a - b;
                    } else {
                        resultado = (Integer) resultado - (Integer) operandoDerecho;
                    }
                }
            }
        }

        return resultado;
    }

    @Override
    public Object visitMultiplicativeExpr(GramaticaParser.MultiplicativeExprContext ctx) {
        // 1. Obtenemos el valor del primer término
        Object resultado = visit(ctx.unaryExpr(0));

        // 2. Si hay más de un término, iteramos sobre los operadores y los siguientes operandos
        if (ctx.unaryExpr().size() > 1) {
            for (int i = 1; i < ctx.unaryExpr().size(); i++) {
                Object operandoDerecho = visit(ctx.unaryExpr(i));

                // Obtenemos el operador correspondiente (MULT '*' o DIV '/')
                // ctx.getChild(i*2 - 1) nos da el token del operador en el árbol
                String operador = ctx.getChild(i * 2 - 1).getText();

                // Verificamos si estamos trabajando con enteros o flotantes
                boolean esFloat = (resultado instanceof Double || operandoDerecho instanceof Double);

                if ("*".equals(operador)) {
                    if (esFloat) {
                        double a = (resultado instanceof Integer) ? (Integer) resultado : (Double) resultado;
                        double b = (operandoDerecho instanceof Integer) ? (Integer) operandoDerecho : (Double) operandoDerecho;
                        resultado = a * b;
                    } else {
                        resultado = (Integer) resultado * (Integer) operandoDerecho;
                    }
                } else if ("/".equals(operador)) {
                    if (esFloat) {
                        double a = (resultado instanceof Integer) ? (Integer) resultado : (Double) resultado;
                        double b = (operandoDerecho instanceof Integer) ? (Integer) operandoDerecho : (Double) operandoDerecho;
                        if (b == 0.0) throw new RuntimeException("Error en ejecución: División por cero.");
                        resultado = a / b;
                    } else {
                        int b = (Integer) operandoDerecho;
                        if (b == 0) throw new RuntimeException("Error en ejecución: División por cero.");
                        resultado = (Integer) resultado / b;
                    }
                }
            }
        }

        return resultado;
    }

    @Override
    public Object visitUnaryExpr(GramaticaParser.UnaryExprContext ctx) {
        // Caso 1: Negación lógica con '!'
        if (ctx.NOT() != null) {
            Object valorHijo = visit(ctx.unaryExpr());
            return !(Boolean) valorHijo;
        }

        // Caso 2: Menos unario con '-'
        if (ctx.MINUS() != null) {
            Object valorHijo = visit(ctx.unaryExpr());

            // Dependiendo de si Java lo interpretó como Integer o Double, cambiamos el signo
            if (valorHijo instanceof Integer) {
                return -(Integer) valorHijo;
            } else if (valorHijo instanceof Double) {
                return -(Double) valorHijo;
            }
        }

        // Caso base: Si no hay operador unario, resolvemos el 'term' [cite: 12]
        return visit(ctx.term());
    }

    @Override
    public Object visitTerm(GramaticaParser.TermContext ctx) {
        // Retornar el valor entero como Integer de Java
        if (ctx.INT() != null) {
            return Integer.parseInt(ctx.INT().getText());
        }

        // Retornar el valor flotante como Double de Java
        if (ctx.FLOAT() != null) {
            return Double.parseDouble(ctx.FLOAT().getText());
        }

        // Retornar el valor booleano como Boolean de Java
        if (ctx.BOOLEAN() != null) {
            return Boolean.parseBoolean(ctx.BOOLEAN().getText());
        }

        // Retornar la cadena de texto quitando las comillas iniciales y finales
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText();
            return str.substring(1, str.length() - 1);
        }

        // Retornar el valor actual de la variable desde la tabla de símbolos
        if (ctx.ID() != null) {
            String varName = ctx.ID().getText();
            return memoria.get(varName);
        }

        // Si es una expresión entre paréntesis, resolverla recursivamente
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }

        return null;
    }

    @Override
    public Object visitBlock(GramaticaParser.BlockContext ctx) {
        for (GramaticaParser.SentenceContext sentenceCtx : ctx.sentence()) {
            visit(sentenceCtx);
        }
        return null;
    }

    @Override
    public Object visitConditional(GramaticaParser.ConditionalContext ctx) {
        Boolean condition = (Boolean) visit(ctx.expression());

        if (condition) {
            visit(ctx.ifBlock);
        } else {
            visit(ctx.elseBlock);
        }

        return null;
    }
    @Override
    public Object visitDoWhile(GramaticaParser.DoWhileContext ctx) {

        do {
            for (GramaticaParser.SentenceContext s : ctx.sentence()) {
                visit(s);
            }
        } while ((Boolean) visit(ctx.expression()));

        return null;
    }


}
