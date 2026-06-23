grammar Gramatica;

// Reglas
program: PROGRAM ID BRACKET_OPEN
            sentence*
        BRACKET_CLOSE;

sentence: println
        | conditional
        | doWhile // pedido por la catedra
        | varDecl
        | varAssign;

println: PRINTLN expression SEMICOLON;

conditional: IF PAR_OPEN expression PAR_CLOSE
                ifBlock=block
             ELSE
                elseBlock=block;

block: BRACKET_OPEN sentence* BRACKET_CLOSE;

//*******************************

doWhile: DO BRACKET_OPEN
            sentence*
         BRACKET_CLOSE
         WHILE PAR_OPEN expression PAR_CLOSE SEMICOLON;

//*******************************

varDecl: VAR ID SEMICOLON;
varAssign: ID ASSIGN expression SEMICOLON;

expression: orExpr;

orExpr: andExpr
            ( OR andExpr )*;

andExpr: comparisonExpr
            ( AND comparisonExpr )*;

comparisonExpr: arithmeticExpr
                ( EQ arithmeticExpr
                | GT arithmeticExpr
                | LT  arithmeticExpr
                | GEQ arithmeticExpr
                | LEQ arithmeticExpr
                | NEQ arithmeticExpr )*;

arithmeticExpr: multiplicativeExpr
                (PLUS multiplicativeExpr
                | MINUS multiplicativeExpr )*;

multiplicativeExpr: unaryExpr
                    ( MULT unaryExpr
                    | DIV  unaryExpr )*;

unaryExpr: NOT unaryExpr
            | MINUS unaryExpr
            | term;

term: INT
        | FLOAT
        | BOOLEAN
        | STRING
        | ID
        | PAR_OPEN expression PAR_CLOSE;

// Tokens
PROGRAM: 'program';
VAR: 'var';
PRINTLN: 'println';

IF: 'if';
ELSE: 'else';

DO: 'do';
WHILE: 'while';

PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';

AND: '&&';
OR: '||';
NOT: '!';

GT: '>';
LT: '<';
GEQ: '>=';
LEQ: '<=';
EQ: '==';
NEQ: '!=';

ASSIGN: '=';

BRACKET_OPEN: '{';
BRACKET_CLOSE: '}';

PAR_OPEN: '(';
PAR_CLOSE: ')';
SEMICOLON: ';';

BOOLEAN: 'true' | 'false';
ID: [a-zA-Z][a-zA-Z0-9_]*;
INT: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]* | '.' [0-9]+;
STRING: '"' (~["\\\r\n'] | '\\' .)* '"';

LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;

WS: [ \t\r\n]+ -> skip;
