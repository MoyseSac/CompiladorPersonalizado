package Semantic;

import Parser.*;
import SymbolTable.Symbol;
import SymbolTable.SymbolTable;
import Utils.CompilerError;
import Utils.ErrorCode;
import Utils.ErrorMessages;
import java.util.*;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable;
    private final List<CompilerError> errors;
    private String currentFunctionReturnType;
    private boolean insideLoop;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
        this.currentFunctionReturnType = null;
        this.insideLoop = false;
    }

    public void analyze(ProgramNode program) {
        if (program == null) return;
        for (ASTNodo statement : program.statements) analyzeNode(statement);
        checkUnusedVariables();
    }

    private void analyzeNode(ASTNodo node) {
        if (node == null) return;

        if (node instanceof VarDeclarationNode) {
            analyzeVarDeclaration((VarDeclarationNode) node);
        } else if (node instanceof FunctionDeclarationNode) {
            analyzeFunctionDeclaration((FunctionDeclarationNode) node);
        } else if (node instanceof AssignmentNode) {
            analyzeAssignment((AssignmentNode) node);
        } else if (node instanceof IfStatementNode) {
            analyzeIfStatement((IfStatementNode) node);
        } else if (node instanceof WhileStatementNode) {
            analyzeWhileStatement((WhileStatementNode) node);
        } else if (node instanceof DoWhileStatementNode) {
            analyzeDoWhileStatement((DoWhileStatementNode) node);
        } else if (node instanceof ForStatementNode) {
            analyzeForStatement((ForStatementNode) node);
        } else if (node instanceof ReturnStatementNode) {
            analyzeReturnStatement((ReturnStatementNode) node);
        } else if (node instanceof FunctionCallNode) {
            analyzeFunctionCall((FunctionCallNode) node);
        } else if (node instanceof BinaryOpNode) {
            analyzeBinaryOp((BinaryOpNode) node);
        } else if (node instanceof UnaryOpNode) {
            analyzeUnaryOp((UnaryOpNode) node);
        } else if (node instanceof VariableNode) {
            analyzeVariable((VariableNode) node);
        } 
    }

    // ================= Declaraciones =================

    private void analyzeVarDeclaration(VarDeclarationNode node) {
    boolean existsHere = symbolTable.existsInCurrentScope(node.name);

    if (!existsHere) {
        symbolTable.declare(
            node.name,
            node.type,
            Symbol.SymbolKind.VARIABLE,
            node.getPosition()
        );
    } else {
        Symbol s = symbolTable.lookup(node.name);
        if (s != null && s.getType() != null && !s.getType().equals(node.type)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(
                    ErrorCode.TYPE_MISMATCH_DECLARATION,
                    node.name,     // '%s' 1
                    s.getType(),   // '%s' 2
                    node.type      // '%s' 3
                ),
                node.getPosition()
            ));
        }
    }

    if (node.initializer != null) {
        String initType = getExpressionType(node.initializer);
        if (initType != null && !isTypeCompatible(node.type, initType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(
                    ErrorCode.TYPE_MISMATCH_ASSIGNMENT,
                    initType,       // '%s' 1
                    node.name,      // '%s' 2
                    node.type       // '%s' 3
                ),
                node.getPosition()
            ));
        }
        analyzeNode(node.initializer);

        Symbol s = symbolTable.lookup(node.name);
        if (s != null) s.setInitialized(true);
    }
}


    private void analyzeFunctionDeclaration(FunctionDeclarationNode node) {
        String previousFunctionType = currentFunctionReturnType;
        currentFunctionReturnType = node.returnType;

        symbolTable.enterScope();
        try {
            for (ParameterNode param : node.parameters) {
                if (!symbolTable.existsInCurrentScope(param.name)) {
                    symbolTable.declare(param.name, param.type, Symbol.SymbolKind.PARAMETER, param.getPosition());
                }
                Symbol psym = symbolTable.lookup(param.name);
                if (psym != null) {
                    psym.setInitialized(true);
                    psym.setUsed(false);
                }
            }

            for (ASTNodo statement : node.body) analyzeNode(statement);

            if (!"void".equals(node.returnType)) {
                boolean hasReturn = hasReturnStatement(node.body);
                if (!hasReturn) {
                    errors.add(new CompilerError(
                        CompilerError.ErrorPhase.SEMANTIC,
                        ErrorMessages.get(
                            ErrorCode.FUNCTION_MUST_RETURN_VALUE,
                            currentFunctionReturnType
                        ),
                        node.getPosition()
                    ));
                }
            }
        } finally {
            symbolTable.exitScope();
            currentFunctionReturnType = previousFunctionType;
        }
    }

    private boolean hasReturnStatement(List<ASTNodo> statements) {
        if (statements == null) return false;
        for (ASTNodo st : statements) {
            if (st instanceof ReturnStatementNode) return true;
            if (st instanceof IfStatementNode) {
                IfStatementNode ifn = (IfStatementNode) st;
                boolean thenHas = hasReturnStatement(ifn.thenBlock);
                boolean elseHas = (ifn.elseBlock != null) && hasReturnStatement(ifn.elseBlock);
                if (thenHas && elseHas) return true;
            }
        }
        return false;
    }

    // ================= Sentencias =================

    private void analyzeAssignment(AssignmentNode node) {
        Symbol symbol = symbolTable.lookup(node.variableName);
        if (symbol == null) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.VAR_NOT_DECLARED, node.variableName),
                node.getPosition()
            ));
        }

        String valueType = getExpressionType(node.value);
        if (symbol != null && valueType != null && !isTypeCompatible(symbol.getType(), valueType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.TYPE_MISMATCH_ASSIGNMENT, 
                valueType,
                node.variableName,
                symbol.getType()
                ),   
                node.getPosition()
            ));
        }

        analyzeNode(node.value);

        if (symbol != null) {
            symbol.setInitialized(true);
            symbol.setUsed(true);
        }
    }

    private void analyzeIfStatement(IfStatementNode node) {
        String conditionType = getExpressionType(node.condition);
        if (conditionType != null && !"bool".equals(conditionType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.IF_CONDITION_NOT_BOOL),
                node.getPosition()
            ));
        }
        analyzeNode(node.condition);

        symbolTable.enterScope();
        try {
            for (ASTNodo st : node.thenBlock) analyzeNode(st);
        } finally {
            symbolTable.exitScope();
        }

        if (node.elseBlock != null) {
            symbolTable.enterScope();
            try {
                for (ASTNodo st : node.elseBlock) analyzeNode(st);
            } finally {
                symbolTable.exitScope();
            }
        }
    }

    private void analyzeWhileStatement(WhileStatementNode node) {
        String conditionType = getExpressionType(node.condition);
        if (conditionType != null && !"bool".equals(conditionType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.WHILE_CONDITION_NOT_BOOL),
                node.getPosition()
            ));
        }
        analyzeNode(node.condition);

        boolean wasInsideLoop = insideLoop;
        insideLoop = true;

        symbolTable.enterScope();
        try {
            for (ASTNodo st : node.body) analyzeNode(st);
        } finally {
            symbolTable.exitScope();
            insideLoop = wasInsideLoop;
        }
    }

    private void analyzeDoWhileStatement(DoWhileStatementNode node) {
        boolean wasInsideLoop = insideLoop;
        insideLoop = true;

        symbolTable.enterScope();
        try {
            for (ASTNodo st : node.body) analyzeNode(st);
        } finally {
            symbolTable.exitScope();
            insideLoop = wasInsideLoop;
        }

        String conditionType = getExpressionType(node.condition);
        if (conditionType != null && !"bool".equals(conditionType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.DO_WHILE_CONDITION_NOT_BOOL),
                node.getPosition()
            ));
        }
        analyzeNode(node.condition);
    }

    private void analyzeForStatement(ForStatementNode node) {
        symbolTable.enterScope();
        try {
            if (node.initialization != null) analyzeNode(node.initialization);

            String conditionType = getExpressionType(node.condition);
            if (conditionType != null && !"bool".equals(conditionType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(ErrorCode.FOR_CONDITION_NOT_BOOL),
                    node.getPosition()
                ));
            }
            analyzeNode(node.condition);

            if (node.increment != null) analyzeNode(node.increment);

            boolean wasInsideLoop = insideLoop;
            insideLoop = true;
            try {
                for (ASTNodo st : node.body) analyzeNode(st);
            } finally {
                insideLoop = wasInsideLoop;
            }
        } finally {
            symbolTable.exitScope();
        }
    }

    private void analyzeReturnStatement(ReturnStatementNode node) {
        if (currentFunctionReturnType == null) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.RETURN_OUTSIDE_FUNCTION),
                node.getPosition()
            ));
            return;
        }

        if (node.value == null && !"void".equals(currentFunctionReturnType)) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(
                    ErrorCode.FUNCTION_MUST_RETURN_VALUE,
                    currentFunctionReturnType
                ),   
                node.getPosition()
            ));
            return;
        }

        if (node.value != null) {
            String returnType = getExpressionType(node.value);
            if ("void".equals(currentFunctionReturnType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                     ErrorMessages.get(ErrorCode.FUNCTION_VOID_CANNOT_RETURN_VALUE),
                    node.getPosition()
                ));
            } else if (returnType != null && !isTypeCompatible(currentFunctionReturnType, returnType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                        ErrorCode.TYPE_MISMATCH_RETURN,
                        currentFunctionReturnType,
                        returnType
                    ),
                    node.getPosition()
                ));
            }
            analyzeNode(node.value);
        }
    }

    private void analyzeFunctionCall(FunctionCallNode node) {
    Symbol symbol = symbolTable.lookup(node.functionName);
    if (symbol == null) {
        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SEMANTIC,
            ErrorMessages.get(
                ErrorCode.FUNCTION_NOT_DECLARED,
                node.functionName
            ),
            node.getPosition()
        ));
    } else if (symbol.getKind() != Symbol.SymbolKind.FUNCTION) {
        errors.add(new CompilerError(
            CompilerError.ErrorPhase.SEMANTIC,
            ErrorMessages.get(
                ErrorCode.NOT_A_FUNCTION,
                node.functionName
            ),
            node.getPosition()
        ));
    } else {
        if (node.arguments.size() != symbol.getParameterCount()) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(
                    ErrorCode.FUNCTION_ARG_COUNT_MISMATCH,
                    node.functionName,
                    symbol.getParameterCount(),
                    node.arguments.size()
                ),
                node.getPosition()
            ));
        }
    }

    for (ASTNodo arg : node.arguments) analyzeNode(arg);
}




    // ================= Expresiones =================

    private void analyzeBinaryOp(BinaryOpNode node) {
        analyzeNode(node.left);
        analyzeNode(node.right);

        String leftType = getExpressionType(node.left);
        String rightType = getExpressionType(node.right);
        if (leftType == null || rightType == null) return;

        String op = node.operator;

        if (op.equals("+")) {
            boolean okString = "string".equals(leftType) && "string".equals(rightType);
            boolean okInt    = "integer".equals(leftType) && "integer".equals(rightType);
            if (!okString && !okInt) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                    ErrorCode.PLUS_INVALID_TYPES,
                    leftType, rightType
                ),
                    node.getPosition()
                ));
            }
        }
        else if (op.matches("[-*/%]")) {
            if (!"integer".equals(leftType) || !"integer".equals(rightType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(ErrorCode.ARITHMETIC_REQUIRES_INT, leftType, rightType),
                    node.getPosition()
                ));
            }
        }
        else if (op.matches("(<|<=|>|>=)")) {
            if (!"integer".equals(leftType) || !"integer".equals(rightType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                        ErrorCode.COMPARISON_REQUIRES_INT,
                        leftType, rightType
                    ),
                    node.getPosition()
                ));
            }
        }

        else if (op.matches("(==|!=)")) {
            if (!leftType.equals(rightType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                        ErrorCode.CANNOT_COMPARE_TYPES, 
                        leftType, rightType
                    ),
                    node.getPosition()
                ));
            }
        }
        else if (op.matches("(&&|\\|\\|)")) {
            if (!"bool".equals(leftType) || !"bool".equals(rightType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                        ErrorCode.LOGICAL_REQUIRES_BOOL,
                        leftType, rightType
                     ),   
                    node.getPosition()
                ));
            }
        }
    }

    private void analyzeUnaryOp(UnaryOpNode node) {
        analyzeNode(node.operand);
        String operandType = getExpressionType(node.operand);
        if (operandType == null) return;

        if ("!".equals(node.operator)) {
            if (!"bool".equals(operandType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                    ErrorCode.NOT_REQUIRES_BOOL,
                    operandType
                ),
                    node.getPosition()
                ));
            }
        } else if ("-".equals(node.operator)) {
            if (!"integer".equals(operandType)) {
                errors.add(new CompilerError(
                    CompilerError.ErrorPhase.SEMANTIC,
                    ErrorMessages.get(
                        ErrorCode.UNARY_MINUS_REQUIRES_INT,
                        operandType
                    ),    
                    node.getPosition()
                ));
            }
        }
    }

    private void analyzeVariable(VariableNode node) {
        Symbol symbol = symbolTable.lookup(node.name);
        if (symbol == null) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.VAR_NOT_DECLARED, node.name),
                node.getPosition()
            ));
            return;
        }

        if (symbol.getKind() != Symbol.SymbolKind.PARAMETER && !symbol.isInitialized()) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(ErrorCode.VAR_MAYBE_UNINITIALIZED, node.name),
                node.getPosition()
            ));
        }

        symbol.setUsed(true);
    }

    // ================= Tipos y utilidades =================

    private String getExpressionType(ASTNodo node) {
        if (node == null) return null;

        if (node instanceof LiteralNode) {
            return ((LiteralNode) node).type;
        }
        if (node instanceof VariableNode) {
            Symbol s = symbolTable.lookup(((VariableNode) node).name);
            return (s != null) ? s.getType() : null;
        }
        if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            String op = bin.operator;

            String lt = getExpressionType(bin.left);
            String rt = getExpressionType(bin.right);
            if (lt == null || rt == null) return null;

            if (op.equals("+")) {
                if ("string".equals(lt) && "string".equals(rt)) return "string";
                if ("integer".equals(lt) && "integer".equals(rt)) return "integer";
                return null;
            }

            if (op.matches("[-*/%]")) return "integer";

            if (op.matches("(<|<=|>|>=|==|!=)")) return "bool";

            if (op.matches("(&&|\\|\\|)")) return "bool";

            return null;
        }
        if (node instanceof UnaryOpNode) {
            UnaryOpNode un = (UnaryOpNode) node;
            if ("!".equals(un.operator)) return "bool";
            if ("-".equals(un.operator)) return "integer";
        }
        if (node instanceof FunctionCallNode) {
            Symbol s = symbolTable.lookup(((FunctionCallNode) node).functionName);
            return (s != null) ? s.getReturnType() : null;
        }

        return null;
    }

    private boolean isTypeCompatible(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }

    private void checkUnusedVariables() {
        List<Symbol> unused = symbolTable.getUnusedSymbols();
        for (Symbol s : unused) {
            errors.add(new CompilerError(
                CompilerError.ErrorPhase.SEMANTIC,
                ErrorMessages.get(
                    ErrorCode.VAR_DECLARED_NEVER_USED,
                    s.getName()
                ),
                s.getDeclarationPosition()
            ));
        }
    }

    public List<CompilerError> getErrors() {
        return errors;
    }
}