package Utils;

// Guarda la posición actual en el código fuente.
public class Position {
    public int line;
    public int column;
    
    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    public Position copy() {
        return new Position(line, column);
    }
    
    // Avanza una posición considerando saltos de línea.
    public void advance(char currentChar) {
        if (currentChar == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Linea:%d Col:%d", line, column);
    }
}