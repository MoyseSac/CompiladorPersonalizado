package Runtime;

import Parser.*;
import java.util.*;

public class Interpreter {

    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();
    private final Map<String, FunctionDeclarationNode> functions = new HashMap<>();
    private final List<String> output = new ArrayList<>();

    public Interpreter() {
        pushScope(); // scope global
    }

    public List<String> getOutput() {
        return output;
    }

    private void pushScope() {
        scopes.push(new HashMap<>());
    }

    private void popScope() {
        scopes.pop();
    }

    private Map<String, Object> currentScope() {
        return scopes.peek();
    }

    private Object getVariable(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Variable '" + name + "' no encontrada en tiempo de ejecución");
    }

    private void setVariable(String name, Object value) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        // Si no existe, la creamos en el scope actual
        currentScope().put(name, value);
    }

    private void declareVariable(String name, Object value) {
        currentScope().put(name, value);
    }

    // ================= Ejecución principal =================

    public void execute(ProgramNode program) {
        if (program == null) return;

        // 1. Registrar funciones
        for (ASTNodo st : program.statements) {
            if (st instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode fn = (FunctionDeclarationNode) st;
                functions.put(fn.name, fn);
            }
        }

        // 2. Ejecutar sentencias "top-level" que no sean funciones
        for (ASTNodo st : program.statements) {
            if (!(st instanceof FunctionDeclarationNode)) {
                executeStatement(st);
            }
        }
    }

    // ================= Sentencias =================

    private void executeStatement(ASTNodo node) {
        if (node instanceof VarDeclarationNode) {
            executeVarDeclaration((VarDeclarationNode) node);
        } else if (node instanceof AssignmentNode) {
            executeAssignment((AssignmentNode) node);
        } else if (node instanceof IfStatementNode) {
            executeIf((IfStatementNode) node);
        } else if (node instanceof WhileStatementNode) {
            executeWhile((WhileStatementNode) node);
        } else if (node instanceof DoWhileStatementNode) {
            executeDoWhile((DoWhileStatementNode) node);
        } else if (node instanceof ForStatementNode) {
            executeFor((ForStatementNode) node);
        } else if (node instanceof ReturnStatementNode) {
                ReturnStatementNode ret = (ReturnStatementNode) node;
        throw new ReturnException(
            ret.value != null ? evaluateExpression(ret.value) : null
        );
        } else if (node instanceof FunctionCallNode) {
            evaluateFunctionCall((FunctionCallNode) node);
        } 
//        else if (node instanceof PrintStatementNode) {
//            executePrint((PrintStatementNode) node);
//        }
    }
    
    private void executeVarDeclaration(VarDeclarationNode node) {
        Object value = null;
        if (node.initializer != null) {
            value = evaluateExpression(node.initializer);
        }
        declareVariable(node.name, value);
    }

    private void executeAssignment(AssignmentNode node) {
        Object value = evaluateExpression(node.value);
        setVariable(node.variableName, value);
    }

    private void executeIf(IfStatementNode node) {
        Object condValue = evaluateExpression(node.condition);
        if (!(condValue instanceof Boolean)) {
            throw new RuntimeException("Condición de if no es bool en tiempo de ejecución");
        }

        if ((Boolean) condValue) {
            pushScope();
            try {
                for (ASTNodo st : node.thenBlock) {
                    executeStatement(st);
                }
            } finally {
                popScope();
            }
        } else if (node.elseBlock != null) {
            pushScope();
            try {
                for (ASTNodo st : node.elseBlock) {
                    executeStatement(st);
                }
            } finally {
                popScope();
            }
        }
    }

    private void executeWhile(WhileStatementNode node) {
        while (true) {
            Object condValue = evaluateExpression(node.condition);
            if (!(condValue instanceof Boolean)) {
                throw new RuntimeException("Condición de while no es bool en tiempo de ejecución");
            }
            if (!((Boolean) condValue)) break;

            pushScope();
            try {
                for (ASTNodo st : node.body) {
                    executeStatement(st);
                }
            } finally {
                popScope();
            }
        }
    }

    private void executeDoWhile(DoWhileStatementNode node) {
        do {
            pushScope();
            try {
                for (ASTNodo st : node.body) {
                    executeStatement(st);
                }
            } finally {
                popScope();
            }

            Object condValue = evaluateExpression(node.condition);
            if (!(condValue instanceof Boolean)) {
                throw new RuntimeException("Condición de do-while no es bool en tiempo de ejecución");
            }
            if (!((Boolean) condValue)) break;
        } while (true);
    }

    private void executeFor(ForStatementNode node) {
        pushScope();
        try {
            if (node.initialization != null) {
                executeStatement(node.initialization);
            }

            while (true) {
                Object condValue = evaluateExpression(node.condition);
                if (!(condValue instanceof Boolean)) {
                    throw new RuntimeException("Condición de for no es bool en tiempo de ejecución");
                }
                if (!((Boolean) condValue)) break;

                pushScope();
                try {
                    for (ASTNodo st : node.body) {
                        executeStatement(st);
                    }
                } finally {
                    popScope();
                }

                if (node.increment != null) {
                    executeStatement(node.increment);
                }
            }
        } finally {
            popScope();
        }
    }
    
    private static class ReturnException extends RuntimeException {
        private final Object value;

        public ReturnException(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }
    
    private Object evaluateFunctionCall(FunctionCallNode node) {
        FunctionDeclarationNode fn = functions.get(node.functionName);
        if (fn == null) {
            throw new RuntimeException("Función '" + node.functionName + "' no encontrada en tiempo de ejecución");
        }

        // Evaluar argumentos
        List<Object> argValues = new ArrayList<>();
        for (ASTNodo arg : node.arguments) {
            argValues.add(evaluateExpression(arg));
        }

        // Nuevo scope para la función
        pushScope();
        try {
            // Parámetros
            for (int i = 0; i < fn.parameters.size(); i++) {
                ParameterNode param = fn.parameters.get(i);
                Object argVal = i < argValues.size() ? argValues.get(i) : null;
                declareVariable(param.name, argVal);
            }

            // Ejecutar cuerpo
            try {
                for (ASTNodo st : fn.body) {
                    executeStatement(st);
                }
            } catch (ReturnException re) {
                return re.getValue();
            }

            // Si la función no retorna nada explícitamente
            return null;
        } finally {
            popScope();
        }
    }
    

    private Object evaluateExpression(ASTNodo node) {
        if (node instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) node;
            switch (lit.type) {
                case "integer":
                    // Parser le pasó un String
                    return Integer.parseInt((String) lit.value);
                case "bool":
                    // Parser le pasó un boolean, que llega como Boolean
                    return (Boolean) lit.value;
                case "string":
                    // Parser le pasó un String
                    return (String) lit.value;
                default:
                    return null;
            }
        }

        if (node instanceof VariableNode) {
            VariableNode var = (VariableNode) node;
            return getVariable(var.name);
        }

        if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            Object lv = evaluateExpression(bin.left);
            Object rv = evaluateExpression(bin.right);
            String op = bin.operator;

            if (op.equals("+")) {
                if (lv instanceof String && rv instanceof String) {
                    return (String) lv + (String) rv;
                }
                if (lv instanceof Integer && rv instanceof Integer) {
                    return (Integer) lv + (Integer) rv;
                }
                throw new RuntimeException("Operador + inválido en tiempo de ejecución");
            }

            if (op.equals("-")) {
                return (Integer) lv - (Integer) rv;
            }
            if (op.equals("*")) {
                return (Integer) lv * (Integer) rv;
            }
            if (op.equals("/")) {
                return (Integer) lv / (Integer) rv;
            }
            if (op.equals("%")) {
                return (Integer) lv % (Integer) rv;
            }

            if (op.equals("<"))  return (Integer) lv <  (Integer) rv;
            if (op.equals("<=")) return (Integer) lv <= (Integer) rv;
            if (op.equals(">"))  return (Integer) lv >  (Integer) rv;
            if (op.equals(">=")) return (Integer) lv >= (Integer) rv;

            if (op.equals("==")) return Objects.equals(lv, rv);
            if (op.equals("!=")) return !Objects.equals(lv, rv);

            if (op.equals("&&")) return (Boolean) lv && (Boolean) rv;
            if (op.equals("||")) return (Boolean) lv || (Boolean) rv;

            throw new RuntimeException("Operador binario desconocido: " + op);
        }

        if (node instanceof UnaryOpNode) {
            UnaryOpNode un = (UnaryOpNode) node;
            Object v = evaluateExpression(un.operand);
            if (un.operator.equals("!")) {
                return !((Boolean) v);
            }
            if (un.operator.equals("-")) {
                return -((Integer) v);
            }
            throw new RuntimeException("Operador unario desconocido: " + un.operator);
        }

        if (node instanceof FunctionCallNode) {
            return evaluateFunctionCall((FunctionCallNode) node);
        }

        return null;
    }
 
}



    


