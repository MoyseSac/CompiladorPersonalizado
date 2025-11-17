// Parser/PrintStatementNode.java
package Parser;

import Utils.Position;

public class PrintStatementNode extends ASTNodo {
    public final ASTNodo expression;

    public PrintStatementNode(ASTNodo expression, Position position) {
        super(position);
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "Print(" + (expression != null ? expression.toString() : "") + ")";
    }
}
