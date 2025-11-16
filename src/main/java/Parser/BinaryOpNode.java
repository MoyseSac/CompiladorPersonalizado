
package Parser;
import Utils.Position;

public class BinaryOpNode extends ASTNodo {
    public ASTNodo left;
    public String operator;  // +, -, *, /, ==, !=, <, >, &&, ||
    public ASTNodo right;
    
    public BinaryOpNode(ASTNodo left, String operator, ASTNodo right, Position position) {
        super(position);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    @Override
    public String toString() {
        return String.format("BinaryOp(%s)", operator);
    }
}