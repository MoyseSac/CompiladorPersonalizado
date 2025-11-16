package Lexer;

import Utils.Position;

// Representa un token del código fuente.
// Guarda la POSICIÓN para poder decir "Error en línea 5, columna 12".
// Almacena el TIPO (palabra reservada, identificador, etc.) y el VALOR (texto real).
public class Token {
    public TokenType type;
    public String value;
    public Position position;  // Dónde se encontró este token
    
    public Token(TokenType type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }
    
    @Override
    public String toString() {
        return String.format("Token(%s, '%s', Linea:%d Col:%d)",
            type, value, position.line, position.column);
    }
}