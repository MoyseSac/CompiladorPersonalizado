
package Parser;
import Utils.Position;

public class AssignmentNode extends ASTNodo {
    public String variableName;
    public ASTNodo value;
    
    public AssignmentNode(String variableName, ASTNodo value, Position position) {
        super(position);
        this.variableName = variableName;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format("Assignment(%s = ...)", variableName);
    }
}
