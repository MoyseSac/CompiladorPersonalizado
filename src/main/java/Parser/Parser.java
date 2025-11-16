// Parser/Parser.java
package Parser;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.Symbol;
import SymbolTable.SymbolTable;
import Utils.CompilerError;
import Utils.Position;
import java.util.*;

/**
 * Analizador sintáctico (Parser)
 * - Verifica gramática
 * - Construye el AST
 * - Registra símbolos
 * - Acumula errores
 */
public class Parser {
    private List<Token> tokens;
    private int currentIndex;
    private Token currentToken;
    private final List<CompilerError> errors;
    private final SymbolTable symbolTable;

    public Parser(List<Token> tokens, SymbolTable symbolTable) {
        // Defensa por si llega null o vacío
        if (tokens == null || tokens.isEmpty()) {
            this.tokens = Collections.singletonList(new Token(TokenType.EOF, "", new Position(1, 1)));
        } else {
            this.tokens = tokens;
        }
        this.currentIndex = 0;
        this.currentToken = this.tokens.get(0);
        this.errors = new ArrayList<>();
        this.symbolTable = symbolTable;
    }

    // ============== NAVEGACIÓN DE TOKENS ==============

    private void advance() {
        if (currentIndex < tokens.size() - 1) {
            currentIndex++;
        }
        currentToken = tokens.get(currentIndex);
    }

    private Token peek(int offset) {
        int index = currentIndex + offset;
        if (index >= 0 && index < tokens.size()) {
            return tokens.get(index);
        }
        return tokens.get(Math.min(currentIndex, tokens.size() - 1));
    }

