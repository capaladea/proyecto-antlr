
###Conceptos y Paradigmas de Lenguajes de Programación

##TRABAJO PRACTICO - Intérprete con ANTLR
=======================================

###Grupo V - opcion Do While

###Lucas DiBenedetto
###Cristian Paladea


Este documento explica cómo esta diseñado el interprete y como funciona el flujo del código y cómo interactúan los archivos editados para pruebas.

##Desarrollo: Del texto plano al árbol sintactico.

Todo el pocesamiento inicial está automatizado por ANTLR4 a partir de nuestra gramática definida en el archivo **Gramatica.g4**. Este fue desarrollado en base a los videos proporcionados por la catedra. 

Cuando el **Main.java** lee un archivo de pruebas, el flujo de datos sigue este orden:

**El Lexer** (GramaticaLexer): Recibe el archivo de texto plano y lo segmenta en unidades mínimas con significado, tokens (program, operadores como * o +, números o cadenas). También se encarga de ignorar los espacios en blanco, saltos de línea, comentarios, etc.

**Los Tokens**: Son los eslabones que el Lexer le entrega al Parser. Cada token tiene un tipo (ej: INT, MULT, ID) y un texto asociado (ejemplo: "5", "* ", "miVariable").

**El Parser** (GramaticaParser): Toma los tokens y valida que cumplan con el orden y las reglas jerárquicas que escribimos en la gramática. Si el código fuente tiene un error de sintaxis (como olvidarse un punto y coma), el parser frena y avisa con un mensaje de error.

**El ParseTree** (Árbol Sintáctico): Si la sintaxis es correcta, el parser construye un árbol en memoria al invocar la regla raíz con **parser.program()**. Las hojas de este árbol son los tokens físicos y las ramas son las reglas gramaticales (ejemplo: una rama multiplicativeExpr que tiene las hojas: el token 5, el token * y el token 4).

##SymbolTable, SintanticAnalyzer e Interpreter. Lo qué hace cada clase.

Para darle lógica y comportamiento a ese árbol de memoria, se crean esos tres componentes en Java. Dividimos la validación de la ejecución en:

**SymbolTable.java** (Tabla de Símbolos): Es un diccionario (HashMap) donde registramos y consultamos las variables del programa. Es una clase genérica que usamos en dos momentos distintos:
En la fase semántica: guarda el tipo de la variable (ej: "ID_X" -> "INT").
En la fase de ejecución: guarda el valor real de la variable (ej: "ID_X" -> 20).

**SemanticAnalyzer.java** (El Auditor Semántico): Su trabajo es recorrer el árbol asegurándose de que el programa "tenga sentido" antes de ejecutarlo. No calcula resultados, solo valida reglas. Ejemplo: Si ve un !5, frena y lanza un error semántico porque no se puede aplicar una negación lógica a un número entero en una estructura de control if-else.

**Interpreter.java** (El Ejecutor): Corre el programa una vez que el Analizador Semántico dio el visto bueno. Su trabajo es procesar los datos reales y realizar las acciones físicas. Ejemplo: Si ve 5 * 4, realiza la multiplicación y devuelve un Integer con el valor 20. Si ve un println, usa un System.out.println() real para mostrar algun dato en la consola.

##El Patrón Visitor y los Métodos Sobrecargados ¿De dónde salen los métodos visit?

Al compilar la gramática, ANTLR genera automáticamente una clase base llamada GramaticaBaseVisitor<T>. Esta clase contiene un método predefinido por cada regla que escribimos en nuestro archivo .g4 (ej: visitTerm, visitUnaryExpr, conditional, doWhile, etc.).

En nuestras clases aplicamos herencia y sobreescribimos (usando @Override) esos métodos para decirle al compilador qué hacer exactamente cuando el recorrido del árbol pase por esa regla específica: que sume, reste, compare, niege, etc.

Tratamos de escribir los metodos que reflejan las reglas de abajo hacia arriba, desde lo más simple a lo más complejo. Ver el archivo Gramatica.g4.

##¿Como se define una operacion? ¿Como se define un número o un string o un float entonces? ¿Y una sentencia?

La sintaxis no solo define qué caracteres son válidos, sino en qué orden matemático y lógico se deben resolver. Al anidar las reglas una dentro de otra, se logra que el parser de ANTLR construya un árbol sintáctico donde lo que está más abajo en la jerarquía se evalúa primero.

Entonces se diseñó una estructura de reglas anidadas de forma estrictamente vertical para delegar en el propio Parser sintáctico la resolución automática de la precedencia de operadores, evitando la ambigüedad. En esta jerarquía, las reglas con menor prioridad envuelven a las de mayor prioridad.

Con esto logramos que el parser no tenga que adivinar qué operador va primero. El árbol sintáctico crece de manera unidireccional y predecible. Ademas facilitamos el desarrollo en el SemanticAnalyzer, ya que cada método visitante se encarga estrictamente de un tipo de operación (aritmética, relacional o lógica).

###¿Por qué los paréntesis están en term entonces? 

Porque romper la estructura natural de precedencia requiere volver a empezar desde arriba. Al definir **PAR_OPEN expression PAR_CLOSE** dentro de term (ver Gramatica.g4), obligamos al compilador a bajar hasta lo más profundo de la jerarquía para, inmediatamente, 'resetear' el flujo y volver a evaluar una expression completa con la tecnica de prioridad antes explicada.

###Resumen de se estructura la gramatica

Se comienza con la regla **program** y este posee, o no, sentencias definidas como reglas **sentence** de distintos tipos: println, conditional (if-else), doWhile, varDecl (declaracion de variables) y varAssign (asignacion de valores a las variables).

Las sentecias println, conditional y dowhile hacen uso de las expresiones (**expresions**) y las sentencias
(**sentence**). La regla **block** agrupa sentecias.

##Compilacion:

Esta definido como un proyecto de tipo MAVEN.

Se utilizo la linea de compilacion **maven clean compile** (usando Intellij IDEA).


##Ejemplos de uso

Se adjuntan en el directorio del proyecto src/test. Alternativamente aqui se detallan:

###Sin errores

program testDoWhileSimple {

    var M;
    M = 0;

    do {
        println (M);
                
        M = M + 1;
        
        } while (M < 5);
}

program testIfElse{

    var L;
    L = 10;

    if (L > 5) {
        println ("Es mayor a 10");
    } else {
         println ("Es menor a 10");
    }
}


###Con errores

program testErrores{

    // fatla un punto y coma

    var x;
    var 10
    
    println (x);
}

program testComparacion{

    // llamado a println sin definir una variable

    var x;
    x = 10;
    
    println (x > y);
}

program testIfElse{

    // el if no valida una condicion booleana

    var L;
    L = 10;

    if (5) {
        println ("Es mayor a 10");
    } else {
         println ("Es menor a 10");
    }
}

