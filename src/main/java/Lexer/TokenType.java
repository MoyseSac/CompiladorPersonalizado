package Lexer;

// - Define todos los tipos de token posibles del lenguaje.
//  token.type == TokenType.IF) en lugar de comparar texto.
public enum TokenType {
    // Palabras reservadas
    STRING, BOOL, INTEGER,          // Tipos de datos
    IF, ELSE,                       // Condicionales
    FOR, WHILE, DO,                 // Bucles
    FUNCTION, RETURN, RETURNS, VOID,// Funciones
    TRUE, FALSE,                    // Literales booleanos

    // Identificadores y literales
    IDENTIFIER,                     // Nombres de variables/funciones
    NUMBER_LITERAL,                 // 123, 45.67
    STRING_LITERAL,                 // "hola"

    // Operadores
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, // + - * / %
    ASSIGN,                         // =
    EQUAL, NOT_EQUAL,               // == !=
    LESS, LESS_EQUAL,               // < <=
    GREATER, GREATER_EQUAL,         // > >=
    AND, OR, NOT,                   // && || !

    // Delimitadores
    SEMICOLON,                      // ;
    COMMA,                          // ,
    LPAREN, RPAREN,                 // ( )
    LBRACE, RBRACE,                 // { 
    
    PRINT,
    // Especiales
    EOF,                            // Fin de archivo
    UNKNOWN                         // Para errores
}