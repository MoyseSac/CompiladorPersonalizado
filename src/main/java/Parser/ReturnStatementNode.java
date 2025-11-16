package Parser;
import Utils.Position;

public class ReturnStatementNode extends ASTNodo {
    public ASTNodo value;  // can be null for void functions
    
    public ReturnStatementNode(ASTNodo value, Position position) {
        super(position);
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "ReturnStatement";
    }
}