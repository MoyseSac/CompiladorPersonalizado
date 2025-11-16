package Main;

import Lexer.*;
import Parser.*;
import Semantic.SemanticAnalyzer;
import SymbolTable.SymbolTable;
import SymbolTable.Symbol;
import Utils.CompilerError;
import java.util.*;
import Runtime.Interpreter;
import Utils.Language;
import Utils.ErrorMessages;

// Flujo: Código fuente → Léxico → Tokens → Sintáctico → AST → Semántico → Resultados
//                  ↓                     ↓               ↓
//            Errores léxicos      Errores sintácticos  Errores semánticos
public class Compiler {
    private String sourceCode;
    private Lexer lexer;
    private Parser parser;
    private SemanticAnalyzer semanticAnalyzer;
    private SymbolTable symbolTable;
    private Interpreter interpreter;

    // Resultados
    private List<Token> tokens;
    private ProgramNode ast;
    private List<CompilerError> allErrors;

    public Compiler(String sourceCode) {
        this.sourceCode = sourceCode;
        this.allErrors = new ArrayList<>();
    }

    // Ejecuta todas las fases. Devuelve true si no hubo errores.
    public boolean compile() {
        allErrors.clear();
        ErrorMessages.setLanguage(Language.SPANISH);
         
        // Fase 1: Análisis léxico
        System.out.println("=== FASE 1: ANÁLISIS LÉXICO ===");
        lexer = new Lexer(sourceCode);
        tokens = lexer.tokenize();
        allErrors.addAll(lexer.getErrors());

        System.out.println("Tokens generados: " + tokens.size());
        System.out.println("Errores léxicos: " + lexer.getErrors().size());

        // Fase 2: Análisis sintáctico
        System.out.println("\n=== FASE 2: ANÁLISIS SINTÁCTICO ===");
        symbolTable = new SymbolTable();
        parser = new Parser(tokens, symbolTable);
        ast = parser.parse();
        allErrors.addAll(parser.getErrors());

        System.out.println("Errores sintácticos: " + parser.getErrors().size());

        // Fase 3: Análisis semántico
        System.out.println("\n=== FASE 3: ANÁLISIS SEMÁNTICO ===");
        semanticAnalyzer = new SemanticAnalyzer(symbolTable);
        semanticAnalyzer.analyze(ast);
        allErrors.addAll(semanticAnalyzer.getErrors());

        System.out.println("Errores semánticos: " + semanticAnalyzer.getErrors().size());

        // Resumen
        System.out.println("\n=== RESUMEN DE COMPILACIÓN ===");
        System.out.println("Errores totales: " + allErrors.size());

        return allErrors.isEmpty();
    }

    // Reporte del análisis léxico para mostrar en UI (JTextArea/diálogo)
    public String getLexicalAnalysisReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RESULTADOS DEL ANÁLISIS LÉXICO ===\n\n");
        report.append(String.format("Tokens totales: %d\n\n", tokens.size()));

        report.append("SECUENCIA DE TOKENS:\n");
        report.append("─".repeat(80)).append("\n");

        int count = 0;
        for (Token token : tokens) {
            if (token.type == TokenType.EOF) break;

            report.append(String.format("%3d. %-20s %-15s Linea:%-3d Col:%-3d\n",
                ++count,
                token.type,
                "'" + token.value + "'",
                token.position.line,
                token.position.column
            ));
        }

        report.append("─".repeat(80)).append("\n");

        // Errores léxicos
        List<CompilerError> lexicalErrors = getErrorsByPhase(CompilerError.ErrorPhase.LEXICAL);
        if (!lexicalErrors.isEmpty()) {
            report.append("\nERRORES LÉXICOS:\n");
            for (CompilerError error : lexicalErrors) {
                report.append("  • ").append(error.toString()).append("\n");
            }
        } else {
            report.append("\n✓ No se encontraron errores léxicos.\n");
        }

        return report.toString();
    }

    // Reporte de errores sintácticos
    public String getSyntaxErrorsReport() {
        return CompilerError.formatErrorsByPhase(allErrors, CompilerError.ErrorPhase.SYNTAX);
    }

    // Reporte de errores semánticos
    public String getSemanticErrorsReport() {
        return CompilerError.formatErrorsByPhase(allErrors, CompilerError.ErrorPhase.SEMANTIC);
    }

    // Reporte consolidado de todos los errores
    public String getAllErrorsReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TODOS LOS ERRORES DE COMPILACIÓN ===\n\n");

        if (allErrors.isEmpty()) {
            report.append("✓ Compilación exitosa. No se encontraron errores.\n");
            return report.toString();
        }

        // Agrupar por fase
        Map<CompilerError.ErrorPhase, List<CompilerError>> errorsByPhase = new HashMap<>();
        for (CompilerError error : allErrors) {
            errorsByPhase.computeIfAbsent(error.getPhase(), k -> new ArrayList<>()).add(error);
        }

        // Imprimir por fase
        for (CompilerError.ErrorPhase phase : CompilerError.ErrorPhase.values()) {
            List<CompilerError> errors = errorsByPhase.get(phase);
            if (errors != null && !errors.isEmpty()) {
                report.append(String.format("─── ERRORES DE %s (%d) ───\n", phase, errors.size()));
                for (CompilerError error : errors) {
                    report.append(error.toString()).append("\n");
                }
                report.append("\n");
            }
        }

        report.append(String.format("Total: %d error(es).\n", allErrors.size()));
        return report.toString();
    }

    // Reporte de la tabla de símbolos (variables y funciones declaradas)
    public String getSymbolTableReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TABLA DE SÍMBOLOS ===\n\n");

        List<Symbol> symbols = symbolTable.getAllSymbols();

        if (symbols.isEmpty()) {
            report.append("No hay símbolos declarados.\n");
            return report.toString();
        }

        report.append(String.format("Símbolos totales: %d\n\n", symbols.size()));
        report.append("─".repeat(100)).append("\n");
        report.append(String.format("%-20s %-15s %-12s %-20s %-12s %-10s\n",
            "NOMBRE", "TIPO", "CLASE", "DECLARADO EN", "INICIALIZADO", "USADO"));
        report.append("─".repeat(100)).append("\n");

        for (Symbol symbol : symbols) {
            report.append(String.format("%-20s %-15s %-12s %-20s %-12s %-10s\n",
                symbol.getName(),
                symbol.getType(),
                symbol.getKind(),
                symbol.getDeclarationPosition().toString(),
                symbol.isInitialized() ? "Sí" : "No",
                symbol.isUsed() ? "Sí" : "No"
            ));
        }

        report.append("─".repeat(100)).append("\n");

        return report.toString();
    }

    // Estado de éxito de compilación
    public boolean isSuccessful() {
        return allErrors.isEmpty();
    }

    public List<String> run() {
        if (ast == null) {
            throw new IllegalStateException("No hay AST disponible. Llama a compile() primero.");
        }

        interpreter = new Interpreter();
        interpreter.execute(ast);

        return interpreter.getOutput();
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }
    
    // Errores por fase específica
    public List<CompilerError> getErrorsByPhase(CompilerError.ErrorPhase phase) {
        List<CompilerError> phaseErrors = new ArrayList<>();
        for (CompilerError error : allErrors) {
            if (error.getPhase() == phase) {
                phaseErrors.add(error);
            }
        }
        return phaseErrors;
    }

    // Acceso a la lista de tokens (para mostrar la secuencia)
    public List<Token> getTokens() {
        return tokens;
    }

    // Acceso al AST (para visualizaciones avanzadas)
    public ProgramNode getAST() {
        return ast;
    }

    // Acceso a la tabla de símbolos
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    

}