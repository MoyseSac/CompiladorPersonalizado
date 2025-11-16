// Lexer/Lexer.java
package Lexer;

import Utils.Position;
import Utils.CompilerError;
import java.util.*;

// Convertimos el texto fuente en TOKENS
public class Lexer {
    private String sourceCode;
    private int currentIndex;
    private char currentChar;
    private Position position;
    private List<CompilerError> errors;
    
    // Diccionario de palabras reservadas
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        // Tipos de datos
        KEYWORDS.put("tzij", TokenType.STRING);       // string
        KEYWORDS.put("kolik", TokenType.BOOL);        // bool
        KEYWORDS.put("ajilabal", TokenType.INTEGER);  // integer
        // Control de flujo
        KEYWORDS.put("we", TokenType.IF);             // if
        KEYWORDS.put("manan", TokenType.ELSE);        // else
        KEYWORDS.put("chik", TokenType.FOR);          // for
        KEYWORDS.put("kam", TokenType.WHILE);         // while
        KEYWORDS.put("kut", TokenType.DO);            // do
        // Funciones
        KEYWORDS.put("samaj", TokenType.FUNCTION);    // function
        KEYWORDS.put("tzol", TokenType.RETURN);       // return
        KEYWORDS.put("tzolil", TokenType.RETURNS);    // returns
        KEYWORDS.put("maj", TokenType.VOID);          // void
        // Booleanos
        KEYWORDS.put("je", TokenType.TRUE);           // true
        KEYWORDS.put("man", TokenType.FALSE);         // false
        KEYWORDS.put("wach", TokenType.PRINT);
    }
    
    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        this.currentIndex = 0;
        this.currentChar = sourceCode.length() > 0 ? sourceCode.charAt(0) : '\0';
        this.position = new Position(1, 1);
        this.errors = new ArrayList<>();
    }
    
    private void advance() {
        position.advance(currentChar);
        currentIndex++;
        currentChar = currentIndex < sourceCode.length() ? sourceCode.charAt(currentIndex) : '\0';
    }
    
    private char peek() {
        int peekIndex = currentIndex + 1;
        return peekIndex < sourceCode.length() ? sourceCode.charAt(peekIndex) : '\0';
    }
    
    private void skipWhitespace() {
        // Saltar espacios en blanco
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }
    
    private Token makeNumber() {
        Position startPos = position.copy();
        StringBuilder number = new StringBuilder();
        boolean hasDecimal = false;
        
        while (currentChar != '\0' && (Character.isDigit(currentChar) || currentChar == '.')) {
            if (currentChar == '.') {
                if (hasDecimal) {
                    // Mantener la fase como "Lexical" para no romper compatibilidad
                    errors.add(new CompilerError("Lexical", 
                        "Formato de número inválido: múltiples puntos decimales", position.copy()));
                    break;
                }
                hasDecimal = true;
            }
            number.append(currentChar);
            advance();
        }
        
        return new Token(TokenType.NUMBER_LITERAL, number.toString(), startPos);
    }
    
    private Token makeIdentifierOrKeyword() {
        Position startPos = position.copy();
        StringBuilder identifier = new StringBuilder();
        
        while (currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            identifier.append(currentChar);
            advance();
        }
        
        String value = identifier.toString();
        TokenType type = KEYWORDS.getOrDefault(value.toLowerCase(), TokenType.IDENTIFIER);
        
        return new Token(type, value, startPos);
    }
    
    private Token makeString() {
        Position startPos = position.copy();
        StringBuilder string = new StringBuilder();
        advance(); // Saltar la comilla inicial
        
        while (currentChar != '\0' && currentChar != '"') {
            if (currentChar == '\\' && peek() == '"') {
                advance();
                string.append('"');
                advance();
            } else {
                string.append(currentChar);
                advance();
            }
        }
        
        if (currentChar == '\0') {
            errors.add(new CompilerError("Lexical", 
                "Cadena de texto sin cerrar", startPos));
            return new Token(TokenType.UNKNOWN, string.toString(), startPos);
        }
        
        advance(); // Saltar la comilla de cierre
        return new Token(TokenType.STRING_LITERAL, string.toString(), startPos);
    }
    
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        while (currentChar != '\0') {
            skipWhitespace();
            if (currentChar == '\0') break;
            
            Position tokenPos = position.copy();
            
            // Números
            if (Character.isDigit(currentChar)) {
                tokens.add(makeNumber());
            }
            // Identificadores y palabras reservadas
            else if (Character.isLetter(currentChar) || currentChar == '_') {
                tokens.add(makeIdentifierOrKeyword());
            }
            // Literales de cadena
            else if (currentChar == '"') {
                tokens.add(makeString());
            }
            // Operadores y delimitadores
            else if (currentChar == '+') {
                tokens.add(new Token(TokenType.PLUS, "+", tokenPos));
                advance();
            }
            else if (currentChar == '-') {
                tokens.add(new Token(TokenType.MINUS, "-", tokenPos));
                advance();
            }
            else if (currentChar == '*') {
                tokens.add(new Token(TokenType.MULTIPLY, "*", tokenPos));
                advance();
            }
            else if (currentChar == '/') {
                tokens.add(new Token(TokenType.DIVIDE, "/", tokenPos));
                advance();
            }
            else if (currentChar == '%') {
                tokens.add(new Token(TokenType.MODULO, "%", tokenPos));
                advance();
            }
            else if (currentChar == '=') {
                if (peek() == '=') {
                    tokens.add(new Token(TokenType.EQUAL, "==", tokenPos));
                    advance();
                    advance();
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", tokenPos));
                    advance();
                }
            }
            else if (currentChar == '!') {
                if (peek() == '=') {
                    tokens.add(new Token(TokenType.NOT_EQUAL, "!=", tokenPos));
                    advance();
                    advance();
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", tokenPos));
                    advance();
                }
            }
            else if (currentChar == '<') {
                if (peek() == '=') {
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", tokenPos));
                    advance();
                    advance();
                } else {
                    tokens.add(new Token(TokenType.LESS, "<", tokenPos));
                    advance();
                }
            }
            else if (currentChar == '>') {
                if (peek() == '=') {
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", tokenPos));
                    advance();
                    advance();
                } else {
                    tokens.add(new Token(TokenType.GREATER, ">", tokenPos));
                    advance();
                }
            }
            else if (currentChar == '&' && peek() == '&') {
                tokens.add(new Token(TokenType.AND, "&&", tokenPos));
                advance();
                advance();
            }
            else if (currentChar == '|' && peek() == '|') {
                tokens.add(new Token(TokenType.OR, "||", tokenPos));
                advance();
                advance();
            }
            else if (currentChar == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", tokenPos));
                advance();
            }
            else if (currentChar == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", tokenPos));
                advance();
            }
            else if (currentChar == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", tokenPos));
                advance();
            }
            else if (currentChar == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", tokenPos));
                advance();
            }
            else if (currentChar == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{", tokenPos));
                advance();
            }
            else if (currentChar == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}", tokenPos));
                advance();
            }
            else {
                errors.add(new CompilerError("Lexical", 
                    "Carácter no reconocido: '" + currentChar + "'", tokenPos));
                advance();
            }
        }
        
        tokens.add(new Token(TokenType.EOF, "", position.copy()));
        return tokens;
    }
    
    public List<CompilerError> getErrors() {
        return errors;
    }
}