grammar Rule;
prog:   expr EOF ;
expr:   '!' expr
    |   expr ('>'|'<'|'>='|'<='|'=='|'!=') expr
    |   expr ('&&'|'||') expr
    |   BOOL
    |   INT
    |   VAR
    |   '(' expr ')'
    ;

SPACE   : [ \t\r\n]+ -> skip;
BOOL    : 'true' | 'false';
INT     : [0-9]+ ;
VAR     : 'm.'?[a-zA-Z_][a-zA-Z_0-9]*;
