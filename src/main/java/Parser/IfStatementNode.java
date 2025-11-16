
package Parser;
import Utils.Position;
import java.util.List;

public class IfStatementNode extends ASTNodo {
    public ASTNodo condition;
    public List<ASTNodo> thenBlock;
    public List<ASTNodo> elseBlock;  // can be null
    
    public IfStatementNode(ASTNodo condition, List<ASTNodo> thenBlock, 
                          List<ASTNodo> elseBlock, Position position) {
        super(position);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
    
    @Override
    public String toString() {
        return "IfStatement";
    }
}
