// SymbolTable/SymbolTable.java
package SymbolTable;

import Utils.Position;
import java.util.*;

// Tabla de símbolos: maneja alcances, evita redeclaraciones y guarda tipos.
public class SymbolTable {
    private Stack<Map<String, Symbol>> scopes;
    private int currentScopeLevel;
    
    private List<Symbol> allSymbolsEverDeclared;
    
    public SymbolTable() {
        this.scopes = new Stack<>();
        this.currentScopeLevel = 0;
        this.allSymbolsEverDeclared = new ArrayList<>();
        enterScope(); // Ámbito global
    }
    
    // Entra a un nuevo ámbito (por ejemplo, al abrir { )
    public void enterScope() {
        scopes.push(new HashMap<>());
        currentScopeLevel++;
    }
    
    // Sale del ámbito actual (por ejemplo, al cerrar } )
    public void exitScope() {
        if (scopes.size() > 1) { // Mantener el ámbito global
            scopes.pop();
            currentScopeLevel--;
        }
    }
    
    // Declara un símbolo en el ámbito actual. Retorna false si ya existe en este ámbito.
    public boolean declare(String name, String type, Symbol.SymbolKind kind, Position position) {
        Map<String, Symbol> currentScope = scopes.peek();
        
        // Verificar redeclaración en el ámbito actual
        if (currentScope.containsKey(name)) {
            return false;
        }
        
        Symbol symbol = new Symbol(name, type, kind, position, currentScopeLevel);
        currentScope.put(name, symbol);
        return true;
    }
    
    // Busca un símbolo por nombre desde el ámbito más interno al más externo.
    public Symbol lookup(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, Symbol> scope = scopes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // No encontrado
    }
    
    // Verifica si el símbolo existe solo en el ámbito actual.
    public boolean existsInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }
    
    // Obtiene todos los símbolos visibles actualmente (para mostrar la tabla).
    public List<Symbol> getAllSymbols() {
        List<Symbol> allSymbols = new ArrayList<>();
        for (Map<String, Symbol> scope : scopes) {
            allSymbols.addAll(scope.values());
        }
        return allSymbols;
    }
    
    // Obtiene variables no usadas (para advertencias).
    public List<Symbol> getUnusedSymbols() {
        List<Symbol> unused = new ArrayList<>();
        for (Symbol symbol : getAllSymbols()) {
            if (!symbol.isUsed() && symbol.getKind() == Symbol.SymbolKind.VARIABLE) {
                unused.add(symbol);
            }
        }
        return unused;
    }
    
    public int getCurrentScopeLevel() {
        return currentScopeLevel;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TABLA DE SÍMBOLOS ===\n");
        for (int i = 0; i < scopes.size(); i++) {
            sb.append(String.format("Nivel de alcance %d:\n", i));
            for (Symbol symbol : scopes.get(i).values()) {
                sb.append("  ").append(symbol).append("\n");
            }
        }
        return sb.toString();
    }
}