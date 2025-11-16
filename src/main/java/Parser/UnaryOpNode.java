package Parser;
import Utils.Position; 

public class UnaryOpNode extends ASTNodo {
    public String operator;  // !, -
    public ASTNodo operand;
    
    public UnaryOpNode(String operator, ASTNodo operand, Position position) {
        super(position);
        this.operator = operator;
        this.operand = operand;
    }
    
    @Override
    public String toString() {
        return String.format("UnaryOp(%s)", operator);
    }
}
