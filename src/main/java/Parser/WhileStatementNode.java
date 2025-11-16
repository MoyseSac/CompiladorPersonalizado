package Parser;
import Utils.Position; 
import java.util.List;


public class WhileStatementNode extends ASTNodo {
    public ASTNodo condition;
    public List<ASTNodo> body;
    
    public WhileStatementNode(ASTNodo condition, List<ASTNodo> body, Position position) {
        super(position);
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "WhileStatement";
    }
}