    private boolean expect(TokenType type) {
        if (currentToken.type == type) {
            advance();
            return true;
        }
        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SYNTAX,
            String.format("Se esperaba '%s' pero se encontró '%s'", type, currentToken.type),
            currentToken.position
        ));
        return false;
    }

    // ============== PUNTO DE ENTRADA ==============

    public ProgramNode parse() {
        List<ASTNodo> statements = new ArrayList<>();

        while (currentToken.type != TokenType.EOF) {
            try {
                ASTNodo stmt = parseStatement();
                if (stmt != null) {
                    statements.add(stmt);
                } else {
                    recoverFromError();
                }
            } catch (Exception ex) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SYNTAX,
                    "Error de parseo: " + ex.getMessage(),
                    currentToken.position
                ));
                recoverFromError();
            }
        }

        return new ProgramNode(statements, new Position(1, 1));
    }

    private void recoverFromError() {
        while (currentToken.type != TokenType.EOF
            && currentToken.type != TokenType.SEMICOLON
            && currentToken.type != TokenType.RBRACE) {
            advance();
        }
        if (currentToken.type == TokenType.SEMICOLON) {
            advance();
        }
    }

    // ============== SENTENCIAS ==============

    private ASTNodo parseStatement() {
        if (isType(currentToken.type)) {
            return parseVarDeclaration();
        }
        if (currentToken.type == TokenType.FUNCTION) {
            return parseFunctionDeclaration();
        }
        if (currentToken.type == TokenType.IF) {
            return parseIfStatement();
        }
        if (currentToken.type == TokenType.WHILE) {
            return parseWhileStatement();
        }
        if (currentToken.type == TokenType.DO) {
            return parseDoWhileStatement();
        }
        if (currentToken.type == TokenType.FOR) {
            return parseForStatement();
        }
        if (currentToken.type == TokenType.RETURN) {
            return parseReturnStatement();
        }
        if (currentToken.type == TokenType.IDENTIFIER) {
            return parseAssignmentOrCall();
        }

        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SYNTAX,
            "Token inesperado: " + currentToken.value,
            currentToken.position
        ));
        advance();
        return null;
    }

    // wach ( expr ) ;

    // <tipo> <nombre> [= expr] ;
    private VarDeclarationNode parseVarDeclaration() {
        Position startPos = currentToken.position;

        if (!isType(currentToken.type)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SYNTAX,
                "Se esperaba un tipo de dato (integer, string, bool)",
                currentToken.position
            ));
            return null;
        }

        String type = getCanonicalType(currentToken.type);
        advance();

        if (currentToken.type != TokenType.IDENTIFIER) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SYNTAX,
                "Se esperaba un nombre de variable después del tipo",
                currentToken.position
            ));
            return null;
        }

        String varName = currentToken.value;
        Position varPos = currentToken.position;
        advance();

        if (symbolTable.existsInCurrentScope(varName)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SYNTAX,
                String.format("La variable '%s' ya fue declarada en este alcance", varName),
                varPos
            ));
        } else {
            symbolTable.declare(varName, type, Symbol.SymbolKind.VARIABLE, varPos);
        }

        ASTNodo initializer = null;

        if (currentToken.type == TokenType.ASSIGN) {
            advance();
            initializer = parseExpression();

            Symbol s = symbolTable.lookup(varName);
            if (s != null) s.setInitialized(true);
        }

        expect(TokenType.SEMICOLON);
        return new VarDeclarationNode(type, varName, initializer, startPos);
    }

    // function name( [tipo id (, tipo id)* ] ) [returns tipo | void] { ... }
    private FunctionDeclarationNode parseFunctionDeclaration() {
        Position startPos = currentToken.position;
        advance(); // 'function'

        if (currentToken.type != TokenType.IDENTIFIER) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SYNTAX,
                "Se esperaba un nombre de función",
                currentToken.position
            ));
            return null;
        }

        String functionName = currentToken.value;
        Position funcPos = currentToken.position;
        advance();

        expect(TokenType.LPAREN);

        List<ParameterNode> parameters = new ArrayList<>();
        while (currentToken.type != TokenType.RPAREN && currentToken.type != TokenType.EOF) {
            if (!isType(currentToken.type)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SYNTAX,
                    "Se esperaba un tipo de parámetro",
                    currentToken.position
                ));
                break;
            }

            String paramType = getCanonicalType(currentToken.type);
            advance();

            if (currentToken.type != TokenType.IDENTIFIER) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SYNTAX,
                    "Se esperaba un nombre de parámetro",
                    currentToken.position
                ));
                break;
            }

            String paramName = currentToken.value;
            Position paramPos = currentToken.position;
            parameters.add(new ParameterNode(paramType, paramName, paramPos));
            advance();

            if (currentToken.type == TokenType.COMMA) {
                advance();
            }
        }

        expect(TokenType.RPAREN);

        String returnType = "void";
        if (currentToken.type == TokenType.RETURNS) {
            advance();
            if (isType(currentToken.type)) {
                returnType = getCanonicalType(currentToken.type);
                advance();
            } else if (currentToken.type == TokenType.VOID) {
                returnType = "void";
                advance();
            } else {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SYNTAX,
                    "Se esperaba un tipo de retorno o 'void' después de 'returns'",
                    currentToken.position
                ));
            }
        } else if (currentToken.type == TokenType.VOID) {
            returnType = "void";
            advance();
        }

        if (!symbolTable.declare(functionName, returnType, Symbol.SymbolKind.FUNCTION, funcPos)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SYNTAX,
                String.format("La función '%s' ya fue declarada", functionName),
                funcPos
            ));
        } else {
            Symbol f = symbolTable.lookup(functionName);
            f.setReturnType(returnType);
            f.setParameterCount(parameters.size());
        }

        expect(TokenType.LBRACE);
        symbolTable.enterScope();

        for (ParameterNode p : parameters) {
            if (!symbolTable.declare(p.name, p.type, Symbol.SymbolKind.PARAMETER, p.position)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SYNTAX,
                    String.format("El parámetro '%s' ya fue declarado", p.name),
                    p.position
                ));
            }
        }

        List<ASTNodo> body = parseBlock();
        symbolTable.exitScope();
        expect(TokenType.RBRACE);

        return new FunctionDeclarationNode(functionName, returnType, parameters, body, startPos);
    }

    // Lee sentencias hasta '}' o EOF. '{' ya fue consumido.
    private List<ASTNodo> parseBlock() {
        List<ASTNodo> statements = new ArrayList<>();
        while (currentToken.type != TokenType.RBRACE && currentToken.type != TokenType.EOF) {
            ASTNodo s = parseStatement();
            if (s != null) statements.add(s);
            else recoverFromError();
        }
        return statements;
    }

    // if (cond) { ... } [ else { ... } ]
    private IfStatementNode parseIfStatement() {
        Position startPos = currentToken.position;
        advance(); // if

        expect(TokenType.LPAREN);
        ASTNodo condition = parseExpression();
        expect(TokenType.RPAREN);

        expect(TokenType.LBRACE);
        symbolTable.enterScope();
        List<ASTNodo> thenBlock = parseBlock();
        symbolTable.exitScope();
        expect(TokenType.RBRACE);

        List<ASTNodo> elseBlock = null;
        if (currentToken.type == TokenType.ELSE) {
            advance();
            expect(TokenType.LBRACE);
            symbolTable.enterScope();
            elseBlock = parseBlock();
            symbolTable.exitScope();
            expect(TokenType.RBRACE);
        }

        return new IfStatementNode(condition, thenBlock, elseBlock, startPos);
    }

    // while (cond) { ... }
    private WhileStatementNode parseWhileStatement() {
        Position startPos = currentToken.position;
        advance(); // while

        expect(TokenType.LPAREN);
        ASTNodo condition = parseExpression();
        expect(TokenType.RPAREN);

        expect(TokenType.LBRACE);
        symbolTable.enterScope();
        List<ASTNodo> body = parseBlock();
        symbolTable.exitScope();
        expect(TokenType.RBRACE);

        return new WhileStatementNode(condition, body, startPos);
    }

    // do { ... } while (cond);
    private DoWhileStatementNode parseDoWhileStatement() {
        Position startPos = currentToken.position;
        advance(); // do

        expect(TokenType.LBRACE);
        symbolTable.enterScope();
        List<ASTNodo> body = parseBlock();
        symbolTable.exitScope();
        expect(TokenType.RBRACE);

        expect(TokenType.WHILE);
        expect(TokenType.LPAREN);
        ASTNodo condition = parseExpression();
        expect(TokenType.RPAREN);
        expect(TokenType.SEMICOLON);

        return new DoWhileStatementNode(body, condition, startPos);
    }

    // for ( init ; cond ; update ) { ... }
    private ForStatementNode parseForStatement() {
        Position startPos = currentToken.position;
        advance(); // for

        expect(TokenType.LPAREN);

        symbolTable.enterScope();

        ASTNodo init = null;
        if (isType(currentToken.type)) {
            init = parseVarDeclaration(); // consume ';'
        } else if (currentToken.type == TokenType.IDENTIFIER) {
            init = parseAssignmentOrCall(); // consume ';'
        } else {
            expect(TokenType.SEMICOLON); // init vacío
        }

        ASTNodo condition = parseExpression();
        expect(TokenType.SEMICOLON);

        ASTNodo increment = null;
        if (currentToken.type == TokenType.IDENTIFIER) {
            String varName = currentToken.value;
            Position varPos = currentToken.position;
            advance();
            expect(TokenType.ASSIGN);
            ASTNodo value = parseExpression();
            increment = new AssignmentNode(varName, value, varPos);
        }

        expect(TokenType.RPAREN);

        expect(TokenType.LBRACE);
        List<ASTNodo> body = parseBlock();
        expect(TokenType.RBRACE);

        symbolTable.exitScope();

        return new ForStatementNode(init, condition, increment, body, startPos);
    }

    // return [expr] ;
    private ReturnStatementNode parseReturnStatement() {
        Position startPos = currentToken.position;
        advance(); // return

        ASTNodo value = null;
        if (currentToken.type != TokenType.SEMICOLON) {
            value = parseExpression();
        }

        expect(TokenType.SEMICOLON);
        return new ReturnStatementNode(value, startPos);
    }

    // <id> '(' args ')' ';'  |  <id> '=' expr ';'
    private ASTNodo parseAssignmentOrCall() {
        String name = currentToken.value;
        Position startPos = currentToken.position;
        advance(); // IDENTIFIER

        // Llamada a función
        if (currentToken.type == TokenType.LPAREN) {
            advance();
            List<ASTNodo> arguments = new ArrayList<>();

            while (currentToken.type != TokenType.RPAREN && currentToken.type != TokenType.EOF) {
                arguments.add(parseExpression());
                if (currentToken.type == TokenType.COMMA) {
                    advance();
                }
            }

            expect(TokenType.RPAREN);
            expect(TokenType.SEMICOLON);

            Symbol sym = symbolTable.lookup(name);
            if (sym != null) sym.setUsed(true);

            return new FunctionCallNode(name, arguments, startPos);
        }

        // Asignación
        if (currentToken.type == TokenType.ASSIGN) {
            advance();
            ASTNodo value = parseExpression();
            expect(TokenType.SEMICOLON);

            Symbol sym = symbolTable.lookup(name);
            if (sym != null) {
                sym.setUsed(true);
                sym.setInitialized(true);
            }

            return new AssignmentNode(name, value, startPos);
        }

        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SYNTAX,
            "Se esperaba '=' o '(' después del identificador",
            currentToken.position
        ));
        return null;
    }

    // ============== EXPRESIONES (precedencia) ==============

    private ASTNodo parseExpression() { return parseLogicalOr(); }

    private ASTNodo parseLogicalOr() {
        ASTNodo left = parseLogicalAnd();
        while (currentToken.type == TokenType.OR) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseLogicalAnd();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseLogicalAnd() {
        ASTNodo left = parseEquality();
        while (currentToken.type == TokenType.AND) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseEquality();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseEquality() {
        ASTNodo left = parseRelational();
        while (currentToken.type == TokenType.EQUAL || currentToken.type == TokenType.NOT_EQUAL) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseRelational();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseRelational() {
        ASTNodo left = parseAdditive();
        while (currentToken.type == TokenType.LESS
            || currentToken.type == TokenType.LESS_EQUAL
            || currentToken.type == TokenType.GREATER
            || currentToken.type == TokenType.GREATER_EQUAL) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseAdditive();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseAdditive() {
        ASTNodo left = parseMultiplicative();
        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseMultiplicative();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseMultiplicative() {
        ASTNodo left = parseUnary();
        while (currentToken.type == TokenType.MULTIPLY
            || currentToken.type == TokenType.DIVIDE
            || currentToken.type == TokenType.MODULO) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo right = parseUnary();
            left = new BinaryOpNode(left, op, right, pos);
        }
        return left;
    }

    private ASTNodo parseUnary() {
        if (currentToken.type == TokenType.NOT || currentToken.type == TokenType.MINUS) {
            String op = currentToken.value;
            Position pos = currentToken.position;
            advance();
            ASTNodo operand = parseUnary();
            return new UnaryOpNode(op, operand, pos);
        }
        return parsePrimary();
    }

    private ASTNodo parsePrimary() {
        Position pos = currentToken.position;

        if (currentToken.type == TokenType.LPAREN) {
            advance();
            ASTNodo expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        if (currentToken.type == TokenType.NUMBER_LITERAL) {
            String value = currentToken.value;
            advance();
            return new LiteralNode(value, "integer", pos);
        }

        if (currentToken.type == TokenType.STRING_LITERAL) {
            String value = currentToken.value;
            advance();
            return new LiteralNode(value, "string", pos);
        }

        if (currentToken.type == TokenType.TRUE || currentToken.type == TokenType.FALSE) {
            boolean b = currentToken.type == TokenType.TRUE;
            advance();
            return new LiteralNode(b, "bool", pos);
        }

        if (currentToken.type == TokenType.IDENTIFIER) {
            String name = currentToken.value;
            Position idPos = currentToken.position;
            advance();

            if (currentToken.type == TokenType.LPAREN) {
                advance();
                List<ASTNodo> args = new ArrayList<>();
                while (currentToken.type != TokenType.RPAREN && currentToken.type != TokenType.EOF) {
                    args.add(parseExpression());
                    if (currentToken.type == TokenType.COMMA) {
                        advance();
                    }
                }
                expect(TokenType.RPAREN);

                Symbol sym = symbolTable.lookup(name);
                if (sym != null) sym.setUsed(true);

                return new FunctionCallNode(name, args, idPos);
            }

            Symbol sym = symbolTable.lookup(name);
            if (sym != null) sym.setUsed(true);

            return new VariableNode(name, idPos);
        }

        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SYNTAX,
            "Token inesperado en expresión: " + currentToken.value,
            currentToken.position
        ));
        advance();
        return new LiteralNode(0, "integer", pos);
    }

    // ============== AYUDAS ==============

    private boolean isType(TokenType type) {
        return type == TokenType.INTEGER
            || type == TokenType.STRING
            || type == TokenType.BOOL;
    }

    // Sin switch para evitar clase sintética Parser$1
    private String getCanonicalType(TokenType type) {
        if (type == null) return "unknown";
        if (type == TokenType.INTEGER) return "integer";
        if (type == TokenType.STRING)  return "string";
        if (type == TokenType.BOOL)    return "bool";
        if (type == TokenType.VOID)    return "void";
        return "unknown";
    }

    public List<CompilerError> getErrors() {
        return errors;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}