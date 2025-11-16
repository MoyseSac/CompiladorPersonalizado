
package Parser;
import Utils.Position;
public class  VariableNode extends ASTNodo {
    public String name;
    
    public VariableNode(String name, Position position) {
        super(position);
        this.name = name;
    }
    
    @Override
    public String toString() {
        return String.format("Variable(%s)", name);
    }
}