package SymbolTable;

import Utils.Position;

public class Symbol {
    public enum SymbolKind {
        VARIABLE,
        FUNCTION,
        PARAMETER
    }
    
    private String name;                 // Identificador
    private String type;                 // Tipo declarado
    private SymbolKind kind;             // Clase del símbolo
    private Position declarationPosition;// Posición de declaración
    private int scopeLevel;              // Nivel de alcance
    private boolean isInitialized;       // Si fue inicializado
    private boolean isUsed;              // Si ha sido usado
    
    // Atributos para funciones
    private String returnType;           // Tipo de retorno
    private int parameterCount;          // Cantidad de parámetros
    
    // Inicializa el símbolo
    public Symbol(String name, String type, SymbolKind kind, Position declarationPosition, int scopeLevel) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.declarationPosition = declarationPosition;
        this.scopeLevel = scopeLevel;
        this.isInitialized = false;
        this.isUsed = false;
    }
    
    // Accesores
    public String getName() { return name; }
    public String getType() { return type; }
    public SymbolKind getKind() { return kind; }
    public Position getDeclarationPosition() { return declarationPosition; }
    public int getScopeLevel() { return scopeLevel; }
    public boolean isInitialized() { return isInitialized; }
    public void setInitialized(boolean initialized) { this.isInitialized = initialized; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { this.isUsed = used; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public int getParameterCount() { return parameterCount; }
    public void setParameterCount(int count) { this.parameterCount = count; }
    
    @Override
    public String toString() {
        return String.format("Symbol(%s, type=%s, declared at %s, scope=%d)", 
            name, type, declarationPosition, scopeLevel);
    }
}