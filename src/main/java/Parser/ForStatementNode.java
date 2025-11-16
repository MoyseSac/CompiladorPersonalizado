package Parser;
import Utils.Position;
import java.util.List;


public class ForStatementNode extends ASTNodo {
    public ASTNodo initialization;  // can be VarDecl or Assignment
    public ASTNodo condition;
    public ASTNodo increment;
    public List<ASTNodo> body;
    
    public ForStatementNode(ASTNodo initialization, ASTNodo condition, 
                           ASTNodo increment, List<ASTNodo> body, Position position) {
        super(position);
        this.initialization = initialization;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "ForStatement";
    }
}
