package Utils;

public class CompilerError {
    public enum ErrorPhase {
        LEXICAL,
        SYNTAX,
        SEMANTIC
    }
    
    private ErrorPhase phase;   // Fase del error
    private String message;     // Mensaje
    private Position position;  // Posición en el código
    
    // Construcción desde texto de fase (p.ej. "Lexical")
    public CompilerError(String phase, String message, Position position) {
        this.phase = ErrorPhase.valueOf(phase.toUpperCase());
        this.message = message;
        this.position = position;
    }
    
    // Construcción desde enum
    public CompilerError(ErrorPhase phase, String message, Position position) {
        this.phase = phase;
        this.message = message;
        this.position = position;
    }
    
    public ErrorPhase getPhase() { return phase; }
    public String getMessage() { return message; }
    public Position getPosition() { return position; }
    
    // Formato legible del error
    @Override
    public String toString() {
        return String.format("[ERROR - %s] Línea %d, Col %d: %s",
            faseEtiqueta(phase), position.line, position.column, message);
    }
    
    // Formatea errores filtrando por fase
    public static String formatErrorsByPhase(java.util.List<CompilerError> errors, ErrorPhase phase) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== ERRORES DE %s ===\n\n", faseEtiqueta(phase)));
        
        int count = 0;
        for (CompilerError error : errors) {
            if (error.getPhase() == phase) {
                sb.append(error.toString()).append("\n");
                count++;
            }
        }
        
        if (count == 0) {
            sb.append("No se encontraron errores en esta fase.\n");
        } else {
            sb.append(String.format("\nTotal: %d error(es)\n", count));
        }
        return sb.toString();
    }

    // Etiqueta en español para la fase
    private static String faseEtiqueta(ErrorPhase phase) {
        switch (phase) {
            case LEXICAL:   return "ANÁLISIS LÉXICO";
            case SYNTAX:    return "ANÁLISIS SINTÁCTICO";
            case SEMANTIC:  return "ANÁLISIS SEMÁNTICO";
            default:        return phase.toString();
        }
    }
}