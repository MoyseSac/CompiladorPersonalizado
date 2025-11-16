
package Parser;
import Utils.Position;
import java.util.List;

public class DoWhileStatementNode extends ASTNodo {
    public List<ASTNodo> body;
    public ASTNodo condition;
    
    public DoWhileStatementNode(List<ASTNodo> body, ASTNodo condition, Position position) {
        super(position);
        this.body = body;
        this.condition = condition;
    }
    
    @Override
    public String toString() {
        return "DoWhileStatement";
    }
}
  
